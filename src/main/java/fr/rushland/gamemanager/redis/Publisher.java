package fr.rushland.gamemanager.redis;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Publisher {

    private final String channel;
    private JedisFactory jedisFactory;

    public Publisher(String channel) {
        this.channel = channel;
        this.jedisFactory = JedisFactory.getInstance();
    }

    public void publish(String message) {
        try {
            jedisFactory.getResource().publish(channel, message);
        } catch (Exception e) {
            Logger.getGlobal().log(Level.SEVERE, "Failed to publish ", e);
        }
    }
}
