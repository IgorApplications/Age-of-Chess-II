package com.iapp.lib.ui.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.iapp.lib.ui.actors.RdDialog;
import com.iapp.lib.ui.actors.RdTable;
import com.iapp.lib.util.DisposeUtil;
import com.iapp.lib.util.Pair;
import com.iapp.lib.util.RdI18NBundle;
import com.iapp.lib.util.WindowUtil;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * The core of the library for rendering graphics and working with resources.
 * Automatically logs to the emergency window all errors that
 * have occurred in the graphics thread.
 * @author Igor Ivanov
 * @version 1.0
 * */
public abstract class RdApplication implements ApplicationListener {

    /** library resources */
    private final RdAssetManager rdAssetManager;
    /** virtual size of the application world */
    private final float minAppWidth, minAppHeight;
    /** standard input */
    private final InputMultiplexer input;
    /** application viewport */
    private ExtendViewport viewport;
    /** string storage in different languages */
    private RdI18NBundle strings;
    /** the current screen being drawn */
    private Activity current;
    /** number of frames per second */
    private int fps = 60;
    /** Screen last render time */
    private long lastRender;
    /** rendering period of one frame */
    private float periodRender;
    /** viewport zoom; by default Desktop = 1.5 */
    private float zoom = 1.6f;
    /** Default zoom initialization */
    private boolean defaultZoom = true;
    /** application scene for 2d rendering */
    private Stage stage;
    /** app background color */
    private Color backgroundColor = Color.BLACK;
    /** storage of dialog boxes that are independent of the activity */
    private final List<Pair<RdDialog, Consumer<RdDialog>>> dialogList = new ArrayList<>();
    /** default application cursor */
    private Cursor defCursor;
    /** cross-platform implementations of platform calls */
    private final Launcher launcher;
    /** directory type for logging */
    private Files.FileType logType;
    /** path to logging directory */
    private String logPath;
    /** list of screen-independent actors, added on top of the rest */
    private final List<Actor> topActors = new ArrayList<>();

    /** @return the application core from anywhere */
    public static RdApplication self() {
        return (RdApplication) Gdx.app.getApplicationListener();
    }

    /** running a task on the library's thread pool */
    public void execute(Runnable task) {
        launcher.execute(task);
    }

    /** sends a task to the graphics thread in a crash-proof form */
    public static void postRunnable(Runnable runnable) {
        Gdx.app.postRunnable(() -> {
            try {
                runnable.run();
            } catch (Throwable t) {
                Gdx.app.error("postRunnable", RdLogger.self().getDescription(t));
                RdLogger.self().showFatalScreen(t);
            }
        });
    }

    /**
     * Creates an application instance
     * @param minAppWidth - minimum virtual screen width
     * @param minAppHeight - minimum virtual screen height
     * @param rdAssetManager - resource loader
     * @param launcher - cross-platform implementations of platform calls
     * */
    public RdApplication(Launcher launcher, float minAppWidth, float minAppHeight,
                         RdAssetManager rdAssetManager, int parallelThreads) {
        this.rdAssetManager = rdAssetManager;
        this.minAppWidth = minAppWidth;
        this.minAppHeight = minAppHeight;
        input = new InputMultiplexer();
        this.launcher = launcher;
        launcher.initPool(parallelThreads);
    }

    /**
     * Creates an application instance
     * @param minAppWidth - minimum virtual screen width
     * @param minAppHeight - minimum virtual screen height
     * @param rdAssetManager - resource loader
     * @param zoom - viewport zoom
     * @param launcher - cross-platform implementations of platform calls
     * */
    public RdApplication(Launcher launcher, float minAppWidth, float minAppHeight,
                         RdAssetManager rdAssetManager, int parallelThreads, float zoom) {
        this.rdAssetManager = rdAssetManager;
        this.minAppWidth = minAppWidth;
        this.minAppHeight = minAppHeight;
        input = new InputMultiplexer();
        this.zoom = zoom;
        defaultZoom = false;
        this.launcher = launcher;
        launcher.initPool(parallelThreads);
    }

    /**  */
    public void setLogHandle(Files.FileType type, String path) {
        if (logType == null) replaceLogOutput();
        logType = type;
        logPath = path;

    }

    /** sets default application cursor */
    public void setCursor(Cursor cursor) {
        defCursor = cursor;
        Gdx.graphics.setCursor(cursor);
    }

