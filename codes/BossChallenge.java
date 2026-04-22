package codes;

import java.util.HashMap;
import java.util.Map;
import javax.swing.JFrame;

/**
 * BossChallenge — the single entry point that Battle.java calls.
 *
 * Maps a boss name to a QuestionPool.Pool, picks a question, and
 * delegates all dialog rendering to ChallengeDialog.show().
 *
 * ── HOW TO USE IN Battle.java ─────────────────────────────────────────────────
 *
 *   In applyDamageToPlayer(), call:
 *
 *       int finalDmg = BossChallenge.challenge(frame, boss, rawDmg);
 *       hp = Math.max(0, hp - finalDmg);
 *
 * ── BOSS REGISTRY (current) ───────────────────────────────────────────────────
 *   rainity       -> RainityPool    linear algebra
 *   main          -> MainPool       chemistry symbols + sports rules
 *   jetroids      -> JetroidsPool   trigonometry, F=ma, KE=half*mv^2
 *   celene        -> FedoraPool     atomic mass + molar mass
 *   cedora        -> FedoraPool     atomic mass + molar mass
 *   ryan          -> RyanPool       mean, median, mode, probability
 *   don malek     -> DonMalekPool   random draw from ALL pools
 *   ivy           -> IvyPool        photosynthesis + plant biology
 *   bin izharfed  -> BinIzharfedPool Saudi Arabia + world geography/history
 *   gigglebot3000 -> GigglebotPool  force, power, electricity
 *   jarrelle      -> JarrellePool   cell biology, human body, general biology
 *   <others>      -> RainityPool    default fallback
 *
 * ── ADDING A NEW BOSS ────────────────────────────────────────────────────────
 *   1. Create YourBossPool.java implementing QuestionPool.Pool.
 *   2. Add a delegating inner class to QuestionPool.java.
 *   3. Add one line below:  reg("yourbossname", new QuestionPool.YourBossPool());
 */
public class BossChallenge {

    // Pool registry: boss name (lower-case) -> question pool
    private static final Map<String, QuestionPool.Pool> POOLS = new HashMap<>();

    static {
        // ── Core bosses ───────────────────────────────────────────────────────
        reg("rainity",   new QuestionPool.RainityPool());
        reg("main",      new QuestionPool.MainPool());
        reg("jetroids",  new QuestionPool.JetroidsPool());

        // ── Fedora Group: Celene and Cedora share the same chemistry pool ─────
        reg("celene",    new QuestionPool.FedoraPool());
        reg("cedora",    new QuestionPool.FedoraPool());

        // ── Ryan Group ────────────────────────────────────────────────────────
        reg("ryan",      new QuestionPool.RyanPool());

        // ── Malcholm Group: Don Malek draws from all pools ────────────────────
        reg("don malek", new QuestionPool.DonMalekPool());

        // ── IVy Group ─────────────────────────────────────────────────────────
        reg("ivy",         new QuestionPool.IvyPool());

        // ── Bin Izharfed Group ────────────────────────────────────────────────
        reg("bin izharfed", new QuestionPool.BinIzharfedPool());

        // ── GIGGLEBOT3000 ─────────────────────────────────────────────────────
        reg("gigglebot3000", new QuestionPool.GigglebotPool());

        // ── Jarrelle Group ────────────────────────────────────────────────────
        reg("jarrelle",    new QuestionPool.JarrellePool());
    }

    private static void reg(String name, QuestionPool.Pool pool) {
        POOLS.put(name.toLowerCase(), pool);
    }

    // Default pool used by any boss not listed above
    private static final QuestionPool.Pool DEFAULT_POOL = new QuestionPool.RainityPool();

    // =========================================================================
    // PUBLIC API
    // =========================================================================

    /**
     * Shows a boss-specific challenge dialog and returns the final damage.
     *
     * @param parent    The parent JFrame.
     * @param bossName  The name of the current boss (case-insensitive).
     * @param rawDamage The damage the boss intended to deal.
     * @return          Reduced damage on correct answer; full damage otherwise.
     */
    public static int challenge(JFrame parent, String bossName, int rawDamage) {
        QuestionPool.Pool pool = POOLS.getOrDefault(
                bossName == null ? "" : bossName.toLowerCase(),
                DEFAULT_POOL);
        ChallengeDialog.Question q = pool.next();
        return ChallengeDialog.show(parent, q, rawDamage);
    }
}
