/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package pkgfinal.project;

import javax.swing.JFrame;
/**
 * Game 
 * Group: GN JUMALON, FEDORA MAYNOPAS & EDWARD MALVAS
 */
public class G3_MapImplementer {

    public static void main(String[] args) {
        System.out.println("--- Booting Chemistry Quest System ---");

        // 1. NOW LOADING NewMain FIRST
        System.out.println("Status: Loading Map 2 (NewMain)...");
        runLevel(new G3_Room1_PD4(), "Chemistry Quest: The Shattered Nite - Map 2");

        // 2. NOW LOADING Main SECOND
        System.out.println("\n--- Transitioning to Map 1 --- \n");
        System.out.println("Status: Loading Map 1 (Main)...");
        runLevel(new G3_Room2_PD6(), "Chemistry Quest - Map 1");

        // 3. Final Epilogue remains last
        System.out.println("\n--- Loading Final Scene --- \n");
        runLevel(new G3_EndScene(), "Epilogue: The Lawyer's Freedom");

        System.out.println("--- Game Session Completed ---");
    }

    private static void runLevel(javax.swing.JPanel panel, String title) {
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // This ensures the next map doesn't open until this one is closed
        while (frame.isVisible()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}