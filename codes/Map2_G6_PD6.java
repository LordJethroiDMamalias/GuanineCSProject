//Guanine Group 6: Estrellan, Gomez, Pañares
package GuanineCSProject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class Map2_G6_PD6 implements KeyListener {

    JFrame frame;
    JPanel topPanel;
    JLabel timerLabel, pointsLabel;

    ImageIcon wall, ground, pt1, pt2, pt3;
    ImageIcon icon, icon2, icon3, icon4, icon5, icon6, icon7;

    JLabel[] tiles;
    JLabel[] chr;
    ImageIcon[] baseTiles;

    int[] mapLayout;

    int mapw = 11, maph = 11;
    int frameWidth = 660, frameHeight = 720;
    int tileSize;

    Player player = new Player();

    int chrmodeR, chrmodeL, chrmodeD;

    Timer timer;
    int timeLeft = 3;
    boolean timerStarted = false;

    Random random = new Random();

    String[] questions = {
        "Symbol for sodium?\nA So\nB S\nC Na\nD N",
        "Particle in nucleus?\nA Electron\nB Proton\nC Photon\nD Ion",
        "Charge of neutron?\nA +1\nB -1\nC 0\nD +2",
        "Atomic number of carbon?\nA 6\nB 12\nC 14\nD 8",
        "pH of pure water?\nA 0\nB 7\nC 14\nD 1",
        "Gas released in photosynthesis?\nA O2\nB CO2\nC N2\nD H2",
        "Formula of water?\nA H2O\nB HO2\nC H2O2\nD OH",
        "Strongest acid?\nA HCl\nB H2SO4\nC CH3COOH\nD HNO3",
        "Covalent bond involves?\nA Electrons shared\nB Electrons transferred\nC Protons shared\nD Neutrons shared",
        "Alkali metals are in group?\nA 1\nB 2\nC 17\nD 18",
        "Atomic mass of hydrogen?\nA 1\nB 2\nC 3\nD 4",
        "Which is noble gas?\nA O\nB N\nC He\nD Cl",
        "What is the formula for table salt?\nA NaCl\nB KCl\nC CaCl2\nD NaF",
        "Electron charge?\nA +1\nB 0\nC -1\nD +2",
        "Proton charge?\nA +1\nB 0\nC -1\nD +2",
        "Neutron charge?\nA +1\nB 0\nC -1\nD +2",
        "Avogadro's number?\nA 6.02e23\nB 3.14\nC 1.67e-24\nD 9.8",
        "Chemical symbol of gold?\nA Au\nB Ag\nC Gd\nD Ga",
        "Chemical symbol of iron?\nA Fe\nB Ir\nC I\nD Fr",
        "Formula of carbon dioxide?\nA CO2\nB CO\nC C2O\nD C2O3",
        "Which is a halogen?\nA F\nB Ne\nC Na\nD Mg",
        "Number of valence electrons in O?\nA 2\nB 4\nC 6\nD 8",
        "Ionic bond involves?\nA Electron sharing\nB Electron transfer\nC Proton sharing\nD Neutron sharing",
        "Which is an alkene?\nA C2H4\nB C2H6\nC CH4\nD C3H8",
        "Which is an alcohol?\nA CH3OH\nB CH4\nC CO2\nD H2O",
        "pH of lemon juice?\nA 2\nB 5\nC 7\nD 9",
        "Which is a base?\nA NaOH\nB HCl\nC CH3COOH\nD H2SO4",
        "Electronegativity of F?\nA 3.98\nB 3.44\nC 2.20\nD 1.90",
        "Boiling point of water?\nA 100°C\nB 0°C\nC 50°C\nD 200°C",
        "Melting point of ice?\nA 0°C\nB 100°C\nC -10°C\nD 50°C",
        "Chemical formula of ammonia?\nA NH3\nB H2O\nC CH4\nD NO2",
        "Which gas is in dry ice?\nA CO2\nB O2\nC N2\nD CH4",
        "Atomic number of O?\nA 8\nB 16\nC 12\nD 6",
        "Atomic number of N?\nA 7\nB 14\nC 6\nD 8",
        "Molecular weight of H2O?\nA 18\nB 16\nC 20\nD 22",
        "Which is a transition metal?\nA Fe\nB Na\nC O\nD Cl",
        "Which is an alkaline earth metal?\nA Mg\nB K\nC F\nD Ne",
        "Formula of sulfuric acid?\nA H2SO4\nB HCl\nC HNO3\nD CH3COOH",
        "Which is a noble gas?\nA Ar\nB F\nC K\nD Na",
        "Which is a strong base?\nA NaOH\nB NH4\nC H2O\nD HCl",
        "Which is a weak acid?\nA CH3COOH\nB HCl\nC H2SO4\nD HNO3",
        "Which is diatomic?\nA O2\nB He\nC Ne\nD H2O",
        "Which element is most reactive?\nA F\nB Ne\nC He\nD Ar",
        "Which is a halogen?\nA Cl\nB Mg\nC Na\nD Ca",
        "Which is used in fertilizers?\nA N\nB Ne\nC Ar\nD He",
        "Which is inert?\nA Ne\nB Cl\nC H\nD O",
        "Which element in group 1?\nA K\nB Ca\nC O\nD F",
        "Which is a metal?\nA Na\nB O\nC Cl\nD He",
        "Which is a metalloid?\nA B\nB C\nC O\nD Ne",
        "Which is radioactive?\nA U\nB O\nC Ne\nD He",
        "Which is an isotope of hydrogen?\nA D\nB He\nC O\nD N",
        "Which is a catalyst?\nA Pt\nB O\nC H2\nD N2"
    };

    char[] answers = {'C','B','C','A','B','A','A','B','A','A','A','C','A','C','A','A','A','A','A','A',
                      'C','B','A','A','A','A','A','A','A','A','A','A','A','A','A','A','A','A','A','A',
                      'A','A','A','A','A','A','A','A','A','A'};

    public Map2_G6_PD6() {

        frame = new JFrame("Chem Quest");
        frame.setLayout(new BorderLayout());

        tileSize = frameWidth / mapw;

        topPanel = new JPanel();
        timerLabel = new JLabel("Time: 3");
        pointsLabel = new JLabel("Points: 0");
        topPanel.add(timerLabel);
        topPanel.add(pointsLabel);
        frame.add(topPanel, BorderLayout.NORTH);

        wall = scale(new ImageIcon("images/G6_brickwall.png"));
        ground = scale(new ImageIcon("images/G6_ground2.png"));
        pt1 = scale(new ImageIcon("images/G6_pt1.png"));
        pt2 = scale(new ImageIcon("images/G6_pt2.png"));
        pt3 = scale(new ImageIcon("images/G6_pt3.png"));

        icon = scale(new ImageIcon("images/G6_char1.png"));
        icon2 = scale(new ImageIcon("images/G6_char2.png"));
        icon3 = scale(new ImageIcon("images/G6_char3.png"));
        icon4 = scale(new ImageIcon("images/G6_char4.png"));
        icon5 = scale(new ImageIcon("images/G6_char5.png"));
        icon6 = scale(new ImageIcon("images/G6_char6.png"));
        icon7 = scale(new ImageIcon("images/G6_char7.png"));

        mapLayout = new int[]{
            0,0,0,0,0,0,0,0,0,0,0,
            0,1,1,1,1,1,1,1,1,1,0,
            0,1,1,0,0,1,0,0,1,1,0,
            0,0,0,0,1,1,1,0,1,1,0,
            0,1,1,1,1,1,1,0,0,0,0,
            0,1,1,0,0,0,1,1,1,1,0,
            0,1,0,0,1,0,0,0,1,1,0,
            0,3,3,3,3,3,3,3,3,3,0,
            0,4,4,4,4,4,4,4,4,4,0,
            0,5,5,5,5,5,5,5,5,5,0,
            0,0,0,0,0,0,0,0,0,0,0
        };

        tiles = new JLabel[mapw * maph];
        chr = new JLabel[mapw * maph];
        baseTiles = new ImageIcon[mapw * maph];

        for (int i = 0; i < tiles.length; i++) {
            if (mapLayout[i] == 0) baseTiles[i] = wall;
            else if (mapLayout[i] == 1) baseTiles[i] = ground;
            else if (mapLayout[i] == 3) baseTiles[i] = pt1;
            else if (mapLayout[i] == 4) baseTiles[i] = pt2;
            else if (mapLayout[i] == 5) baseTiles[i] = pt3;

            tiles[i] = new JLabel(baseTiles[i]);
            chr[i] = new JLabel();
        }

        player.setPosition(12);
        chr[player.getPosition()].setIcon(icon);
    }

    public void setFrame() {
    JPanel gridPanel = new JPanel(new GridLayout(maph, mapw));

    for (int i = 0; i < tiles.length; i++) {
        JPanel cell = new JPanel();
        cell.setLayout(new OverlayLayout(cell));
        cell.add(chr[i]);
        cell.add(tiles[i]);
        gridPanel.add(cell);
    }

    frame.add(gridPanel, BorderLayout.CENTER);

    frame.setSize(frameWidth, frameHeight);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setResizable(false);
    frame.setVisible(true);

    frame.addKeyListener(this);
    frame.setFocusable(true);
    frame.requestFocusInWindow();
}
    private ImageIcon scale(ImageIcon icon) {
        return new ImageIcon(icon.getImage().getScaledInstance(tileSize, tileSize, Image.SCALE_DEFAULT));
    }

    private void startTimer() {
        if (timer != null) timer.cancel();
        timeLeft = 3;
        timerLabel.setText("Time: " + timeLeft);
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                timeLeft--;
                SwingUtilities.invokeLater(() -> timerLabel.setText("Time: " + timeLeft));
                if (timeLeft <= 0) {
                    timer.cancel();
                    SwingUtilities.invokeLater(() -> askQuestion());
                }
            }
        }, 1000, 1000);
    }

    private void askQuestion() {
        int pos = player.getPosition();
        int tileType = mapLayout[pos];
        if (!(tileType == 3 || tileType == 4 || tileType == 5)) {
            JOptionPane.showMessageDialog(frame, "You didn't reach a line in time!");
            resetPlayer();
            return;
        }
        int q = random.nextInt(questions.length);
        String input = JOptionPane.showInputDialog(frame, questions[q]);
        if (input != null && input.length() > 0 &&
            Character.toUpperCase(input.charAt(0)) == answers[q]) {
            if (tileType == 5) player.addPoints(10);
            else if (tileType == 4) player.addPoints(9);
            else if (tileType == 3) player.addPoints(8);
            pointsLabel.setText("Points: " + player.getPoints());
            JOptionPane.showMessageDialog(frame, "Correct!");
        } else JOptionPane.showMessageDialog(frame, "Wrong!");
        if (player.getPoints() >= 67) {
            JOptionPane.showMessageDialog(frame, "You Win!");
            System.exit(0);
        }
        resetPlayer();
    }

    private void resetPlayer() {
        int pos = player.getPosition();
        chr[pos].setIcon(null);
        player.setPosition(12);
        chr[player.getPosition()].setIcon(icon);
        timerStarted = false;
        timerLabel.setText("Time: 3");
    }

    private void movePlayer(int newPos, ImageIcon sprite) {
        int oldPos = player.getPosition();
        chr[oldPos].setIcon(null);
        player.move(newPos);
        chr[player.getPosition()].setIcon(sprite);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!timerStarted) {
            startTimer();
            timerStarted = true;
        }
        int pos = player.getPosition();
        if (e.getKeyCode() == KeyEvent.VK_RIGHT && mapLayout[pos+1] != 0) {
            if(chrmodeR==0){ movePlayer(pos+1, icon2); chrmodeR=1; }
            else{ movePlayer(pos+1, icon3); chrmodeR=0; }
        }
        else if (e.getKeyCode() == KeyEvent.VK_LEFT && mapLayout[pos-1] != 0) {
            if(chrmodeL==0){ movePlayer(pos-1, icon4); chrmodeL=1; }
            else{ movePlayer(pos-1, icon5); chrmodeL=0; }
        }
        else if (e.getKeyCode() == KeyEvent.VK_DOWN && mapLayout[pos+mapw] != 0) {
            if(chrmodeD==0){ movePlayer(pos+mapw, icon); chrmodeD=1; }
            else if(chrmodeD==1){ movePlayer(pos+mapw, icon6); chrmodeD=2; }
            else{ movePlayer(pos+mapw, icon7); chrmodeD=0; }
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    abstract class Entity {
        protected int position;
        public int getPosition(){ return position; }
        public void setPosition(int p){ position = p; }
        public abstract void draw();
    }

    class Player extends Entity {
        private int points;
        public int getPoints(){ return points; }
        public void addPoints(int p){ points += p; }
        @Override public void draw(){}
        public void move(int newPos){ position = newPos; }
        public void move(int dx,int dy){ position += dx + dy*mapw; }
    }
}