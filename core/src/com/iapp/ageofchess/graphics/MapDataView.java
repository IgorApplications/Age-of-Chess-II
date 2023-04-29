package com.iapp.ageofchess.graphics;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.iapp.ageofchess.ChessApplication;
import com.iapp.ageofchess.modding.MapData;
import com.iapp.rodsher.actors.RdImageTextButton;
import com.iapp.rodsher.actors.RdLabel;
import com.iapp.rodsher.actors.RdTable;
import com.iapp.rodsher.util.OnChangeListener;

public class MapDataView extends Table {

    public MapDataView(MapData mapData, OnChangeListener onSelection) {
        setBackground(new NinePatchDrawable(
                new NinePatch(ChessApplication.self().getAssetManager().findRegion("lite_pane"),
                               10,10,10,10)));

        add(getFirstTable(mapData)).align(Align.topLeft);
        add(getSecondTable(mapData)).expand().fill().align(Align.topLeft);
        add(getCreate(onSelection)).fillY().center();
    }

    private RdTable getFirstTable(MapData mapData) {
        var strings = ChessApplication.self().getStrings();
        var table = new RdTable();

        var author = new RdLabel(
                "[75%]" + strings.get("author") + "[_]" + mapData.getStrings().get("author"));
        //author.setPadBottom(20);
        var created = new RdLabel(
                "[75%]" + strings.get("created") + mapData.getStrings().get("created"));
        //created.setPadBottom(20);
        var updated = new RdLabel(
                "[75%]" + strings.get("updated") + mapData.getStrings().get("updated"));
        //updated.setPadBottom(20);
        var typeMap = new RdLabel(
                "[75%]" + strings.get("type_map") + mapData.getTypeMap());
        //typeMap.setPadBottom(20);

        table.align(Align.topLeft);
        table.add(new Image(mapData.getMapIcon()))
                .size(256, 256).left().padBottom(5).row();
        table.add(author).expandX().fillX().row();
        table.add(created).expandX().fillX().row();
        table.add(updated).expandX().fillX().row();
        table.add(typeMap).expandX().fillX();

        return table;
    }

    private Table getSecondTable(MapData mapData) {
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

    private Table getCreate(OnChangeListener onSelection) {
        var create = new RdImageTextButton("","blue");
        create.setImage("iw_add");
        create.addListener(onSelection);

        var table = new Table();
        table.setBackground(new NinePatchDrawable(
                new NinePatch(ChessApplication.self().getAssetManager().findRegion("dark_pane"),
                        10,10,10,10)));
        table.add(create);

        return table;
    }
}
