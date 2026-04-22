package codes;

import java.util.Random;

/**
 * DonMalekPool — question pool for the Malcholm Group (Don Malek).
 *
 * Don Malek is the wildcard boss.  Each attack draws a question randomly
 * from ALL available boss pools in the game:
 *
 *   RainityPool      — linear algebra
 *   MainPool         — chemistry symbols + sports rules
 *   JetroidsPool     — trigonometry, F = ma, KE = ½mv²
 *   FedoraPool       — atomic mass + molar mass
 *   RyanPool         — mean, median, mode, probability
 *   IvyPool          — photosynthesis + plant biology
 *   BinIzharfedPool  — Saudi Arabia + world geography/history
 *   GigglebotPool    — force, power, electricity
 *   JarrellePool     — cell biology, human body, general biology
 *
 * This makes him unpredictable — the player never knows what subject
 * is coming next.
 */
public class DonMalekPool implements QuestionPool.Pool {

    private static final Random RNG = new Random();

    private static final QuestionPool.Pool[] ALL_POOLS = {
        new QuestionPool.RainityPool(),
        new QuestionPool.MainPool(),
        new QuestionPool.JetroidsPool(),
        new QuestionPool.FedoraPool(),
        new QuestionPool.RyanPool(),
        new QuestionPool.IvyPool(),
        new QuestionPool.BinIzharfedPool(),
        new QuestionPool.GigglebotPool(),
        new QuestionPool.JarrellePool(),
    };

    // =========================================================================
    // Public factory — implements QuestionPool.Pool
    // =========================================================================

    @Override
    public ChallengeDialog.Question next() {
        // Pick a random pool, then let that pool pick a random question
        return ALL_POOLS[RNG.nextInt(ALL_POOLS.length)].next();
    }
}
