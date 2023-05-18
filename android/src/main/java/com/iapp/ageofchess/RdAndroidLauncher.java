package com.iapp.ageofchess;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import androidx.core.app.ActivityCompat;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.iapp.lib.ui.screens.Launcher;
import com.iapp.lib.ui.screens.RdApplication;
import com.iapp.lib.util.CallListener;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class RdAndroidLauncher extends AndroidApplication implements Launcher {

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private String[] permissionsStorage;

    private Consumer<Boolean> onKeyboard;
    private Consumer<Boolean> verifyListener;
    private CallListener callListener;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 33) {
            permissionsStorage = new String[] {android.Manifest.permission.READ_MEDIA_IMAGES};
        } else {
            permissionsStorage = new String[] {Manifest.permission.READ_EXTERNAL_STORAGE};
        }

        KeyboardVisibilityEvent.setEventListener(this, isOpen -> {
            if (onKeyboard == null) return;
            RdApplication.postRunnable(() -> onKeyboard.accept(isOpen));
        });
    }

    @Override
    public void onBackPressed() {
        if (callListener == null) super.onBackPressed();
        else RdApplication.postRunnable(() -> callListener.call());
    }

    @Override
    public void setOnFinish(CallListener callListener) {
        this.callListener = callListener;
    }

    @Override
    public void initPool(int threads) {
        executorService = Executors.newFixedThreadPool(threads);
    }

    @Override
    public void execute(Runnable task) {
        executorService.execute(task);
    }

    @Override
    public long currentMillis() {
        return SystemClock.elapsedRealtime();
    }

    @Override
    public void verifyStoragePermissions(Consumer<Boolean> listener) {
        this.verifyListener = listener;

        ActivityCompat.requestPermissions(
            this,
            permissionsStorage,
            REQUEST_EXTERNAL_STORAGE);
    }

    @Override
    public boolean checkStoragePermissions() {
        for (var el : permissionsStorage) {
            if (ActivityCompat.checkSelfPermission(this, el) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public double getKeyboardHeight() {
        Rect rect = new Rect();
        View parent = this.getWindow().getDecorView();
        parent.getWindowVisibleDisplayFrame(rect);

        int screenHeight = parent.getRootView().getHeight();
        return (screenHeight - (rect.bottom - rect.top));
    }

    @Override
    public void setOnKeyboard(Consumer<Boolean> onKeyboard) {
        this.onKeyboard = onKeyboard;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE:

                if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    RdApplication.postRunnable(() -> verifyListener.accept(true));
                }  else {
                    RdApplication.postRunnable(() -> verifyListener.accept(false));
                }
        }

    }
}
