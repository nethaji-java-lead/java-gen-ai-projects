package com.example.springai.excercise;

public class Ranking {

    private int battingRank;

    public Ranking(int battingRank, int bowlingRank) {
        this.battingRank = battingRank;
        this.bowlingRank = bowlingRank;
    }

    public int getBattingRank() {
        return battingRank;
    }

    public void setBattingRank(int battingRank) {
        this.battingRank = battingRank;
    }

    public int getBowlingRank() {
        return bowlingRank;
    }

    public void setBowlingRank(int bowlingRank) {
        this.bowlingRank = bowlingRank;
    }

    private int bowlingRank;

}
