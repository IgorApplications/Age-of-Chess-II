package com.iapp.ageofchess.util;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.github.tommyettinger.textra.Font;
import com.iapp.ageofchess.modding.MapData;
import com.iapp.rodsher.actors.RdDialog;
import com.iapp.rodsher.actors.RdImageTextButton;
import com.iapp.rodsher.screens.RdAssetManager;
import com.iapp.rodsher.util.CallListener;
import com.iapp.rodsher.util.DisposeUtil;
import com.iapp.rodsher.util.StreamUtil;
import com.iapp.rodsher.util.TextureUtil;

import java.util.ArrayList;
import java.util.List;

public class ChessAssetManager extends GrayAssetManager {

    /** applications styles */
    private static final AssetDescriptor<TextureAtlas> CHESS_ATLAS =
            new AssetDescriptor<>("atlases/chess.atlas", TextureAtlas.class);

    private static final AssetDescriptor<Sound> MOVE_SOUND =
            new AssetDescriptor<>("sounds/move.ogg", Sound.class);

    private static final AssetDescriptor<Sound> CASTLE_SOUND =
            new AssetDescriptor<>("sounds/castle.mp3", Sound.class);

    private static final AssetDescriptor<Sound> CHECK_SOUND =
            new AssetDescriptor<>("sounds/check.mp3", Sound.class);

    private static final AssetDescriptor<Music> WIN_SOUND =
            new AssetDescriptor<>("sounds/win.mp3", Music.class);

    private static final AssetDescriptor<Music> LOSE_SOUND =
            new AssetDescriptor<>("sounds/lose.mp3", Music.class);

    private static final AssetDescriptor<Sound> CLICK_SOUND =
            new AssetDescriptor<>("sounds/click.mp3", Sound.class);

    private static final AssetDescriptor<Sound> BELL_SOUND =
            new AssetDescriptor<>("sounds/bell.mp3", Sound.class);

    private static final AssetDescriptor<Music> BACKGROUND_MUSIC =
            new AssetDescriptor<>("sounds/background_music.mp3", Music.class);

    private TextureAtlas chessAtlas;

    private Sound move, castle, check, bell, click;
    private Music backgroundMusic, win,  lose;

    private Texture grayLine, blackTexture, darkTexture, whiteTexture, overWhiteTexture,
            downWhiteTexture, greenTexture, overGreenTexture, downGreenTexture, transpBlackTexture,
            darkRed, goldTexture, redTexture;
    private Texture levelUp, levelOver, levelDown, closedLevelUp;

    private RdDialog.RdDialogStyle selectionStyle, resultStyle, infoStyle;
    private Button.ButtonStyle cancelStyle;
    private ImageButton.ImageButtonStyle levelStyle, closedLevelStyle,
            selectedStyle, avatarStyle, deleteStyle, hideChatStyle, accountPaneStyle;
    private RdImageTextButton.RdImageTextButtonStyle coinsStyle;

    private CallListener clickListener;

    private final List<MapData> dataMaps = new ArrayList<>();

    public static ChessAssetManager current() {
        return (ChessAssetManager) RdAssetManager.current();
    }

    public TextureAtlas.AtlasRegion findChessRegion(String name) {
        TextureAtlas.AtlasRegion region = chessAtlas.findRegion(name);
        if (region == null) throw new IllegalArgumentException("Region can't' be null! Title of region = " + name);
        return region;
    }

    public List<MapData> getDataMaps() {
        return dataMaps;
    }

    public ImageButton.ImageButtonStyle getHideChatStyle() {
        return hideChatStyle;
    }

    public RdImageTextButton.RdImageTextButtonStyle getCoinsStyle() {
        return coinsStyle;
    }

    public ImageButton.ImageButtonStyle getAccountPaneStyle() {
        return accountPaneStyle;
    }

    public ImageButton.ImageButtonStyle getAvatarStyle() {
        return avatarStyle;
    }

