package com.iapp.ageofchess.graphics;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.iapp.ageofchess.ChessApplication;
import com.iapp.ageofchess.multiplayer.AccountType;
import com.iapp.ageofchess.multiplayer.Match;
import com.iapp.ageofchess.multiplayer.RankType;
import com.iapp.ageofchess.util.ChessConstants;
import com.iapp.rodsher.actors.RdImageTextButton;
import com.iapp.rodsher.actors.RdLabel;
import com.iapp.rodsher.actors.RdTable;
import com.iapp.rodsher.util.OnChangeListener;
import com.iapp.rodsher.util.RdI18NBundle;

public class MultiplayerMatchView extends Table {

    private final Match match;
    private final RdI18NBundle strings;

    public MultiplayerMatchView(Match match, OnChangeListener onPlay, OnChangeListener onRemove) {
        this.match = match;
        strings = ChessApplication.self().getStrings();
        init(onPlay, onRemove);
    }

    private void init(OnChangeListener onPlay, OnChangeListener onRemove) {
        setBackground(new NinePatchDrawable(
                new NinePatch(ChessApplication.self().getAssetManager().findRegion("lite_pane"),
                        10,10,10,10)));

        var table1 = new RdTable();
        var table2 = new RdTable();
        table2.setBackground(new NinePatchDrawable(
                new NinePatch(ChessApplication.self().getAssetManager().findRegion("lite_pane"),
                        10,10,10,10)));

        int count = 0;
        if (match.getWhitePlayerId() != -1) count++;
        if (match.getBlackPlayerId() != -1) count++;

        table1.add(new RdLabel(match.getName()))
                .expandX().fillX().row();
        table1.add(new RdLabel(strings.get("turn") + match.getTurn()))
                .expandX().fillX().row();
        table1.add(new RdLabel("[GREEN]" + strings.get("online") + match.getEntered().size()))
                .expandX().fillX().row();
        table1.add(new RdLabel(strings.get("joined") + count))
                .expandX().fillX().row();

        if (match.getRankType() == RankType.UNRANKED) {
            table1.add(new RdLabel(strings.get("non_ranked")))
                    .expandX().fillX().row();
        } else {
            table1.add(new RdLabel("[GOLD]" + defineRankType(match)))
                    .expandX().fillX().row();
        }

        var enter = new RdImageTextButton("", "blue");
        enter.setImage("iw_play");
        enter.addListener(onPlay);

        var remove = new RdImageTextButton("");
        remove.setImage("iw_cancel");
        if (onRemove != null) remove.addListener(onRemove);

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
