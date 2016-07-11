package fr.aquazus.rushland.gamemanager.redis;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

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

public class JedisFactory {

    private static JedisFactory instance;
    private JedisPool jedisPool;

    public JedisFactory() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setTestOnBorrow(true);
        jedisPool = new JedisPool(config, "127.0.0.1", 6379);
    }

    public JedisPool getPool() {
        return jedisPool;
    }

    public static JedisFactory getInstance() {
        if (instance == null) {
            instance = new JedisFactory();
        }
        return instance;
    }
}
