package codes;

import static codes.Menu.stopSound;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * credits.java
 *
 * Minecraft-inspired rolling credits screen for:
 * TOGETHER: Project Guanine
 *
 * Displays the player's total time, fastest record time, and a comment
 * based on total time — all pulled live from SaveSystem.
 *
 * How to run:
 * 1. Save as credits.java
 * 2. Compile: javac credits.java
 * 3. Run: java credits
 *
 * Controls:
 * - ESC: exit
 * - SPACE: speed up credits
 */
public class credits extends JPanel implements ActionListener, KeyListener {

    private final Timer timer;
    private int scrollY;
    private boolean fastScroll = false;

    // Pixel-style fonts using built-in Java font.
    // If you later add a Minecraft-style .ttf, replace these with Font.createFont().
    private final Font titleFont    = new Font("Monospaced", Font.BOLD, 36);
    private final Font monologueFont = new Font("Monospaced", Font.BOLD, 18);
    private final Font headingFont  = new Font("Monospaced", Font.BOLD, 23);
    private final Font leadNameFont = new Font("Monospaced", Font.BOLD, 24);
    private final Font bodyFont     = new Font("Monospaced", Font.BOLD, 17);
    private final Font endingFont   = new Font("Monospaced", Font.BOLD, 20);

    // Not final — built dynamically in the constructor with live time data.
    private String[] credits;
    private static Clip bgmClip = null;

    // ── Time / comment helpers ────────────────────────────────────────────────

    /**
     * Formats a raw second count as HH:MM:SS.
     * Mirrors SaveData.formattedTime() so the credits screen has no
     * dependency on a SaveData instance.
     */
    private static String formatTime(long seconds) {
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        //return String.format("%02d:%02d:%02d", h, m, s);
        return "hours: " + h + " minutes: " + m + " seconds: " + s;
    }

    /**
     * Returns a one-word comment based on total completion time.
     *
     * ≤  5 min  →  "WOW"
     * ≤ 10 min  →  "not bad"
     * ≤ 20 min  →  "ok"
     *  > 20 min →  "BRO"
     */
    private static String timeComment(long seconds) {
        if (seconds <=  300) return "Good time, kid.";
        if (seconds <=  600) return "Not a bad time.";
        if (seconds <= 1200) return "That's fine.";
        return "BRO YOU SLOW AS HEck";
    }

    // ── Constructor ───────────────────────────────────────────────────────────

    public credits() {
        // Pull live values from SaveSystem — populated during the session.
        long totalSecs  = SaveSystem.getTotalSeconds();
        long recordSecs = SaveSystem.getRecordSeconds();

        credits = new String[]{
                "TOGETHER: Project Guanine",
                "",
                "Everyone has been saved from the nites' control.",
                "You have officially... defeated the game, kid.",
                "Not bad.",
                "Your time: "  + formatTime(totalSecs),
                "Best time: "  + formatTime(recordSecs),
                timeComment(totalSecs),
                "",
                "LEAD CODERS",
                "🐐 Lord Jehroi D. Mamalias",
                "👑 Francis Gabriel A. Dangoy",
                "",
                "MAIN CODERS",
                "Ethan Bradly G. Chua",
                "Francis Gabriel A. Dangoy",
                "John Felippe Samonte",
                "Lord Jethroi D. Mamalias",
                "Edward Adam B. Malvas",
                "Maine Azlei M. Estrellan",
                "Destine Ryan J. Salise",
                "James Ryan P. Rios",
                "Heartthea C. Ancla",
                "Donie Pete V. Rasonabe",
                "",
                "DESIGNERS",
                "Ivy Grace S. Sanchez",
                "Ashley Bhel R. Galleto",
                "Fedora Celene B. Maynpoas",
                "Klara Irina R. Mazo",
                "Genesis Anne M. De Guia",
                "Danika Marie S. Panares",
                "Gabrielle Isabel R. Veloso",
                "GN M. Jumalon",
                "Yz M. Sepe",
                "Malcholm Bernard G. Redulla",
                "",
                "HYBRIDS",
                "Anna Mikaela C. Tacder",
                "Jared Linnus P. Cantor",
                "Angelika Margaret T. Dumogho",
                "Jaime Lorenzo G. Asegurado",
                "Ron Jarrel B. Bucas",
                "Christine C. Gomez",
                "Amanda G. Quiocho",
                "Reyan Christian Q. Villaroman",
                "Phoebe Marie A. Badayos",
                "",
                "MUSICIANS",
                "Lord Jethroi D. Mamalias",
                "(shameless plug: https://www.soundcloud.com/upextendedrazor)",
                "Jaime Lorenzo G. Asegurado",
                "",
                "",
                "",
                "Thank you for playing our game!",
                "-Guanine, Bente Otso",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "get outta here the credits are about to loop again (ESC)",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
        };

        setFocusable(true);
        addKeyListener(this);
        timer = new Timer(16, this);
        timer.start();

        scrollY = 720;
    }

