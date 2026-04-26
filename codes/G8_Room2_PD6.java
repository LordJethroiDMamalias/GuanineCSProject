package codes;

import static codes.G8_Room1_PD4.stopMusic;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.Timer;

// ── MapEntity ─────────────────────────────────────────────────────────────────
abstract class MapEntity {
    private int position;
    public MapEntity(int position)   { this.position = position; }
    public int  getPosition()        { return position; }
    public void setPosition(int pos) { this.position = pos; }
    public abstract void onInteract(G8_Dialog dialog, JLayeredPane lp, int w, int h);
}

// ── BushEvent ─────────────────────────────────────────────────────────────────
class BushEvent extends MapEntity {
    public BushEvent(int position) { super(position); }
    @Override
    public void onInteract(G8_Dialog dialog, JLayeredPane lp, int w, int h) {
        dialog.show(lp, new String[]{ "[You searched the wires... nothing but cords.]" }, null, null, w, h, null);
    }
}

// ── Monster ───────────────────────────────────────────────────────────────────
class Monster extends MapEntity {
    private ImageIcon icon;
    public Monster(int position, int tw, int th) {
        super(position);
        // This looks specifically for the Gigglebot image
        this.icon = loadSafeIcon("images/G8_Gigglebot3000.png", tw, th);
    }

    private ImageIcon loadSafeIcon(String path, int w, int h) {
        try {
            ImageIcon temp = new ImageIcon(path);
            // Fallback: try without G8_ if the first path fails
            if (temp.getIconWidth() <= 0 && path.contains("G8_")) {
                temp = new ImageIcon(path.replace("G8_", ""));
            }
            if (temp.getIconWidth() <= 0) return null; 
            return new ImageIcon(temp.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
        } catch (Exception e) { return null; }
    }

    public ImageIcon getIcon() { return icon; }
    
    public void onCatch(G8_Dialog dialog, JLayeredPane lp, int w, int h, Runnable onRestart) {
        dialog.show(lp, new String[]{ "CAUGHT!", "The Gigglebot was too fast for you...", "GAME OVER — restarting room." }, null, null, w, h, onRestart);
    }
    @Override public void onInteract(G8_Dialog dialog, JLayeredPane lp, int w, int h) {}
}

// ── G8_Room2_PD6 ──────────────────────────────────────────────────────────────
public class G8_Room2_PD6 implements KeyListener {

    JFrame       frame;
    JLayeredPane layeredPane;
    G8_Dialog    dialog = new G8_Dialog();
    Battle       battle = new Battle();

    JLabel[] characterLabels;
    JLabel[] monsterLabels; 
    int mapWidth = 11, mapHeight = 11;
    int frameWidth = 660, frameHeight = 660;
    public int[] mapLayout;
    public int   characterPosition;

    // Animation variables: cycles 1, 2, 3, 4
    int walkFrame = 1; 
    int direction = 1; 
    ImageIcon[] upFrames = new ImageIcon[5];
    ImageIcon[] downFrames = new ImageIcon[5];
    ImageIcon[] leftFrames = new ImageIcon[5];
    ImageIcon[] rightFrames = new ImageIcon[5];
    ImageIcon BGimage;

    Timer   monsterTimer, gameTimer;
    Monster enemy;
    int      survivalTime = 30;
    JLabel  timerLabel;
    
    private JLabel cutscene;
    private boolean battleTriggered = false;

