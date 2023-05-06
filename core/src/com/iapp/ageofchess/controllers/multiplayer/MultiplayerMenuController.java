package com.iapp.ageofchess.controllers.multiplayer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.iapp.ageofchess.ChessApplication;
import com.iapp.ageofchess.activity.MenuActivity;
import com.iapp.ageofchess.activity.multiplayer.MultiplayerGamesActivity;
import com.iapp.ageofchess.activity.multiplayer.MultiplayerMenuActivity;
import com.iapp.ageofchess.activity.multiplayer.MultiplayerScenariosActivity;
import com.iapp.ageofchess.graphics.AvatarView;
import com.iapp.ageofchess.multiplayer.*;
import com.iapp.ageofchess.multiplayer.webutil.RequestStatus;
import com.iapp.ageofchess.util.ChessAssetManager;
import com.iapp.ageofchess.util.ChessConstants;
import com.iapp.ageofchess.util.SettingsUtil;
import com.iapp.rodsher.actors.*;
import com.iapp.rodsher.screens.Controller;
import com.iapp.rodsher.screens.RdApplication;
import com.iapp.rodsher.util.OnChangeListener;
import com.iapp.rodsher.util.WindowUtil;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;

public class MultiplayerMenuController extends Controller {

    private static final String[] MONTHS = new String[]{"m", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};

    @SuppressWarnings("SimpleDateFormat")
    private final SimpleDateFormat formatter = new SimpleDateFormat("d MMMM yyyy", ChessConstants.localData.getLocale());
    private final MultiplayerMenuActivity activity;

    public MultiplayerMenuController(MultiplayerMenuActivity activity) {
        super(activity);
        this.activity = activity;

        ChessApplication.self().getAccountPanel()
            .initListeners(ChessConstants.loggingAcc,
                this::seeAccount,
                this::editAccount);
    }

    public void goToMenu() {
        startActivity(new MenuActivity());
    }

    public void goToGames() {
        startActivity(new MultiplayerGamesActivity());
    }

    public void goToScenarios() {
        startActivity(new MultiplayerScenariosActivity());
    }

    private void seeAccount(long id) {
        var dialog = showWatchingDialog();

        MultiplayerEngine.self().getAccount(id, account -> {
            updateWatchingDialog(account, dialog, false);
        });
    }

    public void editAccount(long id) {
        var dialog = showWatchingDialog();

        MultiplayerEngine.self().getAccount(id, account -> {
            updateWatchingDialog(account, dialog);
        });
    }

    public void editAccount(Message message) {
        var dialog = showWatchingDialog();
        dialog.getLoading().setVisible(true);

        MultiplayerEngine.self().getAccount(message.getSenderId(), account -> {
            updateWatchingDialog(account, dialog);
        });

    }

    private RdDialog showWatchingDialog() {
        var dialog = new RdDialog(strings.get("view_acc"), "input");
        dialog.getLoading().setVisible(true);

        dialog.show(activity.getStage());
        dialog.setSize(900, 900);

        ChessApplication.self().addDialog(dialog, localDialog -> {
            var viewport = RdApplication.self().getViewport();
            if (localDialog != null) localDialog.setHeight(viewport.getWorldHeight() - 30);
            WindowUtil.resizeCenter(localDialog);
        });
        RdApplication.self().resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        return dialog;
    }

    private void updateWatchingDialog(Account publicAcc, RdDialog dialog) {
        updateWatchingDialog(publicAcc, dialog, true);
    }

