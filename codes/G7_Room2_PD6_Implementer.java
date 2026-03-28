import javax.swing.*;

public class G7_Room2_PD6_Implementer {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Maze Adventure");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            G7_Room2_PD6 game = new G7_Room2_PD6();
            frame.add(game);

            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
