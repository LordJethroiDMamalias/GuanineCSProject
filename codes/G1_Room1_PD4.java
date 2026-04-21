package deliverable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Deliverable extends JFrame {

    public Deliverable() {
        setTitle("YES");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        GamePanel panel = new GamePanel();
        add(panel);

        panel.setPreferredSize(new Dimension(800, 600));
pack();

        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Deliverable::new);
    }
    
    // -------------------- GamePanel (Inner Class) --------------------
    // The GamePanel class is now defined as an inner class within Deliverable.
    // It must still be public (or package-private) and implements KeyListener.
    public class GamePanel extends JPanel implements KeyListener { 

        private final int gridSize = 12;
        private final int colSize = 16;

        private final Image Vine;
        private final Image blue1;
        private final Image blue2;
        private final Image blue3;
        private final Image blue4;
        
        private final Image pro1;
        private final Image pro2;
        private final Image pro3;
        private final Image pro4;
        private final Image AGH;
        private final Image Dead;
        private int kirbyX = 8; // column index
        private int kirbyY = 11; // row index
        
       
        
        private final Image ogkirby;
        
        private final boolean[][] solidTiles = new boolean[gridSize][colSize];


        public GamePanel() {
            // --- KEYBOARD SETUP ---
            setFocusable(true); 
            addKeyListener(this); 
            requestFocusInWindow(); 
            // ----------------------
            
            // Load images (Note: these files must be in the project root or classpath)
            Vine = new ImageIcon("vines.png").getImage();
            AGH = new ImageIcon("AGH.png").getImage();
            blue1 = new ImageIcon("blue1.png").getImage();
            blue2 = new ImageIcon("blue2.png").getImage();
            blue3 = new ImageIcon("blue3.png").getImage();
            blue4 = new ImageIcon("blue4.png").getImage();
            pro1 = new ImageIcon("pro1.png").getImage();
            pro2 = new ImageIcon("pro2.png").getImage();
            pro3 = new ImageIcon("pro3.png").getImage();
            pro4 = new ImageIcon("pro4.png").getImage();
            ogkirby = new ImageIcon("ogkirby.png").getImage();
            Dead = new ImageIcon("deads.png").getImage();




// Row 1
solidTiles[1][2] = true;
solidTiles[1][3] = true;
solidTiles[1][4] = true;
solidTiles[1][6] = true;
solidTiles[1][7] = true;
solidTiles[1][8] = true;
solidTiles[1][9] = true;
solidTiles[1][10] = true;

// Row 2
solidTiles[2][2] = true;
solidTiles[2][10] = true;

// Row 3
solidTiles[3][2] = true;
solidTiles[3][10] = true;
solidTiles[3][11] = true;
solidTiles[3][12] = true;
solidTiles[3][13] = true;
solidTiles[3][14] = true;
solidTiles[3][15] = true;

// Row 4
solidTiles[4][2] = true;
solidTiles[4][15] = true;

// Row 5
solidTiles[5][0] = true;
solidTiles[5][1] = true;
solidTiles[5][2] = true;
solidTiles[5][15] = true;

// Row 6
solidTiles[6][0] = true;
solidTiles[6][2] = true;

// Row 7
solidTiles[7][2] = true;
solidTiles[7][3] = true;
solidTiles[7][4] = true;
solidTiles[7][5] = true;
solidTiles[7][6] = true;
solidTiles[7][7] = true;
solidTiles[7][15] = true;

// Row 8
solidTiles[8][0] = true;
solidTiles[8][15] = true;

// Row 9
solidTiles[9][0] = true;
solidTiles[9][2] = true;
solidTiles[9][3] = true;
solidTiles[9][4] = true;
solidTiles[9][15] = true;

// Row 10
solidTiles[10][0] = true;
solidTiles[10][1] = true;
solidTiles[10][2] = true;
solidTiles[10][4] = true;
solidTiles[10][6] = true;
solidTiles[10][7] = true;
solidTiles[10][8] = true;
solidTiles[10][9] = true;
solidTiles[10][10] = true;
solidTiles[10][11] = true;
solidTiles[10][12] = true;
solidTiles[10][13] = true;
solidTiles[10][15] = true;

// Row 11
solidTiles[11][4] = true;
solidTiles[11][6] = true;
solidTiles[11][13] = true;
solidTiles[11][14] = true;
solidTiles[11][15] = true;


solidTiles[11][5] = false;
solidTiles[7][0] = false;
solidTiles[6][15] = false;
solidTiles[1][5] = false;





        }

        @Override
        protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    int panelWidth = getWidth();
    int panelHeight = getHeight();

    int cellWidth = panelWidth / colSize;   // FIXED
    int cellHeight = panelHeight / gridSize;

    for (int row = 0; row < gridSize; row++) {
        for (int col = 0; col < colSize; col++) {

            Image img = Vine;

            if (row == 5 && col == 10) img = blue1;
            else if (row == 5 && col == 11) img = blue2;
            else if (row == 6 && col == 10) img = blue3;
            else if (row == 6 && col == 11) img = blue4;

            else if (row == 5 && col == 4) img = blue1;
            else if (row == 5 && col == 5) img = blue2;
            else if (row == 6 && col == 4) img = blue3;
            else if (row == 6 && col == 5) img = blue4;

            else if (row == 2 && col == 7) img = blue1;
            else if (row == 2 && col == 8) img = blue2;
            else if (row == 3 && col == 7) img = blue3;
            else if (row == 3 && col == 8) img = blue4;

            else if (row == 5 && col == 7) img = pro1;
            else if (row == 5 && col == 8) img = pro2;
            else if (row == 6 && col == 7) img = pro3;
            else if (row == 6 && col == 8) img = pro4;
            
            else if (row == 1 && col == 2) img = AGH;
            else if (row == 1 && col == 3) img = AGH;
            else if (row == 1 && col == 4) img = AGH;
            else if (row == 1 && col == 6) img = AGH;
            else if (row == 1 && col == 7) img = AGH;
            else if (row == 1 && col == 8) img = AGH;
            else if (row == 1 && col == 9) img = AGH;
            else if (row == 1 && col == 10) img = AGH;
            
            else if (row == 2 && col == 2) img = AGH;
            else if (row == 2 && col == 10) img = AGH;
            
            else if (row == 3 && col == 2) img = AGH;
            else if (row == 3 && col == 10) img = AGH;
            else if (row == 3 && col == 11) img = AGH;
            else if (row == 3 && col == 12) img = AGH;
            else if (row == 3 && col == 13) img = AGH;
            else if (row == 3 && col == 14) img = AGH;
            else if (row == 3 && col == 15) img = AGH;
            
            else if (row == 4 && col == 2) img = AGH;
            else if (row == 4 && col == 15) img = AGH;
            
            else if (row == 5 && col == 1) img = AGH;
            else if (row == 5 && col == 2) img = AGH;
            else if (row == 5 && col == 0) img = AGH;
            else if (row == 5 && col == 15) img = AGH;
            
            else if (row == 6 && col == 0) img = AGH;
            else if (row == 6 && col == 2) img = AGH;
            
            else if (row == 7 && col == 2) img = AGH;
            else if (row == 7 && col == 3) img = AGH;
            else if (row == 7 && col == 4) img = AGH;
            else if (row == 7 && col == 5) img = AGH;
            else if (row == 7 && col == 6) img = AGH;
            else if (row == 7 && col == 7) img = AGH;
            else if (row == 7 && col == 15) img = AGH;
            
            else if (row == 8 && col == 0) img = AGH;
            else if (row == 8 && col == 15) img = AGH;
            
            
            else if (row == 9 && col == 0) img = AGH;
            else if (row == 9 && col == 2) img = AGH;
            else if (row == 9 && col == 3) img = AGH;
            else if (row == 9 && col == 4) img = AGH;
            else if (row == 9 && col == 15) img = AGH;
            
            
            else if (row == 10 && col == 0) img = AGH;
            else if (row == 10 && col == 1) img = AGH;
            else if (row == 10 && col == 2) img = AGH;
            else if (row == 10 && col == 4) img = AGH;
            else if (row == 10 && col == 6) img = AGH;
            else if (row == 10 && col == 7) img = AGH;
            else if (row == 10 && col == 8) img = AGH;
            else if (row == 10 && col == 9) img = AGH;
            else if (row == 10 && col == 10) img = AGH;
            else if (row == 10 && col == 11) img = AGH;
            else if (row == 10 && col == 12) img = AGH;
            else if (row == 10 && col == 13) img = AGH;
            else if (row == 10 && col == 15) img = AGH;
            
            else if (row == 11 && col == 4) img = AGH;
            else if (row == 11 && col == 15) img = AGH;
            else if (row == 11 && col == 14) img = AGH;
            else if (row == 11 && col == 13) img = AGH;
            else if (row == 11 && col == 6) img = AGH;
            else if (row == 11 && col == 8) img = AGH;
            
            else if (row == 1 && col == 5) img = Dead;
            
            if (row == 6 && col == 15) {
    img = solidTiles[6][15] ? AGH : Dead;
}

if (row == 1 && col == 5) {
    img = solidTiles[1][5] ? AGH : Dead;
}
if (row == 7 && col == 0) {
    img = solidTiles[7][0] ? AGH : Dead;
}

            if (row == 11 && col == 5) {
    img = solidTiles[11][5] ? AGH : Dead;
}
            
           

            g.drawImage(img, col * cellWidth, row * cellHeight, cellWidth, cellHeight, null);
        }
    }

    g.drawImage(ogkirby, kirbyX * cellWidth, kirbyY * cellHeight, cellWidth, cellHeight, null);
}


        
@Override
public void keyPressed(KeyEvent e) {
    int key = e.getKeyCode();

    int oldX = kirbyX;
    int oldY = kirbyY;

    // W key to break the tile if Kirby is standing **next to it**
    if (key == KeyEvent.VK_W) {
        // Example: Kirby can break it from below or left
        if ((kirbyX == 15 && kirbyY == 7) || (kirbyX == 14 && kirbyY == 6)) {  
            solidTiles[6][15] = true;
            JOptionPane.showMessageDialog(this, "The vines are broken!");
        }
    }
    if (key == KeyEvent.VK_A) {
        
        if ((kirbyX == 6 && kirbyY == 1) || (kirbyX == 4 && kirbyY == 1)) {  
            solidTiles[1][5] = true;
            JOptionPane.showMessageDialog(this, "The vines are broken!");
        }
    }
    if (key == KeyEvent.VK_S) {
        // Example: Kirby can break it from below or left
        if ((kirbyX == 0 && kirbyY == 8) || (kirbyX == 0 && kirbyY == 6)) {  
            solidTiles[7][0] = true;
            JOptionPane.showMessageDialog(this, "The vines are broken!");
        }
    }
    if (key == KeyEvent.VK_D) {
        // Example: Kirby can break it from below or left
        if ((kirbyX == 4 && kirbyY == 11) || (kirbyX == 6 && kirbyY == 11)) {  
            solidTiles[11][5] = true;
            JOptionPane.showMessageDialog(this, "The vines are broken!");
        }
    }

    // Movement
    if (key == KeyEvent.VK_UP)    kirbyY--;
    if (key == KeyEvent.VK_DOWN)  kirbyY++;
    if (key == KeyEvent.VK_LEFT)  kirbyX--;
    if (key == KeyEvent.VK_RIGHT) kirbyX++;

    // Prevent out of bounds
    if (kirbyX < 0) kirbyX = 0;
    if (kirbyY < 0) kirbyY = 0;
    if (kirbyX >= colSize) kirbyX = colSize - 1;
    if (kirbyY >= gridSize) kirbyY = gridSize - 1;

    // Collision
    if (!solidTiles[kirbyY][kirbyX]) {
        kirbyX = oldX;
        kirbyY = oldY;
    }

    // Alert if stepping on a special tile
    if (kirbyX == 15 && kirbyY == 7 && !solidTiles[6][15]) {
        JOptionPane.showMessageDialog(this, "Press W to break the vines");
    }
    if (kirbyX == 6 && kirbyY == 1 && !solidTiles[1][5]) {
        JOptionPane.showMessageDialog(this, "Press A to break the vines");
    }
    if (kirbyX == 0 && kirbyY == 8 && !solidTiles[7][0]) {
        JOptionPane.showMessageDialog(this, "Press S to break the vines");
    }
    if (kirbyX == 6 && kirbyY == 11 && !solidTiles[11][5]) {
        JOptionPane.showMessageDialog(this, "Press D to break the vines");
    }

    repaint();
}



            

        @Override
        public void keyReleased(KeyEvent e) {}
        
        @Override
        public void keyTyped(KeyEvent e) {}
    }
}