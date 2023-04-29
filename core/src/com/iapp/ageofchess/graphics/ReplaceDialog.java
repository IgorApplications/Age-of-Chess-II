package com.iapp.ageofchess.graphics;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.iapp.ageofchess.chess_engine.Color;
import com.iapp.ageofchess.chess_engine.TypePiece;
import com.iapp.ageofchess.controllers.EditMapController;
import com.iapp.ageofchess.util.ChessAssetManager;
import com.iapp.rodsher.util.OnChangeListener;
import com.iapp.rodsher.util.Pair;

import java.util.function.Consumer;

public class ReplaceDialog extends Dialog {

    private final EditMapController controller;
    private Consumer<Pair<Color, TypePiece>> selectionListener = typePiece -> {};

    public ReplaceDialog(String title, WindowStyle style, EditMapController controller) {
        super(title, style);
        this.controller = controller;

        initialize();
    }

    private void initialize() {
        reset();

        padTop(50);
        getTitleTable().clear();
        getTitleTable().align(Align.center).columnDefaults(2);
        getTitleLabel().setAlignment(Align.center);
        getTitleTable().add(getTitleLabel()).expandX().fillX().center();

        int i = 0;
        for (var color : Color.values()) {
            for (var piece : TypePiece.values()) {
                putImage(color, piece, ++i);
            }
        }
    }

    private void putImage(Color color, TypePiece typePiece, int iteration) {
        var pieceImage = new TextureRegionDrawable(controller.getRegion(
                color.toString().toLowerCase() + "_" + typePiece.toString().toLowerCase()));

        var pieceStyle  = new ImageButton.ImageButtonStyle();
        if (iteration % 2 != 0) {
            pieceStyle.up = new TextureRegionDrawable(ChessAssetManager.current().getGreenTexture());
            pieceStyle.over = new TextureRegionDrawable(ChessAssetManager.current().getOverGreenTexture());
            pieceStyle.down = new TextureRegionDrawable(ChessAssetManager.current().getDownGreenTexture());

        } else {
            pieceStyle.up = new TextureRegionDrawable(ChessAssetManager.current().getWhiteTexture());
            pieceStyle.over = new TextureRegionDrawable(ChessAssetManager.current().getOverWhiteTexture());
            pieceStyle.down = new TextureRegionDrawable(ChessAssetManager.current().getDownWhiteTexture());
        }
        pieceStyle.imageUp = pieceImage;

        var button = new ImageButton(pieceStyle);
        button.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                selectionListener.accept(new Pair<>(color, typePiece));
            }
        });

        add(button).expand().fill().bottom();
        if (iteration % 3 == 0) row();
    }

    public Consumer<Pair<Color, TypePiece>> getSelectionListener() {
        return selectionListener;
    }

    public void setSelectionListener(Consumer<Pair<Color, TypePiece>> selectionListener) {
        this.selectionListener = selectionListener;
    }
}