    public G8_Room2_PD6() {
        frame = new JFrame("PD6 - Gigglebot Battle");
        int tw = frameWidth / mapWidth;
        int th = frameHeight / mapHeight;

        BGimage = loadSafeIcon("images/G8_PD6BG.png", frameWidth, frameHeight);
        
        // Load frames 1 through 4 for each direction
        for (int i = 1; i <= 4; i++) {
            upFrames[i]    = loadSafeIcon("images/up_" + i + ".png", tw, th);
            downFrames[i]  = loadSafeIcon("images/down_" + i + ".png", tw, th);
            leftFrames[i]  = loadSafeIcon("images/left_" + i + ".png", tw, th);
            rightFrames[i] = loadSafeIcon("images/right_" + i + ".png", tw, th);
        }

        mapLayout = new int[]{
            0,0,0,0,0,0,0,0,0,0,0,
            0,0,1,1,1,1,1,1,1,0,0,
            0,1,1,1,1,1,1,1,1,1,0,
            0,1,1,0,0,1,2,0,1,1,0,
            0,1,1,1,1,1,1,2,0,1,0,
            0,1,0,2,1,1,0,0,1,1,0,
            0,1,0,0,0,1,1,1,1,1,0,
            0,1,1,1,1,1,1,1,0,3,0,
            0,1,0,0,1,1,0,0,1,1,0,
            0,1,1,1,1,3,1,1,1,1,0,
            0,0,0,0,0,0,0,0,0,0,0
        };

        characterLabels = new JLabel[mapWidth * mapHeight];
        monsterLabels = new JLabel[mapWidth * mapHeight];
        characterPosition = 60;
        
        for (int i = 0; i < characterLabels.length; i++) {
            characterLabels[i] = new JLabel();
            characterLabels[i].setHorizontalAlignment(SwingConstants.CENTER);
            if (i == characterPosition) {
                characterLabels[i].setIcon(downFrames[1]); // Initial pose
            }
            monsterLabels[i] = new JLabel();
            monsterLabels[i].setHorizontalAlignment(SwingConstants.CENTER);
        }
    }

    private ImageIcon loadSafeIcon(String path, int w, int h) {
        try {
            ImageIcon icon = new ImageIcon(path);
            // Check for G8_ prefix variation
            if (icon.getIconWidth() <= 0) {
                String altPath = path.contains("G8_") ? path.replace("G8_", "") : path.replace("images/", "images/G8_");
                icon = new ImageIcon(altPath);
            }
            if (icon.getIconWidth() <= 0) return null; 
            return new ImageIcon(icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
        } catch (Exception e) { return null; }
    }

    public void setFrame() {
        layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(frameWidth, frameHeight));

        cutscene = new JLabel();
        cutscene.setBounds(0, 0, frameWidth, frameHeight);
        cutscene.setVisible(false);

        // Monster layer - Ensure this is initialized early
        JPanel monsterGrid = new JPanel(new GridLayout(mapHeight, mapWidth));
        monsterGrid.setBounds(0, 0, frameWidth, frameHeight);
        monsterGrid.setOpaque(false);
        for (JLabel lbl : monsterLabels) monsterGrid.add(lbl);

        // Character layer
        JPanel charGrid = new JPanel(new GridLayout(mapHeight, mapWidth));
        charGrid.setBounds(0, 0, frameWidth, frameHeight);
        charGrid.setOpaque(false);
        for (JLabel lbl : characterLabels) charGrid.add(lbl);

        JLabel background = new JLabel(BGimage);
        background.setBounds(0, 0, frameWidth, frameHeight);

        // STACKING ORDER: Background (0) -> Character (100) -> Monster (200) -> Cutscene (300)
        layeredPane.add(background, Integer.valueOf(0));
        layeredPane.add(charGrid, Integer.valueOf(100));
        layeredPane.add(monsterGrid, Integer.valueOf(200));
        layeredPane.add(cutscene, Integer.valueOf(300));

        frame.add(layeredPane);
        frame.addKeyListener(this);
        dialog.addKey(frame); 

        timerLabel = new JLabel("Survive: " + survivalTime + "s");
        timerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        timerLabel.setForeground(Color.WHITE);
        JPanel glass = (JPanel) frame.getGlassPane();
        glass.setVisible(true);
        glass.setLayout(new FlowLayout(FlowLayout.CENTER));
        glass.add(timerLabel);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        // Brief delay before starting to ensure the window is actually rendered
        Timer startDelay = new Timer(1000, e -> {
            showWelcomeMessage();
            startGameTimer();
            startMonsterChase(); 
        });
        startDelay.setRepeats(false);
        startDelay.start();
    }

