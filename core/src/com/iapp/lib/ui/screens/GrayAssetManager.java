package com.iapp.lib.ui.screens;

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
import com.iapp.lib.ui.actors.*;
import com.iapp.lib.util.DisposeUtil;
import com.iapp.lib.util.StreamUtil;
import com.iapp.lib.util.TextureUtil;

/**
 * Standard gray style loader
 * @author Igor Ivanov
 * @version 1.0
 * */
public class GrayAssetManager extends RdAssetManager {

    private static final AssetDescriptor<TextureAtlas> GRAY_ATLAS_DESC =
            new AssetDescriptor<>("gray_style/atlases/gray_skin.atlas", TextureAtlas.class);

    private Skin graySkin;
    private TextureAtlas grayAtlas;
    private Texture grayLine, lightGray, redTexture;
    private Cursor defCursor, spinnerCursor, scrollCursor;
    private Font font;

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
    }

    @Override
    protected void initialize(AssetManager assetManager) {
        grayAtlas = assetManager.get(GRAY_ATLAS_DESC);
        StreamUtil.streamOf(grayAtlas.getTextures()).forEach(texture ->
                texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear));

        font =  new Font("gray_style/fonts/itxt.fnt", Font.DistanceFieldType.SDF);
        font.setCrispness(3);
        graySkin = new Skin();
        graySkin.add("default", font);

        initCursor();

        addRdLabelStyle();

        addLoadingStyle();

        // "color_screen"
        addScreenButton("white", Color.WHITE);
        addScreenButton("yellow", Color.YELLOW);
        addScreenButton("blue", Color.valueOf("0094FF"));
        addScreenButton("red", Color.RED);

        addRdTextButtonStyle("default", "", Color.WHITE, new RdImageTextButton.RdImageTextButtonStyle());
        addRdTextButtonStyle("blue", "blue_", Color.BLACK, new RdImageTextButton.RdImageTextButtonStyle());
        addRdTextButtonStyle("dark", "dark_", Color.WHITE, new RdImageTextButton.RdImageTextButtonStyle());

        addRdTextButtonStyle("default", "", Color.WHITE, new RdTextButton.RdTextButtonStyle());
        addRdTextButtonStyle("blue", "blue_", Color.BLACK, new RdTextButton.RdTextButtonStyle());
        addRdTextButtonStyle("dark", "dark_", Color.WHITE, new RdTextButton.RdTextButtonStyle());

        addImageButtonStyle("default", "", Color.WHITE);
        addImageButtonStyle("blue", "blue_", Color.BLACK);
        addImageButtonStyle("dark", "dark_", Color.WHITE);

        addRdListStyle();

        addSampleRdScroll();
        addRdScrollStyle();

        addCheckBox();

        addInputDialog();

        addRdDialogStyle();

        addRdTextFieldStyle();

        addSelectionButton();

        addLoadingRdTable();

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

        addCircleButton();

        addWidowGroupStyle();

        RdApplication.self().setCursor(defCursor);
    }

    @Override
    protected TextureAtlas getAtlas() {
        return grayAtlas;
    }

    // ---------------------------------------------------------------------------------------------------------


    private void initCursor() {
        Pixmap pixCursor = new Pixmap(Gdx.files.internal("gray_style/textures/cursor.png"));
        Pixmap pixScroll = new Pixmap(Gdx.files.internal("gray_style/textures/scroll_cursor.png"));
        Pixmap pixSpinner = new Pixmap(Gdx.files.internal("gray_style/textures/spinner_cursor.png"));

        defCursor = Gdx.graphics.newCursor(pixCursor, 0, 0);
        scrollCursor = Gdx.graphics.newCursor(pixScroll, 0, 0);
        spinnerCursor = Gdx.graphics.newCursor(pixSpinner, 0, 0);

        pixCursor.dispose();
        pixScroll.dispose();
        pixSpinner.dispose();
    }

    private void addRdLabelStyle() {
        RdLabel.RdLabelStyle style = new RdLabel.RdLabelStyle();

        style.font = font;
        style.color = Color.WHITE;

        graySkin.add("default", style);
    }

    public void addLoadingStyle() {
        AnimatedImage animatedImage = new AnimatedImage(30,
            new TextureRegionDrawable(current().findRegion("frame-1")),
            new TextureRegionDrawable(current().findRegion("frame-2")),
            new TextureRegionDrawable(current().findRegion("frame-3")),
            new TextureRegionDrawable(current().findRegion("frame-4")),
            new TextureRegionDrawable(current().findRegion("frame-5")),
            new TextureRegionDrawable(current().findRegion("frame-6")),
            new TextureRegionDrawable(current().findRegion("frame-7")),
            new TextureRegionDrawable(current().findRegion("frame-8")),
            new TextureRegionDrawable(current().findRegion("frame-9")),
            new TextureRegionDrawable(current().findRegion("frame-10")),
            new TextureRegionDrawable(current().findRegion("frame-11")),
            new TextureRegionDrawable(current().findRegion("frame-12")),
            new TextureRegionDrawable(current().findRegion("frame-13")),
            new TextureRegionDrawable(current().findRegion("frame-14")),
            new TextureRegionDrawable(current().findRegion("frame-15")),
            new TextureRegionDrawable(current().findRegion("frame-16")),
            new TextureRegionDrawable(current().findRegion("frame-17")),
            new TextureRegionDrawable(current().findRegion("frame-18")),
            new TextureRegionDrawable(current().findRegion("frame-19")),
            new TextureRegionDrawable(current().findRegion("frame-20")),
            new TextureRegionDrawable(current().findRegion("frame-21")),
            new TextureRegionDrawable(current().findRegion("frame-22")),
            new TextureRegionDrawable(current().findRegion("frame-23")),
            new TextureRegionDrawable(current().findRegion("frame-24")),
            new TextureRegionDrawable(current().findRegion("frame-25")),
            new TextureRegionDrawable(current().findRegion("frame-26")),
            new TextureRegionDrawable(current().findRegion("frame-27")),
            new TextureRegionDrawable(current().findRegion("frame-28")),
            new TextureRegionDrawable(current().findRegion("frame-29")),
            new TextureRegionDrawable(current().findRegion("frame-30"))
        );
        graySkin.add("logo_anim", animatedImage);

        NinePatchDrawable loadingBg = new NinePatchDrawable(
            new NinePatch(current().findRegion("load_bg"),
                9, 9, 9, 9));
        AnimatedImage loadingAnim = new AnimatedImage(150,
            new TextureRegionDrawable(current().findRegion("load_logo", 1)),
            new TextureRegionDrawable(current().findRegion("load_logo", 2)),
            new TextureRegionDrawable(current().findRegion("load_logo", 3)),
            new TextureRegionDrawable(current().findRegion("load_logo", 4)),
            new TextureRegionDrawable(current().findRegion("load_logo", 5)),
            new TextureRegionDrawable(current().findRegion("load_logo", 6))
        );

        LoadingTable.LoadingStyle loadingStyle = new LoadingTable.LoadingStyle();

        loadingStyle.loadingText = getSkin().get(RdLabel.RdLabelStyle.class);
        loadingStyle.loadingAnim = loadingAnim;
        loadingStyle.loadingBg = loadingBg;

        graySkin.add("default", loadingStyle);
    }

    private void addLineTableStyle() {
        LineTable.LineTableStyle style = new LineTable.LineTableStyle();

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
        LoggingView.LoggingViewStyle style = new LoggingView.LoggingViewStyle();

        style.font = font;
        style.color = Color.WHITE;
        style.colorFPS = Color.GREEN;
        style.colorRAM = Color.YELLOW;

        graySkin.add("default", style);
    }

    private void addRdTextButtonStyle(String name, String type, Color color, RdTextButton.RdTextButtonStyle style) {
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

    private void addImageButtonStyle(String name, String type, Color color) {
        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();

        style.up = new NinePatchDrawable(
            new NinePatch(findRegion(type + "button_up"),
                13, 15, 16, 19));
        style.over = new NinePatchDrawable(
            new NinePatch(findRegion(type + "button_over"),
                13, 15, 16, 19));
        style.down = new NinePatchDrawable(
            new NinePatch(findRegion(type + "button_down"),
                13, 15, 19, 16));

        graySkin.add(name, style);
    }

    private void addScreenButton(String color, Color textColor) {
        RdImageTextButton.RdImageTextButtonStyle style = new RdImageTextButton.RdImageTextButtonStyle();

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
        RdSelectionButton.RdSelectionButtonStyle style = new RdSelectionButton.RdSelectionButtonStyle();

        RdImageTextButton.RdImageTextButtonStyle left = new RdImageTextButton.RdImageTextButtonStyle();
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

        RdImageTextButton.RdImageTextButtonStyle center = new RdImageTextButton.RdImageTextButtonStyle();
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

        RdImageTextButton.RdImageTextButtonStyle right = new RdImageTextButton.RdImageTextButtonStyle();
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
        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();

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

        TextureAtlas.AtlasRegion on = findRegion("icon_on");
        TextureAtlas.AtlasRegion off = findRegion("icon_off");
        style.imageChecked = new TextureRegionDrawable(on);
        style.imageUp = new TextureRegionDrawable(off);

        graySkin.add("check_box", style);
    }

    private void addPropertyTableStyle() {
        PropertyTable.PropertyTableStyle style = new PropertyTable.PropertyTableStyle();

        grayLine = TextureUtil.create(10, 10, new Color(Color.rgba8888(0, 0, 0, 0.3f)));
        style.scrollStyle = graySkin.get(RdScrollPane.RdScrollPaneStyle.class);
        style.panel = new TextureRegionDrawable(grayLine);
        style.titleStyle = new RdLabel.RdLabelStyle(font, Color.GOLD);
        style.elementStyle = new RdLabel.RdLabelStyle(font, Color.WHITE);
        style.contentStyle = getSkin().get("loading", RdTable.RdTableStyle.class);

        graySkin.add("default", style);
    }

    private void addScreenWindowStyle() {
        RdWindow.RdWindowStyle style = new RdWindow.RdWindowStyle();

        style.titleFont = font;
        style.background = new NinePatchDrawable(
                new NinePatch(findRegion("window"),
                        10, 10, 10, 10));
        style.loadingStyle = new LoadingTable.LoadingStyle(
            getSkin().get(LoadingTable.LoadingStyle.class));

        graySkin.add("screen_window", style);
    }

    private void addInputDialog() {
        RdDialog.RdDialogStyle style = new RdDialog.RdDialogStyle();

        ImageButton.ImageButtonStyle closeBoxStyle = new ImageButton.ImageButtonStyle();
        closeBoxStyle.up = new TextureRegionDrawable(findRegion("cancel_up"));
        closeBoxStyle.down = new TextureRegionDrawable(findRegion("cancel_down"));
        closeBoxStyle.over  = new TextureRegionDrawable(findRegion("cancel_over"));

        style.closeBoxStyle = closeBoxStyle;
        style.titleFont = font;
        style.titleFontColor = Color.BLACK;
        style.background = new TwoNinePath(
                new NinePatchDrawable(new NinePatch(
                        current().findRegion("input_dialog1"),
                        19, 200,92, 0)),
                new NinePatchDrawable(new NinePatch(
                        current().findRegion("input_dialog2"),
                        19, 200,2, 20))
        );
        style.loadingStyle = new LoadingTable.LoadingStyle(
            getSkin().get(LoadingTable.LoadingStyle.class));

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
        RdDialog.RdDialogStyle style = new RdDialog.RdDialogStyle();

        ImageButton.ImageButtonStyle closeBoxStyle = new ImageButton.ImageButtonStyle();
        closeBoxStyle.up = new TextureRegionDrawable(findRegion("cancel_up"));
        closeBoxStyle.down = new TextureRegionDrawable(findRegion("cancel_down"));
        closeBoxStyle.over  = new TextureRegionDrawable(findRegion("cancel_over"));

        style.closeBoxStyle = closeBoxStyle;
        style.titleFont = font;
        style.titleFontColor = Color.BLACK;
        style.background = new NinePatchDrawable(
                new NinePatch(findRegion("dialog"),
                        19, 200,92, 20));
        style.loadingStyle = new LoadingTable.LoadingStyle(
            getSkin().get(LoadingTable.LoadingStyle.class));

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
        Spinner.SpinnerStyle style = new Spinner.SpinnerStyle();

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
        RdList.RdListStyle style = new RdList.RdListStyle();

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
        RdDialogBuilder.RdDialogBuilderStyle style = new RdDialogBuilder.RdDialogBuilderStyle();

        style.textStyle = graySkin.get(RdLabel.RdLabelStyle.class);
        style.rdDialogStyle = graySkin.get(RdDialog.RdDialogStyle.class);
        style.cancelStyle = graySkin.get(RdImageTextButton.RdImageTextButtonStyle.class);
        style.acceptStyle = graySkin.get("blue", RdImageTextButton.RdImageTextButtonStyle.class);
        style.scrollStyle = graySkin.get(RdScrollPane.RdScrollPaneStyle.class);
        style.textFieldStyle = graySkin.get(RdTextField.RdTextFieldStyle.class);

        graySkin.add("default", style);
    }

    private void addInputDialogBuilderStyle() {
        RdDialogBuilder.RdDialogBuilderStyle style = new RdDialogBuilder.RdDialogBuilderStyle();

        style.textStyle = graySkin.get(RdLabel.RdLabelStyle.class);
        style.rdDialogStyle = graySkin.get("input", RdDialog.RdDialogStyle.class);
        style.cancelStyle = graySkin.get(RdImageTextButton.RdImageTextButtonStyle.class);
        style.acceptStyle = graySkin.get("blue", RdImageTextButton.RdImageTextButtonStyle.class);
        style.scrollStyle = graySkin.get(RdScrollPane.RdScrollPaneStyle.class);
        style.textFieldStyle = graySkin.get(RdTextField.RdTextFieldStyle.class);

        graySkin.add("input", style);
    }

    private void addFileSelectorStyle() {
        RdLabel.RdLabelStyle filePath = new RdLabel.RdLabelStyle();

        lightGray = TextureUtil.create(10, 10, Color.LIGHT_GRAY);
        filePath.font = font;
        filePath.color = Color.BLACK;
        filePath.background = new TextureRegionDrawable(lightGray);

        FileSelectorBuilder.FileSelectorStyle style = new FileSelectorBuilder.FileSelectorStyle();

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
        RdSelectBox.RdSelectBoxStyle style = new RdSelectBox.RdSelectBoxStyle();

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
        RdScrollPane.RdScrollPaneStyle style = new RdScrollPane.RdScrollPaneStyle();

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
        style.loadingStyle = new LoadingTable.LoadingStyle(
            getSkin().get(LoadingTable.LoadingStyle.class));

        graySkin.add("default", style);
    }

    private void addSampleRdScroll() {
        RdScrollPane.RdScrollPaneStyle style = new RdScrollPane.RdScrollPaneStyle();

        style.cursor = scrollCursor;
        style.fadeScrollBars = false;
        style.overscrollY = false;
        style.overscrollX = false;

        graySkin.add("sample", style);
    }

    private void addRdTextFieldStyle() {
        RdTextField.RdTextFieldStyle style = new RdTextField.RdTextFieldStyle();

        redTexture = TextureUtil.create(10, 10, Color.RED);
        style.selection = new TextureRegionDrawable(redTexture);
        style.cursor = new TextureRegionDrawable(findRegion("cursor"));
        style.background = new NinePatchDrawable(
                new NinePatch(findRegion("text_field"),
                        24, 24,
                        17, 17));
        style.overBackground = new NinePatchDrawable(
                new NinePatch(findRegion("text_field_over"),
                        24, 24,
                        17, 17));
        style.focusedBackground = new NinePatchDrawable(
                new NinePatch(findRegion("text_field_focused"),
                24, 24,
                17, 17));

        style.font = font;
        style.fontColor = Color.WHITE;

        graySkin.add("default", style);
    }

    private void addRdTextTooltipStyle() {
        RdTextTooltip.RdTextTooltipStyle style = new RdTextTooltip.RdTextTooltipStyle();
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
        RdTable.RdTableStyle style = new RdTable.RdTableStyle();
        style.loadingStyle = new LoadingTable.LoadingStyle(
            getSkin().get(LoadingTable.LoadingStyle.class));

        graySkin.add("loading", style);
    }

    private void addCircleButton() {
        RdImageTextButton.RdImageTextButtonStyle style = new RdImageTextButton.RdImageTextButtonStyle();

        style.up = new TextureRegionDrawable(findRegion("circle_up"));
        style.over = new TextureRegionDrawable(findRegion("circle_over"));
        style.down = new TextureRegionDrawable(findRegion("circle_down"));
        style.font = font;
        style.fontColor = Color.WHITE;

        graySkin.add("circle", style);
    }

    private void addWidowGroupStyle() {
        WindowGroup.WindowGroupStyle style = new WindowGroup.WindowGroupStyle();

        style.padLeft = 15;
        style.padRight = 15;
        style.padBottom = 90;
        style.padTop = 128;
        style.buttonMinWidth = 300;
        style.windowMinWidth = 1200;

        graySkin.add("default", style);
    }
}
