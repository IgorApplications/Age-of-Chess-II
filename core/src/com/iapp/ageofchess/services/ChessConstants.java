package com.iapp.ageofchess.services;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.iapp.ageofchess.controllers.multiplayer.AccountController;
import com.iapp.lib.ui.widgets.AccountPanel;
import com.iapp.lib.ui.widgets.ChatView;
import com.iapp.lib.web.Account;

public final class ChessConstants {

    public static final float WORLD_WIDTH = 900;
    public static final float WORLD_HEIGHT = 900;

    public static final String MAPS_DIRECTORY = "maps";
    public static final String SETTINGS = "settings";
    public static final String LOGS_DIRECTORY = "logs";
    public static final Files.FileType FILE_TYPE = Files.FileType.Local;


    public static volatile AccountPanel accountPanel;
    public static volatile ChatView chatView;
    public static volatile AccountController accountController;

    public static volatile String serverAPI;
    public static volatile LocalData localData;
    public static volatile Account loggingAcc;
}
