package codes;

import java.util.Random;

/**
 * IvyPool — question pool for the IVy boss.
 *
 * Two categories, chosen randomly each attack:
 *   1. Photosynthesis — equation components, reactants, products, conditions
 *   2. Plant Biology  — plant parts and their functions, cell structures
 *
 * Most answers are text (isNumeric = false), matched case-insensitively.
 * Numeric answers (e.g. ATP counts) use isNumeric = true.
 */
public class IvyPool implements QuestionPool.Pool {

    private static final Random RNG = new Random();

    // =========================================================================
    // Public factory — implements QuestionPool.Pool
    // =========================================================================

    @Override
    public ChallengeDialog.Question next() {
        return RNG.nextBoolean() ? photosynthesisQuestion() : plantBiologyQuestion();
    }

    // =========================================================================
    // 1. Photosynthesis questions
    //    Format: { questionText, correctAnswer, revealAnswer, hint, isNumericStr }
    //    isNumericStr: "y" = numeric, anything else = text
    // =========================================================================
    private static final String[][] PHOTO_POOL = {
        // Reactants and products
        { "What gas do plants take in as a raw material for photosynthesis?",
          "carbon dioxide", "Carbon Dioxide (CO₂)",
          "Plants absorb it through tiny pores called stomata", "n" },
        { "What gas is released by plants as a byproduct of photosynthesis?",
          "oxygen", "Oxygen (O₂)",
          "Animals need this gas to breathe", "n" },
        { "What is the main sugar produced during photosynthesis?",
          "glucose", "Glucose (C₆H₁₂O₆)",
          "It is a simple 6-carbon sugar; also called C₆H₁₂O₆", "n" },
        { "What liquid do plants absorb through their roots for photosynthesis?",
          "water", "Water (H₂O)",
          "Chemical formula: H₂O", "n" },
        // Light and chlorophyll
        { "What is the green pigment in plant leaves that captures sunlight?",
          "chlorophyll", "Chlorophyll",
          "It is found inside chloroplasts and gives leaves their colour", "n" },
        { "In which organelle does photosynthesis take place?",
          "chloroplast", "Chloroplast",
          "It is the 'powerhouse for plants' and contains chlorophyll", "n" },
        { "Which colour of light is MOST absorbed by chlorophyll?",
          "red", "Red (and blue)",
          "Chlorophyll absorbs red and blue light most strongly; reflects green", "n" },
        { "Which colour of light is LEAST absorbed (reflected) by chlorophyll, making leaves look green?",
          "green", "Green",
          "Reflected light is the colour we see", "n" },
        // Equation
        { "How many molecules of CO₂ are needed in the balanced photosynthesis equation?",
          "6", "6  (6CO₂ + 6H₂O → C₆H₁₂O₆ + 6O₂)",
          "Balance the carbon atoms: glucose has 6 carbons", "y" },
        { "How many molecules of water (H₂O) are used in the balanced photosynthesis equation?",
          "6", "6  (6CO₂ + 6H₂O → C₆H₁₂O₆ + 6O₂)",
          "Balance the hydrogen atoms on both sides", "y" },
        { "How many molecules of O₂ are produced in the balanced photosynthesis equation?",
          "6", "6  (6CO₂ + 6H₂O → C₆H₁₂O₆ + 6O₂)",
          "Count the oxygen atoms in glucose + water", "y" },
        // Stages
        { "What is the first stage of photosynthesis called, where light energy is captured?",
          "light reactions", "Light Reactions (Light-dependent stage)",
          "It takes place in the thylakoid membranes of the chloroplast", "n" },
        { "What is the second stage of photosynthesis called, where glucose is made?",
          "calvin cycle", "Calvin Cycle (Light-independent / Dark reactions)",
          "Also called the dark reactions; takes place in the stroma", "n" },
        { "What energy molecule is produced in the light reactions and used in the Calvin Cycle?",
          "atp", "ATP (Adenosine Triphosphate)",
          "The universal energy currency of cells; also write 'ATP'", "n" },
        // Conditions
        { "What THREE things does a plant need for photosynthesis? (type the one that is a form of energy)",
          "light", "Light (sunlight / solar energy)",
          "The three inputs are: light, water, and carbon dioxide", "n" },
        { "Which part of the plant captures the most sunlight for photosynthesis?",
          "leaf", "Leaf",
          "Its broad, flat shape maximises surface area for light absorption", "n" },
    };

