package com.iapp.rodsher.util;

/**
 * SystemValidator is a class for determining the version
 * of the operating system and its architecture
 * @author AlexBoy
 * @version 1.0
 */
public final class SystemValidator {

    /** Windows OS version */
    public static final String WINDOWS_XP = "windows xp";
    public static final String WINDOWS_VISTA = "windows vista";
    public static final String WINDOWS_7 = "windows 7";
    public static final String WINDOWS_8 = "windows 8";
    public static final String WINDOWS_10 = "windows 10";
    public static final String WINDOWS_11 = "windows 11";

    /** android OS version */
    public static final String ANDROID_7 = "android 7";
    public static final String ANDROID_8 = "android 8";
    public static final String ANDROID_9 = "android 9";
    public static final String ANDROID_10 = "android 10";
    public static final String ANDROID_11 = "android 11";
    public static final String ANDROID_12 = "android 12";
    public static final String ANDROID_13 = "android 13";

    /** processor architecture */
    public static final String BIT_32 = "32";
    public static final String BIT_64 = "64";

    /** returns this processor name */
    public static String getProcessorName() {
        var partsProcessor = System.getenv("PROCESSOR_ARCHITECTURE");
        return partsProcessor.replaceAll("\\d*", "").toLowerCase();
    }

    /** returns this operating system version */
    public static String getOperationSystem() {
        return System.getProperty("os.name").toLowerCase();
    }

    /** returns this processor architecture */
    public static String getProcessorArchitecture() {
        var partsProcessor = System.getenv("PROCESSOR_ARCHITECTURE");
        return partsProcessor.replaceAll("\\D*", "").toLowerCase();
    }
}