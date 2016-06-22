package fr.rushland.gamemanager.redis;

import java.util.logging.Level;
import java.util.logging.Logger;

import redis.clients.jedis.Jedis;

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
