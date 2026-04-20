/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pkgfinal.project;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

//groupmates: GN JUMALON, FEDORA MAYNOPAS & EDWARD MALVAS

class Entity {
    protected int x, y;
    protected Image sprite;

    public Entity(int x, int y) {
        this.x = x;
        this.y = y;
    }
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

public class G3_Room2_PD6 extends JPanel implements ActionListener, KeyListener {

    Player player = new Player(350, 250, 4);

    boolean debug = false; 
    boolean up, down, left, right;

    Image bg;
    Image upStrip, downStrip, leftStrip, rightStrip;
    Image currentStrip;
    
    Image npcSprite;
    Image shiningSprite;
    Image niteIcon;

    int frame = 0;
    int totalFrames = 4;
    int frameWidth = 16;
    int frameHeight = 32;
    int animDelay = 0;

    Timer timer = new Timer(16, this);

    ArrayList<Rectangle> solidBoxes = new ArrayList<>();

    Rectangle npcBox = new Rectangle(200, 200, 32, 64);
    Rectangle npcInteractBox = new Rectangle(180, 180, 72, 104);
    boolean nearNPC = false;
    boolean showDialogue = false;
    boolean questStarted = false; 
    
    String currentObjective = "Talk to the Ghost";
    String dialogueText = "";
    String dialogueSpeaker = "Ghost2";

    int dialogueStep = 0;
    boolean allNitesSolved = false;
    boolean finalBossTriggered = false;
    
    boolean hasNite = false;
    boolean showNiteSplash = false;
    long splashStartTime = 0;

   
    String statusMessage = "";
    long statusTimer = 0;

    class QuestItem {
        Rectangle bounds;
        String equation;
        String answer;
        boolean solved = false;
        boolean near = false;

        QuestItem(int x, int y, String eq, String ans) {
            this.bounds = new Rectangle(x, y, 32, 32);
            this.equation = eq;
            this.answer = ans;
        }

        @Override
        public String toString() {
            return "Quest: " + equation + " solved=" + solved;
        }
    }

    ArrayList<QuestItem> items = new ArrayList<>();
    QuestItem activeQuestItem = null; 
    String currentInput = ""; 

