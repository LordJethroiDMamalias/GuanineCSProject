/* 
    HELLO! This is Jethroi. Battle system made by yours truly + Francis + Claude AI
    Possible enemies as of now: Jardenito, IVy, Rainity, Celene, Jetroids, Jarrelle, Main, RYAN, GIGGLEBOT3000, Bin Izharfed, Don Malek
    Final boss TBA. have to figure out pa how to add custom attacks

    ── BossChallenge integration ────────────────────────────────────────────────
    Every point where  hp = Math.max(0, hp - dmg)  would have run now goes
    through BossChallenge.challenge(frame, boss, dmg) first.  That call opens a
    modal dialog, freezes all input, and returns either reduced or full damage
    depending on whether the player solved the equation correctly.
*/

package codes;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.*;
import javax.sound.sampled.*;

public class Battle {

    // ── Single shared Random instance ─────────────────────────────────────────
    private static final Random RNG = new Random();

    // ── Swing components ──────────────────────────────────────────────────────
    private JFrame       frame;
    private JLayeredPane overlay;
    private JPanel       hudPanel;
    private JButton      fightButton, checkButton, blockButton, healButton;
    private ImageIcon    backgroundImage, playerBattle, enemyBattle;
    private JPanel       hpBarFill, enemyHpBarFill;
    private JLabel       hpLabel, enemyHpLabel;
    private JTextArea    battleLog;
    private JScrollPane  logScroll;

    // ── Battle state ──────────────────────────────────────────────────────────
    private String         boss;
    private String         flavor, flavorLow, passive, check;
    private boolean        active        = false;
    private boolean        ending        = false;
    private boolean        turnLocked    = false;
    private boolean        blocking           = false;
    private boolean        skipNextTurn       = false;
    private boolean        lastActionWasBlock = false;
    private Clip bgmClip = null;
    private final java.util.List<Clip> activeSFX = new java.util.concurrent.CopyOnWriteArrayList<>();
    private int            healsUsed          = 0;
    private static final int MAX_HEALS        = 5;
    private Timer          enemyTurnTimer = null;
    public  static boolean paused        = false;

    // ── Player stats ──────────────────────────────────────────────────────────
    int hp        = 100;
    int maxHp     = 100;
    int playerAtk = 12;
    int playerDef = 5;

    // ── Enemy stats ───────────────────────────────────────────────────────────
    int enemyHp    = 0;
    int enemyMaxHp = 0;
    int enemyAtk   = 0;
    int enemyDef   = 0;

    // ── Cross-passive mechanic fields ─────────────────────────────────────────
    boolean lastAttackNegated = false;
    int     poisonDuration    = 0;
    int     poisonDmg         = 0;
    int     ryanHpMilestone   = 0;

    // ── Boss-specific state ───────────────────────────────────────────────────
    private int     celeneTurnCounter  = 0;
    private String  celeneState        = "Celene";
    private int     rageStacks         = 0;
    private boolean malekFinalSwigUsed = false;

    private final List<EnemyPassive> enemyPassives = new ArrayList<>();

    // =========================================================================
    // EnemyPassive interface
    // =========================================================================
    interface EnemyPassive {
        default String onBeforeEnemyAttack(Battle b) { return ""; }
        default String onAfterEnemyAttack(Battle b)  { return ""; }
        default int    onPlayerAttack(Battle b, int rawDamage) { return rawDamage; }
        default String onAfterPlayerAttack(Battle b) { return ""; }
    }

    // ── IVy — Poison Ivy ──────────────────────────────────────────────────────
    static class IvyPoisonPassive implements EnemyPassive {
        @Override public String onBeforeEnemyAttack(Battle b) {
            if (b.poisonDuration <= 0) return "";
            // Poison ticks also go through BossChallenge so the player can
            // mitigate each tick by solving an equation.
            int rawPoison = b.poisonDmg;
            int finalPoison = BossChallenge.challenge(b.frame, "ivy", rawPoison);
            b.hp = Math.max(0, b.hp - finalPoison);
            b.poisonDuration--;
            return "Poison burns for " + finalPoison + " damage! ("
                    + b.poisonDuration + " turn" + (b.poisonDuration == 1 ? "" : "s") + " remaining)";
        }
        @Override public String onAfterEnemyAttack(Battle b) {
            int newDmg = 2 + RNG.nextInt(3);
            if (b.poisonDuration > 0) {
                b.poisonDuration = Math.min(b.poisonDuration + 1, 6);
                b.poisonDmg = newDmg;
                return "IVy deepens the infection! Poison duration +1 ("
                        + b.poisonDuration + " turns, " + newDmg + " dmg/turn).";
            } else {
                b.poisonDuration = 1;
                b.poisonDmg = newDmg;
                return "IVy's attack POISONS you! " + newDmg + " dmg/turn for 1 turn.";
            }
        }
    }

    // ── RYAN — Pure Hatred ────────────────────────────────────────────────────
    static class RyanPureHatredPassive implements EnemyPassive {
        @Override public String onAfterPlayerAttack(Battle b) {
            int newMilestone = b.enemyHp / 10;
            if (newMilestone < b.ryanHpMilestone) {
                int steps   = b.ryanHpMilestone - newMilestone;
                int defGain = steps * 2;
                b.enemyDef       += defGain;
                b.ryanHpMilestone = newMilestone;
                return "RYAN's HATRED burns hotter! DEF +" + defGain + " (now " + b.enemyDef + ").";
            }
            return "";
        }
    }

