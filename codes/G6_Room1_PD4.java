//Guanine Group 6: Estrellan, Gomez, Pañares

package codes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import java.util.ArrayList;
import java.util.Collections;
/*This is the main maze code, you first have to collect all the balls. Once that is done, a dino game will pop up
and you have to get a score of 10k (it automatically closes if you do). After that, you can finally move to the exit. */

public class Map1_G6_PD4 implements KeyListener{

    JFrame frame;
    ImageIcon wall, path, ball, exit;
    JLabel tiles[];
    JLabel chr[];
    ImageIcon icon,icon2, icon3, icon4, icon5, icon6, icon7, icon8, icon9, icon10;

    int mapLayout[];
    int chrpos;
    int chrmodeR, chrmodeL, chrmodeD, chrmodeU;

    int mapw = 11;
    int maph = 11;
    int frameWidth = 650;
    int frameHeight = 650;

    int totalBalls = 0;
    int collectedBalls = 0;
    boolean dinoPlayed = false;

    String[] questions = new String[30];
    String[][] choices = new String[30][3];
    int[] correctAnswer = new int[30];
    ArrayList<Integer> unusedQuestions = new ArrayList<>();
    Random rand = new Random();

    public Map1_G6_PD4(){
        frame = new JFrame();

        icon=new ImageIcon("images/G6_char1.png");
        icon2=new ImageIcon("images/G6_char2.png");
        icon3=new ImageIcon("images/G6_char3.png");
        icon4=new ImageIcon("images/G6_char4.png");
        icon5=new ImageIcon("images/G6_char5.png");
        icon6=new ImageIcon("images/G6_char6.png");
        icon7=new ImageIcon("images/G6_char7.png");
        icon8=new ImageIcon("images/G6_char8.png");
        icon9=new ImageIcon("images/G6_char9.png");
        icon10=new ImageIcon("images/G6_char10.png");

        wall=new ImageIcon("images/G6_wall.png");
        path=new ImageIcon("images/G6_bluepath.png");
        ball=new ImageIcon("images/G6_ball.png");
        exit=new ImageIcon("images/G6_door.png");

        wall=new ImageIcon(wall.getImage().getScaledInstance((frameWidth/mapw), (frameHeight/maph), Image.SCALE_DEFAULT));
        path=new ImageIcon(path.getImage().getScaledInstance((frameWidth/mapw), (frameHeight/maph), Image.SCALE_DEFAULT));
        ball=new ImageIcon(ball.getImage().getScaledInstance((frameWidth/mapw), (frameHeight/maph), Image.SCALE_DEFAULT));
        exit=new ImageIcon(exit.getImage().getScaledInstance((frameWidth/mapw), (frameHeight/maph), Image.SCALE_DEFAULT));
      
        icon=new ImageIcon(icon.getImage().getScaledInstance((frameWidth/mapw), (frameHeight/maph), Image.SCALE_DEFAULT));
        icon2=new ImageIcon(icon2.getImage().getScaledInstance((frameWidth/mapw), (frameHeight/maph), Image.SCALE_DEFAULT));
        icon3=new ImageIcon(icon3.getImage().getScaledInstance((frameWidth/mapw), (frameHeight/maph), Image.SCALE_DEFAULT));
        icon4=new ImageIcon(icon4.getImage().getScaledInstance((frameWidth/mapw), (frameHeight/maph), Image.SCALE_DEFAULT));
        icon5=new ImageIcon(icon5.getImage().getScaledInstance((frameWidth/mapw), (frameHeight/maph), Image.SCALE_DEFAULT));
        icon6=new ImageIcon(icon6.getImage().getScaledInstance((frameWidth/mapw), (frameHeight/maph), Image.SCALE_DEFAULT));
        icon7=new ImageIcon(icon7.getImage().getScaledInstance((frameWidth/mapw), (frameHeight/maph), Image.SCALE_DEFAULT));
        icon8=new ImageIcon(icon8.getImage().getScaledInstance((frameWidth/mapw), (frameHeight/maph), Image.SCALE_DEFAULT));
        icon9=new ImageIcon(icon9.getImage().getScaledInstance((frameWidth/mapw), (frameHeight/maph), Image.SCALE_DEFAULT));
        icon10=new ImageIcon(icon10.getImage().getScaledInstance((frameWidth/mapw), (frameHeight/maph), Image.SCALE_DEFAULT));
        
        mapLayout = new int[]{
            0,0,0,0,0,0,0,0,0,0,0,
            4,1,1,1,1,1,0,1,1,1,0,
            0,2,0,1,0,1,0,2,0,1,0,
            0,1,0,1,0,1,1,1,1,1,0,
            0,0,0,1,0,0,0,0,1,1,0,
            0,1,0,1,1,1,1,0,2,0,0,
            0,1,1,1,0,1,1,1,1,1,0,
            0,2,0,1,0,0,1,0,0,0,0,
            0,1,0,1,2,0,1,1,1,1,0,
            0,1,1,1,0,0,2,0,1,1,3,
            0,0,0,0,0,0,0,0,0,0,0
        };

        for (int i:mapLayout){
            if (i==2) totalBalls++;
        }

        tiles=new JLabel[mapw*maph];
        chr=new JLabel[mapw*maph];

        for (int i=0; i<tiles.length; i++){
            if (mapLayout[i]==0) tiles[i]=new JLabel(wall);
            else if (mapLayout[i]==1) tiles[i]=new JLabel(path);
            else if (mapLayout[i]==2) tiles[i]=new JLabel(ball);
            else if (mapLayout[i]==3) tiles[i]=new JLabel(exit);
            else tiles[i]=new JLabel(path);
        }

        for (int i=0; i<chr.length; i++){
            chr[i]=new JLabel();
            if (mapLayout[i]==4) {
                chr[i].setIcon(icon);
                chrpos=i;
            }
        }

        initQuiz();
    }
    void initQuiz() {
    String[] q = {
        "Which activity improves cardiovascular endurance?",
        "Which is NOT a PE activity?",
        "What should you do after exercise?",
        "Which improves flexibility?",
        "Which improves muscular strength?",

        "How many players per team are on a basketball court?",
        "What is the standard height of a basketball ring?",
        "Which skill is used to move while bouncing the ball?",
        "How many points is a free throw worth?",
        "Which violation is walking without dribbling?",

        "What object is hit in badminton?",
        "Which equipment is used to hit the shuttlecock?",
        "How many players in singles badminton?",
        "What divides the badminton court?",
        "Which serve is legal in badminton?",

        "How many players per team on a volleyball court?",
        "What action starts a volleyball rally?",
        "Which skill is used to pass the ball upward?",
        "How many touches are allowed per team?",
        "Which hit sends the ball strongly over the net?",

        "How many players per team in football (soccer)?",
        "Which body part cannot touch the ball in football?",
        "What do you call scoring with the head?",
        "Which player can use hands in soccer?",
        "What card means a player is sent off?",

        "What object is thrown in ultimate disc?",
        "How do players move with the disc?",
        "How is a point scored in ultimate disc?",
        "Ultimate disc is a ___ contact sport?",
        "In ultimate disc, you cannot run while holding the disc. (True or False?)"
    };

    String[][] c = {
        {"Sprint","Long run","Shot put"},
        {"Dance","Basketball","Chess"},
        {"Cool down","Stop","Sleep"},
        {"Stretching","Sprinting","Eating"},
        {"Weight lifting","Jogging","Yoga"},

        {"5","6","11"},
        {"3.05 meters","2 meters","4 meters"},
        {"Dribbling","Passing","Shooting"},
        {"1","2","3"},
        {"Traveling","Double dribble","Foul"},

        {"Ball","Shuttlecock","Disc"},
        {"Racket","Bat","Stick"},
        {"1","2","6"},
        {"Net","Baseline","Service line"},
        {"Underhand","Overhand","Kick"},

        {"6","5","11"},
        {"Serve","Spike","Block"},
        {"Set","Kick","Dribble"},
        {"3","2","5"},
        {"Spike","Set","Dig"},

        {"11","5","6"},
        {"Hand","Foot","Head"},
        {"Header","Volley","Pass"},
        {"Goalkeeper","Defender","Striker"},
        {"Red","Yellow","Blue"},

        {"Frisbee","Ball","Shuttlecock"},
        {"By passing only","By running with it","By kicking"},
        {"Catching in end zone","Hitting net","Crossing line"},
        {"Non-contact","Full-contact","Combat"},
        {"True","False","Sometimes"}
    };

    int[] ans = {
        1,2,0,0,0,
        0,0,0,0,0,
        1,0,0,0,0,
        0,0,0,0,0,
        0,0,0,0,0,
        0,1,0,0,0  
    };

    unusedQuestions.clear();

    for(int i=0;i<30;i++){
        questions[i] = q[i];
        choices[i] = c[i];
        correctAnswer[i] = ans[i];
        unusedQuestions.add(i);
    }

    Collections.shuffle(unusedQuestions);
}


