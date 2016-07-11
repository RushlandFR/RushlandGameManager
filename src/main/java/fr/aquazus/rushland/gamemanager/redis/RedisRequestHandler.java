package fr.aquazus.rushland.gamemanager.redis;

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

public class RedisRequestHandler {

    private final String serverName;
    private JedisFactory jedisFactory;

    public RedisRequestHandler(String serverName) {
        this.serverName = serverName;
        this.jedisFactory = JedisFactory.getInstance();
    }

    public RedisRequestData getData() {
        Jedis jedis = null;
        try {
            jedis = jedisFactory.getPool().getResource();
            String rawData = jedis.get(serverName);
            if (rawData != null) {
                String[] rawDataTable = rawData.split("#");
                return new RedisRequestData(serverName, rawDataTable[0], rawDataTable[1], rawDataTable[2], rawDataTable[3]);
            }
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }
}
