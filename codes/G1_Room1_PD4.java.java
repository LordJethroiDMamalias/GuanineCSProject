package deliverable;

import java.awt.Component;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Deliverable extends JFrame {
   public Deliverable() {
      this.setTitle("YES");
      this.setDefaultCloseOperation(3);
      GamePanel panel = new GamePanel(this);
      this.add(panel);
      panel.setPreferredSize(new Dimension(800, 600));
      this.pack();
      this.setResizable(false);
      this.setLocationRelativeTo((Component)null);
      this.setVisible(true);
   }

   public static void main(String[] args) {
      SwingUtilities.invokeLater(Deliverable::new);
   }
}
