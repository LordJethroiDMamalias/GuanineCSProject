package codes;
//This isnt done yet, di pa gashow up ang mc and the dialog is faulty
//Wala pa save system and battle
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class G6_Room2_PD6 extends JFrame implements KeyListener {

    int mapw = 11;
    int maph = 11;
    int frameWidth = 660;
    int frameHeight = 660;

    JLabel[] chr = new JLabel[mapw * maph];

    int chrpos = 34;

    Dialog dialog = new Dialog();

    ImageIcon icon, icon2, icon3, icon4, icon5,
              icon6, icon7, icon8, icon9, icon10;

    Set<Integer> questionTiles = new HashSet<>(Arrays.asList(
        43, 109, 101, 50, 63, 59, 83
    ));

    Set<Integer> answeredTiles = new HashSet<>();

    public void setFrame(){

        // ================= SPRITES =================
        icon  = new ImageIcon("images/G6_char1.png");
        icon2 = new ImageIcon("images/G6_char2.png");
        icon3 = new ImageIcon("images/G6_char3.png");
        icon4 = new ImageIcon("images/G6_char4.png");
        icon5 = new ImageIcon("images/G6_char5.png");
        icon6 = new ImageIcon("images/G6_char6.png");
        icon7 = new ImageIcon("images/G6_char7.png");
        icon8 = new ImageIcon("images/G6_char8.png");
        icon9 = new ImageIcon("images/G6_char9.png");
        icon10= new ImageIcon("images/G6_char10.png");

        int tileW = frameWidth / mapw;
        int tileH = frameHeight / maph;

        icon  = scale(icon, tileW, tileH);
        icon2 = scale(icon2, tileW, tileH);
        icon3 = scale(icon3, tileW, tileH);
        icon4 = scale(icon4, tileW, tileH);
        icon5 = scale(icon5, tileW, tileH);
        icon6 = scale(icon6, tileW, tileH);
        icon7 = scale(icon7, tileW, tileH);
        icon8 = scale(icon8, tileW, tileH);
        icon9 = scale(icon9, tileW, tileH);
        icon10= scale(icon10, tileW, tileH);

        // ================= CHARACTER GRID =================
        for(int i=0;i<chr.length;i++){
            chr[i] = new JLabel();
        }

        chr[chrpos].setIcon(icon);

        // ================= LAYERED PANE =================
        JLayeredPane layers = getLayeredPane();
        layers.removeAll();

        layers.setLayout(new GraphPaperLayout(new Dimension(mapw, maph)));

        // ================= BACKGROUND (LabMap) =================
        ImageIcon bg = new ImageIcon("images/LabMap.png");

        JLabel background = new JLabel(
            new ImageIcon(bg.getImage().getScaledInstance(frameWidth, frameHeight, Image.SCALE_SMOOTH))
        );

        layers.add(background, new Rectangle(0,0,mapw,maph));

        // ================= CHARACTER LAYER =================
        int x=0,y=0;

        for(int i=0;i<chr.length;i++){
            layers.add(chr[i], new Rectangle(x,y,1,1));

            x++;
            if(x%mapw==0){
                x=0;
                y++;
            }
        }

        setSize(frameWidth, frameHeight);
        setVisible(true);
        setResizable(false);

        addKeyListener(this);
        dialog.addKey(this);

        dialog.show(layers,
            new String[]{
                "Room 3 Lab Map",
                "Move using arrow keys and answer questions on special tiles."
            },
            null,null,mapw,maph);
    }

    // ================= SCALE =================
    ImageIcon scale(ImageIcon img, int w, int h){
        return new ImageIcon(img.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
    }

    // ================= QUESTION =================
    void triggerQuestion(){

        String[][] qBank = {
            {"What is H2O?", "Water", "Oxygen", "Hydrogen", "Salt"},
            {"pH of neutral?", "7", "1", "14", "0"}
        };

        int q = new Random().nextInt(qBank.length);

        String correct = qBank[q][1];

        ArrayList<String> choices = new ArrayList<>();
        choices.add(qBank[q][1]);
        choices.add(qBank[q][2]);
        choices.add(qBank[q][3]);
        choices.add(qBank[q][4]);

        Collections.shuffle(choices);

        Runnable[] events = new Runnable[4];

        for(int i=0;i<4;i++){
            String ans = choices.get(i);

            if(ans.equals(correct)){
                events[i] = () ->
                    dialog.show(getLayeredPane(),
                        new String[]{"Correct!"},null,null,mapw,maph);
            } else {
                events[i] = () ->
                    dialog.show(getLayeredPane(),
                        new String[]{"Wrong!"},null,null,mapw,maph);
            }
        }

        dialog.show(getLayeredPane(),
            new String[]{"Answer this question:"},
            choices.toArray(new String[0]),
            events,
            mapw,maph);
    }

    // ================= MOVEMENT (GRAPH PAPER VERSION) =================
    @Override
    public void keyPressed(KeyEvent e){

        if(dialog != null && dialog.isVisible()) return;

        chr[chrpos].setIcon(null);

        try{

            if(e.getKeyCode()==KeyEvent.VK_RIGHT){
                chrpos++;
                chr[chrpos].setIcon(icon2);
            }

            else if(e.getKeyCode()==KeyEvent.VK_LEFT){
                chrpos--;
                chr[chrpos].setIcon(icon4);
            }

            else if(e.getKeyCode()==KeyEvent.VK_DOWN){
                chrpos += mapw;
                chr[chrpos].setIcon(icon6);
            }

            else if(e.getKeyCode()==KeyEvent.VK_UP){
                chrpos -= mapw;
                chr[chrpos].setIcon(icon8);
            }

        }catch(Exception ex){}

        // ================= QUESTION TRIGGER =================
        if(questionTiles.contains(chrpos) && !answeredTiles.contains(chrpos)){
            answeredTiles.add(chrpos);
            triggerQuestion();
        }
    }

    @Override public void keyReleased(KeyEvent e){}
    @Override public void keyTyped(KeyEvent e){}
    public static void main(String[] args) {
        G6_Room2_PD6 sg=new G6_Room2_PD6();
        sg.setFrame();
    }
}