package snake_game;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public class SnakeGame {

    public static void main(String[] args) throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        new GameFrame();
        GamePanel gPanel = new GamePanel();
        //play the bgm
        gPanel.playBGM();
    }
}
