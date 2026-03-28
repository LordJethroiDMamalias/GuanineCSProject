package CS4_Q2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

//Group 4: Asegurado, Mamalias, Mazo
//with major help from deepai
public class Map2_G4_PD6 implements KeyListener {
    JFrame frame;
    JLayeredPane layers;
    ImageIcon wall, tile, machine, pipe;
    ImageIcon map, door, painting;
    ImageIcon NPCIcon, NPCIcon2;

    ImageIcon[] pUp = new ImageIcon[4];
    ImageIcon[] pDown = new ImageIcon[4];
    ImageIcon[] pLeft = new ImageIcon[4];
    ImageIcon[] pRight = new ImageIcon[4];
    int walkFrame = 0; 
    int direction = 3;

    JLabel[] tiles, character;
    int[] mapLayout;
    int[] characterPlace;
    int mapWidth = 11;
    int mapHeight = 11;
    int frameWidth = 660;
    int frameHeight = 660;
    int characterPosition;
    int NPCLocation = -1, machineLocation = -1;

    //lalalal i love to code i love pd6 pd6 my best FRIEND.
    boolean consoleDone = false;
    boolean paintingDone = false;

    Dialog dialog = new Dialog();

    public Map2_G4_PD6() {
        frame = new JFrame();
        int tw = frameWidth / mapWidth;
        int th = frameHeight / mapHeight;

        //these are .png so that they're invisible. that's intended. it doesnt output an error in netbeans for me so
        wall = scale("images/.png", tw, th);
        tile = scale("images/.png", tw, th);
        machine = scale("images/.png", tw, th);
        pipe = scale("images/.png", tw, th);
        
        map = new ImageIcon("images/G4_map.png");
        door = new ImageIcon("images/G4_door.png");
        painting = new ImageIcon("images/G4_painting.png");

        for(int i = 0; i < 4; i++) {
            pUp[i] = scale("images/up_" + (i+1) + ".png", tw/2, th);
            pDown[i] = scale("images/down_" + (i+1) + ".png", tw/2, th);
            pLeft[i] = scale("images/left_" + (i+1) + ".png", tw/2, th);
            pRight[i] = scale("images/right_" + (i+1) + ".png", tw/2, th);
        }

        //invisible
        NPCIcon = scale("images/.png", tw, th);
        NPCIcon2 = scale("images/.png", tw, th);

        characterPlace = new int[]{
            0,0,0,0,1,2,1,0,0,0,0,
            0,1,1,1,1,1,1,0,4,4,0,
            0,1,1,1,1,1,1,0,1,1,0,
            0,1,1,1,0,0,0,0,1,1,0,
            0,1,1,1,3,1,1,5,1,1,0,
            0,1,1,1,3,1,1,5,1,1,0,
            0,1,1,1,0,1,1,0,1,1,0,
            0,1,1,1,0,1,1,0,0,0,0,
            0,1,1,1,1,1,1,1,1,1,0,
            0,1,1,1,1,1,1,1,1,1,0,
            0,0,0,0,0,0,0,0,0,0,0
        };

        character = new JLabel[mapWidth * mapHeight];
        for (int i = 0; i < character.length; i++) {
            character[i] = new JLabel(); 
            character[i].setHorizontalAlignment(JLabel.CENTER);
            switch (characterPlace[i]) {
                case 2 -> { character[i].setIcon(pRight[0]); characterPosition = i; }
                case 3 -> { character[i].setIcon(NPCIcon); NPCLocation = i; }
                case 4 -> { character[i].setIcon(machine); machineLocation = i; }
            }
        }

        mapLayout = characterPlace.clone();
        tiles = new JLabel[mapWidth * mapHeight];
        for (int i = 0; i < tiles.length; i++) {
            tiles[i] = switch (mapLayout[i]) {
                case 0 -> new JLabel(wall);
                case 5 -> new JLabel(pipe);
                default -> new JLabel(tile);
            };
        }
    }

    private ImageIcon scale(String path, int w, int h) {
        return new ImageIcon(new ImageIcon(path).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
    }

    public void setFrame() {
        layers = new JLayeredPane() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Draw static images at top-left corner
                g.drawImage(map.getImage(), 0, 0, null);
                if (!paintingDone) {
                    g.drawImage(door.getImage(), 0, 0, null);
                }
                g.drawImage(painting.getImage(), 0, 0, null);
            }
        };
        layers.setLayout(new GraphPaperLayout(new Dimension(mapWidth, mapHeight)));
        layers.setBounds(0, 0, frameWidth, frameHeight);
        frame.setContentPane(layers);

        int x = 0, y = 0;
        for (int i = 0; i < tiles.length; i++) {
            layers.add(tiles[i], new Rectangle(x, y, 1, 1), Integer.valueOf(0));
            x++; if (x % mapWidth == 0) { x = 0; y++; }
        }

        x = 0; y = 0;
        for (int i = 0; i < character.length; i++) {
            Rectangle rect = switch (characterPlace[i]) {
                case 3 -> new Rectangle(x, y, 1, 1);
                case 6 -> new Rectangle(x, y, 2, 2);
                default -> new Rectangle(x, y, 1, 1);
            };
            layers.add(character[i], rect, Integer.valueOf(1));
            x++; if (x % mapWidth == 0) { x = 0; y++; }
        }
        
        frame.setSize(frameWidth, frameHeight);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.addKeyListener(this);
        dialog.addKey(frame);
    }

