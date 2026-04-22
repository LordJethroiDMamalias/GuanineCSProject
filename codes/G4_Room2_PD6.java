package codes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

//Group 4: Asegurado, Mamalias, Mazo
//with major help from deepai
public class G4_Room2_PD6 implements KeyListener {
    JFrame frame;
    JLayeredPane layers;
    ImageIcon wall, tile, machine, pipe;
    ImageIcon map, door, jetroids;
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
    int objective = 0;

    //lalalal i love to code i love pd6 pd6 my best FRIEND.
    boolean battleTriggered = false;
    boolean paintingDone = false;
    boolean endDialog = false;

    Dialog dialog = new Dialog();
    Battle battle = new Battle();

    public G4_Room2_PD6() {
        frame = new JFrame();
        int tw = frameWidth / mapWidth;
        int th = frameHeight / mapHeight;

        //these are .png so that they're invisible. that's intended. it doesnt output an error in netbeans for me so
        wall = scale("images/.png", tw, th);
        tile = scale("images/.png", tw, th);
        machine = scale("images/.png", tw, th);
        pipe = scale("images/.png", tw, th);
        
        map = scale("images/G4_map.png", 660, 660);
        door = scale("images/G4_door.png", 660, 660);
        jetroids = scale("images/G4_jetroids.png", 660, 660);

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
            0,0,0,0,0,0,0,0,0,0,0,
            0,1,1,1,1,0,0,0,0,0,0,
            0,1,1,1,1,0,0,0,0,0,0,
            1,1,1,0,3,0,0,0,0,0,0,
            2,1,1,0,3,0,1,1,1,4,0,
            1,1,1,1,1,5,1,1,1,4,0,
            0,1,1,1,1,5,1,1,1,4,0,
            0,1,1,1,1,0,1,1,1,1,0,
            0,1,1,1,1,0,1,1,1,1,0,
            0,1,1,1,1,0,1,1,1,1,0,
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
        loadSaveData();
        if (paintingDone == true) {
            for (int i = 0; i < mapLayout.length; i++) {
                if (mapLayout[i] == 5) {
                    mapLayout[i] = 1;
                    characterPlace[i] = 1;
                    tiles[i].setIcon(tile);
                }
            }
        }
    }
    
    // =========================================================================
    // Save integration — load
    // =========================================================================
    private void loadSaveData() {
        SaveSystem.SaveData save = SaveSystem.loadGame("G4_Room2_PD6");

        if (save.timeSeconds <= 0) {
            SaveSystem.SaveData pd4Save = SaveSystem.loadGame("G4_Room1_PD4");
            SaveSystem.startTimer(pd4Save.timeSeconds);
        } else {
            SaveSystem.startTimer(save.timeSeconds);
        }

        paintingDone = save.hasFlag("objective_painting");

        // Recalculate collected count from flags (single source of truth)
        objective = 0;
        if (paintingDone) objective++;

        // If Jetroids was permanently defeated, skip the battle trigger permanently
        if (save.isDefeated("Jetroids")) battleTriggered = true;

        System.out.println("[Room4] Loaded — Items:" + objective
                + "/1  BattleDone:" + battleTriggered
                + "  Time:" + save.formattedTime());
    }

