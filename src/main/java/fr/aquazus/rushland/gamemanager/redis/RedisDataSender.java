package fr.aquazus.rushland.gamemanager.redis;

import fr.aquazus.rushland.gamemanager.Logger;
import redis.clients.jedis.Jedis;

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
