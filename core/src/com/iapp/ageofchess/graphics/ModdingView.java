package com.iapp.ageofchess.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.iapp.lib.chess_engine.BoardMatrix;
import com.iapp.lib.chess_engine.Color;
import com.iapp.lib.chess_engine.Game;
import com.iapp.ageofchess.controllers.EditMapController;
import com.iapp.ageofchess.modding.MapData;
import com.iapp.ageofchess.services.ChessAssetManager;
import com.iapp.lib.ui.actors.*;
import com.iapp.lib.util.OnChangeListener;

import java.util.Arrays;
import java.util.Objects;

public class ModdingView extends Image {

    private final Vector3 spriteTouchPoint = new Vector3();
    private final MapData data;
    private final EditMapController controller;
    private final Stage stage;
    private final TextureAtlas.AtlasRegion yellowRegion;

    private double padLeft, padBottom, cellWidth, cellHeight;
    private PieceView[][] pieceViews = new PieceView[8][8];
    private PieceView selected;
    private Game game;
    private int scenario = 0;

    public ModdingView(EditMapController controller, MapData data) {
        this.controller = controller;
        this.data = data;
        stage = controller.getActivity().getStage();
        yellowRegion = ChessAssetManager.current().findChessRegion("yellow_frame");
    }

    public int getScenario() {
        return scenario;
    }

    public void setScenario(int scenario) {
        this.scenario = scenario;
    }

    public void updateSize() {
        sizeChanged();
        controller.getActivity()
                .resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    public void update() {
        if (data.getScenarios().length == 0) return;
        game = new Game(Color.BLACK, data.getScenarios()[scenario]);
        var matrix = getMatrix();
        pieceViews = new PieceView[8][8];

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                var pieceView = new PieceView(j, i,
                        (float) (cellWidth * j + padLeft),
                        (float) (cellHeight * i + padBottom),
                        (float) cellWidth, (float) cellHeight);

                if (!game.isCage(matrix[i][j])) {
                    pieceView.sprite.setRegion(getRegion(matrix[i][j]));
                } else {
                    pieceView.cage = true;
                }
                pieceViews[i][j] = pieceView;
            }
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        batch.draw(controller.getRegion("board"), getX(), getY(), getWidth(), getHeight());
        for (var view : getPieceViews()) {
            view.sprite.setPosition(getX() + view.x, getY() + view.y);
            if (view.cage) continue;

            batch.draw(view.sprite, view.sprite.getX(), view.sprite.getY(),
                    view.width, view.height);
        }

        if (selected != null) {
            batch.draw(yellowRegion, selected.sprite.getX(), selected.sprite.getY(),
                    selected.width, selected.height);
        }

        handleTouchSprites();
    }

    private void handleTouchSprites() {
        if (Gdx.input.justTouched() && !controller.getActivity().getBlackout().isVisible()) {
            stage.getViewport().getCamera()
                    .unproject(spriteTouchPoint.set(Gdx.input.getX(), Gdx.input.getY(),0));

            for (var view : getPieceViews()) {
                if (view.sprite.getBoundingRectangle().contains(spriteTouchPoint.x, spriteTouchPoint.y)) {
                    selected = view;
                    showSelectionDialog(view);
                }
            }
        }
    }

