package me.lucaspickering.utils;

import java.util.stream.IntStream;

public class Utils {
    public static int factorial(int n) {
        return Utils.factorial(1, n);
    }

    public static int factorial(int from, int to) {
        return IntStream.range(from, to + 1).reduce((a, b) -> a * b).orElse(1);
    }

    public static int combination(int n, int k) {
        // safety check to prevent overflow/underflow
        if (n < 0 || k < 0 || k > n) {
            throw new IllegalArgumentException(
                    String.format("n and k must both be non-negative, and k <= n, got: n=%d, k=%d", n, k));
        }

        // This is a reduction of the formula `n! / ((n - k)! * k!)`. I've cut it
        // down to:
        // ((n - k + 1) * (n - k + 2) * ... * (n - 1) * n) / k!
        // This reduces the max values we reach, so we stay in bounds for int
        return Utils.factorial(n - k + 1, n) / Utils.factorial(k);
    }

    /**
     * Calculate the binomial distribution. This calculates the odds of getting
     * **exactly** `k` successes in `n` trials, where each trial has `p`
     * probability of success.
     * https://en.wikipedia.org/wiki/Binomial_distribution
     *
     * @param p Probability of a single success, in range [0, 1]
     * @param n Total number of trials
     * @param k Exactly number of desired successes
     * @return Odds of exactly k successes in n trials, in [0, 1]
     */
    public static double binomial(double p, int n, int k) {
        // Validate inputs
        if (p < 0.0 || p > 1.0) {
            throw new IllegalArgumentException(String.format("Probability must be in [0, 1], got: %d", p));
        }

        return Math.pow(p, k) * Math.pow(1.0 - p, n - k) * Utils.combination(n, k);
    }
}
