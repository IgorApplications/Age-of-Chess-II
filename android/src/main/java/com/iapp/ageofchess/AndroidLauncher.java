package com.iapp.ageofchess;

import android.os.Bundle;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.iapp.ageofchess.util.Cheats;
import com.iapp.ageofchess.util.LaunchMode;

/** Launches the Android application. */
public class AndroidLauncher extends RdAndroidLauncher {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useAccelerometer = false;
        config.useCompass = false;

        initialize(new ChessApplication(this, LaunchMode.SERVER, Cheats.USER), config);
    }
}