@Override
public void keyPressed(KeyEvent e) {
    if (dialog.isVisible()) return;

    int move = 0;
    switch (e.getKeyCode()) {
        case KeyEvent.VK_UP -> { direction = 0; move = -mapWidth; }
        case KeyEvent.VK_DOWN -> { direction = 1; move = mapWidth; }
        case KeyEvent.VK_LEFT -> { direction = 2; move = -1; }
        case KeyEvent.VK_RIGHT -> { direction = 3; move = 1; }
    }

    if (move != 0) {
        int nextPos = characterPosition + move;
        if (nextPos >= 0 && nextPos < characterPlace.length) {
            boolean isBlocked = (characterPlace[nextPos] == 0 || characterPlace[nextPos] == 4 || characterPlace[nextPos] == 5);
            if (NPCLocation != -1) {
                int nX = NPCLocation % mapWidth, nY = NPCLocation / mapWidth;
                int nextX = nextPos % mapWidth, nextY = nextPos / mapWidth;
                if (nextX >= nX && nextX < nX + 1 && nextY >= nY && nextY < nY + 1) isBlocked = true;
            }
            if (!isBlocked && characterPlace[nextPos] == 1) {
                character[characterPosition].setIcon(null);
                characterPosition = nextPos;
                walkFrame = (walkFrame + 1) % 4;
            }
        }
        ImageIcon current = switch (direction) {
            case 0 -> pUp[walkFrame];
            case 1 -> pDown[walkFrame];
            case 2 -> pLeft[walkFrame];
            case 3 -> pRight[walkFrame];
            default -> pDown[0];
        };
        character[characterPosition].setIcon(current);
        character[characterPosition].setHorizontalAlignment(JLabel.CENTER);
    }

    if (e.getKeyCode() == KeyEvent.VK_SPACE) {
        int interact = switch (direction) {
            case 0 -> characterPosition - mapWidth;
            case 1 -> characterPosition + mapWidth;
            case 2 -> characterPosition - 1;
            case 3 -> characterPosition + 1;
            default -> -1;
        };

        if (interact >= 0 && interact < characterPlace.length) {
            int tileType = mapLayout[interact];
            if (tileType == 4) {
                if (consoleDone == true) return;
                dialog.show(layers, new String[]{"[It's a console.]","[The console is titled 'PALACEGATEBOTTOM'.]","[You immerse yourself in the console.]","[Just a warning ahead.. This console looks pretty hard to navigate in. It seems to delve into the most difficult mathematical concepts.]"}, null, null, mapWidth, mapHeight);
                Timer monitor = new Timer(100, ev -> {
                    if (!dialog.isVisible()) {
                        ((Timer)ev.getSource()).stop();
                        String in = JOptionPane.showInputDialog(frame, "What is 1^999 + 0*5?", "PALACEGATEBOTTOM", JOptionPane.QUESTION_MESSAGE);
                        if (in != null && in.equals("1")) {
                            dialog.show(layers, new String[]{"[You.. are... a genius...]","[Something outside starts to rumble.]","[Seems like you're done with the room.]"}, null, null, mapWidth, mapHeight);
                            for (int i = 0; i < mapLayout.length; i++) {
                                if (mapLayout[i] == 5) {
                                    mapLayout[i] = 1;
                                    characterPlace[i] = 1;
                                    tiles[i].setIcon(tile);
                                }
                            }
                            consoleDone = true;
                        } else if (in != null) {
                            dialog.show(layers, new String[]{"[The painting removes the ink.]","[It seems that you put in the wrong potential energy.]"}, null, null, mapWidth, mapHeight);
                        }
                    }
                });
                monitor.start();
            }
            
            if (tileType == 3) {
                switch (direction) {
                    case 2:
                        if (paintingDone == true) return;
                        dialog.show(layers, new String[]{"[It's a painting.]","[There is a blank box on the top right corner.]","[Maybe there's a value you're supposed to put.]"}, null, null, mapWidth, mapHeight);
                        Timer monitor = new Timer(100, ev -> {
                            if (!dialog.isVisible()) {
                                ((Timer)ev.getSource()).stop();
                                String in = JOptionPane.showInputDialog(frame, "potential energy...", "Painting", JOptionPane.QUESTION_MESSAGE);
                                if (in != null && in.equals("490")) {
                                    dialog.show(layers, new String[]{"[Your written ink begins to glow.]","[The door behind you begins to open.]"}, null, null, mapWidth, mapHeight);
                                    for (int i = 0; i < mapLayout.length; i++) {
                                        if (mapLayout[i] == 5) {
                                            mapLayout[i] = 1;
                                            characterPlace[i] = 1;
                                            tiles[i].setIcon(tile);
                                        }
                                    }
                                    paintingDone = true;
                                    layers.repaint();
                                    
                                } else if (in != null) {
                                    dialog.show(layers, new String[]{"[The painting removes the ink.]","[It seems that you put in the wrong potential energy.]"}, null, null, mapWidth, mapHeight);
                                }
                            }
                        });
                        monitor.start();
                        break;
                    case 3:
                        dialog.show(layers, new String[] {
                            "[It's a painting.]",
                            "[The painting shows specifications for 'potential energy.']",
                            "'Mass = 10kg, Height = 5 meters...'",
                            "'GRAVITY MY LOVE; 9.8 m/s^2'"
                        }, null, null, mapWidth, mapHeight);
                        break;
                    default:
                        System.out.println("default");
                        break;
                }
            }
        }
    }
}

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    /*public static void main(String[] args) { 
        new PD6().setFrame(); 
    }*/
}
