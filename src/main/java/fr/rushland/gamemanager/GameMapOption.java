package fr.rushland.gamemanager;


import java.util.ArrayList;

import fr.rushland.gamemanager.utils.CodeUtils;

/**
 * Created by Zaral on 11/04/2016.
 */
public class GameMapOption {

    private String gameType;
    private String gameOption;
    private ArrayList<String> listMap = new ArrayList<String>();
    private int maxPlayers;
    private boolean team = false;
    private String name;
    private int order;

    public GameMapOption(String gameType, String option, ArrayList<String> listMap, int maxPlayers, String name, boolean team) {
        this.gameType = gameType;
        this.gameOption = option;
        this.listMap = listMap;
        this.maxPlayers = maxPlayers;
        this.name = name;
        this.order = order;
        this.team = team;
        Main.listOption.add(this);
      //  Main.getInstance().getQueueManager().gameMapOption.add(this);
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
