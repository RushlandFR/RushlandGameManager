package fr.rushland.gamemanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import fr.rushland.gamemanager.redis.RedisDataSender;
import fr.rushland.gamemanager.redis.RedisRequestData;
import fr.rushland.gamemanager.redis.RedisRequestHandler;

/**
 * Created by Zaral on 10/04/2016.
 * <p>
 * <p>
 * Object permétant de recup les infos sur une partie
 * Via redis
 * Car avant de tp un joueur dans une partie on vérifie le nombre de joueurs
 */

/**
 * @author Zaral
 * @implNote Object qui gère le type de games
 */
public class Game {


    /**
     * Les parties seront sous le format gametype+port
     */
    private String gameType = null;

    /**
     * Les ports des parties non lancées
     */
    //private ArrayList<Integer> waitingGames = new ArrayList<Integer>();
    private List<Integer> waitingGames = Collections.synchronizedList(new ArrayList<Integer>());

    /**
     * Les ports des parties lancées
     */
    //private ArrayList<Integer> startedGames = new ArrayList<Integer>();
    private List<Integer> startedGames = Collections.synchronizedList(new ArrayList<Integer>());

    private int maxPlayers;

    private String option;


    private boolean gameRecentlyCreated = false;

    public Game(String type, int maxPlayers, String option) {
        this.gameType = type;
        this.maxPlayers = maxPlayers;
        this.option = option;
        Main.listGames.add(this);
    }

    /**
     * @param port Le port de la partie que l'on ne veut pas
     * @return null si il n'y a pas d'autre partie
     */
    public synchronized String getRandomWaitingGameExcept(int... port) {
        if (waitingGames.size() != 1) {
            synchronized (waitingGames) {
                Iterator<Integer> iter = waitingGames.iterator();
                while (iter.hasNext()) {
                    int gamePort = iter.next();
                    boolean validGame = true;
                    for (int gport : port) {
                        if (gport == gamePort) {
                            validGame = false;
                        }
                    }
                    if (validGame)
                        return getGame(gamePort);

                }
            }
        }
        return null;
    }

