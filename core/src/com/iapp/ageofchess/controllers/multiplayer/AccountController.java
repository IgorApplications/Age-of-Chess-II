package com.iapp.ageofchess.controllers.multiplayer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.iapp.ageofchess.ChessApplication;
import com.iapp.ageofchess.activity.multiplayer.MultiplayerScenariosActivity;
import com.iapp.lib.ui.widgets.AccountView;
import com.iapp.lib.ui.widgets.AvatarView;
import com.iapp.ageofchess.graphics.MultiplayerMatchView;
import com.iapp.ageofchess.multiplayer.Match;
import com.iapp.ageofchess.multiplayer.MultiplayerEngine;
import com.iapp.ageofchess.services.ChessAssetManager;
import com.iapp.ageofchess.services.ChessConstants;
import com.iapp.ageofchess.services.SettingsUtil;
import com.iapp.lib.ui.actors.*;
import com.iapp.lib.ui.screens.RdApplication;
import com.iapp.lib.util.OnChangeListener;
import com.iapp.lib.util.RdI18NBundle;
import com.iapp.lib.util.WindowUtil;
import com.iapp.lib.web.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class AccountController {

    private static final Pattern FLOAT_PATTERN = Pattern.compile("[-+]?[0-9]*\\.?[0-9]+");
    private static final String[] MONTHS = new String[]{"m", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};

    private final RdI18NBundle strings;
    private final Map<Long, Consumer<List<Match>>> idByOnMatches = RdApplication.self().getLauncher().concurrentHashMap();
    @SuppressWarnings("SimpleDateFormat")
    private final SimpleDateFormat birthdayFormatter = new SimpleDateFormat("d MMMM yyyy",
        new Locale(ChessConstants.localData.getLangCode()));
    private final SimpleDateFormat createdFormatter = new SimpleDateFormat("d MMM yyyy",
        new Locale(ChessConstants.localData.getLangCode()));
    private final String[] accountTypeStrings;
    private final List<RdDialog> hiddens = new ArrayList<>();

    public AccountController() {
        strings = RdApplication.self().getStrings();
        accountTypeStrings = new String[]{strings.get("[i18n]User"), strings.get("[i18n]Moderator"),
            strings.get("[i18n]Executor"), strings.get("[i18n]Developer")};
    }

    public void seeAccount(long id, List<RdDialog> hidden) {
        var dialog = showWatchingDialog(isSelf(id));
        hiddens.addAll(hidden);

        MultiplayerEngine.self().getAccount(id, account -> {
            updateAccountDialog(account, dialog, false);
        });
    }

    public void seeAccount(long id) {
        var dialog = showWatchingDialog(isSelf(id));

        MultiplayerEngine.self().getAccount(id, account -> {
            updateAccountDialog(account, dialog, false);
        });
    }

    public void editAccount(long id) {
        var dialog = showWatchingDialog(isSelf(id));

        MultiplayerEngine.self().getAccount(id, account -> {
            updateAccountDialog(account, dialog);
        });
    }

    public void editAccount(Message message) {
        var dialog = showWatchingDialog(isSelf(message.getSenderId()));

        MultiplayerEngine.self().getAccount(message.getSenderId(), account -> {
            updateAccountDialog(account, dialog);
        });

    }

    private RdDialog showWatchingDialog(boolean self) {
        return showWatchingDialog(self, 1100, 2000);
    }

    private RdDialog showWatchingDialog(boolean self, float width, float height) {

        String title = strings.get("[i18n]View account");
        if (self) title = strings.get("[i18n]Account management");

        var dialog = new RdDialog(title);
        dialog.getLoading().setVisible(true);
        dialog.show(RdApplication.self().getStage());

        ChessApplication.self().addDialog(dialog, localDialog -> {
            var viewport = RdApplication.self().getViewport();
            if (localDialog != null) {
                localDialog.setWidth(Math.min(viewport.getWorldWidth() - 30, width));
                localDialog.setHeight(Math.min(viewport.getWorldHeight() - 30, height));
            }
            WindowUtil.resizeCenter(localDialog);
        });

        RdApplication.self().resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        return dialog;
    }

    private void updateAccountDialog(Account account, RdDialog dialog) {
        updateAccountDialog(account, dialog, true);
    }

    // account state ---------------------------------------------------------------------------------------------------

    private void updateAccountDialog(Account account, RdDialog dialog, boolean edit) {

        boolean self = account.getId() == ChessConstants.loggingAcc.getId();
        dialog.getButtonTable().clear();
        dialog.getContentTable().clear();

        RdTable buttons = new RdTable();
        buttons.align(Align.topLeft);
        RdTable content = new RdTable();
        RdScrollPane contentScroll = new RdScrollPane(content);
        contentScroll.setScrollingDisabled(true, false);

        NinePatchDrawable panelBg = new NinePatchDrawable(
            new NinePatch(ChessApplication.self().getAssetManager().findRegion("dark_pane"),
                10,10,10,10));

        RdTable part1 = new RdTable();
        part1.align(Align.topLeft);
        RdTable part2 = new RdTable();
        part2.setBackground(panelBg);
        part2.align(Align.topLeft);
        RdTable part3 = new RdTable();
        part3.setBackground(panelBg);
        part3.align(Align.topLeft);
        RdTable part4 = new RdTable();
        part4.setBackground(panelBg);
        part4.align(Align.topLeft);
        RdTable part5 = new RdTable();
        part5.align(Align.topLeft);
        part5.setBackground(panelBg);

        RdTable avatarTable = new RdTable();
        var avatar = new AvatarView();
        MultiplayerEngine.self().requireAvatar(account, bytes ->
            avatar.update(account, bytes));

        if (edit && self || !self && ChessConstants.loggingAcc.getType().ordinal() >= AccountType.EXECUTOR.ordinal()) {

            avatar.addListener(new OnChangeListener() {
                @Override
                public void onChange(Actor actor) {
                    showUpdatingAvatarView(account, avatar);
                }
            });
        }
        avatarTable.add(avatar).row();
        avatarTable.add(new RdLabel("[%75]" + createdFormatter.format(account.getCreated())));

        RdTable general1 = new RdTable();
        general1.setBackground(panelBg);
        general1.add(part1).expandX().fillX().align(Align.topLeft);
        general1.add(avatarTable).align(Align.topRight);

        content.add(general1).expandX().fillX().align(Align.topLeft).padTop(5).row();
        addAccountInfo(account, part1);

        // !!!
        if (edit && self || !self && ChessConstants.loggingAcc.getType().ordinal() >= AccountType.DEVELOPER.ordinal()
            && ChessConstants.loggingAcc.getType().ordinal() > account.getType().ordinal()) {
            content.add(part2).expandX().fillX().align(Align.topLeft).padTop(7).row();
            content.add(part3).expandX().fillX().align(Align.topLeft).padTop(7).row();

            addLoginPanel(account, part2);
            addChangePassword(account, part3, self);
        }
        if (!self || !edit) {
            content.add(part4).expandX().fillX().align(Align.topLeft).padTop(7).row();

            addRankPanel(account, part4);
        }

        // !!!
        if (edit && self || !self && ChessConstants.loggingAcc.getType().ordinal() >= AccountType.EXECUTOR.ordinal()
            && ChessConstants.loggingAcc.getType().ordinal() > account.getType().ordinal()) {
            content.add(part5).expandX().fillX().align(Align.topLeft).padTop(7).row();

            addProfilePanel(account, part5, dialog, self);
        }

        if (self && edit) {
            ImageButton settings = new ImageButton(ChessAssetManager.current().getSkin(), "gray_settings");
            ImageButton search = new ImageButton(ChessAssetManager.current().getSearchPeopleStyle());
            buttons.add(settings).minSize(110).row();
            buttons.add(search).minSize(110).row();

            search.addListener(new OnChangeListener() {
                @Override
                public void onChange(Actor actor) {
                    dialog.getLoading().setVisible(true);
                    updateSearchingDialog(account, dialog);
                }
            });

        } else {
            RdImageTextButton games = new RdImageTextButton(strings.get("[i18n]Games"));
            RdImageTextButton report = new RdImageTextButton(strings.get("[i18n]Report"));
            RdImageTextButton admin = new RdImageTextButton(strings.get("[i18n]Admin"));

            games.addListener(new OnChangeListener() {
                @Override
                public void onChange(Actor actor) {
                    showGames(dialog, account);
                }
            });
            report.addListener(new OnChangeListener() {
                @Override
                public void onChange(Actor actor) {}
            });
            admin.addListener(new OnChangeListener() {
                @Override
                public void onChange(Actor actor) {
                    showAdminDialog(account);
                }
            });

            buttons.add(games).fillX().minWidth(200).row();
            if (!self) buttons.add(report).minWidth(200).fillX().row();
            if (ChessConstants.loggingAcc.getType().ordinal() >= AccountType.MODERATOR.ordinal()
                && (ChessConstants.loggingAcc.getType().ordinal() > account.getType().ordinal() || isSelf(account))) {
                buttons.add(admin).minWidth(200).fillX().row();
            }
        }

        dialog.setOnCancel(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                dialog.afterHide(Actions.run(avatar::dispose));
            }
        });

        dialog.getContentTable().align(Align.topLeft);
        dialog.getContentTable().add(buttons).fillY().pad(7, 7, 7, 0);
        dialog.getContentTable().add(contentScroll).expandX().fill().pad(7, 7, 7, 7);

        dialog.getLoading().setVisible(false);
    }

    // search state ----------------------------------------------------------------------------------------------------
    private void updateSearchingDialog(Account account, RdDialog dialog) {
        List<AccountView> disposed = new ArrayList<>();

        dialog.setOnCancel(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                dialog.afterHide(Actions.run(() -> {
                    for (AccountView accountView : disposed) {
                        accountView.dispose();
                    }
                }));
            }
        });
        dialog.getButtonTable().clear();
        dialog.getContentTable().clear();
        dialog.getContentTable().align(Align.topLeft);
        NinePatchDrawable panelBg = new NinePatchDrawable(
            new NinePatch(ChessApplication.self().getAssetManager().findRegion("dark_pane"),
                10,10,10,10));

        RdTable content = new RdTable();
        content.setBackground(panelBg);
        content.align(Align.topLeft);
        RdTable accountsContent = new RdTable();
        accountsContent.align(Align.topLeft);

        RdScrollPane scrollContent = new RdScrollPane(content);
        RdTable buttons = new RdTable();
        buttons.align(Align.topLeft);

        ImageButton settings = new ImageButton(ChessAssetManager.current().getSkin(), "gray_settings");
        ImageButton searchOp = new ImageButton(ChessAssetManager.current().getSearchPeopleStyle());
        buttons.add(settings).minSize(110).row();
        buttons.add(searchOp).minSize(110).row();

        RdTextArea field = new RdTextArea("", ChessAssetManager.current().getSkin());
        field.setMaxLength(20);
        field.setMessageText(strings.get("[i18n]Enter account name..."));
        RdImageTextButton search = new RdImageTextButton(strings.get("[i18n]search"), "blue");

        content.add(field).expandX().fillX().padRight(5);
        content.add(search).minWidth(200).row();
        content.add(accountsContent).expand().fill();

        settings.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                for (AccountView accountView : disposed) {
                    accountView.dispose();
                }

                updateAccountDialog(account, dialog, true);
            }
        });

        search.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                MultiplayerEngine.self().searchAccounts(field.getText(),
                    accounts -> {
                        for (AccountView accountView : disposed) {
                            accountView.dispose();
                        }

                        accountsContent.clear();
                        disposed.clear();

                        for (Account acc : accounts) {
                            AccountView accountView = new AccountView(MultiplayerEngine.self(), acc,
                                new OnChangeListener() {
                                    @Override
                                    public void onChange(Actor actor) {
                                        seeAccount(acc.getId());
                                    }
                                });
                            accountsContent.add(accountView).expandX()
                                .align(Align.topLeft).row();
                            disposed.add(accountView);
                        }
                    });
            }
        });

        dialog.getContentTable().add(buttons).align(Align.topLeft).pad(7, 7, 7, 0);
        dialog.getContentTable().add(scrollContent).expand().fill().pad(7, 7, 7, 7);
        dialog.getLoading().setVisible(false);
    }

    //   ------------------------------------------------------------------------------------------------------------

    private void addProfilePanel(Account account, RdTable part5, RdDialog dialog, boolean self) {
        RdTextButton change = new RdTextButton(strings.get("[i18n]Profile"), "blue");
        RdTextButton view = new RdTextButton(strings.get("[i18n]See"));

        part5.add(new RdLabel(strings.get("[i18n]Change profile"))).expandX()
            .left().minWidth(250).padTop(7);
        part5.add(change).minWidth(250).fillX().row();

        if (self) {
            part5.add(new RdLabel(strings.get("[i18n]See profile")))
                .expandX().left();
            part5.add(view).fillX().row();
        }

        change.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                showChangeDialog(dialog, account, self);
            }
        });

        view.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                RdDialog newDialog = showWatchingDialog(account.getId() == ChessConstants.loggingAcc.getId(), 1000, 2000);
                updateAccountDialog(account, newDialog, false);
            }
        });
    }

    private void showChangeDialog(RdDialog watchingDialog, Account account, boolean self) {
        var changeDialog = new RdDialog(strings.get("[i18n]Change account"), "input");
        changeDialog.getIcon().setDrawable(new TextureRegionDrawable(
            ChessAssetManager.current().findRegion("icon_conf")));
        changeDialog.getIcon().setScaling(Scaling.fit);
        changeDialog.getContentTable().align(Align.topLeft);
        changeDialog.getButtonTable().align(Align.bottomLeft);

        var content = new RdTable();
        content.padTop(5);
        content.align(Align.topLeft);
        var scroll = new RdScrollPane(content);
        scroll.setFadeScrollBars(false);
        scroll.setOverscroll(false, false);
        scroll.setScrollingDisabled(true, false);

        RdTextArea nameInput = new RdTextArea(account.getUsername(), ChessAssetManager.current().getSkin());
        nameInput.setMaxLength(20);
        if (ChessConstants.loggingAcc.getType().ordinal() >= AccountType.EXECUTOR.ordinal()) {

            var label2 = new RdLabel(strings.get("[i18n]Name:"));
            label2.setWrap(true);

            content.add(label2).width(350).fillX().padBottom(5);
            content.add(nameInput).width(450).padBottom(5).expandX().fillX();
            content.row();
        }

        RdTextArea userNameInput = new RdTextArea(account.getFullName(), ChessAssetManager.current().getSkin());
        userNameInput.setMaxLength(20);
        if (ChessConstants.loggingAcc.getType().ordinal() >= AccountType.EXECUTOR.ordinal()) {
            var label3 = new RdLabel(strings.get("[i18n]Username:"));
            label3.setWrap(true);

            content.add(label3).fillX().width(350).padBottom(5);
            content.add(userNameInput).width(450).padBottom(8).expandX().fillX().padBottom(5);
            content.row();
        }

        var label4 = new RdLabel(strings.get("[i18n]Quote:"));
        label4.setWrap(true);
        var quote = new RdTextArea(account.getQuote(), ChessAssetManager.current().getSkin());
        quote.setMaxLength(50);

        content.add(label4).width(350).fillX().padBottom(5);
        content.add(quote).width(450).padBottom(5).expandX().fillX();
        content.row();

        var label5 = new RdLabel(strings.get("[i18n]Gender:"), ChessAssetManager.current().getSkin());
        label5.setWrap(true);

        var gender = new RdSelectBox<>(ChessAssetManager.current().getSkin());
        gender.setItems(SettingsUtil.getGendersText());
        gender.setSelected(SettingsUtil.getGenderText(account.getGender()));

        content.add(label5).width(350).fillX().padBottom(5);
        content.add(gender).width(450).padBottom(5).expandX().fillX();
        content.row();

        RdLabel label6 = new RdLabel(strings.get("[i18n]Country: "), ChessAssetManager.current().getSkin());
        label6.setWrap(true);

        RdSelectBox<String> country = new RdSelectBox<>(ChessAssetManager.current().getSkin());
        country.setItems(SettingsUtil.getDisplayCountries());
        country.setSelected(new Locale("en", account.getCountry())
            .getDisplayCountry(new Locale(ChessConstants.localData.getLangCode())));

        content.add(label6).width(350).fillX().padBottom(5);
        content.add(country).width(450).padBottom(5).expandX().fillX();
        content.row();

        RdLabel label7 = new RdLabel(strings.get("[i18n]Birthday:"), ChessAssetManager.current().getSkin());
        label7.setWrap(true);

        RdSelectBox<String> day = new RdSelectBox<>(ChessAssetManager.current().getSkin());
        day.setItems("dd", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14",
            "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31");

        RdSelectBox<String> month = new RdSelectBox<>(ChessAssetManager.current().getSkin());
        month.setItems(MONTHS);
        RdSelectBox<String> year = new RdSelectBox<>(ChessAssetManager.current().getSkin());
        year.setItems(generateYears());

        if (account.getDateBirth() == 0) {
            day.setSelectedIndex(0);
            month.setSelectedIndex(0);
            year.setSelectedIndex(0);
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date(account.getDateBirth()));
            day.setSelectedIndex(calendar.get(Calendar.DAY_OF_MONTH));
            month.setSelectedIndex(calendar.get(Calendar.MONTH) + 1);
            year.setSelected(String.valueOf(calendar.get(Calendar.YEAR)));
        }

        RdTable birthdayTable = new RdTable();
        birthdayTable.add(day).expand().fillX().padRight(3);
        birthdayTable.add(month).expandX().fillX().padRight(3);
        birthdayTable.add(year).expandX().fillX();

        content.add(label7).width(350).fillX().padBottom(5);
        content.add(birthdayTable).width(450).padBottom(5).expandX().fillX();
        content.row();

        RdTextArea bullet = new RdTextArea(String.valueOf(account.getBullet()), ChessAssetManager.current().getSkin());
        bullet.setMaxLength(20);
        RdTextArea blitz = new RdTextArea(String.valueOf(account.getBlitz()), ChessAssetManager.current().getSkin());
        blitz.setMaxLength(20);
        RdTextArea rapid = new RdTextArea(String.valueOf(account.getRapid()), ChessAssetManager.current().getSkin());
        rapid.setMaxLength(20);
        RdTextArea longRank = new RdTextArea(String.valueOf(account.getLongRank()), ChessAssetManager.current().getSkin());
        longRank.setMaxLength(20);

        if (ChessConstants.loggingAcc.getType().ordinal() >= AccountType.EXECUTOR.ordinal()) {
            RdLabel label8 = new RdLabel(strings.get("[i18n]Bullet") + ": ");
            RdLabel label9 = new RdLabel(strings.get("[i18n]Blitz") + ": ");
            RdLabel label10 = new RdLabel(strings.get("[i18n]Rapid") + ": ");
            RdLabel label11 = new RdLabel(strings.get("[i18n]Long") + ": ");

            content.add(label8).width(350).fillX().padBottom(5);
            content.add(bullet).width(450).padBottom(5).expandX().fillX();
            content.row();
            content.add(label9).width(350).fillX().padBottom(5);
            content.add(blitz).width(450).padBottom(5).expandX().fillX();
            content.row();
            content.add(label10).width(350).fillX().padBottom(5);
            content.add(rapid).width(450).padBottom(5).expandX().fillX();
            content.row();
            content.add(label11).width(350).fillX().padBottom(5);
            content.add(longRank).width(450).padBottom(5).expandX().fillX();
            content.row();
        }

        // buttons ----------------------------------------------------------------------------------------------------
        var accept = new RdTextButton(strings.get("[i18n]apply"), "blue");

        accept.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                if (!isASCII(nameInput.getText())) {
                    ChessApplication.self().showError(strings.get(
                        "[i18n]The login should not contain Latin letters!"));
                    return;
                }
                account.setUsername(nameInput.getText());
                account.setFullName(userNameInput.getText());

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

                String incorrectChangeRank = "[i18n]rating change field {0} contains invalid characters";
                if (!FLOAT_PATTERN.matcher(bullet.getText()).matches()) {
                    ChessApplication.self().showError(
                        strings.format(incorrectChangeRank, strings.get("[i18n]Bullet")));
                    return;
                }
                if (!FLOAT_PATTERN.matcher(blitz.getText()).matches()) {
                    ChessApplication.self().showError(
                        strings.format(incorrectChangeRank, strings.get("[i18n]Blitz")));
                    return;
                }
                if (!FLOAT_PATTERN.matcher(rapid.getText()).matches()) {
                    ChessApplication.self().showError(
                        strings.format(incorrectChangeRank, strings.get("[i18n]Rapid")));
                    return;
                }
                if (!FLOAT_PATTERN.matcher(longRank.getText()).matches()) {
                    ChessApplication.self().showError(
                        strings.format(incorrectChangeRank, strings.get("[i18n]Long")));
                    return;
                }

                account.setBullet(Double.parseDouble(bullet.getText()));
                account.setBlitz(Double.parseDouble(blitz.getText()));
                account.setRapid(Double.parseDouble(rapid.getText()));
                account.setLongRank(Double.parseDouble(longRank.getText()));

                changeDialog.hide();
                if (self) {
                    ChessConstants.loggingAcc = new Account(account);
                    ChessConstants.loggingAcc.setPassword(ChessConstants.localData.getPassword());
                }

                MultiplayerEngine.self().changeAccount(account, requestStatus -> {
                    if (requestStatus != RequestStatus.DONE) {
                        ChessApplication.self().showError(strings.format(
                            "[i18n]Profile changes not confirmed, error status \"{0}\"",
                            requestStatus.toString()));
                    } else {
                        ChessApplication.self().showAccept(strings.get(
                            "[i18n]Changes profile confirmed and submitted"));
                    }
                });

                updateAccountDialog(account, watchingDialog);

            }
        });
        var cancel = new RdTextButton(strings.get("[i18n]reject"));
        cancel.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                changeDialog.hide();
            }
        });

        changeDialog.getContentTable().add(scroll).expand().fill();
        changeDialog.getButtonTable().add(accept).expand().fillX().align(Align.bottomLeft);
        changeDialog.getButtonTable().add(cancel).expand().fillX().align(Align.bottomLeft);

        changeDialog.show(RdApplication.self().getStage());
        changeDialog.setSize(900, 700);

        ChessApplication.self().addDialog(changeDialog, WindowUtil::resizeCenter);
        ChessApplication.self().resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    // ----------------------------------------------------------------------------------------------------------------

    private void showUpdatingAvatarView(Account account, AvatarView avatarView) {

        ChessApplication.self().showSelector(handle -> {
            loadAvatar(handle, bytes -> {

                if (bytes.length >= 39_000) {
                    ChessApplication.self().showError(strings.get("[i18n]The avatar is too large, it must be less than 38 Kbyte\n"));
                    return;
                }

                MultiplayerEngine.self().changeAvatar(account.getId(), bytes,
                    requestStatus -> {
                        if (requestStatus == RequestStatus.DONE) {
                            ChessApplication.self().showAccept(strings.get("[i18n]Avatar changed successfully"));
                            updateAvatar(avatarView);
                        } else {
                            ChessApplication.self().showError(strings.get("[i18n]Error updating avatar, error status -\n"));
                        }
                    });
            });

        }, ".png", ".jpg");

    }

    // show games ---------------------------------------------------------------------------------------------------
    void showGames(Account account) {
        showGames(null, account);
    }

    private void showGames(RdDialog parent, Account account) {
        RdI18NBundle strings = RdApplication.self().getStrings();
        RdDialog games = new RdDialog(strings.get("[i18n]Viewing games"));
        games.getLoading().setVisible(true);

        RdTable content = new RdTable();
        content.align(Align.topLeft);
        RdScrollPane scrollPane = new RdScrollPane(content);
        games.getContentTable().add(scrollPane)
            .expand().fill().pad(5, 5, 5, 5);

        games.show(RdApplication.self().getStage());
        RdApplication.self().addDialog(games, 1000, 2000, 30, 30);

        Consumer<List<Match>> onMatches = matches -> {
            MultiplayerEngine.self().removeOnMatches(idByOnMatches.remove(account.getId()));

            for (Match match : matches) {
                if (match.getWhitePlayerId() == account.getId()
                    || match.getBlackPlayerId() == account.getId()) {

                    content.add(
                            new MultiplayerMatchView(match,
                                new OnChangeListener() {
                                    @Override
                                    public void onChange(Actor actor1) {

                                        // closing previous dialog boxes
                                        for (RdDialog hidden : hiddens) {
                                            hidden.setVisible(false);
                                        }

                                        SequenceAction seq1 = new SequenceAction();
                                        seq1.addAction(Actions.run(() -> {
                                            SequenceAction seq2 = new SequenceAction();
                                            seq2.addAction(Actions.run(() ->
                                                RdApplication.self().setScreen(new MultiplayerScenariosActivity(match))));

                                            if (parent == null) {
                                                RdApplication.self().setScreen(new MultiplayerScenariosActivity(match));
                                            } else {
                                                parent.afterHide(seq2);
                                            }

                                        }));
                                        games.afterHide(seq1);

                                    }
                                },
                                null
                            ))
                        .expandX().fillX().padBottom(5).row();
                }
            }
            games.getLoading().setVisible(false);

        };
        MultiplayerEngine.self().addOnMatches(onMatches);
        idByOnMatches.put(account.getId(), onMatches);
    }

    // ---------------------------------------------------------------------------------------------------------------

    private void showAdminDialog(Account account) {
        RdDialog adminDialog = new RdDialog(strings.get("[i18n]Moderation"), "input");
        adminDialog.getContentTable().align(Align.topLeft);

        List<Punishment> punishments = new ArrayList<>();

        // actors
        RdLabel hintType = new RdLabel(strings.get("[i18n]Account rights"));
        RdSelectBox<String> typeAccount = new RdSelectBox<>();
        typeAccount.setItems(getAvailableTypeAccount());

        // your role is always higher than the available roles
        if (!isSelf(account)) {
            typeAccount.setSelectedIndex(account.getType().ordinal());
        }

        RdLabel hintMoney = new RdLabel(strings.get("[i18n]Add/subtract coins"));
        RdTextArea moneyField = new RdTextArea("");
        moneyField.setMessageText(strings.get("[i18n]coins..."));
        moneyField.setMaxLength(6);

        RdLabel hintWarn = new RdLabel(strings.get("[i18n]Add warning"));
        RdImageTextButton warn = new RdImageTextButton("");
        warn.setText(strings.get("[i18n]warn"));

        RdLabel hintMute = new RdLabel(strings.get("[i18n]Add mute"));
        RdImageTextButton mute = new RdImageTextButton(strings.get("[i18n]mute"));
        mute.setText(strings.get("[i18n]mute"));

        RdLabel hintBan = new RdLabel(strings.get("[i18n]Add ban"));
        RdImageTextButton ban = new RdImageTextButton(strings.get("[i18n]ban"));
        ban.setText(strings.get("[i18n]ban"));

        RdImageTextButton apply = new RdImageTextButton(strings.get("[i18n]apply"), "blue");
        RdImageTextButton cancel = new RdImageTextButton(strings.get("[i18n]reject"));
        String reasonPunishment = strings.get("[i18n]Enter the reason for punishment/removal of punishment (in English)");

        // listeners
        typeAccount.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {}
        });
        warn.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                ChessApplication.self().showInput(strings.get(reasonPunishment),
                    (dialog, s) -> {
                        dialog.hide();
                        punishments.add(new Punishment(-1, ChessConstants.loggingAcc.getId(), s, Flag.WARN, true));
                    });
            }
        });
        mute.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                ChessApplication.self().showInput(strings.get(reasonPunishment),
                    (dialog, s) -> {
                        dialog.hide();
                        punishments.add(new Punishment(-1, ChessConstants.loggingAcc.getId(), s, Flag.MUTE, true));
                    });
            }
        });
        ban.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                ChessApplication.self().showInput(strings.get(reasonPunishment),
                    (dialog, s) -> {
                        dialog.hide();
                        punishments.add(new Punishment(-1, ChessConstants.loggingAcc.getId(), s, Flag.BAN, true));
                    });
            }
        });
        apply.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {

                String confPunishments = "";
                if (!punishments.isEmpty()) {
                    confPunishments = strings.format("[i18n]You apply {0} punishments. ", punishments.size());
                }

                ChessApplication.self().showConfWarn(strings.get("[i18n]apply"),
                    strings.get("[i18n]Are you sure you want to apply the changes? This may be irreversible or there will be account issues.") + confPunishments,
                    (dialog, s) -> {
                        dialog.hide();
                        applyChanges(adminDialog, account, moneyField.getText(),
                            typeAccount.getSelectedIndex(), punishments);
                    });


            }
        });
        cancel.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                adminDialog.hide();
            }
        });

        // content table

        RdTable scrollContent = new RdTable();
        scrollContent.align(Align.topLeft);
        RdScrollPane scrollPane = new RdScrollPane(scrollContent);
        adminDialog.getContentTable().add(scrollPane).expand().fill();

        // You cannot demote or promote yourself
        if (!isSelf(account)) {
            scrollContent.add(hintType).expandX().fillX().align(Align.topLeft).padTop(5);
            scrollContent.add(typeAccount).expandX().fillX().align(Align.topLeft).padTop(5).row();
        }

        // only >= EXECUTOR!
        if (ChessConstants.loggingAcc.getType().ordinal() >= AccountType.EXECUTOR.ordinal()) {
            scrollContent.add(hintMoney).expandX().fillX().align(Align.topLeft).padTop(5);
            scrollContent.add(moneyField).expandX().fillX().align(Align.topLeft).padTop(5).row();
        }

        if (!isSelf(account)) {
            scrollContent.add(hintWarn).expandX().fillX().align(Align.topLeft).padTop(5);
            scrollContent.add(warn).expandX().fillX().align(Align.topLeft).padTop(5).row();

            scrollContent.add(hintMute).expandX().fillX().align(Align.topLeft).padTop(5);
            scrollContent.add(mute).expandX().fillX().align(Align.topLeft).padTop(5).row();

            scrollContent.add(hintBan).expandX().fillX().align(Align.topLeft).padTop(5);
            scrollContent.add(ban).expandX().fillX().align(Align.topLeft).padTop(5).row();
        }

        // button table
        adminDialog.getButtonTable().add(apply).expandX().fillX();
        adminDialog.getButtonTable().add(cancel).expandX().fillX();

        adminDialog.show(RdApplication.self().getStage());
        RdApplication.self().addDialog(adminDialog, 1000, 700, 30, 30);
    }

    private void applyChanges(RdDialog adminDialog, Account account, String moneyCoins,
                              int typeSelectedIndex, List<Punishment> punishments) {

        if (!moneyCoins.equals("") && !moneyCoins.matches("(-)?\\d+")) {
            ChessApplication.self().showError(strings.get("[i18n]Incorrect coin format to add/subtract"));
            return;
        }

        for (Punishment punishment : punishments) {
            MultiplayerEngine.self().punish(account.getId(),
                punishment,
                error -> ChessApplication.self().showError(error));
        }

        if (!moneyCoins.equals("")) account.setCoins(account.getCoins() + Long.parseLong(moneyCoins));
        account.setType(AccountType.values()[typeSelectedIndex]);

        MultiplayerEngine.self().changeAccount(account, requestStatus -> {
            adminDialog.hide();
            if (requestStatus == RequestStatus.DONE) {
                ChessApplication.self().showAccept(strings.get("[i18n]Changes submitted successfully"));
            } else {
                ChessApplication.self().showError(strings.get("[i18n]Error when trying to change account"));
            }
        });
    }

    private String[] getAvailableTypeAccount() {
        String[] result = new String[ChessConstants.loggingAcc.getType().ordinal()];
        System.arraycopy(accountTypeStrings, 0, result, 0, ChessConstants.loggingAcc.getType().ordinal());
        return result;
    }

    // ----------------------------------------------------------------------------------------------------------------

    private void addAccountInfo(Account account, RdTable part1) {

        var locale = new Locale(ChessConstants.localData.getLangCode(), account.getCountry());
        var name = new RdLabel(account.getUsername());
        var status = new RdLabel(getAccountStatus(account));
        var userName = new RdLabel(account.getFullName());
        var gender = new RdLabel(SettingsUtil.getGenderText(account.getGender()));
        var birth = new RdLabel("n/d");
        if (account.getDateBirth() != 0) birth.setText(birthdayFormatter.format(account.getDateBirth()));

        var country = new RdLabel(locale.getDisplayCountry(
            new Locale(ChessConstants.localData.getLangCode())));
        if (account.getCountry().equals("")) country.setText("n/d");
        var quote = new RdLabel(account.getQuote());
        quote.setWrap(true);

        part1.add(new Image(new TextureRegionDrawable(ChessAssetManager.current().findRegion("iw_account"))));
        part1.add(name).padLeft(5).expandX().fillX().row();

        part1.add(new Image(new TextureRegionDrawable(ChessAssetManager.current().findRegion("iw_name"))));
        part1.add(userName).padLeft(5).expandX().fillX().row();

        part1.add(new Image(new TextureRegionDrawable(ChessAssetManager.current().findRegion("iw_work"))));
        part1.add(status).padLeft(5).expandX().fillX().row();

        part1.add(new Image(new TextureRegionDrawable(ChessAssetManager.current().findRegion("iw_cake"))));
        part1.add(birth).padLeft(5).expandX().fillX().row();

        if (account.getGender() != Gender.ND) {
            part1.add(getGenderImage(account));
        } else {
            part1.add(new Image(new TextureRegionDrawable(ChessAssetManager.current().findRegion("iw_face"))));
        }
        part1.add(gender).padLeft(5).colspan(2).expandX().fillX().row();

        part1.add(new Image(new TextureRegionDrawable(ChessAssetManager.current().findRegion("iw_country"))));
        part1.add(country).padLeft(5).expandX().fillX().row();

        part1.add(new Image(new TextureRegionDrawable(ChessAssetManager.current().findRegion("iw_quote"))));
        part1.add(quote).padLeft(5).expandX().fillX().row();
    }

    private void addLoginPanel(Account account, RdTable part2) {
        RdLabel loginHint = new RdLabel(strings.get("[i18n]Login") + ": ");
        RdLabel passwordHint = new RdLabel(strings.get("[i18n]Password:"));
        RdLabel password = new RdLabel("******");
        password.setAlignment(Align.right);

        LineTable lineTable1 = new LineTable(strings.get("[i18n]Login options:"));
        lineTable1.add(loginHint).expandX().fillX();
        lineTable1.add(new RdLabel("[GREEN]" + account.getUsername())).row();
        lineTable1.add(passwordHint).expandX().fillX();
        lineTable1.add(password).right().row();

        part2.add(lineTable1).expandX().fillX().row();
    }

    private void addChangePassword(Account account, RdTable part3, boolean self) {
        var lineTable2 = new LineTable(strings.get("[i18n]Change password"));

        var inputPassword = new RdTextArea("", ChessAssetManager.current().getSkin());
        inputPassword.setMaxLength(20);
        inputPassword.setPasswordCharacter('*');
        inputPassword.setPasswordMode(true);
        inputPassword.setMessageText(strings.get("[i18n]enter..."));

        var confPassword = new RdTextArea("", ChessAssetManager.current().getSkin());
        confPassword.setMaxLength(20);
        confPassword.setPasswordMode(true);
        confPassword.setPasswordCharacter('*');
        confPassword.setMessageText(strings.get("[i18n]enter..."));
        var changePassword = new RdTextButton(strings.get("[i18n]Change"));

        changePassword.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                if (inputPassword.getText().length() < 6) {
                    ChessApplication.self().showInfo(strings.get("[i18n]The password should be at least 6 characters"));
                    return;
                }
                if (!inputPassword.getText().equals(confPassword.getText())) {
                    ChessApplication.self().showInfo(strings.get("[1i8n]Password mismatch!"));
                    return;
                }

                inputPassword.setText("");
                confPassword.setText("");
                account.setPassword(inputPassword.getText());
                MultiplayerEngine.self().changeAccount(account, requestStatus -> {
                    if (requestStatus != RequestStatus.DONE) {
                        ChessApplication.self().showError(strings.get("[i18n]Password change not confirmed, error status - ") + requestStatus);
                    } else {
                        ChessApplication.self().showAccept(strings.get("[i18n]Password changes confirmed and sent"));
                    }
                });

                if (self) {
                    ChessConstants.localData.setPassword(inputPassword.getText());
                }
            }
        });

        lineTable2.add(inputPassword).expandX().fillX().row();
        lineTable2.add(confPassword).padTop(5).expandX().fillX();
        lineTable2.add(changePassword).padLeft(5).minWidth(250).right();
        lineTable2.invalidateHierarchy();

        part3.add(lineTable2).expandX().fillX();
    }

    private void addRankPanel(Account account, RdTable part4) {
        RdLabel bullet = new RdLabel("[_]" + strings.get("[i18n]Bullet") + "[]: ");
        bullet.setAlignment(Align.topLeft);
        RdLabel blitz = new RdLabel("[_]" + strings.get("[i18n]Blitz") + "[]: ");
        blitz.setAlignment(Align.topLeft);
        RdLabel rapid = new RdLabel("[_]" + strings.get("[i18n]Rapid") + "[]: ");
        rapid.setAlignment(Align.topLeft);
        RdLabel longRank = new RdLabel("[_]" + strings.get("[i18n]Long") + "[]: ");
        longRank.setAlignment(Align.topLeft);

        part4.add(bullet).expandX().align(Align.left);
        part4.add(new RdLabel(String.valueOf(account.getBullet()))).row();
        part4.add(blitz).expandX().align(Align.left);
        part4.add(new RdLabel(String.valueOf(account.getBlitz()))).row();
        part4.add(rapid).expandX().align(Align.left);
        part4.add(new RdLabel(String.valueOf(account.getRapid()))).row();
        part4.add(longRank).expandX().align(Align.left);
        part4.add(new RdLabel(String.valueOf(account.getLongRank()))).row();
    }

    private void updateAvatar(AvatarView avatarView) {
        MultiplayerEngine.self().requireAvatar(ChessConstants.loggingAcc,
            bytes -> {
                ChessConstants.accountPanel
                    .update(ChessConstants.loggingAcc, bytes);
                avatarView.update(ChessConstants.loggingAcc, bytes);
            });
    }

    private void loadAvatar(FileHandle handle, Consumer<byte[]> onFinish) {

        Spinner spinner = ChessApplication.self().showSpinner(strings.get("[i18n]Loading"));
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
        if (account.getType() == AccountType.DEVELOPER) status.append(strings.get("[i18n]Developer"));
        else if (account.getType() == AccountType.EXECUTOR) status.append(strings.get("[i18n]Executor"));
        else if (account.getType() == AccountType.MODERATOR) status.append(strings.get("[i18n]Moderator"));
        else status.append(strings.get("[i18n]User"));

        boolean warned = false;
        boolean muted = false;
        boolean banned = false;
        for (var flag : account.getFlags()) {
            if (flag == Flag.WARN) warned = true;
            else if (flag == Flag.MUTE) muted = true;
            else if (flag == Flag.BAN) banned = true;
        }


        if (banned) {
            status.append("\n");
            status.append("[red]");
            status.append(strings.get("[i18n]Banned"));
        }
        else if (muted) {
            status.append("\n");
            status.append("[orange]");
            status.append(strings.get("[i18n]Muted"));
        }
        else if (warned) {
            status.append("\n");
            status.append("[yellow]");
            status.append(strings.get("[i18n]Warned"));
        }

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

    private boolean isASCII(String s) {
        for (char c : s.toCharArray()) {
            if (c > 127) return false;
        }
        return true;
    }

    private boolean isSelf(Account account) {
        return isSelf(account.getId());
    }

    private boolean isSelf(long accountId) {
        return accountId == ChessConstants.loggingAcc.getId();
    }
}
