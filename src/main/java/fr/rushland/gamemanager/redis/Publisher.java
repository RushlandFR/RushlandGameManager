package fr.rushland.gamemanager.redis;

import redis.clients.jedis.Jedis;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Publisher {

    private final Jedis publisherJedis;

    private final String channel;

    public Publisher(Jedis publisherJedis, String channel) {
        this.publisherJedis = publisherJedis;
        this.channel = channel;
    }

    public void publish(String message) {
        try {
            publisherJedis.publish(channel, message);
        } catch (Exception e) {
            Logger.getGlobal().log(Level.SEVERE, "Failed to publish ", e);
        }
    }
}