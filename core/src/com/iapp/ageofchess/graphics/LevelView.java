package com.iapp.ageofchess.graphics;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.iapp.ageofchess.ChessApplication;
import com.iapp.ageofchess.util.ChessAssetManager;
import com.iapp.rodsher.actors.RdLabel;
import com.iapp.rodsher.actors.RdTable;

public class LevelView extends ImageButton {

    public LevelView(String name, boolean star1, boolean star2, boolean star3, int bestResult) {
        super(ChessAssetManager.current().getLevelStyle());
        var strings = ChessApplication.self().getStrings();

        var content = new Table();
        var stars = new Table();
        stars.align(Align.center);
        addStar(stars, star1);
        addStar(stars, star2);
        addStar(stars, star3);

        var labelName = new RdLabel("[%125]" + name);
        labelName.setAlignment(Align.center);
        var labelTurns = new RdLabel(bestResult != Integer.MAX_VALUE ?
                strings.format("turns", bestResult) : "???");
        labelTurns.setAlignment(Align.center);

        content.add(labelName).expandX().fillX().center().row();
        content.add(stars).center().row();
        content.add(labelTurns).expandX().fillX().center();

        add(content).expand().fillX().align(Align.center)
                .pad(10, 10, 10, 10);
    }

    public LevelView() {
        super(ChessAssetManager.current().getClosedLevelStyle());
        var strings = ChessApplication.self().getStrings();
        var content = new RdTable();
        var text = new RdLabel(strings.get("closed"));
        var lock = new Image(ChessAssetManager.current().findChessRegion("lock_up_icon"));

        content.add(text);
        content.add(lock).padLeft(30);

        add(content).expand().fillX().align(Align.center)
                .pad(10, 10, 10, 10);
        addListener(new InputListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                lock.setDrawable(new TextureRegionDrawable(
                        ChessAssetManager.current().findChessRegion("lock_down_icon")));
                super.touchDown(event, x, y, pointer, button);
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                lock.setDrawable(new TextureRegionDrawable(
                        ChessAssetManager.current().findChessRegion("lock_up_icon")));
                super.touchUp(event, x, y, pointer, button);
            }
        });
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
    }

    private void addStar(Table stars, boolean enable) {
        Image star;
        if (enable) star = new Image(ChessAssetManager.current().findChessRegion("star"));
        else star = new Image(ChessAssetManager.current().findChessRegion("empty_star"));
        star.setScaling(Scaling.fill);

        stars.add(star).padLeft(3);
    }
}