    public ImageButton.ImageButtonStyle getDeleteStyle() {
        return deleteStyle;
    }

    public Texture getGrayLine() {
        return grayLine;
    }

    public Texture getBlackTexture() {
        return blackTexture;
    }

    public Texture getDarkTexture() {
        return darkTexture;
    }

    public Texture getWhiteTexture() {
        return whiteTexture;
    }

    public Texture getOverWhiteTexture() {
        return overWhiteTexture;
    }

    public Texture getDownWhiteTexture() {
        return downWhiteTexture;
    }

    public Texture getGreenTexture() {
        return greenTexture;
    }

    public Texture getOverGreenTexture() {
        return overGreenTexture;
    }

    public Texture getDownGreenTexture() {
        return downGreenTexture;
    }

    public Texture getTranspBlackTexture() {
        return transpBlackTexture;
    }

    public Texture getDarkRed() {
        return darkRed;
    }

    public Texture getGoldTexture() {
        return goldTexture;
    }

    public Texture getRedTexture() {
        return redTexture;
    }

    public Sound getMove() {
        return move;
    }

    public Sound getCastle() {
        return castle;
    }

    public Sound getCheck() {
        return check;
    }

    public Sound getBell() {
        return bell;
    }

    public Music getWin() {
        return win;
    }

    public Music getLose() {
        return lose;
    }

    public Sound getClick() {
        return click;
    }

    public CallListener getClickListener() {
        return clickListener;
    }

    public Music getBackgroundMusic() {
        return backgroundMusic;
    }

    public RdDialog.RdDialogStyle getSelectionStyle() {
        return selectionStyle;
    }

    public RdDialog.RdDialogStyle getResultStyle() {
        return resultStyle;
    }

    public RdDialog.RdDialogStyle getInfoStyle() {
        return infoStyle;
    }

    public Button.ButtonStyle getCancelStyle() {
        return cancelStyle;
    }

    public ImageButton.ImageButtonStyle getLevelStyle() {
        return levelStyle;
    }

    public ImageButton.ImageButtonStyle getClosedLevelStyle() {
        return closedLevelStyle;
    }

    public ImageButton.ImageButtonStyle getSelectedStyle() {
        return selectedStyle;
    }

    @Override
    public void dispose() {
        super.dispose();

        StreamUtil.streamOf(dataMaps)
                .forEach(DisposeUtil::dispose);
        DisposeUtil.dispose(chessAtlas);
        DisposeUtil.dispose(move);
        DisposeUtil.dispose(castle);
        DisposeUtil.dispose(check);
        DisposeUtil.dispose(win);
        DisposeUtil.dispose(lose);
        DisposeUtil.dispose(bell);
        DisposeUtil.dispose(click);
        DisposeUtil.dispose(backgroundMusic);
        DisposeUtil.dispose(grayLine);
        DisposeUtil.dispose(blackTexture);
        DisposeUtil.dispose(darkTexture);
        DisposeUtil.dispose(whiteTexture);
        DisposeUtil.dispose(overWhiteTexture);
        DisposeUtil.dispose(downWhiteTexture);
        DisposeUtil.dispose(greenTexture);
        DisposeUtil.dispose(overGreenTexture);
        DisposeUtil.dispose(downGreenTexture);
        DisposeUtil.dispose(levelUp);
        DisposeUtil.dispose(levelOver);
        DisposeUtil.dispose(levelDown);
        DisposeUtil.dispose(closedLevelUp);
        DisposeUtil.dispose(darkRed);
        DisposeUtil.dispose(goldTexture);
        DisposeUtil.dispose(redTexture);
    }

    @Override
    protected void load(AssetManager assetManager) {
        super.load(assetManager);

        assetManager.load(CHESS_ATLAS);
        assetManager.load(MOVE_SOUND);
        assetManager.load(CASTLE_SOUND);
        assetManager.load(CHECK_SOUND);
        assetManager.load(WIN_SOUND);
        assetManager.load(BACKGROUND_MUSIC);
        assetManager.load(LOSE_SOUND);
        assetManager.load(BELL_SOUND);
        assetManager.load(CLICK_SOUND);
    }

