/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package codes;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * MEMBERS: FEDORA MAYNOPAS, GN JUMALON, EDWARD MALVAS*/

class Entity {
    protected int x, y;
    public Entity(int x, int y) { this.x = x; this.y = y; }
}

class Player extends Entity {
    private int speed;
    private int hitboxOffsetX = 9, hitboxOffsetY = 53;
    private int hitboxWidth = 13, hitboxHeight = 10;

    public Player(int x, int y, int speed) {
        super(x, y);
        this.speed = speed;
    }

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public int getSpeed() { return speed; }

    public Rectangle getHitbox() {
        return new Rectangle(x + hitboxOffsetX, y + hitboxOffsetY, hitboxWidth, hitboxHeight);
    }
}

public class NewMain extends JPanel implements ActionListener, KeyListener, MouseListener {

    Player player = new Player(380, 480, 5);
    boolean up, down, left, right;

    Image bg, upStrip, downStrip, leftStrip, rightStrip, currentStrip;
    Image npcSprite, niteIcon, light1, light2, light3;

    int frame = 0, totalFrames = 4, frameWidth = 16, frameHeight = 32, animDelay = 0;

    Timer timer = new Timer(16, this);
    ArrayList<Rectangle> solidBoxes = new ArrayList<>();

    Rectangle npcBox = new Rectangle(384, 300, 32, 64); 
    Rectangle npcInteractBox = new Rectangle(350, 280, 100, 120);
    
    boolean nearNPC = false;
    boolean showDialogue = false;
    boolean questStarted = false; 
    
    String currentObjective = "Find the Ghost";
    String dialogueText = "";
    String dialogueSpeaker = "Ghost";

    boolean allNitesSolved = false;
    boolean showNiteSplash = false;
    long splashStartTime = 0;

    String statusMessage = "";
    long statusTimer = 0;

    class QuestItem {
        Rectangle bounds;
        String equation;
        String answer;
        Image fragmentImg;
        boolean solved = false;

        QuestItem(int x, int y, String eq, String ans, Image img) {
            this.bounds = new Rectangle(x, y, 180, 180); 
            this.equation = eq;
            this.answer = ans;
            this.fragmentImg = img;
        }
    }

    ArrayList<QuestItem> items = new ArrayList<>();
    QuestItem activeQuestItem = null; 
    String currentInput = ""; 

    public NewMain() {
        setPreferredSize(new Dimension(800, 600));
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);

        try {
            bg = new ImageIcon(getClass().getResource("/ASSETS/G3_background.png")).getImage();
            upStrip = new ImageIcon(getClass().getResource("/ASSETS/G3_MC_walkUp.png")).getImage();
            downStrip = new ImageIcon(getClass().getResource("/ASSETS/G3_MC_walkDown.png")).getImage();
            leftStrip = new ImageIcon(getClass().getResource("/ASSETS/G3_MC_walkLeft.png")).getImage();
            rightStrip = new ImageIcon(getClass().getResource("/ASSETS/G3_MC_walkRight.png")).getImage();
            npcSprite = new ImageIcon(getClass().getResource("/ASSETS/G3_npc.png")).getImage();
            niteIcon = new ImageIcon(getClass().getResource("/ASSETS/G3_nite2.png")).getImage();
            light1 = new ImageIcon(getClass().getResource("/ASSETS/G3_light1.png")).getImage();
            light2 = new ImageIcon(getClass().getResource("/ASSETS/G3_light2.png")).getImage();
            light3 = new ImageIcon(getClass().getResource("/ASSETS/G3_light3.png")).getImage();
        } catch (Exception e) {
            System.err.println("Resource error: " + e.getMessage());
        }

        currentStrip = downStrip;

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

        items.add(new QuestItem(80, 100, "2Mg + O2 -> 2MgO", "synthesis", light1));
        items.add(new QuestItem(310, 100, "2H2O -> 2H2 + O2", "decomposition", light2));
        items.add(new QuestItem(540, 100, "CH4 + 2O2 -> CO2 + 2H2O", "combustion", light3));

        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (showNiteSplash) {
            if (System.currentTimeMillis() - splashStartTime > 4500) System.exit(0);
            repaint();
            return;
        }

        if (activeQuestItem != null || showDialogue) return; 

        boolean moving = false;
        int nextX = player.getX(), nextY = player.getY();

        if (up) { nextY -= player.getSpeed(); currentStrip = upStrip; moving = true; }
        if (down) { nextY += player.getSpeed(); currentStrip = downStrip; moving = true; }
        if (left) { nextX -= player.getSpeed(); currentStrip = leftStrip; moving = true; }
        if (right) { nextX += player.getSpeed(); currentStrip = rightStrip; moving = true; }

