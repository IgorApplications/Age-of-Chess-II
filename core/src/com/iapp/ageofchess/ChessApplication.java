package com.iapp.ageofchess;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.github.tommyettinger.textra.Font;
import com.iapp.ageofchess.activity.MenuActivity;
import com.iapp.ageofchess.controllers.multiplayer.AccountController;
import com.iapp.ageofchess.modding.LoaderMap;
import com.iapp.ageofchess.multiplayer.MultiplayerEngine;
import com.iapp.ageofchess.services.*;
import com.iapp.lib.ui.actors.*;
import com.iapp.lib.ui.screens.*;
import com.iapp.lib.ui.widgets.AccountPanel;
import com.iapp.lib.util.OnChangeListener;
import com.iapp.lib.util.Pair;
import com.iapp.lib.util.RdI18NBundle;
import com.iapp.lib.util.WindowUtil;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ChessApplication extends RdApplication {

	private final ServerMode serverMode;
	private final LocalFeatures localFeatures;
    private final ApplicationMode appMode;
    /** local copy from RdApplication */
	private RdI18NBundle strings;

	private final List<Locale> countryLocales = new ArrayList<>();
	private final List<String> countries = new ArrayList<>();
    private LoggingView loggingView;
    private RdTable lineContent;
    private boolean initialize;

	public ChessApplication(Launcher launcher, ServerMode serverMode,
                            ApplicationMode appMode, LocalFeatures localFeatures) {
		super(launcher, ChessConstants.WORLD_WIDTH, ChessConstants.WORLD_HEIGHT,
				new ChessAssetManager(), 3);
		this.serverMode = serverMode;
		this.localFeatures = localFeatures;
        this.appMode = appMode;
	}

	public ChessApplication(Launcher launcher) {
		this(launcher, ServerMode.SERVER, ApplicationMode.RELEASE, LocalFeatures.USER);
	}

    public RdTable getLineContent() {
        return lineContent;
    }

    public LoggingView getLoggingView() {
        return loggingView;
    }

	public List<Locale> getCountryLocales() {
		return countryLocales;
	}

	public List<String> getCountries() {
		return countries;
	}

	public static ChessApplication self() {
		return (ChessApplication) RdApplication.self();
	}

	@Override
	public ChessAssetManager getAssetManager() {
		return (ChessAssetManager) super.getAssetManager();
	}

    public LocalFeatures getLocalFeatures() {
        return localFeatures;
    }

	public ServerMode getLaunchMode() {
		return serverMode;
	}

	public LocalFeatures getCheats() {
		return localFeatures;
	}

	@Override
	public void launch(RdAssetManager rdAssetManager) {
		var style = new RdLabel.RdLabelStyle();
		style.font = new Font("gray_style/fonts/itxt_lite.fnt", Font.DistanceFieldType.SDF);
		style.color = Color.WHITE;
		RdLogger.self().setLogStyle(style);
		RdLogger.self().setDescStyle(style);
		initConstants();

        Sound sound = Gdx.audio.newSound(Gdx.files.internal("sounds/splash.mp3"));
        sound.play();

		SplashActivity.loadLibrary(MenuActivity::new,
            "gray_style/textures/logo.png", "gray_style/textures/title_logo.png",
            LoaderMap.self().getTaskLoadDiskMaps(() -> ChessApplication.self().initialize()));

        if (serverMode == ServerMode.SERVER) {
            ChessConstants.serverAPI = "ws://185.104.248.176:8082/ws";
        } else {
            ChessConstants.serverAPI = "ws://localhost:8082/ws";
        }

        if (appMode == ApplicationMode.RELEASE) {
            RdApplication.self().setLogHandle(ChessConstants.FILE_TYPE, ChessConstants.LOGS_DIRECTORY);
        }

		Gdx.app.log("Launch Mode", String.valueOf(serverMode));
        MultiplayerEngine.self().launchMultiplayerEngine();
	}

	public void updateTitle(WindowGroup group, String text) {
        // TODO
    }

	@Override
	public void renderApp() {
		super.renderApp();
	}

	@Override
	public void resizeApp(int width, int height) {
		super.resizeApp(width, height);
	}

	@Override
	public void pauseApp() {
		super.pauseApp();
		DataManager.self().saveLocalData(ChessConstants.localData);
	}

	@Override
	public void dispose() {
		if (ChessConstants.localData.isSaveWindowSize()) {
			ChessConstants.localData.setWindowSize(
					new Pair<>(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
		}
		DataManager.self().saveLocalData(ChessConstants.localData);
		super.dispose();
        System.exit(0);
	}

	public void initialize() {
		RdApplication.self().setBackgroundColor(Color.BLACK);
		RdApplication.self().getLauncher().setOnFinish(null);

        // sounds volume
        Sounds.self().setVolumeEffects(ChessConstants.localData.getEffectsVolume());
        Sounds.self().setVolumeMusic(ChessConstants.localData.getMusicVolume());
        loadStrings();

		RdApplication.self().setFps(ChessConstants.localData.getFps().getValue());
		if (ChessConstants.localData.isSaveWindowSize()) {
			var windowSize = ChessConstants.localData.getWindowSize();
			Gdx.graphics.setWindowedMode(windowSize.getKey(), windowSize.getValue());
		}
		if (ChessConstants.localData.isEnableBackgroundMusic()) {
			Sounds.self().startBackgroundMusic();
		}

		if (ChessConstants.localData.isFullScreen()) Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
		OnChangeListener.setButtonClick(() -> Sounds.self().playLock());

        // single initialize!
		if (!initialize) {
            initialize = true;

            ChessConstants.accountPanel = new AccountPanel();
            ChessConstants.accountPanel.setVisible(false);
            ChessConstants.accountController = new AccountController();

			Runnable task = () -> {

				for (String country : Locale.getISOCountries()) {
					Locale locale = new Locale("en", country);
					countryLocales.add(locale);
					countries.add(country);
				}
			};
			execute(task);

            loggingView = new LoggingView(ChessAssetManager.current().getSkin());
            loggingView.setVisible(ChessConstants.localData.isEnableSysProperties());

            RdTable table = new RdTable();
            table.setTouchable(Touchable.disabled);
            table.setFillParent(true);
            table.add(loggingView).expand().fill().align(Align.topLeft);
            RdApplication.self().getTopActors().add(table);

            lineContent = new RdTable();
            lineContent.add(new Image(new NinePatchDrawable(
                new NinePatch(ChessAssetManager.current().findChessRegion("mode_app"),
                    300, 10, 0, 10)))).expand().fillX().align(Align.topLeft);
            lineContent.setFillParent(true);
            RdApplication.self().getTopActors().add(0, lineContent);
		}

        loggingView.setVisible(ChessConstants.localData.isEnableSysProperties());
	}

    private void loadStrings() {
        Files.FileType fileType = ChessConstants.FILE_TYPE;
        if (!Gdx.files.getFileHandle("languages/lang_" +
                ChessConstants.localData.getLangCode(), fileType).exists()) {
            fileType = Files.FileType.Internal;
        }

        FileHandle baseFileHandle = Gdx.files.getFileHandle("languages/lang", fileType);
        RdApplication.self().setStrings(RdI18NBundle.createBundle(baseFileHandle,
            new Locale(ChessConstants.localData.getLangCode())));
        strings = RdApplication.self().getStrings();
    }

    /** default dialogs */

    public Spinner showSpinner(String text) {
        Spinner spinner = new Spinner(text);
        spinner.show(RdApplication.self().getStage());
        spinner.setSize(400, 100);
        RdApplication.self().addDialog(spinner, WindowUtil::resizeCenter);
        return spinner;
    }

	public void showAccept(String text) {
		var accept = new RdDialogBuilder()
				.title(strings.get("[i18n]Done"))
				.accept(strings.get("[i18n]accept"))
				.text(text)
				.build("input");

		accept.getIcon().setDrawable(new TextureRegionDrawable(
				ChessAssetManager.current().findRegion("icon_accept")));
		accept.getIcon().setScaling(Scaling.fit);

		accept.show(RdApplication.self().getStage());
		accept.setSize(800, 550);
		RdApplication.self().addDialog(accept, WindowUtil::resizeCenter);
	}

    public void showInput(String text, BiConsumer<RdDialog, String> onAccept) {
        showInput(text, strings.get("[i18n]accept"), onAccept);
    }

    public void showSelector(Consumer<FileHandle> onSelect, String... extensions) {
        RdDialog selector = new FileSelectorBuilder()
            .title(strings.get("[i18n]File selector"))
            .endFilters(extensions)
            .select(strings.get("[i18n]Select"), onSelect)
            .cancel(strings.get("[i18n]reject"))
            .build(ChessAssetManager.current().getSkin());

        selector.show(RdApplication.self().getStage());
        selector.setSize(900, 800);
        RdApplication.self().addDialog(selector, WindowUtil::resizeCenter);
    }

    public void showInput(String text, String accept, BiConsumer<RdDialog, String> onAccept) {
        RdDialog conf = new RdDialogBuilder()
            .title(strings.get("input"))
            .cancel(strings.get("[i18n]reject"))
            .accept(accept, onAccept)
            .text(text)
            .input(strings.get("[i18n]enter..."))
            .build("input");

        conf.getIcon().setDrawable(new TextureRegionDrawable(
            ChessAssetManager.current().findRegion("icon_info")));
        conf.getIcon().setScaling(Scaling.fit);

        conf.show(RdApplication.self().getStage());
        conf.setSize(800, 550);
        RdApplication.self().addDialog(conf, WindowUtil::resizeCenter);
    }

	public void showConf(String text, BiConsumer<RdDialog, String> onAccept) {
		showConf(strings.get("[i18n]accept"), text, onAccept, null);
	}

    public void showConf(String accept, String text,
                         BiConsumer<RdDialog, String> onAccept) {
        showConf(accept, text, onAccept, null);
    }

    public void showConf(String accept, String text,
                         BiConsumer<RdDialog, String> onAccept,
                         BiConsumer<RdDialog, String> onCancel) {
        RdDialog conf = new RdDialogBuilder()
            .title(strings.get("[i18n]Information"))
            .cancel(strings.get("[i18n]reject"), onCancel)
            .accept(accept, onAccept)
            .text(text)
            .build("input");

        conf.getIcon().setDrawable(new TextureRegionDrawable(
            ChessAssetManager.current().findRegion("icon_conf")));
        conf.getIcon().setScaling(Scaling.fit);

        conf.show(RdApplication.self().getStage());
        conf.setSize(800, 550);
        RdApplication.self().addDialog(conf, WindowUtil::resizeCenter);
    }

    public void showConfWarn(String text, BiConsumer<RdDialog, String> onAccept) {
        showConfWarn(strings.get("[i18n]accept"), text, onAccept);
    }

    public void showConfWarn(String accept, String text, BiConsumer<RdDialog, String> onAccept) {
        RdDialog conf = new RdDialogBuilder()
            .title(strings.get("[i18n]Information"))
            .cancel(strings.get("[i18n]reject"))
            .accept(accept, onAccept)
            .text(text)
            .build("input");

        conf.getIcon().setDrawable(new TextureRegionDrawable(
            ChessAssetManager.current().findRegion("icon_warn")));
        conf.getIcon().setScaling(Scaling.fit);

        conf.show(RdApplication.self().getStage());
        conf.setSize(800, 550);
        RdApplication.self().addDialog(conf, WindowUtil::resizeCenter);
    }

	public void showInfo(String text) {
		RdDialog info = new RdDialogBuilder()
				.title(strings.get("[i18n]Information"))
				.accept(strings.get("[i18n]accept"))
				.text(text)
				.build("input");

		info.getIcon().setDrawable(new TextureRegionDrawable(
				ChessAssetManager.current().findRegion("icon_info")));
		info.getIcon().setScaling(Scaling.fit);

		info.show(RdApplication.self().getStage());
		info.setSize(800, 550);
		RdApplication.self().addDialog(info, WindowUtil::resizeCenter);
	}

	private int errors = 0;

	public void showError(String text) {
		if (errors > 0) return;
		errors++;

        BiConsumer<RdDialog, String> onCancel = (dialog, s) -> {
            dialog.hide();
            errors = 0;
        };

		RdDialog error = new RdDialogBuilder()
				.title(strings.get("[i18n]Error"))
				.accept(strings.get("[i18n]accept"), onCancel)
                .onHide(onCancel)
				.text(text)
				.build("input");

		error.getIcon().setDrawable(new TextureRegionDrawable(
				ChessAssetManager.current().findRegion("icon_error")));
		error.getIcon().setScaling(Scaling.fit);

		error.show(RdApplication.self().getStage());
		error.setSize(800, 550);
		RdApplication.self().addDialog(error, WindowUtil::resizeCenter);
        Sounds.self().playShowError();
	}

	private void initConstants() {
		ChessConstants.localData = DataManager.self().readLocalData();
		RdLogger.self().setOnFatal(() -> ChessApplication.self().getLauncher().setOnFinish(null));
	}
}
