package codes;

import java.awt.*;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;

public class G2_Room1_PD4 implements KeyListener, ActionListener {
    public static final class MusicPlayer {

        private static Clip   clip    = null;
        private static String current = null;  //

        private MusicPlayer() {}  
        public static synchronized void start(String path) {
            if (clip != null && clip.isRunning() && path.equals(current)) return;

            stop();  
            try {
                File file = new File(path);
                if (!file.exists()) {
                    System.err.println("[MusicPlayer] File not found: " + path);
                    return;
                }

                AudioInputStream raw = AudioSystem.getAudioInputStream(file);
                AudioFormat      src = raw.getFormat();

                AudioInputStream stream;
                if (src.getSampleSizeInBits() != 16
                        || src.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
                    AudioFormat target = new AudioFormat(
                            AudioFormat.Encoding.PCM_SIGNED,
                            src.getSampleRate(),      // keep original rate (e.g. 44100 Hz)
                            16,                       // force 16-bit
                            src.getChannels(),        // keep stereo/mono
                            src.getChannels() * 2,    // frame size = channels x 2 bytes
                            src.getSampleRate(),      // frame rate == sample rate for PCM
                            false);                   // little-endian
                    stream = AudioSystem.getAudioInputStream(target, raw);
                    System.out.println("[MusicPlayer] Converted "
                            + src.getSampleSizeInBits() + "-bit to 16-bit PCM");
                } else {
                    stream = raw;
                }

                clip = AudioSystem.getClip();
                clip.open(stream);
                clip.loop(Clip.LOOP_CONTINUOUSLY);
                clip.start();
                current = path;
                System.out.println("[MusicPlayer] Now playing: " + path);
            } catch (Exception e) {
                System.err.println("[MusicPlayer] Could not start music: " + e.getMessage());
            }
        }

        public static synchronized void stop() {
            if (clip != null) {
                clip.stop();
                clip.close();
                clip    = null;
                current = null;
            }
        }

        /** Returns true if music is currently playing. */
        public static synchronized boolean isPlaying() {
            return clip != null && clip.isRunning();
        }
    }

    JFrame frame;
    JLayeredPane layers;
    
    ImageIcon G2_pinkBrick, G2_door1, G2_pinkFloor, G2_pinkPath, G2_NPCIcon, G2_NPCIcon2;
    ImageIcon G2_makeupShelf, G2_pinkCouch, G2_makeupMirror1, G2_makeupMirror2, G2_makeupDesk, G2_pinkDress, G2_pinkGown, G2_pinkRug;
    
    ImageIcon backgroundImage;

    ImageIcon[] pUp    = new ImageIcon[4];
    ImageIcon[] pDown  = new ImageIcon[4];
    ImageIcon[] pLeft  = new ImageIcon[4];
    ImageIcon[] pRight = new ImageIcon[4];
    int walkFrame = 0;

    JLabel tiles[], character[];
    int mapLayout[], characterPlace[];
    int mapWidth = 11, mapHeight = 11;
    int frameWidth = 660, frameHeight = 660 ;
    int characterPosition, characterMode;
    int tileSize = 64;
    
    int NPCLocation = -1, deskLocation = -1;
    int direction = 1; // default facing down
    boolean quizCompleted = false, hasKey = false;
    
    JLabel   backgroundLabel;
    
    Dialog dialog = new Dialog();

