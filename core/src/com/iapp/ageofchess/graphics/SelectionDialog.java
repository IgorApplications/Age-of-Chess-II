package com.iapp.ageofchess.graphics;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.iapp.ageofchess.chess_engine.Color;
import com.iapp.ageofchess.chess_engine.TypePiece;
import com.iapp.ageofchess.controllers.EngineController;
import com.iapp.ageofchess.util.ChessAssetManager;
import com.iapp.rodsher.actors.RdDialog;
import com.iapp.rodsher.util.OnChangeListener;

import java.util.function.Consumer;

public class SelectionDialog extends RdDialog {

    private final EngineController controller;
    private ImageButton queen, rook, bishop, knight;
    private Consumer<TypePiece> selectionListener = typePiece -> {};

    public SelectionDialog(String title, RdDialogStyle style, EngineController controller) {
        super(title, style);
        this.controller = controller;

        init();
        addActors();
    }

    private void init() {
        reset();
        var assetManger = ChessAssetManager.current();
        var color = controller.getColorMove() == Color.WHITE ? "white_" : "black_";

        var queenImage = new TextureRegionDrawable(controller.getRegion(color + "queen"));
        var rookImage = new TextureRegionDrawable(controller.getRegion(color + "rook"));
        var bishopImage = new TextureRegionDrawable(controller.getRegion(color + "bishop"));
        var knightImage = new TextureRegionDrawable(controller.getRegion(color + "knight"));

        var queenStyle = new ImageButton.ImageButtonStyle();
        queenStyle.up = new TextureRegionDrawable(assetManger.getWhiteTexture());
        queenStyle.over = new TextureRegionDrawable(assetManger.getOverWhiteTexture());
        queenStyle.down = new TextureRegionDrawable(assetManger.getDownWhiteTexture());
        queenStyle.imageUp = queenImage;

        var rookStyle = new ImageButton.ImageButtonStyle();
        rookStyle.up = new TextureRegionDrawable(assetManger.getGreenTexture());
        rookStyle.over = new TextureRegionDrawable(assetManger.getOverGreenTexture());
        rookStyle.down = new TextureRegionDrawable(assetManger.getDownGreenTexture());
        rookStyle.imageUp = rookImage;

        var bishopStyle = new ImageButton.ImageButtonStyle();
        bishopStyle.up = new TextureRegionDrawable(assetManger.getGreenTexture());
        bishopStyle.over = new TextureRegionDrawable(assetManger.getOverGreenTexture());
        bishopStyle.down = new TextureRegionDrawable(assetManger.getDownGreenTexture());
        bishopStyle.imageUp = bishopImage;

        var knightStyle = new ImageButton.ImageButtonStyle();
        knightStyle.up = new TextureRegionDrawable(assetManger.getWhiteTexture());
        knightStyle.over = new TextureRegionDrawable(assetManger.getOverWhiteTexture());
        knightStyle.down = new TextureRegionDrawable(assetManger.getDownWhiteTexture());
        knightStyle.imageUp = knightImage;

        queen = new ImageButton(queenStyle);
        rook = new ImageButton(rookStyle);
        bishop = new ImageButton(bishopStyle);
        knight = new ImageButton(knightStyle);
    }

    private void addActors() {
        queen.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                selectionListener.accept(TypePiece.QUEEN);
            }
        });
        rook.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                selectionListener.accept(TypePiece.ROOK);
            }
        });
        bishop.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                selectionListener.accept(TypePiece.BISHOP);
            }
        });
        knight.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                selectionListener.accept(TypePiece.KNIGHT);
            }
        });

        padTop(50);
        getTitleTable().clear();
        getTitleTable().align(Align.center).columnDefaults(2);
        getTitleLabel().setAlignment(Align.center);
        getTitleTable().add(getTitleLabel()).expandX().fillX().center();

        add(queen).expand().fill().bottom();
        add(rook).expand().fill().bottom().row();
        add(bishop).expand().fill().bottom();
        add(knight).expand().fill().bottom().row();
    }

    public Consumer<TypePiece> getSelectionListener() {
        return selectionListener;
    }

    public void setSelectionListener(Consumer<TypePiece> selectionListener) {
        this.selectionListener = selectionListener;
    }

    public void setImagesSize(float width, float height) {
        queen.getImageCell().size(width, height);
        rook.getImageCell().size(width, height);
        bishop.getImageCell().size(width, height);
        knight.getImageCell().size(width, height);
    }
}
