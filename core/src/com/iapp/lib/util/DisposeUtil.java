package com.iapp.lib.util;

import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Null;

/**
 * Structure Cleanup Utility
 * @version 1.0
 * */
public class DisposeUtil {

    /** safe clearing with null checking */
    public static void dispose(@Null Disposable disposable) {
        if (disposable != null) disposable.dispose();
    }
}
