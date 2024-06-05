package com.iapp.ageofchess.activity;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.iapp.ageofchess.ChessApplication;
import com.iapp.ageofchess.controllers.EditMapController;
import com.iapp.lib.ui.widgets.ChatView;
import com.iapp.ageofchess.graphics.ModdingView;
import com.iapp.ageofchess.modding.MapData;
import com.iapp.ageofchess.modding.MapResources;
import com.iapp.ageofchess.services.*;
import com.iapp.lib.ui.actors.*;
import com.iapp.lib.ui.screens.Activity;
import com.iapp.lib.ui.screens.RdApplication;
import com.iapp.lib.util.OnChangeListener;
import com.iapp.lib.util.StringsGenerator;
import com.iapp.lib.util.TransitionEffects;
import com.iapp.lib.util.WindowUtil;

public class EditMapActivity extends Activity {

    private final EditMapController controller;
    private final MapData mapData;
    private boolean vertically;
    private float coefX, coefY;

    private Table content, boardTable;
    private RdImageTextButton saveExit, change, update, help;
    private ModdingView moddingView;
    private Cell<ModdingView> boardCell;

    private RdDialog confExit, changeDialog, conf, err, selector, helpDialog;
    private RdDialog replaceDialog;
    private Spinner spinner;
    private Image blackout;

    public EditMapActivity(MapData mapData, MapResources resources, boolean newMap) {
        controller = new EditMapController(this, mapData, resources, newMap);
        this.mapData = mapData;
    }

    public void setSpinner(Spinner spinner) {
        this.spinner = spinner;
    }

    public Spinner getSpinner() {
        return spinner;
    }

    public void setConfirmation(RdDialog dialog) {
        conf = dialog;
    }

    public void setConfExit(RdDialog confExit) {
        this.confExit = confExit;
    }

    public RdDialog getConfExit() {
        return confExit;
    }

    public void setChangeDialog(RdDialog changeDialog) {
        this.changeDialog = changeDialog;
    }

    public void setReplaceDialog(RdDialog replaceDialog) {
        this.replaceDialog = replaceDialog;
    }

    public RdDialog getReplaceDialog() {
        return replaceDialog;
    }

    public RdDialog getChangeDialog() {
        return changeDialog;
    }

    public RdDialog getConfirmation() {
        return conf;
    }

    public void setError(RdDialog dialog) {
        err = dialog;
    }

    public RdDialog getError() {
        return err;
    }

    public void setSelector(RdDialog dialog) {
        selector = dialog;
    }

    public RdDialog getSelector() {
        return selector;
    }

    public ModdingView getModdingView() {
        return moddingView;
    }

    public Image getBlackout() {
        return blackout;
    }

    @Override
    public void initActors() {
        if (ChessConstants.chatView != null) {
            ChessConstants.chatView.updateMode(ChatView.Mode.LOBBY_GAMES);
        }
        ChessApplication.self().getLineContent().setVisible(false);
        Image background = new Image(new TextureRegionDrawable(
            controller.getRegion("background")));
        background.setFillParent(true);
        getStage().addActor(background);
        background.setScaling(Scaling.fill);

        content = new Table();
        content.setFillParent(true);
        content.align(Align.center);
        boardTable = new Table();
        boardTable.setFillParent(true);

        saveExit = new RdImageTextButton(strings.get("[i18n]Exit"));
        saveExit.setImage("iw_back");
        change = new RdImageTextButton(strings.get("[i18n]Change"));
        change.setImage("iw_construction");
        update = new RdImageTextButton(strings.get("[i18n]update"));
        update.setImage("iw_sync");
        help = new RdImageTextButton(strings.get("[i18n]Help"));
        help.setImage("iw_help");
        moddingView = new ModdingView(controller, mapData);

        blackout = new Image(ChessAssetManager.current().getDarkTexture());
        blackout.setVisible(false);
        blackout.setFillParent(true);

        getStage().addActor(blackout);
    }

