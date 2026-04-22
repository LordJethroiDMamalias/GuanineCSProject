package codes;

import java.util.Random;

/**
 * JarrellePool — question pool for the Jarrelle boss.
 *
 * Three biology categories, chosen randomly each attack:
 *   1. Cell Biology   — organelles, cell types, cell processes
 *   2. Human Body     — organ systems, organs and their functions
 *   3. Basic Biology  — classification, DNA, genetics, ecology terms
 *
 * All answers are text (isNumeric = false) unless the answer is a count/number,
 * in which case isNumeric = true.
 */
public class JarrellePool implements QuestionPool.Pool {

    private static final Random RNG = new Random();

    // =========================================================================
    // Public factory — implements QuestionPool.Pool
    // =========================================================================

    @Override
    public ChallengeDialog.Question next() {
        return switch (RNG.nextInt(3)) {
            case 0  -> cellBiologyQuestion();
            case 1  -> humanBodyQuestion();
            default -> basicBiologyQuestion();
        };
    }

    // =========================================================================
    // 1. Cell Biology
    //    { questionText, correctAnswer, revealAnswer, hint, isNumericStr }
    // =========================================================================
    private static final String[][] CELL_POOL = {
        { "What is the 'control centre' of the cell that contains the DNA?",
          "nucleus", "Nucleus",
          "It is surrounded by a double membrane called the nuclear envelope", "n" },
        { "What organelle is known as the 'powerhouse of the cell', producing ATP?",
          "mitochondria", "Mitochondria (singular: mitochondrion)",
          "It carries out cellular respiration to release energy", "n" },
        { "What organelle packages and ships proteins throughout the cell?",
          "golgi apparatus", "Golgi Apparatus (Golgi body)",
          "Think of it as the cell's post office", "n" },
        { "What organelle synthesises (makes) proteins?",
          "ribosome", "Ribosome",
          "Found on rough ER or floating freely in the cytoplasm", "n" },
        { "What network of membranes in the cell transports proteins and lipids?",
          "endoplasmic reticulum", "Endoplasmic Reticulum (ER)",
          "Rough ER has ribosomes; Smooth ER does not", "n" },
        { "What organelle breaks down waste materials and worn-out cell parts?",
          "lysosome", "Lysosome",
          "Contains digestive enzymes; sometimes called the cell's 'recycling centre'", "n" },
        { "What flexible barrier surrounds ALL cells, controlling what enters and exits?",
          "cell membrane", "Cell Membrane (Plasma Membrane)",
          "Also called the plasma membrane; made of a phospholipid bilayer", "n" },
        { "Which type of cell has NO nucleus — prokaryotic or eukaryotic?",
          "prokaryotic", "Prokaryotic",
          "Bacteria are prokaryotes; plants and animals are eukaryotes", "n" },
        { "What is the process called by which cells make copies of themselves?",
          "cell division", "Cell Division (Mitosis / Meiosis)",
          "Mitosis produces identical body cells; meiosis produces sex cells", "n" },
        { "What is the process by which a cell takes in large substances by engulfing them?",
          "phagocytosis", "Phagocytosis",
          "White blood cells use this to engulf bacteria", "n" },
        { "How many chromosomes do normal human body cells contain?",
          "46", "46 (23 pairs)",
          "Sex cells (sperm and egg) contain only 23 chromosomes each", "y" },
        { "What is the fluid-filled interior of a cell called?",
          "cytoplasm", "Cytoplasm",
          "Organelles are suspended in this jelly-like substance", "n" },
    };

    private static ChallengeDialog.Question cellBiologyQuestion() {
        String[] e = CELL_POOL[RNG.nextInt(CELL_POOL.length)];
        boolean numeric = "y".equals(e[4]);
        return new ChallengeDialog.Question(
                "<html><center>" + e[0] + "</center></html>",
                e[3],
                e[1],
                numeric,
                "BIOLOGY: CELL BIOLOGY",
                e[2]
        );
    }

