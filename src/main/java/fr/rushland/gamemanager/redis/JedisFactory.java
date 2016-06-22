package fr.rushland.gamemanager.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisFactory {
    
    private static JedisFactory instance;
    private JedisPool jedisPool;
    private Jedis connection;

    public JedisFactory() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setTestOnBorrow(true);
        jedisPool = new JedisPool(config, "127.0.0.1", 6379);
    }
    
    public Jedis getResource() {
        if (connection == null) {
            connection = jedisPool.getResource();
            return connection;
        }
        if (!connection.isConnected()) {
            connection.connect();
        }
        return connection;
    }

    public static JedisFactory getInstance() {
        if (instance == null) {
            instance = new JedisFactory();
        }
        return instance;
    }
}
