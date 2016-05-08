package fr.rushland.gamemanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

import fr.rushland.gamemanager.redis.RedisDataSender;

/**
 * Created by Zaral on 10/04/2016.
 */
public class Main {


    /*public static ArrayList<Game> listGames = new ArrayList<Game>();
    public static ArrayList<Integer> freePort = new ArrayList<Integer>();
    public static ArrayList<GameMapOption> listOption = new ArrayList<>();*/
    public static List<Game> listGames = Collections.synchronizedList(new ArrayList<Game>());
    public static List<Integer> freePort = Collections.synchronizedList(new ArrayList<Integer>());
    public static List<GameMapOption> listOption = Collections.synchronizedList(new ArrayList<GameMapOption>());
    public static ArrayList<String> partyMembers = new ArrayList<String>();
    public static HashMap<String, Integer> partySlotsByLeader = new HashMap<String, Integer>();

    public static int port = 41000;

    public static void main(String[] args) {
        Main.logInfo("Starting GameManager...");

        Main.logInfo("Connecting to Redis...");
        RedisDataSender.setup("gamemanager");

        Main.logSuccess("GameManager Ready! Waiting for data...");
        
        @SuppressWarnings("resource")
        final Scanner scanner = new Scanner(System.in);
        new Thread() {
            public void run() {
                while (true) {
                    String next = scanner.next();
                    if (next.equalsIgnoreCase("debug:listGames")) {
                        synchronized (listGames) {
                            List<Game> listGamesCopy = new ArrayList<Game>(listGames);
                            Iterator<Game> iter = listGamesCopy.iterator();
                            String games = "";
                            while (iter.hasNext()) {
                                Game game = iter.next();
                                games = game + ", ";
                            }
                            Main.logDebug("[listGames] " + games);
                        }
                    } else if (next.equalsIgnoreCase("debug:freePort")) {
                        synchronized (freePort) {
                            List<Integer> freePortCopy = new ArrayList<Integer>(freePort);
                            Iterator<Integer> iter = freePortCopy.iterator();
                            String ports = "";
                            while (iter.hasNext()) {
                                int port = iter.next();
                                ports = port + ", ";
                            }
                            Main.logDebug("[freePort] " + ports);
                        }
                    } else if (next.equalsIgnoreCase("debug:listOption")) {
                        synchronized (listOption) {
                            List<GameMapOption> listOptionCopy = new ArrayList<GameMapOption>(listOption);
                            Iterator<GameMapOption> iter = listOptionCopy.iterator();
                            String options = "";
                            while (iter.hasNext()) {
                                GameMapOption option = iter.next();
                                options = option + ", ";
                            }
                            Main.logDebug("[listOption] " + options);
                        }
                    }
                }
            }
        }.start();
    }

    public static Game findGame(String option, String gameType) {
        synchronized (listGames) {
            List<Game> listGamesCopy = new ArrayList<Game>(listGames);
            Iterator<Game> iter = listGamesCopy.iterator();
            while (iter.hasNext()) {
                Game game = iter.next();
                if (option.equals(game.getOption()) && gameType.equals(game.getGameType())) {
                    return game;
                } /*else if (option.equals( game.getGameType())) {
                    return game;
                }*/
            }
        }
        return null;
    }


    public static GameMapOption getOptionByName(String option, String type) {
        synchronized (listOption) {
            List<GameMapOption> listOptionCopy = new ArrayList<GameMapOption>(listOption);
            Iterator<GameMapOption> iter = listOptionCopy.iterator();
            while (iter.hasNext()) {
                GameMapOption gameMap = iter.next();
                if (gameMap.getGameType().equals(type) && gameMap.getGameOption().equals(option)) {
                    return gameMap;
                }
            }
        }
        return null;
    }
    
    public static ArrayList<Game> findGame(String gameType) {
        ArrayList<Game> games = new ArrayList<>();
        synchronized (listGames) {
            List<Game> listGamesCopy = new ArrayList<Game>(listGames);
            Iterator<Game> iter = listGamesCopy.iterator();
            while (iter.hasNext()) {
                Game game = iter.next();
                if (game.getGameType().equals(gameType)) {
                    games.add(game);
                }
            }
        }
        return games;
    }

    public static Game findGame(int port) {
        synchronized (listGames) {
            List<Game> listGamesCopy = new ArrayList<Game>(listGames);
            Iterator<Game> iter = listGamesCopy.iterator();
            while (iter.hasNext()) {
                Game game = iter.next();
                if (game.gameExist(port)) {
                    return game;
                }
            }
        }
        return null;
    }
    
    public static void logInfo(String msg) {
        System.out.println((char)27 + "[36m" + msg +  (char)27 + "[0m");
    }
    
    public static void logSuccess(String msg) {
        System.out.println((char)27 + "[32m" + msg +  (char)27 + "[0m");
    }
    
    public static void logWarning(String msg) {
        System.out.println((char)27 + "[33m" + msg +  (char)27 + "[0m");
    }
    
    public static void logError(String msg) {
        System.out.println((char)27 + "[31;1m" + msg +  (char)27 + "[0m");
    }
    
    public static void logDebug(String msg) {
        System.out.println((char)27 + "[35m" + msg +  (char)27 + "[0m");
    }

}
