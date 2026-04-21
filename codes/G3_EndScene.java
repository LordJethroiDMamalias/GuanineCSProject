/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class G3_EndScene extends JPanel implements KeyListener {
    private Image characterImg, chemicalImg;
    private int step = 0;
    
    
    private String[] dialogue = {
        "*takes both of the nites from you*",
        "You can tell that these tasks were left from my dear friend Night...",
        "She really does love Chemistry and some Math...",
        "Since you both got the 2 Nites... I'm no longer a ghost!",
        "I was trapped here in this courtroom for years!",
        "I guess Night knew I wanted to be a lawyer",
        "Here... I took this from Night..",
        "It's some chemical that makes her more faster than usuall.",
        "It might help you in your journey"
    };

    public G3_EndScene() {
        setPreferredSize(new Dimension(800, 600));
        setFocusable(true);
        addKeyListener(this);
        
        try {
            characterImg = new ImageIcon(getClass().getResource("/ASSETS/G3_FEDORA.png")).getImage();
            chemicalImg = new ImageIcon(getClass().getResource("/ASSETS/G3_CHEMICAL.png")).getImage();
        } catch (Exception e) {
            System.err.println("Error: Could not find ending PNGs in /ASSETS/");
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (step < dialogue.length) {
            
            g.drawImage(characterImg, 0, 0, getWidth(), getHeight(), null);
            drawDialogueBox(g, dialogue[step]);
        } else if (step == dialogue.length) {
            
            g.drawImage(chemicalImg, 0, 0, getWidth(), getHeight(), null);
            drawDialogueBox(g, "Obtained!");
        } else {
           
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.WHITE);
            g.setFont(new Font("Serif", Font.ITALIC, 18));
            
            String finalMsg = "After drinking the potion, you are suddenly more fast than usuall. Not just your physical body, but your thoughts too!";
            
            
            FontMetrics fm = g.getFontMetrics();
            int x = 100;
            int y = getHeight() / 2;
            g.drawString(finalMsg.substring(0, 58), x, y);
            g.drawString(finalMsg.substring(58), x, y + 30);
        }
    }

    private void drawDialogueBox(Graphics g, String text) {
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRoundRect(50, 480, 700, 80, 15, 15);
        g.setColor(Color.WHITE);
        g.drawRoundRect(50, 480, 700, 80, 15, 15);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString(text, 80, 525);
        g.setFont(new Font("Arial", Font.ITALIC, 12));
        g.drawString("Press E to continue", 630, 550);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_E) {
            step++;
            if (step > dialogue.length + 1) {
                
                javax.swing.JFrame topFrame = (javax.swing.JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);
                topFrame.dispose();
            }
            repaint();
        }
    }


    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}
