package codes;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * SaveSystem — persistent game state for map, flags, boss defeats, and time.
 *
 * File format  (docs/saveFile.txt):
 * ──────────────────────────────────
 *   MAP:RoomClassName
 *   FLAGS:flag1,flag2,flag3
 *   BATTLES:boss1,boss2,boss3
 *   TIME:secondsElapsed
 *
 * ── Time tracking ────────────────────────────────────────────────────────────
 * Call SaveSystem.startTimer() when gameplay begins (or resumes after load).
 * The timer accumulates seconds in the background. When saving, call
 * SaveSystem.saveGame(builder) — the system automatically adds the current
 * session's elapsed seconds to whatever base time was loaded from disk.
 *
 * ── Boss defeat rules ─────────────────────────────────────────────────────────
 * • isDefeated(boss) — returns true if boss is permanently beaten.
 * • markDefeated(boss) — call this on a WIN. Persists across sessions.
 * • A loss does NOT call markDefeated, so the fight can be retried.
 * • Once marked defeated, that boss entry persists forever in the save file.
 *
 * ── Position removed ──────────────────────────────────────────────────────────
 * Position tracking has been removed entirely. Maps manage their own spawn
 * logic independently of the save system.
 *
 * ── Typical usage ─────────────────────────────────────────────────────────────
 *   // On game start / map load:
 *   SaveSystem.SaveData data = SaveSystem.loadGame();
 *   SaveSystem.startTimer(data.timeSeconds);   // resume accumulated time
 *
 *   // On boss win:
 *   SaveSystem.markDefeated("Ron");
 *
 *   // On save:
 *   SaveSystem.saveGame(
 *       new SaveSystem.SaveData.Builder("G2_Room2_PD6")
 *           .flag("item_lipstick")
 *           .battles(data.battles)             // carry over past defeats
 *   );
 *
 *   // On map transition (no save needed — just query state):
 *   if (data.isDefeated("IVy")) { ... }
 */
public class SaveSystem {

    private static final String SAVE_PATH = "docs/saveFile.txt";

    public static final String KEY_MAP     = "MAP";
    public static final String KEY_FLAGS   = "FLAGS";
    public static final String KEY_BATTLES = "BATTLES";
    public static final String KEY_TIME    = "TIME";

    // =========================================================================
    // Time tracking
    // =========================================================================

    /** Wall-clock millisecond at which the current session started. -1 = not running. */
    private static long sessionStartMs = -1;

    /** Accumulated seconds from all previous sessions (loaded from file). */
    private static long baseTimeSeconds = 0;

    /**
     * Start (or restart) the session timer.
     * Pass the timeSeconds value from the loaded SaveData so prior sessions
     * are included in the total.
     *
     * @param loadedSeconds  seconds already saved to disk (0 for a new game)
     */
    public static void startTimer(long loadedSeconds) {
        baseTimeSeconds = loadedSeconds;
        sessionStartMs  = System.currentTimeMillis();
    }

    /**
     * Returns total elapsed seconds = base (from disk) + current session.
     * Safe to call before startTimer — returns baseTimeSeconds in that case.
     */
    public static long getTotalSeconds() {
        if (sessionStartMs < 0) return baseTimeSeconds;
        long sessionSeconds = (System.currentTimeMillis() - sessionStartMs) / 1000L;
        return baseTimeSeconds + sessionSeconds;
    }

    /**
     * Pause the timer (e.g. when the game window loses focus or a menu opens).
     * Flushes the current session into baseTimeSeconds so nothing is lost.
     */
    public static void pauseTimer() {
        if (sessionStartMs < 0) return;
        baseTimeSeconds = getTotalSeconds();
        sessionStartMs  = -1;
    }

    /**
     * Resume the timer after a pause. Call with 0 — baseTimeSeconds is already
     * up to date from the last pauseTimer() call.
     */
    public static void resumeTimer() {
        if (sessionStartMs >= 0) return;   // already running
        sessionStartMs = System.currentTimeMillis();
    }

    // =========================================================================
    // Boss defeat helpers (static convenience — wraps the loaded SaveData)
    // =========================================================================

    /**
     * In-memory set of defeated bosses for the current session.
     * Populated by loadGame() and updated by markDefeated().
     * Always pass this list into your Builder when saving.
     */
    private static final List<String> defeatedBosses = new ArrayList<>();

    /**
     * Permanently mark a boss as defeated for this save file.
     * Call on a player WIN — do NOT call on a loss.
     *
     * @param bossName  the boss name as it appears in battleStats.txt
     */
    public static void markDefeated(String bossName) {
        if (bossName == null || bossName.isBlank()) return;
        String trimmed = bossName.trim();
        boolean alreadyDefeated = defeatedBosses.stream()
                .anyMatch(b -> b.equalsIgnoreCase(trimmed));
        if (!alreadyDefeated) {
            defeatedBosses.add(trimmed);
            System.out.println("[SaveSystem] Boss defeated: " + trimmed);
        }
    }