    public G3_Room2_PD6() {
        setPreferredSize(new Dimension(800, 600));
        setFocusable(true);
        addKeyListener(this);

      
        try {
            bg = new ImageIcon(getClass().getResource("/ASSETS/G3_background.png")).getImage();
            upStrip = new ImageIcon(getClass().getResource("/ASSETS/G3_MC_walkUp.png")).getImage();
            downStrip = new ImageIcon(getClass().getResource("/ASSETS/G3_MC_walkDown.png")).getImage();
            leftStrip = new ImageIcon(getClass().getResource("/ASSETS/G3_MC_walkLeft.png")).getImage();
            rightStrip = new ImageIcon(getClass().getResource("/ASSETS/G3_MC_walkRight.png")).getImage();
            
            npcSprite = new ImageIcon(getClass().getResource("/ASSETS/G3_npc.png")).getImage();
            shiningSprite = new ImageIcon(getClass().getResource("/ASSETS/G3_light.png")).getImage();
            niteIcon = new ImageIcon(getClass().getResource("/ASSETS/G3_nite.png")).getImage();
        } catch (Exception e) {
          
            System.err.println("Error loading assets: " + e.getMessage());
            statusMessage = "Warning: Assets missing. Check /ASSETS/ folder.";
            statusTimer = System.currentTimeMillis();
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

        items.add(new QuestItem(150, 20, "H2 + O2 -> H2O", "2,1,2"));
        items.add(new QuestItem(630, 170, "N2 + H2 -> NH3", "1,3,2"));
        items.add(new QuestItem(400, 500, "CH4 + O2 -> CO2 + H2O", "1,2,1,2"));

        timer.start();
    }

    private void drawDialogue(Graphics g, String text) {
        drawDialogueBox(g, text);
    }

    private void drawDialogue(Graphics g, String speaker, String text) {
        drawDialogueBox(g, speaker + ": " + text);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      
        try {
            if (showNiteSplash) {
    if (System.currentTimeMillis() - splashStartTime > 2000) {
        showNiteSplash = false;
        hasNite = true;
        currentObjective = "Quest Complete!";
        repaint();
        
        // Close only this window
        javax.swing.SwingUtilities.getWindowAncestor(this).dispose();
    }
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
            Rectangle screenBounds = new Rectangle(0, 0, getWidth(), getHeight());

            boolean blocked = false;
            for (Rectangle box : solidBoxes) {
                if (nextHitbox.intersects(box)) {
                    blocked = true;
                    break;
                }
            }

            if (screenBounds.contains(nextHitbox) && !blocked) {
                player.setX(nextX);
                player.setY(nextY);
            }

            nearNPC = player.getHitbox().intersects(npcInteractBox);
            
            if (questStarted && !allNitesSolved) {
                for (QuestItem item : items) {
                    item.near = player.getHitbox().intersects(new Rectangle(item.bounds.x - 20, item.bounds.y - 20, item.bounds.width + 40, item.bounds.height + 40));
                }
            }

            if (moving) {
                animDelay++;
                if (animDelay > 6) {
                    frame = (frame + 1) % totalFrames;
                    animDelay = 0;
                }
            } else {
                frame = 0;
            }
        } catch (Exception ex) {
            System.err.println("Logic Error: " + ex.getMessage());
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.drawImage(bg, 0, 0, getWidth(), getHeight(), null);
        g.drawImage(npcSprite, npcBox.x, npcBox.y, npcBox.width, npcBox.height, null);

        if (questStarted) {
            for (QuestItem item : items) {
                if (!item.solved) {
                    g.drawImage(shiningSprite, item.bounds.x, item.bounds.y, item.bounds.width, item.bounds.height, null);
                    if (item.near && activeQuestItem == null && !showDialogue) {
                        drawPrompt(g, item.bounds.x, item.bounds.y, "Press E");
                    }
                }
            }
        }

        int sx = frame * frameWidth;
        int scale = 2;
        int px = player.getX();
        int py = player.getY();
        
        
        if (currentStrip != null) {
            g.drawImage(currentStrip, px, py, px + frameWidth * scale, py + frameHeight * scale, sx, 0, sx + frameWidth, frameHeight, null);
        }

        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(580, 20, 200, 40);
        g.setColor(Color.WHITE);
        g.drawRect(580, 20, 200, 40);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.drawString("OBJECTIVES:", 590, 35);
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.drawString(currentObjective, 590, 52);

        if (hasNite) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(680, 500, 100, 80);
            g.setColor(Color.WHITE);
            g.drawRect(680, 500, 100, 80);
            g.setFont(new Font("Arial", Font.BOLD, 10));
            g.drawString("INVENTORY", 705, 515);
            g.drawImage(niteIcon, 705, 520, 50, 50, null);
        }

        if (nearNPC && !showDialogue && activeQuestItem == null && !showNiteSplash) {
            drawPrompt(g, npcBox.x, npcBox.y, "Press E");
        }

        if (showDialogue) {
            drawDialogue(g, dialogueSpeaker, dialogueText); 
        }

        if (activeQuestItem != null) {
            drawQuizBox(g);
        }

        if (showNiteSplash) {
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, getWidth(), getHeight());
            int imgSize = 300;
            g.drawImage(niteIcon, (getWidth() - imgSize) / 2, (getHeight() - imgSize) / 2 - 50, imgSize, imgSize, null);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 36));
            String splashTxt = "Found the Nite!";
            int txtWidth = g.getFontMetrics().stringWidth(splashTxt);
            g.drawString(splashTxt, (getWidth() - txtWidth) / 2, (getHeight() / 2) + 180);
        }

    
        if (System.currentTimeMillis() - statusTimer < 3000 && !statusMessage.isEmpty()) {
            g.setColor(new Color(255, 0, 0, 200));
            g.fillRect(200, 10, 400, 30);
            g.setColor(Color.WHITE);
            g.drawRect(200, 10, 400, 30);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.drawString(statusMessage, 400 - g.getFontMetrics().stringWidth(statusMessage)/2, 30);
        }
    }

