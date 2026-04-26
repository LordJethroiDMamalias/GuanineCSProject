package codes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * BinIzharfedPool — question pool for the Bin Izharfed boss.
 *
 * Two categories, chosen randomly each attack:
 *   1. Saudi Arabia — history, geography, culture, government
 *   2. World Geography & History — capitals, continents, major events
 *
 * All answers are text (isNumeric = false) unless the answer is purely
 * numeric (e.g. a year), in which case isNumeric = true.
 *
 * Each question is asked at most once per cycle. A question is removed from
 * the pool as soon as it is returned by next(), regardless of whether the
 * player answered correctly or not. When all questions in a category have
 * been asked, that category resets and reshuffles automatically.
 */
public class BinIzharfedPool implements QuestionPool.Pool {

    private static final Random RNG = new Random();

    // =========================================================================
    // Tracking — indices of questions NOT yet asked this cycle
    // =========================================================================

    private final List<Integer> saudiRemaining = new ArrayList<>();
    private final List<Integer> worldRemaining  = new ArrayList<>();

    public BinIzharfedPool() {
        resetSaudi();
        resetWorld();
    }

    private void resetSaudi() {
        saudiRemaining.clear();
        for (int i = 0; i < SAUDI_POOL.length; i++) saudiRemaining.add(i);
        Collections.shuffle(saudiRemaining, RNG);
    }

    private void resetWorld() {
        worldRemaining.clear();
        for (int i = 0; i < WORLD_POOL.length; i++) worldRemaining.add(i);
        Collections.shuffle(worldRemaining, RNG);
    }

    // =========================================================================
    // Public factory — implements QuestionPool.Pool
    // =========================================================================

    @Override
    public ChallengeDialog.Question next() {
        // Reset a category the moment it runs out
        if (saudiRemaining.isEmpty()) resetSaudi();
        if (worldRemaining.isEmpty())  resetWorld();

        // Pick a category at random; if one is empty, always pick the other
        boolean pickSaudi = RNG.nextBoolean();
        if (saudiRemaining.isEmpty()) pickSaudi = false;
        if (worldRemaining.isEmpty())  pickSaudi = true;

        if (pickSaudi) {
            // Remove the chosen index so it won't appear again this cycle
            int idx = saudiRemaining.remove(RNG.nextInt(saudiRemaining.size()));
            return saudiQuestion(idx);
        } else {
            int idx = worldRemaining.remove(RNG.nextInt(worldRemaining.size()));
            return worldQuestion(idx);
        }
    }

    // =========================================================================
    // 1. Saudi Arabia questions
    //    { questionText, correctAnswer, revealAnswer, hint, isNumericStr }
    // =========================================================================
    private static final String[][] SAUDI_POOL = {
        // Geography
        { "What is the capital city of Saudi Arabia?",
          "riyadh", "Riyadh",
          "It is located in the Najd region in the centre of the Arabian Peninsula", "n" },
        { "What is the desert climate type mostly found in Saudi Arabia?",
          "arid", "Arid (desert climate)",
          "It has very little rainfall and very high temperatures", "n" },
        { "What body of water lies to the west of Saudi Arabia?",
          "red sea", "Red Sea",
          "This sea connects to the Suez Canal to the north", "n" },
        { "What body of water lies to the east of Saudi Arabia?",
          "persian gulf", "Persian Gulf (Arabian Gulf)",
          "Also called the Arabian Gulf; major oil tanker route", "n" },
        { "What is the Islamic holy month when Muslims fast from sunrise to sunset?",
          "ramadan", "Ramadan",
          "It is one of the Five Pillars of Islam", "n" },
        { "What is the holy book of Islam, central to Saudi Arabian culture?",
          "quran", "Quran (Qur'an)",
          "Also spelled Koran; it is written in classical Arabic", "n" },
        { "What strategic strait lies between the Persian Gulf and the Gulf of Oman?",
          "strait of hormuz", "Strait of Hormuz",
          "A narrow passage critical for global oil transport", "n" }, 
        { "What is the capital of United Arab Emirates (UAE)?",
          "abu dhabi", "Abu Dhabi",
          "It's not Dubai.", "n" },
        // History
        { "In what year was the modern Kingdom of Saudi Arabia officially founded?",
          "1932", "1932",
          "It was founded by King Abdulaziz ibn Saud (Ibn Saud)", "y" },
        { "Who was the founder and first king of modern Saudi Arabia?",
          "ibn saud", "Ibn Saud (Abdulaziz ibn Saud)",
          "He unified the regions of the Arabian Peninsula between 1902 and 1932", "n" },
        { "What major natural resource, discovered in 1938, drives Saudi Arabia's economy?",
          "oil", "Oil (Petroleum / Crude Oil)",
          "Saudi Arabia has some of the world's largest proven reserves", "n" },
        // Government and Vision
        { "What type of government does Saudi Arabia have?",
          "absolute monarchy", "Absolute Monarchy",
          "The king holds supreme authority; law is based on Islamic Sharia", "n" },
        { "Who is the Crown Prince and Prime Minister of Saudi Arabia as of 2024?",
          "mohammed bin salman", "Mohammed bin Salman (MBS)",
          "Often referred to by his initials MBS", "n" },
        { "Who is considered the final prophet in Islam?", 
          "muhammad", "Muhammad", 
          "Founder of Islam and regarded as the last prophet", "n" },
        // Culture
        { "What is the official language of Saudi Arabia?",
          "arabic", "Arabic",
          "Specifically Modern Standard Arabic (MSA); regional dialects also spoken", "n" },
        { "What is the official religion of Saudi Arabia?",
          "islam", "Islam",
          "It is one of the world’s major monotheistic religions, practiced widely in the Middle East.", "n" },
        { "What is the currency of Saudi Arabia?",
          "riyal", "Saudi Riyal (SAR)",
          "Also spelled Rial; its code is SAR", "n" },
    };

