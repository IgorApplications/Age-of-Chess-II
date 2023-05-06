package com.iapp.rodsher.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.iapp.rodsher.actors.RdLabel;
import com.iapp.rodsher.util.CallListener;

class LoggingActivity extends Activity {

    private final String logText;
    private final String description;

    private RdLabel logLabel, descriptionLabel;
    private RdLabel.RdLabelStyle logTextStyle, descTextStyle;

    public LoggingActivity(String logText, String description,
                           RdLabel.RdLabelStyle logTextStyle, RdLabel.RdLabelStyle descTextStyle,
                           CallListener onFatal) {
        this.logTextStyle = logTextStyle;
        this.descTextStyle = descTextStyle;
        this.logText = logText.replace("[", "|").replace("]", "|");
        this.description = description.replace("[", "|").replace("]", "|");
        Gdx.app.postRunnable(onFatal::call);
    }

    @Override
    public void initActors() {
        RdApplication.self().setBackgroundColor(Color.BLUE);

        logLabel = new RdLabel(logText, logTextStyle);
        logLabel.setWrap(true);
        descriptionLabel = new RdLabel(description, descTextStyle);
        descriptionLabel.setWrap(true);
    }

    @Override
    public void initListeners() {}

    @Override
    public void show(Stage stage, Activity last) {
        var content = new Table();
        content.setFillParent(true);
        content.align(Align.topLeft);
        stage.addActor(content);

        content.add(descriptionLabel).expandX().fillX()
                .pad(30, 10, 10, 10).align(Align.topLeft).row();
        content.add(logLabel).expandX().fillX()
                .pad(30, 10, 10, 10).align(Align.topLeft);
    }
}
