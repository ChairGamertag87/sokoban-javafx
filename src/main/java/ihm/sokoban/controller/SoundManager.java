package ihm.sokoban.controller;

import javafx.scene.media.AudioClip;

public class SoundManager {

    private static final AudioClip CLICK = new AudioClip(
            SoundManager.class.getResource("/ihm/sokoban/sounds/click.mp3").toExternalForm());

    private static final AudioClip LEVEL_UP = new AudioClip(
            SoundManager.class.getResource("/ihm/sokoban/sounds/levelup.mp3").toExternalForm());

    public static void playClick() {
        CLICK.play();
    }

    public static void playLevelUp() {
        LEVEL_UP.play();
    }
}
