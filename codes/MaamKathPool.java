package codes;

import java.util.Random;

public class MaamKathPool implements QuestionPool.Pool {

    private static final Random RNG = new Random();

    private static final ChallengeDialog.Question[] QUESTIONS = {

        q("Which sentence is grammatically correct?",
            "She don't know the answer.",
            "She doesn't know the answer.",
            "She not knowing the answer.",
            "She didn't knew the answer.",
            "B"),

        q("Choose the correct form: 'Neither the students nor the teacher ___ ready.'",
            "were",
            "are",
            "was",
            "have been",
            "C"),

        q("What is the plural of 'criterion'?",
            "criterions",
            "criterias",
            "criteria",
            "criterium",
            "C"),

        q("Which sentence uses a comma correctly?",
            "I went to the store and, I bought milk.",
            "Although it was raining, we continued the game.",
            "She is smart, but, she is lazy.",
            "He said, that he would come.",
            "B"),

        q("Identify the correct sentence.",
            "The committee have made their decision.",
            "The committee has made its decision.",
            "The committee has made their decision.",
            "The committee have made its decision.",
            "B"),

        q("Which word is spelled correctly?",
            "occurence",
            "occurrence",
            "occurrance",
            "occurance",
            "B"),

        q("'Whom' is correct in which sentence?",
            "Whom is knocking at the door?",
            "I don't know whom left first.",
            "To whom should I address this letter?",
            "Whom went to the market?",
            "C"),

        q("Choose the sentence with correct subject-verb agreement.",
            "The bouquet of roses are beautiful.",
            "Each of the students have a book.",
            "A number of players was injured.",
            "The news is alarming.",
            "D"),

        q("Which sentence uses the semicolon correctly?",
            "I love coffee; but I hate tea.",
            "She studied hard; she passed the exam.",
            "He ran fast; and won the race.",
            "They left early; because it was raining.",
            "B"),

        q("What does 'ambiguous' mean?",
            "Clear and precise",
            "Open to more than one interpretation",
            "Strongly worded",
            "Unimportant",
            "B"),

        q("Which of these is an example of an active voice sentence?",
            "The cake was eaten by the children.",
            "The letter was written by him.",
            "The dog chased the cat.",
            "The song was sung beautifully.",
            "C"),

        q("'Its' vs 'It's' — which is correct here: '___ raining outside.'",
            "Its",
            "It's",
            "Its'",
            "Itss",
            "B"),

        q("Which is NOT a type of journalistic writing?",
            "Feature article",
            "Editorial",
            "Haiku",
            "News report",
            "C"),

        q("In school press writing, what does the inverted pyramid structure mean?",
            "Start with opinions then facts",
            "Start with the least important information",
            "Start with the most important information first",
            "Start with quotes then the headline",
            "C"),

        q("Which of the following is a lead paragraph element?",
            "5 W's and 1 H",
            "Rhyme scheme",
            "Bibliography",
            "Character arc",
            "A"),

        q("What is a 'byline' in journalism?",
            "The last line of an article",
            "A subtitle below the headline",
            "The author's name in a published article",
            "A quote from an interview",
            "C"),

        q("What is the purpose of an editorial?",
            "To report facts without bias",
            "To express the opinion of the publication",
            "To advertise products",
            "To summarize a story",
            "B"),

        q("Which headline follows proper journalistic style?",
            "THE STUDENTS ARE WINNING AWARDS",
            "Students Win Regional Press Conference Honors",
            "students win regional press conference honors",
            "Winning: Students Do It Again!",
            "B"),

        q("What is a 'dateline' in a news article?",
            "The deadline for submission",
            "The date the article will be published",
            "The place and date at the start of a story",
            "The line that separates sections",
            "C"),

        q("Which is the correct possessive form?",
            "the students book",
            "the students' book",
            "the student's books",
            "Both B and C depending on context",
            "D"),

        q("Identify the dangling modifier: '___ Walking down the street, the trees looked beautiful.'",
            "The sentence is correct.",
            "It implies the trees were walking.",
            "It implies the street was walking.",
            "There is no modifier.",
            "B"),

        q("What is a 'feature story' in school press?",
            "A story about the school's budget",
            "An in-depth, human-interest article beyond hard news",
            "A list of school announcements",
            "A summary of a sports event",
            "B"),

        q("Which transition word shows contrast?",
            "Furthermore",
            "In addition",
            "However",
            "Therefore",
            "C"),

        q("What does AP Style refer to in journalism?",
            "A photography format",
            "A set of grammar and writing guidelines for news writing",
            "A type of interview technique",
            "A school press award category",
            "B"),

        q("Which sentence is in the past perfect tense?",
            "She was running when it rained.",
            "She had already left when he arrived.",
            "She will have finished by noon.",
            "She has been studying for hours.",
            "B"),

        // ── SciWings question ────────────────────────────────────────────────
        q("What is SciWings?",
            "The school's official science laboratory handbook",
            "The school's publication paper that produces journalistic articles",
            "A science fair organization for students",
            "An after-school club for creative writing only",
            "B"),

    };

    private static ChallengeDialog.Question q(
            String prompt,
            String a, String b, String c, String d,
            String answer) {
        String[] opts = {a, b, c, d};
        String revealIndex = answer.toUpperCase().trim();
        String revealText;
        switch (revealIndex) {
            case "A" -> revealText = a;
            case "B" -> revealText = b;
            case "C" -> revealText = c;
            case "D" -> revealText = d;
            default  -> revealText = answer;
        }
        String hint = "A: " + a + "  |  B: " + b + "  |  C: " + c + "  |  D: " + d;
        return new ChallengeDialog.Question(
                prompt,
                hint,
                revealIndex,
                false,
                "ENGLISH / PRESS",
                answer + " — " + revealText
        );
    }

    @Override
    public ChallengeDialog.Question next() {
        return QUESTIONS[RNG.nextInt(QUESTIONS.length)];
    }
}