    /**
     * Returns true if the given boss has been permanently defeated.
     * Use this to skip battle triggers on map reload.
     *
     * @param bossName  the boss name to check
     */
    public static boolean isDefeated(String bossName) {
        if (bossName == null || bossName.isBlank()) return false;
        return defeatedBosses.stream().anyMatch(b -> b.equalsIgnoreCase(bossName.trim()));
    }

    /**
     * Returns an unmodifiable snapshot of all defeated boss names.
     * Pass this to Builder.battles() when saving.
     */
    public static List<String> getDefeatedBosses() {
        return List.copyOf(defeatedBosses);
    }

    // =========================================================================
    // SaveData
    // =========================================================================
    public static class SaveData {

        public final String       map;
        public final List<String> flags;
        public final List<String> battles;
        public final long         timeSeconds;

        private SaveData(Builder b) {
            this.map         = b.map;
            this.flags       = List.copyOf(b.flags);
            this.battles     = List.copyOf(b.battles);
            this.timeSeconds = b.timeSeconds;
        }

        public static SaveData defaultData(String mapName) {
            return new Builder(mapName).build();
        }

        /** True if this save belongs to the given map (case-insensitive). */
        public boolean isMap(String mapName) {
            return map != null && map.equalsIgnoreCase(mapName);
        }

        /** True if a story/event flag is set. */
        public boolean hasFlag(String flag) {
            return flags != null
                    && flags.stream().anyMatch(f -> f.equalsIgnoreCase(flag));
        }

        /**
         * True if the boss has been permanently defeated.
         * Delegates to the static defeatedBosses list so the answer is always
         * current even if the SaveData object is stale.
         */
        public boolean isDefeated(String bossName) {
            return SaveSystem.isDefeated(bossName);
        }

        public String formattedTime() {
            long h = timeSeconds / 3600;
            long m = (timeSeconds % 3600) / 60;
            long s = timeSeconds % 60;
            return String.format("%02d:%02d:%02d", h, m, s);
        }

        @Override
        public String toString() {
            return "SaveData{map=" + map
                    + ", flags=" + flags
                    + ", battles=" + battles
                    + ", time=" + formattedTime() + "}";
        }

        // ── Builder ───────────────────────────────────────────────────────────
        /**
         * Fluent builder — construct one before calling SaveSystem.saveGame().
         *
         * Example:
         *   SaveSystem.saveGame(
         *       new SaveSystem.SaveData.Builder("G2_Room2_PD6")
         *           .flag("item_lipstick")
         *           .battles(SaveSystem.getDefeatedBosses())
         *   );
         *   // Time is injected automatically by saveGame() — no need to set it.
         */
        public static class Builder {
            private final String       map;
            private final List<String> flags   = new ArrayList<>();
            private final List<String> battles = new ArrayList<>();
            private long               timeSeconds = 0;

            public Builder(String mapName) {
                this.map = mapName;
            }

            public Builder flag(String flag) {
                if (flag != null && !flag.isBlank()) flags.add(flag);
                return this;
            }

            public Builder flags(List<String> list) {
                if (list != null) list.stream()
                        .filter(f -> f != null && !f.isBlank())
                        .forEach(flags::add);
                return this;
            }

            public Builder battle(String boss) {
                if (boss != null && !boss.isBlank()) battles.add(boss);
                return this;
            }

            public Builder battles(List<String> list) {
                if (list != null) list.stream()
                        .filter(b -> b != null && !b.isBlank())
                        .forEach(battles::add);
                return this;
            }

            /** Normally called internally by saveGame(). Override only if needed. */
            public Builder time(long seconds) {
                this.timeSeconds = seconds;
                return this;
            }

            public SaveData build() { return new SaveData(this); }
        }
    }

    // =========================================================================
    // Save
    // =========================================================================

    /**
     * Save the game. The current total playtime is injected automatically —
     * you do NOT need to call builder.time() yourself.
     *
     * @param builder  a populated Builder (map + flags + battles)
     */
    public static void saveGame(SaveData.Builder builder) {
        // Inject live time — always up to date regardless of when saveGame is called
        builder.time(getTotalSeconds());
        saveGame(builder.build());
    }