    public G2_Room1_PD4() {
        frame = new JFrame("Makeup Room");
        frameWidth  = mapWidth  * tileSize;
        frameHeight = mapHeight * tileSize;
        mapLayout = new int[]{
            1,1,1,1,1,1,1,1,1,1,1,
            1,1,10,9,10,1,1,1,3,1,1,
            1,0,0,0,0,0,1,0,0,0,1,
            1,0,0,8,0,0,0,0,0,0,4,
            1,0,0,7,0,0,1,0,0,0,1,
            1,0,0,0,0,0,1,0,0,0,1,
            1,5,0,6,0,5,1,0,0,0,1,
            1,1,1,1,1,1,1,0,0,0,1,
            1,0,0,0,0,0,0,0,0,0,1,
            1,0,0,0,0,0,0,0,0,0,1,
            1,0,0,0,0,0,0,0,2,0,1,
        };

        characterPlace = new int[]{
            0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,2,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,
            0,1,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,
        };

        tiles = new JLabel[mapHeight * mapWidth];
        for (int i = 0; i < tiles.length; i++) {
            if (mapLayout[i] == 0)
                tiles[i] = new JLabel();
        }

        try {
            backgroundImage = new ImageIcon("images/G2_RoomMap.png");
            if (backgroundImage.getImageLoadStatus() == MediaTracker.ERRORED)
                throw new FileNotFoundException(
                        "Background image not found at images/G2_RoomMap.png");
            backgroundImage = new ImageIcon(backgroundImage.getImage()
                    .getScaledInstance((frameWidth / mapWidth) * 11,
                                       (frameHeight / mapHeight) * 11,
                                       Image.SCALE_DEFAULT));
        } catch (Exception e) {
            System.err.println("Warning: Background image failed — " + e.getMessage());
            backgroundImage = new ImageIcon(
                    new java.awt.image.BufferedImage(frameWidth, frameHeight,
                            java.awt.image.BufferedImage.TYPE_INT_ARGB));
        }
        backgroundLabel = new JLabel(backgroundImage);

        // Sprites — scalePlayer uses fixed 41px to match PD6 visual size
        for (int i = 0; i < 4; i++) {
            pUp[i]    = scalePlayer("images/up_"    + (i + 1) + ".png");
            pDown[i]  = scalePlayer("images/down_"  + (i + 1) + ".png");
            pLeft[i]  = scalePlayer("images/left_"  + (i + 1) + ".png");
            pRight[i] = scalePlayer("images/right_" + (i + 1) + ".png");
        }

        G2_pinkBrick     = scale("images/G2_pinkBrick.png");
        G2_door1         = scale("images/G2_door.png");
        G2_pinkFloor     = scale("images/G2_pinkFloor.png");
        G2_pinkPath      = scale("images/G2_pinkPath.png");
        G2_NPCIcon       = scalePlayer("images/G2_MakeupNPC.png");
        G2_NPCIcon2      = scalePlayer("images/G2_MakeupNPC.png");
        G2_makeupShelf   = scale("images/G2_makeupShelf.png");
        G2_pinkCouch     = scale("images/G2_pinkCouch.png");
        G2_makeupMirror1 = scale("images/G2_makeupMirror1.png");
        G2_makeupMirror2 = scale("images/G2_makeupMirror2.png");
        G2_pinkGown      = scale("images/G2_pinkGown.png");
        G2_pinkDress     = scale("images/G2_pinkDress.png");
        G2_makeupDesk    = scale("images/G2_makeupDesk.png");
        G2_pinkRug       = scale("images/G2_pinkRug.png");

        character = new JLabel[mapWidth * mapHeight];
        for (int i = 0; i < character.length; i++) {
            character[i] = new JLabel();
            character[i].setHorizontalAlignment(JLabel.CENTER);
            if (characterPlace[i] == 1) {
                characterPosition = i;
                character[i].setIcon(pDown[0]);
            } else if (characterPlace[i] == 2) {
                character[i].setIcon(G2_NPCIcon);
                NPCLocation = i;
            }
        }

        System.out.println("Room memory allocation sequence finished.");
        loadSaveData();
    }

    private void loadSaveData() {
        SaveSystem.SaveData save = SaveSystem.loadGame("G2_Room1_PD4");

        SaveSystem.startTimer(save.timeSeconds);

        quizCompleted = save.hasFlag("quiz_completed");
        hasKey        = save.hasFlag("has_key");
        if (save.hasFlag("quiz_completed")) {
            mapLayout[39] = 0;  // door behind NPC becomes walkable
            NPCLocation = -1;   // NPC is gone so player can walk through
        }

        System.out.println("[Room1] Loaded — Quiz:" + quizCompleted
                + "  HasKey:" + hasKey
                + "  Door:" + save.hasFlag("door_unlocked")
                + "  Time:" + save.formattedTime());
    }

