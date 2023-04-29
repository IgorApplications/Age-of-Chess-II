package com.iapp.ageofchess.graphics;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.iapp.ageofchess.multiplayer.Account;
import com.iapp.ageofchess.util.ChessAssetManager;
import com.iapp.ageofchess.util.ChessConstants;
import com.iapp.rodsher.actors.RdImageTextButton;
import com.iapp.rodsher.actors.RdTable;

public class AccountPanel extends Table {

    private final RdTable controls = new RdTable();
    private RdImageTextButton coins;
    private ImageButton games, see, settings;
    private AvatarView avatarView;

    public AccountPanel() {}

    public AvatarView getAvatarView() {
        return avatarView;
    }

    public RdTable getControls() {
        return controls;
    }

    public void update(AvatarView avatarView) {
        clear();
        controls.clear();

        games = new ImageButton(ChessAssetManager.current().getAccountPaneStyle());
        games.add(new Image(ChessAssetManager.current().findChessRegion("menu_games")));
        see = new ImageButton(ChessAssetManager.current().getAccountPaneStyle());
        see.add(new Image(ChessAssetManager.current().findChessRegion("menu_see_acc")));
        settings = new ImageButton(ChessAssetManager.current().getAccountPaneStyle());
        settings.add(new Image(ChessAssetManager.current().findChessRegion("menu_settings_account")));

        coins = new RdImageTextButton(getCoinsStr(ChessConstants.account.getCoins()),
                ChessAssetManager.current().getCoinsStyle());
        coins.getLabelCell().align(Align.right);
        coins.padLeft(10);
        coins.padRight(10);

        controls.add(games);
        controls.add(see);
        controls.add(settings);
        controls.add(coins).width(192);
        this.avatarView = avatarView;

        add(avatarView).size(96, 96);
        add(controls);
    }

    public void update(Account account) {
        coins.setText(getCoinsStr(ChessConstants.account.getCoins()));
        avatarView.update(account, 80);
    }

    private String getCoinsStr(long coins) {
        if (coins > 999_999) return "[GOLD]999999";
        return "[GOLD]" + coins;
    }
}