    @Override
    public void initListeners() {
        ChessApplication.self().getLauncher().setOnFinish(controller::showConfExit);
        saveExit.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                controller.showConfExit();
            }
        });
        change.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                controller.getEditMap(getStage());
            }
        });
        update.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                moddingView.updateSize();
            }
        });
        help.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                blackout.setVisible(true);

                helpDialog = new RdDialog(strings.get("[i18n]Modding Guide"),
                        ChessAssetManager.current().getSkin(), "input");
                helpDialog.getIcon().setDrawable(new TextureRegionDrawable(
                        ChessAssetManager.current().findRegion("icon_info")));
                helpDialog.getIcon().setScaling(Scaling.fit);
                helpDialog.setOnCancel(new OnChangeListener() {
                    @Override
                    public void onChange(Actor actor) {
                        helpDialog.hide();
                        blackout.setVisible(false);
                    }
                });

                var label1 = new RdLabel(strings.get("[i18n]\tTextures and atlases must contain elements with names such as: white_pawn, black_pawn, white_rook, black_rook, white_knight, black_knight, white_bishop, black_bishop, white_queen, black_queen, white_king, black_king, background, board. Such elements can replace the standard images that the application provides. The rest of the names will be ignored. Textures must have an extension (.jpg or .png), atlases must be correct and have an extension (.atlas). Atlases are intended for optimization and are optional, you can use only individual images - textures."),
                        ChessAssetManager.current().getSkin());
                label1.setWrap(true);
                var label2 = new RdLabel(strings.get("[i18n]\tIcons can have any name, but the extension must be optional (.jpg or .png). You can also change the location of the figures using the FEN format and other information about the map."),
                        ChessAssetManager.current().getSkin());
                label2.setWrap(true);
                var accept = new RdTextButton(strings.get("[i18n]accept"),"blue");
                accept.addListener(new OnChangeListener() {
                    @Override
                    public void onChange(Actor actor) {
                        helpDialog.hide();
                        blackout.setVisible(false);
                    }
                });

                var content = new RdTable();
                content.align(Align.topLeft);
                var scroll = new RdScrollPane(content);
                scroll.setFadeScrollBars(false);
                scroll.setOverscroll(false, false);
                content.add(label1).expandX().fillX().row();
                content.add(label2).expandX().fillX();

                helpDialog.getContentTable().align(Align.topLeft);
                helpDialog.getContentTable().add(scroll).expand().fillX();
                helpDialog.getButtonTable().add(accept).expandX().fillX();

                helpDialog.show(getStage());
                helpDialog.setSize(900, 800);
                resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            }
        });
    }

    @Override
    public void show(Stage stage, Activity last) {
        stage.addActor(boardTable);
        stage.addActor(content);

        if (Gdx.app.getType() == Application.ApplicationType.Android) {
            var launcher = ChessApplication.self().getLauncher();
            if (!launcher.checkStoragePermissions()) {
                launcher.verifyStoragePermissions(result -> {
                    if (!result) controller.goToModding();
                });
            }
        }

        TransitionEffects.alphaShow(stage.getRoot(), ChessConstants.localData.getScreenDuration());
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        var viewport = RdApplication.self().getViewport();

        if (viewport.getWorldWidth() < viewport.getWorldHeight()) updateVertically();
        else updateHorizontally();

        WindowUtil.resizeCenter(confExit);
        WindowUtil.resizeCenter(conf);
        WindowUtil.resizeCenter(spinner);
        WindowUtil.resizeCenter(err);
        WindowUtil.resizeCenter(selector);
        WindowUtil.resizeCenter(helpDialog);
        WindowUtil.resizeCenter(replaceDialog);
    }

    private void updateVertically() {
        if (!vertically) {
            content.clear();
            boardTable.clear();

            var buttons = new Table();
            buttons.add(saveExit).fillX().padRight(5);
            buttons.add(change).fillX().padRight(5).row();
            buttons.add(update).fillX().padRight(5);
            buttons.add(help).fillX();

            boardCell = boardTable.add(moddingView).fill().padTop(50);
            boardTable.row();
            boardTable.add(buttons).padTop(10).fillX();
        }

        var viewport = RdApplication.self().getViewport();
        var rectSize = Math.min(viewport.getWorldWidth() - 40, viewport.getWorldHeight() - 290);
        if (rectSize > ChessConstants.localData.getMaxBoardSize()) rectSize = ChessConstants.localData.getMaxBoardSize();

        if (boardCell != null) resizeBoard(boardCell, rectSize);

        if (changeDialog != null) {
            float heightDialog = getStage().getHeight() > 1620 ? 1600f : getStage().getHeight() - 20;
            float widthDialog = getStage().getWidth() > 1320 ? 1300f : getStage().getWidth() - 20;
            changeDialog.setSize(widthDialog, heightDialog);
            WindowUtil.resizeCenter(changeDialog);
        }
    }

    private void updateHorizontally() {
        if (!vertically) {
            boardTable.clear();
            content.clear();

            boardCell = boardTable.add(moddingView)
                    .expand().fill().padLeft(200);

            content.padTop(30).padLeft(5);
            content.align(Align.topLeft);
            content.add(saveExit).fillX().row();
            content.add(change).fillX().row();
            content.add(update).fillX().row();
            content.add(help).fillX();
        }

        var viewport = RdApplication.self().getViewport();
        var rectSize = Math.min(viewport.getWorldWidth() - 450, viewport.getWorldHeight() - 40);
        if (rectSize > ChessConstants.localData.getMaxBoardSize()) rectSize = ChessConstants.localData.getMaxBoardSize();

        if (boardCell != null) resizeBoard(boardCell, rectSize);

        if (changeDialog != null) {
            float heightDialog = getStage().getHeight() > 1620 ? 1600f : getStage().getHeight() - 20;
            float widthDialog = getStage().getWidth() > 1320 ? 1300f : getStage().getWidth() - 20;
            changeDialog.setSize(widthDialog, heightDialog);
            WindowUtil.resizeCenter(changeDialog);
        }
    }

    private void resizeBoard(Cell<ModdingView> cell, float rectSize) {
        coefX = mapData.getWidth()
                / mapData.getHeight();
        coefY = mapData.getHeight()
                / mapData.getWidth();

        if (coefX > coefY) {
            cell.width(rectSize);
            cell.height(rectSize / coefX);
        } else {
            cell.width(rectSize / coefY);
            cell.height(rectSize);
        }
    }

    @Override
    public Actor hide(SequenceAction action, Activity next) {
        TransitionEffects.alphaHide(action, ChessConstants.localData.getScreenDuration());
        return getStage().getRoot();
    }
}
