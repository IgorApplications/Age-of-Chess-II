package com.iapp.lib.util;


import java.io.*;
import java.util.*;

// dangerous
public class I18nConverter {

    private static final List<String> IGNORE = new ArrayList<>();

    static {

        IGNORE.add(I18nConverter.class.getSimpleName() + ".java");
        IGNORE.add(StringsGenerator.class.getSimpleName() + ".java");
        IGNORE.add(RdI18NBundle.class.getSimpleName() + ".java");

    }

    public static void main(String[] args) throws IOException {
        new I18nConverter().convertI18NtoCode();
    }

    private void convertI18NtoCode() throws IOException {
        Map<Long, String> data = StringsGenerator.self().readWords("en");
        Map<String, Long> valueById = new HashMap<>();
        for (Map.Entry<Long, String> entry : data.entrySet()) {
            valueById.put(entry.getValue(), entry.getKey());
        }

        for (File file : StringsGenerator.self().readAllJavaFiles()) {

            if (IGNORE.contains(file.getName())) {
                continue;
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(file));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {

                List<String> lines = new ArrayList<>();
                while (reader.ready()) {
                    String line = reader.readLine();
                    String[] arr = line.split("\"\\[i18n]");

                    if (arr.length <= 1) {
                        lines.add(line);
                        continue;
                    }

                    StringBuilder res = new StringBuilder(arr[0]);
                    for (int i = 1; i < arr.length; i++) {
                        String s = arr[i];

                        int index = s.indexOf("\"");
                        if (index == 0) continue;

                        while (s.charAt(index - 1) == '\\') {
                            index = s.indexOf("\"", index + 1);
                        }

                        res.append("\"").append(valueById.get(s.substring(0, index))).append("\"").append(s.substring(index + 1));
                    }

                    lines.add(res.toString());
                }

                BufferedWriter rem = new BufferedWriter(new FileWriter(file));
                rem.write("");
                rem.close();

                for (int i = 0; i < lines.size() - 1; i++) {
                    writer.write(lines.get(i));
                    writer.newLine();
                }
                writer.write(lines.get(lines.size() - 1));
            }
        }
    }

    private void convertCodeToI18N() {}
}

