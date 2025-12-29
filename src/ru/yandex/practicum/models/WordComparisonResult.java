package ru.yandex.practicum.models;

import java.util.List;

public class WordComparisonResult {
    private final List<LetterResult> letterResults;
    private final String analysis;
    private final boolean correct;
    private String guess;

    public WordComparisonResult(List<LetterResult> letterResults, String analysis, boolean correct) {
        this.letterResults = letterResults;
        this.analysis = analysis;
        this.correct = correct;
    }


    public List<LetterResult> getLetterResults() {
        return letterResults;
    }

    public String getAnalysis() {
        return analysis;
    }

    public boolean isCorrect() {
        return correct;
    }

    public String getGuess() {
        return guess;
    }


    public void setGuess(String guess) {
        this.guess = guess;
    }
}
