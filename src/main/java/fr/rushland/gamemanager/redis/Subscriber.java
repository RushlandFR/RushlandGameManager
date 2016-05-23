package fr.rushland.gamemanager.redis;

import com.google.gson.Gson;

import fr.rushland.gamemanager.GameData;
import fr.rushland.gamemanager.GameManager;
import fr.rushland.gamemanager.GameMapOption;
import fr.rushland.gamemanager.Logger;
import fr.rushland.gamemanager.utils.CodeUtils;
import redis.clients.jedis.JedisPubSub;

import java.util.ArrayList;
import java.util.HashMap;

public class Subscriber extends JedisPubSub {

    public static HashMap<String, GameMapOption> playerCancelled = new HashMap<>();
    public static ArrayList<String> partyMembers = new ArrayList<String>();
    public static HashMap<String, Integer> partySlotsByLeader = new HashMap<String, Integer>();
    private static Logger logger = Logger.getLogger();

    @Override
    public void onMessage(String channel, String message) {
        if (channel.equals("RLGamePS")) {
            String[] packet = message.split("#");
            logger.println("[Subscriber] Received packet '" + message + "' from Proxy");
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
            }
        } else if (channel.equals(RedisDataSender.channelSub)) {
            String[] packet = message.split("#");
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

                RedisDataSender.publisher.publish("proxy#gamesearchmsg#" + player + "#" + CodeUtils.formatNPCType(gameMap.getGameType()));

                int game = GameData.getValidGame(gameMap, requiredSlots);
                if (game == 0) {
                    logger.error("[Subscriber] Can't find any game for option " + gameMap.getGameOption() + ", creating a fresh game.");
                    RedisDataSender.publisher.publish("proxy#creatinggamemsg#" + player + "#" + CodeUtils.formatNPCType(gameMap.getGameType()));
                    GameData.createGame(gameMap, player);
                } else {
                    RedisDataSender.publisher.publish("proxy#gamefound#" + player + "#" + CodeUtils.formatNPCType(gameMap.getGameType()) + "#" + gameMap.getGameType() + game);
                }
            } else if (packet[1].equals("delete")) {
                int port = Integer.parseInt(packet[2]);
                logger.println("[Subscriber] Received packet delete#" + port + ", deleting game in 3 seconds...");
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                GameData.waitingGames.remove(port);
                                GameData.busyGames.remove(port);
                                GameData.unusedPorts.add(port);
                            }
                        },
                        3000
                        );
            } else if (packet[1].equals("flushplayer")) {
                String player = packet[2];
                logger.debug("[Subscriber] Flushing player " + player + "...");
                GameData.waitingPlayers.remove(player);
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

                logger.println("[Subscriber] Received packet 'random#" + player + "' from Proxy");
                
                int game = GameData.getRandomGame(requiredSlots);
                if (game == 0) {
                    logger.error("[Subscriber] Can't find any random game.");
                    RedisDataSender.publisher.publish("proxy#norandomgamemsg#" + player);
                } else {
                    logger.success("[Subscriber] Found the random game no." + game + " for " + player + ".");
                    RedisDataSender.publisher.publish("proxy#randomgamefound#" + player + "#" + GameManager.getInstance().getConfig().getGame() + game);
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
                logger.println("[Subscriber] Sending waiting players to game no. " + port + "...");
                for (String players : GameData.getWaitingPlayers(port)) {
                    RedisDataSender.publisher.publish("proxy#send#" + players + "#" + GameManager.getInstance().getConfig().getGame() + port);
                }
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
