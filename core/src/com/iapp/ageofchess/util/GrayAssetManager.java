package com.iapp.ageofchess.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.github.tommyettinger.textra.Font;
import com.iapp.rodsher.actors.*;
import com.iapp.rodsher.screens.RdApplication;
import com.iapp.rodsher.screens.RdAssetManager;
import com.iapp.rodsher.util.DisposeUtil;
import com.iapp.rodsher.util.StreamUtil;
import com.iapp.rodsher.util.TextureUtil;

/**
 * Standard gray style loader
 * @author Igor Ivanov
 * @version 1.0
 * */
public class GrayAssetManager extends RdAssetManager {

    private static final AssetDescriptor<TextureAtlas> GRAY_ATLAS_DESC =
            new AssetDescriptor<>("gray_style/atlases/gray_skin.atlas", TextureAtlas.class);
    private static final AssetDescriptor<TextureAtlas> EMOJES_ATLAS_DESC =
            new AssetDescriptor<>("gray_style/atlases/emojes.atlas", TextureAtlas.class);

    private Skin graySkin;
    private TextureAtlas grayAtlas;
    private Texture grayLine, lightGray, redTexture;
    private Cursor defCursor, spinnerCursor, scrollCursor;
    private Font font;

    private NinePatchDrawable loadingBg;
    private AnimatedImage loadingAnim;

    /**
     * @return the style store of all actors
     * */
    @Override
    public Skin getSkin() {
        return graySkin;
    }

    @Override
    public void dispose() {
        DisposeUtil.dispose(grayLine);
        DisposeUtil.dispose(font);
        DisposeUtil.dispose(lightGray);
        DisposeUtil.dispose(redTexture);
        DisposeUtil.dispose(defCursor);
        DisposeUtil.dispose(scrollCursor);
        DisposeUtil.dispose(spinnerCursor);
    }

    @Override
    protected void load(AssetManager assetManager) {
        assetManager.load(GRAY_ATLAS_DESC);
        assetManager.load(EMOJES_ATLAS_DESC);
    }

    @Override
    protected void initialize(AssetManager assetManager) {
        grayAtlas = assetManager.get(GRAY_ATLAS_DESC);
        StreamUtil.streamOf(grayAtlas.getTextures()).forEach(texture ->
                texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear));