    static class RyanLastStandPassive implements EnemyPassive {
        private boolean triggeredThisTurn = false;
        @Override public String onBeforeEnemyAttack(Battle b) {
            triggeredThisTurn = false;
            return "";
        }
        @Override public String onAfterPlayerAttack(Battle b) {
            if (!triggeredThisTurn && b.enemyHp > 0 && b.enemyHp <= 10) {
                if (RNG.nextInt(100) < 5) {
                    triggeredThisTurn = true;
                    b.enemyHp = b.enemyMaxHp;
                    b.enemyDef = -999;
                    return "RYAN JUST MESSED YOU UP! He fully heals to " + b.enemyMaxHp + " HP. YOU'LL NEVER KILL HIM NOW!";
                }
            }
            return "";
        }
    }

    // ── Bin Izharfed — Illusion ───────────────────────────────────────────────
    static class IzharfedIllusionPassive implements EnemyPassive {
        @Override public int onPlayerAttack(Battle b, int rawDamage) {
            if (b.enemyHp < b.enemyMaxHp / 2 && RNG.nextInt(100) < 20) {
                b.lastAttackNegated = true;
                return 0;
            }
            return rawDamage;
        }
    }

    // =========================================================================
    // Damage formulae
    // =========================================================================
    private int enemyDamage(int atk) {
        return Math.max(1, (int)(atk + RNG.nextInt(4) - playerDef * 0.5));
    }

    private int playerDamageCalc(int base) {
        return Math.max(1, (int)(base - enemyDef * 0.5));
    }

    // =========================================================================
    // Helper — applies math-challenge gating before reducing player HP.
    //
    // Every enemy attack that damages the player must go through this method
    // instead of writing  hp = Math.max(0, hp - dmg)  directly.
    //
    // Returns the final damage actually dealt (after player's math performance).
    // =========================================================================
    private int applyDamageToPlayer(int rawDmg) {
        if (rawDmg <= 0) return 0;
        // Routes to boss-specific question pool: Rainity→algebra, Main→chemistry/sports,
        // Jetroids→trig/physics, all others→algebra (default).
        int finalDmg = BossChallenge.challenge(frame, boss, rawDmg);
        hp = Math.max(0, hp - finalDmg);
        return finalDmg;
    }

    // =========================================================================
    // Boss-specific attack handlers
    //
    // Each handler now calls applyDamageToPlayer(dmg) instead of modifying
    // hp directly, so BossChallenge always intercepts damage before it lands.
    // =========================================================================

    private String handleCeleneAttack() {
        celeneTurnCounter++;
        if (celeneTurnCounter % 2 == 1) {
            celeneState = "Celene";
            enemyDef   += 5;
            enemyAtk    = Math.max(0, enemyAtk - 5);
            int raw  = enemyDamage(enemyAtk);
            int dmg  = applyDamageToPlayer(raw);
            return "'...'  [DEF +5 | ATK -5] — dealt " + dmg + " damage. (Celene State)";
        } else {
            celeneState = "Celora";
            enemyAtk   += 5;
            enemyDef    = Math.max(0, enemyDef - 5);
            int raw  = enemyDamage(enemyAtk);
            int dmg  = applyDamageToPlayer(raw);
            return "'!!!'  [ATK +5 | DEF -5] — dealt " + dmg + " damage! (Celora State)";
        }
    }

    private String handleJetroidsAttack() {
        // Two-hit attack: each hit gets its own math challenge.
        int raw1 = enemyDamage(enemyAtk);
        int hit1 = applyDamageToPlayer(raw1);
        int raw2 = enemyDamage(enemyAtk);
        int hit2 = applyDamageToPlayer(raw2);
        return boss + " attacks TWICE!\n  First hit deals " + hit1
                + " damage.\n  Second hit deals " + hit2 + " damage!";
    }

    private String handleJarrelleAttack() {
        rageStacks = Math.min(rageStacks + 1, 5);
        if (rageStacks >= 5) {
            int raw = enemyDamage(enemyAtk) + 10;
            int dmg = applyDamageToPlayer(raw);
            rageStacks = 0;
            return "'I'M REALLY ANGRY!' +10 bonus damage!\nDealt " + dmg + " damage! [Rage reset to 0]";
        } else {
            int raw = enemyDamage(enemyAtk);
            int dmg = applyDamageToPlayer(raw);
            return boss + " seethes! dealt " + dmg + " damage. [Rage: " + rageStacks + "/5]";
        }
    }

    private String handleMainAttack() {
        // Case 3 (the only damaging move) always triggers a BossChallenge dialog
        // via applyDamageToPlayer(). The other cases are gimmick moves that deal
        // no direct damage, so they correctly show no dialog. To ensure the player
        // faces a challenge on every other attack, we guarantee the damaging case
        // fires at least every other turn by using a 50/50 split: half the time
        // it's the boosted hit, half the time it's one of the three gimmick moves.
        int roll = RNG.nextInt(2) == 0
                ? 3                     // damaging move — always shows challenge
                : RNG.nextInt(3);       // one of the three gimmick moves (0, 1, 2)

        return switch (roll) {
            case 0 -> {
                // HP swap — no direct damage, no challenge dialog
                int temp = hp;
                hp      = Math.min(enemyHp, maxHp);
                enemyHp = Math.min(temp, enemyMaxHp);
                yield "'Haha I swapped the HP bars lmaooo'";
            }
            case 1 -> "'Oh wait, was I supposed to attack? My bad lol'";
            case 2 -> {
                enemyDef += 2;
                yield "Main: 'I'm getting very defensive rn' DEF +2 (now " + enemyDef + ").";
            }
            default -> {  // case 3 — boosted hit, always shows BossChallenge dialog
                enemyAtk += 4;
                int raw = enemyDamage(enemyAtk);
                int dmg = applyDamageToPlayer(raw);
                yield "'I peeked at the code. I'm going to increase my attack' boosted hit for "
                        + dmg + " damage!";
            }
        };
    }