    boolean askQuestion() {
    if (unusedQuestions.isEmpty()) return true;

    int qIndex = unusedQuestions.remove(0);
    
    String[] shuffledChoices = choices[qIndex].clone();

    String correctText = shuffledChoices[correctAnswer[qIndex]];

    ArrayList<String> choiceList = new ArrayList<>();
    Collections.addAll(choiceList, shuffledChoices);
    Collections.shuffle(choiceList);

    shuffledChoices = choiceList.toArray(new String[0]);

    int newCorrectIndex = 0;
    for (int i = 0; i < shuffledChoices.length; i++) {
        if (shuffledChoices[i].equals(correctText)) {
            newCorrectIndex = i;
            break;
        }
    }

    int answer = JOptionPane.showOptionDialog(
            frame,
            questions[qIndex],
            "PE Question",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            shuffledChoices,
            shuffledChoices[0]
    );

    if (answer == newCorrectIndex) {
        JOptionPane.showMessageDialog(frame, "Correct!");
        return true;
    } else {
        JOptionPane.showMessageDialog(frame, "Wrong! You cannot collect this ball yet.");
        return false;
    }
}


    public void setFrame(){
        frame.setLayout(new GraphPaperLayout(new Dimension(mapw, maph)));

        int x=0, y=0;
        for (int i=0; i<tiles.length; i++){
            frame.add(chr[i], new Rectangle(x,y,1,1));
            frame.add(tiles[i], new Rectangle(x,y,1,1));
            x++;
            if (x%mapw==0){
                x = 0;
                y++;
            }
        }

        frame.setSize(frameWidth, frameHeight);
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.addKeyListener(this);
    }