    private static ChallengeDialog.Question photosynthesisQuestion() {
        String[] e = PHOTO_POOL[RNG.nextInt(PHOTO_POOL.length)];
        boolean numeric = "y".equals(e[4]);
        return new ChallengeDialog.Question(
                "<html><center>" + e[0] + "</center></html>",
                e[3],
                e[1],
                numeric,
                "BIOLOGY: PHOTOSYNTHESIS",
                e[2]
        );
    }

    // =========================================================================
    // 2. Plant Biology questions
    //    { questionText, correctAnswer, revealAnswer, hint }   (all text answers)
    // =========================================================================
    private static final String[][] PLANT_POOL = {
        // Plant parts
        { "Which part of the plant absorbs water and minerals from the soil?",
          "roots", "Roots",
          "They anchor the plant and have tiny root hairs for absorption" },
        { "Which plant part transports water from roots to leaves?",
          "stem", "Stem",
          "It also provides structural support for the plant" },
        { "What are the tiny pores on the surface of a leaf called, through which gases enter and exit?",
          "stomata", "Stomata (singular: stoma)",
          "They open and close, controlled by guard cells" },
        { "What cells control the opening and closing of stomata?",
          "guard cells", "Guard Cells",
          "They swell with water to open stomata and shrink to close them" },
        // Vascular tissue
        { "What is the name of the vascular tissue that carries water and minerals UP from the roots?",
          "xylem", "Xylem",
          "Remember: X-ylem = goes e-X-actly up" },
        { "What is the name of the vascular tissue that carries food (glucose) made in the leaves?",
          "phloem", "Phloem",
          "It transports sugars both up and down the plant" },
        // Cell structures
        { "What rigid outer layer surrounds plant cells but NOT animal cells?",
          "cell wall", "Cell Wall",
          "It is made of cellulose and gives plant cells their rigid shape" },
        { "What large organelle filled with cell sap helps maintain turgor pressure in plant cells?",
          "vacuole", "Central Vacuole",
          "It takes up most of the volume of a mature plant cell" },
        // Reproduction and other
        { "What is the process by which plants lose water through their leaves called?",
          "transpiration", "Transpiration",
          "Water evaporates through open stomata — it drives the water column up the xylem" },
        { "What is the male reproductive part of a flower called?",
          "stamen", "Stamen",
          "It produces pollen; consists of the anther and filament" },
        { "What is the female reproductive part of a flower called?",
          "pistil", "Pistil (Carpel)",
          "It consists of the stigma, style, and ovary" },
        { "What process describes a pollen grain landing on the stigma of a flower?",
          "pollination", "Pollination",
          "Can be carried out by wind, insects, birds, or other animals" },
        { "What green pigment-containing structures inside chloroplasts stack like coins?",
          "thylakoids", "Thylakoids (Grana)",
          "The stacks of thylakoids are called grana; the light reactions happen here" },
        { "What is the fluid-filled space inside the chloroplast (surrounding the thylakoids) called?",
          "stroma", "Stroma",
          "The Calvin Cycle (dark reactions) take place here" },
    };

    private static ChallengeDialog.Question plantBiologyQuestion() {
        String[] e = PLANT_POOL[RNG.nextInt(PLANT_POOL.length)];
        return new ChallengeDialog.Question(
                "<html><center>" + e[0] + "</center></html>",
                e[3],
                e[1],
                false,
                "BIOLOGY: PLANT BIOLOGY",
                e[2]
        );
    }
}
