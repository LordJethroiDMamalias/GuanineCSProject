package bossroom;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class BossRoom extends JPanel implements KeyListener {

    final int COLS = 23;
    final int ROWS = 13;

    final int WIDTH = 660;
    final int HEIGHT = 660;

    final int TILE_W = WIDTH / COLS;
    final int TILE_H = HEIGHT / ROWS;

    int gridX = 5;
    int gridY = 9;

    BufferedImage mapImg;
    BufferedImage playerUp, playerDown, playerLeft, playerRight;
    BufferedImage currentPlayer;

    boolean[][] walkable = new boolean[ROWS][COLS];

    BufferedImage load(String name){
        try{return ImageIO.read(new File("src/assets/"+name));}
        catch(Exception e){return null;}
    }

    public BossRoom(){
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        addKeyListener(this);

        mapImg = load("map 1.png");

        playerUp = load("character_up.png");
        playerDown = load("character_down.png");
        playerLeft = load("character_left.png");
        playerRight = load("character_right.png");

        currentPlayer = playerDown;

        generateCollision();
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

                if(py < imgH * 0.18){
                    walkable[y][x] = true;
                    continue;
                }

                int rgb = mapImg.getRGB(px, py);
                Color c = new Color(rgb);

                int r = c.getRed();
                int g = c.getGreen();
                int b = c.getBlue();

                boolean isWall = (r < 65 && g < 65 && b < 65);

                walkable[y][x] = !isWall;
            }
        }
    }

    int px(){ return gridX * TILE_W; }
    int py(){ return gridY * TILE_H; }

    void move(int dx, int dy, BufferedImage dir){

        int nx = gridX + dx;
        int ny = gridY + dy;

        if(nx < 0 || ny < 0 || nx >= COLS || ny >= ROWS) return;
        if(!walkable[ny][nx]) return;

        gridX = nx;
        gridY = ny;
        currentPlayer = dir;

        repaint();
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        if(mapImg != null){
            g2.drawImage(mapImg, 0, 0, WIDTH, HEIGHT, null);
        }

        if(currentPlayer != null){
            g2.drawImage(currentPlayer, px(), py(), TILE_W, TILE_H, null);
        }
    }

    @Override
    public void keyPressed(KeyEvent e){

        if(e.getKeyCode()==KeyEvent.VK_W)
            move(0,-1,playerUp);

        if(e.getKeyCode()==KeyEvent.VK_S)
            move(0,1,playerDown);

        if(e.getKeyCode()==KeyEvent.VK_A)
            move(-1,0,playerLeft);

        if(e.getKeyCode()==KeyEvent.VK_D)
            move(1,0,playerRight);
    }

    public void keyReleased(KeyEvent e){}
    public void keyTyped(KeyEvent e){}

    public static void main(String[] args){
        JFrame f = new JFrame("Boss Room");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(new BossRoom());
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }
}