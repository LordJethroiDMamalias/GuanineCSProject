package codes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * MEMBERS: FEDORA MAYNOPAS, GN JUMALON, EDWARD MALVAS
 * MODIFIED: Final Boss Battle transition and Focus/Dialogue fix.
 */

public class G3_Room2_PD6 extends JPanel implements ActionListener, KeyListener, MouseListener {

    // ── Grid & Movement Constants ────────────────────────────────────────────
    int playerX = 350, playerY = 250;
    final int TILE_SIZE = 40;

    // ── Animated player sprites ──────────────────────────────────────────────
    ImageIcon[] pUp    = new ImageIcon[4];
    ImageIcon[] pDown  = new ImageIcon[4];
    ImageIcon[] pLeft  = new ImageIcon[4];
    ImageIcon[] pRight = new ImageIcon[4];
    int walkFrame = 0;
    int direction = 1; 

    Image bg, npcSprite, niteIcon, shiningSprite, characterImg, chemicalImg;

    Timer timer = new Timer(16, this);
    ArrayList<Rectangle> solidBoxes = new ArrayList<>();

    Rectangle npcBox         = new Rectangle(200, 200, 32, 64);
    Rectangle npcInteractBox = new Rectangle(180, 180, 100, 120);

    Battle battle = new Battle();
    JFrame frame; 

    boolean nearNPC           = false;
    boolean nearItem          = false;
    boolean showDialogue      = false;
    boolean questStarted      = false;
    boolean allNitesSolved    = false;
    boolean finalBossTriggered = false;
    boolean showNiteSplash    = false;
    boolean hasNite           = false;
    boolean inEnding          = false;
    boolean gameFinished      = false; 
    long    splashStartTime   = 0;

    String currentObjective = "Talk to the Ghost";
    String dialogueText     = "";
    String dialogueSpeaker  = "Ghost2";
    int    dialogueStep     = 0;
    int    endStep          = 0;

    private String[] endDialogue = {
        "*After the fight, Celene steals the nite from you*",
        "You can tell that these tasks were left from my dear friend Night...",
        "She really does love Chemistry and some Math...",
        "Since you both got the 2 Nites... I'm no longer a ghost!",
        "I was trapped here in this courtroom for years!",
        "I guess Night knew I wanted to be a lawyer",
        "Here... I took this from Night..",
        "It's some chemical that makes her think clearer.",
        "It might help you in your journey"
    };

    class QuestItem {
        Rectangle bounds;
        String equation;
        String answer;
        boolean solved = false;

        QuestItem(int x, int y, String eq, String ans) {
            this.bounds   = new Rectangle(x, y, 60, 60);
            this.equation = eq;
            this.answer   = ans;
        }
    }

    ArrayList<QuestItem> items = new ArrayList<>();
    QuestItem activeQuestItem  = null;
    String    currentInput       = "";

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

    public G3_Room2_PD6(JFrame frame) {
        this.frame = frame; 
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

            bg            = new ImageIcon("images/G3_background.png").getImage();
            npcSprite     = new ImageIcon("images/Celene.png").getImage();
            niteIcon      = new ImageIcon("images/G3_nite.png").getImage();
            shiningSprite = new ImageIcon("images/G3_light.png").getImage();
            characterImg  = new ImageIcon("images/G3_FEDORA.png").getImage();
            chemicalImg   = new ImageIcon("images/G3_CHEMICAL.png").getImage();
        } catch (Exception e) {
            System.err.println("Resource error: " + e.getMessage());
        }

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

        items.add(new QuestItem(50,  300, "H2 + O2 -> H2O",        "2,1,2"));
        items.add(new QuestItem(350, 420, "N2 + H2 -> NH3",         "1,3,2"));
        items.add(new QuestItem(600, 200, "CH4 + O2 -> CO2 + H2O", "1,2,1,2"));

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
        if (activeQuestItem != null || showNiteSplash || inEnding) return;

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
        if (showNiteSplash) {
            if (System.currentTimeMillis() - splashStartTime > 2000) {
                showNiteSplash = false;
                hasNite = true;
                inEnding = true;
                endStep = 0;
                this.requestFocusInWindow(); // Grab focus back
            }
        }

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
        else { g.setColor(Color.BLACK); g.fillRect(0, 0, 800, 600); }

        if (npcSprite != null) g.drawImage(npcSprite, npcBox.x, npcBox.y, npcBox.width, npcBox.height, null);

        ImageIcon sprite = currentSprite();
        if (sprite != null && sprite.getIconWidth() > 0)
            g.drawImage(sprite.getImage(), playerX, playerY, playerX + 32, playerY + 64,
                        0, 0, sprite.getIconWidth(), sprite.getIconHeight(), null);

