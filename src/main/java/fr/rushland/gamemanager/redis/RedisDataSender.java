package fr.rushland.gamemanager.redis;

import redis.clients.jedis.Jedis;
import fr.rushland.gamemanager.Logger;

public class RedisDataSender {

    public static String serverId;
    public static String channelSub = "RLGameManager";
    public static Publisher publisher = null;
    private static Logger logger;

    public static void setup(String gameType) {
        serverId = gameType;
        subscribeChannels();
        logger = Logger.getLogger();
    }
    
    private static void subscribeChannels() {
        final Jedis subscriberJedis = JedisFactory.getInstance().getJedisPool().getResource();

        final Subscriber subscriber = new Subscriber();

        new Thread(new Runnable() {
            public void run() {
                try {
                    logger.println("[RedisDataSender] Subscribing to '" + channelSub + "' & 'RLGamePS' channel");
                    subscriberJedis.subscribe(subscriber, channelSub, "RLGamePS");
                    logger.success("[RedisDataSender] Subscription ended.");
                } catch (Exception e) {
                    logger.error("[RedisDataSender] Subscribing failed");
                    e.printStackTrace();
                }

            }
        }).start();

        final Jedis publisherJedis = JedisFactory.getInstance().getJedisPool().getResource();

        publisher = new Publisher(publisherJedis, channelSub);

    }
}