        var emojes = assetManager.get(EMOJES_ATLAS_DESC);
        StreamUtil.streamOf(emojes.getTextures()).forEach(texture ->
                texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear));

        font = new Font("gray_style/fonts/itxt.fnt", Font.DistanceFieldType.SDF)
                .addAtlas(emojes);
        font.setCrispness(3);
        graySkin = new Skin();
        graySkin.add("default", font);

        initCursor();

        initLoading();

        addRdLabelStyle();

        // "color_screen"
        addScreenButton("white", Color.WHITE);
        addScreenButton("yellow", Color.YELLOW);
        addScreenButton("blue", Color.valueOf("0094FF"));
        addScreenButton("red", Color.RED);

        addRdTextButtonStyle("default", "", new RdTextButton.RdTextButtonStyle(), Color.WHITE);
        addRdTextButtonStyle("blue", "blue_", new RdTextButton.RdTextButtonStyle(), Color.BLACK);
        addRdTextButtonStyle("dark", "dark_", new RdTextButton.RdTextButtonStyle(), Color.WHITE);

        addRdTextButtonStyle("default", "", new RdImageTextButton.RdImageTextButtonStyle(), Color.WHITE);
        addRdTextButtonStyle("blue", "blue_", new RdImageTextButton.RdImageTextButtonStyle(), Color.BLACK);
        addRdTextButtonStyle("dark", "dark_", new RdImageTextButton.RdImageTextButtonStyle(), Color.WHITE);

        addRdListStyle();

        addSampleRdScroll();
        addRdScrollStyle();

        addCheckBox();

        addInputDialog();

        addRdDialogStyle();

        addRdTextFieldStyle();

        addSelectionButton();

        addPropertyTableStyle();

        addLoggingViewStyle();

        addSpinnerStyle();

        addScreenWindowStyle();

        addLineTableStyle();

        addRdDialogBuilderStyle();

        addInputDialogBuilderStyle();

        addFileSelectorStyle();

        addRdSelectBox();

        addRdTextTooltipStyle();

        addLoadingRdTable();

        addCircleButton();

        addWidowGroupStyle();

        RdApplication.self().setCursor(defCursor);
    }

    @Override
    protected TextureAtlas getAtlas() {
        return grayAtlas;
    }

    // -----------------

    private void initLoading() {
        loadingBg = new NinePatchDrawable(
                new NinePatch(GrayAssetManager.current().findRegion("load_bg"),
                        9, 9, 9, 9));
        loadingAnim = new AnimatedImage(150,
                new TextureRegionDrawable(GrayAssetManager.current().findRegion("load_logo", 1)),
                new TextureRegionDrawable(GrayAssetManager.current().findRegion("load_logo", 2)),
                new TextureRegionDrawable(GrayAssetManager.current().findRegion("load_logo", 3)),
                new TextureRegionDrawable(GrayAssetManager.current().findRegion("load_logo", 4)),
                new TextureRegionDrawable(GrayAssetManager.current().findRegion("load_logo", 5)),
                new TextureRegionDrawable(GrayAssetManager.current().findRegion("load_logo", 6))
        );

        graySkin.add("loading_bg", loadingBg);
        graySkin.add("logo_anim", loadingAnim);
    }

    private void initCursor() {
        var pixCursor = new Pixmap(Gdx.files.internal("gray_style/textures/cursor.png"));
        var pixScroll = new Pixmap(Gdx.files.internal("gray_style/textures/scroll_cursor.png"));
        var pixSpinner = new Pixmap(Gdx.files.internal("gray_style/textures/spinner_cursor.png"));

        defCursor = Gdx.graphics.newCursor(pixCursor, 0, 0);
        scrollCursor = Gdx.graphics.newCursor(pixScroll, 0, 0);
        spinnerCursor = Gdx.graphics.newCursor(pixSpinner, 0, 0);

        pixCursor.dispose();
        pixScroll.dispose();
        pixSpinner.dispose();
    }

    private void addRdLabelStyle() {
        var style = new RdLabel.RdLabelStyle();

        style.font = font;
        style.color = Color.WHITE;

        graySkin.add("default", style);
    }

    private void addLineTableStyle() {
        var style = new LineTable.LineTableStyle();

        style.part1 = new NinePatchDrawable(
                new NinePatch(findRegion("title_panel", 1),
                        0, 3, 11, 1));
        style.part2 = new NinePatchDrawable(
                new NinePatch(findRegion("title_panel", 2),
                        3, 8, 11, 1));
        style.part3 = new NinePatchDrawable(
                new NinePatch(findRegion("title_panel", 3),
                        11, 11, 0, 12));
        style.labelStyle = graySkin.get(RdLabel.RdLabelStyle.class);

        graySkin.add("default", style);
    }

    private void addLoggingViewStyle() {
        var style = new LoggingView.LoggingViewStyle();

        style.font = font;
        style.color = Color.WHITE;
        style.colorFPS = Color.GREEN;
        style.colorRAM = Color.YELLOW;

        graySkin.add("default", style);
    }

    private void addRdTextButtonStyle(String name, String type, RdTextButton.RdTextButtonStyle style, Color color) {
        style.up = new NinePatchDrawable(
                new NinePatch(findRegion(type + "button_up"),
                        13, 15, 16, 19));
        style.over = new NinePatchDrawable(
                new NinePatch(findRegion(type + "button_over"),
                        13, 15, 16, 19));
        style.down = new NinePatchDrawable(
                new NinePatch(findRegion(type + "button_down"),
                        13, 15, 19, 16));
        style.font = font;
        style.fontColor = color;

        graySkin.add(name, style);
    }

    private void addScreenButton(String color, Color textColor) {
        var style = new RdImageTextButton.RdImageTextButtonStyle();

        style.up = new NinePatchDrawable(
                new NinePatch(findRegion("screen_" + color + "_up"),
                        75, 5, 6, 6));
        style.over = new NinePatchDrawable(
                new NinePatch(findRegion("screen_" + color + "_over"),
                        75, 5, 6, 6));
        style.down = new NinePatchDrawable(
                new NinePatch(findRegion("screen_" + color + "_down"),
                        75, 5, 9, 3));
        style.font = font;
        style.fontColor = textColor;
        style.padLeft = 0;
        style.scaleText = 1.5f;

        graySkin.add(color + "_screen", style);
    }

    private void addSelectionButton() {
        var style = new RdSelectionButton.RdSelectionButtonStyle();

        var left = new RdImageTextButton.RdImageTextButtonStyle();
        left.up = new NinePatchDrawable(
                new NinePatch(findRegion("selection_left_up"),
                        12, 2, 15, 19));
        left.over = new NinePatchDrawable(
                new NinePatch(findRegion("selection_left_over"),
                        12, 2, 15, 19));
        left.down = new NinePatchDrawable(
                new NinePatch(findRegion("selection_left_down"),
                        12, 2, 18, 16));
        left.checked = new NinePatchDrawable(
                new NinePatch(findRegion("selection_left_checked"),
                        12, 2, 15, 19));
        left.font = font;
        left.fontColor = Color.WHITE;

        var center = new RdImageTextButton.RdImageTextButtonStyle();
        center.up = new NinePatchDrawable(
                new NinePatch(findRegion("selection_center_up"),
                        2, 2, 15, 19));
        center.over = new NinePatchDrawable(
                new NinePatch(findRegion("selection_center_over"),
                        2, 2, 15, 19));
        center.down = new NinePatchDrawable(
                new NinePatch(findRegion("selection_center_down"),
                        2, 2, 18, 16));
        center.checked = new NinePatchDrawable(
                new NinePatch(findRegion("selection_center_checked"),
                        2, 2, 15, 19));
        center.font = font;
        center.fontColor = Color.WHITE;

        var right = new RdImageTextButton.RdImageTextButtonStyle();
        right.up = new NinePatchDrawable(
                new NinePatch(findRegion("selection_right_up"),
                        2, 12, 15, 19));
        right.over = new NinePatchDrawable(
                new NinePatch(findRegion("selection_right_over"),
                        2, 12, 15, 19));
        right.down = new NinePatchDrawable(
                new NinePatch(findRegion("selection_right_down"),
                        2, 12, 18, 16));
        right.checked = new NinePatchDrawable(
                new NinePatch(findRegion("selection_right_checked"),
                        2, 12, 15, 19));
        right.font = font;
        right.fontColor = Color.WHITE;

        style.left = left;
        style.center = center;
        style.right = right;

        graySkin.add("default", style);
    }

    private void addCheckBox() {
        var style = new ImageButton.ImageButtonStyle();

        style.up = new NinePatchDrawable(
                new NinePatch(findRegion("check_box_up"),
                        13, 15, 16, 19));
        style.over = new NinePatchDrawable(
                new NinePatch(findRegion("check_box_over"),
                        13, 15, 16, 19));
        style.down = new NinePatchDrawable(
                new NinePatch(findRegion("check_box_down"),
                        13, 15, 19, 16));
        style.checked = new NinePatchDrawable(
                new NinePatch(findRegion("check_box_up"),
                        13, 15, 16, 19));
        style.checkedOver = new NinePatchDrawable(
                new NinePatch(findRegion("check_box_over"),
                        13, 15, 16, 19));
        style.checkedDown = new NinePatchDrawable(
                new NinePatch(findRegion("check_box_down"),
                        13, 15, 19, 16));

        var on = findRegion("icon_on");
        var off = findRegion("icon_off");
        style.imageChecked = new TextureRegionDrawable(on);
        style.imageUp = new TextureRegionDrawable(off);

        graySkin.add("check_box", style);
    }

    private void addPropertyTableStyle() {
        var style = new PropertyTable.PropertyTableStyle();

        grayLine = TextureUtil.create(10, 10, new Color(Color.rgba8888(0, 0, 0, 0.3f)));
        style.scrollStyle = graySkin.get(RdScrollPane.RdScrollPaneStyle.class);
        style.panel = new TextureRegionDrawable(grayLine);
        style.titleStyle = new RdLabel.RdLabelStyle(font, Color.GOLD);

        style.elementStyle = new RdLabel.RdLabelStyle(font, Color.WHITE);

        graySkin.add("default", style);
    }

    private void addScreenWindowStyle() {
        var style = new RdWindow.RdWindowStyle();

        style.titleFont = font;
        style.background = new NinePatchDrawable(
                new NinePatch(findRegion("window"),
                        10, 10, 10, 10));
        style.loadingBg = loadingBg;
        style.loadingAnim = loadingAnim;

        graySkin.add("screen_window", style);
    }

    private void addInputDialog() {
        var style = new RdDialog.RdDialogStyle();

        var closeBoxStyle = new ImageButton.ImageButtonStyle();
        closeBoxStyle.up = new TextureRegionDrawable(findRegion("cancel_up"));
        closeBoxStyle.down = new TextureRegionDrawable(findRegion("cancel_down"));
        closeBoxStyle.over  = new TextureRegionDrawable(findRegion("cancel_over"));

        style.closeBoxStyle = closeBoxStyle;
        style.titleFont = font;
        style.titleFontColor = Color.BLACK;
        style.background = new TwoNinePath(
                new NinePatchDrawable(new NinePatch(
                        GrayAssetManager.current().findRegion("input_dialog1"),
                        19, 200,92, 0)),
                new NinePatchDrawable(new NinePatch(
                        GrayAssetManager.current().findRegion("input_dialog2"),
                        19, 200,2, 20))
        );
        style.loadingBg = loadingBg;
        style.loadingAnim = loadingAnim;

        style.padTopT = 80;
        style.padLeftT = 15;
        style.padRightT = 15;

        style.padBottomTitle = 18;
        style.padBottomIcon = 18;

        style.padLeftB = 15;
        style.padRightB = 15;
        style.padBottomB = 15;
        style.padTopB = 5;

        style.padLeftC = 15;
        style.padRightC = 15;
        style.padBottomC = 5;
        style.padTopC = 5;

        graySkin.add("input", style);
    }

    private void addRdDialogStyle() {
        var style = new RdDialog.RdDialogStyle();

        var closeBoxStyle = new ImageButton.ImageButtonStyle();
        closeBoxStyle.up = new TextureRegionDrawable(findRegion("cancel_up"));
        closeBoxStyle.down = new TextureRegionDrawable(findRegion("cancel_down"));
        closeBoxStyle.over  = new TextureRegionDrawable(findRegion("cancel_over"));

        style.closeBoxStyle = closeBoxStyle;
        style.titleFont = font;
        style.titleFontColor = Color.BLACK;
        style.background = new NinePatchDrawable(
                new NinePatch(findRegion("dialog"),
                        19, 200,92, 20));
        style.loadingBg = loadingBg;
        style.loadingAnim = loadingAnim;

        style.padTopT = 80;
        style.padLeftT = 15;
        style.padRightT = 15;

        style.padBottomTitle = 18;
        style.padBottomIcon = 18;

        style.padLeftB = 15;
        style.padRightB = 15;
        style.padBottomB = 12;
        style.padTopB = 5;

        style.padLeftC = 15;
        style.padRightC = 15;
        style.padBottomC = 5;
        style.padTopC = 5;

        graySkin.add("default", style);
    }

    private void addSpinnerStyle() {
        var style = new Spinner.SpinnerStyle();

        style.titleFont = font;
        style.background = new NinePatchDrawable(
                new NinePatch(findRegion("red_spinner_bg"),
                        88, 11, 11, 11));
        style.image = new AnimatedImage(140,
                new TextureRegionDrawable(findRegion("red_spinner", 1)),
                new TextureRegionDrawable(findRegion("red_spinner", 2)),
                new TextureRegionDrawable(findRegion("red_spinner", 3)),
                new TextureRegionDrawable(findRegion("red_spinner", 4)));
        style.padLeftC = 10;
        style.padTopT = 10;
        style.padBottomC = 10;
        style.padRightC = 10;

        style.cursor = spinnerCursor;

        graySkin.add("default", style);
    }

    private void addRdListStyle() {
        var style = new RdList.RdListStyle();

        style.selection = new NinePatchDrawable(
                new NinePatch(findRegion("selection"),
                        0,2,2, 2));
        style.over = new NinePatchDrawable(
                new NinePatch(findRegion("selection_over"),
                        0,2,2, 2));
        style.down = new NinePatchDrawable(
                new NinePatch(findRegion("selection_down"),
                        0,2,2, 2));
        style.backgroundElement = new NinePatchDrawable(
                new NinePatch(findRegion("selection_el"),
                        0,0,2, 2));
        style.font = font;
        style.padHeight = 30;

        graySkin.add("default", style);
    }

    private void addRdDialogBuilderStyle() {
        var style = new RdDialogBuilder.RdDialogBuilderStyle();

        style.textStyle = graySkin.get(RdLabel.RdLabelStyle.class);
        style.rdDialogStyle = graySkin.get(RdDialog.RdDialogStyle.class);
        style.cancelStyle = graySkin.get(RdImageTextButton.RdImageTextButtonStyle.class);
        style.acceptStyle = graySkin.get("blue", RdImageTextButton.RdImageTextButtonStyle.class);
        style.scrollStyle = graySkin.get(RdScrollPane.RdScrollPaneStyle.class);

        graySkin.add("default", style);
    }

    private void addInputDialogBuilderStyle() {
        var style = new RdDialogBuilder.RdDialogBuilderStyle();

        style.textStyle = graySkin.get(RdLabel.RdLabelStyle.class);
        style.rdDialogStyle = graySkin.get("input", RdDialog.RdDialogStyle.class);
        style.cancelStyle = graySkin.get(RdImageTextButton.RdImageTextButtonStyle.class);
        style.acceptStyle = graySkin.get("blue", RdImageTextButton.RdImageTextButtonStyle.class);
        style.scrollStyle = graySkin.get(RdScrollPane.RdScrollPaneStyle.class);

        graySkin.add("input", style);
    }

    private void addFileSelectorStyle() {
        var filePath = new RdLabel.RdLabelStyle();

        lightGray = TextureUtil.create(10, 10, Color.LIGHT_GRAY);
        filePath.font = font;
        filePath.color = Color.BLACK;
        filePath.background = new TextureRegionDrawable(lightGray);

        var style = new FileSelectorBuilder.FileSelectorStyle();

        style.dialogBuilderStyle =
                new RdDialogBuilder.RdDialogBuilderStyle(
                        new RdDialogBuilder.RdDialogBuilderStyle(graySkin.get("input",
                                RdDialogBuilder.RdDialogBuilderStyle.class)));
        style.dialogBuilderStyle.rdDialogStyle.padBottomC = 0;
        style.dialogBuilderStyle.scrollStyle = new ScrollPane.ScrollPaneStyle(style.dialogBuilderStyle.scrollStyle);
        style.dialogBuilderStyle.scrollStyle.background = new NinePatchDrawable(
                new NinePatch(findRegion("list_bg"),
                        4,4,4, 4));

        style.rdListStyle = graySkin.get(RdList.RdListStyle.class);
        style.file = new TextureRegionDrawable(findRegion("iw_file"));
        style.folder = new TextureRegionDrawable(findRegion("iw_folder"));
        style.back = new TextureRegionDrawable(findRegion("iw_reply"));
        style.filePath = filePath;


        graySkin.add("default", style);
    }

    private void addRdSelectBox() {
        var style = new RdSelectBox.RdSelectBoxStyle();

        style.background = new NinePatchDrawable(
                new NinePatch(findRegion("dropbox"),
                        12, 66, 12, 18));
        style.backgroundOver = new NinePatchDrawable(
                new NinePatch(findRegion("dropbox_over"),
                        12, 66, 12, 18));
        style.backgroundOpen = new NinePatchDrawable(
                new NinePatch(findRegion("dropbox_open"),
                        12, 66, 12, 18));

        style.image = new TextureRegionDrawable(findRegion("image"));
        style.imageOver = new TextureRegionDrawable(findRegion("image"));
        style.imageOpen = new TextureRegionDrawable(findRegion("image_open"));
        style.listStyle = new RdList.RdListStyle(graySkin.get(RdList.RdListStyle.class));

        style.scrollStyle = new RdScrollPane.RdScrollPaneStyle(
                graySkin.get(RdScrollPane.RdScrollPaneStyle.class));
        style.scrollStyle.background = new NinePatchDrawable(
                new NinePatch(findRegion("list_bg"),
                        4,4,4, 4));
        style.font = font;
        style.fontColor = Color.WHITE;

        graySkin.add("default", style);
    }

    private void addRdScrollStyle() {
        var style = new RdScrollPane.RdScrollPaneStyle();

        style.hScroll = new NinePatchDrawable(
                new NinePatch(
                        findRegion("line_h"),
                        8, 9, 8, 9));
        style.vScroll = new NinePatchDrawable(
                new NinePatch(
                        findRegion("line_v"),
                        10, 11, 9, 10));
        style.hScrollKnob = new NinePatchDrawable(
                new NinePatch(findRegion("knob_h"),
                        21, 21, 0, 0));
        style.vScrollKnob = new NinePatchDrawable(
                new NinePatch(findRegion("knob_v"),
                        0, 0,
                        21,21));
        style.cursor = scrollCursor;
        style.fadeScrollBars = false;
        style.overscrollY = false;
        style.overscrollX = false;

        graySkin.add("default", style);
    }

    private void addSampleRdScroll() {
        var style = new RdScrollPane.RdScrollPaneStyle();

        style.cursor = scrollCursor;
        style.fadeScrollBars = false;
        style.overscrollY = false;
        style.overscrollX = false;

        graySkin.add("sample", style);
    }

    private void addRdTextFieldStyle() {
        var style = new RdTextField.RdTextFieldStyle();

        redTexture = TextureUtil.create(10, 10, Color.RED);
        style.selection = new TextureRegionDrawable(redTexture);
        style.cursor = new TextureRegionDrawable(findRegion("cursor"));
        style.background = new NinePatchDrawable(
                new NinePatch(findRegion("text_field"),
                        17, 17,
                        17, 17));
        style.overBackground = new NinePatchDrawable(
                new NinePatch(findRegion("text_field_over"),
                        17, 17,
                        17, 17));
        style.focusedBackground = new NinePatchDrawable(
                new NinePatch(findRegion("text_field_focused"),
                17, 17,
                17, 17));

        style.font = font;
        style.fontColor = Color.WHITE;

        graySkin.add("default", style);
    }

    private void addRdTextTooltipStyle() {
        var style = new RdTextTooltip.RdTextTooltipStyle();
        style.labelStyle = graySkin.get(RdLabel.RdLabelStyle.class);
        style.background = new NinePatchDrawable(
                new NinePatch(findRegion("tooltip_bg"),
                        10, 10,
                        10, 10));
        style.wrapWidth = 400;
        style.alwaysTop = true;

        graySkin.add("default", style);
    }

    private void addLoadingRdTable() {
        var style = new RdTable.RdTableStyle();
        style.loadingBg = loadingBg;
        style.loadingAnim = loadingAnim;

        graySkin.add("loading", style);
    }

    private void addCircleButton() {
        var style = new RdImageTextButton.RdImageTextButtonStyle();

        style.up = new TextureRegionDrawable(findRegion("circle_up"));
        style.over = new TextureRegionDrawable(findRegion("circle_over"));
        style.down = new TextureRegionDrawable(findRegion("circle_down"));
        style.font = font;
        style.fontColor = Color.WHITE;

        graySkin.add("circle", style);
    }

    private void addWidowGroupStyle() {
        var style = new WindowGroup.WindowGroupStyle();

        style.padLeft = 15;
        style.padRight = 15;
        style.padBottom = 30;
        style.padTop = 100;
        style.buttonMinWidth = 300;
        style.windowMinWidth = 1200;

        graySkin.add("default", style);
    }
}
