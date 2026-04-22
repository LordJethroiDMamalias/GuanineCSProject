package codes;

import java.util.Random;

/**
 * RainityPool — question pool for the Rainity boss.
 *
 * Four types of linear algebra equations, chosen randomly each attack:
 *   Type A:  ax + b = c        e.g.  3x + 5 = 14    → x = 3
 *   Type B:  ax - b = c        e.g.  2x - 4 = 10    → x = 7
 *   Type C:  ax = c            e.g.  4x = 28         → x = 7
 *   Type D:  x/a + b = c       e.g.  x/3 + 2 = 5    → x = 9
 *
 * All answers are integers (isNumeric = true).
 */
public class RainityPool implements QuestionPool.Pool {

    private static final Random RNG = new Random();

    // =========================================================================
    // Public factory — implements QuestionPool.Pool
    // =========================================================================

    @Override
    public ChallengeDialog.Question next() {
        return switch (RNG.nextInt(4)) {
            case 0  -> axPlusB();
            case 1  -> axMinusB();
            case 2  -> axEqualsC();
            default -> xOverA();
        };
    }

    // =========================================================================
    // Type A: ax + b = c  →  x = (c - b) / a
    // =========================================================================
    private static ChallengeDialog.Question axPlusB() {
        int a = RNG.nextInt(4) + 2;          // 2–5
        int x = RNG.nextInt(8) + 1;          // 1–8
        int b = RNG.nextInt(10) + 1;         // 1–10
        int c = a * x + b;
        String q = a + "x + " + b + " = " + c;
        return build(q, x);
    }

    // =========================================================================
    // Type B: ax - b = c  →  x = (c + b) / a
    // =========================================================================
    private static ChallengeDialog.Question axMinusB() {
        int a = RNG.nextInt(4) + 2;          // 2–5
        int x = RNG.nextInt(8) + 2;          // 2–9  (keeps c positive)
        int b = RNG.nextInt(8) + 1;          // 1–8
        int c = a * x - b;
        String q = a + "x - " + b + " = " + c;
        return build(q, x);
    }

    // =========================================================================
    // Type C: ax = c  →  x = c / a
    // =========================================================================
    private static ChallengeDialog.Question axEqualsC() {
        int a = RNG.nextInt(6) + 2;          // 2–7
        int x = RNG.nextInt(9) + 1;          // 1–9
        int c = a * x;
        String q = a + "x = " + c;
        return build(q, x);
    }

    // =========================================================================
    // Type D: x/a + b = c  →  x = (c - b) * a
    // =========================================================================
    private static ChallengeDialog.Question xOverA() {
        int a = RNG.nextInt(4) + 2;              // 2–5
        int x = a * (RNG.nextInt(6) + 1);        // multiple of a → integer answer
        int b = RNG.nextInt(8) + 1;              // 1–8
        int c = x / a + b;
        String q = "x/" + a + " + " + b + " = " + c;
        return build(q, x);
    }

    // ── Shared builder ────────────────────────────────────────────────────────

    private static ChallengeDialog.Question build(String questionText, int answer) {
        return new ChallengeDialog.Question(
                questionText,
                "Find the value of  x",
                String.valueOf(answer),
                true,
                "ALGEBRA",
                "x = " + answer
        );
    }
}
