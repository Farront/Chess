package main;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        // Create the main game window
        JFrame window = new JFrame("Chess Game");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);

        // Display a dialog to allow user to choose game mode
        String[] options = {"Play with AI", "Play with another Player"};
        int choice = JOptionPane.showOptionDialog(
                null, 
                "Select Game Mode:", 
                "Chess Game", 
                JOptionPane.DEFAULT_OPTION, 
                JOptionPane.PLAIN_MESSAGE, 
                null, 
                options, 
                options[0]
        );

        if (choice == -1) {
            System.out.println("No selection made. Exiting...");
            System.exit(0); // Exit if no choice is made
        }

        // Initialize the game panel with the chosen mode
        GamePanel gp = new GamePanel(choice == 0); // Pass true for AI mode, false for Player-vs-Player
        window.add(gp);
        window.pack();

        window.setLocationRelativeTo(null);
        window.setVisible(true);

        gp.launchGame(); // Start the game
    }
}
