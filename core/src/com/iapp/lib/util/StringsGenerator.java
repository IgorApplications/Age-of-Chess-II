package com.iapp.lib.util;

import com.iapp.lib.ui.screens.Launcher;
import com.iapp.lib.ui.screens.RdApplication;
import com.iapp.lib.ui.screens.RdAssetManager;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Automatic generator of configuration files from source code to <bold>i18n</bold>
 * text that can be translated into other languages;
 * Example for source code: [i18n]text
 * @author Igor Ivanov
 * */
public class StringsGenerator {

    private static final StringsGenerator INSTANCE = new StringsGenerator();
    private static final String FILE_PATH = "/assets/languages/lang_";
    private static final String FOLDER_PATH = "/assets/languages";

    public static StringsGenerator self() {
        return INSTANCE;
    }

    public static void main(String[] args) throws IOException {

        RdApplication application = new RdApplication(new Launcher() {
            @Override
            public void initPool(int threads) {}
            @Override
            public void execute(Runnable task) {}
        }, 0, 0, null, 0) {
            @Override
            public void launch(RdAssetManager rdAssetManager) {}
        };
        self().createNonExistent(application);

        List<Long> removedKeys = self().updateEnglish();
        for (String langCode : application.getLanguageCodes()) {
            if (langCode.equals("en")) continue;

            Map<Long, String> oldString = self().readWords(langCode);
            for (long key : removedKeys) {
                oldString.remove(key);
            }

            if (!removedKeys.isEmpty()) {
                System.out.printf("langCode = %s, removedKeys = %s%n", langCode, removedKeys);
                self().removeOldKeysFromFile(langCode, removedKeys);
            }
        }

    }

