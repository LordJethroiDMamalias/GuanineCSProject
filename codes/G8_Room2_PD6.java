package codes;

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

class BushEvent extends MapEntity {
    public BushEvent(int position) { super(position); }
    @Override
    public void onInteract(G8_Dialog dialog, JLayeredPane lp, int w, int h) {
        dialog.show(lp, new String[]{ "[You searched the wires... nothing but cords.]" }, null, null, w, h, null);
    }
}

// ── Monster (Gigglebot) ───────────────────────────────────────────────────────
class Monster extends MapEntity {
    private ImageIcon icon;
    public Monster(int position, int tw, int th) {
        super(position);
        this.icon = loadSafeIcon("images/G8_Gigglebot3000.png", tw, th);
    }

    private ImageIcon loadSafeIcon(String path, int w, int h) {
        try {
            ImageIcon temp = new ImageIcon(path);
            if (temp.getIconWidth() <= 0) temp = new ImageIcon(path.replace("G8_", ""));
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

// ── G8_Room2_PD6 Main Class ──────────────────────────────────────────────────
public class G8_Room2_PD6 implements KeyListener {

    JFrame frame;
    JLayeredPane layeredPane;
    G8_Dialog dialog = new G8_Dialog();
    Battle battle = new Battle();

    JLabel[] characterLabels;
    JLabel[] monsterLabels; 
    int mapWidth = 11, mapHeight = 11;
    int frameWidth = 660, frameHeight = 660;
    public int[] mapLayout;
    public int characterPosition;

    int walkFrame = 0;
    int direction = 1; // 0:Up, 1:Down, 2:Left, 3:Right
    ImageIcon pUp1, pUp2, pDown1, pDown2, pLeft1, pLeft2, pRight1, pRight2, BGimage;

    Timer monsterTimer, gameTimer;
    Monster enemy;
    int survivalTime = 30;
    JLabel timerLabel;
    
    private JLabel cutscene;
    private boolean battleTriggered = false;

    public G8_Room2_PD6() {
        frame = new JFrame("PD6 - Gigglebot Battle");
        int tw = frameWidth / mapWidth;
        int th = frameHeight / mapHeight;

        BGimage = loadSafeIcon("images/G8_PD6BG.png", frameWidth, frameHeight);
        pDown1 = loadSafeIcon("images/G8_down1.png", tw, th);
        pDown2 = loadSafeIcon("images/G8_down2.png", tw, th);
        pUp1   = loadSafeIcon("images/G8_up1.png", tw, th);
        pUp2   = loadSafeIcon("images/G8_up2.png", tw, th);
        pLeft1 = loadSafeIcon("images/G8_left1.png", tw, th);
        pLeft2 = loadSafeIcon("images/G8_left2.png", tw, th);
        pRight1= loadSafeIcon("images/G8_right1.png", tw, th);
        pRight2= loadSafeIcon("images/G8_right2.png", tw, th);

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
            monsterLabels[i] = new JLabel();
            monsterLabels[i].setHorizontalAlignment(SwingConstants.CENTER);
        }
        characterLabels[characterPosition].setIcon(pDown1);
    }

    private ImageIcon loadSafeIcon(String path, int w, int h) {
        try {
            ImageIcon icon = new ImageIcon(path);
            if (icon.getIconWidth() <= 0 && path.contains("G8_")) 
                icon = new ImageIcon(path.replace("G8_", ""));
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

        JPanel charGrid = new JPanel(new GridLayout(mapHeight, mapWidth));
        charGrid.setBounds(0, 0, frameWidth, frameHeight);
        charGrid.setOpaque(false);
        for (JLabel lbl : characterLabels) charGrid.add(lbl);

        JPanel monsterGrid = new JPanel(new GridLayout(mapHeight, mapWidth));
        monsterGrid.setBounds(0, 0, frameWidth, frameHeight);
        monsterGrid.setOpaque(false);
        for (JLabel lbl : monsterLabels) monsterGrid.add(lbl);

        JLabel background = new JLabel(BGimage);
        background.setBounds(0, 0, frameWidth, frameHeight);

        layeredPane.add(background, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(charGrid, JLayeredPane.PALETTE_LAYER);
        layeredPane.add(monsterGrid, JLayeredPane.MODAL_LAYER);
        layeredPane.add(cutscene, JLayeredPane.DRAG_LAYER); 

        frame.add(layeredPane);
        frame.addKeyListener(this);
        dialog.addKey(frame); 

        timerLabel = new JLabel("Survive: " + survivalTime + "s");
        timerLabel.setFont(new Font("Monospaced", Font.BOLD, 22));
        timerLabel.setForeground(Color.CYAN);
        JPanel glass = (JPanel) frame.getGlassPane();
        glass.setVisible(true);
        glass.setLayout(new FlowLayout(FlowLayout.RIGHT));
        glass.add(timerLabel);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        showWelcomeMessage();
    }

    public void startMonsterChase() {
        enemy = new Monster(12, frameWidth / mapWidth, frameHeight / mapHeight);
        if (enemy.getIcon() != null) monsterLabels[12].setIcon(enemy.getIcon());
        monsterTimer = new Timer(700, e -> moveMonster());
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
            enemy.setPosition(nextPos);
            monsterLabels[nextPos].setIcon(enemy.getIcon());
            monsterLabels[nextPos].repaint();
        }

        if (enemy.getPosition() == characterPosition) {
            stopGameLogic();
            enemy.onCatch(dialog, layeredPane, mapWidth, mapHeight, this::restartRoom);
        }
    }

    private void stopGameLogic() {
        if (monsterTimer != null) monsterTimer.stop();
        if (gameTimer != null) gameTimer.stop();
    }

    private void restartRoom() {
        frame.dispose();
        new G8_Room2_PD6().setFrame();
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
                    if (tile == 3 && !battleTriggered) { triggerBossBattleSequence(); }
                    else if (tile != 0) {
                        characterLabels[characterPosition].setIcon(null);
                        characterPosition = nextPos;
                        walkFrame = (walkFrame + 1) % 2;
                        updateCharacterIcon();
                    }
                }
            }
        }
    }

