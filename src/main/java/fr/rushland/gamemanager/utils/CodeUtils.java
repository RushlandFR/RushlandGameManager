package fr.rushland.gamemanager.utils;

import java.util.Random;

/**
 * Created by Zaral on 11/04/2016.
 */
public class CodeUtils {

    public static int randomInt(int min, int max) {

        // NOTE: Usually this should be a field rather than a method
        // variable so that it is not re-seeded every call.
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
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