    @Override
    protected void initialize(AssetManager assetManager) {
        super.initialize(assetManager);

        chessAtlas = assetManager.get(CHESS_ATLAS);
        StreamUtil.streamOf(chessAtlas.getTextures()).forEach(texture ->
                texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear));
        move = assetManager.get(MOVE_SOUND);
        castle = assetManager.get(CASTLE_SOUND);
        check = assetManager.get(CHECK_SOUND);
        win = assetManager.get(WIN_SOUND);
        lose = assetManager.get(LOSE_SOUND);
        backgroundMusic = assetManager.get(BACKGROUND_MUSIC);
        bell = assetManager.get(BELL_SOUND);
        click = assetManager.get(CLICK_SOUND);

        grayLine = TextureUtil.create(10, 10, new Color(Color.rgba8888(0, 0, 0, 0.3f)));
        darkTexture = TextureUtil.create(10, 10, new Color(0, 0, 0, 0.3f));
        transpBlackTexture = TextureUtil.create(10, 10, new Color(0, 0, 0, 0.75f));
        blackTexture = TextureUtil.create(10, 10, Color.BLACK);

        whiteTexture = TextureUtil.create(10, 10, new Color(1, 1, 1, 1));
        overWhiteTexture = TextureUtil.create(10, 10, new Color(0.9f, 0.9f, 0.9f, 1));
        downWhiteTexture = TextureUtil.create(10, 10, new Color(0.7f, 0.7f, 0.7f, 1));

        greenTexture = TextureUtil.create(10, 10, new Color(0, 1, 0, 1));
        overGreenTexture = TextureUtil.create(10, 10, new Color(0, 0.9f, 0, 1));
        downGreenTexture = TextureUtil.create(10, 10, new Color(0, 0.7f, 0, 1));

        levelUp = TextureUtil.create(10, 10, new Color(0.1f,0.1f,0.1f,1));
        levelOver = TextureUtil.create(10, 10, new Color(0.2f,0.2f,0.2f,1));
        levelDown = TextureUtil.create(10, 10, new Color(0.3f,0.3f,0.3f,1));
        closedLevelUp = TextureUtil.create(10, 10, new Color(0.2f, 0.2f, 0.2f, 1));
        darkRed = TextureUtil.create(10, 10, new Color(0.1f, 0, 0,1));

        goldTexture = TextureUtil.create(10, 10, Color.GOLD);
        redTexture = TextureUtil.create(10, 10, Color.RED);

