package codes;

import java.util.Random;

/**
 * JetroidsPool — question pool for the Jetroids boss.
 *
 * Three categories, chosen randomly each attack:
 *   1. Trigonometry  — Pythagorean theorem (find a missing side)
 *   2. Force         — F = m × a  (solve for any of the three variables)
 *   3. Kinetic Energy— KE = ½mv²  (solve for KE or m)
 *
 * NOTE: This file is a standalone wrapper that delegates entirely to
 * QuestionPool.JetroidsPool, which is the canonical implementation.
 * BossChallenge already uses QuestionPool.JetroidsPool directly, so
 * this file is provided for completeness / standalone use only.
 *
 * All answers are compared as integers (isNumeric = true).
 */
public class JetroidsPool {

    private static final Random RNG = new Random();

    // =========================================================================
    // Public factory  — matches the QuestionPool.Pool interface contract
    // =========================================================================

    public static ChallengeDialog.Question next() {
        return switch (RNG.nextInt(3)) {
            case 0  -> trigQuestion();
            case 1  -> forceQuestion();
            default -> kineticEnergyQuestion();
        };
    }

    // =========================================================================
    // 1. Trigonometry — Pythagorean theorem (find the missing side)
    //
    //   Pythagorean triples guarantee integer answers with no rounding.
    //   Three variants: find adjacent, find opposite, or find hypotenuse.
    // =========================================================================

    /** { opposite, adjacent, hypotenuse } — Pythagorean triples */
    private static final int[][] TRIANGLES = {
        {3, 4, 5}, {5, 12, 13}, {8, 15, 17}, {7, 24, 25},
        {9, 40, 41}, {6, 8, 10}, {9, 12, 15}, {12, 16, 20}
    };

    private static ChallengeDialog.Question trigQuestion() {
        int[] t   = TRIANGLES[RNG.nextInt(TRIANGLES.length)];
        int scale = RNG.nextInt(3) + 1;
        int opp   = t[0] * scale;
        int adj   = t[1] * scale;
        int hyp   = t[2] * scale;

        int qtype = RNG.nextInt(3);
        String q, answer, reveal, hint;

        switch (qtype) {
            case 0 -> {   // given hyp and opp, find adj
                q      = "Right triangle:  Hypotenuse = " + hyp + ",  Opposite = " + opp
                       + "<br>Find the <b>Adjacent</b> side.";
                answer = String.valueOf(adj);
                reveal = String.valueOf(adj);
                hint   = "Use: adj² = hyp² − opp²  (Pythagorean theorem)";
            }
            case 1 -> {   // given hyp and adj, find opp
                q      = "Right triangle:  Hypotenuse = " + hyp + ",  Adjacent = " + adj
                       + "<br>Find the <b>Opposite</b> side.";
                answer = String.valueOf(opp);
                reveal = String.valueOf(opp);
                hint   = "Use: opp² = hyp² − adj²  (Pythagorean theorem)";
            }
            default -> {  // given opp and adj, find hyp
                q      = "Right triangle:  Opposite = " + opp + ",  Adjacent = " + adj
                       + "<br>Find the <b>Hypotenuse</b>.";
                answer = String.valueOf(hyp);
                reveal = String.valueOf(hyp);
                hint   = "Use: hyp² = opp² + adj²  (Pythagorean theorem)";
            }
        }

        return new ChallengeDialog.Question(q, hint, answer, true, "TRIGONOMETRY", reveal);
    }

    // =========================================================================
    // 2. Force — F = m × a
    //
    //   Three variants: solve for F, solve for m, solve for a.
    //   Values are small integers so the arithmetic is manageable under a timer.
    // =========================================================================

    private static ChallengeDialog.Question forceQuestion() {
        int m = RNG.nextInt(10) + 1;   // mass      1–10 kg
        int a = RNG.nextInt(10) + 1;   // accel     1–10 m/s²
        int F = m * a;

        int solve = RNG.nextInt(3);
        String q, answer, reveal, hint;

        switch (solve) {
            case 0 -> {   // solve for F
                q      = "Mass = <b>" + m + " kg</b>   |   Acceleration = <b>" + a + " m/s²</b>"
                       + "<br>What is the <b>Force</b>?";
                answer = String.valueOf(F);
                reveal = F + " N";
                hint   = "Formula:  F = m × a";
            }
            case 1 -> {   // solve for m
                q      = "Force = <b>" + F + " N</b>   |   Acceleration = <b>" + a + " m/s²</b>"
                       + "<br>What is the <b>Mass</b>?";
                answer = String.valueOf(m);
                reveal = m + " kg";
                hint   = "Formula:  m = F ÷ a";
            }
            default -> {  // solve for a
                q      = "Force = <b>" + F + " N</b>   |   Mass = <b>" + m + " kg</b>"
                       + "<br>What is the <b>Acceleration</b>?";
                answer = String.valueOf(a);
                reveal = a + " m/s²";
                hint   = "Formula:  a = F ÷ m";
            }
        }

        return new ChallengeDialog.Question(q, hint, answer, true, "PHYSICS: F = ma", reveal);
    }

    // =========================================================================
    // 3. Kinetic Energy — KE = ½ × m × v²
    //
    //   Two variants: solve for KE, or solve for m given KE and v.
    //   Integer results guaranteed by constraining m to be even (for KE),
    //   or by picking v from {1, 2, 4} so v² divides evenly (for m).
    // =========================================================================

    private static ChallengeDialog.Question kineticEnergyQuestion() {
        int m = RNG.nextInt(10) + 1;   // 1–10 kg
        int v = RNG.nextInt(8)  + 1;   // 1–8  m/s

        int solve = RNG.nextInt(2);
        String q, answer, reveal, hint;

        if (solve == 0) {              // solve for KE
            if (m % 2 != 0) m++;       // ensure even so ½mv² is integer
            int ke = (m * v * v) / 2;
            q      = "Mass = <b>" + m + " kg</b>   |   Velocity = <b>" + v + " m/s</b>"
                   + "<br>What is the <b>Kinetic Energy</b>?";
            answer = String.valueOf(ke);
            reveal = ke + " J";
            hint   = "Formula:  KE = ½ × m × v²";
        } else {                       // solve for m given KE and v
            int[] safeV = {1, 2, 4};
            v = safeV[RNG.nextInt(safeV.length)];
            if (m % 2 != 0) m++;
            int ke = (m * v * v) / 2;
            q      = "Kinetic Energy = <b>" + ke + " J</b>   |   Velocity = <b>" + v + " m/s</b>"
                   + "<br>What is the <b>Mass</b>?";
            answer = String.valueOf(m);
            reveal = m + " kg";
            hint   = "Formula:  m = 2 × KE ÷ v²";
        }

        return new ChallengeDialog.Question(q, hint, answer, true, "PHYSICS: KE = ½mv²", reveal);
    }
}