    private void updateWatchingDialog(Account publicAcc, RdDialog dialog, boolean edit) {

        boolean self = publicAcc.getId() == ChessConstants.loggingAcc.getId();
        dialog.getButtonTable().clear();
        dialog.getContentTable().clear();
        var locale = new Locale(ChessConstants.localData.getLocale().getLanguage(), publicAcc.getCountry());

        var table1 = new RdTable();
        var scrollPane = new RdScrollPane(table1, ChessAssetManager.current().getSkin());
        scrollPane.setScrollingDisabled(true, false);

        table1.align(Align.topLeft);
        table1.setBackground(new NinePatchDrawable(
                new NinePatch(ChessApplication.self().getAssetManager().findRegion("dark_pane"),
                        10,10,10,10)));
        var table2 = new RdTable();

        var part1 = new RdTable();
        part1.align(Align.topLeft);
        var part2 = new RdTable();
        part2.align(Align.topLeft);
        var part3 = new RdTable();
        part3.align(Align.topLeft);

        table1.add(part1).expandX().fillX().align(Align.topLeft).row();
        table1.add(part2).expandX().fillX().align(Align.topLeft).padTop(5).row();
        table1.add(part3).expandX().fillX().align(Align.topLeft).padTop(10);

        var name = new RdLabel(publicAcc.getUsername());
        var status = new RdLabel(getAccountStatus(publicAcc));
        var userName = new RdLabel(publicAcc.getFullName());
        var gender = new RdLabel(SettingsUtil.getGenderText(publicAcc.getGender()));
        var birth = new RdLabel("n/d");
        if (publicAcc.getDateBirth() != 0) birth.setText(formatter.format(publicAcc.getDateBirth()));

        var country = new RdLabel(locale.getDisplayCountry(ChessConstants.localData.getLocale()));
        if (publicAcc.getCountry().equals("")) country.setText("n/d");
        var quote = new RdLabel(publicAcc.getQuote());
        quote.setWrap(true);

        // ------------------------------------------------------------------------------------------------------ table2

        var avatar = new AvatarView(ChessAssetManager.current().getAvatarStyle());
        MultiplayerEngine.self().getAvatar(publicAcc, bytes ->
            avatar.update(publicAcc, bytes));

        table2.add(avatar).pad(5, 5, 5, 5);
        table2.align(Align.topLeft);

        if (edit && (self || ChessConstants.loggingAcc.getType().ordinal() >= AccountType.EXECUTOR.ordinal())) {

            avatar.addListener(new OnChangeListener() {
                @Override
                public void onChange(Actor actor) {
                    showUpdatingAvatarView(publicAcc, avatar);
                }
            });
        }

        // ------------------------------------------------------------------------------------------------------- part1

        part1.add(new Image(new TextureRegionDrawable(ChessAssetManager.current().findRegion("iw_account"))));
        part1.add(name).padLeft(5).expandX().fillX().row();

        part1.add(new Image(new TextureRegionDrawable(ChessAssetManager.current().findRegion("iw_name"))));
        part1.add(userName).padLeft(5).expandX().fillX().row();

        part1.add(new Image(new TextureRegionDrawable(ChessAssetManager.current().findRegion("iw_work"))));
        part1.add(status).padLeft(5).expandX().fillX().row();

        part1.add(new Image(new TextureRegionDrawable(ChessAssetManager.current().findRegion("iw_cake"))));
        part1.add(birth).padLeft(5).expandX().fillX().row();

        if (publicAcc.getGender() != Gender.ND) {
            part1.add(getGenderImage(publicAcc));
        } else {
            part1.add(new Image(new TextureRegionDrawable(ChessAssetManager.current().findRegion("iw_face"))));
        }
        part1.add(gender).padLeft(5).colspan(2).expandX().fillX().row();

        part1.add(new Image(new TextureRegionDrawable(ChessAssetManager.current().findRegion("iw_country"))));
        part1.add(country).padLeft(5).expandX().fillX().row();

        part1.add(new Image(new TextureRegionDrawable(ChessAssetManager.current().findRegion("iw_quote"))));
        part1.add(quote).padLeft(5).expandX().fillX().row();

        // ------------------------------------------------------------------------------------------------------ part2

        if (edit && (self || ChessConstants.loggingAcc.getType().ordinal() >= AccountType.DEVELOPER.ordinal())) {

            if (self) {
                part2.add(new Image(ChessAssetManager.current().getWhiteTexture())).expandX()
                        .fillX().height(2).colspan(2);
                part2.row();

                var lineTable1 = new LineTable(strings.get("login_op"));
                lineTable1.add(new RdLabel(strings.get("login") + ": " + publicAcc.getUsername()))
                        .expandX().fillX().row();
                lineTable1.add(new RdLabel(strings.get("password") + "[*]******"))
                        .expandX().fillX();

                part2.add(lineTable1).expandX().fillX().row();
            }

            // -------------------------------------------------------------------------------------------------- part3

            part3.add(new Image(ChessAssetManager.current().getWhiteTexture())).expandX()
                    .fillX().height(2).colspan(2);
            part3.row();

            var lineTable2 = new LineTable(strings.get("change_password"));

            var inputPassword = new RdTextArea("");
            inputPassword.setPrefLines(1);
            inputPassword.setPasswordCharacter('*');
            inputPassword.setPasswordMode(true);
            inputPassword.setMessageText(strings.get("enter_hint"));
            var confPassword = new RdTextArea("");
            confPassword.setPrefLines(1);
            confPassword.setPasswordMode(true);
            confPassword.setPasswordCharacter('*');
            confPassword.setMessageText(strings.get("enter_hint"));
            var changePassword = new RdTextButton(strings.get("change"));

            changePassword.addListener(new OnChangeListener() {
                @Override
                public void onChange(Actor actor) {
                    if (inputPassword.getText().length() < 6) {
                        ChessApplication.self().showInfo(strings.get("min_password"));
                        return;
                    }
                    if (!inputPassword.getText().equals(confPassword.getText())) {
                        ChessApplication.self().showInfo(strings.get("password_mismatch"));
                        return;
                    }

                    if (!checkName(inputPassword.getText())) {
                        ChessApplication.self().showInfo(strings.get("login_letters"));
                        return;
                    }

                    publicAcc.setPassword(inputPassword.getText());
                    MultiplayerEngine.self().changeAccount(publicAcc, requestStatus -> {
                        if (requestStatus != RequestStatus.DONE) {
                            ChessApplication.self().showError(strings.get("error_change_password") + requestStatus);
                        } else {
                            ChessApplication.self().showAccept(strings.get("done_change_password"));
                        }
                    });

                    if (self) {
                        ChessConstants.localData.setPassword(inputPassword.getText());
                    }
                }
            });

            lineTable2.add(inputPassword).expandX().fillX().row();
            lineTable2.add(confPassword).padTop(5).expandX().fillX().row();
            lineTable2.add(changePassword).padTop(5).left();

            part3.add(lineTable2).expandX().fillX();
        }

        // -----------------------------------------------------------------------------------------------------------

        dialog.getContentTable().align(Align.topLeft);
        dialog.getContentTable().add(scrollPane).expandX().fill().pad(5, 5, 5, 5);
        dialog.getContentTable().add(table2).expandY()
                .fillY().pad(10, 10, 10, 10);

        dialog.getButtonTable().align(Align.topLeft);
        dialog.getButtonTable().add(new RdTextButton(strings.get("report")));

        if (edit && (ChessConstants.loggingAcc.getType().ordinal() >= AccountType.MODERATOR.ordinal() || self)) {
            var change = new RdTextButton(strings.get("profile"),"blue");
            dialog.getButtonTable().add(change);

            change.addListener(new OnChangeListener() {
                @Override
                public void onChange(Actor actor) {
                    showChangeDialog(dialog, publicAcc, self);
                }
            });
        }

        dialog.setOnCancel(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                var action = Actions.fadeOut(0.4f, Interpolation.fade);
                var sequence  = new SequenceAction();
                sequence.addAction(action);
                sequence.addAction(Actions.run(avatar::dispose));
                dialog.hide(sequence);
            }
        });

        dialog.getLoading().setVisible(false);
    }

    private RdDialog fileSelector;

    private void showChangeDialog(RdDialog watchingDialog, Account account, boolean self) {
        var changeDialog = new RdDialog(strings.get("change_account"), ChessAssetManager.current().getSkin(), "input");
        changeDialog.getIcon().setDrawable(new TextureRegionDrawable(
                ChessAssetManager.current().findRegion("icon_conf")));
        changeDialog.getIcon().setScaling(Scaling.fit);
        changeDialog.getContentTable().align(Align.topLeft);
        changeDialog.getButtonTable().align(Align.bottomLeft);

        var content = new RdTable();
        content.padTop(5);
        content.align(Align.topLeft);
        var scroll = new RdScrollPane(content, ChessAssetManager.current().getSkin());
        scroll.setFadeScrollBars(false);
        scroll.setOverscroll(false, false);
        scroll.setScrollingDisabled(true, false);

        RdTextArea nameInput = null;
        if (ChessConstants.loggingAcc.getType().ordinal() >= AccountType.EXECUTOR.ordinal()) {

            nameInput = new RdTextArea(account.getUsername());
            nameInput.setPrefLines(1);

            var label2 = new RdLabel(strings.get("name"));
            label2.setWrap(true);

            content.add(label2).width(350).fillX().padBottom(5);
            content.add(nameInput).width(450).padBottom(5).expandX().fillX();
            content.row();
        }

        RdTextArea userNameInput = null;
        if (ChessConstants.loggingAcc.getType().ordinal() >= AccountType.MODERATOR.ordinal()) {
            userNameInput = new RdTextArea(account.getFullName());
            userNameInput.setPrefLines(1);

            var label3 = new RdLabel(strings.get("username"));
            label3.setWrap(true);

            content.add(label3).fillX().width(350).padBottom(5);
            content.add(userNameInput).width(450).padBottom(8).expandX().fillX().padBottom(5);
            content.row();
        }

        var label4 = new RdLabel(strings.get("quote"));
        label4.setWrap(true);
        var quote = new RdTextArea(account.getQuote());
        quote.setPrefLines(1);
        quote.setMaxLines(2);

        content.add(label4).width(350).fillX().padBottom(5);
        content.add(quote).width(450).padBottom(5).expandX().fillX();
        content.row();

        var label5 = new RdLabel(strings.get("gender"), ChessAssetManager.current().getSkin());
        label5.setWrap(true);

        var gender = new RdSelectBox<>(ChessAssetManager.current().getSkin());
        gender.setItems(SettingsUtil.getGendersText());
        gender.setSelected(SettingsUtil.getGenderText(account.getGender()));

        content.add(label5).width(350).fillX().padBottom(5);
        content.add(gender).width(450).padBottom(5).expandX().fillX();
        content.row();

        var label6 = new RdLabel(strings.get("country"), ChessAssetManager.current().getSkin());
        label6.setWrap(true);

        var country = new RdSelectBox<String>(ChessAssetManager.current().getSkin());
        country.setItems(SettingsUtil.getDisplayCountries());
        country.setSelected(new Locale("en", account.getCountry())
                .getDisplayCountry(ChessConstants.localData.getLocale()));

        content.add(label6).width(350).fillX().padBottom(5);
        content.add(country).width(450).padBottom(5).expandX().fillX();
        content.row();

        var label7 = new RdLabel(strings.get("birthday"), ChessAssetManager.current().getSkin());
        label7.setWrap(true);

        var day = new RdSelectBox<String>(ChessAssetManager.current().getSkin());
        day.setItems("dd", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14",
                "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31");

        var month = new RdSelectBox<String>(ChessAssetManager.current().getSkin());
        month.setItems(MONTHS);
        var year = new RdSelectBox<String>(ChessAssetManager.current().getSkin());
        year.setItems(generateYears());

        if (account.getDateBirth() == 0) {
            day.setSelectedIndex(0);
            month.setSelectedIndex(0);
            year.setSelectedIndex(0);
        } else {
            var calendar = Calendar.getInstance();
            calendar.setTime(new Date(account.getDateBirth()));
            day.setSelectedIndex(calendar.get(Calendar.DAY_OF_MONTH));
            month.setSelectedIndex(calendar.get(Calendar.MONTH) + 1);
            year.setSelected(String.valueOf(calendar.get(Calendar.YEAR)));
        }

        var table1 = new RdTable();
        table1.add(day).expand().fillX().padRight(3);
        table1.add(month).expandX().fillX().padRight(3);
        table1.add(year).expandX().fillX();

        content.add(label7).width(350).fillX().padBottom(5);
        content.add(table1).width(450).padBottom(5).expandX().fillX();
        content.row();

        // buttons ----------------------------------------------------------------------------------------------------

        RdTextArea finalUserNameInput = userNameInput;
        RdTextArea finalNameInput = nameInput;
        var accept = new RdTextButton(strings.get("apply"), "blue");

        accept.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                changeDialog.hide();

                if (finalNameInput != null) account.setUsername(finalNameInput.getText());
                if (finalUserNameInput != null) account.setFullName(finalUserNameInput.getText());

                account.setQuote(quote.getText());
                account.setGender(SettingsUtil.getGender(gender.getSelected().toString()));
                if (country.getSelectedIndex() == 0) {
                    account.setCountry("");
                } else {
                    account.setCountry(ChessApplication.self()
                            .getCountries().get(country.getSelectedIndex() - 1));
                }

                if (!day.getSelected().equals("dd") && !month.getSelected().equals("m")
                        && !year.getSelected().equals("yyyy")) {
                    var calendar = new GregorianCalendar(
                            Integer.parseInt(year.getSelected()),
                            Integer.parseInt(month.getSelected()) - 1,
                            Integer.parseInt(day.getSelected()));
                    account.setDateBirth(calendar.getTimeInMillis());
                }

                if (self) {
                    ChessConstants.loggingAcc = new Account(account);
                    ChessConstants.loggingAcc.setPassword(ChessConstants.localData.getPassword());
                }

                MultiplayerEngine.self().changeAccount(account, requestStatus -> {
                    if (requestStatus != RequestStatus.DONE) {
                        ChessApplication.self().showError(strings.get("error_change_profile") + requestStatus);
                    } else {
                        ChessApplication.self().showAccept(strings.get("done_change_profile"));
                    }
                });

                updateWatchingDialog(account, watchingDialog);

            }
        });
        var cancel = new RdTextButton(strings.get("cancel"));
        cancel.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                changeDialog.hide();
            }
        });

        changeDialog.getContentTable().add(scroll).expand().fill();
        changeDialog.getButtonTable().add(accept).expand().fillX().align(Align.bottomLeft);
        changeDialog.getButtonTable().add(cancel).expand().fillX().align(Align.bottomLeft);

        changeDialog.show(activity.getStage());
        changeDialog.setSize(900, 900);

        ChessApplication.self().addDialog(changeDialog, WindowUtil::resizeCenter);
        ChessApplication.self().resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }


    // ----------------------------------------------------------------------------------------------------------------

    private void showUpdatingAvatarView(Account account, AvatarView avatarView) {
        fileSelector = new FileSelectorBuilder()
            .title(strings.get("file_selector"))
            .endFilters(".png", ".jpg")
            .cancel(strings.get("cancel"))
            .select(strings.get("select"), handle ->
                loadAvatar(handle, bytes -> {

                    if (bytes.length >= 39_000) {
                        ChessApplication.self().showError(strings.get("error_large_avatar"));
                        return;
                    }

                    fileSelector.hide();
                    MultiplayerEngine.self().changeAvatar(account.getId(), bytes,
                        requestStatus -> {
                        if (requestStatus == RequestStatus.DONE) {
                            ChessApplication.self().showAccept(strings.get("done_change_avatar"));
                            updateAvatar(avatarView);
                        } else {
                            ChessApplication.self().showError(strings.get("error_change_avatar"));
                        }
                    });

                }))
            .build();

        fileSelector.show(activity.getStage());
        fileSelector.setSize(900, 800);

        RdApplication.self().addDialog(fileSelector, WindowUtil::resizeCenter);
        RdApplication.self().resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private void updateAvatar(AvatarView avatarView) {
        MultiplayerEngine.self().getAvatar(ChessConstants.loggingAcc,
            bytes -> {
                ChessApplication.self().getAccountPanel()
                    .update(ChessConstants.loggingAcc, bytes);
                avatarView.update(ChessConstants.loggingAcc, bytes);
            });
    }

    private void loadAvatar(FileHandle handle, Consumer<byte[]> onFinish) {
        var spinner = new Spinner(strings.get("loading"));
        activity.setSpinner(spinner);
        spinner.show(RdApplication.self().getStage());
        spinner.setSize(400, 100);
        activity.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        Runnable task = () -> {
            var bytes = handle.readBytes();
            spinner.hide();
            RdApplication.postRunnable(() ->
                onFinish.accept(bytes));
        };
        RdApplication.self().execute(task);
    }

    private String getAccountStatus(Account account) {
        StringBuilder status = new StringBuilder();
        if (account.getType() == AccountType.DEVELOPER) status.append(strings.get("developer"));
        else if (account.getType() == AccountType.EXECUTOR) status.append(strings.get("executor"));
        else if (account.getType() == AccountType.MODERATOR) status.append(strings.get("moderator"));
        else status.append(strings.get("user"));

        boolean warned = false;
        boolean muted = false;
        boolean banned = false;
        for (var flag : account.getFlags()) {
            if (flag == Flag.WARN) warned = true;
            else if (flag == Flag.MUTE) muted = true;
            else if (flag == Flag.BAN) banned = true;
        }

        if (banned) status.append(strings.get("\nbanned"));
        else if (muted) status.append(strings.get("\nmuted"));
        else if (warned) status.append(strings.get("\nwarned"));

        return status.toString();
    }

    private Image getGenderImage(Account account) {
        if (account.getGender() == Gender.MALE) {
            return new Image(new TextureRegionDrawable(ChessAssetManager.current().findRegion("iw_male")));
        }
        else if (account.getGender() == Gender.FEMALE) {
            return new Image(new TextureRegionDrawable(ChessAssetManager.current().findRegion("iw_female")));
        } else if (account.getGender() == Gender.ANOTHER) {
            return new Image(new TextureRegionDrawable(ChessAssetManager.current().findRegion("iw_transgender")));
        }
        throw new IllegalArgumentException("Gender cannot be determined, gender = " + account.getGender());
    }

    private String[] generateYears() {
        var calendar = Calendar.getInstance();
        var year = calendar.get(Calendar.YEAR);

        String[] years = new String[year - 1900 + 2];
        years[0] = "yyyy";
        for (int i = 1900; i <= year; i++) {
            years[i - 1900 + 1] = String.valueOf(i);
        }
        return years;
    }

    private boolean checkName(String name) {
        var asci = new ArrayList<>();
        for (int i = 0x20; i < 0x7B; i++) asci.add((char)i);

        for (char c : name.toCharArray()) {
            if (!asci.contains(c)) return false;
        }
        return true;
    }
}
