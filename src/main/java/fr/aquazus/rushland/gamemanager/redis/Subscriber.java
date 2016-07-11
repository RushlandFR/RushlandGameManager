package fr.aquazus.rushland.gamemanager.redis;

import com.google.gson.Gson;

import fr.aquazus.rushland.gamemanager.GameData;
import fr.aquazus.rushland.gamemanager.GameManager;
import fr.aquazus.rushland.gamemanager.GameMapOption;
import fr.aquazus.rushland.gamemanager.Logger;
import fr.aquazus.rushland.gamemanager.utils.CodeUtils;
import redis.clients.jedis.JedisPubSub;

import java.util.ArrayList;
import java.util.HashMap;

/*
 * Ce fichier est soumis à des droits d'auteur.
 * Dépot http://www.copyrightdepot.com/cd88/00056542.htm
 * Numéro du détenteur - 00056542
 * Le détenteur des copyrights publiés dans cette page n'autorise 
 * aucun usage de ses créations, en tout ou en partie. 
 * Les archives de CopyrightDepot.com conservent les documents 
 * qui permettent au détenteur de démontrer ses droits d'auteur et d’éventuellement
 * réclamer légalement une compensation financière contre toute personne ayant utilisé 
 * une de ses créations sans autorisation. Conformément à nos règlements, 
 * ces documents sont assermentés, à nos frais, 
 * en cas de procès pour violation de droits d'auteur.
 */

public class Subscriber extends JedisPubSub {

    public static ArrayList<String> partyMembers = new ArrayList<String>();
    public static HashMap<String, Integer> partySlotsByLeader = new HashMap<String, Integer>();
    private static Logger logger = Logger.getLogger();

