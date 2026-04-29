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
 */
public class G3_Room1_PD4 extends JPanel implements ActionListener, KeyListener, MouseListener {

    private static Clip bgmClip = null;
    JFrame frame;

    int playerX = 330, playerY = 528;   
    final int TILE_SIZE = 40;

    ImageIcon[] pUp = new ImageIcon[4], pDown = new ImageIcon[4], pLeft = new ImageIcon[4], pRight = new ImageIcon[4];
    int walkFrame = 0;
    int direction = 1; 

    Image bg, npcSprite, niteIcon, light1, light2, light3;
    Timer timer = new Timer(16, this);
    ArrayList<Rectangle> solidBoxes = new ArrayList<>();

    Rectangle npcBox         = new Rectangle(317, 330, 26, 70);
    Rectangle npcInteractBox = new Rectangle(270, 300, 120, 150);

    boolean nearNPC = false, nearItem = false, showDialogue = false, questStarted = false, allNitesSolved = false;

    String currentObjective = "Find the Ghost";
    String dialogueText = "", dialogueSpeaker = "Ghost";

    class QuestItem {
        Rectangle bounds;
        String equation, answer;
        Image fragmentImg;
        boolean solved = false;

        QuestItem(int x, int y, String eq, String ans, Image img) {
            this.bounds = new Rectangle(x, y, 149, 198);
            this.equation = eq;
            this.answer = ans;
            this.fragmentImg = img;
        }
    }

    ArrayList<QuestItem> items = new ArrayList<>();
    QuestItem activeQuestItem = null;
    String currentInput = "";

    public G3_Room1_PD4() {
        frame = new JFrame("Chemistry Quest");

        setPreferredSize(new Dimension(660, 660));
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);

        try {
            for (int i = 0; i < 4; i++) {
                pUp[i]    = scalePlayer("images/up_" + (i + 1) + ".png");
                pDown[i]  = scalePlayer("images/down_" + (i + 1) + ".png");
                pLeft[i]  = scalePlayer("images/left_" + (i + 1) + ".png");
                pRight[i] = scalePlayer("images/right_" + (i + 1) + ".png");
            }
            bg = new ImageIcon("images/G3_background.png").getImage();
            npcSprite = new ImageIcon("images/Celene.png").getImage();
            light1 = new ImageIcon("images/G3_light1.png").getImage();
            light2 = new ImageIcon("images/G3_light2.png").getImage();
            light3 = new ImageIcon("images/G3_light3.png").getImage();
        } catch (Exception e) {}

        solidBoxes.add(new Rectangle(256, 0, 17, 264));
        solidBoxes.add(new Rectangle(256, 308, 17, 242));
        solidBoxes.add(npcBox);

        items.add(new QuestItem(66, 110, "2Mg + O2 -> 2MgO", "synthesis", light1));
        items.add(new QuestItem(256, 110, "2H2O -> 2H2 + O2", "decomposition", light2));
        items.add(new QuestItem(446, 110, "CH4 + 2O2 -> CO2 + 2H2O", "combustion", light3));

        System.out.println("Room memory allocation sequence finished.");
        loadSaveData();
        saveProgress();
        
