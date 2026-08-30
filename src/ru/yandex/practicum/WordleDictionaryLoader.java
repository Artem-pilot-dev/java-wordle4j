
package ru.yandex.practicum;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class WordleDictionaryLoader {

    private final PrintWriter log;

    public WordleDictionaryLoader(PrintWriter log) {

        this.log = log;
    }

    public WordleDictionary load(String fileName) throws ProgramException {
        List<String> sourceWords = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new FileReader(fileName, StandardCharsets.UTF_8))) {
            String line;

            while ((line = reader.readLine()) != null) {
                sourceWords.add(line);
            }
        } catch (IOException exception) {
            throw new ProgramException("Не удалось прочитать словарь " + fileName, exception);
        }

        log.println("Из файла " + fileName + " прочитано строк: " + sourceWords.size());
        return new WordleDictionary(sourceWords, log);
    }
}
