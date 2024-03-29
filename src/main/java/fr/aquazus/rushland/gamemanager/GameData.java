package fr.aquazus.rushland.gamemanager;

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

import fr.aquazus.rushland.gamemanager.redis.RedisDataSender;
import fr.aquazus.rushland.gamemanager.redis.RedisRequestData;
import fr.aquazus.rushland.gamemanager.redis.RedisRequestHandler;

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

public class GameData {

    public static HashMap<Integer, String> waitingGames = new HashMap<>();
    public static HashMap<Integer, String> waitingTournaments = new HashMap<>();
    public static HashMap<Integer, String> busyGames = new HashMap<>();
    public static HashMap<Integer, String> busyTournaments = new HashMap<>();
    public static HashMap<Integer, String> startingGames = new HashMap<>();
    public static HashMap<Integer, String> startingTournaments = new HashMap<>();
    public static ArrayList<Integer> unusedPorts = new ArrayList<>();
    public static ArrayList<String> seenOptions = new ArrayList<>();
    public static HashMap<String, Integer> waitingPlayers = new HashMap<>();
    public static HashMap<Integer, Integer> startingGamesCapacity = new HashMap<>();
    private static Logger logger = Logger.getLogger();

    public static int getRandomGame(int slotsNeeded) {
        if (!waitingGames.isEmpty()) {
            HashMap<Integer, String> waitingGamesCopy = new HashMap<>(waitingGames);
            for (Entry<Integer, String> entry : waitingGamesCopy.entrySet()) {
                RedisRequestData data = new RedisRequestHandler(GameManager.getInstance().getGameType() + entry.getKey()).getData();
                if (data == null) {
                    continue;
                }
                if (!data.getMotd().contains("Ouvert")) {
                    continue;
                }
                int availableSlots = data.getMaxPlayers() - data.getOnlinePlayers();
                if (availableSlots >= slotsNeeded) {
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
    
    public static int getValidStartingGame(GameMapOption option, int slotsNeeded) {
        if (option.getTournamentCode() != null) {
            if (startingTournaments.containsValue(option.getTournamentCode())) {
                HashMap<Integer, String> startingTournamentsCopy = new HashMap<>(startingTournaments);
                for (Entry<Integer, String> entry : startingTournamentsCopy.entrySet()) {
                    if (!entry.getValue().equals(option.getTournamentCode())) {
                        continue;
                    }
                    if (startingGamesCapacity.get(entry.getKey()) >= slotsNeeded) {
                        startingGamesCapacity.put(entry.getKey(), startingGamesCapacity.get(entry.getKey()) - slotsNeeded);
                        return entry.getKey();
                    }
                }
            }
            return 0;
        } else {
            if (startingGames.containsValue(option.getGameOption())) {
                HashMap<Integer, String> startingGamesCopy = new HashMap<>(startingGames);
                for (Entry<Integer, String> entry : startingGamesCopy.entrySet()) {
                    if (!entry.getValue().equals(option.getGameOption())) {
                        continue;
                    }
                    if (startingGamesCapacity.get(entry.getKey()) >= slotsNeeded) {
                        startingGamesCapacity.put(entry.getKey(), startingGamesCapacity.get(entry.getKey()) - slotsNeeded);
                        return entry.getKey();
                    }
                }
            }
            return 0;
        }
    }

    public static int getValidGame(GameMapOption option, int slotsNeeded) {
        if (option.getTournamentCode() != null) {
            if (waitingTournaments.containsValue(option.getTournamentCode())) {
                HashMap<Integer, String> waitingTournamentsCopy = new HashMap<>(waitingTournaments);
                for (Entry<Integer, String> entry : waitingTournamentsCopy.entrySet()) {
                    if (!entry.getValue().equals(option.getTournamentCode())) {
                        continue;
                    }
                    RedisRequestData data = new RedisRequestHandler(GameManager.getInstance().getGameType() + entry.getKey()).getData();
                    if (data == null) {
                        continue;
                    }
                    if (!data.getMotd().contains("Ouvert")) {
                        continue;
                    }
                    int availableSlots = data.getMaxPlayers() - data.getOnlinePlayers();
                    if (availableSlots >= slotsNeeded) {
                        return entry.getKey();
                    }
                }
                return 0;
            } else {
                return 0;
            }
        } else {
            if (waitingGames.containsValue(option.getGameOption())) {
                HashMap<Integer, String> waitingGamesCopy = new HashMap<>(waitingGames);
                for (Entry<Integer, String> entry : waitingGamesCopy.entrySet()) {
                    if (!entry.getValue().equals(option.getGameOption())) {
                        continue;
                    }
                    RedisRequestData data = new RedisRequestHandler(GameManager.getInstance().getGameType() + entry.getKey()).getData();
                    if (data == null) {
                        continue;
                    }
                    if (!data.getMotd().contains("Ouvert")) {
                        continue;
                    }
                    int availableSlots = data.getMaxPlayers() - data.getOnlinePlayers();
                    if (availableSlots >= slotsNeeded) {
                        return entry.getKey();
                    }
                }
                return 0;
            } else {
                return 0;
            }
        }
    }

    public static void createGame(GameMapOption option, String player, int requiredSlots) {
        int port = unusedPorts.get(0);
        unusedPorts.remove((Object) port);
        waitingPlayers.put(player, port);
        String fullGameName;
        String gameId = GameManager.getInstance().getGameType() + port;
        startingGamesCapacity.put(port, requiredSlots);
        if (option.getTournamentCode() == null) {
            startingGames.put(port, option.getGameOption());
            if (!seenOptions.contains(option.getGameOption())) {
                seenOptions.add(option.getGameOption());
            }
            fullGameName = gameId;
        } else {
            startingTournaments.put(port, option.getTournamentCode());
            fullGameName = "tournois-" + gameId;
        }
        logger.println("[" + fullGameName + "] Injecting into Proxy...");
        RedisDataSender.publisher.publish("proxy#injectserver#" + port + "#" + fullGameName);
        logger.println("[" + fullGameName + "] Creating game...");
        String filePluginPath = null;
        switch (GameManager.getInstance().getGameType()) {
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
        }
        String path = "/home/rushland/games/" + GameManager.getInstance().getGameType() + "/" + gameId + "/plugins/" + filePluginPath;
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
            if (!GameManager.getInstance().getGameType().equals("antwars"))
                bf.write("teams: " + option.withTeam());
            else
                bf.write("mini: " + option.withTeam());
            bf.newLine();
            bf.write("noBow: " + option.getNoBow());
            bf.newLine();
            bf.write("map: " + option.getRandomMap());
            bf.newLine();
            bf.write("tournament: " + (option.getTournamentCode() != null));
            bf.flush();
            bf.close();
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        path = "/home/rushland/games/" + GameManager.getInstance().getGameType() + "/" + gameId + "/server.properties";
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
            FileUtils.copyFile(new File("copy.sh"), new File(fullGameName + ".sh"));
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
                if (oLine.contains("edit")) {
                    oLine = oLine.replace("edit", gameId);
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
            new java.util.Timer().schedule(new java.util.TimerTask() {
                @Override
                public void run() {
                    try {
                        FileUtils.forceDelete(new File(fullGameName + ".sh"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, 500);
        } catch (Exception e) {
            logger.error("[" + fullGameName + "] Error during server start");
            e.printStackTrace();
        }
    }

}
