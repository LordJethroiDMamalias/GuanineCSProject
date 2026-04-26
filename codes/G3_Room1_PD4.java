package codes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 * MEMBERS: FEDORA MAYNOPAS, GN JUMALON, EDWARD MALVAS
 * MODIFIED: Tile-based movement, Post-quest free roam, and Background Music
 */
public class G3_Room1_PD4 extends JPanel implements ActionListener, KeyListener, MouseListener {

    // --- AUDIO CONTROLS ---
    private static Clip bgmClip = null;

    // ── Grid & Movement Constants ─────────────────────────────────────────
    // Window: 660x660. All world coords scaled from original 800x600 design.
    // scaleX = 660/800 = 0.825,  scaleY = 660/600 = 1.1
    int playerX = 330, playerY = 528;   // was (400, 480)
    final int TILE_SIZE = 40;

    // ── Animated player sprites ───────────────────────────────────────────
    ImageIcon[] pUp    = new ImageIcon[4];
    ImageIcon[] pDown  = new ImageIcon[4];
    ImageIcon[] pLeft  = new ImageIcon[4];
    ImageIcon[] pRight = new ImageIcon[4];
    int walkFrame = 0;
    int direction = 1; // 0=up, 1=down, 2=left, 3=right

    Image bg, npcSprite, niteIcon, light1, light2, light3;

    Timer timer = new Timer(16, this);
    ArrayList<Rectangle> solidBoxes = new ArrayList<>();

    // NPC scaled: 384*0.825=317, 300*1.1=330, 32*0.825=26, 64*1.1=70
    Rectangle npcBox         = new Rectangle(317, 330, 26, 70);
    // Interact zone: 350*0.825=289, 280*1.1=308, 100*0.825=83, 120*1.1=132
    Rectangle npcInteractBox = new Rectangle(289, 308, 83, 132);

    boolean nearNPC        = false;
    boolean nearItem       = false;
    boolean showDialogue   = false;
    boolean questStarted   = false;
    boolean allNitesSolved = false;

    String currentObjective = "Find the Ghost";
    String dialogueText     = "";
    String dialogueSpeaker  = "Ghost";

    String statusMessage = "";
    long   statusTimer   = 0;

    class QuestItem {
        Rectangle bounds;
        String equation;
        String answer;
        Image  fragmentImg;
        boolean solved = false;

        QuestItem(int x, int y, String eq, String ans, Image img) {
            // item size scaled: 180*0.825=149, 180*1.1=198
            this.bounds      = new Rectangle(x, y, 149, 198);
            this.equation    = eq;
            this.answer      = ans;
            this.fragmentImg = img;
        }
    }

    ArrayList<QuestItem> items = new ArrayList<>();
    QuestItem activeQuestItem  = null;
    String    currentInput     = "";

    public G3_Room1_PD4() {
        setPreferredSize(new Dimension(660, 660));
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);

        playMusic("music/Fedora.wav");

        try {
            for (int i = 0; i < 4; i++) {
                pUp[i]    = scalePlayer("images/up_"    + (i + 1) + ".png");
                pDown[i]  = scalePlayer("images/down_"  + (i + 1) + ".png");
                pLeft[i]  = scalePlayer("images/left_"  + (i + 1) + ".png");
                pRight[i] = scalePlayer("images/right_" + (i + 1) + ".png");
            }
            bg        = new ImageIcon("images/G3_background.png").getImage();
            npcSprite = new ImageIcon("images/Celene.png").getImage();
            niteIcon  = new ImageIcon("images/G3_nite2.png").getImage();
            light1    = new ImageIcon("images/G3_light1.png").getImage();
            light2    = new ImageIcon("images/G3_light2.png").getImage();
            light3    = new ImageIcon("images/G3_light3.png").getImage();
        } catch (Exception e) {
            System.err.println("Resource error: " + e.getMessage());
        }