        Rectangle nextHitbox = new Rectangle(nextX + 9, nextY + 53, 13, 10);
        boolean blocked = false;
        for (Rectangle box : solidBoxes) {
            if (nextHitbox.intersects(box)) { blocked = true; break; }
        }

        if (!blocked) {
            player.setX(nextX);
            player.setY(nextY);
        }

        nearNPC = player.getHitbox().intersects(npcInteractBox);
        
        if (moving) {
            animDelay++;
            if (animDelay > 6) { frame = (frame + 1) % totalFrames; animDelay = 0; }
        } else { frame = 0; }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        if (bg != null) g.drawImage(bg, 0, 0, getWidth(), getHeight(), null);
        else { g.setColor(Color.BLACK); g.fillRect(0,0,800,600); }

        if (npcSprite != null) g.drawImage(npcSprite, npcBox.x, npcBox.y, npcBox.width, npcBox.height, null);

        int sx = frame * frameWidth;
        int px = player.getX(), py = player.getY();
        if (currentStrip != null) {
            g.drawImage(currentStrip, px, py, px + 32, py + 64, sx, 0, sx + 16, 32, null);
        }

        if (questStarted) {
            g.setColor(new Color(0, 0, 0, 180)); 
            g.fillRect(0, 0, getWidth(), getHeight());

            for (QuestItem item : items) {
                if (!item.solved) {
                    if (item.fragmentImg != null) {
                        float pulse = (float)(Math.sin(System.currentTimeMillis() / 400.0) * 0.15 + 0.85);
                        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pulse));
                        g.drawImage(item.fragmentImg, item.bounds.x, item.bounds.y, item.bounds.width, item.bounds.height, null);
                        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                    }
                    g.setColor(new Color(255, 255, 255, 50));
                    g.drawRoundRect(item.bounds.x, item.bounds.y, item.bounds.width, item.bounds.height, 20, 20);
                } else {
                    g.setColor(new Color(100, 255, 200, 100));
                    g.fillOval(item.bounds.x + 60, item.bounds.y + 60, 60, 60);
                }
            }
        }

        drawUI(g);
        if (showDialogue) drawDialogueBox(g, dialogueSpeaker, dialogueText);
        if (activeQuestItem != null) drawQuizBox(g);
        if (showNiteSplash) drawFinalReward(g);
    }

    private void drawUI(Graphics g) {
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRoundRect(20, 20, 280, 50, 15, 15);
        g.setColor(Color.WHITE);
        g.drawRoundRect(20, 20, 280, 50, 15, 15);
        g.setFont(new Font("Monospaced", Font.BOLD, 14));
        g.drawString("OBJECTIVE:", 35, 40);
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g.setColor(questStarted ? Color.YELLOW : Color.WHITE);
        g.drawString(currentObjective, 35, 58);

       
        if (System.currentTimeMillis() - statusTimer < 3000 && !statusMessage.isEmpty()) {
            g.setColor(new Color(20, 0, 0, 230));
            g.fillRect(200, 520, 400, 40);
            g.setColor(Color.RED);
            g.drawRect(200, 520, 400, 40);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.drawString(statusMessage, 400 - g.getFontMetrics().stringWidth(statusMessage)/2, 545);
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
        g.fillRect(0,0,800,600); 
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 22));
        g.drawString("RECONSTRUCTION TRIAL:", 100, 100);
        g.setFont(new Font("Monospaced", Font.BOLD, 42));
        g.setColor(Color.CYAN);
        String eq = activeQuestItem.equation;
        g.drawString(eq, 400 - g.getFontMetrics().stringWidth(eq)/2, 220);
        g.setColor(Color.GRAY);
        g.setFont(new Font("SansSerif", Font.PLAIN, 14));
        String hint = "Type: synthesis, decomposition, combustion, etc.";
        g.drawString(hint, 400 - g.getFontMetrics().stringWidth(hint)/2, 270);
        g.setFont(new Font("Monospaced", Font.BOLD, 34));
        g.setColor(Color.GREEN);
        String inp = "> " + currentInput + "_";
        g.drawString(inp, 400 - g.getFontMetrics().stringWidth(inp)/2, 380);
    }

    private void drawFinalReward(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        if (niteIcon != null) g.drawImage(niteIcon, 250, 50, 300, 300, null);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Serif", Font.BOLD, 48));
        String msg = "NITE RESTORED";
        g.drawString(msg, 400 - g.getFontMetrics().stringWidth(msg)/2, 450);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        try {
            if (!questStarted || showNiteSplash || activeQuestItem != null) return;
            Point click = e.getPoint();
            boolean clickedSomething = false;
            for (QuestItem item : items) {
                if (!item.solved && item.bounds.contains(click)) {
                    activeQuestItem = item;
                    currentInput = "";
                    clickedSomething = true;
                    repaint();
                    break;
                }
            }
            if (!clickedSomething && questStarted) {
             
                throw new IllegalArgumentException("Invalid click area.");
            }
        } catch (IllegalArgumentException ex) {
            statusMessage = "Please click directly on a fragment!";
            statusTimer = System.currentTimeMillis();
            repaint();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        try {
            if (showNiteSplash) return;
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
                        currentObjective = "Click the pieces to stick them together";
                    }
                } else if (nearNPC) {
                    dialogueSpeaker = "Ghost";
                    dialogueText = "Help her... Night broke the pieces... Please...";
                    showDialogue = true;
                } else {
                    throw new UnsupportedOperationException("Nothing to interact with.");
                }
            } else if (isMovementKey(key)) {
                handleMovement(key, true);
            } else {
               
                throw new KeyException("Invalid input. Use WASD or E.");
            }
        } catch (UnsupportedOperationException ex) {
            statusMessage = "Approach the ghost or a fragment first!";
            statusTimer = System.currentTimeMillis();
        } catch (KeyException ex) {
            statusMessage = ex.getMessage();
            statusTimer = System.currentTimeMillis();
        } catch (Exception ex) {
            statusMessage = "Error: Invalid command.";
            statusTimer = System.currentTimeMillis();
        }
        repaint();
    }

    private boolean isMovementKey(int key) {
        return key == KeyEvent.VK_W || key == KeyEvent.VK_UP ||
               key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN ||
               key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT ||
               key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT;
    }

    private void handleMovement(int key, boolean pressed) {
        switch (key) {
            case KeyEvent.VK_W, KeyEvent.VK_UP -> up = pressed;
            case KeyEvent.VK_S, KeyEvent.VK_DOWN -> down = pressed;
            case KeyEvent.VK_A, KeyEvent.VK_LEFT -> left = pressed;
            case KeyEvent.VK_D, KeyEvent.VK_RIGHT -> right = pressed;
        }
    }

    private void handleQuizInput(KeyEvent e) {
        try {
            int key = e.getKeyCode();
            if (key == KeyEvent.VK_ENTER) {
                if (currentInput.trim().isEmpty()) {
                    throw new Exception("Input cannot be empty.");
                }
                if (currentInput.trim().equalsIgnoreCase(activeQuestItem.answer)) {
                    activeQuestItem.solved = true;
                    activeQuestItem = null;
                    checkAllPieces();
                } else {
                    statusMessage = "REACTION MISMATCH. TRY AGAIN.";
                    statusTimer = System.currentTimeMillis();
                    currentInput = "";
                }
            } else if (key == KeyEvent.VK_BACK_SPACE && currentInput.length() > 0) {
                currentInput = currentInput.substring(0, currentInput.length() - 1);
            } else if (key == KeyEvent.VK_ESCAPE) {
                activeQuestItem = null;
            } else {
                char c = e.getKeyChar();
                 if (Character.isLetter(c) || c == '-') {
                    currentInput += Character.toLowerCase(c);
                } else if (Character.isDigit(c)) {
                    throw new NumberFormatException("Reaction types do not contain numbers.");
                } else if (key != KeyEvent.VK_SHIFT) {
                    throw new KeyException("Invalid character for reaction name.");
                }
            }
        } catch (NumberFormatException ex) {
            statusMessage = ex.getMessage();
            statusTimer = System.currentTimeMillis();
        } catch (KeyException ex) {
            statusMessage = ex.getMessage();
            statusTimer = System.currentTimeMillis();
        } catch (Exception ex) {
            statusMessage = "Invalid Input: " + ex.getMessage();
            statusTimer = System.currentTimeMillis();
        }
        repaint();
    }

    private void checkAllPieces() {
        boolean allDone = true;
        for (QuestItem item : items) if (!item.solved) allDone = false;
        if (allDone) {
            allNitesSolved = true;
            showNiteSplash = true;
            splashStartTime = System.currentTimeMillis();
        }
    }

    @Override public void keyReleased(KeyEvent e) {
        handleMovement(e.getKeyCode(), false);
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}


    class KeyException extends Exception {
        public KeyException(String msg) { super(msg); }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Chemistry Quest: The Shattered Nite");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new Main());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
