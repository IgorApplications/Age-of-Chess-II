package com.iapp.ageofchess.graphics;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.iapp.ageofchess.ChessApplication;
import com.iapp.ageofchess.chess_engine.Result;
import com.iapp.ageofchess.controllers.multiplayer.MultiplayerGameController;
import com.iapp.ageofchess.multiplayer.AccountType;
import com.iapp.ageofchess.multiplayer.MultiplayerEngine;
import com.iapp.ageofchess.util.ChessConstants;
import com.iapp.ageofchess.util.SettingsUtil;
import com.iapp.rodsher.actors.RdImageTextButton;
import com.iapp.rodsher.actors.RdSelectBox;
import com.iapp.rodsher.actors.RdTable;
import com.iapp.rodsher.screens.RdApplication;
import com.iapp.rodsher.util.OnChangeListener;
import com.iapp.rodsher.util.RdI18NBundle;

import java.util.ArrayList;

public class ControlGameView extends Table {

    private final RdI18NBundle strings;
    private final MultiplayerGameController controller;

    private RdImageTextButton join, start;
    private RdSelectBox<String> availableColors;
    private RdTable controlContent;

    public ControlGameView(MultiplayerGameController controller) {
        if (controller.getCurrentMatch().isStarted()) setVisible(false);
        strings = RdApplication.self().getStrings();
        this.controller = controller;
        initControlTable();
    }

    private void initControlTable() {
        controlContent = new RdTable();
        controlContent.align(Align.center);
        controlContent.setBackground(new NinePatchDrawable(
            new NinePatch(ChessApplication.self().getAssetManager().findChessRegion("control_bg"),
                15,15,15,15)));
        controlContent.pad(5, 5, 5, 5);
        add(controlContent).padBottom(20);

        join = new RdImageTextButton("[%125]" + (controller.isInside() ? strings.get("disjoin") : strings.get("join")));
        start = new RdImageTextButton("[%125]" + strings.get("start"), "blue");
        availableColors = new RdSelectBox<>();
        updateControlContent();

        join.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {

                if (!controller.isInside()) {
                    var color = SettingsUtil.defineColor(availableColors.getSelected());
                    MultiplayerEngine.self().join(controller.getMatchId(), color);
                    if (!controller.getCurrentMatch().isRandom()) {
                        controller.update(SettingsUtil.reverse(color));
                    }

                } else {
                    MultiplayerEngine.self().disjoin(controller.getMatchId());
                }

            }
        });

        start.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                int count = 0;
                if (controller.getCurrentMatch().getBlackPlayerId() != -1) count++;
                if (controller.getCurrentMatch().getWhitePlayerId() != -1) count++;

                if (count < 2) {
                    ChessApplication.self().showInfo(strings.get("not_enough_players"));
                    return;
                }
                MultiplayerEngine.self().start(controller.getMatchId());
            }
        });
    }

    public void updateControlContent() {
        controlContent.clear();

        var data = new ArrayList<String>();
        if (controller.getCurrentMatch().getWhitePlayerId() == -1) data.add("[%125]" + strings.get("white"));
        if (controller.getCurrentMatch().getBlackPlayerId() == -1) data.add("[%125]" + strings.get("black"));
        availableColors.setItems(data.toArray(new String[0]));
        join.setText("[%125]" + (controller.isInside() ? strings.get("disjoin") : strings.get("join")));

        controlContent.add(join).padRight(30).padLeft(30).minWidth(300);
        if (controller.isCreator() || ChessConstants.loggingAcc.getType().ordinal() >= AccountType.MODERATOR.ordinal()) {
            controlContent.add(start).minWidth(300).padRight(30);
        }

        if (!controller.isInside() && !controller.getCurrentMatch().isRandom()) {
            controlContent.add(availableColors).padRight(30);
        }
    }
}
