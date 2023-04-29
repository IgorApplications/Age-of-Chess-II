package com.iapp.ageofchess.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.iapp.ageofchess.ChessApplication;
import com.iapp.ageofchess.util.Cheats;
import com.iapp.ageofchess.util.LaunchMode;
import com.iapp.rodsher.util.SystemValidator;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {

    public static void main(String[] args) {
        createApplication();
    }

    private static Lwjgl3Application createApplication() {
        return new Lwjgl3Application(new ChessApplication(
            new RdLwjgl3Launcher(), LaunchMode.LOCAL, Cheats.DEVELOPER), getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setForegroundFPS(60);
        config.setWindowedMode(1530, 850);
        config.setResizable(false);
        config.setTitle("Age of Chess II");
        config.setWindowIcon("chess_icon128x128.png", "chess_icon32x32.png",
            "chess_icon64x64.png", "chess_icon16x16.png");

        // ANGLE is only supported on 64-bit Windows 10 and Windows 11
        if (SystemValidator.getProcessorArchitecture().equals(SystemValidator.BIT_64)
            && (SystemValidator.getOperationSystem().equals(SystemValidator.WINDOWS_10)
            || SystemValidator.getOperationSystem().equals(SystemValidator.WINDOWS_11))) {
            config.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.ANGLE_GLES20,
                2, 0);
        }

        return config;
    }
}
