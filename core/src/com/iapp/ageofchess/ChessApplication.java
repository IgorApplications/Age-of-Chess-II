package com.iapp.ageofchess;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.github.tommyettinger.textra.Font;
import com.iapp.ageofchess.activity.MenuActivity;
import com.iapp.ageofchess.graphics.AccountPanel;
import com.iapp.ageofchess.modding.LoaderMap;
import com.iapp.ageofchess.multiplayer.MultiplayerEngine;
import com.iapp.ageofchess.util.*;
import com.iapp.rodsher.actors.*;
import com.iapp.rodsher.screens.*;
import com.iapp.rodsher.util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChessApplication extends RdApplication {

	private final LaunchMode launchMode;
	private final Cheats cheats;

	private LoggingView loggingView;
	private RdI18NBundle strings;
	private AccountPanel accountPanel;

	private final List<Locale> languageLocales = new ArrayList<>();
	private final List<String> languages = new ArrayList<>();
	private final List<String> displayLanguages = new ArrayList<>();
	private final List<Locale> countryLocales = new ArrayList<>();
	private final List<String> countries = new ArrayList<>();

	public ChessApplication(com.iapp.rodsher.screens.Launcher launcher, LaunchMode launchMode, Cheats cheats) {
		super(launcher, ChessConstants.WORLD_WIDTH, ChessConstants.WORLD_HEIGHT,
				new ChessAssetManager(), 4);
		this.launchMode = launchMode;
		this.cheats = cheats;

	}

	public ChessApplication(Launcher launcher) {
		this(launcher, LaunchMode.RELEASE, Cheats.USER);
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

	public LaunchMode getLaunchMode() {
		return launchMode;
	}

	public Cheats getCheats() {
		return cheats;
	}

	public LoggingView getLoggingView() {
		return loggingView;
	}

	public AccountPanel getAccountPanel() {
		return accountPanel;
	}

	@Override
	public void launch(RdAssetManager rdAssetManager) {
		var style = new RdLabel.RdLabelStyle();
		style.font = new Font("gray_style/fonts/itxt_lite.fnt", Font.DistanceFieldType.SDF);
		style.color = Color.WHITE;
		RdLogger.setLogStyle(style);
		RdLogger.setDescStyle(style);
		initConstants();

		SplashActivity.loadLibrary(MenuActivity::new,
            "gray_style/textures/logo.png", "gray_style/textures/title_logo.png",
            LoaderMap.self().getTaskLoadDiskMaps(() -> ChessApplication.self().initialize()));

        if (launchMode == LaunchMode.RELEASE) {
            ChessConstants.serverAPI = "ws://localhost:8082";
            RdApplication.self().setLogHandle(Files.FileType.External, ChessConstants.LOGS_DIRECTORY);
        } else {
            ChessConstants.serverAPI = "ws://localhost:8082";
        }

		Gdx.app.log("Launch Mode", String.valueOf(launchMode));
        MultiplayerEngine.self().launchMultiplayerThread();
	}

	public void updateTitle(WindowGroup group, String text) {
        //RdApplication.self().getTopContent().add(accountPanel)
        //    .align(Align.topRight).padRight(100);

		var modeTitle = new RdTable();
		modeTitle.setBackground(new TextureRegionDrawable(
				ChessAssetManager.current().findChessRegion("mode_app")));
		modeTitle.add(new RdLabel("[%125]" + text));
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
        MultiplayerEngine.self().exitMatch();
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
		if (loggingView == null) {

            accountPanel = new AccountPanel();
            accountPanel.setVisible(false);

			Runnable task = () -> {

				var langs = new String[]
						{"ar", "az", "be", "bg", "ca", "cze", "da", "de", "gre",
						"en", "aus", "gbr", "esperanto", "es", "es(America Latin)", "est",
						"fa", "fi", "fr", "heb", "?", "cro", "hu", "aeries", "in", "it", "jp",
						"ge", "kz", "ko", "ku", "lt", "lv", "mkd", "mal", "no",
						"dut", "pol", "pt_br", "pt", "ro", "ru", "slo", "slv",
						"al", "sr", "sw", "th", "tagalog", "tk", "uk", "uz",
						"vi", "ch", "hk", "?"};
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
            getStage().addActor(loggingView);

			loggingView.setDefaultToken("[%75]");
		}

		loggingView.setVisible(ChessConstants.localData.isEnableSysProperties());
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

	public RdDialog showConf(String text, OnChangeListener onAccept) {
		var conf = new RdDialogBuilder()
				.title(strings.get("info"))
				.cancel(strings.get("cancel"))
				.accept(strings.get("accept"), onAccept)
				.text(text)
				.build(ChessAssetManager.current().getSkin(), "input");

		conf.getIcon().setDrawable(new TextureRegionDrawable(
				ChessAssetManager.current().findRegion("icon_conf")));
		conf.getIcon().setScaling(Scaling.fit);

		conf.show(RdApplication.self().getStage());
		conf.setSize(800, 550);
		RdApplication.self().addDialog(conf, WindowUtil::resizeCenter);
		resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		return conf;
	}

	public void showInfo(String text) {
		var info = new RdDialogBuilder()
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
	private RdDialog error;

	public void showError(String text) {
		if (errors > 0) return;
		errors++;

		var onCancel = new OnChangeListener() {
			@Override
			public void onChange(Actor actor) {
				error.hide();
				errors = 0;
			}
		};

		error = new RdDialogBuilder()
				.title(strings.get("error"))
				.accept(strings.get("accept"), onCancel)
				.text(text)
				.build(ChessAssetManager.current().getSkin(), "input");
		error.setOnCancel(onCancel);

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
		RdLogger.setOnFatal(() -> ChessApplication.self().getLauncher().setOnFinish(null));
	}
}
