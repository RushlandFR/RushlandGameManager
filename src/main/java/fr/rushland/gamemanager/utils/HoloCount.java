package fr.rushland.gamemanager.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import fr.rushland.gamemanager.GameData;
import fr.rushland.gamemanager.Logger;
import fr.rushland.gamemanager.redis.RedisDataSender;
import fr.rushland.gamemanager.redis.RedisRequestData;
import fr.rushland.gamemanager.redis.RedisRequestHandler;

public class HoloCount extends TimerTask {
    private String gameType;
    private Logger logger;

    public HoloCount(String gameType) {
        this.gameType = gameType;
        this.logger = Logger.getLogger();
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(this, new Date(), 1000);
        logger.println("[HoloCount] Module loaded.");
    }

    @Override
    public void run() {
        sendData();
    }

    private void sendData() {
        int waiting = 0;
        int busy = 0;
        HashMap<Integer, String> waitingGamesCopy = new HashMap<>(GameData.waitingGames);
        HashMap<Integer, String> busyGamesCopy = new HashMap<>(GameData.busyGames);
        for (Entry<Integer, String> entry : waitingGamesCopy.entrySet()) {
            RedisRequestData data = new RedisRequestHandler(gameType + entry.getKey()).getData();
            if (data == null) {
                continue;
            }
            if (data.getMotd().contains("Ouvert")) {
                if (gameType.equalsIgnoreCase("rushtheflag")) {
                    busy = busy + data.getOnlinePlayers();
                } else {
                    waiting = waiting + data.getOnlinePlayers();
                }
            } else {
                busy = busy + data.getOnlinePlayers();
            }
        }
        for (Entry<Integer, String> entry : busyGamesCopy.entrySet()) {
            RedisRequestData data = new RedisRequestHandler(gameType + entry.getKey()).getData();
            if (data == null) {
                continue;
            }
            if (data.getMotd().contains("Ouvert")) {
                if (gameType.equalsIgnoreCase("rushtheflag")) {
                    busy = busy + data.getOnlinePlayers();
                } else {
                    waiting = waiting + data.getOnlinePlayers();
                }
            } else {
                busy = busy + data.getOnlinePlayers();
            }
        }
        int total = busy + waiting;
        RedisDataSender.publisher.publish("hub#holocount#" + gameType + "#waiting#" + waiting);
        RedisDataSender.publisher.publish("hub#holocount#" + gameType + "#busy#" + busy);
        RedisDataSender.publisher.publish(gameType + "#rltrackplugin#" + total);
    }
}