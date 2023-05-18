package com.iapp.ageofchess.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.iapp.ageofchess.activity.GameActivity;
import com.iapp.ageofchess.activity.SavedGamesActivity;
import com.iapp.ageofchess.activity.ScenariosActivity;
import com.iapp.ageofchess.modding.LoaderMap;
import com.iapp.ageofchess.modding.MatchState;
import com.iapp.ageofchess.services.ChessAssetManager;
import com.iapp.ageofchess.services.ChessConstants;
import com.iapp.lib.ui.actors.RdDialog;
import com.iapp.lib.ui.actors.RdDialogBuilder;
import com.iapp.lib.ui.actors.Spinner;
import com.iapp.lib.ui.screens.Controller;
import com.iapp.lib.ui.screens.RdApplication;
import com.iapp.lib.util.OnChangeListener;

public class SavedGamesController extends Controller {

    private final SavedGamesActivity activity;
    private RdDialog question;
    private AssetDescriptor<TextureAtlas> atlasDesc;
    private AssetDescriptor<Texture>[] textureDesc;

    public SavedGamesController(SavedGamesActivity activity) {
        super(activity);
        this.activity = activity;
    }

    public void goToScenario() {
        startActivity(new ScenariosActivity());
    }

    public void lunchGame(MatchState state) {
        if (!state.getMatch().containsMatchData()) {
            showMapNotFound(state);
            return;
        }
        var mapData = state.getMatch().getMatchData();

        var spinner = new Spinner(strings.get("loading"));
        activity.setSpinner(spinner);
        spinner.show(RdApplication.self().getStage());
        spinner.setSize(400, 100);
        activity.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        LoaderMap.self().loadIntoRam(mapData, () ->
                startActivity(GameActivity.newInstance(state)));
    }

    public RdDialog showClearDialog(MatchState ref) {
        question = new RdDialogBuilder()
                .title(strings.get("confirmation"))
                .text(strings.get("question_clear_game"))
                .cancel(strings.get("cancel"))
                .accept(strings.get("accept"), new OnChangeListener() {
                    @Override
                    public void onChange(Actor actor){
                        ChessConstants.localData.getReferences().remove(ref);
                        activity.updateSavedGames();
                        question.hide();
                    }
                })
                .build(ChessAssetManager.current().getSkin(), "input");

        question.getIcon().setDrawable(new TextureRegionDrawable(
                ChessAssetManager.current().findRegion("icon_conf")));
        question.getIcon().setScaling(Scaling.fit);
        question.show(activity.getStage());
        question.setSize(800, 550);
        return question;
    }

    private void showMapNotFound(MatchState state) {
        var mapNotInstalled = new RdDialogBuilder()
                .title(strings.get("error"))
                .text(strings.get("map_not_found") + " \"" + state.getMatch().getMapName() + "\"")
                .accept(strings.get("accept"))
                .build(ChessAssetManager.current().getSkin(), "input");

        mapNotInstalled.getIcon().setDrawable(new TextureRegionDrawable(
                ChessAssetManager.current().findRegion("ib_error")));
        mapNotInstalled.getIcon().setScaling(Scaling.fit);

        mapNotInstalled.show(activity.getStage());
        mapNotInstalled.setSize(800, 550);
        activity.setMapNotFound(mapNotInstalled);
        activity.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }
}
