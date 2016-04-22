package fr.rushland.gamemanager.redis;

import redis.clients.jedis.JedisPool;

/**
*
* @author Aquazus
*/
public class JedisFactory {
	private static JedisFactory instance;
	private static JedisPool jedisPool;

	public JedisFactory() {
		jedisPool = new JedisPool("167.114.240.120", 6379);
	}

	public JedisPool getJedisPool() {
		return jedisPool;
	}

	public static JedisFactory getInstance() {
		if (instance == null) {
			instance = new JedisFactory();
		}
		return instance;
	}
}