    /** returns cross-platform implementations of platform calls */
    public Launcher getLauncher() {
        return launcher;
    }

    /** returns default application cursor */
    public Cursor getCursor() {
        return defCursor;
    }

    /** returns string storage in different languages */
    public RdI18NBundle getStrings() {
        return strings;
    }

    /** sets string storage in different languages */
    public void setStrings(RdI18NBundle strings) {
        this.strings = strings;
    }

    /** returns library resource manager */
    public RdAssetManager getAssetManager() {
        return rdAssetManager;
    }

    /** returns the viewport of the application */
    public Viewport getViewport() {
        return viewport;
    }

    /** returns application scene for 2d rendering */
    public Stage getStage() {
        return stage;
    }

    /** returns standard input */
    public InputMultiplexer getInput() {
        return input;
    }

    /** returns the minimum virtual application width */
    public float getMinAppWidth() {
        return minAppWidth;
    }

    /** returns the minimum virtual application height */
    public float getMinAppHeight() {
        return minAppHeight;
    }

    /** returns number of frames per second */
    public int getFps() {
        return fps;
    }

    /** sets number of frames per second */
    public void setFps(int fps) {
        this.fps = fps;
        periodRender = 1000f / fps;
    }

    /** returns list of screen-independent actors, added on top of the rest */
    public List<Actor> getTopActors() {
        return topActors;
    }

