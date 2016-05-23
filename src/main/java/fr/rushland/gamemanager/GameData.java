package fr.rushland.gamemanager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;

import fr.rushland.gamemanager.redis.RedisDataSender;
import fr.rushland.gamemanager.redis.RedisRequestData;
import fr.rushland.gamemanager.redis.RedisRequestHandler;

public class GameData {

    public static HashMap<Integer, GameMapOption> waitingGames = new HashMap<>();
    public static HashMap<Integer, GameMapOption> busyGames = new HashMap<>();
    public static HashMap<Integer, GameMapOption> startingGames = new HashMap<>();
    public static ArrayList<Integer> unusedPorts = new ArrayList<>();
    public static HashMap<String, Integer> waitingPlayers = new HashMap<>();
    private static Logger logger = Logger.getLogger();
    
    public static int getRandomGame(int slotsNeeded) {
        if (!waitingGames.isEmpty()) {
            HashMap<Integer, GameMapOption> waitingGamesCopy = new HashMap<>(waitingGames);
            for (Entry<Integer, GameMapOption> entry : waitingGamesCopy.entrySet()) {
                RedisRequestData data = new RedisRequestHandler(entry.getValue().getGameType() + entry.getKey()).getData();
                int availableSlots = data.getMaxPlayers() - data.getOnlinePlayers();
                if ((availableSlots >= slotsNeeded) && data.getMotd().equals("§2Ouvert")) {
                    return entry.getKey();
                }
            }
            return 0;
        } else {
            return 0;
        }
    }
    
    public static ArrayList<String> getWaitingPlayers(int port) {
        ArrayList<String> players = new ArrayList<>();
        HashMap<String, Integer> waitingPlayersCopy = new HashMap<>(waitingPlayers);
        for (Entry<String, Integer> entry : waitingPlayersCopy.entrySet()) {
            if (entry.getValue() == port) {
                players.add(entry.getKey());
            }
        }
        return players;
    }

    public static int getValidGame(GameMapOption option, int slotsNeeded) {
        if (waitingGames.containsKey(option)) {
            HashMap<Integer, GameMapOption> waitingGamesCopy = new HashMap<>(waitingGames);
            for (Entry<Integer, GameMapOption> entry : waitingGamesCopy.entrySet()) {
                if (entry.getValue() != option) {
                    continue;
                }
                RedisRequestData data = new RedisRequestHandler(option.getGameType() + entry.getKey()).getData();
                int availableSlots = data.getMaxPlayers() - data.getOnlinePlayers();
                if ((availableSlots >= slotsNeeded) && data.getMotd().equals("§2Ouvert")) {
                    return entry.getKey();
                }
            }
            return 0;
        } else {
            return 0;
        }
    }

    public static void createGame(GameMapOption option, String player) {
        int port = unusedPorts.get(0);
        unusedPorts.remove(port);
        waitingPlayers.put(player, port);
        startingGames.put(port, option);
        String fullGameName = option.getGameType() + port;
        logger.println("[" + fullGameName + "] Injecting into Proxy...");
        RedisDataSender.publisher.publish("proxy#injectserver#" + port + "#" + fullGameName);
        logger.println("[" + fullGameName + "] Creating game...");
        String filePluginPath = null;
        switch (option.getGameType()) {
            case ("rush"):
                filePluginPath = "RLGame-Rush";
                break;
            case ("rushtheflag"):
                filePluginPath = "RLGame-RushTheFlag";
                break;
            case ("rushevent"):
                filePluginPath = "RLGame-RushEvent";
                break;
            case ("skywars"):
                filePluginPath = "RLGame-SkyWars";
                break;
            case ("divided"):
                filePluginPath = "RLGame-DividedTogether";
                break;
            case ("buildchampion"):
                filePluginPath = "RLGame-BuildChampion";
                break;
            case ("antwars"):
                filePluginPath = "RLGame-AntWars";
                break;
            case ("pvpbox"):
                filePluginPath = "RushlandPVPBox";
                break;
        }
        String path = "/home/rushland-games/" + option.getGameType() + "/" + fullGameName + "/plugins/" + filePluginPath;
        if (new File(path + "/config.yml").exists()) {
            try {
                logger.println("[" + fullGameName + "] Deleting previous yaml config plugin");
                FileUtils.forceDelete(new File(path + "/config.yml"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        File file = new File(path + "/config.yml");
        try {
            FileWriter fw = new FileWriter(file);
            BufferedWriter bf = new BufferedWriter(fw);
            bf.write("id: " + port);
            bf.newLine();
            if (!option.getGameType().equals("antwars"))
                bf.write("teams: " + option.withTeam());
            else
                bf.write("mini: " + option.withTeam());

            bf.newLine();
            bf.write("map: " + option.getRandomMap());
            bf.flush();
            bf.close();
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        path = "/home/rushland-games/" + option.getGameType() + "/" + fullGameName + "/server.properties";
        file = new File(path);
        List<String> lines = new ArrayList<String>();
        try {
            final FileReader fr = new FileReader(file);
            final BufferedReader br = new BufferedReader(fr);
            String line = null;
            while ((line = br.readLine()) != null) {
                if (line.contains("max-players")) {
                    line = "max-players=" + option.getMaxPlayers();
                }
                lines.add(line);
            }

            fr.close();
            br.close();
            final FileWriter fw = new FileWriter(file);
            final BufferedWriter out = new BufferedWriter(fw);
            for (final String s : lines) {
                out.write(s);
                out.newLine();
            }
            out.flush();
            out.close();
            line = null;
            lines = new ArrayList<String>();
        } catch (Exception e) {
            e.printStackTrace();
        }

        logger.println("[" + fullGameName + "] Game created. Starting it...");

        try {
            FileUtils.copyFile(new File("run.sh"), new File(fullGameName + ".sh"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            final File f1 = new File(fullGameName + ".sh");
            f1.setExecutable(true);
            final FileReader fr = new FileReader(f1);
            final BufferedReader br = new BufferedReader(fr);
            String oLine = null;
            String line;
            lines.clear();
            do {
                if (oLine == null) {
                    line = br.readLine();
                    oLine = line;
                }
                if (oLine.contains("game")) {
                    oLine = oLine.replace("game", fullGameName);
                }
                lines.add(oLine);
                line = br.readLine();
                oLine = line;
            } while (line != null);

            fr.close();
            br.close();
            final FileWriter fw = new FileWriter(f1);
            final BufferedWriter out = new BufferedWriter(fw);
            for (final String s : lines) {
                out.write(s);
                out.newLine();
            }
            out.flush();
            out.close();
            oLine = null;
            lines = new ArrayList<String>();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            String command = "./" + fullGameName + ".sh";
            Runtime.getRuntime().exec(command);
            logger.success("[" + fullGameName + "] Server started.");
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            try {
                                FileUtils.forceDelete(new File(fullGameName + ".sh"));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    500
                    );
        } catch (Exception e) {
            logger.error("[" + fullGameName + "] Error during server start");
            e.printStackTrace();
        }
    }

}