        // ── Solid boxes scaled from original 800x600 ──────────────────────
        // Each entry: was (origX, origY, origW, origH)
        solidBoxes.add(new Rectangle(  0, 176,  33, 11));  // was (0,   160,  40, 10)
        solidBoxes.add(new Rectangle( 66, 176,  50, 11));  // was (80,  160,  60, 10)
        solidBoxes.add(new Rectangle(107,  99,   8, 88));  // was (130,  90,  10, 80)
        solidBoxes.add(new Rectangle(107,  99, 165, 11));  // was (130,  90, 200, 10)
        solidBoxes.add(new Rectangle(256,   0,  17,264));  // was (310,   0,  20,240)
        solidBoxes.add(new Rectangle(256, 308,  17,242));  // was (310, 280,  20,220)
        solidBoxes.add(new Rectangle(  0, 545, 272, 11));  // was (0,   495, 330, 10)
        solidBoxes.add(new Rectangle(256, 440,  58, 11));  // was (310, 400,  70, 10)
        solidBoxes.add(new Rectangle(371, 440,  74, 11));  // was (450, 400,  90, 10)
        solidBoxes.add(new Rectangle(425,   0,  17,110));  // was (515,   0,  20,100)
        solidBoxes.add(new Rectangle(425, 171,  21,385));  // was (515, 155,  25,350)
        solidBoxes.add(new Rectangle(425, 539, 235, 17));  // was (515, 490, 330, 15) — right edge capped at 660
        solidBoxes.add(new Rectangle(425, 358, 165, 17));  // was (515, 325, 200, 15)
        solidBoxes.add(npcBox);

        // Quest item positions scaled: 80*0.825=66, 310*0.825=256, 540*0.825=446; 100*1.1=110
        items.add(new QuestItem( 66, 110, "2Mg + O2 -> 2MgO",        "synthesis",     light1));
        items.add(new QuestItem(256, 110, "2H2O -> 2H2 + O2",         "decomposition", light2));
        items.add(new QuestItem(446, 110, "CH4 + 2O2 -> CO2 + 2H2O", "combustion",    light3));

