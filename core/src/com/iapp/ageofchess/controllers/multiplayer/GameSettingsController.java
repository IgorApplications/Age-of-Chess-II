package com.iapp.ageofchess.controllers.multiplayer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.github.tommyettinger.textra.Line;
import com.iapp.ageofchess.ChessApplication;
import com.iapp.ageofchess.multiplayer.Match;
import com.iapp.ageofchess.multiplayer.MultiplayerEngine;
import com.iapp.ageofchess.services.ChessAssetManager;
import com.iapp.ageofchess.services.ChessConstants;
import com.iapp.lib.ui.actors.RdDialog;
import com.iapp.lib.ui.actors.RdLabel;
import com.iapp.lib.ui.actors.RdScrollPane;
import com.iapp.lib.ui.actors.RdTable;
import com.iapp.lib.ui.screens.RdApplication;
import com.iapp.lib.ui.screens.RdAssetManager;
import com.iapp.lib.ui.widgets.AccountView;
import com.iapp.lib.util.OnChangeListener;
import com.iapp.lib.util.RdI18NBundle;
import com.iapp.lib.web.Account;
import com.iapp.lib.web.RankType;

import java.util.List;
import java.util.function.Consumer;

public class GameSettingsController {

    private final RdI18NBundle strings;
    private RdDialog settings;

    public GameSettingsController() {
        strings = RdApplication.self().getStrings();
    }

    public void showSettings(Match match) {
        showSettings(match, RdDialog::hide);
    }

    public void showSettings(Match match, Consumer<RdDialog> onCancel) {
        settings = new RdDialog(strings.format("name_match", match.getName()));
        settings.getLoading().setVisible(true);
        settings.align(Align.topLeft);
        settings.setOnCancel(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                onCancel.accept(settings);
            }
        });

        settings.show(RdApplication.self().getStage());
        RdApplication.self().addDialog(settings, 900, 700, 10, 10);

        RdTable content = new RdTable();
        content.align(Align.topLeft);
        RdScrollPane scrollPane = new RdScrollPane(content);
        settings.getContentTable().add(scrollPane).expand().fill();
        settings.removeActor(settings.getButtonTable());

        Image iconClock = new Image(ChessAssetManager.current().findRegion("icon_clock"));
        iconClock.setScaling(Scaling.fit);
        Image iconRandom = new Image(ChessAssetManager.current().findRegion("icon_random"));
        iconRandom.setScaling(Scaling.fit);
        Image iconRank = new Image(ChessAssetManager.current().findChessRegion("cup"));
        iconRank.setScaling(Scaling.fit);
        iconRank.setScaling(Scaling.fit);
        Image iconSponsored = new Image(ChessAssetManager.current().findRegion("icon_sponsored"));
        iconSponsored.setScaling(Scaling.fit);

        RdTable information = new RdTable();
        information.align(Align.topLeft);
        information.setBackground(new NinePatchDrawable(
            new NinePatch(ChessApplication.self().getAssetManager().findRegion("lite_pane"),
                10,10,10,10)));
        content.add(information).expandX().fillX().pad(5, 5, 5, 5).row();

        MultiplayerEngine.self().getAccount(match.getCreatorId(), account -> {

            information.add(new RdLabel(strings.format("creator_match", account.getFullName())))
                .expandX().left().row();

            information.add(new RdLabel(strings.format("turns", match.getTurn())))
                .expandX().left().row();

            if (match.getRankType() == RankType.UNRANKED) {
                information.add(new RdLabel("[GOLD]" + strings.get("non_ranked")))
                    .expandX().left().row();
            } else {
                RdTable column1 = new RdTable();
                column1.add(iconRank);
                column1.add(new RdLabel("[GOLD]" + defineRankType(match))).row();
                information.add(column1).expandX().left().row();
            }

            if (match.getSponsored() > 0) {
                RdTable column2 = new RdTable();
                column2.add(iconSponsored);
                column2.add(new RdLabel("[GOLD]" + match.getSponsored())).row();
                information.add(column2).expandX().left().row();
            }

            RdTable column3 = new RdTable();
            column3.add(iconClock);
            column3.add(new RdLabel(strings.format("time_by_move", getTimeByTurn(match.getTimeByTurn()))));
            information.add(column3)
                .expandX().left().row();
            information.add(new RdLabel(strings.format("time_white", getTimeByGame(match.getTimeByWhite()))))
                .expandX().left().row();
            information.add(new RdLabel(strings.format("time_black", getTimeByGame(match.getTimeByBlack()))))
                .expandX().left().row();

            if (match.isRandom()) {
                RdTable column4 = new RdTable();
                column4.add(iconRandom);
                column4.add(new RdLabel(strings.format("random")));
                information.add(column4).expandX().left();
            }

            addPlayers(match, content);
        });
    }

    private void addPlayers(Match match, RdTable content) {
        long[] players = match.getPlayers();
        if (players.length != 0) {
            MultiplayerEngine.self().getAccounts(players, accounts -> {

                for (Account account : accounts) {
                    AccountView accountView = new AccountView(MultiplayerEngine.self(), account,
                        new OnChangeListener() {
                            @Override
                            public void onChange(Actor actor) {
                                ChessConstants.accountController.seeAccount(account.getId(),
                                    List.of(settings));
                            }
                        });

                    String color;
                    RdLabel.RdLabelStyle style = new RdLabel.RdLabelStyle(
                        RdAssetManager.current().getSkin().get(RdLabel.RdLabelStyle.class));

                    if (account.getId() == match.getWhitePlayerId()) {
                        color = strings.get("white");
                        style.background = new TextureRegionDrawable(
                            ChessAssetManager.current().getWhiteTexture());
                        style.color = Color.BLACK;
                    } else {
                        color = strings.get("black");
                        style.background = new TextureRegionDrawable(
                            ChessAssetManager.current().getBlackTexture());
                    }

                    RdLabel colorLabel = new RdLabel(color, style);
                    RdTable column = new RdTable();
                    column.setBackground(new NinePatchDrawable(
                        new NinePatch(ChessApplication.self().getAssetManager().findRegion("lite_pane"),
                            10,10,10,10)));
                    column.add(accountView).expandX().left();
                    column.add(colorLabel).padRight(15).row();

                    content.add(column).expandX().fillX().pad(5, 5, 5, 5).row();
                }
                settings.getLoading().setVisible(false);

            });
        } else {
            content.add(new RdLabel("[#d7d7d7]" + strings.get("no_players"))).expandX().left();
            settings.getLoading().setVisible(false);
        }
    }

    private String defineRankType(Match match) {
        switch (match.getRankType()) {
            case BULLET: return strings.get("bullet");
            case BLITZ: return strings.get("blitz");
            case RAPID: return strings.get("rapid");
            case LONG: return strings.get("long");
        }
        throw new IllegalArgumentException("Rank type unknown");
    }

    private String getTimeByTurn(long millis) {
        if (millis == -1) return strings.get("infinity");

        long seconds = millis / 1000;
        if (seconds > 60) {
            long minutes = seconds / 60;
            if (minutes > 60) {
                long hours = minutes / 60;
                return strings.format("hours_by_move", hours);
            }
            return strings.format("min_by_move", minutes);
        }
        return strings.format("sec_by_move", seconds);
    }

    private String getTimeByGame(long millis) {
        if (millis == -1) return strings.get("infinity");

        long seconds = millis / 1000;
        if (seconds > 60) {
            long minutes = seconds / 60;
            if (minutes > 60) {
                long hours = minutes / 60;
                return strings.format("hours", hours);
            }
            return strings.format("minutes", minutes);
        }
        return strings.format("seconds", seconds);
    }
}
