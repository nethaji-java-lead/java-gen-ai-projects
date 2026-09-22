package com.example.springai.excercise;

public final class Player {

    private final String name;
    private final int age;
    private final String team;
    private final Ranking ranking;

    public Player(String name, int age, String team, Ranking ranking) {
        this.name = name;
        this.age = age;
        this.team = team;
        this.ranking = ranking;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getTeam() {
        return team;
    }

    public Ranking getRanking() {
        return new Ranking(ranking.getBattingRank(), ranking.getBattingRank());
    }

    public void print() {
        System.out.println("Name: " + this.name + " Age: " + this.age + " Team: "
                + this.team + " Batting: " + ranking.getBattingRank() +
                " Bowling: " + ranking.getBowlingRank());
    }
}