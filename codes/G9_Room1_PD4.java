package codes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

//grp: ancla, badayos, sepe
public class G9_Room1_PD4 implements KeyListener {

    JFrame frame;
    JLayeredPane layers;
    Dialogue dg = new Dialogue();
    ImageIcon skybg, cld1, cld2, cld3, sBTile, sMTile, sTTile;
    ImageIcon cMLTile, cBLTile, cTLTile, cMidTile, cMBTile, cTMTile, cTRTile, cMRTile, cBRTile;
    ImageIcon pFront, pBackW2, pFrontW2, pLeftW, pLeftW2, pLeft, pRightW;
    ImageIcon pRightW2, pRight, pBack, pBackW, pFrontW;
    ImageIcon tYoyo, tCar, tBear, tKite, npcKid;
    JLabel tiles[], character[], toys[];
    int mapLayout[], characterPosition, characterMode, characterLayout[], toyLayout[], direction, movement;
    int[] toyLoc = new int[4];
    int mapW = 11;
    int mapH = 11;
    int frW = 660;
    int frH = 660;
    int invCont, indicator, outIndicator = 0;
    boolean dupe, canMove, talked, isFirstTimeM, isFirstTimeI;
    int quizStep = 0; 
    private int returnStep = 0; // 0=Bear, 1=Kite, 2=YoYo, 3=Locket
    
    // NEW: Counter for tracking invalid key presses
    int wrongKeyCount = 0;
   
