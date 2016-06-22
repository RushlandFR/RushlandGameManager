package fr.rushland.gamemanager.redis;

import fr.rushland.gamemanager.Logger;
import redis.clients.jedis.Jedis;

public class RedisDataSender {

    public static String serverId;
    public static String channelSub = "RLGameManager";
    public static Publisher publisher = null;
    private static Logger logger;
    private static JedisFactory jedisFactory;

    public static void setup(String gameType) {
        serverId = gameType;
        logger = Logger.getLogger();
        jedisFactory = JedisFactory.getInstance();
        subscribeChannels();
    }
    
    private static void subscribeChannels() {
        final Subscriber subscriber = new Subscriber();

        new Thread(new Runnable() {
            public void run() {
                try {
                    logger.println("[RedisDataSender] Subscribing to '" + channelSub + "' & 'RLGamePS' channel");
                    Jedis jedis = null;
                    try {
                        jedis = jedisFactory.getPool().getResource();
                        jedis.subscribe(subscriber, channelSub, "RLGamePS");
                    } finally {
                        if (jedis != null) {
                            jedis.close();
                        }
                    }
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
