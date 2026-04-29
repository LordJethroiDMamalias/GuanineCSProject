package codes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.nio.file.*;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class Menu extends JFrame {

    static final int    WINDOW_SIZE    = 660;
    static final String SAVE_FILE_PATH = "docs/saveFile.txt";
    static final String DEFAULT_IMAGE  = "MAP0.png";

    static final String CARD_MENU = "MENU";
    static final String CARD_MAPS = "MAPS";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel     rootPanel  = new JPanel(cardLayout);

    private static Clip bgmClip = null;

    public static void playSound(String filePath) {
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
            setVolume(bgmClip, 0.8f);
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

    private static void setVolume(Clip clip, float linearGain) {
        if (linearGain <= 0f)     linearGain = 0.0001f;
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

    public static void stopSound() {
        if (bgmClip != null) {
            if (bgmClip.isRunning()) bgmClip.stop();
            bgmClip.close();
            bgmClip = null;
        }
    }

    public static void main(String[] args) {
        playSound("music/Menu.wav");
        SwingUtilities.invokeLater(Menu::new);
    }

    public Menu() {
        setTitle("TOGETHER: Project Guanine");
        setSize(WINDOW_SIZE, WINDOW_SIZE);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        rootPanel.add(new MenuPanel(this), CARD_MENU);
        rootPanel.add(new MapsPanel(this), CARD_MAPS);

        add(rootPanel);
        cardLayout.show(rootPanel, CARD_MENU);
        setVisible(true);
    }

    void showCard(String card) {
        cardLayout.show(rootPanel, card);
    }


    static class MenuPanel extends JPanel {

        private final Menu  parent;
        private Image       bgImage;

        MenuPanel(Menu parent) {
            this.parent = parent;
            setLayout(new GridBagLayout());

            String mapName = readMapName(SAVE_FILE_PATH);
            String imgFile = resolveImage(mapName);
            bgImage = loadImage(imgFile);

            JPanel btnPanel = new JPanel();
            btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.Y_AXIS));
            btnPanel.setOpaque(false);

            btnPanel.setBorder(new javax.swing.border.EmptyBorder(250, 0, 0, 0));

            int[] gaps = { 20, 20, 80 };

            String[] labels = { "", "", "", "" };
            for (int i = 0; i < labels.length; i++) {
                JButton btn = new JButton(labels[i]);
                btn.setAlignmentX(CENTER_ALIGNMENT);
                btn.setMaximumSize(new Dimension(120, 36));
                btn.setPreferredSize(new Dimension(120, 36));
                btn.setBackground(new Color(0, 0, 0, 0));
                btn.setOpaque(false);
                btn.setContentAreaFilled(false);
                btn.setBorderPainted(false);
                btn.setFocusPainted(false);

                final int action = i;
                btn.addActionListener(e -> handleAction(action));

                btnPanel.add(btn);
                if (i < gaps.length)
                    btnPanel.add(Box.createVerticalStrut(gaps[i]));
            }

            add(btnPanel);
        }

        private void handleAction(int action) {
            switch (action) {
                case 0 -> onPlay();
                case 1 -> parent.showCard(CARD_MAPS);
                case 2 -> onHelp();
                case 3 -> System.exit(0);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bgImage != null) {
                g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
            }
        }

        private void onPlay() {
            stopSound();
            String mapName = readMapName(SAVE_FILE_PATH);

            if (mapName == null) {
                /*System.out.println("Error: file not found");
                JOptionPane.showMessageDialog(
                    this,
                    "Could not determine map.\n"
                    + "Check that docs/saveFile.txt exists and contains a valid MAP: line.",
                    "Play – Error",
                    JOptionPane.ERROR_MESSAGE
                );
                return;*/
                mapName = "G0_Jardenito";
            }

            String className = "codes." + mapName;
            try {
                Class<?> clazz = Class.forName(className);

                try {
                    Method m = clazz.getMethod("main", String[].class);
                    parent.dispose();
                    m.invoke(null, (Object) new String[]{});
                    return;
                } catch (NoSuchMethodException ignored) { /* fall through */ }

                try {
                    Object instance = clazz.getDeclaredConstructor().newInstance();
                    Method m = clazz.getMethod("show");
                    parent.dispose();
                    m.invoke(instance);
                } catch (NoSuchMethodException ex) {
                    System.out.println("Error: file not found");
                    JOptionPane.showMessageDialog(
                        this,
                        "Class \"" + className + "\" has no main() or show() method.",
                        "Play – Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }

            } catch (ClassNotFoundException ex) {
                System.out.println("Error: file not found");
                JOptionPane.showMessageDialog(
                    this,
                    "Class \"" + className + "\" does not exist in the codes package.",
                    "Play – Error",
                    JOptionPane.ERROR_MESSAGE
                );
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
                JOptionPane.showMessageDialog(
                    this,
                    "Failed to launch \"" + mapName + "\":\n" + ex.getMessage(),
                    "Play – Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }

        private void onHelp() {
            try {
                Desktop.getDesktop().browse(
                    new URI("https://docs.google.com/document/d/15ycHwvKDdR7xB9TBoEwHecN_c1XWByySd1SNyGKjcYE/edit?usp=sharing"));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                    this,
                    "Could not open browser:\n" + ex.getMessage(),
                    "Help – Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }

        private static Image loadImage(String filename) {
            File f = new File(filename);
            if (f.exists()) return new ImageIcon(f.getAbsolutePath()).getImage();
            var url = MenuPanel.class.getClassLoader().getResource(filename);
            return url != null ? new ImageIcon(url).getImage() : null;
        }
    }


    static class MapsPanel extends JPanel {

        private static final String[][] MAP_DATA = {
            { "G0", "MAP0.png",
              "Jardenito's Bunker",
              "The beginning of your quest to save Guanine. "
            + "This is where Jardenito has been staying for weeks. "
            + "Completely dark.." },
            
            { "G1", "MAP1.png",
              "Nursery of Life",
              "Once a place of growth and peace, this area is now twisted into a maze of overgrown vines and hidden dangers. "
            + "Unseen projectiles strike from the shadows, forcing you to move carefully through every path. "
            + "Nothing here can be trusted. "},

            { "G2", "MAP2.png",
              "Green Room",
              "A vibrant pink-themed makeup room filled with mirrors, cosmetics, and glowing lights sets the stage for a playful yet mysterious experience. "
            + "As players explore the space, the cheerful atmosphere slowly builds tension toward the final challenge. "
            + "At the end of the map, they must face and defeat Rainity, which contrasts sharply with the room's soft, glamorous aesthetic." },

            { "G3", "MAP3.png",
              "Vaults of Rectitude",
              "The Vaults used to be a renowned courthouse, holding only the truth in every being only to be left with the crumbs of its pillars. "
            + "The place is haunted with the illusions of the minds that were long corrupted from the nites of criminals and victims alike, with the goal of collecting the peices of Celene's nite scattered around the map." },
            
            { "G4", "MAP4.png",
              "WORLD WORLD WORLD",
              "A dimension of imagination and torment, clouds are manifestations of suffering. "
            + "Fragile, the world is prone to shattering upon Jetroids' weakening. "
            + "The air is lonely, yet chaotic, as if it could jump on its own (how does that even work?)." },
            
            { "G5", "MAP5.png",
              "Temple of Vanity (NOT INCLUDED)",
              "N/A "},
            
            { "G6", "MAP6.png",
              "Blue Brick Road",
              "The air feels tense, every step echoing louder than it should, as though the place itself is listening. "
            + "Somewhere within the winding paths, a presence lingers—quiet, watchful, and patient. Main is here. "
            + "As you move forward, one truth becomes clear: You are not navigating the maze—the maze is leading you to her. "
            + "The laboratory falls silent as a sharp chemical scent lingers in the air, mixed with the faint hum of unstable reactions. "
            + "Broken glassware and scattered notes hint at experiments that went terribly wrong, as if something inside this room has been waiting. "
            + "With cold precision, you as the 30th Student must stand firm, be ready to see whether you are worthy to escape. " },
            
            { "G7", "MAP7.png",
              "Nites N' Chips",
              "A shadowy casino where flickering lights barely cut through the darkness. "
            + "Every machine whispers false hope, luring players deeper into something they can't escape. "
            + "RYAN lingers here, trapped in a cycle of risk, as if the place refuses to let him go. "
            + "Something is wrong… the longer you stay, the more it feels like the casino is watching you."},
            
            { "G8", "MAP8.png",
              "Sustainer of Arcology",
              "Deep within the hollowed remains of the laboratory where the GIGGLEBOT3000 first drew its mechanical breath."
            + "Step lightly through the graveyard of innovation, where lethal munitions and Haywire automatons guard the path to survival."
            + "Mend the fractured systems of a facility left in ruin, piecing together the ghost of a functional world."
            + "Confront the architect of the chaos: the rogue GIGGLEBOT3000, a creation that has long since outgrown its programmed joy." },
            
            { "G9", "MAP9.png",
              "Luminara Al-Qamar",
              "Here lie the answers to questions that have echoed above the clouds for ages."
            + "Something stirs in the heavens… the Dahaka awaits." },
            
            { "G10", "MAP10.png",
              "Don Malek",
              "A cold wind sweeps across the unknown island, carrying with it the heavy scent of burning cigars and something far more ominous beneath it. "
            + "In the distance, a shadowed figure waits in silence, leaning against the cracked stone walls as if he has been expecting the 30th Student all along. "
            + "With a quiet flick of flame, his cigar glows in the darkness, and without a word he steps forward to begin the battle. "
            + "He is the final guardian before The Gambler's Hall, the domain of the one who waits at the end of the 30th Student and Jardenito's journey." },
            
            { "G11", "MAP11.png",
              "Ma'am Kath",
              "Inside The Gambler's Hall, the lights dim as if the room is slowly being swallowed by something unseen, and Ma'am Kath stands motionless at its center. "
            + "Her gaze locks onto you, and the silence tightens before she finally speaks: This is your final challenge. "
            + "The air feels heavier with every word, as if the space itself is closing in around the moment. "
            + "Only one outcome remains… and it begins now." },
        };

        private final Menu parent;

        private final JLabel    detailTitle = new JLabel("Select a chapter");
        private final JTextArea detailDesc  = new JTextArea("Click a chapter on the left to see its map and details.");
        private final JLabel    detailImage = new JLabel();

        MapsPanel(Menu parent) {
            this.parent = parent;
            setLayout(new BorderLayout());

            styleDetailComponents();

            add(buildTopBar(),    BorderLayout.NORTH);
            add(buildSplitPane(), BorderLayout.CENTER);

            selectMap(0);
        }

        private void styleDetailComponents() {
            detailTitle.setFont(detailTitle.getFont().deriveFont(Font.BOLD, 16f));

            detailDesc.setLineWrap(true);
            detailDesc.setWrapStyleWord(true);
            detailDesc.setEditable(false);
            detailDesc.setOpaque(false);

            detailImage.setHorizontalAlignment(SwingConstants.CENTER);
            detailImage.setPreferredSize(new Dimension(390, 210));
        }

        private JPanel buildTopBar() {
            JPanel bar = new JPanel(new BorderLayout());
            bar.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

            JLabel title = new JLabel("Chapters");
            title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));

            JButton backBtn = new JButton("Back");
            backBtn.addActionListener(e -> parent.showCard(CARD_MENU));

            bar.add(title,   BorderLayout.WEST);
            bar.add(backBtn, BorderLayout.EAST);
            return bar;
        }

        private JSplitPane buildSplitPane() {
            JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                buildLeftList(),
                buildRightDetail()
            );
            split.setDividerLocation(210);
            split.setEnabled(false);
            return split;
        }

        private JScrollPane buildLeftList() {
            JPanel list = new JPanel();
            list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
            list.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

            for (int i = 0; i < MAP_DATA.length; i++) {
                list.add(buildMapEntry(i));
                if (i < MAP_DATA.length - 1)
                    list.add(Box.createVerticalStrut(6));
            }
            list.add(Box.createVerticalGlue());

            JScrollPane scroll = new JScrollPane(list);
            scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            return scroll;
        }

        private JPanel buildMapEntry(int index) {
            String[] row = MAP_DATA[index];

            JPanel entry = new JPanel(new BorderLayout(0, 4));
            entry.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            entry.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            entry.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

            JLabel thumb = new JLabel();
            thumb.setPreferredSize(new Dimension(174, 72));
            thumb.setHorizontalAlignment(SwingConstants.CENTER);
            thumb.setBorder(BorderFactory.createLineBorder(Color.GRAY));

            Image img = loadImage(row[1]);
            if (img != null) {
                thumb.setIcon(new ImageIcon(img.getScaledInstance(174, 72, Image.SCALE_SMOOTH)));
            } else {
                thumb.setText("[no image]");
            }

            JLabel lbl = new JLabel(row[2], SwingConstants.CENTER);
            lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));

            entry.add(thumb, BorderLayout.CENTER);
            entry.add(lbl,   BorderLayout.SOUTH);

            entry.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) { selectMap(index); }
            });

            return entry;
        }

        private JScrollPane buildRightDetail() {
            JPanel detail = new JPanel();
            detail.setLayout(new BoxLayout(detail, BoxLayout.Y_AXIS));
            detail.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

            detailImage.setAlignmentX(LEFT_ALIGNMENT);
            detailTitle.setAlignmentX(LEFT_ALIGNMENT);
            detailDesc.setAlignmentX(LEFT_ALIGNMENT);

            detail.add(detailImage);
            detail.add(Box.createVerticalStrut(8));
            detail.add(detailTitle);
            detail.add(new JSeparator());
            detail.add(Box.createVerticalStrut(6));
            detail.add(detailDesc);
            detail.add(Box.createVerticalGlue());

            return new JScrollPane(detail);
        }

        private void selectMap(int index) {
            String[] row = MAP_DATA[index];
            detailTitle.setText(row[2]);
            detailDesc.setText(row[3]);

            Image img = loadImage(row[1]);
            if (img != null) {
                detailImage.setIcon(new ImageIcon(
                    img.getScaledInstance(390, 210, Image.SCALE_SMOOTH)));
            } else {
                detailImage.setIcon(null);
                detailImage.setText("[image not found]");
            }
        }

        static Image loadImage(String filename) {
            File f = new File(filename);
            if (f.exists()) return new ImageIcon(f.getAbsolutePath()).getImage();
            var url = MapsPanel.class.getClassLoader().getResource(filename);
            return url != null ? new ImageIcon(url).getImage() : null;
        }
    }


    static String readMapName(String filePath) {
        try {
            for (String line : Files.readAllLines(Paths.get(filePath))) {
                if (line.startsWith("MAP:")) {
                    String val = line.substring(4).trim();
                    return val.isEmpty() ? null : val;
                }
            }
        } catch (IOException e) {
            System.err.println("Warning: cannot read save file \""
                + filePath + "\": " + e.getMessage());
        }
        return null;
    }

    static String extractPrefix(String mapName) {
        if (mapName == null) return null;
        String upper = mapName.toUpperCase();

        // Check room-specific G10 prefixes first (more specific before general)
        if (upper.startsWith("G10_ROOM1")) return "G10_ROOM1";
        if (upper.startsWith("G10_ROOM2")) return "G10_ROOM2";

        // Then check remaining prefixes from longest to shortest to avoid partial matches
        String[] prefixes = { "G11", "G10", "G9", "G8", "G7", "G6", "G5", "G4", "G3", "G2", "G1", "G0" };
        for (String p : prefixes) {
            if (upper.startsWith(p)) return p;
        }
        return null;
    }

    static String resolveImage(String mapName) {
        String prefix = extractPrefix(mapName);
        if (prefix == null) return "images/" + DEFAULT_IMAGE;

        return switch (prefix) {
            case "G10_ROOM1" -> "images/MAP10.png";  // G10 image
            case "G10_ROOM2" -> "images/MAP11.png";  // G11 image
            default -> {
                // Strip the leading 'G' and use the number directly
                String number = prefix.substring(1); // e.g. "G3" -> "3", "G11" -> "11"
                yield "images/MAP" + number + ".png";
            }
        };
    }
}