    /**
     * @param game La partie que l'on ne veut pas
     * @return null si il n'y a pas d'autre partie
     */
    @Deprecated
    public synchronized String getRandomWaitingGameExcept(String game) {
        if (waitingGames.size() != 1) {
            synchronized (waitingGames) {
                Iterator<Integer> iter = waitingGames.iterator();
                while (iter.hasNext()) {
                    int gamePort = iter.next();
                    if ((gameType + gamePort) != game) {
                        return getGame(gamePort);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Permet de récupérer une partie valide ou l'on pourra téléporter le
     * joueur. Si cette partie est déjà lancée, la partie sera ajouté
     * dans les parties lancées et sera supprimée des parties en attente.
     *
     * @return Renvoie une partie valide
     */
    public synchronized String findValidGame(final int requiredSlots) {
        if (hasWaitingGames()) {
            System.out.println("GameForFindGame: " + gameType);
            System.out.println("OptionForFindGame: " + option);

            synchronized (waitingGames) {
                List<Integer> waitingGamesCopy = new ArrayList<Integer>(waitingGames);
                Iterator<Integer> iter = waitingGamesCopy.iterator();
                while (iter.hasNext()) {
                    int port = iter.next();
                    String serverName = gameType + port;
                    System.out.println("Game: " + serverName);

                    RedisRequestData data = new RedisRequestHandler(serverName).getData();
                    if (data == null) {
                        Logger.getGlobal().severe("\nError during receiving information about : " + serverName);
                        Logger.getGlobal().severe("\nTrying seconde time to : " + serverName);
                        RedisRequestData data2 = new RedisRequestHandler(serverName).getData();
                        if (data2 == null) {
                            Logger.getGlobal().severe("\nSecond ping didn't work delete server : " + serverName);
                            RedisDataSender.getPublisher.publish("delete#" + gameType + "#" + port);
                            break;
                        } else {
                            Logger.getGlobal().severe("\nReceived information with second ping ! : " + serverName);
                            data = data2;
                        }

                    }
                    System.out.println("data '" + data.getMotd() + "'"); //TODO
                    if (data.getMotd().equalsIgnoreCase("§2Ouvert")) {
                        int freeSlots = data.getMaxPlayers() - data.getOnlinePlayers();
                        if (freeSlots >= requiredSlots) {
                            return serverName;
                        }
                    } else if (data.getMotd().equals("§cEn jeu")) {
                        addStartedGame(port);
                    } else if (data.getMotd().equals("§l§cFermé")) {
                        addStartedGame(data.getPort());
                        removeStartedGame(data.getPort(), true);
                    } else if (data.getMotd().equals("§cFin de partie...")) {
                        removeStartedGame(data.getPort(), true);
                    }
                }
            }
        }
        return null;
    }

    public RedisRequestData findBetterGame(int requiredSlots) {
        RedisRequestData bestServer = null;
        synchronized (waitingGames) {
            Iterator<Integer> iter = waitingGames.iterator();
            while (iter.hasNext()) {
                int waitingGame = iter.next();
                RedisRequestData data = pingServer(gameType + waitingGame);
                if (data == null) break;
                if (data.getMotd().equalsIgnoreCase("§2Ouvert")) {
                    int freeSlots = data.getMaxPlayers() - data.getOnlinePlayers();
                    if (freeSlots >= requiredSlots) {
                        if (bestServer == null) {
                            bestServer = data;
                        } else {
                            if (bestServer.getOnlinePlayers() < data.getOnlinePlayers()) {
                                bestServer = data;
                            }
                        }
                    }
                }
            }
        }
        return bestServer;
    }

    /**
     * Permet de récupérer les données sur un serveur
     *
     * @param serverName le nom du serveur a ping
     * @return object contenant les données
     */
    private RedisRequestData pingServer(String serverName) {
        return new RedisRequestHandler(serverName).getData();
    }

    /**
     * Permet de savoir si il y a des parties en attentes
     *
     * @return boolean si il y a des parties de libre
     */
    public synchronized boolean hasWaitingGames() {
        if (waitingGames.size() != 0)
            return true;
        return false;
    }

    /**
     * Renvoie une partie qui permet par la suite de tp le joueur
     * dedans
     *
     * @return une partie
     */
    public synchronized String getRandomWaitingGame() {
        if (hasWaitingGames()) {
            synchronized (waitingGames) {
                Iterator<Integer> iter = waitingGames.iterator();
                return getGame(iter.next());
            }
        }
        return null;
    }

    /**
     * Permet d'obtenir une partie
     *
     * @param port Le port de la partie
     * @return gameType + port
     */
    public synchronized String getGame(int port) {
        if (gameExist(port))
            return gameType + port;
        return null;
    }

    /**
     * Permet de savoir si la partie existe ou pas donc si
     * le port est utilisé dans ce type de jeu.
     *
     * @param port Le port de la partie
     * @return boolean si la partie existe
     */
    public synchronized boolean gameExist(int port) {
        return waitingGames.contains(port) || startedGames.contains(port);
    }

    /**
     * Permet de savoir si la partie (port) est en attente
     *
     * @param port Le port de la partie
     * @return boolean si la partie est en attente
     */
    public synchronized boolean isWaitingGame(int port) {
        if (gameExist(port))
            if (waitingGames.contains(port))
                return true;
        return false;

    }

    /**
     * @return Le type de game
     */
    public String getGameType() {
        return gameType;
    }

    /**
     * @return Les ports des parties non lancées
     */
    public List<Integer> getWaitingGamesPort() {
        return this.waitingGames;
    }

    /**
     * @return Les ports des parties lancées
     */
    public List<Integer> getStartedGamesGamesPort() {
        return this.startedGames;
    }

    /**
     * @param port Le port de la partie en attente
     */
    public synchronized void addWaitingGame(int port) {
        this.waitingGames.add(port);
    }

    /**
     * Supprime la partie des parties en attente
     *
     * @param port Le port de la partie lancée
     */
    public synchronized void addStartedGame(int port) {
        if (waitingGames.contains(port))
            waitingGames.remove((Object) port);
        this.startedGames.add(port);
    }


    /**
     * On ajoute le port dans la liste des ports disponible
     *
     * @param port Le port de la partie à supprimé
     */
    public synchronized void removeStartedGame(int port, boolean delete) {
        if (waitingGames.contains(port))
            this.waitingGames.remove((Object) port);
        // this.waitingGames.remove(Arrays.asList(port));
        if (startedGames.contains(port))
            this.startedGames.remove((Object) port);
        // this.startedGames.remove(Arrays.asList(port));
        if (!Main.freePort.contains(port))
            Main.freePort.add(port);
        if (delete) {
            RedisDataSender.getPublisher.publish("delete#" + gameType + "#" + port);
        }
    }

    /**
     * @return Le nombre de joueurs max par partie
     */
    public int getMaxPlayers() {
        return this.maxPlayers;
    }


    /**
     * @return renvoie le mode de jeux
     */

    public String getOption() {
        return this.option;
    }

    /**
     * Méthode permettant de dire qu'une partie a été crée
     * récemment.
     */
    public void setGameRecentlyCreated() {
        gameRecentlyCreated = true;
        cooldownGameCreated();
    }


    /**
     * @return boolean permettant de savoir si une partie a
     * été crée récemment
     */
    public boolean gameRecentlyCreated() {
        return this.gameRecentlyCreated;
    }

    /**
     * Cooldown pour créer une partie
     * call automatiquement
     */
    private void cooldownGameCreated() {
        if (gameRecentlyCreated) {
            System.out.println("Game " + option + " recently created, wait few seconds");
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            gameRecentlyCreated = false;
                            System.out.println("Game " + option + " can create game");

                        }
                    },
                    6000
                    );
        }
    }

}