    private void drawPrompt(Graphics g, int px, int py, String text) {
        g.setColor(Color.WHITE);
        g.fillRect(px + 40, py, 65, 25);
        g.setColor(Color.BLACK);
        g.drawRect(px + 40, py, 65, 25);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.drawString(text, px + 50, py + 17);
    }

    private void drawDialogueBox(Graphics g, String text) {
        int bw = 600, bh = 100;
        int bx = (getWidth() - bw) / 2, by = getHeight() - bh - 30;
        g.setColor(new Color(0, 0, 0, 220));
        g.fillRect(bx, by, bw, bh);
        g.setColor(Color.WHITE);
        g.drawRect(bx, by, bw, bh);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString(text, bx + 20, by + 55);
        g.setFont(new Font("Arial", Font.ITALIC, 12));
        g.drawString("Press E to continue...", bx + bw - 130, by + bh - 10);
    }

    private void drawQuizBox(Graphics g) {
        int bw = 540, bh = 240;
        int bx = (getWidth() - bw) / 2, by = (getHeight() - bh) / 2;
        g.setColor(new Color(20, 20, 40, 250));
        g.fillRect(bx, by, bw, bh);
        g.setColor(Color.CYAN);
        g.drawRect(bx, by, bw, bh);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Question:", bx + 40, by + 40);
        g.setFont(new Font("Monospaced", Font.BOLD, 26));
        g.setColor(Color.YELLOW);
        g.drawString(activeQuestItem.equation, bx + 40, by + 85);
        g.setFont(new Font("Arial", Font.PLAIN, 15));
        g.setColor(Color.WHITE);
        g.drawString(finalBossTriggered ? "Write the products (e.g. NaCl+H2O)" : "Enter coefficients separated by commas (e.g. 1,2,1)", bx + 40, by + 120);
        g.setColor(Color.ORANGE);
        g.setFont(new Font("Arial", Font.ITALIC, 14));
        g.drawString("Enter to submit, Escape to exit.", bx + 40, by + 145);
        g.drawString("You are stuck here until you balance the equation!", bx + 40, by + 165);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.setColor(Color.GREEN);
        g.drawString("Answer: " + currentInput + "|", bx + 40, by + 210);
    }

