package codes;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * ChallengeDialog — the shared modal dialog engine that powers every boss challenge.
 *
 * This class only handles the UI: showing a question, collecting an answer,
 * running a countdown, and returning whether the player was correct.
 *
 * It knows nothing about which boss is fighting or how questions are generated.
 * All of that lives in the Question record and the BossChallenge router.
 *
 * ── HOW IT WORKS ─────────────────────────────────────────────────────────────
 *   ChallengeDialog.show(parent, question, rawDamage)
 *   → Opens a modal JDialog (freezes all Swing input automatically)
 *   → Returns the final damage to apply (reduced if correct, full if wrong)
 */
public class ChallengeDialog {

    // ── Global tuning ─────────────────────────────────────────────────────────
    /** Percentage of incoming damage blocked on a correct answer (0–100). */
    public static final int DAMAGE_REDUCTION_PERCENT = 60;

    /** Seconds the player has to answer. Set to 0 to disable the timer. */
    public static final int TIME_LIMIT_SECONDS = 60;

    // ── Palette ───────────────────────────────────────────────────────────────
    static final Color BG_DARK   = new Color(15,  12,  30);
    static final Color BG_PANEL  = new Color(28,  22,  55);
    static final Color ACCENT    = new Color(220, 80,  120);
    static final Color ACCENT2   = new Color(100, 180, 255);
    static final Color TEXT_MAIN = new Color(240, 230, 255);
    static final Color TEXT_DIM  = new Color(160, 145, 200);
    static final Color OK_COLOR  = new Color(60,  210, 130);
    static final Color BAD_COLOR = new Color(230, 70,  70);

    // ── Fonts ─────────────────────────────────────────────────────────────────
    static final Font F_TITLE = new Font("Georgia",     Font.BOLD,   18);
    static final Font F_MATH  = new Font("Courier New", Font.BOLD,   20);
    static final Font F_BODY  = new Font("Courier New", Font.PLAIN,  14);
    static final Font F_HINT  = new Font("Georgia",     Font.ITALIC, 12);
    static final Font F_BTN   = new Font("Georgia",     Font.BOLD,   13);

    // =========================================================================
    // Question record — passed in by the boss-specific question pool
    // =========================================================================

    /**
     * Encapsulates everything the dialog needs to know about one question.
     *
     * @param questionText   The text shown to the player (HTML allowed).
     * @param hintText       A small sub-label below the question (e.g. "Find the value of x").
     * @param correctAnswer  The answer string the player must match (case-insensitive, trimmed).
     * @param isNumeric      If true, the dialog parses the input as a number for comparison.
     *                       If false, it does a case-insensitive string match.
     * @param subjectTag     Short label shown in the title bar (e.g. "ALGEBRA", "CHEMISTRY").
     * @param revealAnswer   The human-readable correct answer shown on wrong/timeout
     *                       (can differ from correctAnswer for display formatting).
     */
    public record Question(
            String  questionText,
            String  hintText,
            String  correctAnswer,
            boolean isNumeric,
            String  subjectTag,
            String  revealAnswer
    ) {}

    // =========================================================================
    // PUBLIC API
    // =========================================================================

    /**
     * Shows the challenge dialog and returns the final damage to deal.
     *
     * @param parent    Parent JFrame — used to centre the dialog.
     * @param q         The question to show.
     * @param rawDamage The damage the enemy intended to deal.
     * @return          Reduced damage on correct answer; full damage otherwise.
     */
    public static int show(JFrame parent, Question q, int rawDamage) {
        boolean[] correct = {false};
        int reduced = Math.max(1, rawDamage * (100 - DAMAGE_REDUCTION_PERCENT) / 100);

        // ── Dialog shell ──────────────────────────────────────────────────────
        JDialog dlg = new JDialog(parent, "Incoming Attack!", true);
        dlg.setUndecorated(true);

        // ── Root (rounded dark panel) ─────────────────────────────────────────
        JPanel root = new JPanel(new BorderLayout(0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_DARK);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.dispose();
            }
        };
        root.setOpaque(false);
        root.setBorder(new LineBorder(ACCENT, 2, true));

