package ihm.sokoban.controller;

import javafx.scene.media.AudioClip;
import java.util.Random;

/**
 * Gestionnaire de sons de l'application.
 * Charge les fichiers audio au demarrage et fournit des methodes statiques pour les jouer.
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

    /** Joue le son de clic (bouton). */
    public static void playClick() {
        CLICK.play();
    }

    /** Joue le son de victoire (niveau termine). */
    public static void playLevelUp() {
        LEVEL_UP.play();
    }

    /** Joue un bruit de pas aleatoire parmi 6 sons (volume reduit a 20%). */
    public static void playWalk() {
        WALK[RANDOM.nextInt(WALK.length)].play(0.20);
    }

    /** Joue le son de l'animation totem (recommencer le niveau). */
    public static void playRestart() {
        RESTART[RANDOM.nextInt(RESTART.length)].play();
    }
}
