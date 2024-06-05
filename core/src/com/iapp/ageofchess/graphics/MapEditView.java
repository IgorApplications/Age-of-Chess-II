package com.iapp.ageofchess.graphics;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.iapp.ageofchess.ChessApplication;
import com.iapp.ageofchess.modding.MapData;
import com.iapp.lib.ui.actors.RdImageTextButton;
import com.iapp.lib.ui.actors.RdTable;
import com.iapp.lib.util.OnChangeListener;

public class MapEditView extends MapView {

    private RdImageTextButton clear;
    private RdImageTextButton edit;

    public MapEditView(MapData mapData, OnChangeListener onClear, OnChangeListener onEdit) {
        super(mapData);
        clear.addListener(onClear);
        edit.addListener(onEdit);
    }

    @Override
    public RdTable getButtonsTable() {
        RdTable table = new RdTable();
        table.setBackground(new NinePatchDrawable(
            new NinePatch(ChessApplication.self().getAssetManager().findRegion("dark_pane"),
                10,10,10,10)));

        clear = new RdImageTextButton("");
        clear.setImage("iw_cancel");
        edit = new RdImageTextButton("");;
        edit.setImage("iw_construction");

        // developer maps
        if (mapData.getType() != Files.FileType.Internal) {
            table.add(clear).padRight(5);
        }
        table.add(edit);

        return table;
    }
}
