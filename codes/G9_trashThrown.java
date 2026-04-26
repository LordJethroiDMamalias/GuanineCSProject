package codes;

import javax.swing.*;

public class G9_trashThrown extends GameEvent {

    public G9_trashThrown() {
        super(); 
    }

    private String getItemName(int tile) {
        switch (tile) {
            case 13: return "Old Batteries";
            case 37: return "Glass Shards";
            case 48: return "Tin Cans";
            case 75: return "Rotten Banana";
            case 79: return "Plastic Bags";
            case 85: return "Dried Leaves";
            case 104: return "Weeds & Grass";
            case 108: return "Boxes";
            default: return "Unknown Item";
        }
    }

    @Override 
    public void handle(G9_Room2_PD6 game, int[] inv, int count, JLayeredPane lp, Dialog dialog, int mw, int mh, Runnable reset) {
        if (count <= 0) {
            dialog.show(lp, new String[]{"Your hands are empty.", "Pick up trash first."}, null, null, mw, mh);
            return;
        }

        int tile = inv[0];
        String itemName = getItemName(tile);
        String[] lines = { itemName + " in hand.", "Which bin does it belong in?" };
        String[] options = {"Red", "Green", "Blue"};

        Runnable[] branches = {
                () -> validate(game, inv, "nonbio", lp, dialog, mw, mh, reset),
                () -> validate(game, inv, "bio", lp, dialog, mw, mh, reset),
                () -> validate(game, inv, "recy", lp, dialog, mw, mh, reset)
        };

        dialog.show(lp, lines, options, branches, mw, mh);
    }

    private void validate(G9_Room2_PD6 game, int[] inv, String category, JLayeredPane lp, Dialog dialog, int mw, int mh, Runnable reset) {
        int tile = inv[0];
        boolean correct = false;

        if (category.equals("nonbio")) {
            if (tile == 13 || tile == 37 || tile == 48) correct = true;
        } else if (category.equals("bio")) {
            if (tile == 75 || tile == 85 || tile == 104) correct = true;
        } else if (category.equals("recy")) {
            if (tile == 48 || tile == 79 || tile == 108) correct = true;
        }

        if (correct) {
            if (!game.cleared[tile]) {
                game.cleared[tile] = true;
                game.incrementTrashCleared();
            }
            
            if (game.getTrashCleared() >= game.totalTrash && !game.rewardGiven) {
                String[] fullSequence = {
                    "Mm. Good.",
                    "Hmph...",
                    "You actually managed to sort everything correctly.",
                    "Here, take this gift.",
                    "Now get out before I change my mind."
                };
                game.rewardGiven = true;
                game.finalDialog = true;
                game.saveProgress();
                reset.run();
                SwingUtilities.invokeLater(() -> {
                    dialog.show(lp, fullSequence, null, null, mw, mh, () -> {
                        game.objComplete = true;
                        game.saveProgress();
                    });
                });
            } else {
                dialog.show(lp, new String[]{"Mm. Good."}, null, null, mw, mh);
                reset.run();
            }
        } else {
            dialog.show(lp, new String[]{"Wrong bin!", "Try again."}, null, null, mw, mh);
        }
    }
}
