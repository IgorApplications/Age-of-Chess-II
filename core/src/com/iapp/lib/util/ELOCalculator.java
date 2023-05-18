package com.iapp.lib.util;

/**
 * Singleton, which calculates the probability of two players winning
 * and their rating after winning/losing
 * */
public class ELOCalculator {

    private static final float COEFFICIENT = 256;
    private static final ELOCalculator INSTANCE = new ELOCalculator();

    public static ELOCalculator getInstance() {
        return INSTANCE;
    }

    /**
     * calculates the probability of one player winning over another
     * @return a number less than 1
     * */
    public double getProbability(double probablyWinner, double probableLoser) {
        return (1.0f / (1 + Math.pow(10, (probableLoser - probablyWinner) / 400)));
    }

    /**
     * calculates the new rating for two players,
     * after the victory of one of them
     * @return new ELO for two players
     * */
    public Pair<Double, Double> calculateELO(double winner, double second) {

        // probability of the second player winning
        double pb = getProbability(second, winner);

        // probability of winning the winner
        double pa = getProbability(winner, second);

        // Case -1 When Player A wins
        // Updating the Elo Ratings
        winner = winner + COEFFICIENT * (1 - pa);
        second = second + COEFFICIENT * (0 - pb);

        return new Pair<>(winner, second);
    }
}
