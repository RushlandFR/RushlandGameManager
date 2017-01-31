package fr.aquazus.rushland.gamemanager;

import java.util.ArrayList;
import java.util.Random;

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

public class GameMapOption {

    private String gameType;
    private String gameOption;
    private ArrayList<String> listMap = new ArrayList<>();
    private int maxPlayers;
    private String name;
    private int order;
    private boolean team;
    private boolean noBow;
    private String tournamentCode;
    private boolean canCreateTournament;

    public GameMapOption(String gameType, String option, ArrayList<String> listMap, int maxPlayers, String name, int order, boolean team, boolean noBow, String tournamentCode, boolean canCreateTournament) {
        this.gameType = gameType;
        this.gameOption = option;
        this.listMap = listMap;
        this.maxPlayers = maxPlayers;
        this.name = name;
        this.order = order;
        this.team = team;
        this.noBow = noBow;
        this.tournamentCode = tournamentCode;
        this.canCreateTournament = canCreateTournament;
    }

    public boolean withTeam() {
        return this.team;
    }

    public String getGameType() {
        return this.gameType;
    }

    public String getGameOption() {
        return this.gameOption;
    }

    public String getName() {
        return this.name;
    }

    public int getOrder() {
        return this.order;
    }
    
    public boolean getNoBow() {
        return this.noBow;
    }

    public String getRandomMap() {
        int random = new Random().nextInt(listMap.size());
        return listMap.get(random);
    }

    public int getMaxPlayers() {
        return this.maxPlayers;
    }
    
    public String getTournamentCode() {
        return this.tournamentCode;
    }
    
    public boolean canCreateTournament() {
        return this.canCreateTournament;
    }
}
