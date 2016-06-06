package fr.rushland.gamemanager.utils;

import java.util.HashMap;
import java.util.Map.Entry;

import fr.rushland.gamemanager.GameData;
import fr.rushland.gamemanager.GameManager;
import fr.rushland.gamemanager.Logger;
import fr.rushland.gamemanager.redis.RedisDataSender;
import fr.rushland.gamemanager.redis.RedisRequestData;
import fr.rushland.gamemanager.redis.RedisRequestHandler;

public class HoloCount {

    public static void load() {
        refresh();
        Logger.getLogger().println("[HoloCount] Module loaded.");
    }

    public static void refresh() {
        new java.util.Timer().schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                int waiting = 0;
                int busy = 0;
                HashMap<Integer, String> waitingGamesCopy = new HashMap<>(GameData.waitingGames);
                HashMap<Integer, String> busyGamesCopy = new HashMap<>(GameData.busyGames);
                for (Entry<Integer, String> entry : waitingGamesCopy.entrySet()) {
                    RedisRequestData data = new RedisRequestHandler(GameManager.getInstance().getGameType() + entry.getKey()).getData();
                    if (data == null) {
                        continue;
                    }
                    if (data.getMotd().contains("Ouvert")) {
                        if (GameManager.getInstance().getGameType().equalsIgnoreCase("rushtheflag")) {
                            busy = busy + data.getOnlinePlayers();
                        } else {
                            waiting = waiting + data.getOnlinePlayers();
                        }
                    } else {
                        busy = busy + data.getOnlinePlayers();
                    }
                }
                for (Entry<Integer, String> entry : busyGamesCopy.entrySet()) {
                    RedisRequestData data = new RedisRequestHandler(GameManager.getInstance().getGameType() + entry.getKey()).getData();
                    if (data == null) {
                        continue;
                    }
                    if (data.getMotd().contains("Ouvert")) {
                        if (GameManager.getInstance().getGameType().equalsIgnoreCase("rushtheflag")) {
                            busy = busy + data.getOnlinePlayers();
                        } else {
                            waiting = waiting + data.getOnlinePlayers();
                        }
                    } else {
                        busy = busy + data.getOnlinePlayers();
                    }
                }
                RedisDataSender.publisher.publish("hub#holocount#" + GameManager.getInstance().getGameType() + "#waiting#" + waiting);
                RedisDataSender.publisher.publish("hub#holocount#" + GameManager.getInstance().getGameType() + "#busy#" + busy);
                refresh();
            }
        }, 5000);
    }
}
