package com.iapp.lib.ui.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.iapp.lib.chess_engine.Chess2dController;
import com.iapp.lib.chess_engine.Move;
import com.iapp.lib.ui.screens.RdApplication;
import com.iapp.lib.util.BooleanList;
import com.iapp.lib.util.CallListener;
import com.iapp.lib.util.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class BoardView extends Image {

    private final Chess2dController controller;
    private final Vector3 spriteTouchPoint = new Vector3();
    private final List<Transition> transitions = RdApplication.self().getLauncher().copyOnWriteArrayList();
    private final BoardViewStyle style;

    private double padLeft, padBottom, cellWidth, cellHeight;
    private boolean blockedMove;
    private final BooleanList blocked = new BooleanList();
    private CallListener onEndMove;
    private PieceView[][] pieceViews = new PieceView[8][8];
    private PieceView selected;
    private PieceView checked;
    private Array<MoveView> moveViews = new Array<>();
    private MoveView lastPosition, hintView;
    private float pieceDuration;

    public BoardView(Chess2dController controller, Skin skin, float pieceDuration) {
        this(controller, skin, "default", pieceDuration);
    }

    public BoardView(Chess2dController controller, Skin skin, String skinName, float pieceDuration) {
        this.controller = controller;
        style = skin.get(skinName, BoardViewStyle.class);
        this.pieceDuration = pieceDuration;
    }

    public boolean isBlocked() {
        return blocked.get();
    }

    public void addBlocked() {
        blocked.addTrue();
    }

    public void addUnblocked() {
        blocked.addFalse();
    }

    public boolean isBlockedMove() {
        return blockedMove;
    }

    public void setBlockedMove(boolean blockedMove) {
        this.blockedMove = blockedMove;
    }

    public void makeMove(Move move, boolean castling, boolean updated, CallListener onEndMove) {
        selected = null;
        this.onEndMove = onEndMove;
        transitions.clear();

        PieceView movingPiece = pieceViews[move.getPieceY()][move.getPieceX()];
        if (movingPiece == checked) checked = null;
        Transition movingTransit = new Transition(movingPiece, move);
        movingTransit.moving = true;
        transitions.add(movingTransit);

        if (castling) {
            boolean undoMove = move.getPieceX() != 3 && move.getPieceX() != 4;

            if (undoMove) {
                if (move.getPieceX() < move.getMoveX()) {
                    transitions.add(new Transition(pieceViews[move.getPieceY()][move.getPieceX() + 1],
                            Move.valueOf(move.getPieceX() + 1, move.getPieceY(), 0, move.getMoveY())));
                } else if (move.getPieceX() > move.getMoveX()) {
                    transitions.add(new Transition(pieceViews[move.getPieceY()][move.getPieceX() - 1],
                            Move.valueOf(move.getPieceX() - 1, move.getPieceY(), 7, move.getMoveY())));
                }
            } else {
                if (move.getPieceX() > move.getMoveX()) {
                    transitions.add(new Transition(pieceViews[move.getPieceY()][0],
                            Move.valueOf(0, move.getMoveY(), move.getMoveX() + 1, move.getMoveY())));
                } else if (move.getPieceX() < move.getMoveX()) {
                    transitions.add(new Transition(pieceViews[move.getPieceY()][7],
                            Move.valueOf(7, move.getMoveY(), move.getMoveX() - 1, move.getMoveY())));
                }
            }
        }

        byte[][] matrix = controller.getMatrix();
        pieceViews = new PieceView[8][8];

        if (updated) {
            movingPiece.sprite.setRegion(controller.getRegion(matrix[move.getMoveY()][move.getMoveX()]));
        }

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                if (controller.isCage(matrix[i][j])) continue;

                boolean skip = false;
                for (Transition transition : transitions) {
                    if (transition.move.getMoveX() == j && transition.move.getMoveY() == i) {
                        pieceViews[i][j] = transition.movingPiece;
                        skip = true;
                        break;
                    } else if (transition.move.getPieceX() == j && transition.move.getPieceY() == i) {
                        skip = true;
                        break;
                    }
                }
                if (skip) continue;

                PieceView pieceView = new PieceView(j, i,
                        (float) (cellWidth * j + padLeft),
                        (float) (cellHeight * i + padBottom),
                        (float) cellWidth, (float) cellHeight);
                pieceView.sprite.setRegion(controller.getRegion(matrix[i][j]));

                pieceViews[i][j] = pieceView;
            }
        }
    }

    public void undoMove(Move move, boolean castling, boolean updated, CallListener onEndMove) {
        checked = null;
        hintView = null;
        lastPosition = null;
        moveViews.clear();
        makeMove(Move.valueOf(move.getMoveX(), move.getMoveY(), move.getPieceX(), move.getPieceY()),
                castling, updated, () -> {
                    selected = null;
                    lastPosition = null;
                    update();
                    onEndMove.call();
                });
    }

    public void cancelMove() {
        selected = null;
        lastPosition = null;
        checked = null;
        moveViews.clear();
        update();
    }

    public void showHint(Move move) {
        moveViews.clear();
        lastPosition = null;
        hintView = new MoveView(move);
        selected = pieceViews[move.getPieceY()][move.getPieceX()];
    }

    public void updateCheck() {
        update();
        Pair<Integer, Integer> position = controller.getCheckKing();
        if (position != null) checked = pieceViews[position.getValue()][position.getKey()];
        else checked = null;
    }

    public void update() {
        byte[][] matrix = controller.getMatrix();
        pieceViews = new PieceView[8][8];

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                if (controller.isCage(matrix[i][j])) continue;

                PieceView pieceView = new PieceView(j, i,
                        (float) (cellWidth * j + padLeft),
                        (float) (cellHeight * i + padBottom),
                        (float) cellWidth, (float) cellHeight);

                pieceView.sprite.setRegion(controller.getRegion(matrix[i][j]));
                pieceViews[i][j] = pieceView;
            }
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        batch.draw(controller.getRegion("board"), getX(), getY(), getWidth(), getHeight());
        for (PieceView view : getPieceViews()) {
            boolean skip = false;
            for (Transition transition : transitions) {
                if (transition.movingPiece == view) {
                    skip = true;
                    break;
                }
            }
            if (skip) continue;

            view.sprite.setPosition(getX() + view.x, getY() + view.y);
            batch.draw(view.sprite, view.sprite.getX(), view.sprite.getY(),
                    view.width, view.height);
        }

        if (selected != null) {
            batch.draw(style.greenFrame, getX() + selected.x - 0.5f, getY() + selected.y -  0.5f,
                    selected.width +  1, selected.height +  1);
        }

        if (checked != null) {
            batch.draw(style.redFrame,
                    getX() + checked.x - 0.5f, getY() + checked.y -  0.5f,
                    checked.width +  1, checked.height +  1);
        }

        for (MoveView moveView : moveViews) {
            moveView.sprite.setPosition(getX() + moveView.spriteX, getY() + moveView.spriteY);
            if (pieceViews[moveView.move.getMoveY()][moveView.move.getMoveX()] != null) {
                batch.draw(style.yellowRegion, getX() + moveView.spriteX - 0.5f, getY() + moveView.spriteY - 0.5f,
                        moveView.sprite.getWidth() + 1, moveView.sprite.getHeight() + 1);
            } else if (moveView.castle) {
                batch.draw(style.castleRegion, getX() + moveView.moveX, getY() + moveView.moveY,
                        moveView.radius, moveView.radius);
            } else {
                batch.draw(style.moveRegion, getX() + moveView.moveX, getY() + moveView.moveY,
                        moveView.radius, moveView.radius);
            }
        }

        if (lastPosition != null) {
            batch.draw(style.greenCross, getX() + lastPosition.spriteX, getY() + lastPosition.spriteY,
                    lastPosition.sprite.getWidth(), lastPosition.sprite.getHeight());
        }

        if (hintView != null) {
            hintView.sprite.setPosition(getX() + hintView.spriteX, getY() + hintView.spriteY);
            batch.draw(style.blueFrame, getX() + hintView.spriteX - 0.5f, getY() + hintView.spriteY - 0.5f,
                    hintView.sprite.getWidth() + 1, hintView.sprite.getHeight() + 1);
        }

        boolean transitionsReady = !transitions.isEmpty();
        for (Transition transition : transitions) {
            transition.update();
            PieceView view = transition.movingPiece;
            view.sprite.setPosition(getX() + view.x, getY() + view.y);
            batch.draw(view.sprite, view.sprite.getX(), view.sprite.getY(),
                    view.width, view.height);

            transitionsReady = transitionsReady && transition.percent >= 0.999_999f;

            if (transition.percent >= 0.999_999f) {
                PieceView piece = transition.movingPiece;
                Move move = transition.move;

                piece.pieceX = move.getMoveX();
                piece.pieceY = move.getMoveY();
                piece.setPosition(
                        (float) (move.getMoveX() * cellWidth + padLeft),
                        (float) (move.getMoveY() * cellHeight + padBottom));

                if (!transition.moving) transitions.remove(transition);
            }
        }

        if (transitionsReady) {
            for (Transition transition : transitions) {
                // single
                if (transition.moving) {
                    PieceView piece = transition.movingPiece;
                    Move move = transition.move;

                    lastPosition = new MoveView(
                            Move.valueOf(move.getPieceX(), move.getPieceY(),
                                    move.getPieceX(), move.getPieceY()));
                    selected = piece;
                    moveViews.clear();
                    updateCheck();
                    onEndMove.call();

                    onEndMove = null;
                    transitions.remove(transition);
                }
                draw(batch, parentAlpha);
            }
        }

        if (transitions.isEmpty() && !blockedMove && !blocked.get()) handleTouchSprites();
    }

    @Override
    protected void sizeChanged() {
        super.sizeChanged();

        double coefficientW = getWidth() / controller.getWidth(),
                coefficientH = getHeight() / controller.getHeight();

        padLeft = controller.getPadLeft() * coefficientW;
        double padRight = controller.getPadRight() * coefficientW;
        padBottom = controller.getPadBottom() * coefficientH;
        double padTop = controller.getPadTop() * coefficientH;

        cellWidth = (getWidth() - padLeft - padRight) / 8;
        cellHeight = (getHeight() - padBottom - padTop) / 8;

        for (MoveView moveView : moveViews) moveView.update();
        update();
        if (selected != null) selected = pieceViews[selected.pieceY][selected.pieceX];
        if (checked != null) checked = pieceViews[checked.pieceY][checked.pieceX];
        if (lastPosition != null) lastPosition.update();
        if (hintView != null) hintView.update();
    }

    private void handleTouchSprites() {
        if (Gdx.input.justTouched()) {
            RdApplication.self().getStage().getViewport().getCamera()
                    .unproject(spriteTouchPoint.set(Gdx.input.getX(), Gdx.input.getY(),0));

            for (MoveView moveView : moveViews) {
                if (moveView.sprite.getBoundingRectangle().contains(spriteTouchPoint.x, spriteTouchPoint.y)) {
                    selected = null;
                    lastPosition = null;
                    hintView = null;
                    moveViews = new Array<>();

                    controller.makeMove(moveView.move, null);
                }
            }

            for (PieceView view : getPieceViews()) {
                if (!controller.getMoves(view.pieceX, view.pieceY).isEmpty()
                        && view.sprite.getBoundingRectangle().contains(spriteTouchPoint.x, spriteTouchPoint.y)
                        && transitions.isEmpty()) {
                    selected = view;
                    lastPosition = null;
                    checked = null;
                    hintView = null;
                    moveViews = new Array<>();

                    Array<Move> moves = controller.getMoves(selected.pieceX, selected.pieceY);
                    for (Move move : moves) {
                        MoveView moveView = new MoveView(move);
                        if (controller.isCastleMove(move)) moveView.castle = true;
                        moveViews.add(moveView);
                    }
                }
            }

            if (hintView != null && hintView.sprite.getBoundingRectangle()
                    .contains(spriteTouchPoint.x, spriteTouchPoint.y)) {
                Move move = hintView.move;
                hintView = null;
                controller.makeMove(move, null);
            }
        }
    }

    private Array<PieceView> getPieceViews() {
        Array<PieceView> arr = new Array<PieceView>();
        Arrays.stream(pieceViews)
                .flatMap(Arrays::stream)
                .filter(Objects::nonNull)
                .forEach(arr::add);
        return arr;
    }

    private static class PieceView {

        private final Sprite sprite;
        private int pieceX, pieceY;
        private float x, y, width, height;

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

    private class MoveView {

        private final Sprite sprite;
        private final Move move;
        private boolean castle;
        private float spriteX, spriteY;
        private float moveX, moveY, radius;

        MoveView(Move move) {
            sprite = new Sprite();
            this.move = move;

            radius = (float) ((cellWidth + cellHeight) / 10);
            double shiftLeft = cellWidth / 2 - radius / 2;
            double shiftTop = cellHeight / 2 - radius / 2;

            moveX = (float) (cellWidth * move.getMoveX() + padLeft + shiftLeft);
            moveY = (float) (cellHeight * move.getMoveY() + padBottom + shiftTop);

            spriteX = (float) (cellWidth * move.getMoveX() + padLeft);
            spriteY = (float) (cellHeight *  move.getMoveY() + padBottom);
            sprite.setSize((float) cellWidth, (float) cellHeight);
        }

        private void update() {
            radius = (float) ((cellWidth + cellHeight) / 10);
            double shiftLeft = cellWidth / 2 - radius / 2;
            double shiftTop = cellHeight / 2 - radius / 2;

            moveX = (float) (cellWidth * move.getMoveX() + padLeft + shiftLeft);
            moveY = (float) (cellHeight * move.getMoveY() + padBottom + shiftTop);

            spriteX = (float) (cellWidth * move.getMoveX() + padLeft);
            spriteY = (float) (cellHeight *  move.getMoveY() + padBottom);
            sprite.setSize((float) cellWidth, (float) cellHeight);
        }
    }

    private class Transition {

        private final PieceView movingPiece;
        private final Move move;
        private float percent;
        private long lastRender;
        private boolean moving;

        Transition(PieceView movingPiece, Move move) {
            this.movingPiece = movingPiece;
            this.move = move;
            percent = 0;
            lastRender = System.currentTimeMillis();
        }

        void update() {
            // movement formula taking into account the screen FPS!
            float lengthX = Math.abs(move.getMoveX() - move.getPieceX());
            float lengthY = Math.abs(move.getMoveY() - move.getPieceY());

            percent += pieceDuration * (System.currentTimeMillis() - lastRender)
                / (Math.max(lengthX, lengthY) / 2);

            double startX = movingPiece.pieceX * cellWidth + padLeft;
            double startY = movingPiece.pieceY * cellHeight + padBottom;

            double vectorX = (move.getMoveX() * cellWidth + padLeft - startX) * percent;
            double vectorY = (move.getMoveY() * cellHeight + padBottom - startY) * percent;

            movingPiece.setPosition((float) (startX + vectorX), (float) (startY + vectorY));
            lastRender = System.currentTimeMillis();
        }
    }

    public static class BoardViewStyle {
        public TextureAtlas.AtlasRegion moveRegion, castleRegion,
            greenFrame, yellowRegion, greenCross, blueFrame, redFrame;


        public BoardViewStyle(TextureAtlas.AtlasRegion moveRegion, TextureAtlas.AtlasRegion castleRegion, TextureAtlas.AtlasRegion greenFrame,
                              TextureAtlas.AtlasRegion yellowRegion, TextureAtlas.AtlasRegion greenCross, TextureAtlas.AtlasRegion blueFrame,
                              TextureAtlas.AtlasRegion redFrame, float pieceDuration) {
            this.moveRegion = moveRegion;
            this.castleRegion = castleRegion;
            this.greenFrame = greenFrame;
            this.yellowRegion = yellowRegion;
            this.greenCross = greenCross;
            this.blueFrame = blueFrame;
            this.redFrame = redFrame;
        }

        public BoardViewStyle(BoardViewStyle style) {
            moveRegion = style.moveRegion;
            castleRegion = style.castleRegion;
            greenFrame = style.greenFrame;
            yellowRegion = style.yellowRegion;
            greenCross = style.greenCross;
            blueFrame = style.blueFrame;
            redFrame = style.redFrame;
        }

        public BoardViewStyle() {}
    }
}