        initAppStyles();
        clickListener = () -> Sounds.self().playClick();
    }

    private void initAppStyles() {
        selectionStyle = new RdDialog.RdDialogStyle();
        selectionStyle.background = new TextureRegionDrawable(ChessAssetManager.current().getBlackTexture());
        selectionStyle.titleFont = getSkin().get(Font.class);
        selectionStyle.titleFontColor = Color.WHITE;

        resultStyle = new RdDialog.RdDialogStyle();
        resultStyle.background = new NinePatchDrawable(new NinePatch(findChessRegion("result_dialog"),
                1, 92, 1, 1));
        resultStyle.titleFont = getSkin().get(Font.class);
        resultStyle.titleFontColor = Color.WHITE;

        cancelStyle = new Button.ButtonStyle();
        cancelStyle.up = new TextureRegionDrawable(findChessRegion("cancel_up"));
        cancelStyle.over = new TextureRegionDrawable(findChessRegion("cancel_over"));
        cancelStyle.down = new TextureRegionDrawable(findChessRegion("cancel_down"));

        infoStyle = new RdDialog.RdDialogStyle();
        infoStyle.background = new TextureRegionDrawable(transpBlackTexture);
        infoStyle.titleFont = getSkin().get(Font.class);
        infoStyle.titleFontColor = Color.WHITE;

        levelStyle = new ImageButton.ImageButtonStyle();
        levelStyle.up = new TextureRegionDrawable(levelUp);
        levelStyle.over = new TextureRegionDrawable(levelOver);
        levelStyle.down = new TextureRegionDrawable(levelDown);
        levelStyle.checked = new TextureRegionDrawable(downGreenTexture);

        selectedStyle  = new ImageButton.ImageButtonStyle();
        selectedStyle.up = new TextureRegionDrawable(getWhiteTexture());
        selectedStyle.over = new TextureRegionDrawable(getOverWhiteTexture());
        selectedStyle.down = new TextureRegionDrawable(getDownGreenTexture());
        selectedStyle.checked = new TextureRegionDrawable(getDownGreenTexture());

        closedLevelStyle = new ImageButton.ImageButtonStyle();
        closedLevelStyle.up = new TextureRegionDrawable(closedLevelUp);

        avatarStyle = new ImageButton.ImageButtonStyle();
        avatarStyle.up = new TextureRegionDrawable(findChessRegion("avatar_bg"));
        avatarStyle.over = new TextureRegionDrawable(findChessRegion("avatar_bg_over"));
        avatarStyle.down = new TextureRegionDrawable(findChessRegion("avatar_bg_down"));

        deleteStyle = new ImageButton.ImageButtonStyle();
        deleteStyle.up = new TextureRegionDrawable(findRegion("iw_delete"));
        deleteStyle.over = new TextureRegionDrawable(findRegion("iw_delete_over"));
        deleteStyle.down = new TextureRegionDrawable(findRegion("iw_delete_down"));

        coinsStyle = new RdImageTextButton.RdImageTextButtonStyle();
        coinsStyle.up = new NinePatchDrawable(
                new NinePatch(ChessAssetManager.current().findChessRegion("acc_pane_up"),
                        3, 3, 3, 3));
        coinsStyle.over = new NinePatchDrawable(
                new NinePatch(ChessAssetManager.current().findChessRegion("acc_pane_over"),
                        3, 3, 3, 3));
        coinsStyle.down = new NinePatchDrawable(
                new NinePatch(ChessAssetManager.current().findChessRegion("acc_pane_down"),
                        3, 3, 3, 3));
        coinsStyle.imageUp = new TextureRegionDrawable(
                ChessAssetManager.current().findChessRegion("coin"));
        coinsStyle.imageOver = new TextureRegionDrawable(
                ChessAssetManager.current().findChessRegion("coin_over"));
        coinsStyle.imageDown = new TextureRegionDrawable(
                ChessAssetManager.current().findChessRegion("coin_down"));

        coinsStyle.font = getSkin().get(Font.class);
        coinsStyle.fontColor = Color.WHITE;

        accountPaneStyle = new ImageButton.ImageButtonStyle();
        accountPaneStyle.up = new NinePatchDrawable(
                new NinePatch(ChessAssetManager.current().findChessRegion("acc_pane_up"),
                        3, 3, 3, 3));
        accountPaneStyle.over = new NinePatchDrawable(
                new NinePatch(ChessAssetManager.current().findChessRegion("acc_pane_over"),
                        3, 3, 3, 3));
        accountPaneStyle.down = new NinePatchDrawable(
                new NinePatch(ChessAssetManager.current().findChessRegion("acc_pane_down"),
                        3, 3, 3, 3));

        hideChatStyle = new ImageButton.ImageButtonStyle();
        hideChatStyle.up = new TextureRegionDrawable(ChessAssetManager.current().findChessRegion("hide_bg"));
        hideChatStyle.imageUp = new TextureRegionDrawable(ChessAssetManager.current().findChessRegion("chat_hide_up"));
        hideChatStyle.imageOver = new TextureRegionDrawable(ChessAssetManager.current().findChessRegion("chat_hide_over"));
        hideChatStyle.imageDown = new TextureRegionDrawable(ChessAssetManager.current().findChessRegion("chat_hide_down"));
    }
}
