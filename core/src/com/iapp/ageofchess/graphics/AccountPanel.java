package com.iapp.ageofchess.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.iapp.ageofchess.ChessApplication;
import com.iapp.ageofchess.activity.multiplayer.MultiplayerScenariosActivity;
import com.iapp.ageofchess.controllers.CreationController;
import com.iapp.ageofchess.controllers.multiplayer.MultiplayerScenariosController;
import com.iapp.ageofchess.multiplayer.Account;
import com.iapp.ageofchess.multiplayer.Match;
import com.iapp.ageofchess.multiplayer.MultiplayerEngine;
import com.iapp.ageofchess.util.ChessAssetManager;
import com.iapp.ageofchess.util.ChessConstants;
import com.iapp.rodsher.actors.RdDialog;
import com.iapp.rodsher.actors.RdImageTextButton;
import com.iapp.rodsher.actors.RdScrollPane;
import com.iapp.rodsher.actors.RdTable;
import com.iapp.rodsher.screens.RdApplication;
import com.iapp.rodsher.util.OnChangeListener;
import com.iapp.rodsher.util.RdI18NBundle;
import com.iapp.rodsher.util.WindowUtil;

import java.util.List;
import java.util.function.Consumer;

public class AccountPanel extends Table {

    private final RdTable controls = new RdTable();
    private RdImageTextButton coins;
    private AvatarView avatarView;
    private ImageButton games, see, settings;
    private Consumer<List<Match>> onMatches;

    private Account account;
    private Consumer<Long> seeAccount;
    private Consumer<Long> editAccount;
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

    public void initListeners(Account account,
                              Consumer<Long> seeAccount,
                              Consumer<Long> editAccount) {
        this.account = account;
        this.seeAccount = seeAccount;
        this.editAccount = editAccount;

        if (initListeners) return;
        initListeners = true;

        games.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                showGames(AccountPanel.this.account);
            }
        });

        see.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                AccountPanel.this.seeAccount.accept(account.getId());
            }
        });

        settings.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                AccountPanel.this.editAccount.accept(account.getId());
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

        controls.add(games).fillY();
        controls.add(see).fillY();
        controls.add(settings).fillY();
        controls.add(coins).width(192);
        updateTable();
    }

    public void updateTable() {
        clear();
        add(avatarView).size(100);
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

    private void showGames(Account account) {
        RdI18NBundle strings = RdApplication.self().getStrings();
        RdDialog games = new RdDialog(strings.get("viewing_your_games"));
        games.getLoading().setVisible(true);

        RdApplication.self().addDialog(games, dialog -> {
            var viewport = RdApplication.self().getViewport();
            if (dialog != null) dialog.setHeight(viewport.getWorldHeight() - 30);
            WindowUtil.resizeCenter(dialog);
        });

        RdTable content = new RdTable();
        content.align(Align.topLeft);
        RdScrollPane scrollPane = new RdScrollPane(content);
        games.getContentTable().add(scrollPane)
            .expand().fill().pad(5, 5, 5, 5);

        games.show(RdApplication.self().getStage());
        games.setWidth(900);
        RdApplication.self().resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        onMatches = matches -> {
            MultiplayerEngine.self().removeOnMatches(onMatches);

            for (Match match : matches) {
                if (match.getWhitePlayerId() == account.getId()
                    || match.getBlackPlayerId() == account.getId()) {

                    content.add(
                        new MultiplayerMatchView(match,
                            new OnChangeListener() {
                                @Override
                                public void onChange(Actor actor1) {
                                    games.hide(Actions.run(() ->
                                        RdApplication.self().setScreen(
                                            new MultiplayerScenariosActivity(match))));
                                }
                            },
                            null
                        ))
                        .expandX().fillX().padBottom(5).row();
                }
            }
            games.getLoading().setVisible(false);

        };
        MultiplayerEngine.self().addOnMatches(onMatches);
    }
}
