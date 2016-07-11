package fr.aquazus.rushland.gamemanager.utils;

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