    public Map<Long, String> readWords(String langCode) throws IOException {
        Map<Long, String> words = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(new FileInputStream(
                new File("").getAbsolutePath() + FILE_PATH + langCode + ".properties"),
                StandardCharsets.UTF_8)
        )) {
            while (reader.ready()) {
                String line = reader.readLine();
                if (line.startsWith("#")) continue;
                String[] tokens = line.split("=");
                if (tokens.length < 2) continue;
                words.put(Long.parseLong(tokens[0]), tokens[1]);
            }
        }

        return words;
    }

    // not english! English generate automatically!
    public void writeWords(String langCode, Map<String, String> strings) throws IOException {
        if (langCode.equals("en")) return;

        try (BufferedWriter writer = new BufferedWriter(
            new OutputStreamWriter(new FileOutputStream(
            new File("").getAbsolutePath() + FILE_PATH + langCode + ".properties", false),
            StandardCharsets.UTF_8))) {

            for (Map.Entry<String, String> entry : strings.entrySet()) {
                writer.write(entry.getKey() + "=" + entry.getValue());
                writer.newLine();
            }
        }
    }

    private List<Long> updateEnglish() throws IOException {
        // GENERATE FROM CODE HERE!
        Pair<Set<String>, List<String>> pair = self().generateWordsFromCode();

        Set<String> wordValues = pair.getKey();
        List<String> wordValuesWithDoc = pair.getValue();
        List<String> generatedKeyByWords = new ArrayList<>();

        Map<Long, String> oldString = self().readWords("en");
        List<Long> removedKeys = new ArrayList<>();

        for (Map.Entry<Long, String> entry : oldString.entrySet()) {

            if (!wordValues.contains(entry.getValue())) {
                removedKeys.add(entry.getKey());
            }
        }

        Set<Long> genNewId = new HashSet<>();
        Set<String> written = new HashSet<>();
        for (String line : wordValuesWithDoc) {

            if (written.contains(line)) {
                continue;
            }

            if (line.equals("") || line.startsWith("#")) {
                generatedKeyByWords.add(line);
                continue;
            }

            if (oldString.containsValue(line)) {
                long foundId = self().findId(oldString, line);
                generatedKeyByWords.add(foundId + "=" + line);
                written.add(line);
            } else {
                // Working with the link! Some keys may be restored (see removedKeys)
                long newId = self().defineLastId(oldString, removedKeys, genNewId);
                genNewId.add(newId);
                generatedKeyByWords.add(newId + "=" + line);
                written.add(line);
            }
        }

        self().writeStrings(generatedKeyByWords, "en");
        return removedKeys;
    }

    private Pair<Set<String>, List<String>> generateWordsFromCode() throws IOException {
        Set<String> lines = new HashSet<>();
        List<String> linesWithDoc = new ArrayList<>();

        linesWithDoc.add("# AUTOMATICALLY GENERATED FROM JAVA SOURCE CODE");
        linesWithDoc.add("# DO NOT MANUALLY EDIT");

        for (File file : readAllJavaFiles()) {

            linesWithDoc.add("");
            linesWithDoc.add("# " + file.getAbsolutePath().replace("\\", ".")
                .replaceAll(".*\\.core\\.src\\.", ""));
            linesWithDoc.add("");

            try (BufferedReader classReader = new BufferedReader(new FileReader(file))) {
                while (classReader.ready()) {

                    String newLine = classReader.readLine();
                    String[] data = newLine.split("\\[i18n]");

                    if (data.length == 1) continue;

                    for (int i = 1; i < data.length; i++) {
                        String el = data[i];

                        // TODO
                        int end = el.indexOf("\"");
                        if (end == -1) continue;

                        // TODO DEVELOPER PATH

                        linesWithDoc.add(el.substring(0, end));

                        lines.add(el.substring(0, end));
                    }
                }
            }

        }

        return new Pair<>(lines, linesWithDoc);
    }

    /**
     * Removes obsolete keys everywhere.
     * Obsolete keys are keys that are not contained in the English version.
     * */
    private void removeOldKeysFromFile(String langCode, List<Long> oldKeys) throws IOException {
        List<String> lines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(
            new File("").getAbsolutePath() + FILE_PATH + langCode + ".properties"))) {
            while (reader.ready()) {
                lines.add(reader.readLine());
            }
        }

        for (long oldKey : oldKeys) {
            List<String> removedLines = new ArrayList<>();
            for (String line : lines) {
                if (line.startsWith(oldKey + "=")) {
                    removedLines.add(line);
                }
            }

            for (String removedLine : removedLines) {
                lines.remove(removedLine);
            }
        }

        writeStrings(lines, langCode);
    }

    private void writeStrings(List<String> lines, String langCode) throws IOException {

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
            new FileOutputStream(new File("").getAbsolutePath() + FILE_PATH + langCode + ".properties",
                false), StandardCharsets.UTF_8))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        }
    }

    public List<File> readAllJavaFiles() {
        List<File> files = new ArrayList<>();
        LinkedList<File> folders = new LinkedList<>();
        folders.add(new File(new File("").getAbsolutePath() + "/core/src/com/iapp"));

        while (!folders.isEmpty()) {

            folders.removeIf(file -> {
                if (!file.isDirectory() && !file.getName().equals(getClass().getSimpleName() + ".java") ) {

                    files.add(file);
                    return true;
                }
                return false;
            });

            LinkedList<File> copyFolders = new LinkedList<>(folders);
            folders.clear();
            for (File file : copyFolders) {
                File[] listFiles = file.listFiles();
                if (listFiles == null) continue;
                folders.addAll(List.of(listFiles));
            }

        }

        return files;
    }

    /**
     * Creates files with translations in languages translations that do not yet exist
     * */
    private void createNonExistent(RdApplication application) throws IOException {

        File dir = new File(new File("").getAbsolutePath() + FOLDER_PATH);
        if (!dir.exists()) {
            boolean result = dir.mkdir();
            if (!result) {
                throw new IOException("Directory languages don't created");
            }
        }

        for (String langCode : application.getLanguageCodes()) {
            File file = new File(dir.getPath() + "/lang_" + langCode + ".properties");

            if (file.exists()) continue;

            try (FileWriter fileWriter = new FileWriter(file, false)) {
                fileWriter.write("");
            }

            System.out.printf("file langCode = %s overwritten%n", langCode);
        }
    }

    private String getClassPath(File file) {
        return file.getAbsolutePath().replace("\\", ".")
            .replaceAll(".*\\.core\\.src\\.", "");
    }

    private long defineLastId(Map<Long, String> oldString, List<Long> removedKeys, Set<Long> genNewId) {
        if (!removedKeys.isEmpty()) {
            return removedKeys.remove(removedKeys.size() - 1);
        }

        for (long id = 0; id < Long.MAX_VALUE - 100; id++) {
            if (!oldString.containsKey(id) && !genNewId.contains(id)) {
                return id;
            }
        }

        throw new IllegalStateException("The number of identifiers in positive numbers is overflowed!!!");
    }

    private long findId(Map<Long, String> oldWords, String newValue) {
        for (Map.Entry<Long, String> entry : oldWords.entrySet()) {
            if (entry.getValue().equals(newValue)) {
                return entry.getKey();
            }
        }
        throw new IllegalStateException("There is no such text in the old words from the previous launch!!!");
    }

    private long getCountLineOfCode() {

        long count = 0;
        // only core
        System.out.println(readAllJavaFiles());
        for (File file : readAllJavaFiles()) {
            if (!file.getPath().endsWith(".java")) continue;

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                while (reader.ready()) {
                    reader.readLine();
                    count++;
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return count;
    }

    private StringsGenerator() {}
}
