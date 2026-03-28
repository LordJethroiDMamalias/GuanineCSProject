/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package codes;

//RASONABE, REDULLA, VILLAROMAN

import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.swing.*;

public class G10_Room2_PD6 extends JFrame {
    private final int GRID = 11;
    private final int TILE_SIZE = 60; 

    private int[][] map = {
            {1,1,1,1,1,1,1,1,1,1,1},
            {1,0,1,0,0,0,0,0,0,1,1}, 
            {1,0,0,0,1,1,1,1,1,1,1},
            {1,0,1,1,0,0,0,0,0,0,1},
            {1,0,1,0,1,0,1,0,1,0,1},
            {1,0,1,0,1,0,1,0,1,0,1}, 
            {1,0,0,0,1,1,0,0,1,0,1},
            {1,0,1,1,0,0,0,1,1,0,1},
            {1,0,0,1,1,0,1,1,1,0,1},
            {1,1,0,0,0,0,0,0,1,3,1}, 
            {1,1,1,1,1,1,1,1,1,1,1}
    };

    private Character character;
    private Point npcPos = new Point(5, 5);
    private boolean npcDefeated = false;
    private boolean gameWon = false;

    private ImageIcon wallIco, floorIco, doorIco, npcIco;
    private ImageIcon pUp, pDown, pLeft, pRight;

    public G10_Room2_PD6() {
        loadImages();
        setTitle("Dungeon");
        setLayout(new GridLayout(GRID, GRID));
        setSize(660, 700); 
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        character = new Character(1, 1); 

        drawGrid();
        setupKeys();
        setVisible(true);
    }

    private void loadImages() {
        try {
            wallIco  = new ImageIcon(getClass().getResource("G10_wall.png"));
            floorIco = new ImageIcon(getClass().getResource("G10_floor.png"));
            doorIco  = new ImageIcon(getClass().getResource("G10_door.png"));
            npcIco   = new ImageIcon(getClass().getResource("G10_npc.png"));
            
            pUp    = new ImageIcon(getClass().getResource("G10_character_up.png"));
            pDown  = new ImageIcon(getClass().getResource("G10_character_down.png"));
            pLeft  = new ImageIcon(getClass().getResource("G10_character_left.png"));
            pRight = new ImageIcon(getClass().getResource("G10_character_right.png"));
        } catch (Exception e) {
            System.out.println("Error: Check image paths in mazegame2.pkg0");
        }
    }

    private void drawGrid() {
        getContentPane().removeAll();
        for(int row = 0; row < GRID; row++) {
            for(int col = 0; col < GRID; col++) {
                JLabel cell = new JLabel();
                cell.setPreferredSize(new Dimension(TILE_SIZE, TILE_SIZE));

                ImageIcon baseIcon;
                
                if(map[row][col] == 1) {
                    baseIcon = wallIco;
                } else if(map[row][col] == 3) {
                    baseIcon = doorIco;
                } else {
                    baseIcon = floorIco;
                }

                
                ImageIcon overlayIcon = null;
                if(!npcDefeated && col == npcPos.x && row == npcPos.y) {
                    overlayIcon = npcIco;
                } else if(col == character.getX() && row == character.getY()) {
                    switch(character.getDirection()) {
                        case UP:    overlayIcon = pUp; break;
                        case DOWN:  overlayIcon = pDown; break;
                        case LEFT:  overlayIcon = pLeft; break;
                        case RIGHT: overlayIcon = pRight; break;
                    }
                }

                
                if (overlayIcon != null) {
                    cell.setIcon(new CombinedIcon(baseIcon, overlayIcon));
                } else {
                    cell.setIcon(baseIcon);
                }

                add(cell);
            }
        }
        revalidate();
        repaint();
    }

    private void setupKeys() {
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if(gameWon) return;
                int dx = 0, dy = 0;
                if(e.getKeyCode() == KeyEvent.VK_UP)    { dy = -1; character.setDirection(Direction.UP); }
                if(e.getKeyCode() == KeyEvent.VK_DOWN)  { dy = 1;  character.setDirection(Direction.DOWN); }
                if(e.getKeyCode() == KeyEvent.VK_LEFT)  { dx = -1; character.setDirection(Direction.LEFT); }
                if(e.getKeyCode() == KeyEvent.VK_RIGHT) { dx = 1;  character.setDirection(Direction.RIGHT); }
                movePlayer(dx, dy);
            }
        });
    }

    private void movePlayer(int dx, int dy) {
        int nX = character.getX() + dx;
        int nY = character.getY() + dy;

        if(map[nY][nX] != 1) {
            if(!npcDefeated && nX == npcPos.x && nY == npcPos.y) {
                mathBattle();
            } else {
                character.setPosition(nX, nY);
            }
        }

        if(map[nY][nX] == 3) {
            if(npcDefeated) {
                gameWon = true;
                drawGrid();
                JOptionPane.showMessageDialog(this, "WIN!");
            } else {
                JOptionPane.showMessageDialog(this, "The door is locked! You must defeat the Guardian first.");
            }
        }
        drawGrid();
    }

    private void mathBattle() {
        Random rand = new Random();
        JOptionPane.showMessageDialog(this, "Guardian: Answer 3 questions to unlock the exit!");

        for(int i = 1; i <= 3; i++) {
            int a = rand.nextInt(10) + 1;
            int b = rand.nextInt(10) + 1;
            String ans = JOptionPane.showInputDialog("Question " + i + ": " + a + " + " + b);
            
            try {
                if(ans != null && Integer.parseInt(ans) == (a + b)) {
                    continue; 
                } else {
                    JOptionPane.showMessageDialog(this, "Wrong! You are teleported back to the start.");
                    character.setPosition(1, 1);
                    return;
                }
            } catch (Exception e) { return; }
        }
        npcDefeated = true;
        JOptionPane.showMessageDialog(this, "Guardian defeated! The exit door is now unlocked.");
    }

    public static void main(String[] args) { new G10_Room2_PD6(); }
}


class CombinedIcon implements Icon {
    private Icon bottom, top;
    public CombinedIcon(Icon bottom, Icon top) {
        this.bottom = bottom;
        this.top = top;
    }
    @Override
    public void paintIcon(java.awt.Component c, Graphics g, int x, int y) {
        bottom.paintIcon(c, g, x, y);
        top.paintIcon(c, g, x, y);
    }
    @Override
    public int getIconWidth() { return bottom.getIconWidth(); }
    @Override
    public int getIconHeight() { return bottom.getIconHeight(); }
}

enum Direction { UP, DOWN, LEFT, RIGHT }

class Character {
    private int x, y;
    private Direction dir = Direction.DOWN;
    public Character(int x, int y) { this.x = x; this.y = y; }
    public int getX() { return x; }
    public int getY() { return y; }
    public void setPosition(int x, int y) { this.x = x; this.y = y; }
    public void setDirection(Direction d) { this.dir = d; }
    public Direction getDirection() { return dir; }
}