    private void saveProgress() {
        SaveSystem.saveGame(
            new SaveSystem.SaveData.Builder("G2_Room1_PD4")
                .time(SaveSystem.getTotalSeconds())
                .flag(quizCompleted ? "quiz_completed" : null)
                .flag(hasKey        ? "has_key"        : null)
                .battles(SaveSystem.getDefeatedBosses())
        );
    }
    private ImageIcon scale(String path) throws NullPointerException {
        try {
            ImageIcon icon = new ImageIcon(path);
            if (icon.getImageLoadStatus() == MediaTracker.ERRORED) {
                throw new Exception("File not found at: " + path);
            }
            return new ImageIcon(icon.getImage().getScaledInstance(tileSize, tileSize, Image.SCALE_DEFAULT));
        } catch (Exception e) {
            System.out.println("Warning: Image scale failed for " + path + ". Using blank icon.");
            return new ImageIcon(); 
        }
    }
    private ImageIcon scalePlayer(String path) {
        int spriteSize = 41; // match PD6 visual size (660/16 = 41)
        try {
            ImageIcon icon = new ImageIcon(path);
            if (icon.getImageLoadStatus() == MediaTracker.ERRORED)
                return new ImageIcon(new java.awt.image.BufferedImage(
                        spriteSize, spriteSize, java.awt.image.BufferedImage.TYPE_INT_ARGB));
            return new ImageIcon(icon.getImage()
                    .getScaledInstance(spriteSize, spriteSize, Image.SCALE_SMOOTH));
        } catch (Exception e) {
            return new ImageIcon();
        }
    }