        if (questStarted) {
            for (QuestItem item : items) {
                if (!item.solved && shiningSprite != null) {
                    float pulse = (float)(Math.sin(System.currentTimeMillis() / 400.0) * 0.15 + 0.85);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pulse));
                    g.drawImage(shiningSprite, item.bounds.x, item.bounds.y, item.bounds.width, item.bounds.height, null);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                }
            }
        }

        if (showNiteSplash) {
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, 800, 600);
            if (niteIcon != null) g.drawImage(niteIcon, 250, 100, 300, 300, null);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Serif", Font.BOLD, 36));
            String msg = "Found the Nite!";
            g.drawString(msg, 400 - g.getFontMetrics().stringWidth(msg) / 2, 450);
        }

        drawUI(g);
        
        // Priority drawing: Ending dialogue covers normal dialogue
        if (inEnding && !gameFinished) {
            drawEnding(g); 
        } else if (showDialogue) {
            drawDialogueBox(g, dialogueSpeaker, dialogueText);
        }
        
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
        
        String obj = gameFinished ? "Quest Complete!" : currentObjective;
        g.drawString(obj, 35, 58);

        if (hasNite && niteIcon != null) {
            g.drawImage(niteIcon, 720, 20, 50, 50, null);
        }

        if ((nearNPC || nearItem) && !showDialogue && activeQuestItem == null && !inEnding) {
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRoundRect(300, 400, 200, 30, 10, 10);
            g.setColor(Color.YELLOW);
            g.setFont(new Font("SansSerif", Font.BOLD, 13));
            g.drawString("[E] to interact", 348, 420);
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
        g.drawString("BALANCING TRIAL:", 100, 100);
        g.setFont(new Font("Monospaced", Font.BOLD, 36));
        g.setColor(Color.CYAN);
        String eq = activeQuestItem.equation;
        g.drawString(eq, 400 - g.getFontMetrics().stringWidth(eq) / 2, 220);
        g.setFont(new Font("Monospaced", Font.BOLD, 34));
        g.setColor(Color.GREEN);
        String inp = "> " + currentInput + "_";
        g.drawString(inp, 400 - g.getFontMetrics().stringWidth(inp) / 2, 380);
    }

    private void drawEnding(Graphics g) {
        if (endStep < endDialogue.length) {
            if (characterImg != null) {
                g.drawImage(characterImg, 500, 50, 250, 400, null);
            }
            drawDialogueBox(g, "Celene", endDialogue[endStep]);
        } else if (endStep == endDialogue.length) {
            if (chemicalImg != null) {
                g.drawImage(chemicalImg, 300, 150, 200, 200, null);
            }
            drawDialogueBox(g, "System", "Obtained Chemical Potion!");
        } else {
            drawDialogueBox(g, "System", "Your head feels lighter! Your thoughts are clearer.");
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        // 1. High Priority: Ending sequence input handling
        if (inEnding) {
            if (key == KeyEvent.VK_E) {
                endStep++;
                if (endStep > endDialogue.length + 1) {
                    inEnding = false;
                    gameFinished = true;
                    currentObjective = "Quest Complete!";
                }
                repaint();
            }
            return; // Important: blocks other code from running
        }

        // 2. Quiz Input
        if (activeQuestItem != null) {
            handleQuizInput(e);
            return;
        }

        // 3. Regular Input
        if (key == KeyEvent.VK_E) {
            if (showDialogue) {
                advanceDialogue();
            } else if (nearNPC) {
                if (!questStarted) {
                    dialogueSpeaker = "Ghost2";
                    dialogueText    = "Let's make it harder shall we? Go find the nites.";
                    showDialogue    = true;
                    questStarted    = true;
                    currentObjective = "Collect the Nites";
                } else if (allNitesSolved && !finalBossTriggered) {
                    dialogueStep = 20;
                    showDialogue = true; 
                    advanceDialogue();
                }
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
        else if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP)    { direction = 0; attemptMove(0, -TILE_SIZE); }
        else if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN)   { direction = 1; attemptMove(0,  TILE_SIZE); }
        else if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT)   { direction = 2; attemptMove(-TILE_SIZE, 0); }
        else if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT)  { direction = 3; attemptMove( TILE_SIZE, 0); }

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
                currentInput  = "";
            }
        } else if (key == KeyEvent.VK_BACK_SPACE && currentInput.length() > 0) {
            currentInput = currentInput.substring(0, currentInput.length() - 1);
        } else {
            char c = e.getKeyChar();
            if (Character.isLetterOrDigit(c) || c == ',' || c == '+') currentInput += c;
        }
        repaint();
    }

    private void advanceDialogue() {
        if      (dialogueStep == 20) { dialogueSpeaker = "Me";     dialogueText = "Hey! I can't find it anywhere!?";    dialogueStep = 21; }
        else if (dialogueStep == 21) { dialogueSpeaker = "Me";     dialogueText = "Why are you smiling like that?";     dialogueStep = 22; }
        else if (dialogueStep == 22) { dialogueSpeaker = "Ghost2"; dialogueText = "Hehe, you've been fooled.";           dialogueStep = 23; }
        else if (dialogueStep == 23) { dialogueSpeaker = "Ghost2"; dialogueText = "It was with me all along!";          dialogueStep = 24; }
        else if (dialogueStep == 24) { dialogueSpeaker = "Celene"; dialogueText = "My name... is Celene!";             dialogueStep = 25; }
        else if (dialogueStep == 25) {
            showDialogue       = false;
            finalBossTriggered = true;
            
            battle.start(frame, "images/G3_background.png", "Celene");
            
            // Set ending state immediately
            inEnding = true; 
            endStep = 0;
            
            // Request focus back to ensure E works after the battle window closes
            this.requestFocusInWindow();
        }
        else if (dialogueStep == 30) {
            showDialogue     = false;
            showNiteSplash   = true;
            splashStartTime  = System.currentTimeMillis();
        }
        else { showDialogue = false; }
        repaint();
    }

    private void checkAllPieces() {
        if (finalBossTriggered) {
            dialogueSpeaker = "Ghost2";
            dialogueText    = "Fine.. hmph... here you go...";
            showDialogue    = true;
            dialogueStep    = 30; 
            return;
        }

        boolean allDone = true;
        for (QuestItem item : items) {
            if (!item.solved) {
                allDone = false;
                break;
            }
        }

        if (allDone && !allNitesSolved) { 
            allNitesSolved   = true;
            currentObjective = "Confront the Ghost";
            
            dialogueSpeaker  = "Me";
            dialogueText     = "Wait, did she trick me?";
            showDialogue     = true; 
            dialogueStep     = 0; 
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Chemistry Quest: Room 2");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new G3_Room2_PD6(frame)); 
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