        timer.start();
    }

    // --- AUDIO METHODS ---
    private void playMusic(String location) {
        try {
            File musicPath = new File(location);
            if (musicPath.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicPath);
                bgmClip = AudioSystem.getClip();
                bgmClip.open(audioInput);
                bgmClip.start();
                bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
            } else {
                System.out.println("Can't find audio file: " + location);
            }
        } catch (Exception e) {
            System.out.println("Error playing music: " + e);
        }
    }

    public static void stopMusic() {
        if (bgmClip != null && bgmClip.isOpen()) {
            if (bgmClip.isRunning()) bgmClip.stop();
            bgmClip.close();
            bgmClip = null;
        }
    }

    private ImageIcon scalePlayer(String path) {
        try {
            ImageIcon icon = new ImageIcon(path);
            if (icon.getImageLoadStatus() == MediaTracker.ERRORED)
                return new ImageIcon(new java.awt.image.BufferedImage(32, 64, java.awt.image.BufferedImage.TYPE_INT_ARGB));
            return new ImageIcon(icon.getImage().getScaledInstance(32, 64, Image.SCALE_SMOOTH));
        } catch (Exception e) {
            return new ImageIcon();
        }
    }

    private ImageIcon currentSprite() {
        return switch (direction) {
            case 0 -> pUp[walkFrame];
            case 2 -> pLeft[walkFrame];
            case 3 -> pRight[walkFrame];
            default -> pDown[walkFrame];
        };
    }

    private void attemptMove(int dx, int dy) {
        if (activeQuestItem != null || showDialogue) return;
        int nextX = playerX + dx;
        int nextY = playerY + dy;
        if (nextX < 0 || nextX > 660 - 32 || nextY < 0 || nextY > 660 - 64) return;

        Rectangle nextHitbox = new Rectangle(nextX + 9, nextY + 53, 13, 10);
        boolean blocked = false;
        for (Rectangle box : solidBoxes) {
            if (nextHitbox.intersects(box)) { blocked = true; break; }
        }
        if (!blocked) {
            playerX = nextX;
            playerY = nextY;
            walkFrame = (walkFrame + 1) % 4;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Rectangle playerHitbox = new Rectangle(playerX + 9, playerY + 53, 13, 10);
        nearNPC = playerHitbox.intersects(npcInteractBox);

        nearItem = false;
        if (questStarted && !allNitesSolved) {
            Rectangle interactZone = new Rectangle(playerX - 30, playerY - 30, 92, 124);
            for (QuestItem item : items) {
                if (!item.solved && interactZone.intersects(item.bounds)) {
                    nearItem = true;
                    break;
                }
            }
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        if (bg != null) g.drawImage(bg, 0, 0, getWidth(), getHeight(), null);
        if (npcSprite != null) g.drawImage(npcSprite, npcBox.x, npcBox.y, npcBox.width, npcBox.height, null);

        ImageIcon sprite = currentSprite();
        if (sprite != null && sprite.getIconWidth() > 0)
            g.drawImage(sprite.getImage(), playerX, playerY, playerX + 32, playerY + 64,
                        0, 0, sprite.getIconWidth(), sprite.getIconHeight(), null);

        if (questStarted) {
            for (QuestItem item : items) {
                if (!item.solved && item.fragmentImg != null) {
                    float pulse = (float)(Math.sin(System.currentTimeMillis() / 400.0) * 0.15 + 0.85);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pulse));
                    g.drawImage(item.fragmentImg, item.bounds.x, item.bounds.y, item.bounds.width, item.bounds.height, null);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                }
            }
            if (allNitesSolved) {
                g.setColor(new Color(0, 255, 150, 40));
                g.fillRect(0, 0, 660, 660);
                g.setColor(Color.WHITE);
                g.setFont(new Font("Serif", Font.BOLD, 48));
                String msg = "NITE RESTORED!";
                g.drawString(msg, 330 - g.getFontMetrics().stringWidth(msg) / 2, 330);
            }
        }

        drawUI(g);
        if (showDialogue) drawDialogueBox(g, dialogueSpeaker, dialogueText);
        if (activeQuestItem != null) drawQuizBox(g);
    }

    private void drawUI(Graphics g) {
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRoundRect(20, 20, 280, 50, 15, 15);
        g.setColor(Color.WHITE);
        g.drawRoundRect(20, 20, 280, 50, 15, 15);
        g.setFont(new Font("Monospaced", Font.BOLD, 14));
        g.drawString("OBJECTIVE:", 35, 40);
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g.setColor(questStarted ? (allNitesSolved ? Color.GREEN : Color.YELLOW) : Color.WHITE);
        g.drawString(currentObjective, 35, 58);

        if ((nearNPC || nearItem) && !showDialogue && activeQuestItem == null) {
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRoundRect(230, 390, 200, 30, 10, 10);
            g.setColor(Color.YELLOW);
            g.setFont(new Font("SansSerif", Font.BOLD, 13));
            g.drawString("[SPACE] to interact", 265, 410);
        }

        if (System.currentTimeMillis() - statusTimer < 3000 && !statusMessage.isEmpty()) {
            g.setColor(new Color(20, 0, 0, 230));
            g.fillRect(130, 580, 400, 40);
            g.setColor(Color.RED);
            g.drawRect(130, 580, 400, 40);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.drawString(statusMessage, 330 - g.getFontMetrics().stringWidth(statusMessage) / 2, 605);
        }
    }

    private void drawDialogueBox(Graphics g, String speaker, String text) {
        int bw = 620, bh = 110, bx = 20, by = 520;
        g.setColor(new Color(0, 0, 0, 240));
        g.fillRoundRect(bx, by, bw, bh, 20, 20);
        g.setColor(Color.WHITE);
        g.drawRoundRect(bx, by, bw, bh, 20, 20);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.setColor(Color.LIGHT_GRAY);
        g.drawString(speaker, bx + 25, by + 35);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Serif", Font.ITALIC, 22));
        g.drawString(text, bx + 25, by + 75);
    }

    private void drawQuizBox(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 22));
        g.drawString("RECONSTRUCTION TRIAL:", 100, 100);
        g.setFont(new Font("Monospaced", Font.BOLD, 42));
        g.setColor(Color.CYAN);
        String eq = activeQuestItem.equation;
        g.drawString(eq, 330 - g.getFontMetrics().stringWidth(eq) / 2, 220);
        g.setColor(Color.GRAY);
        g.setFont(new Font("SansSerif", Font.PLAIN, 14));
        String hint = "Type: synthesis, decomposition, combustion, etc.";
        g.drawString(hint, 330 - g.getFontMetrics().stringWidth(hint) / 2, 270);
        g.setFont(new Font("Monospaced", Font.BOLD, 34));
        g.setColor(Color.GREEN);
        String inp = "> " + currentInput + "_";
        g.drawString(inp, 330 - g.getFontMetrics().stringWidth(inp) / 2, 380);
    }

    @Override
