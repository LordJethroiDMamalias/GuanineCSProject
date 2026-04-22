package codes;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * RyanPool — question pool for the RYAN boss.
 *
 * Four statistics categories, chosen randomly each attack:
 *   1. Mean        — given a dataset of 4–6 integers, find the mean
 *   2. Median      — given a shuffled dataset of 5 integers, find the median
 *   3. Mode        — given a dataset where one value appears 3×, find the mode
 *   4. Probability — marble bag, 6-sided die, or coin flip
 *
 * Mean, median, and mode answers are whole integers (isNumeric = true).
 * Probability answers are reduced fractions, e.g. "1/6" (isNumeric = false,
 * case-insensitive string match).
 */
public class RyanPool implements QuestionPool.Pool {

    private static final Random RNG = new Random();

    // =========================================================================
    // Public factory — implements QuestionPool.Pool
    // =========================================================================

    @Override
    public ChallengeDialog.Question next() {
        return switch (RNG.nextInt(4)) {
            case 0  -> meanQuestion();
            case 1  -> medianQuestion();
            case 2  -> modeQuestion();
            default -> probabilityQuestion();
        };
    }

    // =========================================================================
    // 1. Mean
    //
    //   Generates n values (4–6) where the last value is set so that
    //   the total is exactly divisible by n, guaranteeing a whole-number mean.
    // =========================================================================
    private static ChallengeDialog.Question meanQuestion() {
        int n      = RNG.nextInt(3) + 4;        // 4–6 values
        int target = RNG.nextInt(13) + 5;       // target mean 5–17
        int[] d    = new int[n];
        int sumSoFar = 0;

        for (int i = 0; i < n - 1; i++) {
            d[i]     = RNG.nextInt(16) + 2;     // 2–17
            sumSoFar += d[i];
        }
        d[n - 1] = target * n - sumSoFar;

        // Clamp the last value if it falls out of a sensible range
        if (d[n - 1] < 1 || d[n - 1] > 30) {
            d[n - 1] = Math.max(1, Math.min(30, d[n - 1]));
        }

        // Recompute the actual mean from the clamped dataset
        int total = 0;
        for (int v : d) total += v;
        int meanVal = total / n;

        return new ChallengeDialog.Question(
                "Dataset:  <b>" + dataset(d) + "</b><br>What is the <b>Mean</b>?",
                "Mean = sum of all values  ÷  count",
                String.valueOf(meanVal),
                true,
                "STATISTICS: MEAN",
                String.valueOf(meanVal)
        );
    }

    // =========================================================================
    // 2. Median
    //
    //   Always 5 values (odd count) so the median is exactly the middle
    //   element after sorting.  Dataset is shuffled before display.
    // =========================================================================
    private static ChallengeDialog.Question medianQuestion() {
        int n   = 5;
        int[] d = new int[n];
        for (int i = 0; i < n; i++) d[i] = RNG.nextInt(20) + 1;

        int[] sorted = d.clone();
        Arrays.sort(sorted);
        int medianVal = sorted[n / 2];

        shuffle(d);     // display in unsorted order so the player has to sort

        return new ChallengeDialog.Question(
                "Dataset:  <b>" + dataset(d) + "</b><br>What is the <b>Median</b>?",
                "Sort the values first, then pick the middle one",
                String.valueOf(medianVal),
                true,
                "STATISTICS: MEDIAN",
                String.valueOf(medianVal)
        );
    }