    private static ChallengeDialog.Question saudiQuestion(int index) {
        String[] e = SAUDI_POOL[index];
        boolean numeric = "y".equals(e[4]);
        return new ChallengeDialog.Question(
                "<html><center>" + e[0] + "</center></html>",
                e[3],
                e[1],
                numeric,
                "GEOGRAPHY: SAUDI ARABIA",
                e[2]
        );
    }

    // =========================================================================
    // 2. World Geography & History questions
    //    { questionText, correctAnswer, revealAnswer, hint, isNumericStr }
    // =========================================================================
    private static final String[][] WORLD_POOL = {
        // Capitals
        { "What is the capital of France?",
          "paris", "Paris",
          "It is located on the Seine River and is home to the Eiffel Tower", "n" },
        { "What is the capital of Japan?",
          "tokyo", "Tokyo",
          "It is the most populous metropolitan area in the world", "n" },
        { "What is the capital of Brazil?",
          "brasilia", "Brasília",
          "It was purpose-built as the capital and inaugurated in 1960", "n" },
        { "What is the capital of Australia?",
          "canberra", "Canberra",
          "A common wrong answer is Sydney — the capital was chosen as a compromise", "n" },
        { "What is the capital of Canada?",
          "ottawa", "Ottawa",
          "Located in Ontario, near the border with Quebec", "n" },
        { "What is the capital of Russia?",
          "moscow", "Moscow",
          "It is the largest city in Europe by population", "n" },
        { "What is the capital of Egypt?",
          "cairo", "Cairo",
          "It is the largest city in Africa and the Arab world", "n" },
        // Continents and geography
        { "What is the largest continent by area?",
          "asia", "Asia",
          "It covers about 30% of Earth's total land area", "n" },
        { "What is the smallest continent by area?",
          "australia", "Australia (Oceania)",
          "Also called Oceania when including surrounding island nations", "n" },
        { "What is the longest river in the world?",
          "nile", "Nile River",
          "It flows northward through northeastern Africa into the Mediterranean Sea", "n" },
        { "What is the largest ocean on Earth?",
          "pacific", "Pacific Ocean",
          "It covers more area than all of Earth's landmasses combined", "n" },
        { "What mountain range contains Mount Everest, the world's highest peak?",
          "himalayas", "Himalayas",
          "Located on the border between Nepal and Tibet (China)", "n" },
        { "What is the name of the world's largest rainforest?",
          "amazon", "Amazon Rainforest",
          "It covers much of Brazil and parts of neighbouring South American countries", "n" },
        // History
        { "In what year did World War II end?",
          "1945", "1945",
          "Germany surrendered in May and Japan in September of that year", "y" },
        { "In what year did World War I begin?",
          "1914", "1914",
          "It was triggered by the assassination of Archduke Franz Ferdinand", "y" },
        { "What ancient wonder was located in Alexandria, Egypt?",
          "lighthouse", "Lighthouse of Alexandria (Pharos)",
          "One of the Seven Wonders of the Ancient World; also called the Pharos", "n" },
        { "Which empire was the largest contiguous land empire in history?",
          "mongol", "Mongol Empire",
          "Founded by Genghis Khan in the 13th century", "n" },
        { "What year did the Berlin Wall fall, symbolising the end of the Cold War?",
          "1989", "1989",
          "Around the 1980s", "y" },
        { "What is the name of the international organisation formed after World War II to maintain peace?",
          "united nations", "United Nations (UN)",
          "It was established in 1945 and is headquartered in New York City", "n" },
        { "Which country has the largest population in the world?",
          "india", "India",
          "It surpassed China as the most populous country in 2023", "n" },
    };

    private static ChallengeDialog.Question worldQuestion(int index) {
        String[] e = WORLD_POOL[index];
        boolean numeric = "y".equals(e[4]);
        return new ChallengeDialog.Question(
                "<html><center>" + e[0] + "</center></html>",
                e[3],
                e[1],
                numeric,
                "WORLD GEOGRAPHY & HISTORY",
                e[2]
        );
    }
}
