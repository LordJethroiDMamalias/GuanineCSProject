package codes;

import java.util.Random;

/**
 * FedoraPool — question pool for the Fedora Group (Celene and Cedora).
 *
 * Randomly selects between two chemistry categories each attack:
 *   • Atomic Mass  — "What is the atomic mass of <element>?"
 *   • Molar Mass   — "What is the molar mass of <compound>?"
 *
 * All values use rounded whole-number atomic masses so answers are always
 * clean integers.  Answers are compared numerically (isNumeric = true).
 *
 * Atomic masses used throughout:
 *   H=1, He=4, Li=7, C=12, N=14, O=16, F=19, Ne=20, Na=23, Mg=24,
 *   Al=27, Si=28, P=31, S=32, Cl=35, Ar=40, K=39, Ca=40, Fe=56,
 *   Cu=64, Zn=65, Ag=108, Au=197, Hg=201, Pb=207, Sn=119, Ni=58,
 *   Ti=48, Cr=52, Mn=55, Br=80, I=127, Pt=195, Ba=137, Co=59, W=184
 */
public class FedoraPool implements QuestionPool.Pool {

    private static final Random RNG = new Random();

    // =========================================================================
    // Atomic mass table  — { "Element display name", "atomic mass (amu)" }
    // =========================================================================
    private static final String[][] ATOMIC_MASSES = {
        {"Hydrogen (H)",    "1"},    {"Helium (He)",     "4"},    {"Lithium (Li)",    "7"},
        {"Carbon (C)",     "12"},    {"Nitrogen (N)",   "14"},    {"Oxygen (O)",     "16"},
        {"Fluorine (F)",   "19"},    {"Neon (Ne)",      "20"},    {"Sodium (Na)",    "23"},
        {"Magnesium (Mg)", "24"},    {"Aluminum (Al)",  "27"},    {"Silicon (Si)",   "28"},
        {"Phosphorus (P)", "31"},    {"Sulfur (S)",     "32"},    {"Chlorine (Cl)",  "35"},
        {"Argon (Ar)",     "40"},    {"Potassium (K)",  "39"},    {"Calcium (Ca)",   "40"},
        {"Iron (Fe)",      "56"},    {"Copper (Cu)",    "64"},    {"Zinc (Zn)",      "65"},
        {"Silver (Ag)",   "108"},    {"Gold (Au)",     "197"},    {"Mercury (Hg)",  "201"},
        {"Lead (Pb)",     "207"},    {"Tin (Sn)",      "119"},    {"Nickel (Ni)",    "58"},
        {"Titanium (Ti)",  "48"},    {"Chromium (Cr)", "52"},     {"Manganese (Mn)","55"},
        {"Bromine (Br)",   "80"},    {"Iodine (I)",    "127"},    {"Platinum (Pt)", "195"},
        {"Barium (Ba)",   "137"},    {"Cobalt (Co)",    "59"},    {"Tungsten (W)",  "184"},
    };

    // =========================================================================
    // Molar mass table  — { "Compound name", "formula", "molar mass (g/mol)" }
    // =========================================================================
    private static final String[][] MOLAR_MASSES = {
        {"Water",                "H2O",    "18"},   // 2(1)+16
        {"Carbon dioxide",       "CO2",    "44"},   // 12+2(16)
        {"Oxygen gas",           "O2",     "32"},   // 2(16)
        {"Hydrogen gas",         "H2",      "2"},   // 2(1)
        {"Nitrogen gas",         "N2",     "28"},   // 2(14)
        {"Ammonia",              "NH3",    "17"},   // 14+3(1)
        {"Methane",              "CH4",    "16"},   // 12+4(1)
        {"Hydrogen chloride",    "HCl",    "36"},   // 1+35
        {"Sodium chloride",      "NaCl",   "58"},   // 23+35
        {"Calcium oxide",        "CaO",    "56"},   // 40+16
        {"Carbon monoxide",      "CO",     "28"},   // 12+16
        {"Sulfur dioxide",       "SO2",    "64"},   // 32+2(16)
        {"Sulfur trioxide",      "SO3",    "80"},   // 32+3(16)
        {"Nitric oxide",         "NO",     "30"},   // 14+16
        {"Nitrogen dioxide",     "NO2",    "46"},   // 14+2(16)
        {"Magnesium oxide",      "MgO",    "40"},   // 24+16
        {"Calcium carbonate",    "CaCO3", "100"},   // 40+12+3(16)
        {"Sodium hydroxide",     "NaOH",   "40"},   // 23+16+1
        {"Potassium chloride",   "KCl",    "74"},   // 39+35
        {"Iron(II) oxide",       "FeO",    "72"},   // 56+16
        {"Aluminum oxide",       "Al2O3", "102"},   // 2(27)+3(16)
        {"Phosphorus pentoxide", "P2O5",  "142"},   // 2(31)+5(16)
        {"Zinc oxide",           "ZnO",    "81"},   // 65+16
        {"Copper(II) oxide",     "CuO",    "80"},   // 64+16
        {"Hydrogen peroxide",    "H2O2",   "34"},   // 2(1)+2(16)
        {"Ethane",               "C2H6",   "30"},   // 2(12)+6(1)
        {"Ethylene",             "C2H4",   "28"},   // 2(12)+4(1)
        {"Propane",              "C3H8",   "44"},   // 3(12)+8(1)
    };

    // =========================================================================
    // Public factory — implements QuestionPool.Pool
    // =========================================================================

    @Override
    public ChallengeDialog.Question next() {
        return RNG.nextBoolean() ? atomicMassQuestion() : molarMassQuestion();
    }

    // =========================================================================
    // Atomic mass question
    // =========================================================================
    private static ChallengeDialog.Question atomicMassQuestion() {
        String[] pick = ATOMIC_MASSES[RNG.nextInt(ATOMIC_MASSES.length)];
        String element = pick[0];
        String mass    = pick[1];

        return new ChallengeDialog.Question(
                "What is the <b>atomic mass</b> of  <b>" + element + "</b>?",
                "Answer in atomic mass units (amu) — whole number",
                mass,
                true,
                "CHEMISTRY: ATOMIC MASS",
                mass + " amu"
        );
    }

    // =========================================================================
    // Molar mass question
    // =========================================================================
    private static ChallengeDialog.Question molarMassQuestion() {
        String[] pick    = MOLAR_MASSES[RNG.nextInt(MOLAR_MASSES.length)];
        String compound  = pick[0];
        String formula   = pick[1];
        String mass      = pick[2];

        return new ChallengeDialog.Question(
                "What is the <b>molar mass</b> of  <b>" + compound + "  (" + formula + ")</b>?",
                "Answer in g/mol — whole number",
                mass,
                true,
                "CHEMISTRY: MOLAR MASS",
                mass + " g/mol"
        );
    }
}
