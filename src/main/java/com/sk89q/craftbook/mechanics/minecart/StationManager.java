package com.sk89q.craftbook.mechanics.minecart;

import java.util.HashMap;
import java.util.Map;

public class StationManager {

    private static Map<String, String> stationSelection = new HashMap<>();

    public static String getStation(String playerName) {

        return stationSelection.get(playerName);
    }

    public static void setStation(String playerName, String stationName) {

        stationSelection.put(playerName, stationName);
    }
}