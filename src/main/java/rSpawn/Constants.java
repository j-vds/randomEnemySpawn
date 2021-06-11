package rSpawn;

import mindustry.gen.Iconc;

public class Constants {
    static int BORDEROFFSET = 10;
    static float MINCOREDIST = 25f;

    static String INFOMSG = Iconc.settings + " %02d:%02d \n " + Iconc.unitDagger + "(%4s;%4s)";
    static String MSGAMOUNT = Iconc.settings + " %02d:%02d \n " + Iconc.unitDagger + " x %s";
    static String HELPMSG = "[orange]RandomEnemySpawn[]\nAn amount of enemies will spawn on a random location. They will consist of daggers or flares.\n\n[sky]Made by Fishbuilder[]";


    static int DEFAULTTIME = 90; //210
    static int DEFAULTAMOUNT = 1;
    static int DEFAULTFIRSTWAVE = 180;
    static int DEFAULTBETWEENWAVES = 30;

    //maybe small randomisation
}
