package com.iapp.ageofchess;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.iapp.lib.ui.screens.Launcher;
import com.iapp.lib.ui.screens.RdApplication;
import com.iapp.lib.util.CallListener;
import com.iapp.lib.web.Account;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class RdAndroidLauncher extends AndroidApplication implements Launcher {

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private String[] permissionsStorage;

    private final List<Consumer<Boolean>> openList = new CopyOnWriteArrayList<>();
    private final List<Consumer<Boolean>> hideList = new CopyOnWriteArrayList<>();
    private Consumer<Boolean> verifyListener;
    private CallListener callListener;
    private ExecutorService executorService;

    GoogleSignInOptions gso;
    GoogleSignInClient gsc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 33) {
            permissionsStorage = new String[] {android.Manifest.permission.READ_MEDIA_IMAGES};
        } else {
            permissionsStorage = new String[] {Manifest.permission.READ_EXTERNAL_STORAGE};
        }

        KeyboardVisibilityEvent.setEventListener(this, isOpen -> {
            if (isOpen) {
                for (Consumer<Boolean> onKeyboard : openList) {
                    if (onKeyboard == null) continue;
                    RdApplication.postRunnable(() -> onKeyboard.accept(true));
                    hideList.add(onKeyboard);
                }
                openList.clear();
            } else  {
                for (Consumer<Boolean> onKeyboard : hideList) {
                    if (onKeyboard == null) continue;
                    RdApplication.postRunnable(() -> onKeyboard.accept(false));
                }
                hideList.clear();
            }
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
    public void addOnKeyboard(Consumer<Boolean> onKeyboard) {
        openList.add(onKeyboard);
    }

    @Override
    public void googleLogin() {
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        gsc = GoogleSignIn.getClient(this, gso);

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if(acct != null) {
            System.out.println(acct);
        } else {
            Intent signInIntent = gsc.getSignInIntent();
            startActivityForResult(signInIntent,1000);
        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                System.out.println(task.getResult(ApiException.class).getEmail());
            } catch (ApiException e) {
                Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        }

    }
}
