package com.iapp.ageofchess.graphics;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.github.tommyettinger.textra.TextraLabel;
import com.iapp.ageofchess.ChessApplication;
import com.iapp.ageofchess.modding.MapData;
import com.iapp.ageofchess.util.ChessAssetManager;
import com.iapp.rodsher.actors.RdImageTextButton;
import com.iapp.rodsher.actors.RdLabel;
import com.iapp.rodsher.util.OnChangeListener;

public class ScenarioView extends Table {

    public ScenarioView(MapData mapData, int scenarioI, OnChangeListener onCreate) {
        setBackground(new NinePatchDrawable(
                new NinePatch(ChessApplication.self().getAssetManager().findRegion("dark_pane"),
                        10,10,10,10)));

        var create = new RdImageTextButton("");
        create.setImage("iw_add");
        create.addListener(onCreate);
        var createTable = new Table();
        createTable.add(create);
        createTable.setBackground(new NinePatchDrawable(
                new NinePatch(ChessApplication.self().getAssetManager().findRegion("lite_pane"),
                        10,10,10,10)));

        var table = new Table();
        table.add(new Image(mapData.getScenarioIcons()[scenarioI]))
                .size(200, 200);
        table.add(getTextTable(mapData, scenarioI)).padLeft(5).expand().fill().top();

        add(table).expand().fill().left();
        add(createTable).right();
    }

    private Table getTextTable(MapData mapData, int scenarioI) {
        var table = new Table();

        var titleTable = new Table();
        titleTable.align(Align.topLeft);
        var title = new RdLabel("[*][%100]" + mapData.getStrings().get("title_scenario_" + (scenarioI + 1)));
        title.setAlignment(Align.topLeft);

        titleTable.add(title);
        if (mapData.isRatingScenario(scenarioI)) {
            var star = new Image(ChessAssetManager.current().findChessRegion("cup"));
            star.setScaling(Scaling.fit);
            titleTable.add(star);
        }

        var description = new RdLabel("[%75]" + mapData.getStrings().get("desc_scenario_" + (scenarioI + 1)));
        description.setWrap(true);
        description.setAlignment(Align.topLeft);

        table.add(titleTable).expandX().fillX().align(Align.topLeft).row();
        table.add(description).expand().fill().align(Align.topLeft);

        return table;
    }
}
