package codes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;


//yea bai i broke the explosion code
public class G4_Room1_PD4 implements KeyListener {
    JFrame frame;
    JLayeredPane layers;
    ImageIcon wall, tile, machine, pipe;
    ImageIcon rocket1, rocket2, rocket3;
    ImageIcon NPCIcon, NPCIcon2;
    JLabel rLabel;
    JLabel nLabel;

    ImageIcon[] pUp    = new ImageIcon[4];
    ImageIcon[] pDown  = new ImageIcon[4];
    ImageIcon[] pLeft  = new ImageIcon[4];
    ImageIcon[] pRight = new ImageIcon[4];
    int walkFrame = 0;
    int direction = 3;

    JLabel tiles[], character[];
    int mapLayout[];
    int characterPlace[];
    int mapWidth  = 11;
    int mapHeight = 11;
    int frameWidth  = 660;
    int frameHeight = 660;
    int characterPosition;
    int NPCLocation, machineLocation = -1, rocketLocation = -1;
    int objective = 0;
    boolean yesTrigger  = false;
    boolean doneTrigger = false;

    private boolean hasLeftSpawn  = false;
    private boolean transitioning = false;

    private static Clip sharedBGM = null;
    private static String currentBGM = null;

    public static void playSharedBGM(String filePath) {
        try {
            if (sharedBGM != null && sharedBGM.isRunning()
                    && filePath.equals(currentBGM)) {
                return;
            }

            stopSharedBGM();

            File audioFile = new File(filePath);
            if (!audioFile.exists()) {
                System.out.println("BGM not found: " + filePath);
                return;
            }

            AudioInputStream stream = AudioSystem.getAudioInputStream(audioFile);
            sharedBGM = AudioSystem.getClip();
            sharedBGM.open(stream);

            if (sharedBGM.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gain = (FloatControl) sharedBGM.getControl(FloatControl.Type.MASTER_GAIN);
                gain.setValue(-10f);
            }

            sharedBGM.loop(Clip.LOOP_CONTINUOUSLY);
            sharedBGM.start();

            currentBGM = filePath;

        } catch (Exception e) {
            System.out.println("BGM error: " + e.getMessage());
        }
    }

    public static void stopSharedBGM() {
        if (sharedBGM != null) {
            if (sharedBGM.isRunning()) sharedBGM.stop();
            sharedBGM.close();
            sharedBGM = null;
            currentBGM = null;
        }
    }
    
    Dialog dialog = new Dialog();

    private Clip bgmClip = null;