    private String handleRainityAttack() {
        boolean focused = hp < maxHp / 2;
        int base = enemyAtk + RNG.nextInt(4);
        if (focused) base = (int)(base * 1.5f);
        int raw = Math.max(1, (int)(base - playerDef * 0.5));
        int dmg = applyDamageToPlayer(raw);
        String suffix = focused ? "\nShe senses your weakness! Her damage is increased by +50%!" : "";
        return boss + " strikes for " + dmg + " damage!" + suffix;
    }

    private String handleGigglebotAttack() {
        int raw  = enemyDamage(enemyAtk);
        int dmg  = applyDamageToPlayer(raw);
        int heal = Math.max(1, (int)(dmg * 0.25));
        enemyHp  = Math.min(enemyHp + heal, enemyMaxHp);
        return boss + " processes: dealt " + dmg + " damage, self-repaired " + heal + " HP.";
    }
    
    private String handleBinIzharfedAttack() {
        // 60% chance Stellar Descent, 40% chance Cloudbombs.
        // Weighted so the multi-hit move comes up more often, keeping
        // pressure varied without Cloudbombs being spammable.
        int roll = RNG.nextInt(10);

        return switch (roll < 6 ? 0 : 1) {

            // ── Move 0: Stellar Descent ───────────────────────────────────────
            // 2–3 impacts at full enemyAtk (not 75%).
            // Total raw output: ~2–3× a normal hit, so a bad-answer streak
            // across all impacts genuinely threatens the player.
            // Each impact still gets its own BossChallenge, so a skilled
            // player can dramatically reduce the total.
            case 0 -> {
                int hits     = 2 + RNG.nextInt(2);           // 2 or 3 impacts (not 4)
                int totalDmg = 0;
                StringBuilder sb = new StringBuilder();
                sb.append("✦ Bin Izharfed summons the heavens! Celestial bodies rain down!\n");

                for (int i = 1; i <= hits; i++) {
                    // Full enemyAtk base — same formula as every other boss.
                    // This makes each rock hit like a normal attack, so 2–3
                    // of them stacks up without feeling like a one-shot.
                    int raw = enemyDamage(enemyAtk);
                    int dmg = applyDamageToPlayer(raw);
                    totalDmg += dmg;
                    sb.append("  Impact ").append(i).append(": ").append(dmg).append(" damage!\n");
                    if (hp <= 0) break;
                }

                sb.append("Stellar Descent — ").append(hits)
                  .append(" impacts, ").append(totalDmg).append(" total damage!");
                yield sb.toString();
            }

            // ── Move 1: Cloudbombs ────────────────────────────────────────────
            // Reduced to 5 ticks (down from 7) but each tick hits harder:
            // 0.35× enemyAtk instead of 0.18×.
            // Total raw: ~1.75× a normal hit — meaningful but not oppressive.
            // The 5-tick cap also keeps the real-time wait reasonable.
            // Block beforehand and the whole move is negated (matching how
            // blocking works for every other boss).
            default -> {
                int tickDmg   = Math.max(2, (int)(enemyAtk * 0.35));  // ~4–5 dmg/tick at typical ATK
                int tickCount = 5;

                turnLocked = true;
                setButtonsEnabled(false);

                int[]     tick     = {0};
                Timer[]   timerRef = {null};

                timerRef[0] = new Timer(1000, e -> {
                    tick[0]++;
                    if (!active || ending) { timerRef[0].stop(); return; }

                    int dmg = applyDamageToPlayer(tickDmg);
                    appendLog("  ☁ Cloudbomb tick " + tick[0] + "/" + tickCount
                            + ": suffocated for " + dmg + " damage!");
                    updateHpBar();

                    if (hp <= 0) {
                        timerRef[0].stop();
                        defeat();
                        return;
                    }

                    if (tick[0] >= tickCount) {
                        timerRef[0].stop();
                        appendLog("☁ The suffocating clouds finally dissipate...");
                        updateHpBar();
                        updateEnemyHpBar();
                        if (skipNextTurn) {
                            skipNextTurn = false;
                            appendLog("You're still recovering from your block... your turn is skipped.");
                            scheduleEnemyTurn(600);
                        } else {
                            turnLocked = false;
                            setButtonsEnabled(true);
                        }
                    }
                });
                timerRef[0].start();

                yield "☁ Bin Izharfed releases dense Cloudbombs! Suffocating clouds engulf you for 5 seconds...";
            }
        };
    }

    private String handleDonMalekAttack() {
        StringBuilder log = new StringBuilder();
        if (!malekFinalSwigUsed && enemyHp < enemyMaxHp / 2) {
            malekFinalSwigUsed = true;
            int heal = Math.max(1, (int)(enemyMaxHp * 0.15));
            enemyHp  = Math.min(enemyHp + heal, enemyMaxHp);
            enemyDef += 6;
            log.append("'One last swig for the road...'")
               .append("\n  Healed ").append(heal)
               .append(" HP, DEF +6 (now ").append(enemyDef).append(")! (Final Swig)\n");
        }
        int raw = enemyDamage(enemyAtk);
        int dmg = applyDamageToPlayer(raw);
        log.append(boss).append(" deals ").append(dmg).append(" damage!");
        return log.toString();
    }

