package com.iapp.ageofchess.activity;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.iapp.ageofchess.ChessApplication;
import com.iapp.ageofchess.controllers.GuideController;
import com.iapp.ageofchess.services.ChessAssetManager;
import com.iapp.ageofchess.services.ChessConstants;
import com.iapp.lib.ui.actors.*;
import com.iapp.lib.ui.screens.Activity;
import com.iapp.lib.util.OnChangeListener;
import com.iapp.lib.util.TasksLoader;
import com.iapp.lib.util.TransitionEffects;

public class GuideActivity extends Activity {

    private final GuideController controller;
    private RdImageTextButton back;
    private WindowGroup windowGroup;
    private RdWindow window;
    private RdTable descContent;

    public GuideActivity() {
        controller = new GuideController(this);
    }

    @Override
    public void initActors() {
        back = new RdImageTextButton(strings.get("[i18n]Back"), "red_screen");
        back.setImage("ib_back");
    }

    @Override
    public void initListeners() {
        ChessApplication.self().getLauncher().setOnFinish(controller::goToMenu);
        back.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                controller.goToMenu();
            }
        });
    }

    @Override
    public void show(Stage stage, Activity last) {
        Image background = new Image(new TextureRegionDrawable(
            ChessAssetManager.current().findChessRegion("menu_background")));
        background.setFillParent(true);
        background.setScaling(Scaling.fill);
        getStage().addActor(background);

        var content = new RdTable();
        content.setFillParent(true);
        getStage().addActor(content);

        window = new RdWindow("", "screen_window");
        window.setMovable(false);
        PropertyTable properties = new PropertyTable(400);
        window.add(properties).expand().fill();

        properties.setVisibleBackground(false);
        properties.add(new PropertyTable.Title(strings.get("[i18n]Guide")));

        descContent = new RdTable();
        descContent.padBottom(30);
        descContent.align(Align.topLeft);

        properties.getContent().add(descContent).expand().fill().pad(15, 10, 15, 10);
        window.getLoading().setVisible(true);
        window.getLoading().setLoadingText("[i18n]building...");

        windowGroup = new WindowGroup(window, back);
        ChessApplication.self().updateTitle(windowGroup, strings.get("[i18n]Single Player"));

        windowGroup.setFillParent(true);
        stage.addActor(windowGroup);
        windowGroup.update();

        TransitionEffects.transitionBottomShow(windowGroup, ChessConstants.localData.getScreenDuration());
        loadText();
    }


    private void loadText() {
        TasksLoader loader = new TasksLoader();

        loader.addTask(() -> {
            addText(strings.get("[i18n][*]Rules"));
            addText(strings.get("[i18n]%t1) Rules for the acceptable use of the application, primarily for the 'Multiplayer' mode%n%tThe rules are designed to create comfortable conditions for all users. Observe them when using the application. Please review these rules from time to time as they may change."));
        });

        loader.addTask(() ->
            addText(strings.get("[i18n]%t2) Spamming and commercial exploitation%n%tDo not send spam, commercial messages in large quantities or automatic messages and do not promote them. Do not use calls for the same purpose. Spam includes, in particular, mass mailings of an advertising or commercial nature without the consent of the recipients, as well as the imposition of services. Comply with laws that prohibit solicitation of goods and services over the phone.")));

        loader.addTask(() ->
            addText(strings.get("[i18n]%t3) Protect your rights and stay safe%n%tTo report a user, click on the 'complaint' button in the account of the user you think has violated the rules. Our moderators will take immediate action to enforce the rules. Also, your complaints will be saved and the moderators will inform you about their status. We would appreciate your vigilance.")));

        loader.addTask(() ->
            addText(strings.get("[i18n]%t4) Fraud, phishing and other deceptive activities%n%tYou must not use Age of Chess for phishing attacks, unauthorized collection of confidential information (including passwords, bank card information and social security numbers). You may not use the application to deceive, mislead or misuse other users' data.%n%tYou may not impersonate another person or provide false information about yourself or the user sending the message.")));

        loader.addTask(() ->
            addText(strings.get("[i18n]%t5) Malicious software%n%tYou must not distribute malware, viruses, Trojan horses, corrupted files, dangerous code or other materials that may interfere with the operation of IgorApplications networks, servers and other infrastructure.")));

        loader.addTask(() ->
            addText(strings.get("[i18n]%t6) Child Safety%n%tYou must not use Age of Chess in any way that endangers children. This includes any form of representation of minors in a sexual context and any material of a sexual nature that depicts children. IgorApplications has a zero tolerance policy for such content. In this case, the account will be blocked, and all materials will be deleted immediately.")));

        loader.addTask(() ->
            addText(strings.get("[i18n]%t7) Harassment%n%tYou must not use Age of Chess to harass, intimidate or threaten. It is forbidden to invite other users to participate in such activities.")));

        loader.addTask(() ->
            addText(strings.get("[i18n]%t8) Personal and Confidential Information%n%tDo not distribute sensitive third party information, such as social security and credit card numbers or account passwords, without the permission of their respective owners.")));

        loader.addTask(() ->
            addText(strings.get("[i18n]%t9) Illegal Activities%n%tYou must not use Age of Chess to promote, organize, or commit any illegal activity.")));

        loader.addTask(() ->
            addText(strings.get("[i18n]%t10) Communication%n%tSwearing, insults, slander are prohibited. If you prevent other users from using this application by exerting pressure on them, we will take action and apply penalties to your account.")));

        loader.addTask(() ->
            addText(strings.get("[i18n]%t11) Collection of personal data%n%tAge of Chess to ensure the functionality and security of the application collects such personal data as: the password specified by YOU, the name specified by YOU for the operation of the multiplayer. The rest of the data is optional, such as: gender, country, date of birth. We do not pass this data on to third parties. Also, we store the password specified by YOU in encrypted form. All messages and account information are stored in the database in an unencrypted form.")));

        loader.addTask(() ->
            addText(strings.get("[i18n]%t12) Policy Enforcement%n%tIgorApplications may ban accounts whose owners violate the policies. If you think that your account was blocked by mistake, please contact the developers at igorapplications@gmail.com. Sincerely, IgorApplications.%n")));

        loader.addTask(() ->
            addText(strings.get("[i18n][*]Credits")));

        loader.addTask(() ->
            addText(strings.get("[i18n]%t[p] Developer - {LINK=http://185.112.101.253:8080/}[_]Igor Ivanov[]{ENDLINK} (IgorApplications)%n%t[p] UI designer - {LINK=http://185.112.101.253:8080/}[_]Igor Ivanov[]{ENDLINK} (IgorApplications)%n%t[p] UX designer - {LINK=http://185.112.101.253:8080/}[_]Igor Ivanov[]{ENDLINK} (IgorApplications)%n%t[p] Modding - {LINK=http://185.112.101.253:8080/}[_]Igor Ivanov[]{ENDLINK} (IgorApplications)%n%t[p] Tester - [_]Egor Belomestnykh[]%n%t[p] Tester - [_]Artur Kozhevnikov[] (Kreeg)%n%t[p] Tester - [_]Timur[]%n%t[p] Tester - [_]Movsar[]%n%t[p] Musical author - [_]Alexander Nakarada[]%n%t[p] Chess pieces - https://pixabay.com/images/search/%n%t[p] Icons & fonts - https://m2.material.io/design%n%t[p] Icons - https://www.iconarchive.com%n%t[p] Icons - https://pngtree.com%n%t[p] Icons - https://www.iconarchive.com/")));

        loader.addTask(() ->
            addText(strings.get("[i18n][*]Technical support")));

        loader.addTask(() ->{
                addText(strings.get("[i18n]%t[p] Programming language - Java%n%t[p] Rendering - libGDX framework (Mario Zechner & team)%n%t[p] Fonts - TextraTypist library (Tommyettinger)%n%t[p] Chess AI - Carballo library%n%t[p] Server - Spring framework%n%t[p] Database - PostgreSQL%n%t[p] Server logging - SLF4J%n%t[p] Version control - GitHub%n%t[p] Build system - Gradle%n%t[p] Build system - Maven%n%t[p] Development environment - Itellij IDEA%n%t[p] Desktop runtime - Java%n%t[p] Desktop OpenGl - LWJGL%n%t[p] Desktop OpenGL ES driver - ANGLE%n%t[p] App Installer - Inno Setup Compiler%n%t[p] Android runtime - Android%n%t[p] iOS runtime - RoboVM"));
            });

        loader.setOnFinish(() ->
            window.getLoading().setVisible(false));
        loader.loadFinish();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        windowGroup.update();
    }


    private void addText(String data) {
        var label = new RdLabel(data);
        label.setWrap(true);
        descContent.add(label).expandX().fillX().row();
    }

    @Override
    public Actor hide(SequenceAction action, Activity next) {
        TransitionEffects.transitionBottomHide(action, windowGroup, ChessConstants.localData.getScreenDuration());
        return windowGroup;
    }
}
