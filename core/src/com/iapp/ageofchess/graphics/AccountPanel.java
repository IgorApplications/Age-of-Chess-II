package com.iapp.ageofchess.graphics;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.iapp.lib.web.Account;
import com.iapp.ageofchess.services.ChessAssetManager;
import com.iapp.ageofchess.services.ChessConstants;
import com.iapp.lib.ui.actors.RdImageTextButton;
import com.iapp.lib.ui.actors.RdTable;
import com.iapp.lib.util.OnChangeListener;

import java.util.function.Consumer;

public class AccountPanel extends Table {

    private final RdTable controls = new RdTable();
    private RdImageTextButton coins;
    private AvatarView avatarView;
    private ImageButton games, see, settings;

    private Account account;
    private Consumer<Long> seeAccount;
    private Consumer<Long> editAccount;
    private Consumer<Account> onMatches;

    private boolean initListeners;

    public AccountPanel() {
        init();
    }

    public AvatarView getAvatarView() {
        return avatarView;
    }

    public RdTable getControls() {
        return controls;
    }

    public void setOnMatches(Consumer<Account> onMatches) {
        this.onMatches = onMatches;
    }

    public void updateListeners(Account account,
                                Consumer<Long> seeAccount,
                                Consumer<Long> editAccount,
                                Consumer<Account> onMatches) {
        this.account = account;
        this.seeAccount = seeAccount;
        this.editAccount = editAccount;
        this.onMatches = onMatches;

        if (initListeners) return;
        initListeners = true;

        games.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                AccountPanel.this.onMatches.accept(AccountPanel.this.account);
            }
        });

        see.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                AccountPanel.this.seeAccount.accept(AccountPanel.this.account.getId());
            }
        });

        settings.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                AccountPanel.this.editAccount.accept(AccountPanel.this.account.getId());
            }
        });
    }

    private void init() {
        setBackground(new NinePatchDrawable(new NinePatch(
                ChessAssetManager.current().findChessRegion("mode_app"),
                390, 10, 0, 0)));
        align(Align.topRight);
        padRight(10);

        avatarView = new AvatarView(ChessAssetManager.current().getAvatarStyle());
        games = new ImageButton(ChessAssetManager.current().getGamesStyle());
        see = new ImageButton(ChessAssetManager.current().getProfileStyle());
        settings = new ImageButton(ChessAssetManager.current().getSettingsStyle());

        coins = new RdImageTextButton("", ChessAssetManager.current().getCoinsStyle());
        coins.getLabelCell().align(Align.right);
        coins.padLeft(10);
        coins.padRight(10);

        controls.add(games).minSize(100).fillY();
        controls.add(see).minSize(100).fillY();
        controls.add(settings).minSize(100).fillY();
        controls.add(coins).minHeight(100).width(256);
        updateTable();
    }

    public void updateTable() {
        clear();
        add(avatarView).size(128);
        add(controls);
    }

    public void update(Account account, byte[] avatar) {
        avatarView.update(account, avatar);
        coins.setText(getCoinsStr(ChessConstants.loggingAcc.getCoins()));
    }

    private String getCoinsStr(long coins) {
        if (coins > 999_999) return "[GOLD]999999";
        return "[GOLD]" + coins;
    }


}