        // ── Top bar ───────────────────────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createEmptyBorder(12, 18, 6, 18));

        JLabel titleLbl = new JLabel("INCOMING ATTACK, " + q.subjectTag());
        titleLbl.setFont(F_TITLE);
        titleLbl.setForeground(ACCENT);
        topBar.add(titleLbl, BorderLayout.WEST);

        JLabel timerLbl = new JLabel(TIME_LIMIT_SECONDS > 0 ? "" + TIME_LIMIT_SECONDS + "s" : "");
        timerLbl.setFont(F_BODY);
        timerLbl.setForeground(ACCENT2);
        topBar.add(timerLbl, BorderLayout.EAST);
        root.add(topBar, BorderLayout.NORTH);

        // ── Card ──────────────────────────────────────────────────────────────
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_PANEL);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(14, 24, 14, 24));

        // Flavour line
        addCentered(card, label(
                "<html><center>Answer correctly to reduce incoming damage!</center></html>",
                F_HINT, TEXT_DIM));
        card.add(Box.createVerticalStrut(10));
        card.add(separator());
        card.add(Box.createVerticalStrut(12));

        // Question text (supports multi-line via HTML)
        JLabel questionLbl = new JLabel("<html><center>" + q.questionText() + "</center></html>");
        questionLbl.setFont(F_MATH);
        questionLbl.setForeground(TEXT_MAIN);
        questionLbl.setHorizontalAlignment(SwingConstants.CENTER);
        questionLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(questionLbl);
        card.add(Box.createVerticalStrut(6));

        // Hint sub-label
        if (q.hintText() != null && !q.hintText().isEmpty()) {
            addCentered(card, label(q.hintText(), F_HINT, TEXT_DIM));
        }
        card.add(Box.createVerticalStrut(12));

        // Answer field
        JTextField answerField = new JTextField(14);
        answerField.setFont(F_MATH);
        answerField.setHorizontalAlignment(JTextField.CENTER);
        answerField.setBackground(BG_DARK);
        answerField.setForeground(TEXT_MAIN);
        answerField.setCaretColor(ACCENT2);
        answerField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(ACCENT2, 1, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        answerField.setMaximumSize(new Dimension(280, 44));
        answerField.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(answerField);
        card.add(Box.createVerticalStrut(10));

        // Feedback line
        JLabel feedback = label(" ", F_BODY, TEXT_DIM);
        addCentered(card, feedback);
        card.add(Box.createVerticalStrut(8));

        // Damage preview
        addCentered(card, label(
                "<html><center>"
                + "<span style='color:#E04646;'>Wrong: <b>" + rawDamage + " HP</b></span>"
                + " &nbsp;&nbsp;|&nbsp;&nbsp; "
                + "<span style='color:#3CD282;'>Correct: <b>" + reduced + " HP</b></span>"
                + "</center></html>",
                F_HINT, TEXT_DIM));

        JPanel cardWrap = new JPanel(new BorderLayout());
        cardWrap.setOpaque(false);
        cardWrap.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
        cardWrap.add(card, BorderLayout.CENTER);
        root.add(cardWrap, BorderLayout.CENTER);

        // ── Submit button ─────────────────────────────────────────────────────
        JButton submitBtn = new JButton("SUBMIT ANSWER");
        submitBtn.setFont(F_BTN);
        submitBtn.setForeground(TEXT_MAIN);
        submitBtn.setBackground(ACCENT);
        submitBtn.setFocusPainted(false);
        submitBtn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(ACCENT.brighter(), 1, true),
                BorderFactory.createEmptyBorder(7, 18, 7, 18)));
        submitBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel botBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        botBar.setOpaque(false);
        botBar.add(submitBtn);
        root.add(botBar, BorderLayout.SOUTH);

        dlg.setContentPane(root);
        dlg.pack();
        dlg.setMinimumSize(new Dimension(520, dlg.getHeight()));
        dlg.setLocationRelativeTo(parent);

        // ── Answer checking ───────────────────────────────────────────────────
        Timer[] countdownHolder = {null};

        Runnable doSubmit = () -> {
            if (!submitBtn.isEnabled()) return;
            if (countdownHolder[0] != null) countdownHolder[0].stop();
            submitBtn.setEnabled(false);
            answerField.setEnabled(false);

            String playerInput = answerField.getText().trim();
            boolean ok = checkAnswer(playerInput, q);
            correct[0] = ok;

            if (ok) {
                feedback.setText("Correct!  Damage reduced to " + reduced + " HP.");
                feedback.setForeground(OK_COLOR);
                questionLbl.setForeground(OK_COLOR);
            } else {
                feedback.setText("Wrong!  Answer: " + q.revealAnswer() + ". Full " + rawDamage + " HP taken.");
                feedback.setForeground(BAD_COLOR);
                questionLbl.setForeground(BAD_COLOR);
                answerField.setBackground(new Color(50, 15, 15));
            }
            card.revalidate();
            card.repaint();

            Timer close = new Timer(1600, ev -> dlg.dispose());
            close.setRepeats(false);
            close.start();
        };

        submitBtn.addActionListener(e -> doSubmit.run());
        answerField.addActionListener(e -> doSubmit.run());

        // ── Countdown ─────────────────────────────────────────────────────────
        if (TIME_LIMIT_SECONDS > 0) {
            int[] left = {TIME_LIMIT_SECONDS};
            Timer countdown = new Timer(1000, null);
            countdownHolder[0] = countdown;
            countdown.addActionListener(ev -> {
                left[0]--;
                if (left[0] <= 5) timerLbl.setForeground(BAD_COLOR);
                if (left[0] <= 0) {
                    countdown.stop();
                    timerLbl.setText("0s");
                    feedback.setText("Ran out of time...  Answer: " + q.revealAnswer() + ". You take full damage.");
                    feedback.setForeground(BAD_COLOR);
                    submitBtn.setEnabled(false);
                    answerField.setEnabled(false);
                    card.revalidate();
                    card.repaint();
                    Timer close = new Timer(1600, ev2 -> dlg.dispose());
                    close.setRepeats(false);
                    close.start();
                } else {
                    timerLbl.setText("" + left[0] + "s");
                }
            });
            countdown.start();
        }

        dlg.addWindowListener(new WindowAdapter() {
            @Override public void windowOpened(WindowEvent e) { answerField.requestFocusInWindow(); }
        });

        dlg.setVisible(true);   // BLOCKS — modal
        return correct[0] ? reduced : rawDamage;
    }

    // ── Answer matching ───────────────────────────────────────────────────────

    private static boolean checkAnswer(String playerInput, Question q) {
        if (playerInput == null || playerInput.isEmpty()) return false;
        if (q.isNumeric()) {
            try {
                // Accept both integer and decimal answers; compare as doubles
                double player  = Double.parseDouble(playerInput.replace(" ", ""));
                double correct = Double.parseDouble(q.correctAnswer().replace(" ", ""));
                return Math.abs(player - correct) < 0.6;   // ±0.5 tolerance for rounding
            } catch (NumberFormatException e) { return false; }
        } else {
            // String match: case-insensitive, whitespace-normalised
            return playerInput.trim().equalsIgnoreCase(q.correctAnswer().trim());
        }
    }

    // ── Swing helpers ─────────────────────────────────────────────────────────

    static JLabel label(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    static void addCentered(JPanel panel, JLabel lbl) {
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lbl);
    }

    static JSeparator separator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(80, 50, 100));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }
}
