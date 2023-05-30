package com.iapp.ageofchess.activity;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.iapp.ageofchess.ChessApplication;
import com.iapp.ageofchess.controllers.GuideController;
import com.iapp.ageofchess.services.ChessAssetManager;
import com.iapp.ageofchess.services.ChessConstants;
import com.iapp.lib.ui.actors.*;
import com.iapp.lib.ui.screens.Activity;
import com.iapp.lib.ui.screens.RdApplication;
import com.iapp.lib.util.OnChangeListener;
import com.iapp.lib.util.TransitionEffects;

public class GuideActivity extends Activity {

    private final GuideController controller;
    private RdImageTextButton back;
    private WindowGroup windowGroup;
    private RdWindow window;

    public GuideActivity() {
        controller = new GuideController(this);
    }

    @Override
    public void initActors() {
        back = new RdImageTextButton(strings.get("back"), "red_screen");
        back.setImage("ib_back");
    }

    @Override
    public void initListeners() {
        ChessApplication.self().getLauncher().setOnFinish(controller::goToMenu);
        back.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                controller.goToMenu();
            }
        });
    }

    @Override
    public void show(Stage stage, Activity last) {
        Image background = new Image(new TextureRegionDrawable(
            ChessAssetManager.current().findChessRegion("menu_background")));
        background.setFillParent(true);
        background.setScaling(Scaling.fill);
        getStage().addActor(background);

        var content = new RdTable();
        content.setFillParent(true);
        getStage().addActor(content);

        window = new RdWindow("", "screen_window");
        window.setMovable(false);
        var properties = new PropertyTable(400, ChessAssetManager.current().getSkin());
        window.add(properties).expand().fill();

        properties.setVisibleBackground(false);
        properties.add(new PropertyTable.Title(strings.get("guide")));

        var descContent = new RdTable();
        descContent.padBottom(30);
        descContent.align(Align.topLeft);

        var scroll = new ScrollPane(descContent);
        properties.getContent().add(scroll).expand().fill().pad(15, 10, 15, 10);

        windowGroup = new WindowGroup(window, back);
        ChessApplication.self().updateTitle(windowGroup, strings.get("single-player"));

        windowGroup.setFillParent(true);
        stage.addActor(windowGroup);
        windowGroup.update();

        TransitionEffects.transitionBottomShow(windowGroup, ChessConstants.localData.getScreenDuration());

        addText(descContent, strings.get("rules"));
        addText(descContent, strings.get("guide_desc_1"));
        addText(descContent, strings.get("guide_desc_2"));
        addText(descContent, strings.get("guide_desc_3"));
        addText(descContent, strings.get("guide_desc_4"));
        addText(descContent, strings.get("guide_desc_5"));
        addText(descContent, strings.get("guide_desc_6"));
        addText(descContent, strings.get("guide_desc_7"));
        addText(descContent, strings.get("guide_desc_8"));
        addText(descContent, strings.get("guide_desc_9"));
        addText(descContent, strings.get("guide_desc_10"));
        addText(descContent, strings.get("guide_desc_11"));
        addText(descContent, strings.get("guide_desc_12"));

        addText(descContent, strings.get("credits"));
        addText(descContent, strings.get("guide_desc_13"));

        addText(descContent, strings.get("technical_support"));
        addText(descContent, strings.get("guide_desc_14"));
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        windowGroup.update();
    }


    private void addText(RdTable table, String data) {
        var label = new RdLabel(data);
        label.setWrap(true);
        RdApplication.postRunnable(() ->
            table.add(label).expandX().fillX().row());
    }

    @Override
    public Actor hide(SequenceAction action, Activity next) {
        TransitionEffects.transitionBottomHide(action, windowGroup, ChessConstants.localData.getScreenDuration());
        return windowGroup;
    }
}
