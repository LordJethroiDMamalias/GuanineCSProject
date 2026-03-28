package codes;

/**
 *
 * @author John Felippe Samonte, James Ryan Rios, Angelika Margaret Dumogho
 * 
 * Game objective: When stepping into a bush, a monster might ambush you so you have to answer it's questions to defeat it and move on to the next map.
 * 
 * This game was created with the assistance of Gemini AI. !!! Please read !!!
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.Timer;

abstract class MapEntity {
    private int position;

    public MapEntity(int position) {
        this.position = position;
    }

    public int getPosition() { return position; }
    public void setPosition(int pos) { this.position = pos; }

    public abstract void onInteract(Dialog dialog, JLayeredPane lp, int w, int h); 
}

class BushEvent extends MapEntity {
    public BushEvent(int position) {
        super(position);
    }

    @Override
    public void onInteract(Dialog dialog, JLayeredPane lp, int w, int h) {
        String[] lines = {
            "A monster ambushes you!",
            "Riddle: What has keys but can't open locks?"
        };
        String[] choices = {"Piano", "Keychain", "Map"};
        
        // Actions that happen after you pick a choice
        Runnable[] events = {
            () -> System.out.println("Correct!"), 
            () -> System.out.println("Wrong!"),
            () -> System.out.println("Wrong!")
        };

        dialog.show(lp, lines, choices, events, w, h);
    }
}

class Monster extends MapEntity {
    private ImageIcon icon;

    public Monster(int position, int tileSizeW, int tileSizeH) {
        super(position);
        icon = new ImageIcon(new ImageIcon("Images/monster.png").getImage()
               .getScaledInstance(tileSizeW, tileSizeH, Image.SCALE_SMOOTH));
    }

    public ImageIcon getIcon() { return icon; }

    @Override
    public void onInteract(Dialog dialog, JLayeredPane lp, int w, int h) {
        String[] lines = {"The monster caught you!", "GAME OVER..."};
        dialog.show(lp, lines, null, new Runnable[]{() -> System.exit(0)}, w, h);
    }
}

public class G8_Room2_PD6 implements KeyListener {
    JFrame frame;
    JLayeredPane layeredPane; 
    
    // Using the specific instance as requested
    Dialog dialog = new Dialog(); 
    
    JLabel[] characterLabels;
    int mapWidth = 11;
    int mapHeight = 11;
    int frameWidth = 660;
    int frameHeight = 660;
    int[] mapLayout;
    int characterPosition;
    int characterMode = 0; 
    
    ImageIcon pUp1, pUp2, pRight1, pRight2, pLeft1, pLeft2, pDown1, pDown2, BGimage;
    Timer monsterTimer, gameTimer;
    Monster enemy;
    int survivalTime = 30;
    JLabel timerLabel;

    public G8_Room2_PD6() {
        frame = new JFrame("PD6 - Monster Ambush");
        
        // Setup images and map (keeping your original logic)
        int tileSizeW = frameWidth / mapWidth;
        int tileSizeH = frameHeight / mapHeight;
        BGimage = new ImageIcon(new ImageIcon("Images/PD6BG.png").getImage().getScaledInstance(frameWidth, frameHeight, Image.SCALE_SMOOTH));
        
        // ... (Image Icon loading stays the same) ...
        pDown1 = new ImageIcon(new ImageIcon("Images/down1.png").getImage().getScaledInstance(tileSizeW, tileSizeH, Image.SCALE_SMOOTH));

        mapLayout = new int[]{
            0,0,0,0,0,0,0,0,0,0,0,
            0,2,1,1,1,1,2,1,1,2,0,
            0,1,1,0,1,1,1,1,1,1,0,
            0,2,1,1,1,2,1,1,2,0,0,
            0,1,2,0,1,1,1,2,1,0,0,
            0,1,1,1,1,1,2,0,1,1,0,
            0,1,1,1,1,1,1,1,1,2,0,
            0,1,2,1,0,1,1,2,1,1,0,
            0,1,1,1,2,1,1,1,1,2,0,
            0,1,1,1,1,1,1,1,1,1,0,
            0,0,0,0,0,0,0,0,0,0,0
        };

        characterLabels = new JLabel[mapWidth * mapHeight];
        characterPosition = 60; 
        for (int i = 0; i < characterLabels.length; i++) {
            characterLabels[i] = new JLabel();
            if (i == characterPosition) characterLabels[i].setIcon(pDown1);
        }
    }

    public void setFrame() {
        layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(frameWidth, frameHeight));

        // Background Panel
        JPanel bgPanel = new JPanel(new GridLayout(mapHeight, mapWidth));
        bgPanel.setBounds(0, 0, frameWidth, frameHeight);
        bgPanel.setOpaque(false);
        for (JLabel label : characterLabels) bgPanel.add(label);

        JLabel backgroundLabel = new JLabel(BGimage);
        backgroundLabel.setBounds(0, 0, frameWidth, frameHeight);

        layeredPane.add(backgroundLabel, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(bgPanel, JLayeredPane.PALETTE_LAYER);

        frame.add(layeredPane);
        
        // Apply dialog key listener to the frame
        dialog.addKey(frame);
        frame.addKeyListener(this);

        // UI Timer
        timerLabel = new JLabel("Survive: " + survivalTime + "s");
        timerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        timerLabel.setForeground(Color.WHITE);
        JPanel glass = (JPanel) frame.getGlassPane();
        glass.setVisible(true);
        glass.setLayout(new FlowLayout(FlowLayout.CENTER));
        glass.add(timerLabel);

        frame.setSize(frameWidth, frameHeight);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        startGameTimer();
        startMonsterChase();
    }

    private void move(int newPos, ImageIcon icon1, ImageIcon icon2) {
        // Prevent movement if dialog is open
        if (dialog.isVisible()) return;

        if (newPos >= 0 && newPos < mapLayout.length && mapLayout[newPos] != 0) {
            if (mapLayout[newPos] == 2) {
                new BushEvent(newPos).onInteract(dialog, layeredPane, mapWidth, mapHeight);
            }
            characterLabels[characterPosition].setIcon(null);
            characterPosition = newPos;
            characterLabels[characterPosition].setIcon(icon1);
        }
    }

    private void moveMonster() {
        if (dialog.isVisible()) return; // Pause monster during dialog

        int mPos = enemy.getPosition();
        int pPos = characterPosition;
        int mX = mPos % mapWidth; int mY = mPos / mapWidth;
        int pX = pPos % mapWidth; int pY = pPos / mapWidth;

        int nextPos = mPos;
        if (mX < pX) nextPos++;
        else if (mX > pX) nextPos--;
        else if (mY < pY) nextPos += mapWidth;
        else if (mY > pY) nextPos -= mapWidth;

        if (mapLayout[nextPos] != 0) {
            characterLabels[mPos].setIcon(null);
            enemy.setPosition(nextPos);
            characterLabels[nextPos].setIcon(enemy.getIcon());
        }

        if (enemy.getPosition() == characterPosition) {
            monsterTimer.stop();
            gameTimer.stop();
            enemy.onInteract(dialog, layeredPane, mapWidth, mapHeight);
        }
    }

    public void startGameTimer() {
        gameTimer = new Timer(1000, e -> {
            if (!dialog.isVisible()) {
                survivalTime--;
                timerLabel.setText("Survive: " + survivalTime + "s");
                if (survivalTime <= 0) {
                    gameTimer.stop();
                    monsterTimer.stop();
                    JOptionPane.showMessageDialog(frame, "You survived!");
                    System.exit(0);
                }
            }
        });
        gameTimer.start();
    }

    public void startMonsterChase() {
        enemy = new Monster(12, frameWidth/mapWidth, frameHeight/mapHeight); 
        monsterTimer = new Timer(1000, e -> moveMonster());
        monsterTimer.start();
    }

    @Override public void keyPressed(KeyEvent e) {
        if (dialog.isVisible()) return;
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_UP)    move(characterPosition - mapWidth, pUp1, pUp2);
        if (key == KeyEvent.VK_DOWN)  move(characterPosition + mapWidth, pDown1, pDown2);
        if (key == KeyEvent.VK_LEFT)  move(characterPosition - 1, pLeft1, pLeft2);
        if (key == KeyEvent.VK_RIGHT) move(characterPosition + 1, pRight1, pRight2);
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new G8_Room2_PD6().setFrame());
    }
}