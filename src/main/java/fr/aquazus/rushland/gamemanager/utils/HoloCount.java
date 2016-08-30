package fr.aquazus.rushland.gamemanager.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import fr.aquazus.rushland.gamemanager.GameData;
import fr.aquazus.rushland.gamemanager.Logger;
import fr.aquazus.rushland.gamemanager.redis.RedisDataSender;
import fr.aquazus.rushland.gamemanager.redis.RedisRequestData;
import fr.aquazus.rushland.gamemanager.redis.RedisRequestHandler;

import java.util.Timer;
import java.util.TimerTask;

/*
 * Ce fichier est soumis à des droits d'auteur.
 * Dépot http://www.copyrightdepot.com/cd88/00056542.htm
 * Numéro du détenteur - 00056542
 * Le détenteur des copyrights publiés dans cette page n'autorise 
 * aucun usage de ses créations, en tout ou en partie. 
 * Les archives de CopyrightDepot.com conservent les documents 
 * qui permettent au détenteur de démontrer ses droits d'auteur et d’éventuellement
 * réclamer légalement une compensation financière contre toute personne ayant utilisé 
 * une de ses créations sans autorisation. Conformément à nos règlements, 
 * ces documents sont assermentés, à nos frais, 
 * en cas de procès pour violation de droits d'auteur.
 */

public class HoloCount extends TimerTask {
    private String gameType;
    private Logger logger;

    public HoloCount(String gameType) {
        this.gameType = gameType;
        this.logger = Logger.getLogger();
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(this, new Date(), 1000);
        logger.println("[HoloCount v2] Module loaded.");
    }

    @Override
    public void run() {
        int waiting = 0;
        int busy = 0;
        HashMap<Integer, String> waitingGamesCopy = new HashMap<>(GameData.waitingGames);
        HashMap<Integer, String> busyGamesCopy = new HashMap<>(GameData.busyGames);
        HashMap<String, Integer> waitingOptions = new HashMap<>();
        HashMap<String, Integer> busyOptions = new HashMap<>();
        for (String seenOption : GameData.seenOptions) {
            waitingOptions.put(seenOption, 0);
            busyOptions.put(seenOption, 0);
        }
        for (Entry<Integer, String> entry : waitingGamesCopy.entrySet()) {
            RedisRequestData data = new RedisRequestHandler(gameType + entry.getKey()).getData();
            if (data == null) {
                continue;
            }
            if (data.getMotd().contains("Ouvert")) {
                if (gameType.equalsIgnoreCase("rushtheflag")) {
                    busy = busy + data.getOnlinePlayers();
                    busyOptions.put(entry.getValue(), busyOptions.get(entry.getValue()) + data.getOnlinePlayers());
                } else {
                    waiting = waiting + data.getOnlinePlayers();
                    waitingOptions.put(entry.getValue(), waitingOptions.get(entry.getValue()) + data.getOnlinePlayers());
                }
            } else {
                busy = busy + data.getOnlinePlayers();
                busyOptions.put(entry.getValue(), busyOptions.get(entry.getValue()) + data.getOnlinePlayers());
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
                    busyOptions.put(entry.getValue(), busyOptions.get(entry.getValue()) + data.getOnlinePlayers());
                } else {
                    waiting = waiting + data.getOnlinePlayers();
                    waitingOptions.put(entry.getValue(), waitingOptions.get(entry.getValue()) + data.getOnlinePlayers());
                }
            } else {
                busy = busy + data.getOnlinePlayers();
                busyOptions.put(entry.getValue(), busyOptions.get(entry.getValue()) + data.getOnlinePlayers());
            }
        }
        int total = busy + waiting;
        RedisDataSender.publisher.publish("hub#holocount#" + gameType + "#waiting#" + waiting);
        RedisDataSender.publisher.publish("hub#holocount#" + gameType + "#busy#" + busy);
        for (Entry<String, Integer> entry : waitingOptions.entrySet()) {
            RedisDataSender.publisher.publish("hub#holocount#" + gameType + "#waiting#" + entry.getKey() + "#" + entry.getValue());
        }
        for (Entry<String, Integer> entry : busyOptions.entrySet()) {
            RedisDataSender.publisher.publish("hub#holocount#" + gameType + "#busy#" + entry.getKey() + "#" + entry.getValue());
        }
        RedisDataSender.publisher.publish(gameType + "#rltrackplugin#" + total);
    }
}