        timer.start();
    }

    private void loadSaveData() {
        SaveSystem.SaveData save = SaveSystem.loadGame("G3_Room1_PD4");
        SaveSystem.startTimer(save.timeSeconds);

        allNitesSolved = save.hasFlag("quest_complete");
        if (allNitesSolved) {
            currentObjective = "Quest Complete!";
            for (QuestItem item : items) item.solved = true;
        }

        System.out.println("[Room1] Loaded — Quest:" + allNitesSolved + " Time:" + save.formattedTime());
    }

    private void saveProgress() {
        SaveSystem.saveGame(
            new SaveSystem.SaveData.Builder("G3_Room1_PD4")
                .time(SaveSystem.getTotalSeconds())
                .flag(allNitesSolved ? "quest_complete" : null)
                .battles(SaveSystem.getDefeatedBosses())
        );
    }

    public void setFrame() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(this);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setResizable(false);
        playFedoraMusic("music/Celene.wav");
    }

    /**
     * Transition logic following G2's pattern
     */
    private void transitionToRoom2() {
        saveProgress(); 
        SwingUtilities.invokeLater(() -> {
            try {
                if (bgmClip != null) {
                    bgmClip.stop();
                    bgmClip.close();
                }
                frame.dispose();
                // Load the next room
                G3_Room2_PD6 room2 = new G3_Room2_PD6();
                room2.setFrame();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null,
                    "Failed to load Room 2: " + ex.getMessage(),
                    "Transition Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
    }

    private ImageIcon scalePlayer(String path) {
        ImageIcon icon = new ImageIcon(path);
        return new ImageIcon(icon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH));
    }

    private void attemptMove(int dx, int dy) {
        if (activeQuestItem != null || showDialogue) return;
        int nextX = playerX + dx, nextY = playerY + dy;
        if (nextX < 0 || nextX > 628 || nextY < 0 || nextY > 596) return;

        Rectangle nextHitbox = new Rectangle(nextX + 9, nextY + 53, 13, 10);
        for (Rectangle box : solidBoxes) if (nextHitbox.intersects(box)) return;
        
        playerX = nextX; playerY = nextY;
        walkFrame = (walkFrame + 1) % 4;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Rectangle playerHitbox = new Rectangle(playerX + 9, playerY + 53, 13, 10);
        nearNPC = playerHitbox.intersects(npcInteractBox);

        nearItem = false;
        if (questStarted && !allNitesSolved) {
            Rectangle interactZone = new Rectangle(playerX - 40, playerY - 40, 112, 144);
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
        Graphics2D g3 = (Graphics2D) g;
        if (bg != null) g.drawImage(bg, 0, 0, 660, 660, null);
        if (npcSprite != null) g.drawImage(npcSprite, npcBox.x, npcBox.y, npcBox.width, npcBox.height, null);

        ImageIcon sprite = switch (direction) {
            case 0 -> pUp[walkFrame];
            case 2 -> pLeft[walkFrame];
            case 3 -> pRight[walkFrame];
            default -> pDown[walkFrame];
        };
        if (sprite != null) g.drawImage(sprite.getImage(), playerX, playerY, null);

        if (questStarted) {
            for (QuestItem item : items) {
                if (!item.solved && item.fragmentImg != null) {
                    float pulse = (float)(Math.sin(System.currentTimeMillis() / 400.0) * 0.2 + 0.8);
                    g3.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pulse));
                    g.drawImage(item.fragmentImg, item.bounds.x, item.bounds.y, item.bounds.width, item.bounds.height, null);
                }
            }
        }
        g3.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

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
            g.fillRoundRect(230, 385, 200, 35, 10, 10);
            g.setColor(Color.WHITE);
            g.drawRoundRect(230, 385, 200, 35, 10, 10);
            g.setColor(Color.YELLOW);
            g.setFont(new Font("SansSerif", Font.BOLD, 14));
            String prompt = "[SPACE] to interact";
            int stringWidth = g.getFontMetrics().stringWidth(prompt);
            g.drawString(prompt, 230 + (200 - stringWidth) / 2, 408);
        }
    }

    private void drawDialogueBox(Graphics g, String speaker, String text) {
        g.setColor(new Color(0, 0, 0, 230));
        g.fillRoundRect(20, 500, 620, 130, 20, 20);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        g.drawString(speaker + ":", 40, 530);
        g.setFont(new Font("Serif", Font.ITALIC, 20));
        g.drawString(text, 40, 570);
    }

    private void drawQuizBox(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 660, 660);
        g.setColor(Color.CYAN);
        g.setFont(new Font("Monospaced", Font.BOLD, 30));
        g.drawString("Identify the Reaction:", 100, 150);
        g.setColor(Color.WHITE);
        g.drawString(activeQuestItem.equation, 100, 250);
        g.setColor(Color.GREEN);
        g.drawString("> " + currentInput + "_", 100, 400);
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
                    currentObjective = "Collect 3 Fragments"; 
                    saveProgress();
                } else if (allNitesSolved) {
                    // Transition after final quest-complete dialogue is closed
                    transitionToRoom2();
                }
            } else if (nearNPC) {
                dialogueSpeaker = "Ghost";
                dialogueText = allNitesSolved ? "Thank you... we are whole again." : "Help her... find the 3 fragments.";
                showDialogue = true;
            } else if (nearItem) {
                Rectangle interactZone = new Rectangle(playerX - 40, playerY - 40, 112, 144);
                for (QuestItem item : items) {
                    if (!item.solved && interactZone.intersects(item.bounds)) {
                        activeQuestItem = item;
                        currentInput = "";
                        break;
                    }
                }
            }
        } else {
            if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP) { direction = 0; attemptMove(0, -TILE_SIZE); }
            else if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN) { direction = 1; attemptMove(0, TILE_SIZE); }
            else if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT) { direction = 2; attemptMove(-TILE_SIZE, 0); }
            else if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) { direction = 3; attemptMove(TILE_SIZE, 0); }
        }
    }

    private void handleQuizInput(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_ENTER) {
            if (currentInput.trim().equalsIgnoreCase(activeQuestItem.answer)) {
                activeQuestItem.solved = true;
                activeQuestItem = null;
                checkAllPieces();
                saveProgress();
            } else {
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
            currentObjective = "Quest Complete!"; 
            // Show dialogue informing user they are done
            dialogueSpeaker = "Ghost";
            dialogueText = "Thank you... We are whole again.";
            showDialogue = true;
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Point click = e.getPoint();
        if (showDialogue) { showDialogue = false; return; }
        
        if (npcInteractBox.contains(click)) {
            dialogueSpeaker = "Ghost";
            dialogueText = "Please... the fragments are nearby.";
            showDialogue = true;
        }

        if (questStarted && !allNitesSolved) {
            for (QuestItem item : items) {
                if (!item.solved && item.bounds.contains(click)) {
                    activeQuestItem = item;
                    currentInput = "";
                    repaint();
                    break;
                }
            }
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new G3_Room1_PD4().setFrame();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    private static void playFedoraMusic(String location) {
        try {
            File musicPath = new File(location);
            if (musicPath.exists()) {
                AudioInputStream ai = AudioSystem.getAudioInputStream(musicPath);
                bgmClip = AudioSystem.getClip();
                bgmClip.open(ai);
                bgmClip.start();
                bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
            }
        } catch (Exception e) {}
    }
}