package org.example;

public class RankingEntry {
    private String player;
    private int score;

    public RankingEntry(String player, int score) {
        this.player = player;
        this.score = score;
    }

    public String getPlayer() {
        return player;
    }

    public int getScore() {
        return score;
    }
}