    // ── Painting ──────────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        // Pixelated text look
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_OFF);

        drawFunBlockBackground(g2);
        drawSoftOverlay(g2);
        drawCredits(g2);
    }

    private void drawFunBlockBackground(Graphics2D g2) {
        int tile = 32;

        Color colorA = new Color(35, 24, 45);
        Color colorB = new Color(46, 32, 63);
        Color colorC = new Color(28, 55, 60);

        for (int y = 0; y < getHeight(); y += tile) {
            for (int x = 0; x < getWidth(); x += tile) {
                int pattern = (x / tile + y / tile) % 3;

                if (pattern == 0) {
                    g2.setColor(colorA);
                } else if (pattern == 1) {
                    g2.setColor(colorB);
                } else {
                    g2.setColor(colorC);
                }

                g2.fillRect(x, y, tile, tile);

                g2.setColor(new Color(255, 255, 255, 18));
                g2.drawRect(x, y, tile, tile);
            }
        }

        // Fun subtle sparkle blocks
        g2.setColor(new Color(255, 221, 110, 65));
        for (int i = 0; i < 45; i++) {
            int x = (i * 173) % Math.max(getWidth(),  1);
            int y = (i * 97)  % Math.max(getHeight(), 1);
            g2.fillRect(x, y, 4, 4);
        }
    }

    private void drawSoftOverlay(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 95));
        g2.fillRect(0, 0, getWidth(), getHeight());
    }

    private void drawCredits(Graphics2D g2) {
        int centerX = getWidth() / 2;
        int y = scrollY;

        for (String line : credits) {
            applyStyle(g2, line);
            drawCenteredText(g2, line, centerX, y);

            if (line.equals("TOGETHER: Project Guanine")) {
                y += 55;
            } else if (isLeadName(line)) {
                y += 39;
            } else if (isHeading(line)) {
                y += 40;
            } else if (isEnding(line)) {
                y += 35;
            } else {
                y += line.isEmpty() ? 25 : 29;
            }
        }

        // Reset scroll when all lines have scrolled past the top
        if (y < -100) {
            scrollY = getHeight() + 100;
        }
    }

    // ── Style helpers ─────────────────────────────────────────────────────────

    private void applyStyle(Graphics2D g2, String line) {
        if (line.equals("TOGETHER: Project Guanine")) {
            g2.setFont(titleFont);
        } else if (isHeading(line)) {
            g2.setFont(headingFont);
        } else if (isLeadName(line)) {
            g2.setFont(leadNameFont);
        } else if (isEnding(line)) {
            g2.setFont(endingFont);
        } else if (isMonologue(line)) {
            g2.setFont(monologueFont);
        } else {
            g2.setFont(bodyFont);
        }
    }

    private Color getLineColor(String line) {
        if (line.equals("TOGETHER: Project Guanine"))
            return new Color(135, 255, 190);   // mint title

        if (line.equals("LEAD CODERS"))
            return new Color(255, 229, 120);   // gold

        if (line.equals("🐐 Lord Jehroi D. Mamalias"))
            return new Color(170, 235, 255);   // cyan

        if (line.equals("👑 Francis Gabriel A. Dangoy"))
            return new Color(255, 190, 95);    // warm crown orange

        if (isHeading(line))
            return new Color(255, 210, 245);   // pink-lavender headings

        if (isEnding(line))
            return new Color(255, 245, 145);   // bright ending yellow

        if (isMonologue(line))
            return new Color(190, 255, 170);   // soft green monologue

        // Time lines and comment get a distinct warm-white so they stand out
        if (line.startsWith("Your time: ") || line.startsWith("Best time: "))
            return new Color(255, 240, 200);   // warm cream

        // Single-word comment (WOW / not bad / ok / BRO)
        if (isComment(line))
            return new Color(255, 180, 120);   // warm orange accent

        return new Color(220, 235, 255);       // clean light blue-white body
    }

    private boolean isHeading(String line) {
        return line.equals("LEAD CODERS")
                || line.equals("MAIN CODERS")
                || line.equals("DESIGNERS")
                || line.equals("HYBRIDS")
                || line.equals("MUSICIANS");
    }

    private boolean isLeadName(String line) {
        return line.equals("🐐 Lord Jehroi D. Mamalias")
                || line.equals("👑 Francis Gabriel A. Dangoy");
    }

    private boolean isEnding(String line) {
        return line.equals("Thank you for playing our game!")
                || line.equals("-Guanine, Bente Otso");
    }

    private boolean isMonologue(String line) {
        return line.equals("Everyone has been saved from the nites' control.")
                || line.equals("You have officially... defeated the game, kid.")
                || line.equals("Not bad.");
    }

    /**
     * Matches the four possible comment values produced by timeComment().
     * Kept separate from isMonologue() so each can be styled independently.
     */
    private boolean isComment(String line) {
        return line.equals("WOW")
                || line.equals("not bad")
                || line.equals("ok")
                || line.equals("BRO");
    }

    // ── Drawing ───────────────────────────────────────────────────────────────

    private void drawCenteredText(Graphics2D g2, String text, int centerX, int y) {
        FontMetrics fm = g2.getFontMetrics();
        int x = centerX - fm.stringWidth(text) / 2;

        Color mainColor = getLineColor(text);

        // Chunky Minecraft-like shadow
        g2.setColor(new Color(0, 0, 0, 210));
        g2.drawString(text, x + 3, y + 3);

        // Tiny highlight for a cohesive glow effect
        if (!text.isEmpty()) {
            g2.setColor(new Color(255, 255, 255, 55));
            g2.drawString(text, x - 1, y - 1);
        }

        g2.setColor(mainColor);
        g2.drawString(text, x, y);

        // Draw again slightly offset for a blockier look
        g2.drawString(text, x + 1, y);
    }

    // ── Timer / input ─────────────────────────────────────────────────────────

    @Override
    public void actionPerformed(ActionEvent e) {
        scrollY -= fastScroll ? 4 : 1;
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            System.exit(0);
        }
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            fastScroll = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            fastScroll = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    // ── Entry point ───────────────────────────────────────────────────────────

    public static void playSound(String filePath) {
        stopSound();

        File audioFile = new File(filePath);
        if (!audioFile.exists()) {
            System.out.println("Audio file not found: " + audioFile.getAbsolutePath());
            return;
        }

        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            bgmClip = AudioSystem.getClip();
            bgmClip.open(audioStream);
            setVolume(bgmClip, 0.8f);
            bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
            bgmClip.start();
            System.out.println("Now playing: " + filePath);
        } catch (UnsupportedAudioFileException e) {
            System.out.println("Unsupported audio format: " + filePath);
        } catch (LineUnavailableException e) {
            System.out.println("Audio line unavailable: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("I/O error reading audio: " + e.getMessage());
        }
    }
    
    private static void setVolume(Clip clip, float linearGain) {
        if (linearGain <= 0f)     linearGain = 0.0001f;
        else if (linearGain > 1f) linearGain = 1f;

        if (!clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            System.out.println("Volume control not supported — playing at default volume.");
            return;
        }

        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        float dB      = 20f * (float) Math.log10(linearGain);
        float min     = gainControl.getMinimum();
        float max     = gainControl.getMaximum();
        float clamped = Math.max(min, Math.min(max, dB));
        gainControl.setValue(clamped);
        System.out.printf("Volume set to %.0f%% (%.2f dB)%n", linearGain * 100, clamped);
    }

    public static void stopSound() {
        if (bgmClip != null) {
            if (bgmClip.isRunning()) bgmClip.stop();
            bgmClip.close();
            bgmClip = null;
        }
    }
    
    public static void main(String[] args) {
        playSound("music/Credits.wav");
        JFrame frame = new JFrame("TOGETHER: Project Guanine");
        credits creditsScene = new credits();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(660, 660);
        frame.setLocationRelativeTo(null);
        frame.add(creditsScene);
        frame.setVisible(true);
        frame.setResizable(false);
    }
}