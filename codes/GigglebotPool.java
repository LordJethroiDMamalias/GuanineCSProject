package codes;

import java.util.Random;

/**
 * GigglebotPool — question pool for the GIGGLEBOT3000 boss.
 *
 * Three physics categories, chosen randomly each attack:
 *   1. Force      — F = ma  (already in JetroidsPool; re-implemented here with
 *                   wider value ranges and weight / friction variants)
 *   2. Power      — P = W/t  and  P = Fv  (solve for any variable)
 *   3. Electricity— Ohm's Law V = IR, power P = IV = I²R = V²/R
 *
 * All answers are integers (isNumeric = true).
 * Values are chosen so that results are always whole numbers.
 */
public class GigglebotPool implements QuestionPool.Pool {

    private static final Random RNG = new Random();

    // =========================================================================
    // Public factory — implements QuestionPool.Pool
    // =========================================================================

    @Override
    public ChallengeDialog.Question next() {
        return switch (RNG.nextInt(3)) {
            case 0  -> forceQuestion();
            case 1  -> powerQuestion();
            default -> electricityQuestion();
        };
    }

    // =========================================================================
    // 1. Force — F = m × a
    //    Wider ranges than JetroidsPool; includes weight variant W = mg (g = 10).
    // =========================================================================
    private static ChallengeDialog.Question forceQuestion() {
        int variant = RNG.nextInt(4);   // 0–2: F=ma variants; 3: weight W=mg
        String q, answer, reveal, hint;

        if (variant == 3) {
            // Weight: W = m × g,  g = 10 m/s²
            int m = RNG.nextInt(20) + 1;   // 1–20 kg
            int g = 10;
            int W = m * g;
            q      = "An object has a mass of <b>" + m + " kg</b>.<br>"
                   + "What is its <b>weight</b> on Earth? (use g = 10 m/s²)";
            answer = String.valueOf(W);
            reveal = W + " N";
            hint   = "Formula:  W = m × g  (g = 10 m/s²)";
        } else {
            // Standard F = ma with larger ranges
            int m = RNG.nextInt(15) + 1;   // 1–15 kg
            int a = RNG.nextInt(12) + 1;   // 1–12 m/s²
            int F = m * a;
            switch (variant) {
                case 0 -> {   // find F
                    q      = "Mass = <b>" + m + " kg</b>   |   Acceleration = <b>" + a + " m/s²</b>"
                           + "<br>What is the <b>Net Force</b>?";
                    answer = String.valueOf(F);
                    reveal = F + " N";
                    hint   = "Formula:  F = m × a";
                }
                case 1 -> {   // find m
                    q      = "Net Force = <b>" + F + " N</b>   |   Acceleration = <b>" + a + " m/s²</b>"
                           + "<br>What is the <b>Mass</b>?";
                    answer = String.valueOf(m);
                    reveal = m + " kg";
                    hint   = "Formula:  m = F ÷ a";
                }
                default -> {  // find a
                    q      = "Net Force = <b>" + F + " N</b>   |   Mass = <b>" + m + " kg</b>"
                           + "<br>What is the <b>Acceleration</b>?";
                    answer = String.valueOf(a);
                    reveal = a + " m/s²";
                    hint   = "Formula:  a = F ÷ m";
                }
            }
        }
        return new ChallengeDialog.Question(q, hint, answer, true, "PHYSICS: FORCE (F = ma)", reveal);
    }

