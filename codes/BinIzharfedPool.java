package codes;

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
 */
public class BinIzharfedPool implements QuestionPool.Pool {

    private static final Random RNG = new Random();

    // =========================================================================
    // Public factory — implements QuestionPool.Pool
    // =========================================================================

    @Override
    public ChallengeDialog.Question next() {
        return RNG.nextBoolean() ? saudiQuestion() : worldQuestion();
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
        { "What is the largest city in Saudi Arabia?",
          "riyadh", "Riyadh",
          "It is also the political and administrative capital", "n" },
        { "What body of water lies to the west of Saudi Arabia?",
          "red sea", "Red Sea",
          "This sea connects to the Suez Canal to the north", "n" },
        { "What body of water lies to the east of Saudi Arabia?",
          "persian gulf", "Persian Gulf (Arabian Gulf)",
          "Also called the Arabian Gulf; major oil tanker route", "n" },
        { "What is the name of the world's largest continuous sand desert, located mostly in Saudi Arabia?",
          "rub al khali", "Rub' al Khali (Empty Quarter)",
          "Its Arabic name means 'Empty Quarter'", "n" },
        { "Which two cities in Saudi Arabia are the holiest sites in Islam?",
          "mecca", "Mecca (and Medina)",
          "The birthplace of Islam and the home of the Prophet's Mosque", "n" },
        { "What is the holy book of Islam, central to Saudi Arabian culture?",
          "quran", "Quran (Qur'an)",
          "Also spelled Koran; it is written in classical Arabic", "n" },
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
        { "What is the name of the Saudi Arabian state oil company, one of the largest in the world?",
          "aramco", "Saudi Aramco",
          "Its full name is Saudi Arabian Oil Company", "n" },
        // Government and Vision
        { "What type of government does Saudi Arabia have?",
          "absolute monarchy", "Absolute Monarchy",
          "The king holds supreme authority; law is based on Islamic Sharia", "n" },
        { "What is the name of Saudi Arabia's ambitious economic reform plan launched in 2016?",
          "vision 2030", "Vision 2030",
          "It aims to diversify the economy away from oil dependence", "n" },
        { "Who is the Crown Prince and Prime Minister of Saudi Arabia as of 2024?",
          "mohammed bin salman", "Mohammed bin Salman (MBS)",
          "Often referred to by his initials MBS", "n" },
        // Culture
        { "What is the official language of Saudi Arabia?",
          "arabic", "Arabic",
          "Specifically Modern Standard Arabic (MSA); regional dialects also spoken", "n" },
        { "What is the official religion of Saudi Arabia?",
          "islam", "Islam",
          "Saudi Arabia is the birthplace of Islam and home to its two holiest cities", "n" },
        { "What is the currency of Saudi Arabia?",
          "riyal", "Saudi Riyal (SAR)",
          "Also spelled Rial; its code is SAR", "n" },
    };

    private static ChallengeDialog.Question saudiQuestion() {
        String[] e = SAUDI_POOL[RNG.nextInt(SAUDI_POOL.length)];
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
          "It fell on November 9, 1989", "y" },
        { "What is the name of the international organisation formed after World War II to maintain peace?",
          "united nations", "United Nations (UN)",
          "It was established in 1945 and is headquartered in New York City", "n" },
        { "Which country has the largest population in the world?",
          "india", "India",
          "India surpassed China as the most populous country in 2023", "n" },
    };

    private static ChallengeDialog.Question worldQuestion() {
        String[] e = WORLD_POOL[RNG.nextInt(WORLD_POOL.length)];
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
