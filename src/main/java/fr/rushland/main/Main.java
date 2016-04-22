package fr.rushland.main;

import fr.rushland.main.redis.RedisDataSender;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Created by Zaral on 10/04/2016.
 */
public class Main {


    public static ArrayList<Game> listGames = new ArrayList<Game>();
    public static ArrayList<Integer> freePort = new ArrayList<Integer>();
    public static ArrayList<GameMapOption> listOption = new ArrayList<>();

    public static int port = 41000;


    public static void main(String[] args) {
        Logger.getGlobal().info("Starting RushlandGameManager program...");

        Logger.getGlobal().info("Setup redis...");
        RedisDataSender.setup("gamemanager");


        Logger.getGlobal().info("Ready ! Waiting data...");

    }

    public static Game findGame(String option, String gameType) {
        for (Game game : listGames) {
            if (option.equals(game.getOption()) && gameType.equals(game.getGameType())) {
                return game;
            } /*else if (option.equals( game.getGameType())) {
                return game;
            }*/
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
        for (Game game : listGames) {
            if (game.getGameType().equals(gameType)) {
                games.add(game);
            }
        }
        return games;
    }

    public static Game findGame(int port) {
        for (Game game : listGames) {
            if (game.gameExist(port)) {
                return game;
            }
        }
        return null;
    }

}
