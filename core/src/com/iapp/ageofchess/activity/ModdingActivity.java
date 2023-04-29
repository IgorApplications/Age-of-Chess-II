package com.iapp.ageofchess.activity;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.iapp.ageofchess.ChessApplication;
import com.iapp.ageofchess.controllers.ModdingController;
import com.iapp.ageofchess.graphics.EditMapDataView;
import com.iapp.ageofchess.modding.MapData;
import com.iapp.ageofchess.modding.TypeMap;
import com.iapp.ageofchess.util.ChessAssetManager;
import com.iapp.ageofchess.util.DataManager;
import com.iapp.rodsher.actors.*;
import com.iapp.rodsher.screens.Activity;
import com.iapp.rodsher.screens.RdApplication;
import com.iapp.rodsher.util.OnChangeListener;
import com.iapp.rodsher.util.WindowUtil;

import java.util.List;

public class ModdingActivity extends Activity {

    private final ModdingController controller;
    private RdImageTextButton back;
    private RdTextButton createMap;
    private WindowGroup windowGroup;
    private Spinner spinner;
    private PropertyTable properties;
    private RdDialog deleteMap;

    public ModdingActivity() {
        controller = new ModdingController(this);
    }

    public void setSpinner(Spinner spinner) {
        this.spinner = spinner;
    }


    @Override
    public void initActors() {
        RdApplication.self().setBackground(new TextureRegionDrawable(
                ChessAssetManager.current().findChessRegion("menu_background")));

        back = new RdImageTextButton(strings.get("back"), "red_screen");
        back.setImage("ib_back");
        back.padLeft(0).align(Align.left);
        back.getLabelCell().expandX().center();

        createMap = new RdTextButton(strings.get("create_map"),"blue");
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
        createMap.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                controller.goToEdit(new MapData(
                        MapData.generateModdingId(ChessAssetManager.current().getDataMaps()),
                        Files.FileType.External,
                        TypeMap.TWO_D,
                        720, 720,
                        0,0,
                        0,0
                ), true);
            }
        });
    }

    @Override
    public void show(Stage stage) {
        var content = new Table();
        content.setFillParent(true);
        getStage().addActor(content);

        var window = new RdWindow("","screen_window");
        window.setMovable(false);
        properties = new PropertyTable(400, ChessAssetManager.current().getSkin());
        window.add(properties).expand().fill();

        properties.setVisibleBackground(false);
        addMaps(ChessAssetManager.current().getDataMaps());

        windowGroup = new WindowGroup(window, back);
        ChessApplication.self().updateTitle(windowGroup, strings.get("single-player"));

        windowGroup.setFillParent(true);
        stage.addActor(windowGroup);
        windowGroup.update();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        windowGroup.update();
        WindowUtil.resizeCenter(spinner);
        WindowUtil.resizeCenter(deleteMap);
    }

    private void addMaps(List<MapData> maps) {
        properties.add(new PropertyTable.Title(strings.get("modding")));

        for (int i = 0; i < maps.size(); i++) {
            int finalI = i;
            properties.getContent().add(
                    new EditMapDataView(maps.get(i), new OnChangeListener() {
                @Override
                public void onChange(Actor actor) {
                    showDeleteMap(maps.get(finalI));
                }
            }, new OnChangeListener() {
                @Override
                public void onChange(Actor actor) {
                    if (maps.get(finalI).getType() == Files.FileType.Internal) {
                        controller.goToEdit(new MapData(maps.get(finalI)), true);
                    } else  {
                        controller.goToEdit(new MapData(maps.get(finalI)), false);
                    }
                }
            }))
            .expandX().fillX().pad(10, 10, 10, 10).row();
        }
        properties.getContent().add(createMap)
                .expandX().left().padLeft(5).padBottom(5);
    }

    private void showDeleteMap(MapData mapData) {
        deleteMap = new RdDialogBuilder()
                .title(strings.get("confirmation"))
                .cancel(strings.get("cancel"))
                .accept(strings.get("accept"), new OnChangeListener() {
                    @Override
                    public void onChange(Actor actor) {
                        Runnable task = () -> DataManager.self().removeMapData(mapData);
                        RdApplication.self().execute(task);

                        // equals by link
                        ChessAssetManager.current().getDataMaps().remove(mapData);
                        properties.getContent().clear();
                        addMaps(ChessAssetManager.current().getDataMaps());
                        deleteMap.hide();
                    }
                })
                .text(strings.get("conf_del_map"))
                .build(ChessAssetManager.current().getSkin(), "input");

        deleteMap.show(getStage());
        deleteMap.setSize(800, 550);
        deleteMap.getIcon().setDrawable(new TextureRegionDrawable(
                ChessAssetManager.current().findRegion("icon_warn")));
        deleteMap.getIcon().setScaling(Scaling.fit);
    resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }
}