    private void showSelectionDialog(PieceView piece) {
        controller.getActivity().getBlackout().setVisible(true);
        var dialog = new RdDialog(controller.getStrings().get("piece_replacement"),
                ChessAssetManager.current().getSkin(), "input");
        dialog.setOnCancel(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                selected = null;
                controller.getActivity().getReplaceDialog().hide();
                controller.getActivity().getBlackout().setVisible(false);
            }
        });
        var group = new ButtonGroup<ImageButton>();

        var content = new Table();
        var scroll = new RdScrollPane(content, ChessAssetManager.current().getSkin());
        scroll.setFadeScrollBars(false);
        scroll.setOverscroll(false, false);
        scroll.setScrollingDisabled(true, false);

        content.add(generateButton(getRegion(BoardMatrix.PAWN), piece, group)).size(128, 128).pad(3, 3, 3, 3);
        content.add(generateButton(getRegion(BoardMatrix.ROOK), piece, group)).size(128, 128).pad(3, 3, 3, 3);
        content.add(generateButton(getRegion(BoardMatrix.BISHOP), piece, group)).size(128, 128).pad(3, 3, 3, 3);
        content.add(generateButton(getRegion(BoardMatrix.KNIGHT), piece, group)).size(128, 128).pad(3, 3, 3, 3);
        content.row();
        content.add(generateButton(getRegion(BoardMatrix.QUEEN), piece, group)).size(128, 128).pad(3, 3, 3, 3);
        content.add(generateButton(getRegion(BoardMatrix.KING), piece, group)).size(128, 128).pad(3, 3, 3, 3);
        content.add(generateButton(getRegion((byte) -BoardMatrix.PAWN), piece, group)).size(128, 128).pad(3, 3, 3, 3);
        content.add(generateButton(getRegion((byte) -BoardMatrix.ROOK), piece, group)).size(128, 128).pad(3, 3, 3, 3);
        content.row();
        content.add(generateButton(getRegion((byte) -BoardMatrix.BISHOP), piece, group)).size(128, 128).pad(3, 3, 3, 3);
        content.add(generateButton(getRegion((byte) -BoardMatrix.KNIGHT), piece, group)).size(128, 128).pad(3, 3, 3, 3);
        content.add(generateButton(getRegion((byte) -BoardMatrix.QUEEN), piece, group)).size(128, 128).pad(3, 3, 3, 3);
        content.add(generateButton(getRegion((byte) -BoardMatrix.KING), piece, group)).size(128, 128).pad(3, 3, 3, 3);
        content.row();
        content.add(generateButton(null, piece, group)).size(128, 128).pad(3, 3, 3, 3);
        dialog.getContentTable().add(scroll).expand().fill();

        var replace = new RdTextButton(controller.getStrings().get("replace"), "blue");
        replace.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                replacePiece(group.getCheckedIndex(), piece);

                update();
                selected = null;
                controller.getActivity().getReplaceDialog().hide();
                controller.getActivity().getBlackout().setVisible(false);
            }
        });
        var cancel = new RdTextButton(controller.getStrings().get("cancel"));
        cancel.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                selected = null;
                controller.getActivity().getReplaceDialog().hide();
                controller.getActivity().getBlackout().setVisible(false);
            }
        });

        dialog.getButtonTable().add(replace).expandX().fillX();
        dialog.getButtonTable().add(cancel).expandX().fillX();

        dialog.show(controller.getActivity().getStage());
        dialog.setSize(700, 700);
        controller.getActivity().setReplaceDialog(dialog);
        controller.getActivity().resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private ImageButton generateButton(TextureAtlas.AtlasRegion region, PieceView piece, ButtonGroup<ImageButton> group) {
        var style = new ImageButton.ImageButtonStyle(ChessAssetManager.current().getSelectedStyle());
        if (region != null) style.imageUp = new TextureRegionDrawable(region);
        var button = new ImageButton(style);
        group.add(button);

        if (piece != null && ((piece.cage && region == null) ||
                (!piece.cage && getRegion(getMatrix()[piece.pieceY][piece.pieceX]).equals(region)))
        ) {
            button.setChecked(true);
        }

        return button;
    }

    private char definePiece(int index) {
        switch (index) {
            case 0: return 'p';
            case 1: return 'r';
            case 2: return 'b';
            case 3: return 'n';
            case 4: return 'q';
            case 5: return 'k';
            case 6: return 'P';
            case 7: return 'R';
            case 8: return 'B';
            case 9: return 'N';
            case 10: return 'Q';
            case 11: return 'K';
            case 12: return '1';
        }
        throw new IllegalArgumentException("Unknown piece list index, index = " + index);
    }

    private void replacePiece(int index, PieceView view) {
        var fen = data.getScenarios()[scenario]
                .replaceAll("8", "11111111")
                .replaceAll("7", "1111111")
                .replaceAll("6", "111111")
                .replaceAll("5", "11111")
                .replaceAll("4", "1111")
                .replaceAll("3", "111")
                .replaceAll("2", "11");

        var array = fen.split(" ");
        var lines = array[0].split("/");

        var charArr = lines[7 - view.pieceY].toCharArray();
        charArr[view.pieceX] = definePiece(index);
        lines[7 - view.pieceY] = new String(charArr);

        var builder = new StringBuilder();
        for (var line : lines) {
            builder.append(line);
            builder.append("/");
        }
        builder.append(" ");

        for (int i = 1; i < array.length; i++) {
            builder.append(array[i]);
            builder.append(" ");
        }

        data.getScenarios()[scenario] = builder.toString().trim();
    }

    @Override
    protected void sizeChanged() {
        super.sizeChanged();

        double coefficientW = getWidth() / data.getWidth(),
                coefficientH = getHeight() / data.getHeight();

        padLeft = data.getPadLeft() * coefficientW;
        double padRight = data.getPadRight() * coefficientW;
        padBottom = data.getPadBottom() * coefficientH;
        double padTop = data.getPadTop() * coefficientH;

        cellWidth = (getWidth() - padLeft - padRight) / 8;
        cellHeight = (getHeight() - padBottom - padTop) / 8;

        update();
    }

    private Array<PieceView> getPieceViews() {
        var arr = new Array<PieceView>();
        Arrays.stream(pieceViews)
                .flatMap(Arrays::stream)
                .filter(Objects::nonNull)
                .forEach(arr::add);
        return arr;
    }

    private byte[][] getMatrix() {
        var gameMatrix = game.getMatrix();
        var matrix = new byte[gameMatrix.length][gameMatrix[0].length];
        matrix[0] = gameMatrix[7];
        matrix[1] = gameMatrix[6];
        matrix[2] = gameMatrix[5];
        matrix[3] = gameMatrix[4];
        matrix[4] = gameMatrix[3];
        matrix[5] = gameMatrix[2];
        matrix[6] = gameMatrix[1];
        matrix[7] = gameMatrix[0];

        return matrix;
    }

    private TextureAtlas.AtlasRegion getRegion(byte type) {
        String color = game.getColor(type) == Color.BLACK ? "black_" : "white_";
        if (game.isPawn(type)) return controller.getRegion(color + "pawn");
        else if (game.isRook(type)) return controller.getRegion(color + "rook");
        else if (game.isKnight(type)) return controller.getRegion(color + "knight");
        else if (game.isBishop(type)) return controller.getRegion(color + "bishop");
        else if (game.isQueen(type)) return controller.getRegion(color + "queen");
        else if (game.isKing(type)) return controller.getRegion(color + "king");
        throw new IllegalArgumentException("unknown type");
    }

    private static class PieceView {

        private final Sprite sprite;
        private int pieceX, pieceY;
        private float x, y, width, height;
        private boolean cage;

        PieceView() {
            sprite = new Sprite();
        }

        PieceView(int pieceX, int pieceY, float x, float y, float width, float height) {
            this();
            update(pieceX, pieceY, x, y, width, height);
        }

        void update(int pieceX, int pieceY, float x, float y, float width, float height) {
            this.pieceX = pieceX;
            this.pieceY = pieceY;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            sprite.setPosition(x, y);
            sprite.setSize(width, height);
        }

        void setPosition(float x, float y) {
            this.x = x;
            this.y = y;
            sprite.setPosition(x, y);
        }

        void setSize(float width, float height) {
            this.width = width;
            this.height = height;
            sprite.setSize(width, height);
        }
    }
}