    private void updateCharacterIcon() {
        ImageIcon current = switch (direction) {
            case 0 -> (walkFrame == 0) ? pUp1 : pUp2;
            case 1 -> (walkFrame == 0) ? pDown1 : pDown2;
            case 2 -> (walkFrame == 0) ? pLeft1 : pLeft2;
            case 3 -> (walkFrame == 0) ? pRight1 : pRight2;
            default -> pDown1;
        };
        characterLabels[characterPosition].setIcon(current);
        characterLabels[characterPosition].repaint();
    }

    private void triggerBossBattleSequence() {
        battleTriggered = true;
        stopGameLogic();
        
        showCutscene("images/PDs game/map2/cutscn1.png", 4000, () -> {
            G8_Room1_PD4.playMusic("music/GIGGLEBOT3000.wav");
            battle.start(frame, "images/G8_PD6BG.png", "GIGGLEBOT3000");

            Timer battleMonitor = new Timer(500, null);
            battleMonitor.addActionListener(ev -> {
                if (!Battle.paused) {
                    battleMonitor.stop();
                    if (battle.hp <= 0) handleVictory();
                    else restartRoom();
                }
            });
            battleMonitor.start();
        });
    }

    private void handleVictory() {
        saveProgress();
        showCutscene("images/PDs game/map2/cutscn2.png", 5000, this::showVictoryDialog);
    }

    private void showCutscene(String path, int duration, Runnable onComplete) {
        ImageIcon img = loadSafeIcon(path, frameWidth, frameHeight);
        if (img != null) {
            cutscene.setIcon(img);
            cutscene.setVisible(true);
            layeredPane.moveToFront(cutscene);
            
            Timer t = new Timer(duration, e -> {
                ((Timer)e.getSource()).stop();
                cutscene.setVisible(false);
                onComplete.run();
            });
            t.setRepeats(false);
            t.start();
        } else {
            onComplete.run();
        }
    }

    public void showWelcomeMessage() {
        G8_Room1_PD4.playMusic("music/GIGGLEBOT3000.wav");
        dialog.show(layeredPane, new String[]{
            "Welcome dear traveller, you have been trapped >:)",
            "Here you must survive and run away from Gigglebot.",
            "GIGGLEBOT3000: ESCAPE IS ELECTRICALLY IMPROBABLE"
        }, null, null, mapWidth, mapHeight, () -> {
            startGameTimer();
            startMonsterChase();
        });
    }

    public void startGameTimer() {
        gameTimer = new Timer(1000, e -> {
            if (dialog.isVisible() || Battle.paused || cutscene.isVisible()) return;
            survivalTime--;
            timerLabel.setText("Survive: " + survivalTime + "s");
            if (survivalTime <= 0) {
                stopGameLogic();
                showSurvivedCutscene();
            }
        });
        gameTimer.start();
    }

    private void showSurvivedCutscene() {
        dialog.show(layeredPane, new String[]{
            "You survived the onslaught!",
            "But something grabs you from behind...",
            "GIGGLEBOT3000: SPECIMEN ACQUIRED. TRANSPORTING TO LAB."
        }, null, null, mapWidth, mapHeight, () -> {
            timerLabel.setText("");
            triggerBossBattleSequence();
        });
    }

    private void showVictoryDialog() {
        dialog.show(layeredPane, new String[]{
            "GIGGLEBOT3000 sparks and collapses.",
            "Silence. Finally.",
            "A helicopter arrives to take you to the clouds...",
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
