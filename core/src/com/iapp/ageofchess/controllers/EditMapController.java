package com.iapp.ageofchess.controllers;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Scaling;
import com.iapp.ageofchess.ChessApplication;
import com.iapp.ageofchess.activity.EditMapActivity;
import com.iapp.ageofchess.activity.ModdingActivity;
import com.iapp.ageofchess.chess_engine.Game;
import com.iapp.ageofchess.modding.LoaderMap;
import com.iapp.ageofchess.modding.MapData;
import com.iapp.ageofchess.modding.MapResources;
import com.iapp.ageofchess.util.ChessAssetManager;
import com.iapp.ageofchess.util.ChessConstants;
import com.iapp.ageofchess.util.DataManager;
import com.iapp.ageofchess.util.ResourcesLoader;
import com.iapp.rodsher.actors.*;
import com.iapp.rodsher.screens.Controller;
import com.iapp.rodsher.screens.RdApplication;
import com.iapp.rodsher.util.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class EditMapController extends Controller {

    private static final Pattern FLOAT_PATTERN = Pattern.compile("[-+]?[0-9]*\\.?[0-9]+");

    private final EditMapActivity activity;
    private final MapData mapData;
    private final MapResources resources;
    private final boolean newMap;
    private RdSelectBox<String> stringsScenario;
    private static final List<String> reservedNames =
            Arrays.asList("white_pawn", "black_pawn",
                    "white_rook", "black_rook",
                    "white_knight", "black_knight",
                    "white_bishop", "black_bishop",
                    "white_queen", "black_queen",
                    "white_king", "black_king",
                    "board", "background");

    public EditMapController(EditMapActivity activity, MapData mapData,
                             MapResources resources, boolean newMap) {
        super(activity);
        this.activity = activity;
        this.mapData = mapData;
        this.resources = resources;
        this.newMap = newMap;
    }

    public RdI18NBundle getStrings() {
        return strings;
    }

    public void goToModding(Action action) {
        startActivity(new ModdingActivity(), action);
    }

    public void goToModding() {
        goToModding(Actions.run(() -> {
            resources.dispose();
            mapData.getAtlas().dispose();
            System.gc();
        }));
    }

    public EditMapActivity getActivity() {
        return activity;
    }

    public void showConfExit() {
        activity.getBlackout().setVisible(true);
        var text = newMap ? strings.get("conf_exit_new_mod") : strings.get("conf_exit_mod");
        var confExit = new RdDialogBuilder()
                .title(strings.get("confirmation"))
                .text(text)
                .onHide(new OnChangeListener() {
                    @Override
                    public void onChange(Actor actor) {
                        activity.getConfExit().hide();
                        activity.getBlackout().setVisible(false);
                    }
                })
                .cancel(strings.get("not_save"), new OnChangeListener() {
                    @Override
                    public void onChange(Actor actor) {
                        goToModding();
                    }
                })
                .accept(strings.get("save"), new OnChangeListener() {
                    @Override
                    public void onChange(Actor actor) {
                        for (int i = 0; i < mapData.getScenarios().length; i++) {
                            var part1 = mapData.getScenarios()[i].split(" ")[0];
                            if (part1.matches(".*(k.*k).*") || part1.matches(".*(K.*K).*")
                                    || !part1.contains("k") || !part1.contains("K")) {
                                showError(strings.format("incorrect_scenario", i + 1),
                                        true, () -> activity.getBlackout().setVisible(true));
                                return;
                            }
                        }

                        var spinner = new Spinner(strings.get("loading"));
                        activity.setSpinner(spinner);
                        spinner.show(RdApplication.self().getStage());
                        spinner.setSize(400, 100);
                        activity.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

                        Runnable task = () -> {
                            var copy = new ArrayList<>(ChessAssetManager.current().getDataMaps());
                            ChessAssetManager.current().getDataMaps().clear();
                            DataManager.self().loadMapData(mapData, resources);

                            var taskLoad = LoaderMap.self().getTaskLoadDiskMaps(() ->
                                goToModding(Actions.run(() -> {
                                    resources.dispose();
                                    mapData.getAtlas().dispose();
                                    for (var mapData : copy) {
                                        mapData.dispose();
                                    }
                                }))
                            );
                            taskLoad.load();

                        };
                        RdApplication.self().execute(task);
                    }
                })
                .build(ChessAssetManager.current().getSkin(), "input");

        confExit.getIcon().setDrawable(new TextureRegionDrawable(
                ChessAssetManager.current().findRegion("icon_conf")));
        confExit.getIcon().setScaling(Scaling.fit);
        confExit.show(activity.getStage());
        activity.setConfExit(confExit);
        confExit.setSize(800, 550);
        activity.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    public TextureAtlas.AtlasRegion getRegion(String name) {
        TextureAtlas.AtlasRegion region = null;
        if (mapData.getAtlas() != null) {
            region = mapData.getAtlas().findRegion(name);
        }
        if (region == null) return ChessAssetManager.current().findChessRegion(name);
        return region;
    }

    //--------------------------------------------//

    public void getEditMap(Stage stage) {
        activity.getBlackout().setVisible(true);
        var dialog = new RdDialog(strings.get("map_change"), ChessAssetManager.current().getSkin());
        dialog.setOnCancel(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                for (int i = 0; i < mapData.getScenarios().length; i++) {
                    if (!Game.isValidFEN(mapData.getScenarios()[i])) {
                        showError(strings.format("incorrect_fen", (i + 1)), true, () -> {});
                        return;
                    }
                }
                dialog.hide();
                activity.getBlackout().setVisible(false);
            }
        });
        var properties = new PropertyTable(500, ChessAssetManager.current().getSkin());
        dialog.getContentTable().add(properties).expand().fill();

        addTextures(properties);
        addMapIconTexture(properties);
        addAtlas(properties);
        addScenarios(properties);
        addDescription(properties);
        addSettingsMap(properties);

        dialog.show(stage);
        activity.setChangeDialog(dialog);
        activity.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private void addTextures(PropertyTable properties) {
        var textures = new RdSelectBox<String>(ChessAssetManager.current().getSkin());
        textures.setItems(mapData.getNameTextures());
        var removeTexture = new RdTextButton(strings.get("remove"), "dark");
        var loadTexture = new RdTextButton(strings.get("load"), "blue");

        loadTexture.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                Consumer<FileHandle> onSelect = handle -> {
                    if (!reservedNames.contains(handle.nameWithoutExtension())) {
                        showError(strings.get("wrong_name") + reservedNames.toString()
                                        .replaceAll("\\[", "|").replaceAll("]", "|"),
                                true, () -> {});
                        return;
                    }
                    // Report!
                    if (contains(mapData.getNameTextures(), handle.nameWithoutExtension())) return;

                    var spinner = new Spinner(strings.get("loading"));
                    activity.setSpinner(spinner);
                    spinner.show(RdApplication.self().getStage());
                    spinner.setSize(400, 100);
                    activity.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                    activity.getSelector().hide();

                    loadTexture(handle, () -> {
                        textures.setItems(mapData.getNameTextures());
                        activity.getSpinner().hide();
                    });
                };

                Gdx.input.setOnscreenKeyboardVisible(false);
                showSelector(onSelect, ".png", ".jpg");
            }
        });

        removeTexture.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                removeTexture(textures);
            }
        });

        properties.add(new PropertyTable.Title(strings.get("textures")));
        properties.add(new PropertyTable.Element(strings.get("load_texture"), loadTexture));
        properties.add(new PropertyTable.Element(strings.get("content_textures"), textures));
        properties.add(new PropertyTable.Element(strings.get("remove_texture"), removeTexture));
    }

    private void addMapIconTexture(PropertyTable properties) {
        var name = mapData.getMapIconPath() == null ? "null"
                : Gdx.files.getFileHandle(mapData.getMapIconPath(), mapData.getType()).nameWithoutExtension();

        var mapIcon = new RdLabel(name);
        var loadIcon = new RdTextButton(strings.get("load"), "blue");
        var removeIcon = new RdTextButton(strings.get("remove"), "dark");

        loadIcon.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {

                Consumer<FileHandle> onSelect = handle -> {
                    // Report!
                    if (reservedNames.contains(handle.nameWithoutExtension())) {
                        showError(strings.get("name_reserved"), true, () -> {});
                        return;
                    }

                    if (mapData.getMapIconPath() != null &&
                            Gdx.files.getFileHandle(mapData.getMapIconPath(), mapData.getType())
                            .name().equals(handle.name())) return;

                    var spinner = new Spinner(strings.get("loading"));
                    activity.setSpinner(spinner);
                    spinner.show(RdApplication.self().getStage());
                    spinner.setSize(400, 100);
                    activity.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                    activity.getSelector().hide();

                    loadMapIcon(handle, () -> {
                        mapIcon.setText(handle.nameWithoutExtension());
                        activity.getSpinner().hide();
                    });

                    activity.getSelector().hide();
                };

                Gdx.input.setOnscreenKeyboardVisible(false);
                showSelector(onSelect, ".png", ".jpg");
            }
        });
        removeIcon.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                removeMapIcon(mapIcon);
            }
        });

        properties.add(new PropertyTable.Element(strings.get("map_icon"), mapIcon));
        properties.add(new PropertyTable.Element(strings.get("load_icon"), loadIcon));
        properties.add(new PropertyTable.Element(strings.get("remove_icon"), removeIcon));
    }

    private void addAtlas(PropertyTable properties) {
        var atlasEl = new RdSelectBox<String>(ChessAssetManager.current().getSkin());
        atlasEl.setItems(getAtlasElements());

        var removeAtlas = new RdTextButton(strings.get("remove"), "dark");
        var loadAtlas = new RdTextButton(strings.get("load"), "blue");

        loadAtlas.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                Consumer<FileHandle> onSelect = handle -> {
                    if (checkAtlasNames(handle)) {
                        showError(strings.get("wrong_names"), true, () -> {});
                        return;
                    }

                    // Report!
                    if (mapData.getAtlasPath() != null && Gdx.files.getFileHandle(mapData.getAtlasPath(), mapData.getType())
                            .nameWithoutExtension().equals(handle.nameWithoutExtension())) return;

                    var atlasPngs = getAtlasPngs(handle);
                    if (!atlasPngs.getKey()) return;

                    var spinner = new Spinner(strings.get("loading"));
                    activity.setSpinner(spinner);
                    spinner.show(RdApplication.self().getStage());
                    spinner.setSize(400, 100);
                    activity.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                    activity.getSelector().hide();

                    loadAtlas(handle, atlasPngs.getValue(), () -> {
                        atlasEl.setItems(getAtlasElements());
                        activity.getSpinner().hide();
                    });
                };

                Gdx.input.setOnscreenKeyboardVisible(false);
                showSelector(onSelect, ".atlas");
            }
        });

        removeAtlas.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                removeAtlas(atlasEl);
            }
        });

        properties.add(new PropertyTable.Title(strings.get("atlas")));
        properties.add(new PropertyTable.Element(strings.get("loading_atlas"), loadAtlas));
        properties.add(new PropertyTable.Element(strings.get("content_atlas"), atlasEl));
        properties.add(new PropertyTable.Element(strings.get("remove_atlas"), removeAtlas));
    }

    private void addScenarios(PropertyTable properties) {
        var scenariosList = new RdSelectBox<String>(ChessAssetManager.current().getSkin());
        var items = getScenariosList();
        scenariosList.setItems(items);
        int index = activity.getModdingView().getScenario();
        scenariosList.setSelectedIndex(index);

        var rankScenario = new RdLabel(getRanked(index));
        var createScenario = new RdTextButton(strings.get("create"), "blue");
        var removeScenario = new RdTextButton(strings.get("remove"), "dark");

        var scenarioIcon = new RdLabel(getScenarioIcon(index));
        var loadIcon = new RdTextButton(strings.get("load"), "blue");
        var removeIcon = new RdTextButton(strings.get("remove"), "dark");

        scenariosList.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                int index = getScenarioIndex(scenariosList);
                rankScenario.setText(getRanked(index));
                scenarioIcon.setText(getScenarioIcon(index));
                scenarioIcon.setText(getScenarioIcon(index));

                activity.getModdingView().setScenario(index);
                activity.getModdingView().update();
            }
        });

        createScenario.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                mapData.setScenarios(Arrays.copyOf(
                        mapData.getScenarios(), mapData.getScenarios().length + 1));
                mapData.getScenarios()[mapData.getScenarios().length - 1] =
                        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq";
                mapData.setScenarioIcons(Arrays.copyOf(mapData.getScenarioIcons(),
                        mapData.getScenarioIcons().length + 1));
                mapData.setScenarioIconPaths(Arrays.copyOf(mapData.getScenarioIconPaths(),
                        mapData.getScenarioIcons().length + 1));

                var items = getScenariosList();
                scenariosList.setItems(items);
                stringsScenario.setItems(getScenariosList());

                for (var lang : resources.getStrings().keys()) {
                    lang = ChessApplication.self().getDisplayLanguages().get(ChessApplication.self().getLanguages().indexOf(lang));

                    putString(lang, "title_scenario_" + (mapData.getScenarios().length), "null");
                    putString(lang, "desc_scenario_" + (mapData.getScenarios().length), "null");
                }

                int index = getScenarioIndex(scenariosList);
                activity.getModdingView().setScenario(index);
                activity.getModdingView().update();
            }
        });

        removeScenario.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                if (mapData.getScenarios().length == 1) return;
                int index = getScenarioIndex(scenariosList);

                Texture copy = null;
                if (mapData.getScenarioIcons()[index] != null) {
                    copy = mapData.getDisposableMap().remove(mapData.getScenarioIcons()[index]);
                }
                mapData.setScenarios(remove(mapData.getScenarios(), index));
                mapData.setScenarioIcons(remove(mapData.getScenarioIcons(), index));
                mapData.setScenarioIconPaths(remove(mapData.getScenarioIconPaths(), index));
                DisposeUtil.dispose(copy);

                var items = getScenariosList();
                scenariosList.setItems(items);
                index = getScenarioIndex(scenariosList);

                stringsScenario.setItems(getScenariosList());
                rankScenario.setText(getRanked(index));
                scenarioIcon.setText(getScenarioIcon(index));

                for (var lang : resources.getStrings().keys()) {
                    resources.getStrings().get(lang).remove("title_scenario_" + (index + 1));
                    resources.getStrings().get(lang).remove("desc_scenario_" + (index + 1));
                }

                for (int i = index + 2; i < Integer.MAX_VALUE; i++) {
                    if (!resources.getStrings().get("en").containsKey("title_scenario_" + i)) break;

                    for (var lang : resources.getStrings().keys()) {
                        var val1 = resources.getStrings().get(lang).remove("title_scenario_" + i);
                        var val2 = resources.getStrings().get(lang).remove("desc_scenario_" + i);
                        resources.getStrings().get(lang).put("title_scenario_" + (i - 1), val1);
                        resources.getStrings().get(lang).put("desc_scenario_" + (i - 1), val2);
                    }
                }

                index = getScenarioIndex(scenariosList);
                rankScenario.setText(getRanked(index));
                scenarioIcon.setText(getScenarioIcon(index));
                scenarioIcon.setText(getScenarioIcon(index));

                activity.getModdingView().setScenario(index);
                activity.getModdingView().update();
            }
        });

        loadIcon.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {

               Consumer<FileHandle> onSelect = handle -> {
                    int index = getScenarioIndex(scenariosList);
                    if (reservedNames.contains(handle.nameWithoutExtension())) {
                        showError(strings.get("name_reserved"), true, () -> {});
                        return;
                    }

                    // Report!
                    if (mapData.getScenarioIconPaths()[index] != null &&
                            Gdx.files.getFileHandle(mapData.getScenarioIconPaths()[index], mapData.getType())
                            .name().equals(handle.name())) return;

                    var spinner = new Spinner(strings.get("loading"));
                    activity.setSpinner(spinner);
                    spinner.show(RdApplication.self().getStage());
                    spinner.setSize(400, 100);
                    activity.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                    activity.getSelector().hide();

                    loadScenarioIcon(handle, getScenarioIndex(scenariosList), () -> {
                        scenarioIcon.setText(getScenarioIcon(index));
                        spinner.hide();
                    });
                };

                Gdx.input.setOnscreenKeyboardVisible(false);
                showSelector(onSelect, ".jpg", ".png");

            }
        });

        removeIcon.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                int index = getScenarioIndex(scenariosList);
                removeScenarioIcon(index,
                        () -> scenarioIcon.setText(getScenarioIcon(index)));
            }
        });

        properties.add(new PropertyTable.Title(strings.get("scenarios")));
        properties.add(new PropertyTable.Element(strings.get("list_scenarios"), scenariosList));
        properties.add(new PropertyTable.Element(strings.get("create_scenario"), createScenario));
        properties.add(new PropertyTable.Element(strings.get("remove_scenario"), removeScenario));
        properties.add(new PropertyTable.Element(strings.get("ranked_scenario"), rankScenario));

        properties.add(new PropertyTable.Element(strings.get("scenario_icon"), scenarioIcon));
        properties.add(new PropertyTable.Element(strings.get("load_icon_scenario"), loadIcon));
        properties.add(new PropertyTable.Element(strings.get("remove_icon_scenario"), removeIcon));
    }

    private void addDescription(PropertyTable properties) {
        var englishKey = Locale.ENGLISH.getDisplayLanguage(Locale.ENGLISH);
        properties.add(new PropertyTable.Title(strings.get("mod_desc")));

        var lang = new RdSelectBox<String>(ChessAssetManager.current().getSkin());
        lang.setItems(ChessApplication.self().getDisplayLanguages().toArray(new String[0]));
        var addLang = new RdTextButton(strings.get("add"), "blue");
        var removeLang = new RdTextButton(strings.get("remove"), "dark");

        var name = new RdTextArea("");
        name.setPrefLines(2);
        name.setText(getString(englishKey, "name"));

        var description = new RdTextArea("");
        description.setPrefLines(4);
        description.setText(getString(englishKey, "description"));

        var author = new RdTextArea("");
        author.setPrefLines(2);
        author.setText(getString(englishKey, "author"));

        var created = new RdLabel("");
        created.setText(getString(englishKey, "created"));

        var updated = new RdLabel("");
        updated.setText(getString(englishKey, "updated"));

        var mapLang = new RdSelectBox<String>();
        mapLang.setItems(getMapLanguages());

        stringsScenario = new RdSelectBox<>(ChessAssetManager.current().getSkin());
        var items = getScenariosList();
        stringsScenario.setItems(items);
        int index = getScenarioIndex(stringsScenario);

        var titleScenario = new RdTextArea(getString(mapLang.getSelected(), "title_scenario_" + (index + 1)));
        titleScenario.setPrefLines(2);
        var descScenario = new RdTextArea(getString(mapLang.getSelected(), "desc_scenario_" + (index + 1)));
        descScenario.setPrefLines(4);

        stringsScenario.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                int index = getScenarioIndex(stringsScenario);
                titleScenario.setText(getString(mapLang.getSelected(), "title_scenario_" + (index + 1)));
                descScenario.setText(getString(mapLang.getSelected(), "desc_scenario_" + (index + 1)));
            }
        });

        titleScenario.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                int index = getScenarioIndex(stringsScenario);
                putString(mapLang.getSelected(), "title_scenario_" + (index + 1), titleScenario.getText());
            }
        });

        descScenario.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                int index = getScenarioIndex(stringsScenario);
                putString(mapLang.getSelected(), "desc_scenario_" + (index + 1), descScenario.getText());
            }
        });

        addLang.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                if (mapLang.getItems().contains(lang.getSelected(), false)) return;

                var langMap = new ObjectMap<>(resources.getStrings().get("en"));
                resources.getStrings().put(
                        ChessApplication.self().getLanguages().get(
                                ChessApplication.self().getDisplayLanguages().indexOf(lang.getSelected())), langMap);
                mapLang.setItems(getMapLanguages());
            }
        });

        removeLang.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                if (lang.getSelected().equals("English")
                        || !mapLang.getItems().contains(lang.getSelected(), false)) return;

                resources.getStrings()
                        .remove(ChessApplication.self().getLanguages().get(
                                ChessApplication.self().getDisplayLanguages().indexOf(lang.getSelected())));
                mapLang.setItems(getMapLanguages());
            }
        });

        mapLang.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                name.setText(getString(mapLang.getSelected(), "name"));
                description.setText(getString(mapLang.getSelected(), "description"));
                author.setText(getString(mapLang.getSelected(), "author"));
                updated.setText(getString(mapLang.getSelected(), "updated"));
                created.setText(getString(mapLang.getSelected(), "created"));

                int index = getScenarioIndex(stringsScenario);
                titleScenario.setText(getString(mapLang.getSelected(), "title_scenario_" + (index + 1)));
                descScenario.setText(getString(mapLang.getSelected(), "desc_scenario_" + (index + 1)));
            }
        });

        name.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                putString(mapLang.getSelected(), "name", name.getText());
            }
        });
        description.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                putString(mapLang.getSelected(), "description", description.getText());
            }
        });
        author.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                putString(mapLang.getSelected(), "author", author.getText());
            }
        });

        properties.add(new PropertyTable.Element(strings.get("available_lang"), lang));
        properties.add(new PropertyTable.Element(strings.get("add_lang"), addLang));
        properties.add(new PropertyTable.Element(strings.get("remove_lang"), removeLang));
        properties.add(new PropertyTable.Element(strings.get("map_lang"), mapLang));
        properties.add(new PropertyTable.Element(strings.get("mod_name"), name));
        properties.add(new PropertyTable.Element(strings.get("mod_desc"), description));
        properties.add(new PropertyTable.Element(strings.get("mod_author"), author));
        properties.add(new PropertyTable.Element(strings.get("mod_created"), created));
        properties.add(new PropertyTable.Element(strings.get("mod_updated"), updated));

        properties.add(new PropertyTable.Element(strings.get("list_scenarios"), stringsScenario));
        properties.add(new PropertyTable.Element(strings.get("name_scenario"), titleScenario));
        properties.add(new PropertyTable.Element(strings.get("desc_scenario"), descScenario));
    }

    private void addSettingsMap(PropertyTable properties) {
        var mapWidth = new RdTextArea(String.valueOf(mapData.getWidth()));
        var mapHeight = new RdTextArea(String.valueOf(mapData.getHeight()));
        var padLeft = new RdTextArea(String.valueOf(mapData.getPadLeft()));
        var padRight = new RdTextArea(String.valueOf(mapData.getPadRight()));
        var padBottom = new RdTextArea(String.valueOf(mapData.getPadBottom()));
        var padTop = new RdTextArea(String.valueOf(mapData.getPadTop()));

        properties.add(new PropertyTable.Title(strings.get("map_settings")));
        properties.add(new PropertyTable.Element(strings.get("width_board"), mapWidth));
        properties.add(new PropertyTable.Element(strings.get("height_board"), mapHeight));
        properties.add(new PropertyTable.Element(strings.get("pad_left_board"), padLeft));
        properties.add(new PropertyTable.Element(strings.get("pad_right_board"), padRight));
        properties.add(new PropertyTable.Element(strings.get("pad_bottom_board"), padBottom));
        properties.add(new PropertyTable.Element(strings.get("pad_top_board"), padTop));

        mapWidth.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                if (!FLOAT_PATTERN.matcher(mapWidth.getText()).matches()) return;
                mapData.setWidth(Float.parseFloat(mapWidth.getText()));
                activity.getModdingView().update();
            }
        });

        mapHeight.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                if (!FLOAT_PATTERN.matcher(mapHeight.getText()).matches()) return;
                mapData.setHeight(Float.parseFloat(mapHeight.getText()));
                activity.getModdingView().update();
            }
        });

        padLeft.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                if (!FLOAT_PATTERN.matcher(padLeft.getText()).matches()) return;
                mapData.setPadLeft(Float.parseFloat(padLeft.getText()));
                activity.getModdingView().update();
            }
        });

        padRight.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                if (!FLOAT_PATTERN.matcher(padRight.getText()).matches()) return;
                mapData.setPadRight(Float.parseFloat(padRight.getText()));
                activity.getModdingView().update();
            }
        });

        padBottom.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                if (!FLOAT_PATTERN.matcher(padBottom.getText()).matches()) return;
                mapData.setPadBottom(Float.parseFloat(padBottom.getText()));
                activity.getModdingView().update();
            }
        });

        padTop.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                if (!FLOAT_PATTERN.matcher(padTop.getText()).matches()) return;
                mapData.setPadTop(Float.parseFloat(padTop.getText()));
                activity.getModdingView().update();
            }
        });
    }

    //------------------------------------------------------------------

    private void loadTexture(FileHandle handle, CallListener callListener) {
        var desc = new AssetDescriptor<>(handle, Texture.class);
        var loader = new ResourcesLoader(Files.FileType.Absolute);
        loader.getAssetManager().load(desc);
        mapData.setTexturePaths(Arrays.copyOf(mapData.getTexturePaths(), mapData.getTexturePaths().length + 1));
        mapData.getTexturePaths()[mapData.getTexturePaths().length - 1]
                = String.format("%s/map%d/%s", ChessConstants.MAPS_DIRECTORY, mapData.getId(),  handle.name());

        AtomicBoolean thread1 = new AtomicBoolean(false),
                thread2 = new AtomicBoolean(false);

        Runnable task = () -> {
            resources.setTextures(Arrays.copyOf(resources.getTextures(),
                    resources.getTextures().length + 1));
            resources.getTextures()[resources.getTextures().length - 1] = handle.readBytes();

            resources.setTextureNames(Arrays.copyOf(resources.getTextureNames(),
                    resources.getTextureNames().length + 1));
            resources.getTextureNames()[resources.getTextureNames().length - 1]
                    = handle.name();
            thread1.set(true);

            if (thread1.get() && thread2.get()) {
                Gdx.app.postRunnable(() -> {
                    activity.getModdingView().update();
                    callListener.call();
                });
            }
        };
        RdApplication.self().execute(task);

        loader.setOnFinish(() -> {
            var texture = loader.getAssetManager().get(desc);
            mapData.getAtlas().addRegion(handle.nameWithoutExtension(), new TextureRegion(texture));
            mapData.setNameTextures(Arrays.copyOf(mapData.getNameTextures(), mapData.getNameTextures().length + 1));
            mapData.getNameTextures()[mapData.getNameTextures().length - 1] = handle.nameWithoutExtension();
            thread2.set(true);

            if (thread1.get() && thread2.get()) {
                Gdx.app.postRunnable(() -> {
                    activity.getModdingView().update();
                    callListener.call();
                });
            }
        });
        loader.launchLoad();
    }

    private void loadMapIcon(FileHandle handle, CallListener callListener) {
        var desc = new AssetDescriptor<>(handle, Texture.class);
        var loader = new ResourcesLoader(Files.FileType.Absolute);
        loader.getAssetManager().load(desc);

        if (mapData.getMapIconPath() != null) {
            int index = findIndex(resources.getTextureNames(), Gdx.files.external(mapData.getMapIconPath()).name());
            resources.setTextures(remove(resources.getTextures(), index));
            resources.setTextureNames(remove(resources.getTextureNames(), index));
        }

        mapData.setMapIconPath(
                String.format("%s/map%d/%s", ChessConstants.MAPS_DIRECTORY,
                        mapData.getId(),  handle.name()));

        AtomicBoolean thread1 = new AtomicBoolean(false),
                thread2 = new AtomicBoolean(false);

        Runnable task = () -> {
            resources.setTextures(Arrays.copyOf(resources.getTextures(),
                    resources.getTextures().length + 1));
            resources.getTextures()[resources.getTextures().length - 1] = handle.readBytes();

            resources.setTextureNames(Arrays.copyOf(resources.getTextureNames(),
                    resources.getTextureNames().length + 1));
            resources.getTextureNames()[resources.getTextureNames().length - 1]
                    = handle.name();
            thread1.set(true);

            if (thread1.get() && thread2.get()) {
                Gdx.app.postRunnable(() -> {
                    activity.getModdingView().update();
                    callListener.call();
                });
            }
        };
        RdApplication.self().execute(task);

        loader.setOnFinish(() -> {
            var texture = loader.getAssetManager().get(desc);
            Texture copy = mapData.getDisposableMap().remove(mapData.getMapIcon());
            mapData.setMapIcon(texture);
            thread2.set(true);

            if (thread1.get() && thread2.get()) {
                Gdx.app.postRunnable(() -> {
                    DisposeUtil.dispose(copy);
                    activity.getModdingView().update();
                    callListener.call();
                });
            }
        });
        loader.launchLoad();
    }

    private void removeMapIcon(RdLabel icon) {
        if (mapData.getMapIconPath() == null) return;
        Runnable task = () -> {
            var extension = mapData.getMapIconPath().split("\\.");
            var copy = mapData.getDisposableMap().remove(mapData.getMapIcon());
            mapData.setMapIcon(null);
            mapData.setMapIconPath(null);

            int index1 = findIndex(resources.getTextureNames(), icon.getOriginalText().toString()
                    + (extension.length > 1 ? "." + extension[1] : ""));
            if (index1 == -1) {
                Gdx.app.error("removeMapIcon", "Texture with the same name was not found in the resources");
                return;
            }
            resources.setTextureNames(remove(resources.getTextureNames(), index1));
            resources.setTextures(remove(resources.getTextures(), index1));

            Gdx.app.postRunnable(() -> {
                DisposeUtil.dispose(copy);
                icon.setText("null");
            });
        };
        RdApplication.self().execute(task);
    }

    private void removeTexture(RdSelectBox<String> textureBox) {
        Runnable task = () -> {
            int index2 = findIndex(mapData.getNameTextures(), textureBox.getSelected());
            if (index2 == -1) return;

            var extension = mapData.getTexturePaths()[index2].split("\\.");
            mapData.setTexturePaths(remove(mapData.getTexturePaths(), index2));
            mapData.setNameTextures(remove(mapData.getNameTextures(), index2));

            int index1 = findIndex(resources.getTextureNames(), textureBox.getSelected()
                    + (extension.length > 1 ? "." + extension[1] : ""));
            if (index1 == -1) {
                Gdx.app.error("removeTexture", "Texture with the same name was not found in the resources");
                return;
            }
            resources.setTextureNames(remove(resources.getTextureNames(), index1));
            resources.setTextures(remove(resources.getTextures(), index1));

            TextureAtlas.AtlasRegion result = null;
            for (var region : mapData.getAtlas().getRegions()) {
                if (region.name.equals(textureBox.getSelected())) {
                    result = region;
                }
            }
            if (result == null) {
                Gdx.app.error("removeTexture", "When trying to delete a region from atalas, it is not found");
                return;
            }

            // clear allocated memory
            TextureAtlas.AtlasRegion finalResult = result;
            Gdx.app.postRunnable(() -> finalResult.getTexture().dispose());

            mapData.getAtlas().getRegions().removeValue(result, true);
            textureBox.setItems(mapData.getNameTextures());
            Gdx.app.postRunnable(() -> activity.getModdingView().update());
        };
        RdApplication.self().execute(task);
    }

    private void loadAtlas(FileHandle handle, Array<FileHandle> atlasPngs, CallListener callListener) {
        var desc = new AssetDescriptor<>(handle, TextureAtlas.class);
        var loader = new ResourcesLoader(Files.FileType.Absolute);
        loader.getAssetManager().load(desc);
        mapData.setAtlasPath(
                String.format("%s/map%d/%s", ChessConstants.MAPS_DIRECTORY,
                        mapData.getId(),  handle.name()));

        AtomicBoolean thread1 = new AtomicBoolean(false),
                thread2 = new AtomicBoolean(false);

        Runnable task = () -> {
            resources.setAtlasDesc(handle.readBytes());
            resources.setAtlasDescName(handle.name());

            var atlasPngBytes = new byte[atlasPngs.size][];
            var atlasPngNames = new String[atlasPngs.size];
            for (int i = 0; i < atlasPngs.size; i++) {
                atlasPngBytes[i] = atlasPngs.get(i).readBytes();
                atlasPngNames[i] = atlasPngs.get(i).name();
            }
            resources.setAtlasPngNames(atlasPngNames);
            resources.setAtlasPng(atlasPngBytes);

            thread1.set(true);
            if (thread1.get() && thread2.get()) {
                Gdx.app.postRunnable(() -> {
                    activity.getModdingView().update();
                    callListener.call();
                });
            }
        };
        RdApplication.self().execute(task);

        loader.setOnFinish(() -> {
            Runnable finishTask = () -> {
                var atlas = loader.getAssetManager().get(desc);
                var regions = removeTextureRegions();

                mapData.getNameTextures();
                mapData.setAtlas(atlas);
                for (var region : regions) {
                    mapData.getAtlas().addRegion(region.name, region);
                }

                thread2.set(true);
                if (thread1.get() && thread2.get()) {
                    Gdx.app.postRunnable(() -> {
                        activity.getModdingView().update();
                        callListener.call();
                    });
                }
            };
            RdApplication.self().execute(finishTask);
        });
        loader.launchLoad();
    }

    private void removeAtlas(RdSelectBox<String> atlasRegions) {
        Runnable task = () -> {
            var regions = removeTextureRegions();
            var atlas = new TextureAtlas();
            for (var region : regions) {
                atlas.addRegion(region.name, region);
            }
            var copy = mapData.getAtlas();

            mapData.setAtlasPath(null);
            mapData.setAtlas(atlas);

            resources.setAtlasDesc(null);
            resources.setAtlasPng(null);
            resources.setAtlasDescName(null);
            resources.setAtlasPngNames(null);

            Gdx.app.postRunnable(() -> {
                atlasRegions.setItems(getAtlasElements());
                // clear allocated memory
                copy.dispose();
                activity.getModdingView().update();
            });
        };
        RdApplication.self().execute(task);
    }

    private void loadScenarioIcon(FileHandle handle, int index, CallListener callListener) {
        var desc = new AssetDescriptor<>(handle, Texture.class);
        var loader = new ResourcesLoader(Files.FileType.Absolute);
        loader.getAssetManager().load(desc);

        mapData.getScenarioIconPaths()[index] =
                String.format("%s/map%d/%s", ChessConstants.MAPS_DIRECTORY,
                        mapData.getId(),  handle.name());

        AtomicBoolean thread1 = new AtomicBoolean(false),
                thread2 = new AtomicBoolean(false);

        Runnable task = () -> {
            resources.setTextures(Arrays.copyOf(resources.getTextures(),
                    resources.getTextures().length + 1));
            resources.getTextures()[resources.getTextures().length - 1] = handle.readBytes();

            resources.setTextureNames(Arrays.copyOf(resources.getTextureNames(),
                    resources.getTextureNames().length + 1));
            resources.getTextureNames()[resources.getTextureNames().length - 1]
                    = handle.name();
            thread1.set(true);

            if (thread1.get() && thread2.get()) {
                Gdx.app.postRunnable(() -> {
                    activity.getModdingView().update();
                    callListener.call();
                });
            }
        };
        RdApplication.self().execute(task);

        loader.setOnFinish(() -> {
            var texture = loader.getAssetManager().get(desc);
            texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            var drawable = new TextureRegionDrawable(texture);
            var disposable = mapData.getDisposableMap().remove(mapData.getScenarioIcons()[index]);
            mapData.getDisposableMap().put(drawable, texture);

            mapData.getScenarioIcons()[index] = drawable;
            thread2.set(true);

            if (thread1.get() && thread2.get()) {
                Gdx.app.postRunnable(() -> {
                    DisposeUtil.dispose(disposable);
                    activity.getModdingView().update();
                    callListener.call();
                });
            }
        });
        loader.launchLoad();
    }

    private void removeScenarioIcon(int index, CallListener listener) {
        Runnable task = () -> {
            if (mapData.getScenarioIconPaths()[index] != null) {
                int index2 = findIndex(resources.getTextureNames(),
                        Gdx.files.getFileHandle(mapData.getScenarioIconPaths()[index], mapData.getType()).name());
                if (index2 == -1) {
                    Gdx.app.error("removeScenarioIcon", "scenario icon don't found in resource icons");
                } else {
                    resources.setTextureNames(remove(resources.getTextureNames(), index2));
                    resources.setTextures(remove(resources.getTextures(), index2));
                }
            }

            var disposable = mapData.getDisposableMap().remove(mapData.getScenarioIcons()[index]);
            mapData.getScenarioIconPaths()[index] = null;
            mapData.getScenarioIcons()[index] = new TextureRegionDrawable(
                    ChessAssetManager.current().findChessRegion("cross"));

            Gdx.app.postRunnable(() -> {
                DisposeUtil.dispose(disposable);
                listener.call();
            });
        };
        RdApplication.self().execute(task);
    }

    // --------------------------------------------------------------------

    private String getRanked(int index) {
        if (index == -1) return "null";
        return String.valueOf(mapData.isRatingScenario(index));
    }

    private String getScenarioIcon(int index) {
        if (mapData.getScenarioIconPaths()[index] == null) return "null";
        return Gdx.files.getFileHandle(mapData.getScenarioIconPaths()[index], mapData.getType()).nameWithoutExtension();
    }

    private String[] getScenariosList() {
        if (mapData.getScenarios().length == 0) return new String[]{};

        var scenariosList = new String[mapData.getScenarios().length];
        for (int i = 0; i < mapData.getScenarios().length; i++) {
            scenariosList[i] = String.valueOf(i + 1);
        }
        return scenariosList;
    }

    private int getScenarioIndex(RdSelectBox<String> scenarios) {
        if (scenarios.getItems().size < 1) return -1;
        return Integer.parseInt(scenarios.getSelected()) - 1;
    }

    private String getScenarioConf(int index) {
        if (index == -1) return "";
        return mapData.getScenarios()[index];
    }

    // lang is short title language!
    private void putString(String lang, String key, String value) {
        lang = ChessApplication.self().getLanguages().get(ChessApplication.self().getDisplayLanguages().indexOf(lang));

        var langStrings = resources.getStrings()
                .get(lang);
        langStrings.put(key, value);
    }

    private String[] getMapLanguages() {
        // short titles languages
        var keys = resources.getStrings().keys();
        // long titles languages
        var data = new String[resources.getStrings().size];

        for (int i = 0; i < data.length; i++) {
            var current = keys.next();
            var index = ChessApplication.self().getLanguages().indexOf(current);
            data[i] = ChessApplication.self().getDisplayLanguages().get(index);
        }

        return data;
    }

    // lang is short title language!
    private String getString(String lang, String key) {
        lang = ChessApplication.self().getLanguages().get(ChessApplication.self().getDisplayLanguages().indexOf(lang));

        if (!resources.getStrings().containsKey(lang)) return "null";
        var result = resources.getStrings().get(lang).get(key);
        if (result == null) return "null";
        return result;
    }

    private void showConfirmation(String text, CallListener callListener) {
        var conf = new RdDialogBuilder()
                .title(strings.get("confirmation"))
                .text(text)
                .cancel(strings.get("cancel"))
                .accept(strings.get("accept"), new OnChangeListener() {
                    @Override
                    public void onChange(Actor actor) {
                        callListener.call();
                        activity.getConfirmation().hide();
                    }
                })
                .build(ChessAssetManager.current().getSkin(), "input");

        conf.getIcon().setDrawable(new TextureRegionDrawable(
                ChessAssetManager.current().findRegion("ib_help")));
        conf.getIcon().setScaling(Scaling.fit);
        conf.show(activity.getStage());
        conf.setSize(800, 550);

        activity.setConfirmation(conf);
        activity.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private void showError(String text, boolean visible, CallListener callListener) {
        var error = new RdDialogBuilder()
                .title(strings.get("error"))
                .text(text)
                .accept(strings.get("accept"), new OnChangeListener() {
                    @Override
                    public void onChange(Actor actor) {
                        callListener.call();
                        activity.getError().hide();
                        activity.getBlackout().setVisible(visible);
                    }
                })
                .build(ChessAssetManager.current().getSkin(), "input");

        error.getIcon().setDrawable(new TextureRegionDrawable(
                ChessAssetManager.current().findRegion("icon_error")));

        error.getIcon().setScaling(Scaling.fit);
        error.show(activity.getStage());
        activity.getBlackout().setVisible(true);
        error.setSize(700, 550);

        activity.setError(error);
        activity.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private void showSelector(Consumer<FileHandle> onSelect, String... extensions) {
        var selector = new FileSelectorBuilder()
                .title(strings.get("file_selector"))
                .endFilters(extensions)
                .select(strings.get("select"), onSelect)
                .cancel(strings.get("cancel"))
                .build(ChessAssetManager.current().getSkin());

        selector.show(activity.getStage());
        selector.setSize(900, 800);
        activity.setSelector(selector);
        activity.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private Pair<Boolean, Array<FileHandle>> getAtlasPngs(FileHandle atlasHandle) {
        var data = new String(atlasHandle.readBytes());
        var atlasPngs = new Array<FileHandle>();

        for (var line : data.split("\n")) {
            if (line.endsWith(".png")) {
                atlasPngs.add(atlasHandle.parent().child(line));
            }
        }

        for (var handle : atlasPngs) {
            if (!handle.exists()) return new Pair<>(false, atlasPngs);
        }

        return new Pair<>(true, atlasPngs);
    }

    private String[] getAtlasElements() {
        var textures = getTextureRegions();
        var data = new ArrayList<String>();
        for (int i = 0; i < mapData.getAtlas().getRegions().size; i++) {
            if (textures.contains(mapData.getAtlas().getRegions().get(i), true)) continue;
            data.add(mapData.getAtlas().getRegions().get(i).name);
        }
        return data.toArray(new String[0]);
    }

    private Array<TextureAtlas.AtlasRegion> getTextureRegions() {
        var regions = new Array<TextureAtlas.AtlasRegion>();
        for (var name : mapData.getNameTextures()) {
            for (var region : mapData.getAtlas().getRegions()) {
                if (region.name.equals(name)) {
                    regions.add(region);
                }
            }
        }
        return regions;
    }

    private Array<TextureAtlas.AtlasRegion> removeTextureRegions() {
        var regions = new Array<TextureAtlas.AtlasRegion>();
        for (var name : mapData.getNameTextures()) {
            for (var region : mapData.getAtlas().getRegions()) {
                if (region.name.equals(name)) {
                    regions.add(region);
                }
            }
        }

        for (var region : regions) {
            mapData.getAtlas().getRegions()
                    .removeValue(region, true);
            mapData.getAtlas().getTextures()
                            .remove(region.getTexture());
        }

        return regions;
    }

    private byte[][] remove(byte[][] data, int index) {
        byte[][] copyData = new byte[data.length - 1][];

        System.arraycopy(data, 0, copyData, 0, index);
        System.arraycopy(data, index + 1, copyData, index, data.length - index - 1);

        return copyData;
    }

    private String[] remove(String[] data, int index) {
        var copyData = new String[data.length - 1];

        System.arraycopy(data, 0, copyData, 0, index);
        System.arraycopy(data, index + 1, copyData, index, data.length - index - 1);

        return copyData;
    }

    private Drawable[] remove(Drawable[] data, int index) {
        var copyData = new Drawable[data.length - 1];

        System.arraycopy(data, 0, copyData, 0, index);
        System.arraycopy(data, index + 1, copyData, index, data.length - index - 1);

        return copyData;
    }

    private int findIndex(String[] arr, String found) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(found)) {
                return i;
            }
        }
        return -1;
    }

    private boolean contains(String[] arr, String found) {
        for (var el : arr) {
            if (el.equals(found)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkAtlasNames(FileHandle handle) {
        var data = handle.readString();
        for (var el : data.split("\n")) {
            if (!el.contains(":") && reservedNames.contains(el)) {
                return false;
            }
        }

        return true;
    }
}
