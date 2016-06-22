package fr.rushland.gamemanager.redis;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

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
