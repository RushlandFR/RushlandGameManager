package fr.rushland.gamemanager.redis;

public class RedisRequestData {

    private final String serverId;
    private final String motd;
    private final String onlinePlayers;
    private final String maxPlayers;
    private final String port;

    public RedisRequestData(String serverId, String port , String motd, String onlinePlayers, String maxPlayers) {
        this.serverId = serverId;
        this.motd = motd;
        this.onlinePlayers = onlinePlayers;
        this.maxPlayers = maxPlayers;
        this.port = port;
    }

    public String getServerName() {
        return serverId;
    }

    public String getMotd() {
        if (motd != null) {
            return motd;
        }
        return "§4§lFermé";
    }

    public int getPort() {
        if (this.port != null) {
            return Integer.parseInt(port);
        }
        return 0;
    }

    public int getOnlinePlayers() {
        if (onlinePlayers != null) {
            return Integer.parseInt(onlinePlayers);
        }
        return 0;
    }

    public int getMaxPlayers() {
        if (maxPlayers != null) {
            return Integer.parseInt(maxPlayers);
        }
        return 0;
    }
}