    public G4_Room1_PD4() {
        frame = new JFrame();
        int tw = frameWidth  / mapWidth;
        int th = frameHeight / mapHeight;

        wall    = scale("images/G4_wall.png",    tw, th);
        tile    = scale("images/G4_tile.png",    tw, th);
        machine = scale("images/G4_machine.png", tw, th);
        pipe    = scale("images/G4_pipe.png",    tw, th);

        for (int i = 0; i < 4; i++) {
            pUp[i]    = scale("images/up_"    + (i + 1) + ".png", tw / 2, th);
            pDown[i]  = scale("images/down_"  + (i + 1) + ".png", tw / 2, th);
            pLeft[i]  = scale("images/left_"  + (i + 1) + ".png", tw / 2, th);
            pRight[i] = scale("images/right_" + (i + 1) + ".png", tw / 2, th);
        }

        NPCIcon  = scale("images/G4_bro1.png",   tw * 4, th * 6);
        NPCIcon2 = scale("images/G4_bro2.png",   tw * 4, th * 6);
        rocket1  = scale("images/G4_rocket1.png", tw * 2, th * 2);
        rocket2  = scale("images/G4_rocket2.png", tw * 2, th * 2);
        rocket3  = scale("images/G4_rocket3.png", tw * 2, th * 2);

        characterPlace = new int[]{
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 6, 1, 1, 1, 3, 1, 1, 1, 0, 0,
            0, 1, 1, 1, 1, 8, 1, 1, 1, 1, 7,
            0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 7,
            0, 5, 4, 5, 0, 0, 1, 1, 1, 1, 7,
            0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0,
            0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0,
            0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0,
            0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0,
            0, 1, 2, 1, 0, 0, 0, 0, 0, 0, 0
        };

        character = new JLabel[mapWidth * mapHeight];
        for (int i = 0; i < character.length; i++) {
            character[i] = new JLabel();
            character[i].setHorizontalAlignment(JLabel.CENTER);
            switch (characterPlace[i]) {
                case 2 -> { character[i].setIcon(pRight[0]); characterPosition = i; }
                case 3 -> { character[i].setIcon(NPCIcon);   NPCLocation       = i; }
                case 4 -> { character[i].setIcon(machine);   machineLocation   = i; }
                case 6 -> { character[i].setIcon(rocket1);   rocketLocation    = i; }
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
        
        rLabel = character[rocketLocation];
        nLabel = character[NPCLocation];
    }

    private void loadSaveData() {
        SaveSystem.SaveData save = SaveSystem.loadGame("G4_Room1_PD4");

        SaveSystem.startTimer(save.timeSeconds);

        doneTrigger = save.hasFlag("objective_rocket");
        yesTrigger = save.hasFlag("objective_yes");

        objective = 0;
        if (doneTrigger) {
            objective++;
            layers.remove(nLabel);
            layers.remove(rLabel);
            layers.revalidate();
            layers.repaint();
            rocketLocation = -1;
            NPCLocation    = -1;
        }
    }

    private void saveProgress() {
        SaveSystem.saveGame(
            new SaveSystem.SaveData.Builder("G4_Room1_PD4")
                .flag(doneTrigger ? "objective_rocket" : null)
                .flag(yesTrigger ? "objective_yes" : null)
                .battles(SaveSystem.getDefeatedBosses())
        );
    }

    private ImageIcon scale(String path, int w, int h) {
        return new ImageIcon(
            new ImageIcon(path).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH)
        );
    }

    public void setFrame() {
        layers = new JLayeredPane();
        layers.setLayout(new GraphPaperLayout(new Dimension(mapWidth, mapHeight)));
        layers.setBounds(0, 0, frameWidth, frameHeight);
        frame.setContentPane(layers);

        int x = 0, y = 0;
        for (int i = 0; i < tiles.length; i++) {
            layers.add(tiles[i], new Rectangle(x, y, 1, 1), Integer.valueOf(0));
            x++;
            if (x % mapWidth == 0) { x = 0; y++; }
        }

        x = 0; y = 0;
        for (int i = 0; i < character.length; i++) {
            Rectangle rect = switch (characterPlace[i]) {
                case 3 -> new Rectangle(x, y, 4, 6);
                case 6 -> new Rectangle(x, y, 2, 2);
                default -> new Rectangle(x, y, 1, 1);
            };
            layers.add(character[i], rect, 0);
            x++;
            if (x % mapWidth == 0) { x = 0; y++; }
        }
        

        Timer rocketAnim = new Timer(150, ev -> {
            if (rocketLocation != -1) {
                if (doneTrigger) {
                    Icon curr = character[rocketLocation].getIcon();
                    character[rocketLocation].setIcon(curr == rocket2 ? rocket3 : rocket2);
                } else {
                    character[rocketLocation].setIcon(rocket1);
                }
            }
        });
        rocketAnim.start();

        frame.setSize(frameWidth, frameHeight);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.addKeyListener(this);
        dialog.addKey(frame);
        loadSaveData();
        saveProgress();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (dialog.isVisible() || Battle.paused) return;

        int move = 0;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W -> { direction = 0; move = -mapWidth; }
            case KeyEvent.VK_S -> { direction = 1; move =  mapWidth; }
            case KeyEvent.VK_A -> { direction = 2; move = -1; }
            case KeyEvent.VK_D -> { direction = 3; move =  1; }
        }

        if (move != 0) {
            int nextPos = characterPosition + move;

            if (nextPos >= 0 && nextPos < characterPlace.length) {

                boolean isBlocked = (characterPlace[nextPos] == 0
                                  || characterPlace[nextPos] == 4
                                  || characterPlace[nextPos] == 5);

                if (NPCLocation != -1) {
                    int nX    = NPCLocation % mapWidth, nY    = NPCLocation / mapWidth;
                    int nextX = nextPos     % mapWidth, nextY = nextPos     / mapWidth;
                    if (nextX >= nX && nextX < nX + 4 && nextY >= nY && nextY < nY + 6)
                        isBlocked = true;
                }

                boolean isExitTile = (characterPlace[nextPos] == 7)
                                  || (characterPlace[nextPos] == 2 && hasLeftSpawn);

                if (!isBlocked && (characterPlace[nextPos] == 1 || isExitTile)) {

                    int oldPosition = characterPosition;

                    character[characterPosition].setIcon(null);
                    characterPosition = nextPos;
                    walkFrame = (walkFrame + 1) % 4;

                    if (!hasLeftSpawn && characterPlace[oldPosition] == 2)
                        hasLeftSpawn = true;

                    boolean landedOnExit = (characterPlace[characterPosition] == 7);

                    if (landedOnExit) {
                        transitionToPD6();
                        return;
                    }
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

            boolean touchingNPC = false;
            if (NPCLocation != -1 && interact != -1) {
                int nX = NPCLocation % mapWidth, nY = NPCLocation / mapWidth;
                int iX = interact     % mapWidth, iY = interact     / mapWidth;
                if (iX >= nX && iX < nX + 4 && iY >= nY && iY < nY + 6)
                    touchingNPC = true;
            }

            if (touchingNPC) {
                Timer talkingAnim = new Timer(150, ev -> {
                    if (NPCLocation == -1 || !dialog.isVisible()) {
                        if (NPCLocation != -1) character[NPCLocation].setIcon(NPCIcon);
                        ((Timer) ev.getSource()).stop();
                    } else {
                        boolean toggle = (System.currentTimeMillis() / 150) % 2 == 0;
                        character[NPCLocation].setIcon(toggle ? NPCIcon2 : NPCIcon);
                    }
                });
                talkingAnim.start();

                if (!doneTrigger) {
                    if (!yesTrigger) {
                        String[] lines = {
                            "LALALALALALALALALAL LAL ALA LAL AL LALALA.",
                            "I LOVE GAMES. AND GAMES LOVE ME..!",
                            "HEY. YOU. STOP POKING ON MY SHINGLES",
                            "WHAT'S THAT? YOU WANT TO GET PAST ME?",
                            "OKAY BUT YOU NEED TO DO SOMETHING FOR ME FIRST..",
                            "I WANT TO PLAY WITH MY ROCKET BUT IT'S BROKEN",
                            "FIX MY ROCKET? >~<? sorry"
                        };
                        String[] options = {"Okay", "Just because of that, no"};
                        Runnable[] branches = {
                            () -> {
                                yesTrigger = true;
                                saveProgress();
                                dialog.show(layers, new String[]{
                                    "EXEMPLARY",
                                    "I WANT IT TO MOVE WITH AN ACCELERATION OF 5 M/S^2",
                                    "JUST USE THE MACHINE THERE",
                                    "CLICK THE RED BUTTON 2 TIMES AND TURN IT ON",
                                    "CHANGE THE FORCE OF THE ROCKET",
                                    "OK? THANK YOU"
                                }, null, null, mapWidth, mapHeight);
                            },
                            () -> {
                                dialog.show(layers, new String[]{
                                    "...",
                                    "I SEE HOW IT IS",
                                    "YOU'RE JUST LIKE JETROIDS",
                                    "STUPID JETROIDS",
                                    "STUPID YOU",
                                    "I HATE YOU"
                                }, null, null, mapWidth, mapHeight);
                            }
                        };
                        dialog.show(layers, lines, options, branches, mapWidth, mapHeight);
                    } else {
                        dialog.show(layers, new String[]{
                            "YOU... DIDN'T DO IT",
                            "DISAPPOINTING",
                            "TALK TO ME AGAIN WHEN YOU'RE DONE"
                        }, null, null, mapWidth, mapHeight);
                    }
                } else {
                    dialog.show(layers, new String[]{
                        "YOU... DID IT",
                        "FLY HIGH ROCKET",
                        "wait",
                        "so where is it supposed to go now",
                        "...",
                        "IT'S FACING TOWARDS ME",
                        "YOU DID THIS",
                        "NO NO NO NO NO NO NO NO NO NO NO NO NO NO NO NO NO NO NO NO NO NO NO NO NO NO NO NO NO NO NO NO NO NO NO NO NO NO NO"
                    }, null, null, mapWidth, mapHeight);

                    Timer rocketMove = new Timer(20, null);
                    rocketMove.addActionListener(ev -> {
                        if (!dialog.isVisible() && rocketLocation != -1 && NPCLocation != -1) {
                            JLabel rLabel = character[rocketLocation];
                            JLabel nLabel = character[NPCLocation];
                            if (rLabel.getX() + rLabel.getWidth() < nLabel.getX() + 50) {
                                rLabel.setLocation(rLabel.getX() + 8, rLabel.getY());
                            } else {
                                ((Timer) ev.getSource()).stop();

                                int oldR = rocketLocation;
                                int oldN = NPCLocation;
                                int nx   = oldN % mapWidth;
                                int ny   = oldN / mapWidth;

                                characterPlace[oldR] = 1;
                                characterPlace[oldN] = 0;

                                layers.remove(rLabel);
                                layers.remove(nLabel);

                                rocketLocation = -1;
                                NPCLocation    = -1;

                                File f  = new File("images/G4_explosion.gif");
                                int tw  = frameWidth  / mapWidth;
                                int th  = frameHeight / mapHeight;
                                String html = "<html><img src='" + f.toURI().toString()
                                        + "' width='" + (tw * 4) + "' height='"
                                        + (th * 6) + "'></html>";
                                JLabel explosionLabel = new JLabel(html);

                                layers.add(explosionLabel,
                                        new Rectangle(nx, ny, 4, 6),
                                        0);
                                layers.revalidate();
                                layers.repaint();
                                playSound("sfx/explode.wav");

                                new Timer(1500, e2 -> {
                                    layers.remove(explosionLabel);
                                    layers.revalidate();
                                    layers.repaint();
                                    stopSound();
                                    ((Timer) e2.getSource()).stop();
                                }).start();
                            }
                        }
                    });
                    rocketMove.start();
                }
            }

            if (interact == machineLocation) {
                if (!yesTrigger) {
                    dialog.show(layers, new String[]{
                        "[An odd machine.]",
                        "[It changes the force applied to an object.]",
                        "[You don't know how to turn it on.]",
                        "[Someone might know.]"
                    }, null, null, mapWidth, mapHeight);
                } else if (!doneTrigger) {
                    dialog.show(layers, new String[]{
                        "[An odd machine.]",
                        "[It changes the force applied to an object.]",
                        "[You pressed on the red button two times.]",
                        "[The machine turned on.]",
                        "[Before you use the machine, you find a sticky note beside the screen.]",
                        "'FORMULA FOR FORCE: MASS * ACCELERATION :)'"
                    }, null, null, mapWidth, mapHeight);
                    Timer monitor = new Timer(100, ev -> {
                        if (!dialog.isVisible()) {
                            ((Timer) ev.getSource()).stop();
                            String in = JOptionPane.showInputDialog(
                                frame,
                                "DETECTED MASS: 5 KG\nWANTED ACCELERATION: 5 M/S^2\nPLEASE ENTER CORRECT FORCE:",
                                "ROCKET MACHINE X9000",
                                JOptionPane.QUESTION_MESSAGE
                            );
                            if (in != null && in.equals("25")) {
                                JOptionPane.showMessageDialog(frame, "FORCE INPUTTED; ATTEMPT SUCCESS");
                                dialog.show(layers, new String[]{
                                    "[The machine radiates with POWER!]",
                                    "[The rocket begins to whir.]",
                                    "[Let's get this show on the road.]"
                                }, null, null, mapWidth, mapHeight);
                                doneTrigger = true;
                                saveProgress();
                            } else if (in != null) {
                                JOptionPane.showMessageDialog(frame, "FORCE INPUTTED; ATTEMPT FAILURE");
                                dialog.show(layers, new String[]{
                                    "[The machine grows tired of your measly efforts.]",
                                    "[...How does it even do that?]"
                                }, null, null, mapWidth, mapHeight);
                            }
                        }
                    });
                    monitor.start();
                } else {
                    dialog.show(layers, new String[]{
                        "[An odd machine.]",
                        "[It changes the force applied to an object.]",
                        "[Seems like it broke and can't be used again.]",
                        "[Ah, but surely there's nothing else that could go wrong.]",
                        "[...Like the rocket killing the big creature blocking your road.]",
                        "[Totally not foreshadowing.]"
                    }, null, null, mapWidth, mapHeight);
                }
            }
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e)    {}

    private void transitionToPD6() {
        if (transitioning) return;
        transitioning = true;

        frame.dispose();
        SwingUtilities.invokeLater(() -> new G4_Room2_PD6().setFrame());
    }

    public void playSound(String filePath) {
        stopSound();

        File audioFile = new File(filePath);
        if (!audioFile.exists()) {
            System.out.println("Audio file not found: " + audioFile.getAbsolutePath());
            return;
        }

        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            bgmClip = AudioSystem.getClip();
            bgmClip.open(audioStream);
            setVolume(bgmClip, 0.5f);
            bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
            bgmClip.start();
            System.out.println("Now playing: " + filePath);
        } catch (UnsupportedAudioFileException e) {
            System.out.println("Unsupported audio format: " + filePath);
        } catch (LineUnavailableException e) {
            System.out.println("Audio line unavailable: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("I/O error reading audio: " + e.getMessage());
        }
    }

    private void setVolume(Clip clip, float linearGain) {
        if (linearGain <= 0f)  linearGain = 0.0001f;
        else if (linearGain > 1f) linearGain = 1f;

        if (!clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            System.out.println("Volume control not supported — playing at default volume.");
            return;
        }

        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        float dB      = 20f * (float) Math.log10(linearGain);
        float min     = gainControl.getMinimum();
        float max     = gainControl.getMaximum();
        float clamped = Math.max(min, Math.min(max, dB));
        gainControl.setValue(clamped);
        System.out.printf("Volume set to %.0f%% (%.2f dB)%n", linearGain * 100, clamped);
    }

    public void stopSound() {
        if (bgmClip != null) {
            if (bgmClip.isRunning()) bgmClip.stop();
            bgmClip.close();
            bgmClip = null;
        }
    }

    public static void main(String[] args) {
        playSharedBGM("music/Jetroids.wav");
        new G4_Room1_PD4().setFrame();
    }
}