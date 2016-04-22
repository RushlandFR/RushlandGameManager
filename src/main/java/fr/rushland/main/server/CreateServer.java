package fr.rushland.main.server;

import fr.rushland.main.Game;
import fr.rushland.main.GameMapOption;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Zaral on 11/04/2016.
 */
public class CreateServer {


    private Game game;
    private GameMapOption option;
    private int port;
    private String map;
    private String serverName;
    private ArrayList<String> lines = new ArrayList<String>();
    private String prefix = null;
    private String line;

    public CreateServer(Game game, GameMapOption option, int port) {
        this.game = game;
        this.option = option;
        this.port = port;

        if (option != null)
        this.map = option.getRandomMap();

        this.serverName = game.getGameType() + port;
        this.prefix = "[" + serverName + "] ";

    }

    public String getGameDirectory() {
        String path = "/home/rushland/" + serverName;
        return path;
    }

    public String getOriginalMapDirectory() {
        String path = "maps/" + this.game.getGameType() + "/" + this.map;
        return path;
    }

    public String getTempMapDirectory() {
        String path = getGameDirectory() + "/game/original";
        return path;
    }

    /**
     * @return directory des lobby
     * @deprecated Pas besoin de copier le lobby car il est déjà généré avec la map
     */
    @Deprecated
    public String getLobbyDirectory() {
        String path = "maps/" + this.game.getGameType() + "/lobby";
        return path;
    }

    public String getOriginalGame() {
        String path = "game/" + game.getGameType();
        return path;
    }

    public void copyGameServer() {
        if (new File(getGameDirectory()).exists()) {
            try {
                Logger.getGlobal().info(prefix + "Previous game found, deleting it...");
                FileUtils.deleteDirectory(new File(getGameDirectory()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            Logger.getGlobal().info(prefix + "Creating a fresh game...");
            FileUtils.copyDirectory(new File(getOriginalGame()), new File(getGameDirectory()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*
        try {
            Logger.getGlobal().info(prefix + "Loading good map...");
            FileUtils.copyDirectory(new File(getOriginalMapDirectory()), new File(getTempMapDirectory()));
        } catch (IOException e) {
            e.printStackTrace();
        }
*/
        String filePluginPath = null;
        switch (game.getGameType()) {
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

        String path = getGameDirectory() + "/plugins/" + filePluginPath;
        if (new File(path + "/config.yml").exists()) {
            try {
                Logger.getGlobal().info(prefix + "Deleting previous yaml config plugin");
                FileUtils.forceDelete(new File(path + "/config.yml"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        File file = new File(path + "/config.yml");
        try {
            FileWriter fw = new FileWriter(file);
            BufferedWriter bf = new BufferedWriter(fw);
            bf.write("id: " + this.port);
            bf.newLine();
            System.out.print("teams: " + this.option.withTeam());
            if (!game.getGameType().equals("antwars"))
                bf.write("teams: " + this.option.withTeam());
            else
                bf.write("mini: " + this.option.withTeam());

            bf.newLine();
            bf.write("map: " + this.map);
            bf.flush();
            bf.close();
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        path = getGameDirectory() + "/server.properties";
        file = new File(path);
        try {
            final FileReader fr = new FileReader(file);
            final BufferedReader br = new BufferedReader(fr);
            String line = null;
            while ((line = br.readLine()) != null) {
                if (line.contains("server-port")) {
                    line = line.replace("1", new StringBuilder().append(port).toString());
                }
                if (line.contains("max-players")) {
                    line = line.replace("1", new StringBuilder().append(option.getMaxPlayers()).toString());
                }
                this.lines.add(line);
            }

            fr.close();
            br.close();
            final FileWriter fw = new FileWriter(file);
            final BufferedWriter out = new BufferedWriter(fw);
            for (final String s : this.lines) {
                out.write(s);
                out.newLine();
            }
            out.flush();
            out.close();
            line = null;
            this.lines = new ArrayList<String>();
        } catch (Exception e) {
            e.printStackTrace();
        }
        modifySh();
        Logger.getGlobal().info(prefix + "Server ready ! ");

    }

    public void runServer() {
        copySh();
        try {
            Logger.getGlobal().info("Starting server " + serverName);
      /*      CommandLine commandLine = CommandLine.parse("sh " + getGameDirectory() + "/run.sh");
            DefaultExecutor exec = new DefaultExecutor();
            exec.setExitValue(0);
            try {
                exec.execute(commandLine);
            } catch (ExecuteException e) {
                System.err.println("Execution failed.");
                e.printStackTrace();
            } catch (IOException e) {
                System.err.println("permission denied.");
                e.printStackTrace();
            }
            */
            game.addWaitingGame(port);
            //Runtime.getRuntime().exec("mkdir caca");
            /*
            String service = getGameDirectory() + "/spigot-1.9.jar";
            String invocation ="java -Xms750M -Xmx750M -jar " + service;
            //Runtime.getRuntime().exec("screen -S caca2 echo test");
            String command = "screen -dmS " + serverName  + " "+ invocation;
            System.out.print("Commande:" + command);
            Runtime.getRuntime().exec(command);
*/
            String command = "./" + serverName + ".sh";
            Runtime.getRuntime().exec(command);

            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            try {
                                FileUtils.forceDelete(new File(serverName + ".sh"));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    4000
            );
        } catch (Exception e) {
            Logger.getGlobal().log(Level.SEVERE, prefix + "Error during starting server", e);
        }


    }

    public void copySh() {
        try {
            Logger.getGlobal().info(prefix + "Creating a fresh game...");
            FileUtils.copyFile(new File("run.sh"), new File(serverName + ".sh"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            final File f1 = new File(serverName + ".sh");
            f1.setExecutable(true);
            final FileReader fr = new FileReader(f1);
            final BufferedReader br = new BufferedReader(fr);
            String line;
            do {
                if (this.line == null) {
                    line = br.readLine();
                    this.line = line;
                }
                if (this.line.contains("game")) {
                    this.line = this.line.replace("game", serverName);
                }
                this.lines.add(this.line);
                line = br.readLine();
                this.line = line;
            } while (line != null);

            fr.close();
            br.close();
            final FileWriter fw = new FileWriter(f1);
            final BufferedWriter out = new BufferedWriter(fw);
            for (final String s : this.lines) {
                out.write(s);
                out.newLine();
            }
            out.flush();
            out.close();
            this.line = null;
            this.lines = new ArrayList<String>();
        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }

    public void modifySh() {
        try {
            final File f1 = new File(getGameDirectory() + "/run.sh");
            f1.setExecutable(true);
            final FileReader fr = new FileReader(f1);
            final BufferedReader br = new BufferedReader(fr);
            String line;
            boolean ok = true;
            do {
                if (this.line == null) {
                    line = br.readLine();
                    this.line = line;
                }
                if (this.line.contains(game.getGameType()) && ok) {
                    this.line = this.line.replace(game.getGameType(), serverName);
                    ok = false;
                }
                this.lines.add(this.line);
                line = br.readLine();
                this.line = line;
            } while (line != null);

            fr.close();
            br.close();
            final FileWriter fw = new FileWriter(f1);
            final BufferedWriter out = new BufferedWriter(fw);
            for (final String s : this.lines) {
                out.write(s);
                out.newLine();
            }
            out.flush();
            out.close();
            this.line = null;
            this.lines = new ArrayList<String>();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
