package fr.rushland.gamemanager.redis;

import redis.clients.jedis.Jedis;

public class RedisRequestHandler {

    private final String serverName;

    public RedisRequestHandler(String serverName) {
        this.serverName = serverName;
    }

    public RedisRequestData getData() {
        Jedis jedis = JedisFactory.getInstance().getJedisPool().getResource();
        String rawData = jedis.get(serverName);
        jedis.close();
        if (rawData != null) {
            String[] rawDataTable = rawData.split("#");
            return new RedisRequestData(serverName, rawDataTable[0], rawDataTable[1], rawDataTable[2], rawDataTable[3]);
        }
        return null;
    }
}