    /** returns app background color */
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /** sets app background color */
    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }


    /** returns zoom viewport; by default Desktop = 1.5  */
    public float getZoom() {
        return zoom;
    }

    /** sets zoom viewport */
    public void setZoom(float zoom) {
        this.zoom = zoom;
    }


    /** Application launch */
    public abstract void launch(RdAssetManager rdAssetManager);

    /** @see RdApplication#render() */
    public void renderApp() {}

    /** @see RdApplication#resize(int, int) */
    public void resizeApp(int width, int height) {}

    /** @see RdApplication#pause() */
    public void pauseApp() {}

    /** @see RdApplication#resume()  */
    public void resumeApp() {}

    /**
     * adds a listener to the pool, called each time the window is resized;
     * Automatically deleted when hide is called (isHidden)
     * @return index - a pointer to a place in the pool
     * */
    public int addDialog(RdDialog dialog, Consumer<RdDialog> resize) {
        dialogList.add(new Pair<>(dialog, resize));
        return dialogList.size() - 1;
    }

    /**
     * resizes the dialog box each time the window is resized using the following formula:
     * width = min(worldWidth - padX, maxWidth),
     * height = min(worldHeight - padY, maxHeight);
     * Automatically deleted when hide is called (isHidden)
     * @return index - a pointer to a place in the pool
     * */
    public int addDialog(RdDialog dialog, float maxWidth, float maxHeight, float padX, float padY) {
        int index = addDialog(dialog, current -> {
            Viewport viewport = RdApplication.self().getViewport();
            if (current != null) {
                current.setWidth(Math.min(viewport.getWorldWidth() - padX, maxWidth));
                current.setHeight(Math.min(viewport.getWorldHeight() - padY, maxHeight));
            }
            WindowUtil.resizeCenter(current);
        });
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        return index;
    }

    /** Cleans up application resources */
    @Override
    public void dispose() {
        DisposeUtil.dispose(stage);
        rdAssetManager.dispose();
        pushLogs();
        Gdx.app.log("dispose", "Application finished");
    }

    /**
     * switches application screens.
     * With automatic cleaning {@link Screen#dispose}
     * @param next - render object
     * */
    public void setScreen(Activity next, Action... actions) {
        if (RdLogger.blockedTransition) return;

        SequenceAction sequence = new SequenceAction();
        Gdx.input.setOnscreenKeyboardVisible(false);
        InputProcessor inputProcessor = Gdx.input.getInputProcessor();
        Gdx.input.setInputProcessor(null);

        Runnable intent = () -> {
            stage.clear();

            if (current != null) {
                current.dispose();
            }

            if (next != null) {
                next.show(current);
                next.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            }
            current = next;

            for (Actor actor : topActors) {
                getStage().addActor(actor);
            }
            Gdx.input.setInputProcessor(inputProcessor);
        };

        if (current != null) {
            Actor anim = current.hide(sequence, next);

            if (anim == null) intent.run();
            else {
                for (Action el : actions) {
                    sequence.addAction(el);
                }
                sequence.addAction(Actions.run(intent));
                anim.addAction(sequence);
            }
        } else {
            intent.run();
        }
    }

    void setFatalScreen(Activity next) {
        stage.getActors().clear();
        stage.getRoot().clearActions();
        stage.getRoot().getColor().a = 1;

        if (current != null) {
            current.dispose();
        }

        if (next != null) {
            next.show(current);
            next.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
        current = next;
    }

    /**
     * The method initializes the application resources
     * and called once when the application is created
     * */
    @Override
    public final void create() {
        try {
            readVersion();
            if (defaultZoom && (Gdx.app.getType() == Application.ApplicationType.Android
                    || Gdx.app.getType() == Application.ApplicationType.iOS)) {
                zoom = 1f;
            }
            viewport = new ExtendViewport(minAppWidth * zoom, minAppHeight * zoom);
            viewport.setScaling(Scaling.contain);
            stage = new Stage(viewport);
            input.addProcessor(stage);
            Gdx.input.setInputProcessor(input);

            // instant start
            launch(rdAssetManager);
            // logging and configuration in a background thread
            Runnable task = () -> {
                Gdx.app.log("launch", "Application launched");

                RdLogger.self().logSysInfo();
            };
            execute(task);
        } catch (Throwable t) {
            Gdx.app.error("launch", RdLogger.self().getDescription(t));
            RdLogger.self().showFatalScreen(t);
        }
    }

    /** called when the application is rendering */
    @Override
    public final void render() {
        try {
            ScreenUtils.clear(backgroundColor);
            if (System.currentTimeMillis() - lastRender < periodRender) {
                var sleepTime = (long) (periodRender - (System.currentTimeMillis() - lastRender));
                Thread.sleep((long) (sleepTime * 0.8f));
            }

            Batch batch = stage.getBatch();
            if (batch.isDrawing()) batch.end();
            stage.act(Gdx.graphics.getDeltaTime());
            stage.draw();
            renderApp();

            if (current != null) current.render(Gdx.graphics.getDeltaTime());
            lastRender = System.currentTimeMillis();
        } catch (Throwable t) {
            Gdx.app.error("render", RdLogger.self().getDescription(t));
            RdLogger.self().showFatalScreen(t);
        }
    }

    /** called when the application window is resized
     * @param width - window width
     * @param height - window height
     * */
    @Override
    public final void resize(int width, int height) {
        try {
            resizeApp(width, height);
            viewport.update(width, height, true);

            dialogList.removeIf(pair -> pair.getKey().isHidden());
            for (var pair : dialogList) {
                var dialog = pair.getKey();
                pair.getValue().accept(dialog);
            }

            if (current != null) current.resize(width, height);
        } catch (Throwable t) {
            Gdx.app.error("resize", RdLogger.self().getDescription(t));
            RdLogger.self().showFatalScreen(t);
        }
    }

    /** called when the application goes into the background */
    @Override
    public  void pause() {
        try {
            if (current != null) current.pause();
            pauseApp();
        } catch (Throwable t) {
            Gdx.app.error("pause", RdLogger.self().getDescription(t));
            RdLogger.self().showFatalScreen(t);
        }
    }

    /** called when the application exits the background **/
    @Override
    public void resume() {
        try {
            if (current != null) current.resume();
            resumeApp();
        } catch (Throwable t) {
            Gdx.app.error("resume", RdLogger.self().getDescription(t));
            RdLogger.self().showFatalScreen(t);
        }
    }

    private final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
    private final ByteArrayOutputStream stderr = new ByteArrayOutputStream();

    private void replaceLogOutput() {
        System.setOut(new PrintStream(stdout));
        System.setErr(new PrintStream(stderr));
    }

    private void pushLogs() {
        if (logType != null) {
            Gdx.files.getFileHandle(logPath, logType).child("stdout.txt")
                .writeBytes(stdout.toByteArray(), false);
            Gdx.files.getFileHandle(logPath, logType).child("stderr.txt")
                .writeBytes(stderr.toByteArray(), false);
        }
    }

    private void readVersion() {
        String result = Gdx.files.internal("version").readString();
        String[] tokens = result.split("\n");
        String stringVersion = tokens[1].split("=")[1].replace(".", "");
        RdLogger.self().setVersion(Integer.parseInt(stringVersion));
        RdLogger.self().setTime(tokens[0].split("=")[1]);
    }
}
