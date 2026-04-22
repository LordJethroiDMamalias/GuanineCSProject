package codes;

import java.util.Random;

/**
 * MainPool — question pool for the MAIN boss.
 *
 * Randomly selects between two categories each attack:
 *   • Chemistry  — "What element has this chemical symbol?"
 *   • Sports     — Rules trivia across basketball, soccer, volleyball, baseball, and tennis
 *
 * NOTE: This file is a standalone wrapper that delegates entirely to
 * QuestionPool.MainPool, which is the canonical implementation.
 * BossChallenge already uses QuestionPool.MainPool directly, so
 * this file is provided for completeness / standalone use only.
 *
 * Chemistry answers are case-insensitive string matches (isNumeric = false).
 * Sports answers are numeric where applicable (isNumeric = true).
 */
public class MainPool {

    private static final Random RNG = new Random();

    // =========================================================================
    // Chemistry pool  — { symbol, name }
    // =========================================================================
    private static final String[][] CHEM_POOL = {
        {"H",  "hydrogen"},   {"He", "helium"},     {"Li", "lithium"},
        {"Be", "beryllium"},  {"B",  "boron"},       {"C",  "carbon"},
        {"N",  "nitrogen"},   {"O",  "oxygen"},      {"F",  "fluorine"},
        {"Ne", "neon"},       {"Na", "sodium"},      {"Mg", "magnesium"},
        {"Al", "aluminum"},   {"Si", "silicon"},     {"P",  "phosphorus"},
        {"S",  "sulfur"},     {"Cl", "chlorine"},    {"Ar", "argon"},
        {"K",  "potassium"},  {"Ca", "calcium"},     {"Fe", "iron"},
        {"Cu", "copper"},     {"Zn", "zinc"},        {"Ag", "silver"},
        {"Au", "gold"},       {"Hg", "mercury"},     {"Pb", "lead"},
        {"Sn", "tin"},        {"Ni", "nickel"},      {"Mn", "manganese"},
        {"Cr", "chromium"},   {"Co", "cobalt"},      {"Ti", "titanium"},
        {"Br", "bromine"},    {"I",  "iodine"},      {"Pt", "platinum"},
        {"U",  "uranium"},    {"Ra", "radium"},      {"W",  "tungsten"},
        {"Ba", "barium"}
    };

    // =========================================================================
    // Sports pool  — { question, answer, display answer }
    // =========================================================================
    private static final String[][] SPORTS_POOL = {
        // Basketball
        {"How many players per team are on the court in basketball?",
         "5", "5"},
        {"How many points is a shot worth beyond the three-point line?",
         "3", "3"},
        {"How many seconds can a player hold the ball without moving in basketball?",
         "5", "5"},
        {"How many personal fouls before a basketball player fouls out?",
         "6", "6 (NBA) / 5 (FIBA)"},
        {"How many periods are in a standard NBA game?",
         "4", "4"},
        // Soccer / Football
        {"How many players per team are on the field in soccer?",
         "11", "11"},
        {"How many minutes are in a standard soccer match (regulation)?",
         "90", "90"},
        {"How many steps can a goalkeeper take while holding the ball?",
         "6", "6"},
        {"In soccer, how many players can be on the field including the goalkeeper?",
         "11", "11"},
        // Volleyball
        {"How many players per team are on the court in volleyball?",
         "6", "6"},
        {"How many sets are in a standard volleyball match (best of)?",
         "5", "5"},
        {"What is the maximum number of touches a team can use before returning the ball in volleyball?",
         "3", "3"},
        {"How many points must a team score to win a set in volleyball (standard sets)?",
         "25", "25"},
        {"How many points win the fifth (deciding) set in volleyball?",
         "15", "15"},
        // Baseball
        {"How many innings are in a standard baseball game?",
         "9", "9"},
        {"How many strikes make an out in baseball?",
         "3", "3"},
        {"How many balls result in a walk in baseball?",
         "4", "4"},
        {"How many outs per inning does each team get in baseball?",
         "3", "3"},
        // Tennis
        {"How many sets must a player win in a standard men's Grand Slam match?",
         "3", "3"},
        {"What score follows 'deuce' for the next point advantage in tennis?",
         "advantage", "Advantage"},
    };

    // =========================================================================
    // Public factory
    // =========================================================================

    /** Returns a randomly selected question from this pool. */
    public static ChallengeDialog.Question next() {
        return RNG.nextBoolean() ? chemQuestion() : sportsQuestion();
    }

    // ── Chemistry ─────────────────────────────────────────────────────────────

    private static ChallengeDialog.Question chemQuestion() {
        String[] entry = CHEM_POOL[RNG.nextInt(CHEM_POOL.length)];
        String sym  = entry[0];
        String name = entry[1];
        // Capitalise for the reveal label
        String displayName = name.substring(0, 1).toUpperCase() + name.substring(1);

        return new ChallengeDialog.Question(
                "<b>" + sym + "</b>  is the symbol for what element?",
                "Type the full element name",
                name,               // correctAnswer — matched case-insensitively by ChallengeDialog
                false,              // isNumeric
                "CHEMISTRY",
                displayName         // revealAnswer
        );
    }

    // ── Sports ────────────────────────────────────────────────────────────────

    private static ChallengeDialog.Question sportsQuestion() {
        String[] entry = SPORTS_POOL[RNG.nextInt(SPORTS_POOL.length)];
        String question     = entry[0];
        String answer       = entry[1];
        String displayAnswer = entry[2];

        boolean numeric;
        try { Integer.parseInt(answer); numeric = true; }
        catch (NumberFormatException e) { numeric = false; }

        return new ChallengeDialog.Question(
                question,
                numeric ? "Type a number" : "Type your answer",
                answer,
                numeric,
                "SPORTS RULES",
                displayAnswer
        );
    }
}