    @Override
public void keyPressed(KeyEvent e){
    //Exception Handling code that handles invalid keyboard keys for movement.
    try{
        if (e.getKeyCode()==KeyEvent.VK_RIGHT){
            if(mapLayout[chrpos+1] != 0){
                chr[chrpos].setIcon(null);
                if(chrmodeR==0){
                    chr[chrpos+1].setIcon(icon2);
                    chrmodeR=1;
                } else {
                    chr[chrpos+1].setIcon(icon3);
                    chrmodeR=0;
                }
                chrpos++;
            }
        }       
        else if (e.getKeyCode() == KeyEvent.VK_LEFT){
            if(mapLayout[chrpos-1] != 0){
                chr[chrpos].setIcon(null);
                if(chrmodeL==0){
                    chr[chrpos-1].setIcon(icon4);
                    chrmodeL=1;
                } else {
                    chr[chrpos-1].setIcon(icon5);
                    chrmodeL=0;
                }
                chrpos--;
            }
        }
        else if (e.getKeyCode() == KeyEvent.VK_DOWN){
            if(mapLayout[chrpos+mapw]!=0){
                chr[chrpos].setIcon(null);
                chrpos += mapw;

                if(chrmodeD==0){
                    chr[chrpos].setIcon(icon);
                    chrmodeD=1;
                }
                else if(chrmodeD==1){
                    chr[chrpos].setIcon(icon6);
                    chrmodeD=2;
                }
                else{
                    chr[chrpos].setIcon(icon7);
                    chrmodeD=0;
                }
            }
        }
        else if (e.getKeyCode() == KeyEvent.VK_UP){
            if(mapLayout[chrpos-mapw]!= 0){
                chr[chrpos].setIcon(null);
                chrpos -= mapw;

                if(chrmodeU==0){
                    chr[chrpos].setIcon(icon8);
                    chrmodeU=1;
                }
                else if(chrmodeU==1){
                    chr[chrpos].setIcon(icon9);
                    chrmodeU=2;
                }
                else{
                    chr[chrpos].setIcon(icon10);
                    chrmodeU=0;
                }
            }
        }
        else{
            throw new Exception();
        }
    }catch(Exception ex){
        JOptionPane.showMessageDialog(frame,
            "Invalid input. Please enter a valid command for movement.");
    }

    if (mapLayout[chrpos]==2){

        boolean correct = askQuestion();

        if(correct){
            collectedBalls++;
            mapLayout[chrpos]=1;
            tiles[chrpos].setIcon(path);

            JOptionPane.showMessageDialog(frame,
                "Ball collected! (" + collectedBalls + "/" + totalBalls + ")");

            if (collectedBalls==totalBalls && !dinoPlayed){
                launchDinoGame();
                dinoPlayed=true;
            }
        }
    }

    if (mapLayout[chrpos]==3){
        if (!dinoPlayed){
            JOptionPane.showMessageDialog(frame,"Exit is locked!\nCollect all balls first.");
        }else{
            JOptionPane.showMessageDialog(frame,"You escaped successfully!");
        }
    }
}

    void launchDinoGame(){
    JOptionPane.showMessageDialog(frame, 
        "All balls collected!\nLaunching Dino Game...");

    frame.setEnabled(false);

    JFrame dinoFrame = new JFrame("Dino Game");
    dinogame game = new dinogame();

    dinoFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    dinoFrame.add(game);
    dinoFrame.pack();
    dinoFrame.setResizable(false);
    dinoFrame.setLocationRelativeTo(frame);

    dinoFrame.addWindowListener(new java.awt.event.WindowAdapter() {
        @Override
        public void windowClosed(java.awt.event.WindowEvent e) {
            frame.setEnabled(true);
            frame.toFront();
            frame.requestFocus();
        }
    });

    dinoFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    dinoFrame.addWindowListener(new java.awt.event.WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
            JOptionPane.showMessageDialog(dinoFrame,
                "You cannot close the game! Finish it to continue.",
                "Warning", JOptionPane.WARNING_MESSAGE);
        }
    });

    dinoFrame.setVisible(true);
}

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}