    // =========================================================================
    // 2. Human Body Systems
    //    { questionText, correctAnswer, revealAnswer, hint, isNumericStr }
    // =========================================================================
    private static final String[][] BODY_POOL = {
        // Circulatory
        { "What organ pumps blood throughout the human body?",
          "heart", "Heart",
          "It is a muscular organ located slightly left of centre in the chest", "n" },
        { "What type of blood vessel carries blood AWAY from the heart?",
          "artery", "Artery",
          "Arteries carry oxygenated blood (except the pulmonary artery)", "n" },
        { "What type of blood vessel carries blood BACK TO the heart?",
          "vein", "Vein",
          "Veins carry deoxygenated blood (except the pulmonary vein)", "n" },
        { "How many chambers does the human heart have?",
          "4", "4 (two atria and two ventricles)",
          "Upper chambers = atria; lower chambers = ventricles", "y" },
        // Respiratory
        { "What organ is the primary site of gas exchange in the human body?",
          "lungs", "Lungs",
          "Oxygen enters the blood and carbon dioxide leaves through tiny sacs called alveoli", "n" },
        { "What dome-shaped muscle below the lungs helps you breathe?",
          "diaphragm", "Diaphragm",
          "When it contracts, it flattens and pulls air into the lungs", "n" },
        // Digestive
        { "What organ produces bile to help digest fats?",
          "liver", "Liver",
          "It is the largest internal organ and also detoxifies the blood", "n" },
        { "In which organ does most chemical digestion and nutrient absorption occur?",
          "small intestine", "Small Intestine",
          "It is lined with tiny finger-like projections called villi", "n" },
        { "What organ absorbs water from undigested food?",
          "large intestine", "Large Intestine (Colon)",
          "Also called the colon; it leads to the rectum", "n" },
        // Nervous
        { "What is the main organ of the central nervous system?",
          "brain", "Brain",
          "It is protected by the skull and consists of the cerebrum, cerebellum, and brain stem", "n" },
        { "What long bundle of nerves runs down the spine?",
          "spinal cord", "Spinal Cord",
          "Together with the brain it forms the Central Nervous System (CNS)", "n" },
        // Skeletal / Muscular
        { "How many bones are in the adult human body?",
          "206", "206",
          "Babies are born with around 270 bones that fuse as they grow", "y" },
        { "What is the tissue that connects bones to other bones at joints?",
          "ligament", "Ligament",
          "Tendons connect muscle to bone; ligaments connect bone to bone", "n" },
        // Endocrine / Immune
        { "What organ produces insulin to regulate blood sugar levels?",
          "pancreas", "Pancreas",
          "It also produces digestive enzymes and the hormone glucagon", "n" },
        { "What type of blood cell fights infections and is part of the immune system?",
          "white blood cell", "White Blood Cell (Leukocyte)",
          "Also called leukocytes; they include neutrophils, lymphocytes, and more", "n" },
    };

    private static ChallengeDialog.Question humanBodyQuestion() {
        String[] e = BODY_POOL[RNG.nextInt(BODY_POOL.length)];
        boolean numeric = "y".equals(e[4]);
        return new ChallengeDialog.Question(
                "<html><center>" + e[0] + "</center></html>",
                e[3],
                e[1],
                numeric,
                "BIOLOGY: HUMAN BODY",
                e[2]
        );
    }

    // =========================================================================
    // 3. Basic Biology — classification, genetics, ecology
    //    { questionText, correctAnswer, revealAnswer, hint, isNumericStr }
    // =========================================================================
    private static final String[][] BASIC_POOL = {
        // Classification
        { "What is the system of naming organisms using two Latin names (genus and species) called?",
          "binomial nomenclature", "Binomial Nomenclature",
          "Developed by Carl Linnaeus in the 18th century", "n" },
        { "What are the five kingdoms of life? (type one: plants, animals, fungi, protists, or bacteria)",
          "plants", "Plants / Animals / Fungi / Protists / Bacteria",
          "Any one of the five traditional kingdoms is accepted", "n" },
        { "What is the basic unit of life?",
          "cell", "Cell",
          "All living things are made of one or more cells", "n" },
        // DNA and genetics
        { "What does DNA stand for?",
          "deoxyribonucleic acid", "Deoxyribonucleic Acid",
          "It stores the genetic instructions for building and operating living things", "n" },
        { "What are the four nitrogen bases found in DNA?",
          "adenine", "Adenine, Thymine, Guanine, Cytosine (A, T, G, C)",
          "Type any one: Adenine, Thymine, Guanine, or Cytosine", "n" },
        { "In DNA base pairing, Adenine always pairs with which base?",
          "thymine", "Thymine",
          "A pairs with T; Cytosine (C) pairs with Guanine (G)", "n" },
        { "What is the term for a segment of DNA that codes for a specific protein or trait?",
          "gene", "Gene",
          "Humans have approximately 20,000–25,000 genes", "n" },
        { "What is the process by which DNA makes a copy of itself called?",
          "replication", "DNA Replication",
          "It occurs before a cell divides so each daughter cell gets a full set of DNA", "n" },
        // Ecology
        { "What is an organism that makes its own food through photosynthesis called?",
          "producer", "Producer (Autotroph)",
          "Also called autotrophs; plants and algae are examples", "n" },
        { "What is an organism that eats other organisms to get energy called?",
          "consumer", "Consumer (Heterotroph)",
          "Also called heterotrophs; includes herbivores, carnivores, and omnivores", "n" },
        { "What is the relationship between a clownfish and a sea anemone (both benefit) called?",
          "mutualism", "Mutualism",
          "Both organisms benefit; other types include commensalism and parasitism", "n" },
        { "What is the term for all the populations of different species living in the same area?",
          "community", "Community",
          "An ecosystem includes a community PLUS the non-living (abiotic) environment", "n" },
        // Miscellaneous
        { "What molecule carries genetic information from the nucleus to the ribosome?",
          "mrna", "mRNA (Messenger RNA)",
          "mRNA is transcribed from DNA and translated into protein at the ribosome", "n" },
        { "What is the name of the process by which living things produce offspring?",
          "reproduction", "Reproduction",
          "Can be sexual (two parents) or asexual (one parent)", "n" },
    };

    private static ChallengeDialog.Question basicBiologyQuestion() {
        String[] e = BASIC_POOL[RNG.nextInt(BASIC_POOL.length)];
        boolean numeric = "y".equals(e[4]);
        return new ChallengeDialog.Question(
                "<html><center>" + e[0] + "</center></html>",
                e[3],
                e[1],
                numeric,
                "BIOLOGY: GENERAL",
                e[2]
        );
    }
}
