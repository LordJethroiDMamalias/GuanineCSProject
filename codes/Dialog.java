package codes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/*
GUANINE DIALOG SYSTEM (Dialog.java CLASS)

i used a lot of gemini's help here ... so i apologize.
how was i supposed to figure all of this out by myself
*/

public class Dialog implements KeyListener, ActionListener {

    JPanel panel;
    JTextArea textSpace;
    Timer timer; 

    String[] dialogQueue;
    int queueIndex = 0;
    String currentText = "";
    int charIndex = 0;
    
    boolean isVisible = false;
    boolean dialogFinished = false;
    boolean choicesDisplayed = false;
    boolean canSkip = false;

    String[] currentChoices = null;
    int selectedChoice = 0;
    Runnable[] choiceEvents = null;
    
    JLayeredPane savedLayers;
    int savedGridW, savedGridH;
    private Runnable onCloseCallback = null;

    public Dialog() {
        panel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setColor(getBackground());
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
                super.paintComponent(g);
            }
        };

        panel.setBackground(new Color(0, 0, 0, 200));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));

        textSpace = new JTextArea();
        textSpace.setBounds(15, 10, 520, 90);
        textSpace.setForeground(Color.WHITE);
        textSpace.setOpaque(false);
        textSpace.setLineWrap(true);
        textSpace.setWrapStyleWord(true);
        textSpace.setEditable(false);
        textSpace.setFocusable(false);
        textSpace.setFont(new Font("Comic Sans MS", Font.BOLD, 17));

        panel.add(textSpace);
        timer = new javax.swing.Timer(25, this); 
    }

    public void show(JLayeredPane layers, String[] lines, String[] choices, Runnable[] events, int gridW, int gridH) {
        if (isVisible) return; 

        this.isVisible = true;
        this.dialogQueue = lines;
        this.queueIndex = 0;
        this.currentChoices = choices;
        this.choiceEvents = events;
        this.selectedChoice = 0;
        this.choicesDisplayed = false;
        
        this.canSkip = false; 

        this.savedLayers = layers;
        this.savedGridW = gridW;
        this.savedGridH = gridH;

        setupPanel(layers, gridW, gridH);
        prepareNextLine();

        javax.swing.Timer guardTimer = new javax.swing.Timer(150, e -> canSkip = true);
        guardTimer.setRepeats(false);
        guardTimer.start();
    }

    public void show(JLayeredPane layers, String[] lines, String[] choices,
                    Runnable[] events, int gridW, int gridH, Runnable onClose) {
       this.onCloseCallback = onClose;
       show(layers, lines, choices, events, gridW, gridH);
   }

    private void setupPanel(JLayeredPane layers, int gridW, int gridH) {
        if (panel.getParent() == null) {
            Rectangle constraints = new Rectangle(1, gridH - 2, gridW - 2, 2);
            layers.add(panel, constraints);
            layers.setLayer(panel, JLayeredPane.POPUP_LAYER);
        }
        panel.setVisible(true);
        layers.revalidate();
        layers.repaint();
    }

    private void prepareNextLine() {
        currentText = dialogQueue[queueIndex];
        charIndex = 0;
        dialogFinished = false;
        textSpace.setText(""); 
        timer.setDelay(25);
        timer.setInitialDelay(100);
        timer.start();
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (charIndex < currentText.length()) {
            char currentChar = currentText.charAt(charIndex);
            textSpace.append(String.valueOf(currentChar));
            charIndex++;

            int nextDelay = 25;
            if (currentChar == '.' || currentChar == '?' || currentChar == '!') nextDelay = 400;
            else if (currentChar == ',' || currentChar == ':' || currentChar == ';') nextDelay = 200;

            timer.setDelay(nextDelay);
        } else {
            timer.stop();
            dialogFinished = true;
        }
    }

    private void renderChoices() {
        choicesDisplayed = true;
        textSpace.setText("\n"); 
        for (int i = 0; i < currentChoices.length; i++) {
            String prefix = (i == selectedChoice) ? "  * " : "    ";
            textSpace.append(prefix + currentChoices[i] + "          ");
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!isVisible) return;

        if (choicesDisplayed) {
            if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                selectedChoice = Math.max(0, selectedChoice - 1);
                renderChoices();
                return;
            }
            if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                selectedChoice = Math.min(currentChoices.length - 1, selectedChoice + 1);
                renderChoices();
                return;
            }
        }

        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (!canSkip) return;

            if (!dialogFinished) {
                timer.stop();
                textSpace.setText(currentText);
                dialogFinished = true;
                return;
            }

            if (dialogFinished && !choicesDisplayed && queueIndex == dialogQueue.length - 1 && currentChoices != null) {
                renderChoices();
                return;
            }

            if (choicesDisplayed) {
                isVisible = false; 
                hide();
                choiceEvents[selectedChoice].run();
                return;
            }

            if (dialogFinished) {
                queueIndex++;
                if (queueIndex < dialogQueue.length) {
                    prepareNextLine();
                } else {
                    hide();
                }
            }
        }
    }

    public void hide() {
        timer.stop();
        panel.setVisible(false);
        isVisible = false;

        // Fire the onClose callback if one was registered, then clear it
        // so it can never fire twice
        if (onCloseCallback != null) {
            Runnable cb      = onCloseCallback;
            onCloseCallback  = null;
            cb.run();
        }
    }

    public void addKey(JFrame frame) { frame.addKeyListener(this); }
    public boolean isVisible() { return isVisible; }
    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}