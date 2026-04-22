package codes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * MEMBERS: FEDORA MAYNOPAS, GN JUMALON, EDWARD MALVAS
 * MODIFIED: Tile-based movement & Post-quest free roam
 */

public class G3_Room1_PD4 extends JPanel implements ActionListener, KeyListener, MouseListener {

    // ── Grid & Movement Constants ────────────────────────────────────────────
    int playerX = 400, playerY = 480; 
    final int TILE_SIZE = 40; 
    // ────────────────────────────────────────────────────────────────────────

    // ── Animated player sprites (4 frames × 4 directions) ───────────────────
    ImageIcon[] pUp    = new ImageIcon[4];
    ImageIcon[] pDown  = new ImageIcon[4];
    ImageIcon[] pLeft  = new ImageIcon[4];
    ImageIcon[] pRight = new ImageIcon[4];
    int walkFrame = 0;
    int direction = 1; // 0=up, 1=down, 2=left, 3=right

    Image bg, npcSprite, niteIcon, light1, light2, light3;

    Timer timer = new Timer(16, this);
    ArrayList<Rectangle> solidBoxes = new ArrayList<>();

    Rectangle npcBox         = new Rectangle(384, 300, 32, 64);
    Rectangle npcInteractBox = new Rectangle(350, 280, 100, 120);

    boolean nearNPC      = false;
    boolean showDialogue = false;
    boolean questStarted = false;
    boolean allNitesSolved = false;

