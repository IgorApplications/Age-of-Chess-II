package com.iapp.lib.ui.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.iapp.lib.ui.actors.RdLabel;
import com.iapp.lib.ui.actors.RdTable;
import com.iapp.lib.util.CallListener;

class LoggingActivity extends Activity {

    private final String logText;
    private final String description;

    private RdLabel logLabel, descriptionLabel;
    private final RdLabel.RdLabelStyle logTextStyle;
    private final RdLabel.RdLabelStyle descTextStyle;

    public LoggingActivity(String logText, String description,
                           RdLabel.RdLabelStyle logTextStyle, RdLabel.RdLabelStyle descTextStyle,
                           CallListener onFatal) {
        this.logTextStyle = logTextStyle;
        this.descTextStyle = descTextStyle;
        this.logText = logText;
        this.description = description;
        Gdx.app.postRunnable(onFatal::call);
    }

    @Override
    public void initActors() {
        RdApplication.self().setBackgroundColor(Color.BLUE);

        logLabel = new RdLabel(logText, logTextStyle, false);
        logLabel.setWrap(true);
        descriptionLabel = new RdLabel(description, descTextStyle, false);
        descriptionLabel.setWrap(true);
    }

    @Override
    public void initListeners() {}

    @Override
    public void show(Stage stage, Activity last) {
        RdTable content = new RdTable();
        content.setFillParent(true);
        content.align(Align.topLeft);
        stage.addActor(content);

        content.add(descriptionLabel).expandX().fillX()
                .pad(30, 10, 10, 10).align(Align.topLeft).row();
        content.add(logLabel).expandX().fillX()
                .pad(30, 10, 10, 10).align(Align.topLeft);
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        RdApplication.self().getStage().getRoot().getColor().a = 1;
        RdApplication.self().getStage().getRoot().clearActions();
    }
}