    private String defaultEnemyAttack() {
        int raw = enemyDamage(enemyAtk);
        int dmg = applyDamageToPlayer(raw);
        return boss + " attacks for " + dmg + " damage!";
    }

    // =========================================================================
    // Passive registration
    // =========================================================================
    private void registerPassives(String enemy) {
        enemyPassives.clear();
        switch (enemy.toLowerCase()) {
            case "ivy"          -> enemyPassives.add(new IvyPoisonPassive());
            case "ryan"         -> {
                enemyPassives.add(new RyanPureHatredPassive());
                enemyPassives.add(new RyanLastStandPassive());
            }
            case "bin izharfed" -> enemyPassives.add(new IzharfedIllusionPassive());
        }
    }

    // =========================================================================
    // start()
    // =========================================================================
    File statsFile = new File("docs/battleStats.txt");

    public void start(JFrame parentFrame, String background, String boss) {
        if (active) return;
        active  = true;
        ending  = false;
        paused  = true;
        this.boss = boss;
        frame     = parentFrame;

        backgroundImage = new ImageIcon(background);
        playerBattle    = new ImageIcon("images/battlePlayer.png");
        enemyBattle     = new ImageIcon("images/" + boss + ".png");

        loadBossStats(boss);
        registerPassives(boss);
        initBossState(boss);

        buildOverlay();
        buildHUD();
        frame.requestFocusInWindow();

        appendLog(boss + " stands in the way!");
        playSound("music/" + boss + "-boss.wav");
    }

    private void initBossState(String enemy) {
        if (enemy.equalsIgnoreCase("ryan")) {
            ryanHpMilestone = enemyMaxHp / 10;
        }
    }