    public void setFrame() {
        try {
            layers = new JLayeredPane();
            layers.setLayout(new GraphPaperLayout(new Dimension(mapWidth, mapHeight)));
            frame.setContentPane(layers);

            int x = 0, y = 0, w = 1, h = 1;
            for (int i = 0; i < character.length; i++) {
                if (character[i] == null) throw new NullPointerException("Character label at index " + i + " is null.");
                layers.add(character[i], new Rectangle(x, y, w, h), 0); // layer 0 = top
                x++;
                if (x % mapWidth == 0) { x = 0; y++; }
            }
            
            layers.add(backgroundLabel, new Rectangle(0, 0, mapWidth, mapHeight));

            frame.setSize(frameWidth, frameHeight);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.addKeyListener(this);
            frame.setVisible(true);
            
            dialog.addKey(frame); 
        } catch (NullPointerException | IllegalArgumentException e) {
            JOptionPane.showMessageDialog(null, "GUI Error: " + e.getMessage(), "Initialization Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void transitionToRoom2() {
        saveProgress(); 
        dialog.show(layers,
            new String[]{"[You step through the door...]", "[The hallway leads to the Living Room.]"},
            null, null, mapWidth, mapHeight,
            () -> SwingUtilities.invokeLater(() -> {
                frame.dispose();
                try {
                    G2_Room2_PD6 room2 = new G2_Room2_PD6();
                    room2.setFrame();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null,
                        "Failed to load Room 2: " + ex.getMessage(),
                        "Transition Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            })
        );
    }

    public void startQuiz() {
        try {
            String[] q1 = {"Question 1: What is primer mainly used for?"};
            String[] ops1 = {"To remove makeup", "To help makeup last longer", "To change skin color", "To clean brushes"};
        
            Runnable[] ans1 = {
                () -> failQuiz("Primer doesn't remove makeup!"),
                () -> { 
                    dialog.show(layers, new String[]{"Correct!"}, new String[]{"Next Question"}, 
                        new Runnable[]{ () -> question2() }, mapWidth, mapHeight);
                }, 
                () -> failQuiz("Primer isn't for changing skin color."),
                () -> failQuiz("You don't use primer on brushes!")
            };
        
            dialog.show(layers, q1, ops1, ans1, mapWidth, mapHeight);
        } catch (Exception e) {
            System.err.println("Failed to initiate quiz: " + e.getMessage());
        }
    }

    public void question2() {
        String[] q2 = {"Question 2: Which product hides dark circles?"};
        String[] ops2 = {"Blush", "Highlighter", "Concealer", "Bronzer"};
    
        Runnable[] ans2 = {
            () -> failQuiz("Blush adds color, it doesn't hide circles."),
            () -> failQuiz("Highlighter adds glow, try again."),
            () -> { 
                dialog.show(layers, new String[]{"Correct again!"}, new String[]{"Next Question!"}, 
                    new Runnable[]{ () -> question3() }, mapWidth, mapHeight);
            }, 
            () -> failQuiz("Bronzer is for contouring.")
        };
    
        dialog.show(layers, q2, ops2, ans2, mapWidth, mapHeight);
    }
    
    public void question3() {
        String[] q3 = {"Question 3: If you want a natural look, which is best?"};
        String[] ops3 = {"Bold contour", "Neon eyeshadow", "Neutral tones", "Smoky eyes"};
    
        Runnable[] ans3 = {
            () -> failQuiz("Bold contour is for high glam, not natural."),
            () -> failQuiz("Neon is for parties, try again."),
            () -> { 
                dialog.show(layers, new String[]{"Correct! Neutral is key."}, new String[]{"Question 4"}, 
                    new Runnable[]{ () -> question4() }, mapWidth, mapHeight);
            }, 
            () -> failQuiz("Smoky eyes are too dark for a natural look.")
        };
        dialog.show(layers, q3, ops3, ans3, mapWidth, mapHeight);
    }

    public void question4() {
        String[] q4 = {"Question 4: What should you do BEFORE foundation?"};
        String[] ops4 = {"Apply lipstick", "Moisturize skin", "Use setting spray", "Apply blush"};
    
        Runnable[] ans4 = {
            () -> failQuiz("Lipstick is usually the last step!"),
            () -> { 
                dialog.show(layers, new String[]{"Correct! Prep is important."}, new String[]{"Question 5"}, 
                    new Runnable[]{ () -> question5() }, mapWidth, mapHeight);
            }, 
            () -> failQuiz("Setting spray comes at the very end."),
            () -> failQuiz("Blush goes on top of foundation.")
        };
        dialog.show(layers, q4, ops4, ans4, mapWidth, mapHeight);
    }

    public void question5() {
        String[] q5 = {"Question 5: Which suits sensitive skin best?"};
        String[] ops5 = {"Fragrance products", "Expired makeup", "Hypoallergenic", "Sharing brushes"};
    
        Runnable[] ans5 = {
            () -> failQuiz("Fragrances often irritate sensitive skin."),
            () -> failQuiz("Never use expired makeup!"),
            () -> { 
                dialog.show(layers, new String[]{"Correct! Hypoallergenic is safest."}, new String[]{"Final Question"}, 
                    new Runnable[]{ () -> question6() }, mapWidth, mapHeight);
            }, 
            () -> failQuiz("Sharing brushes spreads bacteria.")
        };
        dialog.show(layers, q5, ops5, ans5, mapWidth, mapHeight);
    }

    public void question6() {
        String[] q6 = {"Final Question: Why remove makeup before sleep?"};
        String[] ops6 = {"To save it", "To avoid breakouts", "To make skin dark", "To dry skin"};
    
        Runnable[] ans6 = {
            () -> failQuiz("You can't reuse makeup once it's removed!"),
            () -> { 
                dialog.show(layers, new String[]{"Exactly! Your skin needs to breathe."}, new String[]{"Finish"}, 
                    new Runnable[]{ () -> finishQuiz() }, mapWidth, mapHeight);
            }, 
            () -> failQuiz("Makeup removal doesn't change skin pigment."),
            () -> failQuiz("We want to hydrate skin, not dry it out.")
        };
        dialog.show(layers, q6, ops6, ans6, mapWidth, mapHeight);
    }

    public void failQuiz(String reason) {
        String[] lines = {reason, "You have failed the test.", "Talk to me again when you are ready to retry."};
        dialog.show(layers, lines, null, null, mapWidth, mapHeight);
    }

    public void finishQuiz() {
       quizCompleted = true;
       saveProgress(); 
       dialog.show(layers, new String[]{"Excellent work!", "The door is now unlocked.", "You may proceed."}, null, null, mapWidth, mapHeight);

       int npcDoorIndex = 39;  // index 11 tile — door behind the NPC
       mapLayout[npcDoorIndex] = 0;  // make walkable so player can pass through

       Timer vanishTimer = new Timer(100, null);
       vanishTimer.addActionListener(ev -> {
           try {
               if (!dialog.isVisible()) {
                   if (NPCLocation != -1) {
                       character[NPCLocation].setIcon(null);
                       NPCLocation = -1; 
                       layers.repaint();
                   }
                   vanishTimer.stop();
               }
           } catch (Exception ex) {
               vanishTimer.stop();
           }
       });
       vanishTimer.start();
   }

    @Override
    public void keyPressed(KeyEvent e) {
        if (dialog.isVisible()) return;

        int move = 0;
        try {
            int key = e.getKeyCode();
            if      (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) { direction = 3; move =  1; }
            else if (key == KeyEvent.VK_LEFT  || key == KeyEvent.VK_A) { direction = 2; move = -1; }
            else if (key == KeyEvent.VK_UP    || key == KeyEvent.VK_W) { direction = 0; move = -mapWidth; }
            else if (key == KeyEvent.VK_DOWN  || key == KeyEvent.VK_S) { direction = 1; move =  mapWidth; }

            if (move != 0) {
                int nextPos = characterPosition + move;
                if (nextPos < 0 || nextPos >= mapLayout.length) {
                    throw new ArrayIndexOutOfBoundsException("Attempted to move outside map boundaries.");
                }

                boolean isWrapping = (Math.abs(move) == 1
                        && (nextPos / mapWidth != characterPosition / mapWidth));

                boolean isWalkable = (mapLayout[nextPos] == 0 || mapLayout[nextPos] == 4);
                boolean isNPCPresent = (nextPos == NPCLocation);

                if (!isWrapping && isWalkable && !isNPCPresent) {
                    character[characterPosition].setIcon(null);
                    characterPosition = nextPos;
                    walkFrame = (walkFrame + 1) % 4;
                    // Stepping onto index 4 tile (tile 35) transitions to Room 2
                    if (mapLayout[characterPosition] == 4) {
                        if (hasKey) {
                            transitionToRoom2();
                            return;
                        } else {
                            // Bumped into door without key — step back and show message
                            character[characterPosition].setIcon(null);
                            characterPosition -= move;
                            walkFrame = 0;
                            dialog.show(layers, new String[]{"[The door is locked tight.]", "[You need the Pink Key.]"}, null, null, mapWidth, mapHeight);
                        }
                    }
                } else {
                    walkFrame = 0;
                }

                // Always update the displayed animation frame to match direction
                ImageIcon current = switch (direction) {
                    case 0 -> pUp[walkFrame];
                    case 1 -> pDown[walkFrame];
                    case 2 -> pLeft[walkFrame];
                    case 3 -> pRight[walkFrame];
                    default -> pDown[0];
                };
                if (current != null) character[characterPosition].setIcon(current);
            }

            if (key == KeyEvent.VK_SPACE) {
                int interact = switch (direction) {
                    case 0 -> characterPosition - mapWidth;
                    case 1 -> characterPosition + mapWidth;
                    case 2 -> characterPosition - 1;
                    case 3 -> characterPosition + 1;
                    default -> -1;
                };

                if (interact < 0 || interact >= mapLayout.length) return;

                // NPC Interaction
                if (interact == NPCLocation && NPCLocation != -1) {
                    if (!quizCompleted) {
                        dialog.show(layers, new String[]{"Are you ready to start?"}, new String[]{"Start Quiz", "No"},
                            new Runnable[]{ () -> startQuiz(), null }, mapWidth, mapHeight);
                    } else {
                        dialog.show(layers, new String[]{"[You've already completed the quiz!]", "[The door is open. You may proceed.]"}, null, null, mapWidth, mapHeight);
                    }
                }

                // Dress Interaction (index 2)
                else if (mapLayout[interact] == 2) {
                    dialog.show(layers, new String[]{"[A beautiful designer dress.]", "[It's made of the finest silk.]"}, null, null, mapWidth, mapHeight);
                }

                // Makeup Mirror 1 Interaction (index 3)
                else if (mapLayout[interact] == 3) {
                    dialog.show(layers, new String[]{"[You look in the mirror.]", "[You look fabulous today!]"}, null, null, mapWidth, mapHeight);
                }

                // NPC Door Interaction (index 11) - only reachable if quiz not done yet
                else if (mapLayout[interact] == 11) {
                    dialog.show(layers, new String[]{"[The door is blocked.]", "[Talk to the NPC to get through.]"}, null, null, mapWidth, mapHeight);
                }

                // Locked Door Interaction (index 4) - walk into it to transition (requires Pink Key)
                else if (mapLayout[interact] == 4) {
                    if (hasKey) {
                        dialog.show(layers, new String[]{"[Walk through the door to proceed.]"}, null, null, mapWidth, mapHeight);
                    } else {
                        dialog.show(layers, new String[]{"[The door is locked tight.]", "[You need the Pink Key.]"}, null, null, mapWidth, mapHeight);
                    }
                }

                // Makeup Shelves Interaction (index 5)
                else if (mapLayout[interact] == 5) {
                    dialog.show(layers, new String[]{"[A shelf full of high-end makeup.]", "[Everything is organized by color.]"}, null, null, mapWidth, mapHeight);
                }

                // Rug Interaction (index 6)
                else if (mapLayout[interact] == 6) {
                    dialog.show(layers, new String[]{"[A fluffy pink rug.]", "[It feels like walking on a cloud.]"}, null, null, mapWidth, mapHeight);
                }

                // Makeup Desk Interaction (index 7)
                else if (mapLayout[interact] == 7) {
                    if (quizCompleted && !hasKey) {
                        hasKey = true;
                        saveProgress();
                        dialog.show(layers, new String[]{"[You opened the desk...]", "[You found the Pink Key!]"}, null, null, mapWidth, mapHeight);
                    } else if (!quizCompleted) {
                        dialog.show(layers, new String[]{"[The desk is locked.]", "[The NPC has the code.]"}, null, null, mapWidth, mapHeight);
                    } else {
                        dialog.show(layers, new String[]{"[The desk is empty now.]"}, null, null, mapWidth, mapHeight);
                    }
                }

                // Couch Interaction (index 8)
                else if (mapLayout[interact] == 8) {
                    dialog.show(layers, new String[]{"[A very soft pink velvet couch.]", "[It looks brand new.]"}, null, null, mapWidth, mapHeight);
                }

                // Gown Interaction (index 9)
                else if (mapLayout[interact] == 9) {
                    dialog.show(layers, new String[]{"[An elegant pink gown.]", "[It shimmers under the light.]"}, null, null, mapWidth, mapHeight);
                }

                // Makeup Mirror 2 Interaction (index 10)
                else if (mapLayout[interact] == 10) {
                    dialog.show(layers, new String[]{"[You look in the mirror.]", "[Your makeup looks stunning!]"}, null, null, mapWidth, mapHeight);
                }
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            System.err.println("Movement Warning: " + ex.getMessage());
        } catch (Exception ex) {
            System.err.println("Interaction error: " + ex.getMessage());
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void actionPerformed(ActionEvent e) {}

    public static void main(String[] args) {
        MusicPlayer.start("music/Green Room.wav");

        SwingUtilities.invokeLater(() -> {
            try {
                new G2_Room1_PD4().setFrame();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