    public PD4_gameobj1() {
        characterPosition = -1; direction = -1;
        characterMode = 0; movement = 0;
        dupe = false; canMove = false; talked = false;
        isFirstTimeM = true; isFirstTimeI = true;
        frame = new JFrame("PD4");
        layers = new JLayeredPane();

        // --- load and scale images ---
        skybg = scale(new ImageIcon("images/G9_sky.png"));
        cld1 = scale(new ImageIcon("images/G9_cloud1.png"));
        cld2 = scale(new ImageIcon("images/G9_cloud2.png"));
        cld3 = scale(new ImageIcon("images/G9_cloud3.png"));
        cMLTile = scale(new ImageIcon("images/G9_cldMLTile.png")); 
        cBLTile = scale(new ImageIcon("images/G9_cldBLTile.png"));
        cMidTile = scale(new ImageIcon("images/G9_cldMidTile.png"));
        cTLTile = scale(new ImageIcon("images/G9_cldTLTile.png"));
        cMBTile = scale(new ImageIcon("images/G9_cldMBTile.png"));
        cTMTile = scale(new ImageIcon("images/G9_cldTMTile.png"));
        cTRTile = scale(new ImageIcon("images/G9_cldTRTile.png"));
        cMRTile = scale(new ImageIcon("images/G9_cldMRTile.png"));
        cBRTile = scale(new ImageIcon("images/G9_cldBRTile.png"));
        sMTile = scale(new ImageIcon("images/G9_stairMTile.png"));
        sBTile = scale(new ImageIcon("images/G9_stairBTile.png"));
        sTTile = scale(new ImageIcon("images/G9_stairTTile.png"));
        
        pFront = scale(new ImageIcon("images/G9_pFront.png"));
        pFrontW2 = scale(new ImageIcon("images/G9_pFrontRW.png"));
        pFrontW = scale(new ImageIcon("images/G9_pFrontLW.png"));
        pBack = scale(new ImageIcon("images/G9_pBack.png"));
        pBackW = scale(new ImageIcon("images/G9_pBack1.png"));
        pBackW2 = scale(new ImageIcon("images/G9_pBack2.png"));
        pLeftW = scale(new ImageIcon("images/G9_pLeft.png"));
        pLeft = scale(new ImageIcon("images/G9_pLeft1.png"));
        pLeftW2 = scale(new ImageIcon("images/G9_pLeft2.png"));
        pRight = scale(new ImageIcon("images/G9_pRight.png"));
        pRightW = scale(new ImageIcon("images/G9_pRight1.png"));
        pRightW2 = scale(new ImageIcon("images/G9_pRight2.png"));
        
        npcKid = scale(new ImageIcon("images/G9_caseohT.png"));
        tYoyo = scale(new ImageIcon("images/G9_yoyo.png"));
        tCar = scale(new ImageIcon("images/G9_locket.png"));
        tKite = scale(new ImageIcon("images/G9_torn kite.png"));
        tBear = scale(new ImageIcon("images/G9_teddy.png"));

        character = new JLabel[mapW * mapH];
        characterLayout = new int[mapW * mapH];
        toys = new JLabel[mapW * mapH];
        for (int i = 0; i < toys.length; i++) toys[i] = new JLabel();

        characterLayout = new int[]{
            0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,2,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,
            1,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0
        };

        for (int i = 0; i < character.length; i++) {
            character[i] = new JLabel();
            if (characterLayout[i] == 1) {
                character[i].setIcon(pFront);
                characterPosition = i;
            } else if (characterLayout[i] == 2) {
                character[i].setIcon(npcKid);
            }
        }

        mapLayout = new int[]{
            0,3,0,0,1,0,2,0,1,0,2,
            2,0,0,3,0,0,0,2,0,0,0,
            0,3,0,0,0,0,1,0,0,3,0,
            0,0,1,0,0,1,0,0,3,0,0,
            0,0,0,0,0,0,0,0,0,0,0,
            15,15,7,9,9,9,9,9,9,9,9,
            13,13,4,6,6,6,6,6,6,6,6,
            13,13,4,6,6,6,6,6,6,6,6,
            13,13,4,6,6,6,6,6,6,6,6,
            13,13,4,6,6,6,6,6,6,6,6,
            14,14,5,8,8,8,8,8,8,8,8,
        };

        tiles = new JLabel[mapW * mapH];
        for (int i = 0; i < tiles.length; i++) {
            if (mapLayout[i] == 0) tiles[i] = new JLabel(skybg);
            else if (mapLayout[i] == 1) tiles[i] = new JLabel(cld1);
            else if (mapLayout[i] == 2) tiles[i] = new JLabel(cld2);
            else if (mapLayout[i] == 3) tiles[i] = new JLabel(cld3);
            else if (mapLayout[i] == 4) tiles[i] = new JLabel(cMLTile);
            else if (mapLayout[i] == 5) tiles[i] = new JLabel(cBLTile);
            else if (mapLayout[i] == 6) tiles[i] = new JLabel(cMidTile);
            else if (mapLayout[i] == 7) tiles[i] = new JLabel(cTLTile);
            else if (mapLayout[i] == 8) tiles[i] = new JLabel(cMBTile);
            else if (mapLayout[i] == 9) tiles[i] = new JLabel(cTMTile);
            else if (mapLayout[i] == 10) tiles[i] = new JLabel(cTRTile);
            else if (mapLayout[i] == 11) tiles[i] = new JLabel(cMRTile);
            else if (mapLayout[i] == 12) tiles[i] = new JLabel(cBRTile);
            else if (mapLayout[i] == 13) tiles[i] = new JLabel(sMTile);
            else if (mapLayout[i] == 14) tiles[i] = new JLabel(sBTile);
            else if (mapLayout[i] == 15) tiles[i] = new JLabel(sTTile);
        }

        for (int ci = 0; ci < 4; ci++) {
            do {
                dupe = false;
                toyLoc[ci] = (int) (Math.random() * 32 + 1);
                for (int prev = 0; prev < ci; prev++) {
                    if (toyLoc[ci] == toyLoc[prev]) { dupe = true; break; }
                }
            } while (dupe);
        }
        toyLayout = new int[]{
            0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,1,2,3,4,5,6,7,8,
            0,0,0,9,10,11,12,13,14,15,16,
            0,0,0,17,18,19,20,21,22,23,24,
            0,0,0,25,26,27,28,29,30,31,32,
            0,0,0,0,0,0,0,0,0,0,0
        };
    }

    private ImageIcon scale(ImageIcon icon) {
        return new ImageIcon(icon.getImage().getScaledInstance(frW / mapW, frH / mapH, Image.SCALE_REPLICATE));
    }

