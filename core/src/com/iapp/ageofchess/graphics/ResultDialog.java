package com.iapp.ageofchess.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.iapp.ageofchess.util.ChessAssetManager;
import com.iapp.rodsher.actors.RdCell;
import com.iapp.rodsher.actors.RdDialog;
import com.iapp.rodsher.actors.RdLabel;
import com.iapp.rodsher.actors.RdWindow;
import com.iapp.rodsher.util.OnChangeListener;

public class ResultDialog extends RdDialog {

    private final String title;
    private final Color titleColor;
    private RdCell<Table> tittleTableCell;
    private OnChangeListener onChange = new OnChangeListener() {
        @Override
        public void onChange(Actor actor) {
            hide();
        }
    };
    private Button cancelButton;

    public ResultDialog(String title, Color titleColor, RdDialog.RdDialogStyle style) {
        super("", style);
        this.title = title;
        this.titleColor = titleColor;
        init();
    }

    private void init() {
        getTitleTable().removeActor(getTitleLabel());

        var titleLabel = new RdLabel(title);
        titleLabel.setColor(titleColor);
        titleLabel.setAlignment(Align.center);
        cancelButton = new Button(ChessAssetManager.current().getCancelStyle());
        cancelButton.addListener(onChange);

        pad(0);
        getTitleTable().padTop(100);

        var titleTable = new Table();
        titleTable.add(titleLabel).expandX().fillX().left().row();
        titleTable.add(defineLine()).height(5).expandX().fillX().left();

        tittleTableCell = getTitleTable().add(titleTable).expandX().fillX().left();
        getTitleTable().add(cancelButton).padRight(13).padLeft(15).row();
    }

    public void setOnCancel(OnChangeListener onChange) {
        cancelButton.removeListener(this.onChange);
        this.onChange = onChange;
        cancelButton.addListener(onChange);
    }

    @Override
    protected void sizeChanged() {
        super.sizeChanged();
        if (tittleTableCell != null) tittleTableCell.width(getWidth() - 93);
    }

    private Image defineLine() {
        if (titleColor == Color.GOLD) return new Image(ChessAssetManager.current().getGoldTexture());
        else if (titleColor == Color.GREEN) return new Image(ChessAssetManager.current().getGreenTexture());
        else if (titleColor == Color.RED) return new Image(ChessAssetManager.current().getRedTexture());
        throw new IllegalArgumentException("title color != GOLD or GREEN or RED");
    }
}