    // =========================================================================
    // 2. Power — P = W/t  and  P = F×v
    //    Guarantees integer answers by construction.
    //
    //    Variant A: P = W/t   — Work (J) and time (s) chosen so W is divisible by t
    //    Variant B: P = F×v   — Force and velocity, solve for P, F, or v
    // =========================================================================
    private static ChallengeDialog.Question powerQuestion() {
        int variant = RNG.nextInt(2);
        String q, answer, reveal, hint;

        if (variant == 0) {
            // P = W / t,  pick t first then build W = P * t
            int t = RNG.nextInt(10) + 1;    // 1–10 s
            int P = RNG.nextInt(15) + 2;    // 2–16 W
            int W = P * t;

            int solve = RNG.nextInt(3);
            switch (solve) {
                case 0 -> {   // find P
                    q      = "Work done = <b>" + W + " J</b>   |   Time = <b>" + t + " s</b>"
                           + "<br>What is the <b>Power</b>?";
                    answer = String.valueOf(P);
                    reveal = P + " W";
                    hint   = "Formula:  P = W ÷ t";
                }
                case 1 -> {   // find W
                    q      = "Power = <b>" + P + " W</b>   |   Time = <b>" + t + " s</b>"
                           + "<br>What is the <b>Work Done</b>?";
                    answer = String.valueOf(W);
                    reveal = W + " J";
                    hint   = "Formula:  W = P × t";
                }
                default -> {  // find t
                    q      = "Power = <b>" + P + " W</b>   |   Work Done = <b>" + W + " J</b>"
                           + "<br>What is the <b>Time</b> taken?";
                    answer = String.valueOf(t);
                    reveal = t + " s";
                    hint   = "Formula:  t = W ÷ P";
                }
            }
        } else {
            // P = F × v
            int F = RNG.nextInt(10) + 1;   // 1–10 N
            int v = RNG.nextInt(10) + 1;   // 1–10 m/s
            int P = F * v;

            int solve = RNG.nextInt(3);
            switch (solve) {
                case 0 -> {   // find P
                    q      = "Force = <b>" + F + " N</b>   |   Velocity = <b>" + v + " m/s</b>"
                           + "<br>What is the <b>Power</b>?";
                    answer = String.valueOf(P);
                    reveal = P + " W";
                    hint   = "Formula:  P = F × v";
                }
                case 1 -> {   // find F
                    q      = "Power = <b>" + P + " W</b>   |   Velocity = <b>" + v + " m/s</b>"
                           + "<br>What is the <b>Force</b>?";
                    answer = String.valueOf(F);
                    reveal = F + " N";
                    hint   = "Formula:  F = P ÷ v";
                }
                default -> {  // find v
                    q      = "Power = <b>" + P + " W</b>   |   Force = <b>" + F + " N</b>"
                           + "<br>What is the <b>Velocity</b>?";
                    answer = String.valueOf(v);
                    reveal = v + " m/s";
                    hint   = "Formula:  v = P ÷ F";
                }
            }
        }
        return new ChallengeDialog.Question(q, hint, answer, true, "PHYSICS: POWER (P = W/t)", reveal);
    }

