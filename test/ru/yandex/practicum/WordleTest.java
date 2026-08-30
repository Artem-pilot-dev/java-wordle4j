package ru.yandex.practicum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.PrintWriter;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class WordleTest {

    private PrintWriter log;

    private WordleDictionary dictionary;

    private WordleGame game;

    @BeforeEach
    void prepareGame() throws ProgramException {
        log = new PrintWriter(System.out, true);
        dictionary = new WordleDictionary(
                Arrays.asList("герой", "гонец", "берег", "ветер", "номер", "пирог", "ручка"), log);
        game = new WordleGame(dictionary, "герой", log);
    }

    @Test
    void dictionaryNormalizesWords() {
        assertTrue(dictionary.contains("ГЕРОЙ"));
        assertEquals("берег", WordleDictionary.normalize(" БЕРЁГ "));
        assertFalse(dictionary.contains("house"));
    }

    @Test
    void comparesWordsFromTaskExample() {
        assertEquals("+^-^-", WordleGame.compareWords("гонец", "герой"));
    }

    @Test
    void repeatedLettersAreComparedCorrectly() {
        assertEquals("+++--", WordleGame.compareWords("ветер", "ветка"));
        assertEquals("^^-+-", WordleGame.compareWords("сосна", "осень"));
    }

    @Test
    void incorrectInputDoesNotUseAttempt() throws Exception {
        try {
            game.makeMove("дом");
            fail("Ожидалось исключение GameException");
        } catch (GameException expected) {
            assertEquals(6, game.getSteps());
        }

        try {
            game.makeMove("слово");
            fail("Ожидалось исключение GameException");
        } catch (GameException expected) {
            assertEquals(6, game.getSteps());
        }
    }

    @Test
    void correctAnswerFinishesGame() throws GameException {
        String result = game.makeMove("ГЕРОЙ");

        assertEquals("+++++", result);
        assertTrue(game.isWon());
        assertTrue(game.isFinished());
        assertEquals(5, game.getSteps());
    }

    @Test
    void hintMatchesPreviousResults() throws GameException {
        String firstResult = game.makeMove("гонец");
        String hint = game.suggestWord();

        assertNotEquals("гонец", hint);
        assertTrue(dictionary.contains(hint));
        assertEquals(firstResult, WordleGame.compareWords("гонец", hint));
    }

}