public void mousePressed(MouseEvent e) {
    if (activeQuestItem != null || showDialogue) return;

    Point click = e.getPoint();

    // Check if clicked on NPC interact zone
    if (npcInteractBox.contains(click)) {
        dialogueSpeaker = "Ghost";
        dialogueText = allNitesSolved
                ? "Thank you... we are whole again."
                : "Help her... Night broke the pieces...";
        showDialogue = true;
        repaint();
        return;
    }

    // Check if clicked on a quest item
    if (questStarted && !allNitesSolved) {
        for (QuestItem item : items) {
            if (!item.solved && item.bounds.contains(click)) {
                activeQuestItem = item;
                currentInput = "";
                repaint();
                return;
            }
        }
    }
}
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (activeQuestItem != null) { handleQuizInput(e); return; }

        if (key == KeyEvent.VK_SPACE) {
            if (showDialogue) {
                showDialogue = false;
                if (!questStarted) {
                    questStarted = true;
                    currentObjective = "Collect the fragments";
                }
            } else if (nearNPC) {
                dialogueSpeaker = "Ghost";
                dialogueText = allNitesSolved
                        ? "Thank you... we are whole again."
                        : "Help her... Night broke the pieces...";
                showDialogue = true;
            } else if (nearItem) {
                Rectangle interactZone = new Rectangle(playerX - 30, playerY - 30, 92, 124);
                for (QuestItem item : items) {
                    if (!item.solved && interactZone.intersects(item.bounds)) {
                        activeQuestItem = item;
                        currentInput = "";
                        break;
                    }
                }
            }
        }
        else if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP)   { direction = 0; attemptMove(0, -TILE_SIZE); }
        else if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN)  { direction = 1; attemptMove(0,  TILE_SIZE); }
        else if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT)  { direction = 2; attemptMove(-TILE_SIZE, 0); }
        else if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) { direction = 3; attemptMove( TILE_SIZE, 0); }
        repaint();
    }

    private void handleQuizInput(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_ENTER) {
            if (currentInput.trim().equalsIgnoreCase(activeQuestItem.answer)) {
                activeQuestItem.solved = true;
                activeQuestItem = null;
                checkAllPieces();
            } else {
                statusMessage = "REACTION MISMATCH.";
                statusTimer = System.currentTimeMillis();
                currentInput = "";
            }
        } else if (key == KeyEvent.VK_BACK_SPACE && currentInput.length() > 0) {
            currentInput = currentInput.substring(0, currentInput.length() - 1);
        } else if (key == KeyEvent.VK_ESCAPE) {
            activeQuestItem = null;
        } else {
            char c = e.getKeyChar();
            if (Character.isLetter(c)) currentInput += Character.toLowerCase(c);
        }
        repaint();
    }

    private void checkAllPieces() {
        boolean allDone = true;
        for (QuestItem item : items) if (!item.solved) allDone = false;
        if (allDone) {
            allNitesSolved = true;
            currentObjective = "Free Roam Mode";
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Chemistry Quest: Free Roam");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new G3_Room1_PD4());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
