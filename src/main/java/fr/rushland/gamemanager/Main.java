package fr.rushland.gamemanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
        Logger.getGlobal().info("Starting RushlandGameManager program...");

        Logger.getGlobal().info("Setup redis...");
        RedisDataSender.setup("gamemanager");


        Logger.getGlobal().info("Ready ! Waiting data...");

    }

    public static Game findGame(String option, String gameType) {
        synchronized (listGames) {
            Iterator<Game> iter = listGames.iterator();
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
        for (GameMapOption gameMap : listOption) {
            if (gameMap.getGameType().equals(type) && gameMap.getGameOption().equals(option)) {
                return gameMap;
            }
        }
        return null;
    }
    public static ArrayList<Game> findGame(String gameType) {
        ArrayList<Game> games = new ArrayList<>();
        synchronized (listGames) {
            Iterator<Game> iter = listGames.iterator();
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
            Iterator<Game> iter = listGames.iterator();
            while (iter.hasNext()) {
                Game game = iter.next();
                if (game.gameExist(port)) {
                    return game;
                }
            }
        }
        return null;
    }

}
