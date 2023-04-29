package com.iapp.rodsher.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.iapp.rodsher.actors.RdLabel;
import com.iapp.rodsher.util.CallListener;

import java.util.Arrays;

/**
 * Application debugging tool
 * @author Igor Ivanov
 * @version 1.0
 * */
public final class RdLogger {

    /** explanatory text on crushed window */
    private static String description = "IgorApplications application crushed, report send!\nSorry for the inconvenience :-(";
    /** actions in case of a fatal error */
    private static CallListener OnFatal = () -> {};
    /** fatal activity text styles */
    private static RdLabel.RdLabelStyle descStyle, logStyle;

    /**
     * sets explanatory text on crushed window
     * @param description - explanation of what is happening
     * */
    public static void setDescription(String description) {
        RdLogger.description = description;
    }

    /**
     * sets actions in case of a fatal error
     * @param onFatal - actions
     * */
    public static void setOnFatal(CallListener onFatal) {
        RdLogger.OnFatal = onFatal;
    }

    /** sets stack trace style */
    public static void setLogStyle(RdLabel.RdLabelStyle logStyle) {
        RdLogger.logStyle = logStyle;
    }

    /**
     * sets the style of the message to the user
     * @see RdLogger#description
     * */
    public static void setDescStyle(RdLabel.RdLabelStyle descriptionStyle) {
        RdLogger.descStyle = descriptionStyle;
    }

    /** Displays a crushed window with a description of the error
     * @param error - application exception
     * */
    public static Screen showFatalScreen(Throwable error) {
        var loggingScreen = new LoggingActivity(getDescription(error), description,
                logStyle, descStyle, OnFatal);
        RdApplication.self().setScreen(loggingScreen);

        return loggingScreen;
    }

    /**
     * returns the full description of the error
     * @param error - any error or exception
     * */
    public static String getDescription(Throwable error) {

        var logText = new StringBuilder(parseThrowable(error));
        var count = 0;
        if (error.getCause() != null) {
            logText.append("\n\nCause-").append(parseThrowable(error.getCause()));
        }

        if (error.getSuppressed() != null) {
            for (Throwable suppressed : error.getSuppressed()) {
                logText.append("\n\nSuppressed-").append(count++)
                    .append(":\n").append(parseThrowable(suppressed));
            }
        }
        return logText.toString();
    }

    /** @return the number of frames per second */
    public static int getFPS() {
        return Gdx.graphics.getFramesPerSecond();
    }

    /** @return the amount of RAM the application is using in Mb */
    public static long getRAM() {
        long bytes  = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        return bytes / 1024 / 1024;
    }

    /** Logs system information */
    public static void logSysInfo() {
        var properties = System.getProperties();
        var keys = properties.keys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            String value = (String) properties.get(key);
            Gdx.app.log(key, value);
        }
    }

    private static String parseThrowable(Throwable t) {
        var logText = Arrays.toString(t.getStackTrace());
        logText = logText.replaceAll(",", ",\n");
        logText = t + "\n" + logText;

        return logText;
    }

    private RdLogger() {}
}