    /** Overload that accepts a fully built SaveData (time should already be set). */
    public static void saveGame(SaveData data) {
        File dir = new File("docs");
        if (!dir.exists()) dir.mkdirs();

        try (BufferedWriter bw =
                new BufferedWriter(new FileWriter(new File(SAVE_PATH), false))) {

            bw.write(KEY_MAP     + ":" + sanitize(data.map));    bw.newLine();
            bw.write(KEY_FLAGS   + ":" + join(data.flags));      bw.newLine();
            bw.write(KEY_BATTLES + ":" + join(data.battles));    bw.newLine();
            bw.write(KEY_TIME    + ":" + data.timeSeconds);      bw.newLine();

            System.out.println("[SaveSystem] Saved → " + new File(SAVE_PATH).getAbsolutePath()
                    + "  time=" + data.timeSeconds + "s");

        } catch (IOException ex) {
            System.err.println("[SaveSystem] Failed to save: " + ex.getMessage());
        }
    }

    // =========================================================================
    // Load
    // =========================================================================

    /**
     * Load the save file. Also populates the static defeatedBosses list so
     * isDefeated() / markDefeated() work correctly during the session.
     *
     * After calling this, call SaveSystem.startTimer(data.timeSeconds) to
     * resume the accumulated playtime.
     *
     * @param expectedMap  if non-null, a warning is logged when the save
     *                     belongs to a different map (does not block loading)
     */
    public static SaveData loadGame(String expectedMap) {
        File save = new File(SAVE_PATH);
        if (!save.exists()) {
            System.out.println("[SaveSystem] No save file — fresh start.");
            defeatedBosses.clear();
            return SaveData.defaultData(expectedMap != null ? expectedMap : "");
        }

        String       parsedMap     = "";
        List<String> parsedFlags   = new ArrayList<>();
        List<String> parsedBattles = new ArrayList<>();
        long         parsedTime    = 0;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(save), "UTF-8"))) {

            String line;
            while ((line = br.readLine()) != null) {
                line = line.replace("\uFEFF", "").trim();
                if (line.isEmpty()) continue;

                int colon = line.indexOf(':');
                if (colon < 0) continue;

                String key   = line.substring(0, colon).trim().toUpperCase();
                String value = line.substring(colon + 1).trim();

                switch (key) {
                    case KEY_MAP -> parsedMap = value;

                    case KEY_FLAGS -> {
                        if (!value.isEmpty())
                            Arrays.stream(value.split(","))
                                  .map(String::trim)
                                  .filter(s -> !s.isBlank())
                                  .forEach(parsedFlags::add);
                    }
                    case KEY_BATTLES -> {
                        if (!value.isEmpty())
                            Arrays.stream(value.split(","))
                                  .map(String::trim)
                                  .filter(s -> !s.isBlank())
                                  .forEach(parsedBattles::add);
                    }
                    case KEY_TIME -> {
                        try { parsedTime = Long.parseLong(value); }
                        catch (NumberFormatException ex) {
                            System.err.println("[SaveSystem] Bad TIME '" + value
                                    + "' — defaulting to 0.");
                        }
                    }
                    // Silently ignore POSITION (legacy) and any unknown keys
                    default ->
                        System.out.println("[SaveSystem] Skipped key: '" + key + "'");
                }
            }

        } catch (IOException ex) {
            System.err.println("[SaveSystem] Read error: " + ex.getMessage()
                    + " — using defaults.");
            defeatedBosses.clear();
            return SaveData.defaultData(expectedMap != null ? expectedMap : "");
        }

        if (expectedMap != null && !expectedMap.equalsIgnoreCase(parsedMap)) {
            System.out.println("[SaveSystem] WARNING: save is for map '"
                    + parsedMap + "' but current map is '" + expectedMap + "'.");
        }

        // Populate the static defeated-boss registry for this session
        defeatedBosses.clear();
        parsedBattles.forEach(SaveSystem::markDefeated);

        SaveData result = new SaveData.Builder(parsedMap)
                .flags(parsedFlags)
                .battles(parsedBattles)
                .time(parsedTime)
                .build();

        System.out.println("[SaveSystem] Loaded → " + result);
        return result;
    }

    /** Load without map validation. */
    public static void loadGame() {
        loadGame(null);
    }

    // =========================================================================
    // Delete
    // =========================================================================

    /** Wipe the save file and reset in-memory state (use for New Game). */
    public static void deleteSave() {
        File save = new File(SAVE_PATH);
        if (save.exists() && save.delete())
            System.out.println("[SaveSystem] Save deleted.");
        defeatedBosses.clear();
        baseTimeSeconds = 0;
        sessionStartMs  = -1;
    }

    // =========================================================================
    // Internal helpers
    // =========================================================================

    private static String join(List<String> list) {
        return (list == null || list.isEmpty()) ? "" : String.join(",", list);
    }

    private static String sanitize(String s) {
        return (s == null) ? "" : s.replaceAll("[:\\n\\r,]", "_");
    }
}