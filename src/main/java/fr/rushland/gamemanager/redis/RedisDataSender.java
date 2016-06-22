package fr.rushland.gamemanager.redis;

import fr.rushland.gamemanager.Logger;

public class RedisDataSender {

    public static String serverId;
    public static String channelSub = "RLGameManager";
    public static Publisher publisher = null;
    private static Logger logger;

    public static void setup(String gameType) {
        serverId = gameType;
        logger = Logger.getLogger();
        subscribeChannels();
    }
    
    private static void subscribeChannels() {
        final Subscriber subscriber = new Subscriber();

        new Thread(new Runnable() {
            public void run() {
                try {
                    logger.println("[RedisDataSender] Subscribing to '" + channelSub + "' & 'RLGamePS' channel");
                    JedisFactory.getInstance().getResource().subscribe(subscriber, channelSub, "RLGamePS");
                    logger.success("[RedisDataSender] Subscription ended.");
                } catch (Exception e) {
                    logger.error("[RedisDataSender] Subscribing failed");
                    e.printStackTrace();
                }

            }
        }).start();

        publisher = new Publisher(channelSub);
    }
}
