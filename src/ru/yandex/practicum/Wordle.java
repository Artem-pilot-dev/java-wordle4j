package ru.yandex.practicum;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;

public class Wordle {

    private static final String DEFAULT_DICTIONARY_FILE = "words_ru.txt";
    private static final String LOG_FILE = "wordle.log";

    public static void main(String[] args) {
        try (PrintWriter log = createLog()) {
            try {
                startGame(log);
            } catch (Exception exception) {
                log.println("Критическая ошибка программы:");
                exception.printStackTrace(log);
                log.flush();
            }
        } catch (ProgramException exception) {
            System.err.println(exception.getMessage());
        }
    }

    private static void startGame(PrintWriter log) throws ProgramException {

        WordleDictionaryLoader loader = new WordleDictionaryLoader(log);
        WordleDictionary dictionary = loader.load(DEFAULT_DICTIONARY_FILE);
        WordleGame game = new WordleGame(dictionary, log);

        System.out.println("Игра Wordle");
        System.out.println("Введите русское слово из пяти букв.");
        System.out.println("Чтобы получить подсказку, нажмите Enter.");

        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {
            while (!game.isFinished()) {
                System.out.println();
                System.out.println("Осталось попыток: " + game.getSteps());
                System.out.print("> ");

                if (!scanner.hasNextLine()) {
                    log.println("Пользователь закрыл поток ввода.");
                    break;
                }

                String input = scanner.nextLine();
                String word = input;

                try {
                    if (input.trim().isEmpty()) {
                        word = game.suggestWord();
                        System.out.println(word + " (подсказка компьютера)");
                    }

                    String result = game.makeMove(word);
                    System.out.println(WordleDictionary.normalize(word));
                    System.out.println(result);
                } catch (GameException exception) {
                    System.out.println(exception.getMessage());
                    log.println("Ошибка ввода пользователя: " + exception.getMessage());
                }
            }
        }

        if (game.isWon()) {
            System.out.println("\nСлово отгадано!");
        } else if (game.getSteps() == 0) {
            System.out.println("\nПопытки закончились.");
        } else {
            System.out.println("\nИгра остановлена.");
        }

        System.out.println("# загаданное слово: " + game.getAnswer());
    }

    private static PrintWriter createLog() throws ProgramException {
        try {
            BufferedWriter writer = Files.newBufferedWriter(
                    Path.of(LOG_FILE),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );

            return new PrintWriter(writer, true);
        } catch (IOException exception) {
            throw new ProgramException("Не удалось создать лог-файл " + LOG_FILE, exception);
        }
    }
}

