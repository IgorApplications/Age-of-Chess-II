package com.iapp.ageofchess;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.github.tommyettinger.textra.Font;
import com.iapp.ageofchess.activity.MenuActivity;
import com.iapp.ageofchess.controllers.multiplayer.AccountController;
import com.iapp.ageofchess.graphics.AccountPanel;
import com.iapp.ageofchess.modding.LoaderMap;
import com.iapp.ageofchess.multiplayer.MultiplayerEngine;
import com.iapp.ageofchess.services.*;
import com.iapp.lib.ui.screens.*;
import com.iapp.lib.ui.actors.*;
import com.iapp.lib.util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ChessApplication extends RdApplication {

	private final ServerMode serverMode;
	private final Cheats cheats;
    private final ApplicationMode appMode;
	private RdI18NBundle strings;

	private final List<Locale> languageLocales = new ArrayList<>();
	private final List<String> languages = new ArrayList<>();
	private final List<String> displayLanguages = new ArrayList<>();
	private final List<Locale> countryLocales = new ArrayList<>();
	private final List<String> countries = new ArrayList<>();
    private LoggingView loggingView;
    private RdTable lineContent;
    private boolean initialize;

	public ChessApplication(Launcher launcher, ServerMode serverMode,
                            ApplicationMode appMode, Cheats cheats) {
		super(launcher, ChessConstants.WORLD_WIDTH, ChessConstants.WORLD_HEIGHT,
				new ChessAssetManager(), 3);
		this.serverMode = serverMode;
		this.cheats = cheats;
        this.appMode = appMode;
	}

	public ChessApplication(Launcher launcher) {
		this(launcher, ServerMode.SERVER, ApplicationMode.RELEASE, Cheats.USER);
	}

    public RdTable getLineContent() {
        return lineContent;
    }

    public LoggingView getLoggingView() {
        return loggingView;
    }

	public List<String> getLanguages() {
		return languages;
	}

	public List<String> getDisplayLanguages() {
		return displayLanguages;
	}

	public List<Locale> getLanguageLocales() {
		return languageLocales;
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

	public ServerMode getLaunchMode() {
		return serverMode;
	}

	public Cheats getCheats() {
		return cheats;
	}

	@Override
	public void launch(RdAssetManager rdAssetManager) {
		var style = new RdLabel.RdLabelStyle();
		style.font = new Font("gray_style/fonts/itxt_lite.fnt", Font.DistanceFieldType.SDF);
		style.color = Color.WHITE;
		RdLogger.self().setLogStyle(style);
		RdLogger.self().setDescStyle(style);
		initConstants();

		SplashActivity.loadLibrary(MenuActivity::new,
            "gray_style/textures/logo.png", "gray_style/textures/title_logo.png",
            LoaderMap.self().getTaskLoadDiskMaps(() -> ChessApplication.self().initialize()));

        if (serverMode == ServerMode.SERVER) {
            ChessConstants.serverAPI = "ws://185.104.248.176:8082/ws";
        } else {
            ChessConstants.serverAPI = "ws://localhost:8082/ws";
        }

        if (appMode == ApplicationMode.RELEASE) {
            RdApplication.self().setLogHandle(Files.FileType.External, ChessConstants.LOGS_DIRECTORY);
        }

		Gdx.app.log("Launch Mode", String.valueOf(serverMode));
        MultiplayerEngine.self().launchMultiplayerEngine();
	}

	public void updateTitle(WindowGroup group, String text) {}

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

		var baseFileHandle = Gdx.files.internal("languages/lang");
		RdApplication.self().setStrings(RdI18NBundle.createBundle(baseFileHandle,
				ChessConstants.localData.getLocale()));
		strings = RdApplication.self().getStrings();

		RdApplication.self().setFps(ChessConstants.localData.getFps().getValue());
		if (ChessConstants.localData.isSaveWindowSize()) {
			var windowSize = ChessConstants.localData.getWindowSize();
			Gdx.graphics.setWindowedMode(windowSize.getKey(), windowSize.getValue());
		}
		if (ChessConstants.localData.isEnableBackgroundMusic()) {
			Sounds.self().startBackgroundMusic();
		}

		if (ChessConstants.localData.isFullScreen()) Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
		OnChangeListener.setButtonClick(ChessAssetManager.current().getClickListener());

        // single initialize!
		if (!initialize) {
            initialize = true;

            ChessConstants.accountPanel = new AccountPanel();
            ChessConstants.accountPanel.setVisible(false);
            ChessConstants.accountController = new AccountController();

			Runnable task = () -> {

				var langs = new String[]
						{"ar", "az", "be", "bg", "cs", "cze", "da", "de", "el",
						"en", "es", "est", "fa", "fi", "fr", "he", "hr",
                        "hu", "hy", "id", "it", "ja", "ka", "kk", "ku", "lt",
                        "lv", "mk", "mal", "no", "dut", "pol", "pt",
                        "ro", "ru", "slo", "slv", "sq", "sr", "sw", "th", "tl",
                        "tr", "uk", "uz", "vi", "zh"};
				for (var lang : langs) {
					var locale = new Locale(lang);
					languageLocales.add(locale);
					languages.add(lang);
					displayLanguages.add(locale.getDisplayLanguage(locale));
				}

				for (var country : Locale.getISOCountries()) {
					var locale = new Locale("en", country);
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

    public Spinner showSpinner(String text) {
        Spinner spinner = new Spinner(text);
        spinner.show(RdApplication.self().getStage());
        spinner.setSize(400, 100);
        RdApplication.self().addDialog(spinner, WindowUtil::resizeCenter);
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        return spinner;
    }

	public void showAccept(String text) {
		var accept = new RdDialogBuilder()
				.title(strings.get("done"))
				.accept(strings.get("accept"))
				.text(text)
				.build(ChessAssetManager.current().getSkin(), "input");

		accept.getIcon().setDrawable(new TextureRegionDrawable(
				ChessAssetManager.current().findRegion("icon_accept")));
		accept.getIcon().setScaling(Scaling.fit);

		accept.show(RdApplication.self().getStage());
		accept.setSize(800, 550);
		RdApplication.self().addDialog(accept, WindowUtil::resizeCenter);
		resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}

    public void showInput(String text, BiConsumer<RdDialog, String> onAccept) {
        showInput(text, strings.get("accept"), onAccept);
    }

    public void showSelector(Consumer<FileHandle> onSelect, String... extensions) {
        RdDialog selector = new FileSelectorBuilder()
            .title(strings.get("file_selector"))
            .endFilters(extensions)
            .select(strings.get("select"), onSelect)
            .cancel(strings.get("cancel"))
            .build(ChessAssetManager.current().getSkin());

        selector.show(RdApplication.self().getStage());
        selector.setSize(900, 800);
        RdApplication.self().addDialog(selector, WindowUtil::resizeCenter);
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    public void showInput(String text, String accept, BiConsumer<RdDialog, String> onAccept) {
        RdDialog conf = new RdDialogBuilder()
            .title(strings.get("input"))
            .cancel(strings.get("cancel"))
            .accept(accept, onAccept)
            .text(text)
            .input(strings.get("enter_hint"))
            .build(ChessAssetManager.current().getSkin(), "input");

        conf.getIcon().setDrawable(new TextureRegionDrawable(
            ChessAssetManager.current().findRegion("icon_info")));
        conf.getIcon().setScaling(Scaling.fit);

        conf.show(RdApplication.self().getStage());
        conf.setSize(800, 550);
        RdApplication.self().addDialog(conf, WindowUtil::resizeCenter);
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

	public void showConf(String text, BiConsumer<RdDialog, String> onAccept) {
		showConf(strings.get("accept"), text, onAccept);
	}

    public void showConf(String accept, String text, BiConsumer<RdDialog, String> onAccept) {
        RdDialog conf = new RdDialogBuilder()
            .title(strings.get("info"))
            .cancel(strings.get("cancel"))
            .accept(accept, onAccept)
            .text(text)
            .build(ChessAssetManager.current().getSkin(), "input");

        conf.getIcon().setDrawable(new TextureRegionDrawable(
            ChessAssetManager.current().findRegion("icon_conf")));
        conf.getIcon().setScaling(Scaling.fit);

        conf.show(RdApplication.self().getStage());
        conf.setSize(800, 550);
        RdApplication.self().addDialog(conf, WindowUtil::resizeCenter);
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    public void showConfWarn(String text, BiConsumer<RdDialog, String> onAccept) {
        showConfWarn(strings.get("accept"), text, onAccept);
    }

    public void showConfWarn(String accept, String text, BiConsumer<RdDialog, String> onAccept) {
        RdDialog conf = new RdDialogBuilder()
            .title(strings.get("info"))
            .cancel(strings.get("cancel"))
            .accept(accept, onAccept)
            .text(text)
            .build(ChessAssetManager.current().getSkin(), "input");

        conf.getIcon().setDrawable(new TextureRegionDrawable(
            ChessAssetManager.current().findRegion("icon_warn")));
        conf.getIcon().setScaling(Scaling.fit);

        conf.show(RdApplication.self().getStage());
        conf.setSize(800, 550);
        RdApplication.self().addDialog(conf, WindowUtil::resizeCenter);
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

	public void showInfo(String text) {
		RdDialog info = new RdDialogBuilder()
				.title(strings.get("info"))
				.accept(strings.get("accept"))
				.text(text)
				.build(ChessAssetManager.current().getSkin(), "input");

		info.getIcon().setDrawable(new TextureRegionDrawable(
				ChessAssetManager.current().findRegion("icon_info")));
		info.getIcon().setScaling(Scaling.fit);

		info.show(RdApplication.self().getStage());
		info.setSize(800, 550);
		RdApplication.self().addDialog(info, WindowUtil::resizeCenter);
		resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

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
				.title(strings.get("error"))
				.accept(strings.get("accept"), onCancel)
                .onHide(onCancel)
				.text(text)
				.build(ChessAssetManager.current().getSkin(), "input");

		error.getIcon().setDrawable(new TextureRegionDrawable(
				ChessAssetManager.current().findRegion("icon_error")));
		error.getIcon().setScaling(Scaling.fit);

		error.show(RdApplication.self().getStage());
		error.setSize(800, 550);
		RdApplication.self().addDialog(error, WindowUtil::resizeCenter);
		resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}

	private void initConstants() {
		ChessConstants.localData = DataManager.self().readLocalData();
		RdLogger.self().setOnFatal(() -> ChessApplication.self().getLauncher().setOnFinish(null));
	}
}
