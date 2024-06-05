package com.iapp.lib.ui.screens;

import com.badlogic.gdx.Gdx;
import com.iapp.lib.ui.actors.RdLabel;
import com.iapp.lib.util.CallListener;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Application debugging tool
 * @author Igor Ivanov
 * @version 1.0
 * */
public final class RdLogger {

    private static final RdLogger INSTANCE = new RdLogger();

    /** explanatory text on crushed window */
    private String description = "IgorApplications application crushed, please send a screenshot to igorapplications@gmail.com\nSorry for the inconvenience :-(";
    /** actions in case of a fatal error */
    private CallListener onFatal = () -> {};
    /** fatal activity text styles */
    private RdLabel.RdLabelStyle descStyle, logStyle;
    /** application version */
    private int version;
    /** application version build time */
    private String time;
    /** disable screen transitions */
    static boolean blockedTransition;

    public static RdLogger self() {
        return INSTANCE;
    }

    /** returns the application version */
    public int getVersion() {
        return version;
    }

    /** returns the build time of the application version */
    public String getTime() {
        return time;
    }

    /**
     * sets explanatory text on crushed window
     * @param description - explanation of what is happening
     * */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * sets actions in case of a fatal error
     * @param onFatal - actions
     * */
    public void setOnFatal(CallListener onFatal) {
        this.onFatal = onFatal;
    }

    /** sets stack trace style */
    public void setLogStyle(RdLabel.RdLabelStyle logStyle) {
        this.logStyle = logStyle;
    }

    /**
     * sets the style of the message to the user
     * @see RdLogger#description
     * */
    public void setDescStyle(RdLabel.RdLabelStyle descriptionStyle) {
       this.descStyle = descriptionStyle;
    }

    /** Displays a crushed window with a description of the error
     * @param error - application exception
     * */
    public Activity showFatalScreen(Throwable error) {
        blockedTransition = true;

        LoggingActivity loggingScreen = new LoggingActivity(getDescription(error), description,
                logStyle, descStyle, onFatal);
        RdApplication.self().setFatalScreen(loggingScreen);

        return loggingScreen;
    }

    /**
     * returns the full description of the error
     * @param error - any error or exception
     * */
    public String getDescription(Throwable error) {

        StringBuilder logText = new StringBuilder(parseThrowable(error));
        long count = 0;
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
    public int getFPS() {
        return Gdx.graphics.getFramesPerSecond();
    }

    /** @return the amount of RAM the application is using in Mb */
    public long getRAM() {
        long bytes  = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        return bytes / 1024 / 1024;
    }

    /** Logs system information */
    public void logSysInfo() {
        Properties properties = System.getProperties();
        Enumeration keys = properties.keys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            String value = (String) properties.get(key);
            Gdx.app.log(key, value);
        }
    }

    private String parseThrowable(Throwable t) {
        String logText = Arrays.toString(t.getStackTrace());
        logText = logText.replaceAll(",", ",\n");
        logText = t + "\n" + logText;

        return logText;
    }

    void setVersion(int version) {
        this.version = version;
    }

    void setTime(String time) {
        this.time = time;
    }

    private RdLogger() {}
}
