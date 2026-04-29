package codes;

import java.util.Random;

/**
 * QuestionPool — central registry of every boss pool in the game.
 *
 * Each inner class implements the Pool interface and delegates to its
 * corresponding standalone pool file.  This keeps BossChallenge's
 * registration call consistent (new QuestionPool.XPool()) while
 * the actual question logic lives in the individual files.
 *
 * ── Standalone pool files ─────────────────────────────────────────────────────
 *   RainityPool.java      — linear algebra
 *   MainPool.java         — chemistry symbols + sports rules
 *   JetroidsPool.java     — trigonometry, F = ma, KE = ½mv²
 *   FedoraPool.java       — atomic mass + molar mass   (Celene / Celora)
 *   RyanPool.java         — mean, median, mode, probability
 *   DonMalekPool.java     — random draw from ALL pools (Don Malek)
 *   IvyPool.java          — photosynthesis + plant biology (IVy)
 *   BinIzharfedPool.java  — Saudi Arabia + world geography/history (Bin Izharfed)
 *   GigglebotPool.java    — force, power, electricity (GIGGLEBOT3000)
 *   JarrellePool.java     — cell biology, human body, general biology (Jarrelle)
 *
 * ── Adding a new boss pool ────────────────────────────────────────────────────
 *   1. Create YourBossPool.java implementing QuestionPool.Pool.
 *   2. Add a delegating inner class here (see pattern below).
 *   3. Register it in BossChallenge.java.
 */
public class QuestionPool {

    // =========================================================================
    // Pool interface — one method, one contract
    // =========================================================================
    public interface Pool {
        ChallengeDialog.Question next();
    }

    // =========================================================================
    // Inner delegates — each wraps its standalone file
    // =========================================================================

    /** Rainity — linear algebra (ax +- b = c, ax = c, x/a + b = c) */
    public static class RainityPool implements Pool {
        private final codes.RainityPool delegate = new codes.RainityPool();
        @Override public ChallengeDialog.Question next() { return delegate.next(); }
    }

    /** MAIN — chemistry symbols OR sports rules */
    public static class MainPool implements Pool {
        private final codes.MainPool delegate = new codes.MainPool();
        @Override public ChallengeDialog.Question next() { return delegate.next(); }
    }

    /** Jetroids — trigonometry, F = ma, KE = half*mv^2 */
    public static class JetroidsPool implements Pool {
        private final codes.JetroidsPool delegate = new codes.JetroidsPool();
        @Override public ChallengeDialog.Question next() { return delegate.next(); }
    }

    /** Fedora Group (Celene / Cedora) — atomic mass + molar mass */
    public static class FedoraPool implements Pool {
        private final codes.FedoraPool delegate = new codes.FedoraPool();
        @Override public ChallengeDialog.Question next() { return delegate.next(); }
    }

    /** RYAN — statistics: mean, median, mode, probability */
    public static class RyanPool implements Pool {
        private final codes.RyanPool delegate = new codes.RyanPool();
        @Override public ChallengeDialog.Question next() { return delegate.next(); }
    }

    /** Malcholm Group (Don Malek) — random draw from ALL pools */
    public static class DonMalekPool implements Pool {
        private final codes.DonMalekPool delegate = new codes.DonMalekPool();
        @Override public ChallengeDialog.Question next() { return delegate.next(); }
    }

    /** IVy — photosynthesis and plant biology */
    public static class IvyPool implements Pool {
        private final codes.IvyPool delegate = new codes.IvyPool();
        @Override public ChallengeDialog.Question next() { return delegate.next(); }
    }

    /** Bin Izharfed — Saudi Arabia history/geography + world geography/history */
    public static class BinIzharfedPool implements Pool {
        private final codes.BinIzharfedPool delegate = new codes.BinIzharfedPool();
        @Override public ChallengeDialog.Question next() { return delegate.next(); }
    }

    /** GIGGLEBOT3000 — physics: force, power, electricity */
    public static class GigglebotPool implements Pool {
        private final codes.GigglebotPool delegate = new codes.GigglebotPool();
        @Override public ChallengeDialog.Question next() { return delegate.next(); }
    }

    /** Jarrelle — general biology: cells, human body, genetics, ecology */
    public static class JarrellePool implements Pool {
        private final codes.JarrellePool delegate = new codes.JarrellePool();
        @Override public ChallengeDialog.Question next() { return delegate.next(); }
    }

    /** Ma'am Kath — grammar and school press writing */
    public static class MaamKathPool implements Pool {
        private final codes.MaamKathPool delegate = new codes.MaamKathPool();
        @Override public ChallengeDialog.Question next() { return delegate.next(); }
    }
}