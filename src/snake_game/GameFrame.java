package snake_game;

import javax.swing.*;
import java.io.IOException;

public class GameFrame extends JFrame {

    GameFrame() throws IOException {
        this.add(new GamePanel());
        this.setTitle("Snake Game");
        this.setSize(700, 800);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.pack();
        this.setVisible(true);
        this.setLocationRelativeTo(null);
    }
}
