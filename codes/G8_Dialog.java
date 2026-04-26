package codes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/*
GUANINE DIALOG SYSTEM (G8_Dialog.java CLASS)
Restored to original formatting style.
*/

public class G8_Dialog implements KeyListener, ActionListener {

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
    private Runnable onCloseCallback = null;

    public G8_Dialog() {
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

        // Original "Guanine" Styling
        panel.setBackground(new Color(0, 0, 0, 200));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));

        textSpace = new JTextArea();
        textSpace.setForeground(Color.WHITE);
        textSpace.setOpaque(false);
        textSpace.setLineWrap(true);
        textSpace.setWrapStyleWord(true);
        textSpace.setEditable(false);
        textSpace.setFocusable(false);
        textSpace.setFont(new Font("Comic Sans MS", Font.BOLD, 17));

        panel.add(textSpace);
        timer = new Timer(25, this); 
    }

    public void show(JLayeredPane layers, String[] lines, String[] choices, Runnable[] events, int gridW, int gridH, Runnable onClose) {
        if (isVisible) return; 

        this.onCloseCallback = onClose;
        this.isVisible = true;
        this.dialogQueue = lines;
        this.queueIndex = 0;
        this.currentChoices = choices;
        this.choiceEvents = events;
        this.selectedChoice = 0;
        this.choicesDisplayed = false;
        this.canSkip = false; 

        this.savedLayers = layers;

        setupPanel(layers, gridW, gridH);
        prepareNextLine();

        Timer guardTimer = new Timer(150, e -> canSkip = true);
        guardTimer.setRepeats(false);
        guardTimer.start();
    }

    private void setupPanel(JLayeredPane layers, int gridW, int gridH) {
        // Calculate pixel-based bounds so it looks like the original but is visible
        int pWidth = layers.getWidth();
        int pHeight = layers.getHeight();
        
        // Match the original 15, 10 text space offset logic
        int width = pWidth - 100; // Leave some margin
        int height = 120;
        int x = (pWidth - width) / 2;
        int y = pHeight - height - 40;

        panel.setBounds(x, y, width, height);
        textSpace.setBounds(15, 10, width - 30, height - 20);

        if (panel.getParent() == null) {
            layers.add(panel, JLayeredPane.POPUP_LAYER);
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
        if (onCloseCallback != null) {
            Runnable cb = onCloseCallback;
            onCloseCallback = null;
            cb.run();
        }
    }

    public void addKey(JFrame frame) { frame.addKeyListener(this); }
    public boolean isVisible() { return isVisible; }
    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}
