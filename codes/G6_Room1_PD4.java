//Guanine Group 6: Estrellan, Gomez, Pañares
//with savesystem 
package codes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class G6_Room1_PD4 implements KeyListener{

    JFrame frame;
    ImageIcon wall, path, ball, exit;
    JLabel tiles[];
    JLabel chr[];

    ImageIcon icon,icon2, icon3, icon4, icon5, icon6, icon7, icon8, icon9, icon10, dan, tine;

    int mapLayout[];
    int chrpos;

    int mapw = 11;
    int maph = 11;
    int frameWidth = 660;
    int frameHeight = 660;

    int chrmodeR, chrmodeL, chrmodeD, chrmodeU;

    int totalBalls = 0;
    int collectedBalls = 0;

    Dialog dialog;
    boolean introShown = false;
    boolean thirdBallShown = false;
    boolean fifthBallShown = false;

    // SAVE SYSTEM
    SaveSystem.SaveData saveData;

    // QUIZ
    String[] questions = new String[30];
    String[][] choices = new String[30][3];
    int[] correctAnswer = new int[30];
    ArrayList<Integer> unusedQuestions = new ArrayList<>();
    Random rand = new Random();

    public G6_Room1_PD4(){

        frame = new JFrame();
        dialog = new Dialog();
        Music.playLoop("music/BBR.mp3");
        loadImages();
        initMap();
        initQuiz();

        // ================= SAVE LOAD =================
        saveData = SaveSystem.loadGame("G6_Room1_PD4");
        SaveSystem.startTimer(saveData.timeSeconds);

        for(String b : saveData.battles){
            SaveSystem.markDefeated(b);
        }
    }

    // ================= LOAD IMAGES =================
    void loadImages(){

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

        dan = new ImageIcon(new ImageIcon("images/G6_dan.png")
                .getImage().getScaledInstance((frameWidth/mapw), (frameHeight/maph), Image.SCALE_DEFAULT));

        tine = new ImageIcon(new ImageIcon("images/G6_tine.png")
                .getImage().getScaledInstance((frameWidth/mapw), (frameHeight/maph), Image.SCALE_DEFAULT));
    }

    // ================= MAP =================
    void initMap(){

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

        tiles=new JLabel[mapw*maph];
        chr=new JLabel[mapw*maph];

        for (int i=0;i<tiles.length;i++){
            if (mapLayout[i]==0) tiles[i]=new JLabel(wall);
            else if (mapLayout[i]==1) tiles[i]=new JLabel(path);
            else if (mapLayout[i]==2) tiles[i]=new JLabel(ball);
            else if (mapLayout[i]==3) tiles[i]=new JLabel(exit);
            else tiles[i]=new JLabel(path);
        }

        tiles[107]=new JLabel(dan);
        tiles[105]=new JLabel(tine);

        for (int i=0;i<chr.length;i++){
            chr[i]=new JLabel();
            if (mapLayout[i]==4){
                chr[i].setIcon(icon);
                chrpos=i;
            }
        }
    }

    // ================= FRAME =================
    public void setFrame(){

        JLayeredPane layers = frame.getLayeredPane();
        layers.setLayout(new GraphPaperLayout(new Dimension(mapw, maph)));

        int x=0,y=0;

        for(int i=0;i<tiles.length;i++){
            layers.add(chr[i], new Rectangle(x,y,1,1));
            layers.add(tiles[i], new Rectangle(x,y,1,1));

            x++;
            if(x%mapw==0){ x=0; y++; }
        }

        frame.setSize(frameWidth, frameHeight);
        frame.setVisible(true);
        frame.setResizable(false);
        frame.addKeyListener(this);
        dialog.addKey(frame);

        if(!introShown){
            introShown=true;
            dialog.show(frame.getLayeredPane(),
                new String[]{
                    "You just stepped into the Blue Brick Road...",
                    "Collect all the balls."
                },
                null,null,mapw,maph);
        }
    }

    // ================= MOVEMENT =================
    @Override
    public void keyPressed(KeyEvent e){

        if(dialog!=null && dialog.isVisible()) return;

        int newPos = chrpos;

        if(e.getKeyCode()==KeyEvent.VK_RIGHT) newPos = chrpos+1;
        else if(e.getKeyCode()==KeyEvent.VK_LEFT) newPos = chrpos-1;
        else if(e.getKeyCode()==KeyEvent.VK_DOWN) newPos = chrpos+mapw;
        else if(e.getKeyCode()==KeyEvent.VK_UP) newPos = chrpos-mapw;

        // BLOCK WALL + DAN + TINE
        if(mapLayout[newPos]==0 || newPos==105 || newPos==107) return;

        chr[chrpos].setIcon(null);
        chrpos=newPos;

        // ===== DIRECTION SPRITE =====
        if(e.getKeyCode()==KeyEvent.VK_RIGHT){
            chr[chrpos].setIcon(chrmodeR==0?icon2:icon3);
            chrmodeR=1-chrmodeR;
        }
        else if(e.getKeyCode()==KeyEvent.VK_LEFT){
            chr[chrpos].setIcon(chrmodeL==0?icon4:icon5);
            chrmodeL=1-chrmodeL;
        }
        else if(e.getKeyCode()==KeyEvent.VK_DOWN){
            chr[chrpos].setIcon(chrmodeD==0?icon:icon6);
            chrmodeD=1-chrmodeD;
        }
        else if(e.getKeyCode()==KeyEvent.VK_UP){
            chr[chrpos].setIcon(chrmodeU==0?icon8:icon9);
            chrmodeU=1-chrmodeU;
        }

        // ================= BALL =================
        if(mapLayout[chrpos]==2){
            askQuestion();
        }

        // ================= EXIT SAVE =================
        if(mapLayout[chrpos]==3){
            SaveSystem.saveGame(
                new SaveSystem.SaveData.Builder("G6_Room1_PD4")
                    .battles(SaveSystem.getDefeatedBosses())
            );

            JOptionPane.showMessageDialog(frame,
                "Game Saved!\nTime: "+SaveSystem.getTotalSeconds()+"s");
        }
    }

    // ================= QUIZ =================
    void askQuestion(){

        int qIndex = unusedQuestions.remove(0);

        Runnable[] events = new Runnable[3];

        for(int i=0;i<3;i++){
            final int idx=i;
            events[i]=()->{
                if(idx==correctAnswer[qIndex]) correctBall();
                else wrongBall();
            };
        }

        dialog.show(frame.getLayeredPane(),
            new String[]{questions[qIndex]},
            choices[qIndex],
            events,
            mapw,maph);
    }

    void correctBall(){
        collectedBalls++;
        mapLayout[chrpos]=1;
        tiles[chrpos].setIcon(path);

        dialog.show(frame.getLayeredPane(),
            new String[]{"Correct!"},
            null,null,mapw,maph);
    }

    void wrongBall(){
        dialog.show(frame.getLayeredPane(),
            new String[]{"Wrong answer."},
            null,null,mapw,maph);
    }

    // ================= FULL QUIZ DATA =================
    void initQuiz(){

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
        "How many touches are allowed per team in volleyball?",
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
        {"3.05m","2m","4m"},
        {"Moving ball","Passing","Shooting"},
        {"1","2","3"},
        {"Traveling","Double dribble","Foul"},

        {"Ball","Shuttlecock","Disc"},
        {"Racket","Bat","Stick"},
        {"1","2","6"},
        {"Net","Line","Border"},
        {"Underhand","Kick","Throw"},

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
        {"Passing","Running","Kicking"},
        {"End zone","Net","Line"},
        {"Non-contact","Full","Combat"},
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

        for(int i=0;i<q.length;i++){
            questions[i]=q[i];
            choices[i]=c[i];
            correctAnswer[i]=ans[i];
            unusedQuestions.add(i);
        }

        Collections.shuffle(unusedQuestions);
    }

    @Override public void keyReleased(KeyEvent e){}
    @Override public void keyTyped(KeyEvent e){}
    public static void main(String[] args) {
        G6_Room1_PD4 sg=new G6_Room1_PD4();
        sg.setFrame();
    }
}