    // =========================================================================
    // TXT file loading
    // =========================================================================
    private void loadBossStats(String enemy) {
        if (boss.equals("Jetroids")) enemyBattle = new ImageIcon("images/" + boss + ".gif");
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(statsFile), "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.replace("\uFEFF", "").trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("\\|");
                if (parts.length < 6) continue;
                if (!parts[0].trim().equalsIgnoreCase(enemy)) continue;

                for (String seg : parts[1].replace("STATS:", "").trim().split(",")) {
                    seg = seg.trim();
                    if      (seg.startsWith("HP:"))  { enemyMaxHp = safeInt(seg, "HP:",  100); enemyHp = enemyMaxHp; }
                    else if (seg.startsWith("ATK:")) { enemyAtk   = safeInt(seg, "ATK:", 10); }
                    else if (seg.startsWith("DEF:")) { enemyDef   = safeInt(seg, "DEF:", 0); }
                }

                flavor    = injectName(parts[2].replace("FLAVOR:",     "").trim(), enemy);
                flavorLow = injectName(parts[3].replace("FLAVOR_LOW:", "").trim(), enemy);
                passive   = injectName(parts[4].replace("PASSIVE:",    "").trim(), enemy);
                check     = injectName(parts[5].replace("CHECK:",      "").trim(), enemy);

                System.out.println("Loaded " + enemy
                        + "  HP:" + enemyMaxHp + "  ATK:" + enemyAtk + "  DEF:" + enemyDef);
                return;
            }
            System.out.println("No stats found for: " + enemy);
        } catch (IOException ex) {
            System.out.println("Error reading stats: " + ex.getMessage());
        }
    }

    private String injectName(String text, String name) {
        return (text == null) ? "" : text.replace("[Enemy]", name);
    }

    private String resolveFlavor(String text) {
        return (text == null) ? "" : text.replace("Cel(ene/ora)", celeneState);
    }

    private int safeInt(String segment, String prefix, int fallback) {
        try { return Integer.parseInt(segment.replace(prefix, "").trim()); }
        catch (NumberFormatException e) { return fallback; }
    }

    // =========================================================================
    // Overlay
    // =========================================================================
    private void buildOverlay() {
        int cw = frame.getContentPane().getWidth();
        int ch = frame.getContentPane().getHeight();

        overlay = new JLayeredPane() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                int battleH = 7 * getHeight() / 11;
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), battleH);

                if (backgroundImage != null && backgroundImage.getIconWidth() > 0)
                    g.drawImage(backgroundImage.getImage(), 0, 0, getWidth(), battleH, this);

                if (playerBattle != null && playerBattle.getIconWidth() > 0)
                    g.drawImage(playerBattle.getImage(), getWidth() / 8, 240, getWidth() / 8, 90, this);

                if (enemyBattle != null && enemyBattle.getIconWidth() > 0) {
                    int ew = getWidth() / 4;       // was getWidth()/8  — now 2× wider
                    int eh = ew;                   // square sprite keeps correct aspect ratio
                    int ex = getWidth() * 5 / 8;  // shifted left so it stays on screen
                    int ey = (battleH - eh) / 2;  // vertically centred in the battle area
                    g.drawImage(enemyBattle.getImage(), ex, ey, ew, eh, this);
                } else {
                    int ew = getWidth() / 4;
                    int eh = ew;
                    int ex = getWidth() * 5 / 8;
                    int ey = (battleH - eh) / 2;
                    g.setColor(new Color(200, 40, 40, 180));
                    g.fillRect(ex, ey, ew, eh);
                    g.setColor(Color.WHITE);
                    g.drawString("?", ex + ew / 2 - 4, ey + eh / 2 + 6);
                }
            }
        };

        overlay.setOpaque(true);
        overlay.setBackground(new Color(100, 100, 100));
        overlay.setBounds(0, 0, cw, ch);
        overlay.setLayout(null);
        frame.getLayeredPane().add(overlay, Integer.valueOf(JLayeredPane.MODAL_LAYER));
    }

    // =========================================================================
    // HUD
    // =========================================================================
    private void buildHUD() {
        int fw = frame.getWidth();
        int fh = frame.getHeight();

        int hudY = (int)(fh * 0.60);
        int hudH = fh - hudY;
        int hudW = fw;

        hudPanel = new JPanel(null);
        hudPanel.setBackground(new Color(20, 20, 20));
        hudPanel.setBounds(0, hudY, hudW, hudH);
        hudPanel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));

        int barRowY = 10;
        int barW    = fw / 4;
        int barH    = 28;

        JPanel playerBarPanel = buildHpBarPanel(barW, false);
        playerBarPanel.setBounds(fw / 16, barRowY, barW, barH);
        hpBarFill = (JPanel)((JPanel) playerBarPanel.getComponent(0)).getComponent(0);
        hpLabel   = (JLabel) playerBarPanel.getComponent(1);
        hpLabel.setText("HP: " + hp + "/" + maxHp);
        updateSingleBar(hpBarFill, hp, maxHp, false);
        hudPanel.add(playerBarPanel);

        JPanel enemyBarPanel = buildHpBarPanel(barW, true);
        enemyBarPanel.setBounds(fw - fw / 16 - barW, barRowY, barW, barH);
        enemyHpBarFill = (JPanel)((JPanel) enemyBarPanel.getComponent(0)).getComponent(0);
        enemyHpLabel   = (JLabel) enemyBarPanel.getComponent(1);
        enemyHpLabel.setText(boss + " HP: " + enemyHp + "/" + enemyMaxHp);
        updateSingleBar(enemyHpBarFill, enemyHp, enemyMaxHp, true);
        hudPanel.add(enemyBarPanel);

        int btnAreaH = Math.max(36, (int)(hudH * 0.28));
        int logY     = barRowY + barH + 6;
        int logH     = hudH - logY - btnAreaH - 14;

        battleLog = new JTextArea();
        battleLog.setForeground(Color.WHITE);
        battleLog.setBackground(new Color(10, 10, 10));
        battleLog.setFont(new Font("Comic Sans MS", Font.PLAIN, 14));
        battleLog.setLineWrap(true);
        battleLog.setWrapStyleWord(true);
        battleLog.setEditable(false);
        battleLog.setFocusable(false);

        logScroll = new JScrollPane(battleLog,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        logScroll.setBounds(10, logY, hudW - 20, logH);
        logScroll.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60)));
        logScroll.getViewport().setBackground(new Color(10, 10, 10));
        logScroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        logScroll.getVerticalScrollBar().setOpaque(false);
        hudPanel.add(logScroll);

        int btnY   = logY + logH + 8;
        int btnH   = hudH - btnY - 8;
        int btnW   = (int)(hudW * 0.18);
        int gap    = (hudW - 4 * btnW) / 5;
        int startX = gap;

        fightButton = makeButton("FIGHT", startX,                    btnY, btnW, btnH, new Color(160, 30,  30));
        checkButton = makeButton("CHECK", startX + (gap + btnW),     btnY, btnW, btnH, new Color(30,  80,  160));
        blockButton = makeButton("BLOCK", startX + 2 * (gap + btnW), btnY, btnW, btnH, new Color(30,  130, 60));
        healButton  = makeButton("HEAL",  startX + 3 * (gap + btnW), btnY, btnW, btnH, new Color(150, 110, 0));

        ActionListener al = this::handleButtonPress;
        fightButton.addActionListener(al);
        checkButton.addActionListener(al);
        blockButton.addActionListener(al);
        healButton.addActionListener(al);

        hudPanel.add(fightButton);
        hudPanel.add(checkButton);
        hudPanel.add(blockButton);
        hudPanel.add(healButton);

        frame.getLayeredPane().add(hudPanel, Integer.valueOf(JLayeredPane.POPUP_LAYER));
        frame.getLayeredPane().revalidate();
        frame.getLayeredPane().repaint();
    }

    // =========================================================================
    // Button handler
    // =========================================================================
    private void handleButtonPress(ActionEvent e) {
        if (turnLocked || ending) return;
        switch (e.getActionCommand()) {
            case "FIGHT" -> handleFight();
            case "CHECK" -> handleCheck();
            case "BLOCK" -> handleBlock();
            case "HEAL"  -> handleHeal();
        }
    }

    private void handleFight() {
        lastActionWasBlock = false;
        int base   = playerAtk + RNG.nextInt(6);
        int damage = playerDamageCalc(base);

        lastAttackNegated = false;
        for (EnemyPassive p : enemyPassives)
            damage = p.onPlayerAttack(this, damage);

        final boolean negated  = lastAttackNegated;
        final int     finalDmg = negated ? 0 : damage;

        if (!negated) enemyHp = Math.max(0, enemyHp - finalDmg);

        StringBuilder log = new StringBuilder();
        if (negated) {
            log.append("Your attack phases through an illusion — no damage dealt!");
        } else {
            log.append("You attacked ").append(boss).append(" for ").append(finalDmg).append(" damage!");
            playSFX("sfx/attack.wav");
        }

        for (EnemyPassive p : enemyPassives) {
            String msg = p.onAfterPlayerAttack(this);
            if (!msg.isEmpty()) log.append("\n").append(msg);
        }

        typewriterLog(log.toString(), () -> {
            updateEnemyHpBar();
            if (enemyHp <= 0) { victory(); return; }
            scheduleEnemyTurn(300);
        });
    }

    private void handleCheck() {
        String passiveText = (passive != null && !passive.isEmpty()) ? passive : "—";
        String checkText   = (check   != null && !check.isEmpty())   ? check   : "—";
        String info =
                "Jardenito's Check...\n\n"
                + boss + "  |  HP: " + enemyHp + "/" + enemyMaxHp
                + "   ATK: " + enemyAtk + "   DEF: " + enemyDef + "\n"
                + "PASSIVE: " + passiveText + "\n"
                + checkText;
        typewriterLog(info, null);
        playSFX("sfx/button.wav");
    }

    private void handleBlock() {
        if (lastActionWasBlock) {
            typewriterLog("You cannot block twice in a row!", null);
            return;
        }
        blocking           = true;
        //skipNextTurn       = true;
        lastActionWasBlock = true;
        typewriterLog("You brace for the next attack!", () ->
                scheduleEnemyTurn(300));
        playSFX("sfx/block.wav");
    }

    private void handleHeal() {
        if (healsUsed >= MAX_HEALS) {
            typewriterLog("No heals remaining! (0/" + MAX_HEALS + " left)", null);
            return;
        }
        healsUsed++;
        lastActionWasBlock = false;
        int healsLeft = MAX_HEALS - healsUsed;
        int heal = 20 + RNG.nextInt(11);
        hp = Math.min(hp + heal, maxHp);
        typewriterLog("You healed " + heal + " HP!  (" + hp + "/" + maxHp + ")  ["
                + healsLeft + "/" + MAX_HEALS + " heals left]", () -> {
            updateHpBar();
            scheduleEnemyTurn(300);
        });
        playSFX("sfx/heal.wav");
    }

    // =========================================================================
    // Enemy turn
    // =========================================================================
    private void scheduleEnemyTurn(int delayMs) {
        if (ending) return;
        if (enemyTurnTimer != null && enemyTurnTimer.isRunning()) {
            enemyTurnTimer.stop();
        }
        turnLocked = true;
        setButtonsEnabled(false);
        enemyTurnTimer = new Timer(delayMs, e -> runEnemyTurn());
        enemyTurnTimer.setRepeats(false);
        enemyTurnTimer.start();
    }

    private void runEnemyTurn() {
        if (!active || ending) return;

        StringBuilder log = new StringBuilder();

        // Before-attack passives (e.g. poison tick — each tick prompts its own
        // math challenge inside IvyPoisonPassive.onBeforeEnemyAttack)
        for (EnemyPassive p : enemyPassives) {
            String msg = p.onBeforeEnemyAttack(this);
            if (!msg.isEmpty()) log.append(msg).append("\n");
        }

        if (hp <= 0) {
            String preLog = log.toString().trim();
            if (!preLog.isEmpty()) {
                typewriterLog(preLog, () -> defeat());
            } else {
                defeat();
            }
            return;
        }

        // Main attack — applyDamageToPlayer() inside each handler opens the dialog
        log.append(resolveEnemyAttack());

        // After-attack passives
        for (EnemyPassive p : enemyPassives) {
            String msg = p.onAfterEnemyAttack(this);
            if (!msg.isEmpty()) log.append("\n").append(msg);
        }

        String ft = resolveFlavor(enemyHp < enemyMaxHp / 2 ? flavorLow : flavor);
        if (ft != null && !ft.isEmpty()) log.append("\n").append(ft);

        typewriterLog(log.toString(), () -> {
            updateHpBar();
            updateEnemyHpBar();
            if (hp <= 0) {
                defeat();
            } else if (skipNextTurn) {
                skipNextTurn = false;
                appendLog("You're still recovering from your block... your turn is skipped.");
                scheduleEnemyTurn(600);
            } else {
                turnLocked = false;
                setButtonsEnabled(true);
            }
        });
    }

    private String resolveEnemyAttack() {
        if (blocking) {
            blocking = false;
            return boss + " has their attack fully blocked!";
        }
        return switch (boss.toLowerCase()) {
            case "celene"        -> handleCeleneAttack();
            case "jetroids"      -> handleJetroidsAttack();
            case "jarrelle"      -> handleJarrelleAttack();
            case "main"          -> handleMainAttack();
            case "rainity"       -> handleRainityAttack();
            case "gigglebot3000" -> handleGigglebotAttack();
            case "bin izharfed"  -> handleBinIzharfedAttack();
            case "don malek"     -> handleDonMalekAttack();
            default              -> defaultEnemyAttack();
        };
    }

    // =========================================================================
    // Victory / Defeat
    // =========================================================================
    private void victory() {
        if (ending) return;
        ending = true;
        disableButtons();
        typewriterLog("You did it! We saved " + boss + " for real!                                             ", () -> {
            Timer delay = new Timer(1800, e -> end());
            delay.setRepeats(false);
            delay.start();
        });
    }

    private void defeat() {
        if (ending) return;
        ending = true;
        disableButtons();
        typewriterLog("DON'T DIE, MY FRIEND! NOOO!!", () -> {
            Timer delay = new Timer(1800, e -> end());
            delay.setRepeats(false);
            delay.start();
        });
    }

    // =========================================================================
    // end()
    // =========================================================================
    public void end() {
        if (!active) return;
        active = false;
        ending = true;

        stopAllAudio();   // ← ADD THIS — stops BGM before cleanup

        if (enemyTurnTimer != null && enemyTurnTimer.isRunning()) {
            enemyTurnTimer.stop();
            enemyTurnTimer = null;
        }

        JLayeredPane lp = frame.getLayeredPane();
        if (overlay  != null) { lp.remove(overlay);  overlay  = null; }
        if (hudPanel != null) { lp.remove(hudPanel); hudPanel = null; }

        hpBarFill      = null;
        hpLabel        = null;
        enemyHpBarFill = null;
        enemyHpLabel   = null;
        battleLog      = null;
        logScroll      = null;
        fightButton    = null;
        checkButton    = null;
        blockButton    = null;
        healButton     = null;

        lp.revalidate();
        lp.repaint();

        paused = false;

        System.out.println("[Battle] Cleaned up. Player can move again.");
    }

    // =========================================================================
    // HP bar helpers
    // =========================================================================
    private JPanel buildHpBarPanel(int barW, boolean alwaysRed) {
        JPanel panel = new JPanel(null);
        panel.setOpaque(false);

        JPanel track = new JPanel(null);
        track.setBackground(new Color(60, 60, 60));
        track.setBounds(0, 16, barW, 12);
        panel.add(track);

        JPanel fill = new JPanel();
        fill.setBackground(alwaysRed ? new Color(200, 40, 40) : new Color(50, 200, 50));
        fill.setBounds(0, 0, barW, 12);
        track.add(fill);

        JLabel label = new JLabel("HP: --/--");
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 13));
        label.setBounds(0, 0, barW, 14);
        panel.add(label);

        return panel;
    }

    private void updateHpBar() {
        updateSingleBar(hpBarFill, hp, maxHp, false);
        if (hpLabel != null) hpLabel.setText("HP: " + hp + "/" + maxHp);
        repaintBar(hpBarFill);
    }

    private void updateEnemyHpBar() {
        updateSingleBar(enemyHpBarFill, enemyHp, enemyMaxHp, true);
        if (enemyHpLabel != null) enemyHpLabel.setText(boss + " HP: " + enemyHp + "/" + enemyMaxHp);
        repaintBar(enemyHpBarFill);
    }

    private void updateSingleBar(JPanel fill, int current, int max, boolean alwaysRed) {
        if (fill == null || fill.getParent() == null) return;
        int trackW = fill.getParent().getWidth();
        int w      = (max > 0) ? (int)((double) current / max * trackW) : 0;
        fill.setBounds(0, 0, Math.max(w, 0), fill.getHeight());
        if (alwaysRed) {
            fill.setBackground(new Color(200, 40, 40));
        } else {
            float ratio = (max > 0) ? (float) current / max : 0f;
            if      (ratio > 0.50f) fill.setBackground(new Color(50,  200, 50));
            else if (ratio > 0.25f) fill.setBackground(new Color(220, 180, 0));
            else                    fill.setBackground(new Color(200, 40,  40));
        }
    }

    private void repaintBar(JPanel fill) {
        if (fill != null && fill.getParent() != null) {
            fill.getParent().revalidate();
            fill.getParent().repaint();
        }
    }

    // =========================================================================
    // Typewriter log
    // =========================================================================
    private void typewriterLog(String message, Runnable onComplete) {
        turnLocked = true;
        setButtonsEnabled(false);
        appendLogRaw("\n");

        char[] chars = message.toCharArray();
        int[]  idx   = {0};
        Timer  tw    = new Timer(18, null);
        tw.addActionListener(e -> {
            if (battleLog == null) { tw.stop(); return; }
            if (idx[0] < chars.length) {
                appendLogRaw(String.valueOf(chars[idx[0]++]));
            } else {
                tw.stop();
                appendLogRaw("\n");
                if (onComplete != null) {
                    onComplete.run();
                } else {
                    if (!ending) {
                        turnLocked = false;
                        setButtonsEnabled(true);
                    }
                }
            }
        });
        tw.start();
    }

    private void appendLogRaw(String text) {
        if (battleLog == null) return;
        battleLog.append(text);
        battleLog.setCaretPosition(battleLog.getDocument().getLength());
    }

    private void appendLog(String line) {
        if (battleLog == null) return;
        battleLog.append(line + "\n");
        battleLog.setCaretPosition(battleLog.getDocument().getLength());
    }

    // =========================================================================
    // Button helpers
    // =========================================================================
    private JButton makeButton(String label, int x, int y, int w, int h, Color bg) {
        JButton btn = new JButton(label);
        btn.setBounds(x, y, w, h);
        btn.setFocusPainted(false);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 16));
        btn.setActionCommand(label);
        btn.setFocusable(false);
        return btn;
    }

    private void setButtonsEnabled(boolean on) {
        for (JButton b : new JButton[]{fightButton, checkButton, blockButton, healButton})
            if (b != null) b.setEnabled(on);
    }

    private void disableButtons() {
        setButtonsEnabled(false);
        for (JButton b : new JButton[]{fightButton, checkButton, blockButton, healButton})
            if (b != null) b.setBackground(new Color(50, 50, 50));
    }
    
    // =========================================================================
    // Music — filesystem-based loading, looping, with volume control.
    // Stores the Clip as a field so it can be stopped cleanly in end().
    // =========================================================================
    public void playSound(String filePath) {
        stopSound(); // Stop any currently playing BGM before starting a new one.

        File audioFile = new File(filePath);

        if (!audioFile.exists()) {
            System.out.println("[Battle] Audio file not found: "
                    + audioFile.getAbsolutePath());
            return;
        }

        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            bgmClip = AudioSystem.getClip();
            bgmClip.open(audioStream);

            setVolume(bgmClip, 0.85f); // 85% volume → ~-1.41 dB

            bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
            bgmClip.start();
            System.out.println("[Battle] Now playing: " + filePath);

        } catch (UnsupportedAudioFileException e) {
            System.out.println("[Battle] Unsupported audio format: " + filePath);
        } catch (LineUnavailableException e) {
            System.out.println("[Battle] Audio line unavailable: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("[Battle] I/O error reading audio: " + e.getMessage());
        }
    }

    // =========================================================================
    // Plays a one-shot sound effect independently from the background music.
    //
    // Each call opens a brand-new Clip on its own audio line, so:
    //   - Multiple SFX can overlap freely (rapid attacks, hits, etc.)
    //   - The BGM Clip is never touched or interrupted
    //   - The SFX Clip closes and releases itself via LineListener when done
    //
    // linearGain follows the same 0.0–1.0 scale as setVolume():
    //   1.0f  = full volume   (same level as BGM master)
    //   1.2f  = slightly louder than BGM (use sparingly — may clip on some systems)
    //   0.75f = slightly quieter
    // =========================================================================
    public void playSFX(String filePath, float linearGain) {
        File audioFile = new File(filePath);

        if (!audioFile.exists()) {
            System.out.println("[SFX] File not found: " + audioFile.getAbsolutePath());
            return;
        }

        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);

            // Each SFX gets its own Clip — completely separate from bgmClip.
            // AudioSystem.getClip() allocates a new audio line every call.
            Clip sfxClip = AudioSystem.getClip();
            sfxClip.open(audioStream);

            setVolume(sfxClip, linearGain);

            // Register the clip before starting so the listener never races ahead
            // of the add() call on extremely short audio files.
            activeSFX.add(sfxClip);

            // LineListener fires on the audio thread when playback reaches STOP
            // (either natural end or manual stop). This is the correct place to
            // release resources — do NOT call close() from the EDT here.
            sfxClip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    activeSFX.remove(sfxClip);
                    sfxClip.close(); // Releases the audio line back to the system
                }
            });

            sfxClip.start(); // Fire-and-forget — the listener handles cleanup

        } catch (UnsupportedAudioFileException e) {
            System.out.println("[SFX] Unsupported format: " + filePath);
        } catch (LineUnavailableException e) {
            // Happens if the system has hit its audio line limit (too many open clips).
            System.out.println("[SFX] No audio line available: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("[SFX] I/O error: " + e.getMessage());
        }
    }

    // =========================================================================
    // Convenience overload — plays SFX at full volume without specifying gain.
    // Use this when you don't need per-effect volume tuning.
    // =========================================================================
    public void playSFX(String filePath) {
        playSFX(filePath, 0.9f);
    }

    // =========================================================================
    // Converts a linear gain (0.0 = silent, 1.0 = full) to decibels and
    // applies it to the given Clip via MASTER_GAIN FloatControl.
    //
    // Formula: dB = 20 * log10(linearGain)
    //   0.85f → ~-1.41 dB  (subtle reduction, still very audible)
    //   0.50f → ~-6.02 dB  (noticeably quieter)
    //
    // Falls back silently if the audio system doesn't support gain control
    // (e.g. some Linux drivers or virtual audio devices).
    // =========================================================================
    private void setVolume(Clip clip, float linearGain) {
        // Guard: clamp input to a valid range to avoid log10(0) = -Infinity
        // and log10(negative) = NaN, both of which would corrupt the control.
        if (linearGain <= 0f) {
            linearGain = 0.0001f; // Effectively silent but avoids -Infinity dB
        } else if (linearGain > 1f) {
            linearGain = 1f;      // Clamp to full volume — don't allow clipping
        }

        if (!clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            // Not an error — some audio systems simply don't expose this control.
            // Music will play at default volume rather than crashing or going silent.
            System.out.println("[Battle] Volume control not supported on this system — playing at default volume.");
            return;
        }

        FloatControl gainControl =
                (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

        // Convert linear gain to decibels.
        float dB = 20f * (float) Math.log10(linearGain);

        // Clamp to the range the hardware actually supports.
        // Typical range is [-80.0, 6.0] dB but varies by system.
        float min = gainControl.getMinimum(); // e.g. -80.0f
        float max = gainControl.getMaximum(); // e.g.   6.0f
        float clamped = Math.max(min, Math.min(max, dB));

        gainControl.setValue(clamped);
        System.out.printf("[Battle] Volume set to %.0f%%%% (%.2f dB)%n",
                linearGain * 100, clamped);
    }

    // =========================================================================
    // Stops and releases all active SFX clips immediately.
    // Safe to call at any time — skips clips that have already self-closed.
    // =========================================================================
    public void stopAllSFX() {
        for (Clip sfx : activeSFX) {       // CopyOnWriteArrayList — safe to iterate
            if (sfx.isOpen()) {
                if (sfx.isRunning()) sfx.stop(); // Triggers the LineListener → close()
            }
        }
        activeSFX.clear(); // Belt-and-suspenders clear in case any listeners lag
    }

    // =========================================================================
    // Stops BGM. Unchanged except for the added stopAllSFX() call.
    // =========================================================================
    public void stopSound() {
        if (bgmClip != null) {
            if (bgmClip.isRunning()) bgmClip.stop();
            bgmClip.close();
            bgmClip = null;
        }
    }

    // =========================================================================
    // Full audio teardown — call this from end() to clean up everything.
    // =========================================================================
    public void stopAllAudio() {
        stopSound();    // BGM
        stopAllSFX();   // Any SFX still playing (e.g. attack sound mid-animation)
    }
}