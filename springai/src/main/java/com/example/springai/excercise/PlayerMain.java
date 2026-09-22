package com.example.springai.excercise;

public class PlayerMain {

    static void main() {
        Player p1 = new Player("Rohit", 40, "MI", new Ranking(1, 100));

        p1.print();

        Ranking rank = p1.getRanking();
        rank.setBattingRank(2);

        p1.print();
    }
}
