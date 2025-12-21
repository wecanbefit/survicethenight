package com.stn.survival.client.hud;

/**
 * Stores gamestage HUD data received from server.
 * Rendering disabled - data is used by other HUD elements.
 */
public class GamestageHudOverlay {

    // Cached data from server
    private static long currentDay = 0;
    private static int worldGamestage = 0;
    private static int playerDeaths = 0;
    private static int globalDeaths = 0;

    public static void register() {
        // Rendering disabled - data exposed via getters for other HUD elements
    }

    public static void updateData(long day, int gamestage, int playerD, int globalD) {
        currentDay = day;
        worldGamestage = gamestage;
        playerDeaths = playerD;
        globalDeaths = globalD;
    }

    // Getters for other HUD elements to access the data
    public static long getCurrentDay() {
        return currentDay;
    }

    public static int getWorldGamestage() {
        return worldGamestage;
    }

    public static int getPlayerDeaths() {
        return playerDeaths;
    }

    public static int getGlobalDeaths() {
        return globalDeaths;
    }
}
