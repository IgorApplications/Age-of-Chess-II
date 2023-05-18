package com.iapp.ageofchess.services;

import com.iapp.lib.web.Account;

public final class ChessConstants {

    public static final float WORLD_WIDTH = 900;
    public static final float WORLD_HEIGHT = 900;

    public static final String STORAGE_DIRECTORY = "age_of_chess";
    public static final String MAPS_DIRECTORY = "age_of_chess/maps";
    public static final String SETTINGS = "age_of_chess/settings";
    public static final String LOGS_DIRECTORY = "age_of_chess/logs";

    public static volatile String serverAPI;
    public static volatile LocalData localData;
    public static volatile Account loggingAcc;
}
