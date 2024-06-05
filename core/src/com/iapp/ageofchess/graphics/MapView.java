package com.iapp.ageofchess.graphics;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.iapp.ageofchess.ChessApplication;
import com.iapp.ageofchess.modding.MapData;
import com.iapp.lib.ui.actors.RdLabel;
import com.iapp.lib.ui.actors.RdTable;
import com.iapp.lib.util.RdI18NBundle;

public abstract class MapView extends RdTable {

    final MapData mapData;

    public MapView(MapData mapData) {
        this.mapData = mapData;
        setBackground(new NinePatchDrawable(
            new NinePatch(ChessApplication.self().getAssetManager().findRegion("lite_pane"),
                10,10,10,10)));

        add(getFirstTable()).align(Align.topLeft);
        add(getSecondTable()).padLeft(3).expand().fill()
            .align(Align.topLeft).padRight(3);
        add(getButtonsTable()).fillY().center();
    }

    private RdTable getFirstTable() {
        RdI18NBundle strings = ChessApplication.self().getStrings();
        RdTable table = new RdTable();

        RdLabel author = new RdLabel(
            "[75%]" + strings.get("[i18n]author:") + " [_]" + mapData.getStrings().get("author"));
        RdLabel created = new RdLabel(
            "[75%]" + strings.get("[i18n]created:") + " " + mapData.getStrings().get("created"));
        RdLabel updated = new RdLabel(
            "[75%]" + strings.get("[i18n]updated:") + " " + mapData.getStrings().get("updated"));
        RdLabel typeMap = new RdLabel(
            "[75%]" + strings.get("[i18n]map type:") + " " + mapData.getTypeMap());

        table.align(Align.topLeft);
        table.add(new Image(mapData.getMapIcon()))
            .size(256, 256).left().padBottom(5).row();
        table.add(author).expandX().fillX().row();
        table.add(created).expandX().fillX().row();
        table.add(updated).expandX().fillX().row();
        table.add(typeMap).expandX().fillX();

        return table;
    }

    private Table getSecondTable() {
        var table = new Table();

        var name = new RdLabel("[GOLD]" + mapData.getStrings().get("name"));
        name.setWrap(true);
        name.setAlignment(Align.topLeft);
        var description = new RdLabel(mapData.getStrings().get("description"));
        description.setWrap(true);

        table.align(Align.topLeft);
        table.add(name).expandX().fillX().row();
        table.add(description).expandX().fillX();

        return table;
    }

    public abstract RdTable getButtonsTable();
}
