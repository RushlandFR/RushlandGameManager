package fr.rushland.main.redis;

import com.google.gson.Gson;
import fr.rushland.main.Game;
import fr.rushland.main.GameMapOption;
import fr.rushland.main.Main;
import fr.rushland.main.server.CreateServer;
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
        if (channel.equals(RedisDataSender.channelSub)) {
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
                final String gsonString = msg[2];

                Gson gson = new Gson();

                GameMapOption gameMap = gson.fromJson(gsonString, GameMapOption.class); //Permet de générer la map etc...

                if (gameMap == null) {
                    Logger.getGlobal().severe("Game is null !");
                    return;
                }

                Game game = Main.findGame(gameMap.getGameOption(), gameMap.getGameType());
                if (game == null) {
                    Logger.getGlobal().severe("Game '" + gameMap.getGameOption() + "' is null, create it.");
                    game = new Game(gameMap.getGameType(), gameMap.getMaxPlayers(), gameMap.getGameOption());
                }

                System.out.print("\nGameOption: " + game.getOption());
                System.out.print("\nGame: " + game.getGameType());

                findGame(player, gameMap, game);

            } else if (msg[0].equals("delete")) {
                String typeGame = msg[1];
                int port = Integer.parseInt(msg[2]);
                System.out.print("\nDelete Game: " + typeGame);
                System.out.print("\nDelete Port: " + port);

                Game game = Main.findGame(port);
                if (game == null) {
                    Logger.getGlobal().severe("\nGame '" + typeGame + port + "' is null !");
                    return;
                }

                System.out.print("\nOption de la game a supprimer: " + game.getOption());
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
                ArrayList<Game> game = Main.findGame(msg[2]);

                if (game.size() == 0) {
                    RedisDataSender.getPublisher.publish("say#" + playerName + "#Aucun joueur n'est actuellement dans ce mode de jeu, si vous tenez à jouer à ce mode de jeu," +
                            "merci de choisir une option.");

                    return;
                }
                RedisRequestData bestServer = null;
                for (Game typeGame : game) {
                    RedisRequestData data = typeGame.findBetterGame();
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
                        System.out.print("Error during creation of game");
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
                        findGame(playerName, gameMap, type);
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

    public void findGame(final String player, final GameMapOption gameMap, final Game game) {
        String validGame = game.findValidGame();
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
                System.out.print("\nPort: " + port);
                final CreateServer newSrv = new CreateServer(game, gameMap, port);
                newSrv.copyGameServer();

                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                RedisDataSender.getPublisher.publish("createsrv#" + finalGame.getGameType() + "#" + finalPort + "#" + player);
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
                                findGame(player, gameMap, game);
                            }
                        },
                        2000
                );
            }
        } else {
            System.out.print("\nSend player to: " + validGame);
            RedisDataSender.getPublisher.publish("send#" + player + "#" + validGame);
        }

    }
}
