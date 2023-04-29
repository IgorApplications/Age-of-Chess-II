package com.iapp.ageofchess.graphics;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.iapp.ageofchess.ChessApplication;
import com.iapp.ageofchess.modding.MatchState;
import com.iapp.ageofchess.util.ChessAssetManager;
import com.iapp.ageofchess.util.ChessConstants;
import com.iapp.ageofchess.util.SettingsUtil;
import com.iapp.rodsher.actors.RdImageTextButton;
import com.iapp.rodsher.actors.RdLabel;
import com.iapp.rodsher.util.OnChangeListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class SavedGameView extends Table {

    public SavedGameView(MatchState state, OnChangeListener onClear, OnChangeListener onPlay) {
        var strings = ChessApplication.self().getStrings();

        var formatter = new SimpleDateFormat("dd MMMM yyyy hh:mm",
                ChessConstants.localData.getLocale());
        formatter.setTimeZone(TimeZone.getDefault());
        setBackground(new NinePatchDrawable(
                new NinePatch(ChessApplication.self().getAssetManager().findRegion("lite_pane"),
                        10,10,10,10)));

        var gameName = new RdLabel("[ORANGE]" + state.getMatch().getName());
        var date = new RdLabel("[%75]" + formatter.format(new Date(state.getMatch().getCreatedTimeUTC())));
        var pair = SettingsUtil.defineResult(state.getResult());
        var result = new RdLabel("[%75]" + pair.getKey());
        result.setColor(pair.getValue());
        var turns = new RdLabel("[%75]" + strings.format("turns", state.getGame().getTurn()));
        var ranked = getRanked(state);

        var content = new Table();
        content.add(gameName).fillX().left().row();
        content.add(result).fillX().left().row();
        content.add(turns).fillX().left().row();
        content.add(date).fillX().left().row();
        content.add(ranked).fillX().left().row();

        var buttons = new Table();
        buttons.setBackground(new NinePatchDrawable(
                new NinePatch(ChessApplication.self().getAssetManager().findRegion("dark_pane"),
                        10,10,10,10)));

        var enter = new RdImageTextButton("", "blue");
        enter.setImage("iw_play");
        enter.addListener(onPlay);

        var clear = new RdImageTextButton("");
        clear.setImage("iw_close");
        clear.addListener(onClear);
        buttons.add(clear);
        buttons.add(enter).padLeft(10);

        align(Align.topLeft);
        add(new Image(ChessAssetManager.current().findRegion("iw_game")));
        add(content).padLeft(15);
        add(buttons).expandX().fillY().right();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
    }

    private Table getRanked(MatchState state) {
        var strings = ChessApplication.self().getStrings();
        var table = new Table();
        table.align(Align.left);

        var ranked = new RdLabel("");
        var rankedFlag = state.getMatch().isRanked();
        if (rankedFlag) {
            ranked.setText("[%75][GOLD]" + strings.get("ranked"));
            table.add(new Image(ChessAssetManager.current().findChessRegion("small_star")));
        } else {
            ranked.setText("[%75]" + strings.get("non_ranked"));
        }
        //ranked.setPadBottom(15);
        table.add(ranked).padLeft(5);

        return table;
    }
}
