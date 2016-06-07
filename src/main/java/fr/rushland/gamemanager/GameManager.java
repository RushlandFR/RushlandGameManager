package fr.rushland.gamemanager;

import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.Scanner;

import fr.rushland.gamemanager.redis.RedisDataSender;
import fr.rushland.gamemanager.utils.HoloCount;

public class GameManager {

    private static GameManager instance;
    private Logger logger;
    private final String configFile = "gamemanager.properties";
    private Configuration config;
    private String gameType;

    public static void main(String[] args) {
        GameManager.instance = new GameManager();
        GameManager.instance.logger = Logger.getLogger();
        GameManager.instance.start();
    }

    private void start() {
        logger.println("[GameManager] Starting GameManager...");
        if (!new File(configFile).isFile()) {
            logger.error("[GameManager] Config file not found, creating one...");
            saveConfig();
        } else {
            loadConfig();
        }
        logger.println("[GameManager] Loading unused ports...");
        for (int i = config.getPortRange()[0]; i < config.getPortRange()[1] + 1; i++) {
            GameData.unusedPorts.add(i);
        }
        logger.println("[GameManager] Loading Redis...");
        RedisDataSender.setup("gamemanager");
        logger.println("[GameManager] Loading HoloCount...");
        new HoloCount(gameType);
        logger.println("[GameManager] Ready!");
        startCommandScanner();
    }

    public static GameManager getInstance() {
        return instance;
    }

    public Configuration getConfig() {
        return this.config;
    }

    public String getGameType() {
        return this.gameType;
    }

    private void saveConfig() {
        try {
            PrintWriter writer = new PrintWriter(configFile, "UTF-8");
            writer.println("game=changeme");
            writer.println("port-range=40000-41000");
            writer.println("debug=false");
            writer.close();
        } catch (Exception ex) {
            logger.error("[GameManager] An internal error has occured whilist generating " + configFile + ".");
            System.exit(0);
        }
        logger.success("[GameManager] Config has been generated. Please configure it then restart the GameManager.");
        System.exit(0);
    }

    public void loadConfig() {
        logger.println("[GameManager] Reading configuration...");
        this.config = new Configuration();
        try (FileReader reader = new FileReader(this.configFile)) {
            Properties properties = new Properties();
            properties.load(reader);
            this.config.setGame(properties.getProperty("game"));
            this.gameType = properties.getProperty("game");
            String[] portRange = properties.getProperty("port-range").split("-");
            this.config.setPortRange(new int[]{Integer.parseInt(portRange[0]), Integer.parseInt(portRange[1])});
            this.config.setDebug(Boolean.parseBoolean(properties.getProperty("debug")));
        } catch (Exception ex) {
            this.logger.error("[GameManager] An internal error has occured whilist reading " + this.configFile + "!");
            this.logger.error(ex.getMessage());
            System.exit(0);
        }
    }

    private void startCommandScanner() {
        final Scanner scanner = new Scanner(System.in);
        new Thread() {
            public void run() {
                while (true) {
                    String next = scanner.next();
                    if (next.equalsIgnoreCase("help")) {
                        logger.println("[Commands] Commands list:");
                        logger.println("[Commands] waiting - Show waiting games");
                        logger.println("[Commands] busy - Show busy games");
                        logger.println("[Commands] starting - Show starting games");
                        logger.println("[Commands] unused - Show unused ports");
                        logger.println("[Commands] waitingplayers - Show waiting players");
                        logger.println("[Commands] stop - Stop the GameManager");
                    } else if (next.equalsIgnoreCase("stop")) {
                        logger.error("[Commands] Stopping Games...");
                        RedisDataSender.publisher.publish(GameManager.getInstance().getGameType() + "#shutdown");
                        logger.error("[Commands] Stopping GameManager...");
                        scanner.close();
                        System.exit(0);
                    } else if (next.equalsIgnoreCase("waiting")) {
                        logger.println("[Commands] Waiting games: " + GameData.waitingGames.toString());
                    } else if (next.equalsIgnoreCase("busy")) {
                        logger.println("[Commands] Busy games: " + GameData.busyGames.toString());
                    } else if (next.equalsIgnoreCase("starting")) {
                        logger.println("[Commands] Starting games: " + GameData.startingGames.toString());
                    } else if (next.equalsIgnoreCase("unused")) {
                        logger.println("[Commands] Unused ports: " + GameData.unusedPorts.toString());
                    } else if (next.equalsIgnoreCase("waitingplayers")) {
                        logger.println("[Commands] Waiting players: " + GameData.waitingPlayers.toString());
                    } else {
                        logger.error("[Commands] Command not found. Type 'help' to get a list of commands.");
                    }
                }
            }
        }.start();
    }

}
