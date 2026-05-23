package ihm.sokoban.controller;

import javafx.scene.media.AudioClip;
import java.util.Random;

/**
 * Gestionnaire de sons du jeu.
 * Les sons de pas sont choisis aléatoirement parmi 6 variantes (style Minecraft).
 */
public class SoundManager {

    private static final AudioClip CLICK = new AudioClip(
            SoundManager.class.getResource("/ihm/sokoban/sounds/click.mp3").toExternalForm());

    private static final AudioClip LEVEL_UP = new AudioClip(
            SoundManager.class.getResource("/ihm/sokoban/sounds/levelup.mp3").toExternalForm());

    private static final AudioClip[] WALK = {
            new AudioClip(SoundManager.class.getResource("/ihm/sokoban/sounds/walk/Copper_step1.mp3").toExternalForm()),
            new AudioClip(SoundManager.class.getResource("/ihm/sokoban/sounds/walk/Copper_step2.mp3").toExternalForm()),
            new AudioClip(SoundManager.class.getResource("/ihm/sokoban/sounds/walk/Copper_step3.mp3").toExternalForm()),
            new AudioClip(SoundManager.class.getResource("/ihm/sokoban/sounds/walk/Copper_step4.mp3").toExternalForm()),
            new AudioClip(SoundManager.class.getResource("/ihm/sokoban/sounds/walk/Copper_step5.mp3").toExternalForm()),
            new AudioClip(SoundManager.class.getResource("/ihm/sokoban/sounds/walk/Copper_step6.mp3").toExternalForm()),
    };

    private static final AudioClip[] RESTART = {
            new AudioClip(SoundManager.class.getResource("/ihm/sokoban/sounds/totem.mp3").toExternalForm()),
    };

    private static final Random RANDOM = new Random();

    public static void playClick() {
        CLICK.play();
    }

    public static void playLevelUp() {
        LEVEL_UP.play();
    }

    public static void playWalk() {
        WALK[RANDOM.nextInt(WALK.length)].play(0.20);
    }

    public static void playRestart() {
        RESTART[RANDOM.nextInt(RESTART.length)].play();
    }
}
