package com.iapp.ageofchess.graphics;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.iapp.ageofchess.ChessApplication;
import com.iapp.ageofchess.modding.MapData;
import com.iapp.lib.ui.actors.RdImageTextButton;
import com.iapp.lib.ui.actors.RdTable;
import com.iapp.lib.util.OnChangeListener;

public class MapScenariosView extends MapView {

    private RdImageTextButton create;

    public MapScenariosView(MapData mapData, OnChangeListener onSelection) {
        super(mapData);
        create.addListener(onSelection);
    }

    @Override
    public RdTable getButtonsTable() {
        RdTable buttonsTable = new RdTable();
        buttonsTable.setBackground(new NinePatchDrawable(
            new NinePatch(ChessApplication.self().getAssetManager().findRegion("dark_pane"),
                10,10,10,10)));

        create = new RdImageTextButton("","blue");
        create.setImage("iw_add");
        buttonsTable.add(create);

        return buttonsTable;
    }
}
