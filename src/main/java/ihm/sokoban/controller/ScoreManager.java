package ihm.sokoban.controller;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

public class ScoreManager {

    private static final Path FICHIER_SCORES =
            Paths.get(System.getProperty("user.home"), ".sokobanfx", "scores.properties");

    private final Properties scores = new Properties();

    public ScoreManager() {
        charger();
    }

    private void charger() {
        if (Files.exists(FICHIER_SCORES)) {
            try (InputStream in = Files.newInputStream(FICHIER_SCORES)) {
                scores.load(in);
            } catch (IOException e) {
            }
        }
    }

    private void sauvegarder() {
        try {
            Files.createDirectories(FICHIER_SCORES.getParent());
            try (OutputStream out = Files.newOutputStream(FICHIER_SCORES)) {
                scores.store(out, "Sokoban JavaFX - Meilleurs scores");
            }
        } catch (IOException e) {
        }
    }

    public boolean enregistrer(String nomNiveau, int mouvements) {
        int ancien = getMeilleurScore(nomNiveau);
        if (ancien == -1 || mouvements < ancien) {
            scores.setProperty(normaliser(nomNiveau), String.valueOf(mouvements));
            sauvegarder();
            return true;
        }
        return false;
    }

    public int getMeilleurScore(String nomNiveau) {
        String val = scores.getProperty(normaliser(nomNiveau));
        if (val == null) return -1;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private String normaliser(String nom) {
        return nom.trim().toLowerCase().replace(" ", "_");
    }
}