    private void setStatus(String msg) {
        this.statusMessage = msg;
        this.statusTimer = System.currentTimeMillis();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (showNiteSplash) return;

        int key = e.getKeyCode();

       
        if (key == KeyEvent.VK_UP || key == KeyEvent.VK_DOWN || key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT) {
            setStatus("Invalid input. Please use W, A, S, D for movement.");
            return;
        }

        if (activeQuestItem != null) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                if (currentInput.equalsIgnoreCase(activeQuestItem.answer)) {
                    activeQuestItem.solved = true;
                    activeQuestItem = null;
                    if (finalBossTriggered) {
                        dialogueSpeaker = "Ghost2";
                        dialogueText = "fine.. hmph... *mutters* here you go...";
                        showDialogue = true;
                        dialogueStep = 30;
                    } else {
                        dialogueSpeaker = "System";
                        dialogueText = "The nite fades away...";
                        showDialogue = true;
                        boolean allDone = true;
                        for (QuestItem item : items) if (!item.solved) allDone = false;
                        if (allDone) { allNitesSolved = true; dialogueStep = 10; }
                    }
                } else { 
                    setStatus("Incorrect! Please check your chemistry coefficients.");
                    currentInput = ""; 
                }
            } else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                if (currentInput.length() > 0) currentInput = currentInput.substring(0, currentInput.length() - 1);
            } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                
                setStatus("You must finish the equation to exit!");
            } else {
                char c = e.getKeyChar();
                
              
                if (!finalBossTriggered && Character.isLetter(c)) {
                    setStatus("Invalid input. Please enter numbers and commas.");
                } else if (Character.isLetterOrDigit(c) || c == ',' || c == '+') {
                    currentInput += c;
                }
            }
            repaint();
            return;
        }

        if (e.getKeyCode() == KeyEvent.VK_E) {
            if (showDialogue) advanceDialogue();
            else if (nearNPC) {
                if (!questStarted) {
                    dialogueSpeaker = "Ghost2";
                    dialogueText = "Let's make it harder shall we? Go find the nite";
                    showDialogue = true;
                    questStarted = true;
                    currentObjective = "Find the Nites (0/3)";
                } else if (allNitesSolved && !finalBossTriggered) {
                    dialogueStep = 20;
                    advanceDialogue();
                }
            } else if (questStarted && !allNitesSolved) {
                boolean itemFound = false;
                for (QuestItem item : items) {
                    if (item.near && !item.solved) {
                        activeQuestItem = item;
                        currentInput = "";
                        itemFound = true;
                        break;
                    }
                }
                if (!itemFound) {
                    
                    setStatus("Nothing to interact with here.");
                }
            }
        }

        switch (e.getKeyCode()) {
            case KeyEvent.VK_W -> up = true;
            case KeyEvent.VK_S -> down = true;
            case KeyEvent.VK_A -> left = true;
            case KeyEvent.VK_D -> right = true;
        }
        repaint();
    }

    private void advanceDialogue() {
        int solvedCount = 0;
        for(QuestItem i : items) if(i.solved) solvedCount++;
        
        if (dialogueStep == 0) {
            showDialogue = false;
            if (questStarted && !allNitesSolved) currentObjective = "Find the Nites (" + solvedCount + "/3)";
        } 
        else if (dialogueStep == 10) {
            dialogueSpeaker = "Me";
            dialogueText = "huh? did she trick me?";
            dialogueStep = 11;
        } else if (dialogueStep == 11) {
            currentObjective = "Confront the ghost";
            showDialogue = false;
            dialogueStep = 0;
        }
        else if (dialogueStep == 20) {
            dialogueSpeaker = "Me";
            dialogueText = "Hey! I cant find it anywhere!?";
            showDialogue = true;
            dialogueStep = 21;
        } else if (dialogueStep == 21) {
            dialogueSpeaker = "Me";
            dialogueText = "Why are you smiling like that?";
            dialogueStep = 22;
        } else if (dialogueStep == 22) {
            dialogueSpeaker = "Ghost2";
            dialogueText = "hehe, you've been fooled.";
            dialogueStep = 23;
        } else if (dialogueStep == 23) {
            dialogueSpeaker = "Ghost2";
            dialogueText = "it was with me all along!";
            dialogueStep = 24;
        } else if (dialogueStep == 24) {
            dialogueSpeaker = "Ghost2";
            dialogueText = "but first...";
            dialogueStep = 25;
        } else if (dialogueStep == 25) {
            showDialogue = false;
            finalBossTriggered = true;
            activeQuestItem = new QuestItem(0,0, "HCl + NaOH -> ?", "NaCl+H2O");
            currentInput = "";
            dialogueStep = 0;
        } 
        else if (dialogueStep == 30) {
            showDialogue = false;
            showNiteSplash = true;
            splashStartTime = System.currentTimeMillis();
            dialogueStep = 0;
        }
    }

    @Override public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W -> up = false;
            case KeyEvent.VK_S -> down = false;
            case KeyEvent.VK_A -> left = false;
            case KeyEvent.VK_D -> right = false;
        }
    }
    @Override public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
       
        try {
            JFrame frame = new JFrame("Chemistry Quest");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new G3_Room2_PD6());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        } catch (Exception e) {
            System.err.println("Critical Launch Error: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "The game failed to start properly.", "Launch Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