    public void startMonsterChase() {
        // Monster starts at index 12
        enemy = new Monster(12, frameWidth / mapWidth, frameHeight / mapHeight);
        
        if (enemy.getIcon() != null) {
            monsterLabels[12].setIcon(enemy.getIcon());
        } else {
            // If the image is STILL missing, this RED box proves the code is working
            monsterLabels[12].setOpaque(true);
            monsterLabels[12].setBackground(Color.RED);
            System.out.println("DEBUG: Monster image not found, showing red square.");
        }
        
        // Force the UI to show the monster immediately
        monsterLabels[12].revalidate();
        monsterLabels[12].repaint();

        if (monsterTimer != null) monsterTimer.stop();
        monsterTimer = new Timer(800, e -> moveMonster());
        monsterTimer.start();
    }

    private void moveMonster() {
        if (dialog.isVisible() || Battle.paused || enemy == null || cutscene.isVisible()) return;
        int mPos = enemy.getPosition();
        int nextPos = mPos;
        
        if (mPos % mapWidth < characterPosition % mapWidth) nextPos++;
        else if (mPos % mapWidth > characterPosition % mapWidth) nextPos--;
        else if (mPos / mapWidth < characterPosition / mapWidth) nextPos += mapWidth;
        else if (mPos / mapWidth > characterPosition / mapWidth) nextPos -= mapWidth;

        if (nextPos != mPos && nextPos >= 0 && nextPos < mapLayout.length && mapLayout[nextPos] != 0) {
            monsterLabels[mPos].setIcon(null);
            monsterLabels[mPos].setOpaque(false);
            enemy.setPosition(nextPos);
            
            if (enemy.getIcon() != null) {
                monsterLabels[nextPos].setIcon(enemy.getIcon());
            } else {
                monsterLabels[nextPos].setOpaque(true);
                monsterLabels[nextPos].setBackground(Color.RED);
            }
        }

        if (enemy != null && enemy.getPosition() == characterPosition) {
            monsterTimer.stop(); 
            gameTimer.stop();
            Monster caughtEnemy = enemy;
            enemy = null;
            caughtEnemy.onCatch(dialog, layeredPane, mapWidth, mapHeight, () -> {
                frame.dispose();
                new G8_Room2_PD6().setFrame();
            });
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (Battle.paused || dialog.isVisible() || cutscene.isVisible()) return;
        int move = 0;
        int key = e.getKeyCode();
        if      (key == KeyEvent.VK_UP    || key == KeyEvent.VK_W) { direction = 0; move = -mapWidth; }
        else if (key == KeyEvent.VK_DOWN  || key == KeyEvent.VK_S) { direction = 1; move =  mapWidth; }
        else if (key == KeyEvent.VK_LEFT  || key == KeyEvent.VK_A) { direction = 2; move = -1; }
        else if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) { direction = 3; move =  1; }
        
        if (key == KeyEvent.VK_SPACE) {
            int interactPos = characterPosition + (direction == 0 ? -mapWidth : direction == 1 ? mapWidth : direction == 2 ? -1 : 1);
            if (interactPos >= 0 && interactPos < mapLayout.length && mapLayout[interactPos] == 2) {
                new BushEvent(interactPos).onInteract(dialog, layeredPane, mapWidth, mapHeight);
            }
            return;
        }

        if (move != 0) {
            int nextPos = characterPosition + move;
            if (nextPos >= 0 && nextPos < mapLayout.length) {
                boolean isWrapping = (Math.abs(move) == 1 && (nextPos / mapWidth != characterPosition / mapWidth));
                if (!isWrapping) {
                    int tile = mapLayout[nextPos];
                    if (tile == 3 && !battleTriggered) { triggerBossBattleSequence(nextPos); }
                    else if (tile != 0) {
                        characterLabels[characterPosition].setIcon(null);
                        characterPosition = nextPos;
                        
                        // Increment frame 1 -> 2 -> 3 -> 4 -> 1
                        walkFrame = (walkFrame % 4) + 1;
                        updateCharacterIcon();
                    }
                }
            }
        }
    }

