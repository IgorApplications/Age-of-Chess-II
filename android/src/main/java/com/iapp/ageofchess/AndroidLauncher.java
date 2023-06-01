package com.iapp.ageofchess;

import android.os.Bundle;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.github.czyzby.websocket.CommonWebSockets;

/** Launches the Android application. */
public class AndroidLauncher extends RdAndroidLauncher {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useAccelerometer = false;
        config.useCompass = false;

        CommonWebSockets.initiate();
        initialize(new ChessApplication(this), config);
    }
}
