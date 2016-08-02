package fr.aquazus.rushland.gamemanager;

import java.util.ArrayList;

import fr.aquazus.rushland.gamemanager.utils.CodeUtils;

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
    private ArrayList<String> listMap = new ArrayList<String>();
    private int maxPlayers;
    private boolean team = false;
    private String name;
    private int order;

    public GameMapOption(String gameType, String option, ArrayList<String> listMap, int maxPlayers, String name, boolean team, int order) {
        this.gameType = gameType;
        this.gameOption = option;
        this.listMap = listMap;
        this.maxPlayers = maxPlayers;
        this.name = name;
        this.order = order;
        this.team = team;
    }

    public String getGameType() {
        return this.gameType;
    }

    public boolean withTeam() {
        return this.team;
    }

    public String getGameOption() {
        return this.gameOption;
    }

    public String getRandomMap() {
        int random = CodeUtils.randomInt(0, listMap.size()-1);
        return listMap.get(random);
    }

    public String getName() {
        return this.name;
    }

    public int getOrder() {
        return this.order;
    }

    public int getMaxPlayers() {
        return this.maxPlayers;
    }
}