    public void setFrame() {
        layers.setLayout(new GraphPaperLayout(new Dimension(mapW, mapH)));
        layers.setBounds(0, 0, frW, frH);
        frame.setContentPane(layers);
        int x = 0, y = 0, w = 1, h = 1;
        for (int i = 0; i < tiles.length; i++) {
            layers.add(tiles[i], new Rectangle(x, y, w, h), Integer.valueOf(0));
            x++; if (x % mapW == 0) { x = 0; y++; }
        }
        x = 0; y = 0;
        for (int i = 0; i < toys.length; i++) {
            layers.add(toys[i], new Rectangle(x, y, w, h), Integer.valueOf(1));
            x++; if (x % mapW == 0) { x = 0; y++; }
        }
        x = 0; y = 0;
        for (int i = 0; i < character.length; i++) {
            layers.add(character[i], new Rectangle(x, y, w, h), Integer.valueOf(2));
            x++; if (x % mapW == 0) { x = 0; y++; }
        }
        frame.setSize(frW, frH);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setResizable(false);
        frame.addKeyListener(this);
        dg.addKey(frame);
        frame.setVisible(true);
        frame.requestFocusInWindow();
    }

    private void checkKey(int keyCode) throws WASDException, InvalidKeyException, TooManyErrorsException {
        // Track errors for all invalid keys
        boolean isWASD = (keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_A || 
                          keyCode == KeyEvent.VK_S || keyCode == KeyEvent.VK_D);
        boolean isSpace = (keyCode == KeyEvent.VK_SPACE);
        boolean isArrow = (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_LEFT ||
                                  keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_DOWN);
        
        
        if (!isSpace && !isArrow) {
            wrongKeyCount++;
            
            if(isFirstTimeM == true && isWASD) {
                wrongKeyCount = 0; 
                isFirstTimeM = false;
                throw new TooManyErrorsException("USE ARROW KEYS FOR MOVEMENT.");
            } else if (isFirstTimeI == true && !isArrow && !isWASD && !isSpace){
                wrongKeyCount = 0; 
                isFirstTimeI = false;
                throw new TooManyErrorsException("USE SPACEBAR TO INTERACT.");
            }
            
            if(wrongKeyCount==5 && (!isFirstTimeM || !isFirstTimeI)) {
                wrongKeyCount = 0; 
                throw new TooManyErrorsException("DON'T FORGET TO USE ARROW KEYS FOR MOVEMENT AND SPACEBAR TO INTERACT.");
            }
        } else {
            wrongKeyCount=0;
        }
        
        // Optional: Reset counter if they finally press a right key
        // wrongKeyCount = 0;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (dg.isVisible()) return;
        int keyCode = e.getKeyCode();
        try {
            checkKey(keyCode);
            if (keyCode == KeyEvent.VK_RIGHT) { direction = 3; movement = 1; }
            else if (keyCode == KeyEvent.VK_LEFT) { direction = 2; movement = -1; }
            else if (keyCode == KeyEvent.VK_DOWN) { direction = 1; movement = mapW; }
            else if (keyCode == KeyEvent.VK_UP) { direction = 0; movement = mapW * -1; }
            else if (keyCode == KeyEvent.VK_SPACE) { handleInteraction(); return; }
            processMovement();
        } catch (WASDException | InvalidKeyException | TooManyErrorsException ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage(), "Input Error", JOptionPane.WARNING_MESSAGE);
            frame.requestFocusInWindow();
        }
    }

    private void handleInteraction() {
        int targetTile = -1;
        if (direction == 0) targetTile = characterPosition - mapW;
        else if (direction == 1) targetTile = characterPosition + mapW;
        else if (direction == 2 && characterPosition % mapW != 0) targetTile = characterPosition - 1;
        else if (direction == 3 && (characterPosition + 1) % mapW != 0) targetTile = characterPosition + 1;

        if (targetTile >= 0 && targetTile < characterLayout.length && characterLayout[targetTile] == 2) {
            if (invCont == 4) { showReturnDialogue(); return; }
            if (!talked) { showQuizDialogue(); return; }
        }

        if (talked && invCont < 4) {
            if (toyLayout[characterPosition] >= 1 && toyLayout[characterPosition] <= 32) {
                boolean toyFound = false;
                if (toyLayout[characterPosition] == toyLoc[0] && toys[characterPosition].getIcon() == null) { toys[characterPosition].setIcon(tYoyo); toyFound = true; }
                else if (toyLayout[characterPosition] == toyLoc[1] && toys[characterPosition].getIcon() == null) { toys[characterPosition].setIcon(tCar); toyFound = true; }
                else if (toyLayout[characterPosition] == toyLoc[2] && toys[characterPosition].getIcon() == null) { toys[characterPosition].setIcon(tKite); toyFound = true; }
                else if (toyLayout[characterPosition] == toyLoc[3] && toys[characterPosition].getIcon() == null) { toys[characterPosition].setIcon(tBear); toyFound = true; }
                
                if (toyFound) {
                    invCont++;
                    if (invCont == 4) JOptionPane.showMessageDialog(frame, "YOU FOUND ALL ITEMS! RETURN TO THE NPC.");
                } else if (indicator == 0) {
                    JOptionPane.showMessageDialog(frame, "JUST A BUNCH OF FLUFFY CLOUDS... TRY ANOTHER SPOT!");
                    indicator++;
                }
            } else if (outIndicator == 0) {
                JOptionPane.showMessageDialog(frame, "NOTHING TO INTERACT WITH OR DIG HERE.");
                outIndicator++;
            }
        }
    }

    private void showQuizDialogue() {
        SwingUtilities.invokeLater(() -> {
            if (quizStep == 0) {
                String[] lines = {
                    "BEFORE THE SKY WAS DISRUPTED… LUMINARA AL-QAMAR WAS PEACEFUL. MY PARENTS AND I WERE HAPPY HERE.",
                    "THEN THOSE 3 MONSTERS WHO USED TO FIGHT EACH OTHER JOINED FORCES AND TOOK IT UPON US!",
                    "THEY WERE PITILESS.. I SWEAR DURING THE MASSACRE IT WENT CRACK-THOOM!",
                    "AND THAT MOMENT BECAME THE GOSSAMER SNAP.",
                    "MY DAD WAS A BIG FAN OF OLD STORIES, HE WAS A GREEK HOBBYIST...",
                    "HE SAID OUR HOME FELT LIKE A LEGEND, AND EVEN THE GODS KNEW WHAT IT WAS LIKE TO LOSE EVERYTHING TO THE DARK.",
                    "MY DAD TOLD ME STORIES ABOUT A SUPER-DUPER LONG TIME AGO... BEFORE ALL THIS HAPPENED...",
                    "HE SAID THE GIANT, OLD GODS FOUGHT THE YOUNGER ONES AND THE WHOLE WORLD WENT BOOM.",
                    "DO YOU KNOW THE NAME OF THAT BIG, SCARY WAR?"
                };
                String[] options = {"THE ODYSSEY", "THE TITANOMACHY", "THE ILIAD"};
                Runnable[] branches = {
                    () -> dg.show(layers, new String[]{"MMM... NAH IT WAS THE TITANOMACHY!"}, null, null, mapW, mapH),
                    () -> { quizStep = 1; showQuizQuestion2(); },
                    () -> dg.show(layers, new String[]{"HMM I DONT THINK SO... IT WAS THE TITANOMACHY!"}, null, null, mapW, mapH)
                };
                dg.show(layers, lines, options, branches, mapW, mapH);
            } else if (quizStep == 1) showQuizQuestion2();
            else if (quizStep == 2) showQuizQuestion3();
            else if (quizStep == 3) showQuizQuestion4();
            else if (quizStep == 4) showFinalClues();
        });
    }

    private void showQuizQuestion2() {
        String[] q2Lines = {
            "OH YEAHH! I REMEMBER NOW! TI-TAN-O-MA-CHY! THAT’S THE ONE WHERE THE TITANS FELL...",
            "JUST LIKE HOW THE CLOUDS FELL HERE. IT’S A REALLY BIG WORD FOR A REALLY BIG MESS!",
            "WHEN THE MONSTERS CAME TO LUMINARA AL-QAMAR, EVERYTHING GOT ALL FOGGY AND WEIRD...",
            "MY DADDY SAID BEFORE THERE WERE TREES OR BIRDS OR EVEN LIGHT, THERE WAS JUST A BIG 'NOTHING.' WHAT DID HE CALL THAT BIG, EMPTY 'NOTHING'?"
        };
        String[] q2Options = {"CHAOS", "ELYSIUM", "OLYMPUS"};
        Runnable[] q2Branches = {
            () -> { quizStep = 2; showQuizQuestion3(); },
            () -> dg.show(layers, new String[]{"EHHH NO..? WASN'T IT CHAOS?"}, null, null, mapW, mapH),
            () -> dg.show(layers, new String[]{"HMM I DONT THINK SO... IT WAS CHAOS!"}, null, null, mapW, mapH)
        };
        dg.show(layers, q2Lines, q2Options, q2Branches, mapW, mapH);
    }

    private void showQuizQuestion3() {
        String[] q3Lines = {
            "YES! THAT’S IT! CHAOS! IT’S LIKE WHEN I SPILL ALL MY CRAYONS ON THE FLOOR AND EVERYTHING IS MESSY, BUT WAY BIGGER.",
            "EVERYTHING FEELS LIKE CHAOS RIGHT NOW, DOESN'T IT?",
            "I SAW MY MOM’S FAVORITE VASE BREAK... AND THEN EVERYTHING ELSE BROKE TOO.",
            "DADDY SAID THERE ARE THREE SISTERS WHO HOLD A LONG, LONG STRING OF OUR LIVES.",
            "WHEN THEY GET TIRED, THEY GO SNIP-SNIP WITH THEIR SCISSORS. DO YOU KNOW THEIR NAMES?"
        };
        String[] q3Options = {"THE MUSES", "THE FURIES", "THE MOIRAI"};
        Runnable[] q3Branches = {
            () -> dg.show(layers, new String[]{"EHHH NO..? WASN'T IT THE MOIRAI?"}, null, null, mapW, mapH),
            () -> dg.show(layers, new String[]{"HMM I DONT THINK SO... IT WAS THE MOIRAI!"}, null, null, mapW, mapH),
            () -> { quizStep = 3; showQuizQuestion4(); }
        };
        dg.show(layers, q3Lines, q3Options, q3Branches, mapW, mapH);
    }

    private void showQuizQuestion4() {
        String[] q4Lines = {
            "OOOH!! YOU GOT IT RIGHT! THE MOI-RAI! THEY’RE THE MEAN SISTERS WITH THE SCISSORS. I WISH THEY DIDN’T HAVE THEM...",
            "THEN THEY WOULDN'T HAVE CUT MY STORY AWAY FROM MY PARENTS.",
            "SNIFF I DON’T LIKE IT HERE... IT’S TOO QUIET. I’M SCARED I’LL FORGET WHAT MY DADDY’S VOICE SOUNDS LIKE.",
            "HE TOLD ME ABOUT A RIVER WHERE IF YOU DRINK THE WATER, YOUR BRAIN GOES ALL BLANK AND YOU FORGET YOUR NAME. WHAT WAS THAT RIVER?"
        };
        String[] q4Options = {"THE RIVER STYX", "THE RIVER LETHE", "THE RIVER ACHERON"};
        Runnable[] q4Branches = {
            () -> dg.show(layers, new String[]{"MMM... NAH IT WAS THE RIVER LETHE!"}, null, null, mapW, mapH),
            () -> { quizStep = 4; showFinalClues(); },
            () -> dg.show(layers, new String[]{"HMM I DONT THINK SO... IT WAS THE RIVER LETHE!"}, null, null, mapW, mapH)
        };
        dg.show(layers, q4Lines, q4Options, q4Branches, mapW, mapH);
    }

    private void showFinalClues() {
        talked = true;
        String[] finalLines = {
            "WHOA, HOW DO YOU KNOW ALL THIS! LETHE! THAT’S THE ONE! I’M GONNA KEEP MY MOUTH REAL TIGHT SO I DON'T DRINK ANY.",
            "I NEVER, EVER WANT TO FORGET THEM.",
            "SO MANY PEOPLE…. MY MOM…MY DAD.. WITH THAT, PEOPLE LIKE ME ARE SCATTERED ALL AROUND, WHERE A HAVEN USED TO EXIST.",
            "WHEN THE GROUND SHOOK AND THE CLOUDS BROKE APART, THE THINGS THEY GAVE ME WERE TAKEN TOO..",
            "MY MOST VALUABLE ITEMS WERE ALL LOST AT THE SAME MOMENT I LOST THEM.",
            "THESE ITEMS WERE ALL THAT CONNECT ME TO MY PARENTS.. AND I SNIFF THINK THEY'D GIVE ME A REASON TO KEEP GOING…",
            "CAN YOU HELP ME? …PLEASE?",
            "BACK WHEN I WAS SCARED, SOFT AND SMALL. MY DAD CALLED IT MY LITTLE CALLISTO. FIND THIS… AND YOU’LL TOUCH MY MEMORIES.",
            "IT DANCED WITH THE WIND, LIKE THE WINGS OF ICARUS. FIND THIS… AND YOU’LL FIND A PIECE OF US.",
            "MOVES UP AND DOWN, LIKE PERSEPHONE VISITING THE DARK. FIND THIS… AND YOU’LL FEEL ITS ENDLESS SPIN.",
            "SMALL, HOLDS FACES AND TIME. IT’S MY OWN MNEMOSYNE—MY GODDESS OF MEMORY. FIND THIS… AND YOU’LL HOLD WHAT KEEPS ME STANDING."
        };
        dg.show(layers, finalLines, null, null, mapW, mapH);
    }

    private void showReturnDialogue() {
        SwingUtilities.invokeLater(() -> {
            if (returnStep == 0) {
                String[] riddle = {"DID YOU FIND THE ONE MY MOM HANDED ME WHEN STORMS GOT LOUD? I COULD HOLD IT TIGHT WHEN I WAS SCARED...",
                    "IT WAS SOFT ENOUGH THAT I DIDN’T FEEL ALONE."};
                String[] options = {"BEAR", "KITE", "YO-YO", "LOCKET"};
                Runnable[] branches = {
                    () -> { returnStep = 1; dg.show(layers, new String[]{"YOU FOUND IT… I HAVEN’T HELD IT IN YEARS. IT SMELLS FAINTLY LIKE HOME."}, null, null, mapW, mapH); },
                    () -> dg.show(layers, new String[]{"NO... THAT'S NOT THE SOFT ONE."}, null, null, mapW, mapH),
                    () -> dg.show(layers, new String[]{"NO... THAT'S NOT THE SOFT ONE."}, null, null, mapW, mapH),
                    () -> dg.show(layers, new String[]{"NO... THAT'S NOT THE SOFT ONE."}, null, null, mapW, mapH)
                };
                dg.show(layers, riddle, options, branches, mapW, mapH);
            } 
            else if (returnStep == 1) {
                String[] riddle = {"GIVE ME THE ONE THAT DANCED WITH THE WIND... EVERY PULL FELT LIKE WE WERE TOGETHER."};
                String[] options = {"BEAR", "KITE", "YO-YO", "LOCKET"};
                Runnable[] branches = {
                    () -> dg.show(layers, new String[]{"I ALREADY HAVE THAT ONE!"}, null, null, mapW, mapH),
                    () -> { returnStep = 2; dg.show(layers, new String[]{"IT’S DAMAGED. BUT I REMEMBER THE WAY THEY SMILED WHEN IT FLEW. MAYBE MEMORIES DON’T BREAK AS EASILY..."}, null, null, mapW, mapH); },
                    () -> dg.show(layers, new String[]{"NO... THAT ONE DOESN'T FLY."}, null, null, mapW, mapH),
                    () -> dg.show(layers, new String[]{"NO... THAT ONE DOESN'T FLY."}, null, null, mapW, mapH)
                };
                dg.show(layers, riddle, options, branches, mapW, mapH);
            }
            else if (returnStep == 2) {
                String[] riddle = {"HOW ABOUT THE ONE THAT DANCES ON A STRING? SHE PROMISED IT WOULD LISTEN IF I CALLED IT BACK."};
                String[] options = {"BEAR", "KITE", "YO-YO", "LOCKET"};
                Runnable[] branches = {
                    () -> dg.show(layers, new String[]{"NO... THAT'S NOT IT."}, null, null, mapW, mapH),
                    () -> dg.show(layers, new String[]{"NO... THAT'S NOT IT."}, null, null, mapW, mapH),
                    () -> { returnStep = 3; dg.show(layers, new String[]{"THIS TAUGHT ME A QUIET LESSON. IT ALWAYS GOES DOWN BEFORE IT RISES."}, null, null, mapW, mapH); },
                    () -> dg.show(layers, new String[]{"NO... THAT'S NOT IT."}, null, null, mapW, mapH)
                };
                dg.show(layers, riddle, options, branches, mapW, mapH);
            }
            else if (returnStep == 3) {
                String[] riddle = {"AND FINALLY... IT’S SMALL, BUT IT CARRIES FACES AND TIME. WHAT KEEPS ME STANDING?"};
                String[] options = {"BEAR", "KITE", "YO-YO", "LOCKET"};
                Runnable[] branches = {
                    () -> dg.show(layers, new String[]{"HMM... NO."}, null, null, mapW, mapH),
                    () -> dg.show(layers, new String[]{"HMM... NO."}, null, null, mapW, mapH),
                    () -> dg.show(layers, new String[]{"HMM... NO."}, null, null, mapW, mapH),
                    () -> {
                        String[] finalLines = {
                            "…THEY’RE STILL HERE. WHEN I HOLD THIS, I DON’T FEEL ERASED.",
                            "THE SKY STILL HURTS. BUT NOW IT FEELS FAMILIAR.",
                            "YES. BECAUSE THEY TAUGHT ME HOW TO KEEP GOING. AND NOW… YOU DID TOO."
                        };
                        dg.show(layers, finalLines, null, null, mapW, mapH);
                    }
                };
                dg.show(layers, riddle, options, branches, mapW, mapH);
            }
        });
    }

    private void processMovement() {
        int targetTile = characterPosition + movement;
        if (targetTile >= 0 && targetTile < mapLayout.length && mapLayout[targetTile] != 0 && characterLayout[targetTile] != 2) {
            if (direction == 3 && (characterPosition + 1) % mapW == 0) canMove = false;
            else if (direction == 2 && characterPosition % mapW == 0) canMove = false;
            else canMove = true;
        } else canMove = false;

        if (canMove) {
            character[characterPosition].setIcon(null);
            characterLayout[characterPosition] = 0;
            characterPosition = targetTile;
            if (direction == 0) CharacterModes(pBack, pBackW, pBackW2);
            else if (direction == 1) CharacterModes(pFront, pFrontW, pFrontW2);
            else if (direction == 2) CharacterModes(pLeft, pLeftW, pLeftW2);
            else if (direction == 3) CharacterModes(pRight, pRightW, pRightW2);
            characterLayout[characterPosition] = 1;
        } else {
            if (direction == 0) character[characterPosition].setIcon(pBack);
            else if (direction == 1) character[characterPosition].setIcon(pFront);
            else if (direction == 2) character[characterPosition].setIcon(pLeft);
            else if (direction == 3) character[characterPosition].setIcon(pRight);
        }
        movement = 0;
    }

    private void CharacterModes(ImageIcon idle, ImageIcon walk1, ImageIcon walk2) {
        if (characterMode == 0) { character[characterPosition].setIcon(walk1); characterMode = 1; }
        else if (characterMode == 1) { character[characterPosition].setIcon(walk2); characterMode = 2; }
        else { character[characterPosition].setIcon(idle); characterMode = 0; }
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}

    class WASDException extends Exception {
        public WASDException(String message) {
            super(message);
        }
    }

    class InvalidKeyException extends Exception {
        public InvalidKeyException(String message) {
            super(message);
        }
    }

    class TooManyErrorsException extends Exception {
        public TooManyErrorsException(String message) {
            super(message);
        }
    }
}

/*

- FIX DIALOGUE
- REMOVE SOME QUESTIONS ?
also had help from gemini
*/
