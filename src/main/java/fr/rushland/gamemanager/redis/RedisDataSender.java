package fr.rushland.gamemanager.redis;


import redis.clients.jedis.Jedis;

import java.util.logging.Level;
import java.util.logging.Logger;

public class RedisDataSender {

    public static String serverId;
    public static String channelSub = "RLGame";
    public static Publisher getPublisher = null;

    public static void setup(String gameType) {
        serverId = gameType;
        subscribeChannels();
        //refreshTimer();
    }

    /*	public static void sendData() {
		String key = "RLSrvData-" + serverId;
		String value = Variables.motd + "_" + Bukkit.getServer().getOnlinePlayers().size() + "_" + Bukkit.getServer().getMaxPlayers();
		Jedis jedis = JedisFactory.getInstance().getJedisPool().getResource();
		jedis.set(key, value);
		jedis.expire(key, 8);
		jedis.close();
	}

	public static void refreshTimer() {
		Bukkit.getScheduler().runTaskLater(Main.plugin, new Runnable() {
			@Override
			public void run() {
				sendData();
				refreshTimer();
			}
		}, 20L * 4);
	}
     */

    private static void subscribeChannels() {
        final Jedis subscriberJedis = JedisFactory.getInstance().getJedisPool().getResource();

        final Subscriber subscriber = new Subscriber(); //permet de reçevoir les données

        new Thread(new Runnable() {
            public void run() {
                try {
                    Logger.getGlobal().info("Subscribing to '" + channelSub + "' & 'RLGamePS' channel");
                    subscriberJedis.subscribe(subscriber, channelSub, "RLGamePS");
                    Logger.getGlobal().info("Subscription ended.");
                } catch (Exception e) {
                    Logger.getGlobal().log(Level.SEVERE, "Subscribing failed", e );
                }

            }
        }).start();

        final Jedis publisherJedis = JedisFactory.getInstance().getJedisPool().getResource();

        getPublisher = new Publisher(publisherJedis, channelSub);

    }
}