    String currentObjective = "Find the Ghost";
    String dialogueText      = "";
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
            this.bounds      = new Rectangle(x, y, 180, 180);
            this.equation    = eq;
            this.answer      = ans;
            this.fragmentImg = img;
        }
    }

    ArrayList<QuestItem> items = new ArrayList<>();
    QuestItem activeQuestItem  = null;
    String    currentInput     = "";

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

    public G3_Room1_PD4() {
        setPreferredSize(new Dimension(800, 600));
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);

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

        // Environment hitboxes
        solidBoxes.add(new Rectangle(0, 160, 40, 10));
        solidBoxes.add(new Rectangle(80, 160, 60, 10));
        solidBoxes.add(new Rectangle(130, 90, 10, 80));
        solidBoxes.add(new Rectangle(130, 90, 200, 10));
        solidBoxes.add(new Rectangle(310, 0, 20, 240));
        solidBoxes.add(new Rectangle(310, 280, 20, 220));
        solidBoxes.add(new Rectangle(0, 495, 330, 10));
        solidBoxes.add(new Rectangle(310, 400, 70, 10));
        solidBoxes.add(new Rectangle(450, 400, 90, 10));
        solidBoxes.add(new Rectangle(515, 0, 20, 100));
        solidBoxes.add(new Rectangle(515, 155, 25, 350));
        solidBoxes.add(new Rectangle(515, 490, 330, 15));
        solidBoxes.add(new Rectangle(515, 325, 200, 15));
        solidBoxes.add(npcBox);

        items.add(new QuestItem(80,  100, "2Mg + O2 -> 2MgO",        "synthesis",    light1));
        items.add(new QuestItem(310, 100, "2H2O -> 2H2 + O2",         "decomposition", light2));
        items.add(new QuestItem(540, 100, "CH4 + 2O2 -> CO2 + 2H2O", "combustion",    light3));

        timer.start();
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
        // Movement is only blocked by active Dialogue or active Quiz, NOT completion status
        if (activeQuestItem != null || showDialogue) return;

        int nextX = playerX + dx;
        int nextY = playerY + dy;

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
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Background
        if (bg != null) g.drawImage(bg, 0, 0, getWidth(), getHeight(), null);
        else { g.setColor(Color.BLACK); g.fillRect(0, 0, 800, 600); }

        // NPC
        if (npcSprite != null) g.drawImage(npcSprite, npcBox.x, npcBox.y, npcBox.width, npcBox.height, null);

        // Player
        ImageIcon sprite = currentSprite();
        if (sprite != null && sprite.getIconWidth() > 0)
            g.drawImage(sprite.getImage(), playerX, playerY, playerX + 32, playerY + 64,
                        0, 0, sprite.getIconWidth(), sprite.getIconHeight(), null);

        // Quest Overlay (Fragments)
        if (questStarted) {
            // Draw fragments only if not solved
            for (QuestItem item : items) {
                if (!item.solved) {
                    if (item.fragmentImg != null) {
                        float pulse = (float)(Math.sin(System.currentTimeMillis() / 400.0) * 0.15 + 0.85);
                        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pulse));
                        g.drawImage(item.fragmentImg, item.bounds.x, item.bounds.y, item.bounds.width, item.bounds.height, null);
                        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                    }
                }
            }

            // Completion Message (Transparent overlay so you can still see the world)
            if (allNitesSolved) {
                g.setColor(new Color(0, 255, 150, 40)); // Very light green tint
                g.fillRect(0, 0, 800, 600);
                g.setColor(Color.WHITE);
                g.setFont(new Font("Serif", Font.BOLD, 48));
                String msg = "NITE RESTORED!";
                g.drawString(msg, 400 - g.getFontMetrics().stringWidth(msg) / 2, 300);
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

        if (System.currentTimeMillis() - statusTimer < 3000 && !statusMessage.isEmpty()) {
            g.setColor(new Color(20, 0, 0, 230));
            g.fillRect(200, 520, 400, 40);
            g.setColor(Color.RED);
            g.drawRect(200, 520, 400, 40);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.drawString(statusMessage, 400 - g.getFontMetrics().stringWidth(statusMessage) / 2, 545);
        }
    }

    private void drawDialogueBox(Graphics g, String speaker, String text) {
        int bw = 640, bh = 110, bx = 80, by = 450;
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
        g.fillRect(0, 0, 800, 600);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 22));
        g.drawString("RECONSTRUCTION TRIAL:", 100, 100);
        g.setFont(new Font("Monospaced", Font.BOLD, 42));
        g.setColor(Color.CYAN);
        String eq = activeQuestItem.equation;
        g.drawString(eq, 400 - g.getFontMetrics().stringWidth(eq) / 2, 220);
        g.setColor(Color.GRAY);
        g.setFont(new Font("SansSerif", Font.PLAIN, 14));
        String hint = "Type: synthesis, decomposition, combustion, etc.";
        g.drawString(hint, 400 - g.getFontMetrics().stringWidth(hint) / 2, 270);
        g.setFont(new Font("Monospaced", Font.BOLD, 34));
        g.setColor(Color.GREEN);
        String inp = "> " + currentInput + "_";
        g.drawString(inp, 400 - g.getFontMetrics().stringWidth(inp) / 2, 380);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (!questStarted || activeQuestItem != null || allNitesSolved) return;
        Point click = e.getPoint();
        for (QuestItem item : items) {
            if (!item.solved && item.bounds.contains(click)) {
                activeQuestItem = item;
                currentInput = "";
                repaint();
                break;
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (activeQuestItem != null) {
            handleQuizInput(e);
            return;
        }

        if (key == KeyEvent.VK_E) {
            if (showDialogue) {
                showDialogue = false;
                if (!questStarted) {
                    questStarted = true;
                    currentObjective = "Collect the fragments";
                }
            } else if (nearNPC) {
                dialogueSpeaker = "Ghost";
                dialogueText = allNitesSolved ? "Thank you... we are whole again." : "Help her... Night broke the pieces...";
                showDialogue = true;
            }
        } 
        // Movement
        else if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP) { direction = 0; attemptMove(0, -TILE_SIZE); }
        else if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN) { direction = 1; attemptMove(0, TILE_SIZE); }
        else if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT) { direction = 2; attemptMove(-TILE_SIZE, 0); }
        else if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) { direction = 3; attemptMove(TILE_SIZE, 0); }
        
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