    // =========================================================================
    // 3. Electricity — Ohm's Law (V = I × R) and electrical power variants
    //    P = I×V  |  P = I²×R  |  P = V²/R
    //
    //    Values are chosen so that all answers are integers:
    //    - For V=IR: pick I and R as integers → V is integer
    //    - For P=IV: pick I and V as integers → P is integer
    //    - For P=I²R: pick I ∈ {1,2,3} and R so I²*R is integer → always integer
    //    - For P=V²/R: pick V and R so V² is divisible by R
    // =========================================================================
    private static ChallengeDialog.Question electricityQuestion() {
        int variant = RNG.nextInt(4);
        String q, answer, reveal, hint;

        switch (variant) {
            case 0 -> {
                // Ohm's Law: V = I × R
                int I = RNG.nextInt(10) + 1;   // 1–10 A
                int R = RNG.nextInt(10) + 1;   // 1–10 Ω
                int V = I * R;
                int solve = RNG.nextInt(3);
                switch (solve) {
                    case 0 -> {
                        q = "Current = <b>" + I + " A</b>   |   Resistance = <b>" + R + " Ω</b>"
                          + "<br>What is the <b>Voltage</b>?";
                        answer = String.valueOf(V); reveal = V + " V";
                        hint = "Formula:  V = I × R  (Ohm's Law)";
                    }
                    case 1 -> {
                        q = "Voltage = <b>" + V + " V</b>   |   Resistance = <b>" + R + " Ω</b>"
                          + "<br>What is the <b>Current</b>?";
                        answer = String.valueOf(I); reveal = I + " A";
                        hint = "Formula:  I = V ÷ R  (Ohm's Law)";
                    }
                    default -> {
                        q = "Voltage = <b>" + V + " V</b>   |   Current = <b>" + I + " A</b>"
                          + "<br>What is the <b>Resistance</b>?";
                        answer = String.valueOf(R); reveal = R + " Ω";
                        hint = "Formula:  R = V ÷ I  (Ohm's Law)";
                    }
                }
            }
            case 1 -> {
                // P = I × V
                int I = RNG.nextInt(10) + 1;
                int V = RNG.nextInt(10) + 1;
                int P = I * V;
                int solve = RNG.nextInt(3);
                switch (solve) {
                    case 0 -> {
                        q = "Current = <b>" + I + " A</b>   |   Voltage = <b>" + V + " V</b>"
                          + "<br>What is the <b>Power</b>?";
                        answer = String.valueOf(P); reveal = P + " W";
                        hint = "Formula:  P = I × V";
                    }
                    case 1 -> {
                        q = "Power = <b>" + P + " W</b>   |   Voltage = <b>" + V + " V</b>"
                          + "<br>What is the <b>Current</b>?";
                        answer = String.valueOf(I); reveal = I + " A";
                        hint = "Formula:  I = P ÷ V";
                    }
                    default -> {
                        q = "Power = <b>" + P + " W</b>   |   Current = <b>" + I + " A</b>"
                          + "<br>What is the <b>Voltage</b>?";
                        answer = String.valueOf(V); reveal = V + " V";
                        hint = "Formula:  V = P ÷ I";
                    }
                }
            }
            case 2 -> {
                // P = I² × R  — I ∈ {1,2,3} to keep numbers reasonable
                int[] safeI = {1, 2, 3};
                int I = safeI[RNG.nextInt(safeI.length)];
                int R = RNG.nextInt(10) + 1;
                int P = I * I * R;
                int solve = RNG.nextInt(2);
                if (solve == 0) {
                    q = "Current = <b>" + I + " A</b>   |   Resistance = <b>" + R + " Ω</b>"
                      + "<br>What is the <b>Power</b>?   (use P = I² × R)";
                    answer = String.valueOf(P); reveal = P + " W";
                    hint = "Formula:  P = I² × R";
                } else {
                    q = "Power = <b>" + P + " W</b>   |   Current = <b>" + I + " A</b>"
                      + "<br>What is the <b>Resistance</b>?   (use P = I² × R)";
                    answer = String.valueOf(R); reveal = R + " Ω";
                    hint = "Formula:  R = P ÷ I²";
                }
            }
            default -> {
                // P = V² / R  — pick V and R so V² divisible by R
                // Strategy: pick R first, then V as a multiple of R (so V²/R = V*(V/R) is integer)
                int[] safeR = {1, 2, 4, 5};
                int R = safeR[RNG.nextInt(safeR.length)];
                int factor = RNG.nextInt(4) + 1;    // V = factor * R  → V² / R = factor² * R
                int V = factor * R;
                int P = (V * V) / R;
                int solve = RNG.nextInt(2);
                if (solve == 0) {
                    q = "Voltage = <b>" + V + " V</b>   |   Resistance = <b>" + R + " Ω</b>"
                      + "<br>What is the <b>Power</b>?   (use P = V² ÷ R)";
                    answer = String.valueOf(P); reveal = P + " W";
                    hint = "Formula:  P = V² ÷ R";
                } else {
                    q = "Power = <b>" + P + " W</b>   |   Voltage = <b>" + V + " V</b>"
                      + "<br>What is the <b>Resistance</b>?   (use P = V² ÷ R)";
                    answer = String.valueOf(R); reveal = R + " Ω";
                    hint = "Formula:  R = V² ÷ P";
                }
            }
        }
        return new ChallengeDialog.Question(q, hint, answer, true, "PHYSICS: ELECTRICITY", reveal);
    }
}
