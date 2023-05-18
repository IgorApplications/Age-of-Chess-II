package com.iapp.ageofchess.ios;

import com.iapp.ageofchess.ChessApplication;
import com.iapp.ageofchess.services.ApplicationMode;
import com.iapp.ageofchess.services.Cheats;
import com.iapp.ageofchess.services.ServerMode;
import org.robovm.apple.foundation.NSAutoreleasePool;
import org.robovm.apple.uikit.UIApplication;

import com.badlogic.gdx.backends.iosrobovm.IOSApplication;
import com.badlogic.gdx.backends.iosrobovm.IOSApplicationConfiguration;

/** Launches the iOS (RoboVM) application. */
public class IOSLauncher extends RdIOSLauncher {
    @Override
    protected IOSApplication createApplication() {
        IOSApplicationConfiguration configuration = new IOSApplicationConfiguration();
        return new IOSApplication(new ChessApplication(this, ServerMode.SERVER, ApplicationMode.RELEASE, Cheats.USER), configuration);
    }

    public static void main(String[] argv) {
        NSAutoreleasePool pool = new NSAutoreleasePool();
        UIApplication.main(argv, null, IOSLauncher.class);
        pool.close();
    }
}
