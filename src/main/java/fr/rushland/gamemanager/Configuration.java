package fr.rushland.gamemanager;

import lombok.Data;

public @Data class Configuration {

    private String game;
    private int[] portRange;
    private boolean debug;
}