    // =========================================================================
    // Save integration — save
    // =========================================================================
    private void saveProgress() {
        SaveSystem.saveGame(
            new SaveSystem.SaveData.Builder("G4_Room2_PD6")
                .flag(paintingDone ? "objective_painting" : null)
                .battles(SaveSystem.getDefeatedBosses())   // always up-to-date list
        );
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
                g.drawImage(jetroids.getImage(), 0, 0, null);
                if (!paintingDone) {
                    g.drawImage(door.getImage(), 0, 0, null);
                }
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
    battle.start(frame, "", "Bin Izharfed");
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
                if (battleTriggered == true) {
                    if (endDialog == false) {
                        endDialog = true;
                        String[] lines = {"hey, our business is already done.","you've done your deed.","why don't you go for everyone else now?","i just... gotta take a last look at everything.","you know, before it falls apart.","you know what? i was never under any heavy influence.","i just wanted to cause mischief for fun, and that nite amplified my imagination.","so much so that i began to fabricate stuff from my head.","i let it sit around my head because of that.","..yeah, that's enough talk.","better get moving."};
                        dialog.show(layers, lines, null, null, mapWidth, mapHeight);
                        return;
                    }
                    String[] lines = {"what'd i tell you?","you want to go down with this world?","just, leave where you came from.","i'll teleport you, i'll try."};
                    dialog.show(layers, lines, null, null, mapWidth, mapHeight);
                    return;
                }
                String[] regardless = {"AFTER THAT BIPOLAR MESS WITH TWO NAMES, YOU COME TO ME, JETROIDS","YOU WANT TO TAKE THIS NITE OUT OF ME, HUH?","BUT THIS IS WHAT DRIVES MY CREATIVITY. I DON'T WANT IT GONE...","...BUT IF IT CAN'T BE HELPED... IF YOU REALLY WANT TO BE SUCH A HERO...","...","then let's get started, shall we?"};
                String[] lines = {"HEY THERE KID","ENJOY THE VIEW?"};
                        String[] options = {"Yes", "I HATE IT."};
                        Runnable[] branches = {
                            () -> {
                                String[] current = {"HAHAHA YOU'RE FUNNY","THESE BLACK CLOUDS ARE THE RESULT OF MY INFLICTED TORMENT TOWARDS OTHERS","AND I DO THAT FOR FUN","ANYWAY, I KNOW WHAT YOU'RE UP TO"};
                                String[] yes = new String[current.length + regardless.length];
                                System.arraycopy(current, 0, yes, 0, current.length);
                                System.arraycopy(regardless, 0, yes, current.length, regardless.length);
                                dialog.show(layers, yes, null, null, mapWidth, mapHeight);
                                
                            },
                            () -> {
                                String[] current = {"...","I'M MAD!","I'M REALLY MAD!!","BUT... UGH OKAY","I'LL TAKE THIS SERIOUSLY NOW","...WELL","TO PUT IT BLUNTLY, I KNOW WHAT YOU'RE UP TO"};
                                String[] no = new String[current.length + regardless.length];
                                System.arraycopy(current, 0, no, 0, current.length);
                                System.arraycopy(regardless, 0, no, current.length, regardless.length);
                                dialog.show(layers, no, null, null, mapWidth, mapHeight); 
                            }
                        };
                dialog.show(layers, lines, options, branches, mapWidth, mapHeight);
                Timer monitor = new Timer(100, ev -> {
                    if (!dialog.isVisible()) {
                        ((Timer)ev.getSource()).stop();
                            battle.start(frame, "images/G4_jetroidsBG.png", "Jetroids");
                            waitForBattleEnd(() -> {
                                if (!SaveSystem.isDefeated("Jetroids")) {
                                    SaveSystem.markDefeated("Jetroids");
                                }
                                saveProgress();
                            });
                            for (int i = 0; i < mapLayout.length; i++) {
                                if (mapLayout[i] == 5) {
                                    mapLayout[i] = 1;
                                    characterPlace[i] = 1;
                                    tiles[i].setIcon(tile);
                                }
                            }
                        battleTriggered = true;
                    }
                });
                monitor.start();
            }
            
            if (tileType == 3) {
                switch (direction) {
                    case 1:
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
                                    saveProgress();
                                } else if (in != null) {
                                    dialog.show(layers, new String[]{"[The painting removes the ink.]","[It seems that you put in the wrong potential energy.]"}, null, null, mapWidth, mapHeight);
                                }
                            }
                        });
                        monitor.start();
                        break;
                    case 0:
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

    private void waitForBattleEnd(Runnable onEnd) {
        javax.swing.Timer poller = new javax.swing.Timer(200, null);
        poller.addActionListener(ev -> {
            if (!Battle.paused) {          // battle overlay is gone
                poller.stop();
                onEnd.run();
            }
        });
        poller.start();
    }
    
    public static void main(String[] args) { 
        new G4_Room2_PD6().setFrame(); 
    }
}