package com.iapp.lib.ui.widgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Align;
import com.iapp.lib.ui.actors.*;
import com.iapp.lib.ui.screens.RdApplication;
import com.iapp.lib.util.OnChangeListener;
import com.iapp.lib.util.RdI18NBundle;
import com.iapp.lib.util.StringsGenerator;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TranslationDialog extends RdDialog {

    private final RdI18NBundle strings;
    private RdSelectBox<String> languages;
    private RdList<String> list;
    private RdLabel word;
    private RdTextArea currentLangInput;
    // TODO
    private Map<String, String> english = new HashMap<>();
    private Map<String, String> current = new HashMap<>();
    private final Map<String, Map<String, String>> changedWords = new HashMap<>();

    public TranslationDialog(String title) {
        super(title);
        strings = RdApplication.self().getStrings();
        init();
    }

    public TranslationDialog(String title, String styleName) {
        super(title, styleName);
        strings = RdApplication.self().getStrings();
        init();
    }

    private void init() {
        // write without loader
        setOnCancel(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                saveChangedWords();
                hide();
            }
        });

        languages = new RdSelectBox<>();
        languages.setItems(RdApplication.self().getDisplayLanguages());

        // actors
        list = new RdList<>();
        RdScrollPane scrollPane = new RdScrollPane(list);
        scrollPane.setScrollingDisabled(true, false);

        RdTable keysTable = new RdTable();
        keysTable.add(languages).expandX().fillX().row();
        keysTable.add(scrollPane).expand().fill().row();

        word = new RdLabel("");
        word.setWrap(true);
        currentLangInput = new RdTextArea("");
        RdTextButton apply = new RdTextButton(strings.get("[i18n=apply]apply"), "blue");

        RdTable wordsTable = new RdTable();
        wordsTable.add(word).expandX().fillX().row();
        wordsTable.add(currentLangInput).expand().fill().row();
        wordsTable.add(apply).expandX().row();

        languages.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                updateLanguage();
            }
        });

        list.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                updateWord();
            }
        });

        apply.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                current.put(list.getSelected(), currentLangInput.getText());
                changedWords.put(RdApplication.self().getLanguageCodes()[languages.getSelectedIndex()],
                    current);
            }
        });

        // content table
        getContentTable().align(Align.topLeft);
        getContentTable().add(keysTable).expandY().fill().maxWidth(500);
        getContentTable().add(wordsTable).expand().fill();

        updateLanguage();
    }

    private void saveChangedWords() {
        try {
            for (Map<String, String> langWords : changedWords.values()) {
                StringsGenerator.self().writeWords(
                    RdApplication.self().getLanguageCodes()[languages.getSelectedIndex()], langWords);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateWord() {
        word.setText(english.get(list.getItems().get(list.getSelectedIndex())));
        currentLangInput.setText(current.get(list.getItems().get(list.getSelectedIndex())));
    }

    private void updateLanguage() {
        /*try {
            // TODO
            //english = StringsGenerator.self().readWords("en");
            //current = StringsGenerator.self().readWords(
              //  RdApplication.self().getLanguageCodes()[languages.getSelectedIndex()]);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }*/
        english = new HashMap<>();
        current = new HashMap<>();

        list.setItems(english.keySet().toArray(new String[0]));
    }
}
