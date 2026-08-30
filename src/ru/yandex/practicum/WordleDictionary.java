package ru.yandex.practicum;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WordleDictionary {

    private List<String> words;

    private PrintWriter log;

    public WordleDictionary(List<String> sourceWords, PrintWriter log) throws ProgramException {
        this.words = new ArrayList<>();
        this.log = log;

        for (String sourceWord : sourceWords) {
            String word = normalize(sourceWord);

            if (isCorrectWord(word) && !words.contains(word)) {
                words.add(word);
            }
        }

        Collections.sort(words);

        if (words.isEmpty()) {
            throw new ProgramException("В словаре нет русских слов из пяти букв.");
        }

        log.println("Игровой словарь содержит слов: " + words.size());
    }

    public int size() {

        return words.size();
    }

    public String get(int index) {

        return words.get(index);
    }

    public boolean contains(String sourceWord) {
        String word = normalize(sourceWord);
        return words.contains(word);
    }

    public List<String> getWords() {

        return new ArrayList<>(words);
    }

    public static String normalize(String sourceWord) {
        if (sourceWord == null) {
            return "";
        }

        return sourceWord.trim()
                .toLowerCase()
                .replace('ё', 'е');
    }

    public static boolean isCorrectWord(String word) {
        if (word == null) {
            return false;
        }

        return word.matches("[а-я]{5}");
    }

}