    // =========================================================================
    // 3. Mode
    //
    //   One value appears exactly 3 times; all others appear exactly once.
    //   This guarantees a single, unambiguous mode.
    // =========================================================================
    private static ChallengeDialog.Question modeQuestion() {
        int modeVal = RNG.nextInt(15) + 1;      // 1–15
        int extra   = RNG.nextInt(2) + 3;       // 3 or 4 unique filler values
        int n       = 3 + extra;
        int[] d     = new int[n];

        // First three slots hold the mode value
        d[0] = modeVal;
        d[1] = modeVal;
        d[2] = modeVal;

        // Fill the rest with distinct values that differ from the mode
        Set<Integer> used = new HashSet<>();
        used.add(modeVal);
        int idx = 3;
        while (idx < n) {
            int v = RNG.nextInt(20) + 1;
            if (!used.contains(v)) {
                used.add(v);
                d[idx++] = v;
            }
        }

        shuffle(d);     // randomise display order

        return new ChallengeDialog.Question(
                "Dataset:  <b>" + dataset(d) + "</b><br>What is the <b>Mode</b>?",
                "The value that appears most often",
                String.valueOf(modeVal),
                true,
                "STATISTICS: MODE",
                String.valueOf(modeVal)
        );
    }

    // =========================================================================
    // 4. Probability
    //
    //   Three question styles chosen randomly:
    //     A) Coloured marbles — bag of 10, pick 1–4 of one colour
    //     B) 6-sided die      — probability of rolling a specific number
    //     C) Coin flip        — probability of heads or tails
    //
    //   Answers use reduced fraction notation, e.g. "3/10", "1/6", "1/2".
    //   ChallengeDialog uses case-insensitive string match for non-numeric.
    // =========================================================================
    private static ChallengeDialog.Question probabilityQuestion() {
        int type = RNG.nextInt(3);
        String q, answer, reveal, hint;

        switch (type) {
            case 0 -> {   // Coloured marbles
                int total    = 10;
                int favCount = RNG.nextInt(4) + 1;   // 1–4 coloured marbles
                String colour = new String[]{"red", "blue", "green", "yellow"}[RNG.nextInt(4)];
                int[] frac   = reduce(favCount, total);
                q      = "A bag has <b>" + total + " marbles</b>: <b>" + favCount
                       + " are " + colour + "</b>, the rest are white."
                       + "<br>P(drawing a <b>" + colour + "</b> marble) = ?";
                answer = frac[0] + "/" + frac[1];
                reveal = frac[0] + "/" + frac[1];
                hint   = "P = favourable ÷ total  (write as a reduced fraction, e.g. 3/10)";
            }
            case 1 -> {   // 6-sided die
                int target = RNG.nextInt(6) + 1;
                q      = "You roll a fair <b>6-sided die</b>."
                       + "<br>What is the probability of rolling a <b>" + target + "</b>?";
                answer = "1/6";
                reveal = "1/6";
                hint   = "P = 1 ÷ 6  (type:  1/6)";
            }
            default -> {  // Coin flip
                String side = RNG.nextBoolean() ? "heads" : "tails";
                q      = "You flip a fair coin."
                       + "<br>What is the probability of getting <b>" + side + "</b>?";
                answer = "1/2";
                reveal = "1/2";
                hint   = "P = 1 ÷ 2  (type:  1/2)";
            }
        }

        return new ChallengeDialog.Question(q, hint, answer, false,
                "STATISTICS: PROBABILITY", reveal);
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /** Formats an int array as a human-readable dataset: {3, 7, 2, ...} */
    private static String dataset(int[] d) {
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < d.length; i++) {
            sb.append(d[i]);
            if (i < d.length - 1) sb.append(", ");
        }
        return sb.append("}").toString();
    }

    /** Fisher-Yates in-place shuffle. */
    private static void shuffle(int[] d) {
        for (int i = d.length - 1; i > 0; i--) {
            int j = RNG.nextInt(i + 1);
            int t = d[i]; d[i] = d[j]; d[j] = t;
        }
    }

    /** Returns {numerator, denominator} reduced to lowest terms. */
    private static int[] reduce(int num, int den) {
        int g = gcd(num, den);
        return new int[]{num / g, den / g};
    }

    private static int gcd(int a, int b) {
        return b == 0 ? a : gcd(b, a % b);
    }
}
