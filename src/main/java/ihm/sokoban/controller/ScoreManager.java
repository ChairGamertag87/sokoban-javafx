package ihm.sokoban.controller;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

/**
 * Gestionnaire de sauvegarde des meilleurs scores.
 * Stocke les records dans un fichier Properties dans le dossier utilisateur (~/.sokobanfx/).
 */
public class ScoreManager {

    private static final Path FICHIER_SCORES =
            Paths.get(System.getProperty("user.home"), ".sokobanfx", "scores.properties");

    private final Properties scores = new Properties();

    /**
     * Constructeur qui charge les scores existants depuis le fichier.
     */
    public ScoreManager() {
        charger();
    }

    /**
     * Charge les scores depuis le fichier Properties s'il existe.
     */
    private void charger() {
        if (Files.exists(FICHIER_SCORES)) {
            try (InputStream in = Files.newInputStream(FICHIER_SCORES)) {
                scores.load(in);
            } catch (IOException e) {
            }
        }
    }

    /**
     * Sauvegarde les scores dans le fichier Properties.
     * Cree le dossier parent si necessaire.
     */
    private void sauvegarder() {
        try {
            Files.createDirectories(FICHIER_SCORES.getParent());
            try (OutputStream out = Files.newOutputStream(FICHIER_SCORES)) {
                scores.store(out, "Sokoban JavaFX - Meilleurs scores");
            }
        } catch (IOException e) {
        }
    }

    /**
     * Enregistre un score si c'est un nouveau record pour le niveau.
     *
     * @param nomNiveau  le nom du niveau termine
     * @param mouvements le nombre de mouvements effectues
     * @return true si c'est un nouveau record, false sinon
     */
    public boolean enregistrer(String nomNiveau, int mouvements) {
        int ancien = getMeilleurScore(nomNiveau);
        if (ancien == -1 || mouvements < ancien) {
            scores.setProperty(normaliser(nomNiveau), String.valueOf(mouvements));
            sauvegarder();
            return true;
        }
        return false;
    }

    /**
     * Retourne le meilleur score enregistre pour un niveau.
     *
     * @param nomNiveau le nom du niveau a chercher
     * @return le meilleur score en mouvements, ou -1 si aucun record
     */
    public int getMeilleurScore(String nomNiveau) {
        String val = scores.getProperty(normaliser(nomNiveau));
        if (val == null) return -1;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Normalise un nom de niveau pour l'utiliser comme cle.
     * Trim, lowercase et remplace les espaces par des underscores.
     *
     * @param nom le nom du niveau a normaliser
     * @return la cle normalisee
     */
    private String normaliser(String nom) {
        return nom.trim().toLowerCase().replace(" ", "_");
    }
}
