package com.iapp.ageofchess.activity;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.iapp.ageofchess.ChessApplication;
import com.iapp.ageofchess.controllers.ModdingController;
import com.iapp.ageofchess.graphics.MapEditView;
import com.iapp.ageofchess.services.LocalFeatures;
import com.iapp.lib.ui.widgets.ChatView;
import com.iapp.ageofchess.modding.MapData;
import com.iapp.ageofchess.modding.TypeMap;
import com.iapp.ageofchess.services.ChessAssetManager;
import com.iapp.ageofchess.services.ChessConstants;
import com.iapp.ageofchess.services.DataManager;
import com.iapp.lib.ui.actors.*;
import com.iapp.lib.ui.screens.Activity;
import com.iapp.lib.ui.screens.RdApplication;
import com.iapp.lib.ui.widgets.TranslationDialog;
import com.iapp.lib.util.OnChangeListener;
import com.iapp.lib.util.TransitionEffects;
import com.iapp.lib.util.WindowUtil;

import java.util.List;

public class ModdingActivity extends Activity {

    private final ModdingController controller;
    private RdImageTextButton back;
    private RdTextButton createMap, translator;
    private WindowGroup windowGroup;
    private Spinner spinner;
    private PropertyTable properties;

    public ModdingActivity() {
        controller = new ModdingController(this);
    }

    public void setSpinner(Spinner spinner) {
        this.spinner = spinner;
    }


    @Override
    public void initActors() {
        if (ChessConstants.chatView != null) {
            ChessConstants.chatView.updateMode(ChatView.Mode.LOBBY);
        }
        ChessApplication.self().getLineContent().setVisible(true);
        Image background = new Image(new TextureRegionDrawable(
            ChessAssetManager.current().findChessRegion("menu_background")));
        background.setFillParent(true);
        background.setScaling(Scaling.fill);
        getStage().addActor(background);

        back = new RdImageTextButton(strings.get("[i18n]Back"), "red_screen");
        back.setImage("ib_back");
        back.padLeft(0).align(Align.left);
        back.getLabelCell().expandX().center();

        createMap = new RdTextButton(strings.get("[i18n]Create map"),"blue");
        translator = new RdTextButton(strings.get("[i18n]Translator"));
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
                        ChessConstants.FILE_TYPE,
                        TypeMap.TWO_D,
                        720, 720,
                        0,0,
                        0,0
                ), true);
            }
        });
        translator.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                TranslationDialog translation = new TranslationDialog("[i18n]Translator");
                translation.show(getStage());
                RdApplication.self().addDialog(translation, 900, 900, 10, 10);
            }
        });
    }

    @Override
    public void show(Stage stage, Activity last) {
        var content = new Table();
        content.setFillParent(true);
        getStage().addActor(content);

        if (ChessConstants.loggingAcc != null) {
            RdTable panel = new RdTable();
            panel.align(Align.topLeft);
            panel.setFillParent(true);
            getStage().addActor(panel);
            panel.add(ChessConstants.accountPanel)
                .expandX().fillX();
        }

        var window = new RdWindow("","screen_window");
        window.setMovable(false);
        properties = new PropertyTable(400);
        window.add(properties).expand().fill();

        properties.setVisibleBackground(false);
        addMaps(ChessAssetManager.current().getDataMaps());

        windowGroup = new WindowGroup(window, back);
        ChessApplication.self().updateTitle(windowGroup, strings.get("[i18n]Single Player"));

        windowGroup.setFillParent(true);
        stage.addActor(windowGroup);
        windowGroup.update();

        if (last instanceof EditMapActivity) {
            TransitionEffects.alphaShow(getStage().getRoot(), ChessConstants.localData.getScreenDuration());
        } else if (!(last instanceof ModdingActivity)){
            TransitionEffects.transitionBottomShow(windowGroup, ChessConstants.localData.getScreenDuration());
        }
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        windowGroup.update();
        WindowUtil.resizeCenter(spinner);
    }

    private void addMaps(List<MapData> maps) {
        properties.add(new PropertyTable.Title(strings.get("[i18n]Modding")));

        for (int i = 0; i < maps.size(); i++) {
            int finalI = i;
            properties.getContent().add(
                    new MapEditView(maps.get(i), new OnChangeListener() {
                @Override
                public void onChange(Actor actor) {
                    showDeleteMap(maps.get(finalI));
                }
            }, new OnChangeListener() {
                @Override
                public void onChange(Actor actor) {
                    controller.goToEdit(new MapData(maps.get(finalI)),
                        maps.get(finalI).getType() == Files.FileType.Internal);
                }
            }))
            .expandX().fillX().pad(10, 10, 10, 10).row();
        }

        RdTable bottomTable = new RdTable();
        bottomTable.add(createMap);
        if (ChessApplication.self().getLocalFeatures() == LocalFeatures.DEVELOPER) {
            bottomTable.add(translator).padLeft(5);
        }

        properties.getContent().add(bottomTable)
            .expandX().left().padLeft(5).padBottom(5);
    }

    private void showDeleteMap(MapData mapData) {
        RdDialog deleteMap = new RdDialogBuilder()
                .title(strings.get("[i18n]confirmation"))
                .cancel(strings.get("[i18n]reject"))
                .accept(strings.get("[i18n]accept"), (dialog, s) -> {
                    Runnable task = () -> DataManager.self().removeMapData(mapData);
                    RdApplication.self().execute(task);

                    // equals by link
                    ChessAssetManager.current().getDataMaps().remove(mapData);
                    properties.getContent().clear();
                    addMaps(ChessAssetManager.current().getDataMaps());
                    dialog.hide();
                })
                .text(strings.get("[i18n]Are you sure you want to delete this map? If it is not on our servers, then the action will be irrevocable."))
                .build("input");

        deleteMap.show(getStage());
        deleteMap.setSize(800, 550);
        deleteMap.getIcon().setDrawable(new TextureRegionDrawable(
                ChessAssetManager.current().findRegion("icon_warn")));
        deleteMap.getIcon().setScaling(Scaling.fit);
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public Actor hide(SequenceAction action, Activity next) {
        if (next instanceof EditMapActivity) {
            TransitionEffects.alphaHide(action, ChessConstants.localData.getScreenDuration());
            return getStage().getRoot();
        } else {
            TransitionEffects.transitionBottomHide(action, windowGroup, ChessConstants.localData.getScreenDuration());
            return windowGroup;
        }
    }
}
