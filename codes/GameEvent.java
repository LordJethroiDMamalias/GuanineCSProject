package codes;

import javax.swing.*;

public class GameEvent {
    public GameEvent() {
        // Base initialization if needed
    }

    public void handle(G9_Room2_PD6 game, int[] inv, int count, JLayeredPane lp, Dialog dialog, int mw, int mh, Runnable reset) {
        dialog.show(lp, new String[]{"Nothing happens..."}, null, null, mw, mh);
    }
}
