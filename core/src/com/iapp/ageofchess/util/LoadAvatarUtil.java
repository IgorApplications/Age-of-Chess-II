package com.iapp.ageofchess.util;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Gdx2DPixmap;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.iapp.ageofchess.ChessApplication;
import com.iapp.ageofchess.multiplayer.Account;
import com.iapp.rodsher.util.Pair;

import java.io.IOException;

public class LoadAvatarUtil {

    public static Pair<TextureRegionDrawable, Texture> loadAvatar(Account account) {
        TextureRegionDrawable avatarDrawable;
        Texture avatarTexture = null;

        if (account.getAvatar().length == 0) {
            avatarDrawable = new TextureRegionDrawable(
                    ChessApplication.self().getAssetManager().findChessRegion("cross"));
        } else {
            var avatarBytes = account.getAvatar();
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
            avatarDrawable = new TextureRegionDrawable(
                ChessApplication.self().getAssetManager().findChessRegion("cross"));
        }

        return new Pair<>(avatarDrawable, avatarTexture);
    }

    public static Image getOnline(Account account) {
        if (account.isOnlineNow()) {

            var login = account.getLogins().get(account.getLogins().size() - 1);
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
