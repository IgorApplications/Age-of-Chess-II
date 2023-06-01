package com.iapp.ageofchess.graphics;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.iapp.ageofchess.ChessApplication;
import com.iapp.ageofchess.controllers.multiplayer.GameSettingsController;
import com.iapp.ageofchess.services.ChessAssetManager;
import com.iapp.lib.web.AccountType;
import com.iapp.ageofchess.multiplayer.Match;
import com.iapp.lib.web.RankType;
import com.iapp.ageofchess.services.ChessConstants;
import com.iapp.lib.ui.actors.RdImageTextButton;
import com.iapp.lib.ui.actors.RdLabel;
import com.iapp.lib.ui.actors.RdTable;
import com.iapp.lib.util.OnChangeListener;
import com.iapp.lib.util.RdI18NBundle;

public class MultiplayerMatchView extends Table {

    private final Match match;
    private final RdI18NBundle strings;
    private final GameSettingsController settingsController;

    public MultiplayerMatchView(Match match, OnChangeListener onPlay, OnChangeListener onRemove) {
        this.match = match;
        strings = ChessApplication.self().getStrings();
        settingsController = new GameSettingsController();
        init(onPlay, onRemove);
    }

    private void init(OnChangeListener onPlay, OnChangeListener onRemove) {
        setBackground(new NinePatchDrawable(
                new NinePatch(ChessApplication.self().getAssetManager().findRegion("lite_pane"),
                        10,10,10,10)));

        var table1 = new RdTable();
        table1.align(Align.topLeft);
        var table2 = new RdTable();
        table2.setBackground(new NinePatchDrawable(
                new NinePatch(ChessApplication.self().getAssetManager().findRegion("lite_pane"),
                        10,10,10,10)));

        Image iconChessGame = new Image(ChessAssetManager.current().findChessRegion("icon_chess_game"));
        iconChessGame.setScaling(Scaling.fit);
        Image iconPeoples = new Image(ChessAssetManager.current().findRegion("icon_peoples"));
        iconPeoples.setScaling(Scaling.fit);
        Image iconRank = new Image(ChessAssetManager.current().findChessRegion("cup"));
        iconRank.setScaling(Scaling.fit);
        Image iconSponsored = new Image(ChessAssetManager.current().findRegion("icon_sponsored"));
        iconSponsored.setScaling(Scaling.fit);

        int count = 0;
        if (match.getWhitePlayerId() != -1) count++;
        if (match.getBlackPlayerId() != -1) count++;

        table1.add(iconChessGame);
        table1.add(new RdLabel(match.getName() + " (" + strings.format("turns", match.getTurn()) + ")"))
                .expandX().fillX().row();

        table1.add(iconPeoples);
        table1.add(new RdLabel(strings.get("joined") + count))
                .expandX().fillX().left().row();


        if (match.getRankType() == RankType.UNRANKED) {
            table1.add(new RdLabel("[GOLD]" + strings.get("non_ranked")))
                .expandX().fillX().colspan(2).row();
        } else {
            table1.add(iconRank);
            table1.add(new RdLabel("[GOLD]" + defineRankType(match)))
                .expandX().fillX().row();
        }

        if (match.getSponsored() > 0) {
            table1.add(iconSponsored);
            table1.add(new RdLabel("[GOLD]"+ match.getSponsored()))
                .expandX().fillX().row();
        }

        // buttons

        RdImageTextButton remove = new RdImageTextButton("");
        remove.setImage("iw_cancel");
        if (onRemove != null) remove.addListener(onRemove);

        RdImageTextButton enter = new RdImageTextButton("", "blue");
        enter.setImage("iw_play");
        enter.addListener(onPlay);

        RdImageTextButton settings = new RdImageTextButton("");
        settings.setImage("iw_settings");
        settings.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                settingsController.showSettings(match);
            }
        });

        table2.add(settings).padRight(5);
        if (onRemove != null && ((ChessConstants.loggingAcc.getId() == match.getCreatorId() && !match.isStarted())
            || ChessConstants.loggingAcc.getType().ordinal() >= AccountType.MODERATOR.ordinal())) {
            table2.add(remove).padRight(5);
        }
        table2.add(enter);

        add(table1).expandX().fillX();
        add(table2).expandY().fillY();
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
}
