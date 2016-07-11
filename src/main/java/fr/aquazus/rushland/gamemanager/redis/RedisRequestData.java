package fr.aquazus.rushland.gamemanager.redis;

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
