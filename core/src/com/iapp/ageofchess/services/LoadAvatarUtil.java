package com.iapp.ageofchess.services;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Gdx2DPixmap;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.iapp.ageofchess.ChessApplication;
import com.iapp.lib.web.Account;
import com.iapp.lib.util.Pair;

import java.io.IOException;

public class LoadAvatarUtil {

    public static Pair<TextureRegionDrawable, Texture> loadAvatar(byte[] avatarBytes) {
        TextureRegionDrawable avatarDrawable;
        Texture avatarTexture = null;

        if (avatarBytes == null || avatarBytes.length == 0) {
            avatarDrawable = new TextureRegionDrawable(
                    ChessApplication.self().getAssetManager().findChessRegion("cross"));
        } else {
            Gdx2DPixmap gdxPixmap;
            try {
                gdxPixmap = new Gdx2DPixmap(avatarBytes, 0, avatarBytes.length, 0);
                var pixmap = new Pixmap(gdxPixmap);
                avatarTexture = new Texture(pixmap);
                pixmap.dispose();
                avatarDrawable = new TextureRegionDrawable(avatarTexture);
            } catch (IOException e) {
                avatarDrawable = new TextureRegionDrawable(
                        ChessApplication.self().getAssetManager().findChessRegion("cross"));
            }
       }

        return new Pair<>(avatarDrawable, avatarTexture);
    }

    public static Image getOnline(Account account) {
        if (account.isOnlineNow()) {

            var login = account.getLogin();
            Image online;
            if (login.getSystem().contains("android")
                    || login.getSystem().contains("ios")) {

                online = new Image(ChessAssetManager.current().findChessRegion("ig_phone"));
                online.setSize(22, 34);

            } else {

                online = new Image(ChessAssetManager.current().findChessRegion("online"));
                online.setSize(15, 15);
            }
            return online;
        }
        return null;
    }
}
