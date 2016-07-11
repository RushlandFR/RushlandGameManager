package fr.aquazus.rushland.gamemanager.redis;

import java.util.logging.Level;
import java.util.logging.Logger;

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

public class Publisher {

    private final String channel;
    private JedisFactory jedisFactory;

    public Publisher(String channel) {
        this.channel = channel;
        this.jedisFactory = JedisFactory.getInstance();
    }

    public void publish(String message) {
        try {
            Jedis jedis = null;
            try {
                jedis = jedisFactory.getPool().getResource();
                jedis.publish(channel, message);
            } finally {
                if (jedis != null) {
                    jedis.close();
                }
            }
        } catch (Exception e) {
            Logger.getGlobal().log(Level.SEVERE, "Failed to publish ", e);
        }
    }
}
