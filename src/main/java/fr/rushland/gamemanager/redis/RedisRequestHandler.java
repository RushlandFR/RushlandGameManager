package fr.rushland.gamemanager.redis;

import redis.clients.jedis.Jedis;

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
            jedis.close();
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
