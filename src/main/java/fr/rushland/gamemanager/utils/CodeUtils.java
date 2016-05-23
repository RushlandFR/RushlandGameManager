package fr.rushland.gamemanager.utils;

import java.util.Random;

public class CodeUtils {

    public static int randomInt(int min, int max) {
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }

    public static String formatNPCType(String type) {
        if (type.equalsIgnoreCase("rush")) {
            return "Rush";
        } else if (type.equalsIgnoreCase("rushevent")) {
            return "RushEvent";
        } else if (type.equalsIgnoreCase("rushtheflag")) {
            return "RushTheFlag";
        } else if (type.equalsIgnoreCase("antwars")) {
            return "AntWars";
        } else if (type.equalsIgnoreCase("skywars")) {
            return "SkyWars";
        } else if (type.equalsIgnoreCase("buildchampion")) {
            return "BuildChampion";
        } else if (type.equalsIgnoreCase("divided")) {
            return "DividedTogether";
        } else if (type.equalsIgnoreCase("pvpbox")) {
            return "PVPBox";
        } else {
            return "ERROR";
        }
    }
}
