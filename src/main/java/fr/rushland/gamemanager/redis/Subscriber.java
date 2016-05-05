package fr.rushland.gamemanager.redis;

import com.google.gson.Gson;

import fr.rushland.gamemanager.Game;
import fr.rushland.gamemanager.GameMapOption;
import fr.rushland.gamemanager.Main;
import fr.rushland.gamemanager.server.CreateServer;
import fr.rushland.gamemanager.utils.CodeUtils;

import org.apache.commons.io.FileUtils;
import redis.clients.jedis.JedisPubSub;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * Created by Zaral on 10/04/2016.
 */
public class Subscriber extends JedisPubSub {

    public static HashMap<String, GameMapOption> playerCancelled = new HashMap<>();

    @Override
    public void onMessage(String channel, String message) {
        //Reçois les messages.
        if (channel.equals("RLGamePS")) {
            String[] packet = message.split("#");
            System.out.println("Received packet '" + message + "' from Proxy");
            if (packet[0].equals("members")) {
                String member = packet[2];
                if (packet[1].equals("add")) {
                    Main.partyMembers.add(member);
                } else if (packet[1].equals("remove")) {
                    Main.partyMembers.remove(member);
                }
            } else if (packet[0].equals("slots")) {
                String leader = packet[2];
                int slots = 1;
                if (packet.length > 3) {
                    slots = Integer.parseInt(packet[3]);
                }
                if (packet[1].equals("put")) {
                    Main.partySlotsByLeader.put(leader, slots);
                } else if (packet[1].equals("remove")) {
                    Main.partySlotsByLeader.remove(leader);
                }
            }
        } else if (channel.equals(RedisDataSender.channelSub)) {
            String[] msg = message.split("#");
            if (msg[0].equals("init")) {
                /*           String gameType = msg[1];
                String option = msg[2];
                int maxPlayers = Integer.parseInt(msg[3]);
                String map = msg[4];
                new GameQueue(gameType, maxPlayers, map, option);
                return;
                 */
            } else if (msg[0].equals("findGame")) {
                final String player = msg[1];
                if (Main.partyMembers.contains(player)) {
                    RedisDataSender.getPublisher.publish("say#" + player + "#§cSeul le chef de votre groupe peut rejoindre une file d'attente.");
                    return;
                }
                int requiredSlots = 1;
                if (Main.partySlotsByLeader.containsKey(player)) {
                    requiredSlots = Main.partySlotsByLeader.get(player);
                }

                final String gsonString = msg[2];

                Gson gson = new Gson();

                GameMapOption gameMap = gson.fromJson(gsonString, GameMapOption.class); //Permet de générer la map etc...

                if (gameMap == null) {
                    Logger.getGlobal().severe("Game is null !");
                    return;
                }

                if (requiredSlots > gameMap.getMaxPlayers()) {
                    RedisDataSender.getPublisher.publish("say#" + player + "#§cVotre groupe contient trop de joueurs pour ce mode de jeu.");
                    return;
                }

                RedisDataSender.getPublisher.publish("queuejoinedmsg#" + player + "#" + CodeUtils.formatNPCType(gameMap.getGameType()));

                Game game = Main.findGame(gameMap.getGameOption(), gameMap.getGameType());
                if (game == null) {
                    Logger.getGlobal().severe("Game '" + gameMap.getGameOption() + "' is null, create it.");
                    game = new Game(gameMap.getGameType(), gameMap.getMaxPlayers(), gameMap.getGameOption());
                }

                System.out.println("GameOption: " + game.getOption());
                System.out.println("Game: " + game.getGameType());

                findGame(player, gameMap, game, requiredSlots);

            } else if (msg[0].equals("delete")) {
                String typeGame = msg[1];
                int port = Integer.parseInt(msg[2]);
                System.out.println("Delete Game: " + typeGame);
                System.out.println("Delete Port: " + port);

                Game game = Main.findGame(port);
                if (game == null) {
                    Logger.getGlobal().severe("\nGame '" + typeGame + port + "' is null !");
                    return;
                }
                System.out.println("Option de la game a supprimer: " + game.getOption());
                if (new File(game.getGame(port)).exists()) {
                    try {
                        FileUtils.deleteDirectory(new File(game.getGame(port)));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                game.removeStartedGame(port);
            } else if (msg[0].equals("random")) {
                String playerName = msg[1];
                if (Main.partyMembers.contains(playerName)) {
                    RedisDataSender.getPublisher.publish("say#" + playerName + "#§cSeul le chef de votre groupe peut rejoindre une file d'attente.");
                    return;
                }
                int requiredSlots = 1;
                if (Main.partySlotsByLeader.containsKey(playerName)) {
                    requiredSlots = Main.partySlotsByLeader.get(playerName);
                }
                ArrayList<Game> game = Main.findGame(msg[2]);

                if (game.size() == 0) {
                    RedisDataSender.getPublisher.publish("say#" + playerName + "#Aucun joueur n'est actuellement dans ce mode de jeu, si vous tenez à jouer à ce mode de jeu," +
                            "merci de choisir une option.");

                    return;
                }
                RedisRequestData bestServer = null;
                for (Game typeGame : game) {
                    RedisRequestData data = typeGame.findBetterGame(requiredSlots);
                    if (data == null) break;
                    if (bestServer == null) {
                        bestServer = data;
                    } else {
                        if (bestServer.getOnlinePlayers() < data.getOnlinePlayers()) {
                            bestServer = data;
                        }
                    }
                }
                if (bestServer == null) {
                    Game type = game.get(0);
                    if (type == null) {
                        System.out.println("Error during creation of game");
                        RedisDataSender.getPublisher.publish("say#" + playerName + "#Aucun joueur n'est actuellement en attente dans ce mode de jeu, si vous tenez à jouer à ce mode de jeu," +
                                "merci de choisir une option.");

                        return;
                    }
                    String option = type.getOption();
                    try {
                        GameMapOption gameMap = Main.getOptionByName(option, type.getGameType());
                        if (gameMap == null) {
                            RedisDataSender.getPublisher.publish("say#" + playerName + "#Aucun joueur n'est actuellement en attente dans ce mode de jeu, si vous tenez à jouer à ce mode de jeu," +
                                    "merci de choisir une option.");
                            return;
                        }
                        findGame(playerName, gameMap, type, requiredSlots);
                    } catch (Exception e) {
                        RedisDataSender.getPublisher.publish("say#" + playerName + "#Aucun joueur n'est actuellement en attente dans ce mode de jeu, si vous tenez à jouer à ce mode de jeu," +
                                "merci de choisir une option.");
                    }
                } else {
                    String serverName = bestServer.getServerName();
                    RedisDataSender.getPublisher.publish("send#" + playerName + "#" + serverName);
                }
            }
        }
    }

    public void findGame(final String player, final GameMapOption gameMap, final Game game, final int requiredSlots) {
        String validGame = game.findValidGame(requiredSlots);
        if (validGame == null) {
            if (!game.gameRecentlyCreated()) {
                game.setGameRecentlyCreated();
                int port = 0;
                if (Main.freePort.size() != 0) {
                    port = Main.freePort.get(0);
                    Main.freePort.remove((Object) port);
                } else {
                    port = Main.port;
                    Main.port++;
                }
                final int finalPort = port;
                final Game finalGame = game;
                //Creation du serveur
                System.out.println("Port: " + port);
                final CreateServer newSrv = new CreateServer(game, gameMap, port);
                newSrv.copyGameServer();
                RedisDataSender.getPublisher.publish("createsrv#" + finalGame.getGameType() + "#" + finalPort + "#" + player);
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                newSrv.runServer();
                            }
                        },
                        2000
                        );
            } else {
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                findGame(player, gameMap, game, requiredSlots);
                            }
                        },
                        4000
                        );
            }
        } else {
            System.out.println("Send player to: " + validGame);
            RedisDataSender.getPublisher.publish("send#" + player + "#" + validGame);
        }

    }
}
