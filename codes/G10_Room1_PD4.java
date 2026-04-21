package bossroom;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.*;

public class minibossroom extends JPanel implements KeyListener {

    final int COLS = 33;
    final int ROWS = 20;

    final int WIDTH = 660;
    final int HEIGHT = 660;

    final int TILE_W = WIDTH / COLS;
    final int TILE_H = HEIGHT / ROWS;

    int gridX = 16;
    int gridY = 13;

    BufferedImage mapImg;
    BufferedImage player;

    boolean[][] walkable = new boolean[ROWS][COLS];

    // 🚪 DOOR (DOWN-LEFT DIAGONAL)
    final int doorX = 15;
    final int doorY = 7;

    BufferedImage load(String name){
        try {
            return ImageIO.read(new File("src/assets/" + name));
        } catch(Exception e){
            System.out.println("Failed to load: " + name);
            return null;
        }
    }

    public minibossroom(){
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        addKeyListener(this);

        mapImg = load("map 2.png");
        player = load("character_down.png");

        generateCollision();

        // ensure spawn is not blocked
        if(!walkable[gridY][gridX]){
            gridY = 12;
        }

        requestFocusInWindow();
    }

    void generateCollision(){

        int imgW = mapImg.getWidth();
        int imgH = mapImg.getHeight();

        for(int y = 0; y < ROWS; y++){
            for(int x = 0; x < COLS; x++){

                int px = (int)((x + 0.5) * imgW / COLS);
                int py = (int)((y + 0.5) * imgH / ROWS);

                if(px < 0) px = 0;
                if(py < 0) py = 0;
                if(px >= imgW) px = imgW - 1;
                if(py >= imgH) py = imgH - 1;

                int rgb = mapImg.getRGB(px, py);
                Color c = new Color(rgb);

                int r = c.getRed();
                int g = c.getGreen();
                int b = c.getBlue();

                boolean isBlocked =
                        (b > 120 && g > 120) ||   // water
                        (r < 70 && g < 70 && b < 70); // walls

                // 🚪 FORCE DOOR TILE TO BE WALKABLE
                if(x == doorX && y == doorY){
                    walkable[y][x] = true;
                    continue;
                }

                walkable[y][x] = !isBlocked;
            }
        }
    }

    boolean isAtDoor(int x, int y){
        return x == doorX && y == doorY;
    }

    void saveProgress(){
        try{
            FileWriter fw = new FileWriter("save.txt");
            fw.write("ENTERED_MINIBOSS");
            fw.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    void enterDoor(){
        saveProgress();

        JFrame current = (JFrame) SwingUtilities.getWindowAncestor(this);
        current.dispose();

        SwingUtilities.invokeLater(() -> {
            JFrame bossFrame = new JFrame("Boss Room");
            BossRoom boss = new BossRoom();

            bossFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            bossFrame.add(boss);
            bossFrame.pack();
            bossFrame.setLocationRelativeTo(null);
            bossFrame.setVisible(true);

            boss.requestFocusInWindow();
        });
    }

    public void move(int dx, int dy){
        int nx = gridX + dx;
        int ny = gridY + dy;

        if(nx < 0 || ny < 0 || nx >= COLS || ny >= ROWS) return;
        if(!walkable[ny][nx]) return;

        gridX = nx;
        gridY = ny;

        if(isAtDoor(gridX, gridY)){
            enterDoor();
            return;
        }

        repaint();
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);

        g.drawImage(mapImg, 0, 0, WIDTH, HEIGHT, null);

        // 🟦 DOOR OUTLINE
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(new Color(100, 200, 255));
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(doorX * TILE_W, doorY * TILE_H, TILE_W, TILE_H);

        // PLAYER
        g.drawImage(player, gridX * TILE_W, gridY * TILE_H, TILE_W, TILE_H, null);
    }

    @Override
    public void keyPressed(KeyEvent e){
        if(e.getKeyCode() == KeyEvent.VK_W) move(0, -1);
        if(e.getKeyCode() == KeyEvent.VK_S) move(0, 1);
        if(e.getKeyCode() == KeyEvent.VK_A) move(-1, 0);
        if(e.getKeyCode() == KeyEvent.VK_D) move(1, 0);
    }

    public void keyReleased(KeyEvent e){}
    public void keyTyped(KeyEvent e){}

    public static void main(String[] args){
        JFrame f = new JFrame("Island");
        minibossroom game = new minibossroom();

        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(game);
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);

        game.requestFocusInWindow();
    }
}