    private void updateCharacterIcon() {
        ImageIcon current = switch (direction) {
            case 0 -> upFrames[walkFrame];
            case 1 -> downFrames[walkFrame];
            case 2 -> leftFrames[walkFrame];
            case 3 -> rightFrames[walkFrame];
            default -> downFrames[1];
        };
        // Safety check if images failed to load
        if (current == null) {
            characterLabels[characterPosition].setText("P"); // Placeholder
        } else {
            characterLabels[characterPosition].setIcon(current);
        }
    }

    private void triggerBossBattleSequence(int pos) {
        battleTriggered = true;
        if (gameTimer != null) gameTimer.stop();
        if (monsterTimer != null) monsterTimer.stop();
        ImageIcon introImg = loadSafeIcon("images/PDs game/map2/cutscn1.png", frameWidth, frameHeight);
        if (introImg != null) cutscene.setIcon(introImg);
        cutscene.setVisible(true);
        new Timer(500, e -> {
            ((Timer) e.getSource()).stop();
            cutscene.setVisible(false);
            stopMusic();
            G8_Room1_PD4.playMusic("music/GIGGLEBOT3000-boss.wav");
            battle.start(frame, "images/G8_PD6BG.png", "GIGGLEBOT3000");
            new Timer(500, ev -> {
                if (!Battle.paused) {
                    ((Timer) ev.getSource()).stop();
                    if (battle.hp > 0) { 
                        saveProgress();
                        ImageIcon victoryImg = loadSafeIcon("images/PDs game/map2/cutscn2.png", frameWidth, frameHeight);
                        if (victoryImg != null) cutscene.setIcon(victoryImg);
                        cutscene.setVisible(true);
                        new Timer(5000, end -> {
                            ((Timer) end.getSource()).stop();
                            cutscene.setVisible(false);
                            showVictoryCutscene();
                        }).start();
                    } else {
                        battleTriggered = false;
                        battle = new Battle();
                        characterLabels[characterPosition].setIcon(null);
                        characterPosition = 65; 
                        direction = 1;
                        updateCharacterIcon();
                        startGameTimer();
                        startMonsterChase();
                    }
                }
            }).start();
        }).start();
    }

    public void showWelcomeMessage() {
        G8_Room1_PD4.playMusic("music/GIGGLEBOT3000.wav");
        dialog.show(layeredPane, new String[]{
            "Welcome dear traveller, you have been trapped >:), ",
            "Here you must survive and run away from Gigglebot",
            "See what happens when you get near my minions!.",
            "GIGGLEBOT3000: ESCAPE IS ELECTRICALLY IMPROBABLE"
        }, null, null, mapWidth, mapHeight, null);
    }

    public void startGameTimer() {
        gameTimer = new Timer(1000, e -> {
            if (dialog.isVisible() || Battle.paused || cutscene.isVisible()) return;
            survivalTime--;
            timerLabel.setText("Survive: " + survivalTime + "s");
            if (survivalTime <= 0) {
                gameTimer.stop(); monsterTimer.stop();
                showSurvivedCutscene();
            }
        });
        gameTimer.start();
    }

    private void showSurvivedCutscene() {
        dialog.show(layeredPane, new String[]{
            "You survived the onslaught!",
            "But something grabs you from behind...",
            "GIGGLEBOT3000: SPECIMEN ACQUIRED. TRANSPORTING TO LAB.",
            "Everything goes dark as you are dragged through a metal corridor.",
            "You wake up... somewhere different. The lab."
        }, null, null, mapWidth, mapHeight, () -> { timerLabel.setText(""); });
    }

    private void showVictoryCutscene() {
        dialog.show(layeredPane, new String[]{
            "GIGGLEBOT3000 sparks and collapses.",
            "Silence. Finally.",
            "A door at the far end slides open, you see a helicopter and take it to the clouds...",
            "[END OF Gigglebot — next area loading...]"
        }, null, null, mapWidth, mapHeight, () -> {
            JOptionPane.showMessageDialog(frame, "PD6 complete!");
            System.exit(0);
        });
    }

    private void saveProgress() {
        SaveSystem.saveGame(new SaveSystem.SaveData.Builder("G8_Room2_PD6").battles(SaveSystem.getDefeatedBosses()));
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new G8_Room2_PD6().setFrame());
    }
}