    @Override
    public void onMessage(String channel, String message) {
        if (channel.equals("RLGamePS")) {
            String[] packet = message.split("#");
            //logger.println("[Subscriber] Received packet '" + message + "' from Proxy");
            if (packet[0].equals("members")) {
                String member = packet[2];
                if (packet[1].equals("add")) {
                    partyMembers.add(member);
                } else if (packet[1].equals("remove")) {
                    partyMembers.remove(member);
                }
            } else if (packet[0].equals("slots")) {
                String leader = packet[2];
                int slots = 1;
                if (packet.length > 3) {
                    slots = Integer.parseInt(packet[3]);
                }
                if (packet[1].equals("put")) {
                    partySlotsByLeader.put(leader, slots);
                } else if (packet[1].equals("remove")) {
                    partySlotsByLeader.remove(leader);
                }
            } else if (packet[0].equals("flushplayer")) {
                String player = packet[1];
                //logger.debug("[Subscriber] Flushing player " + player + "...");
                GameData.waitingPlayers.remove(player);
            }
        } else if (channel.equals(RedisDataSender.channelSub)) {
            String[] packet = message.split("#");
            if (packet[0].equals("cancel")) {
                final String player = packet[1];
                GameData.waitingPlayers.remove(player);
                return;
            }
            if (!packet[0].equals(GameManager.getInstance().getConfig().getGame())) {
                return;
            }
            if (packet[1].equals("findGame")) {
                final String player = packet[2];
                if (partyMembers.contains(player)) {
                    RedisDataSender.publisher.publish("proxy#onlyleadercanjoinmsg#" + player);
                    return;
                }
                int requiredSlots = 1;
                if (partySlotsByLeader.containsKey(player)) {
                    requiredSlots = partySlotsByLeader.get(player);
                }

                logger.println("[Subscriber] Received packet 'findGame#" + player + "' from Proxy");

                final String gsonString = packet[3];

                Gson gson = new Gson();

                GameMapOption gameMap = gson.fromJson(gsonString, GameMapOption.class);

                if (gameMap == null) {
                    logger.error("[Subscriber] GameMapOption is null !");
                    return;
                }

                if (requiredSlots > gameMap.getMaxPlayers()) {
                    RedisDataSender.publisher.publish("proxy#groupsizeerrormsg#" + player);
                    return;
                }

                RedisDataSender.publisher.publish("proxy#gamesearchmsg#" + player + "#" + CodeUtils.formatNPCType(gameMap.getGameType()) + " " + gameMap.getGameOption());

                int game = GameData.getValidGame(gameMap, requiredSlots);
                if (game == 0) {
                    logger.error("[Subscriber] Can't find any game for option " + gameMap.getGameOption() + ", creating a fresh game.");
                    RedisDataSender.publisher.publish("proxy#creatinggamemsg#" + player + "#" + CodeUtils.formatNPCType(gameMap.getGameType()));
                    GameData.createGame(gameMap, player);
                } else {
                    RedisDataSender.publisher.publish("proxy#gamefound#" + player + "#" + CodeUtils.formatNPCType(gameMap.getGameType()) + "#" + gameMap.getGameType() + game + "#" + game);
                }
            } else if (packet[1].equals("delete")) {
                int port = Integer.parseInt(packet[2]);
                logger.println("[Subscriber] Received packet delete#" + port + ", deleting game in 1 seconds...");
                RedisDataSender.publisher.publish("proxy#extractserver#" + GameManager.getInstance().getGameType() + port);
                new java.util.Timer().schedule(new java.util.TimerTask() {
                    @Override
                    public void run() {
                        GameData.waitingGames.remove(port);
                        GameData.busyGames.remove(port);
                        GameData.unusedPorts.add(port);
                    }
                }, 1000);
            } else if (packet[1].equals("random")) {
                final String player = packet[2];
                if (partyMembers.contains(player)) {
                    RedisDataSender.publisher.publish("proxy#onlyleadercanjoinmsg#" + player);
                    return;
                }
                int requiredSlots = 1;
                if (partySlotsByLeader.containsKey(player)) {
                    requiredSlots = partySlotsByLeader.get(player);
                }

                RedisDataSender.publisher.publish("proxy#randomgamesearchmsg#" + player + "#" + CodeUtils.formatNPCType(GameManager.getInstance().getConfig().getGame()));
                logger.println("[Subscriber] Received packet 'random#" + player + "' from Proxy");

                int game = GameData.getRandomGame(requiredSlots);
                if (game == 0) {
                    logger.error("[Subscriber] Can't find any random game.");
                    RedisDataSender.publisher.publish("proxy#norandomgamemsg#" + player);
                } else {
                    logger.success("[Subscriber] Found the random game no." + game + " for " + player + ".");
                    RedisDataSender.publisher.publish("proxy#randomgamefound#" + player + "#" + GameManager.getInstance().getConfig().getGame() + game + "#" + game);
                }
            } else if (packet[1].equals("wakeup")) {
                final int port = Integer.parseInt(packet[2]);
                logger.println("[Subscriber] Server no. " + port + " just woke up!");
                if (!GameData.startingGames.containsKey(port)) {
                    logger.error("[Subscriber] FATAL ERROR: Server is not in a starting state.");
                    return;
                }
                GameData.waitingGames.put(port, GameData.startingGames.get(port));
                GameData.startingGames.remove(port);
                logger.println("[Subscriber] Sending waiting players to game no. " + port + " in 0.5s...");
                new java.util.Timer().schedule(new java.util.TimerTask() {
                    @Override
                    public void run() {
                        for (String players : GameData.getWaitingPlayers(port)) {
                            GameData.waitingPlayers.remove(players);
                            RedisDataSender.publisher.publish("proxy#gamefound#" + players + "#" + CodeUtils.formatNPCType(GameManager.getInstance().getConfig().getGame()) + "#" + GameManager.getInstance().getConfig().getGame() + port);
                        }
                    }
                }, 500);
            } else if (packet[1].equals("nowbusy")) {
                final int port = Integer.parseInt(packet[2]);
                logger.println("[Subscriber] Server no. " + port + " going into busy mode!");
                if (!GameData.waitingGames.containsKey(port)) {
                    logger.error("[Subscriber] FATAL ERROR: Server is not in a waiting state.");
                    return;
                }
                GameData.busyGames.put(port, GameData.waitingGames.get(port));
                GameData.waitingGames.remove(port);
            }
        }
    }
}
