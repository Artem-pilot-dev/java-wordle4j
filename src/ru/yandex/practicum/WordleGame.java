package ru.yandex.practicum;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class WordleGame {
    private final String answer;
    private int steps;
    private final WordleDictionary dictionary;
    private final PrintWriter log;
    private boolean won;
    private final List<String> enteredWords;
    private final List<String> results;
    private final Set<String> suggestedWords;

    public WordleGame(WordleDictionary dictionary, PrintWriter log) throws ProgramException {
        this(dictionary, chooseAnswer(dictionary), log);
    }

    public WordleGame(WordleDictionary dictionary, String answer, PrintWriter log) throws ProgramException {
        if (dictionary == null) {
            throw new ProgramException("Словарь игры не создан.");
        }

        this.dictionary = dictionary;
        this.log = log;
        this.answer = WordleDictionary.normalize(answer);
        this.steps = 6;
        this.won = false;
        this.enteredWords = new ArrayList<>();
        this.results = new ArrayList<>();
        this.suggestedWords = new HashSet<>();

        if (!dictionary.contains(this.answer)) {
            throw new ProgramException("Загаданного слова нет в словаре.");
        }

        writeStateToLog("Игра создана");
    }

    private static String chooseAnswer(WordleDictionary dictionary) throws ProgramException {
        if (dictionary == null || dictionary.size() == 0) {
            throw new ProgramException("Нельзя выбрать слово из пустого словаря.");
        }

        Random random = new Random();
        int index = random.nextInt(dictionary.size());
        return dictionary.get(index);
    }

    public String makeMove(String sourceWord) throws GameException {
        if (isFinished()) {
            throw new GameException("Игра уже завершена.");
        }

        String word = WordleDictionary.normalize(sourceWord);

        if (!WordleDictionary.isCorrectWord(word)) {
            throw new GameException("Введите русское слово ровно из пяти букв.");
        }

        if (!dictionary.contains(word)) {
            throw new GameException("Слова \"" + word + "\" нет в словаре.");
        }

        String result = compareWords(word, answer);
        enteredWords.add(word);
        results.add(result);
        steps--;

        if (word.equals(answer)) {
            won = true;
        }

        writeStateToLog("Ход: " + word + "; результат: " + result);
        return result;
    }

    public String suggestWord() throws GameException {
        if (isFinished()) {
            throw new GameException("Нельзя получить подсказку: игра завершена.");
        }

        List<String> candidates = new ArrayList<String>();
        List<String> allWords = dictionary.getWords();

        for (String word : allWords) {
            if (matchesPreviousMoves(word) && !enteredWords.contains(word) && !suggestedWords.contains(word)) {
                candidates.add(word);
            }
        }

        if (candidates.isEmpty()) {
            throw new GameException("Компьютер не смог подобрать новую подсказку.");
        }

        String suggestion = chooseBestCandidate(candidates);
        suggestedWords.add(suggestion);

        log.println("Подсказка: " + suggestion + "; подходящих вариантов: " + candidates.size());

        return suggestion;
    }

    private boolean matchesPreviousMoves(String possibleAnswer) {
        for (int index = 0; index < enteredWords.size(); index++) {
            String oldWord = enteredWords.get(index);
            String oldResult = results.get(index);
            String newResult = compareWords(oldWord, possibleAnswer);

            if (!oldResult.equals(newResult)) {
                return false;
            }
        }

        return true;
    }

    private String chooseBestCandidate(List<String> candidates) {
        Map<Character, Integer> letterFrequency = calculateLetterFrequency(candidates);
        String bestWord = candidates.get(0);
        int bestScore = getWordScore(bestWord, letterFrequency);

        for (int index = 1; index < candidates.size(); index++) {
            String candidate = candidates.get(index);
            int candidateScore = getWordScore(candidate, letterFrequency);

            if (candidateScore > bestScore) {
                bestWord = candidate;
                bestScore = candidateScore;
            }
        }

        return bestWord;
    }

    private Map<Character, Integer> calculateLetterFrequency(List<String> words) {
        Map<Character, Integer> frequency = new HashMap<Character, Integer>();

        for (String word : words) {
            Set<Character> uniqueLetters = new HashSet<Character>();

            for (int index = 0; index < word.length(); index++) {
                uniqueLetters.add(word.charAt(index));
            }

            for (Character letter : uniqueLetters) {
                Integer count = frequency.get(letter);

                if (count == null) {
                    frequency.put(letter, 1);
                } else {
                    frequency.put(letter, count + 1);
                }
            }
        }

        return frequency;
    }

    private int getWordScore(String word, Map<Character, Integer> frequency) {
        Set<Character> uniqueLetters = new HashSet<>();
        int score = 0;

        for (int index = 0; index < word.length(); index++) {
            uniqueLetters.add(word.charAt(index));
        }

        for (Character letter : uniqueLetters) {
            Integer letterScore = frequency.get(letter);

            if (letterScore != null) {
                score = score + letterScore;
            }
        }

        return score;
    }

    public static String compareWords(String sourceWord, String sourceAnswer) {
        String word = WordleDictionary.normalize(sourceWord);
        String correctAnswer = WordleDictionary.normalize(sourceAnswer);

        if (!WordleDictionary.isCorrectWord(word) || !WordleDictionary.isCorrectWord(correctAnswer)) {
            throw new IllegalArgumentException("Для сравнения нужны два слова из пяти букв.");
        }

        char[] result = new char[5];
        Arrays.fill(result, '-');
        Map<Character, Integer> unusedLetters = new HashMap<>();

        for (int index = 0; index < 5; index++) {
            char wordLetter = word.charAt(index);
            char answerLetter = correctAnswer.charAt(index);

            if (wordLetter == answerLetter) {
                result[index] = '+';
            } else {
                Integer count = unusedLetters.get(answerLetter);

                if (count == null) {
                    unusedLetters.put(answerLetter, 1);
                } else {
                    unusedLetters.put(answerLetter, count + 1);
                }
            }
        }

        for (int index = 0; index < 5; index++) {
            if (result[index] == '+') {
                continue;
            }

            char wordLetter = word.charAt(index);
            Integer count = unusedLetters.get(wordLetter);

            if (count != null && count > 0) {
                result[index] = '^';
                unusedLetters.put(wordLetter, count - 1);
            }
        }

        return new String(result);
    }

    private void writeStateToLog(String message) {
        log.println(message + "; осталось попыток: " + steps + "; победа: " + won + "; ответ: " + answer
                + "; введённые слова: " + enteredWords);
    }

    public boolean isFinished() {

        return won || steps == 0;
    }

    public boolean isWon() {

        return won;
    }

    public int getSteps() {

        return steps;
    }

    public String getAnswer() {

        return answer;
    }

    public List<String> getEnteredWords() {

        return new ArrayList<>(enteredWords);
    }

}

