package ihm.sokoban.controller;

import ihm.sokoban.model.JeuSokoban;
import ihm.sokoban.model.SokobanException;
import ihm.sokoban.util.LoaderNiveauxXSB;

import ihm.sokoban.util.NiveauxTutoriel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Controleur du menu principal.
 * Permet de choisir le mode de jeu, d'ouvrir l'editeur ou de quitter.
 */
public class MenuController {

    private static final int TAILLE_MIN = 3;
    private static final int TAILLE_MAX = 15;

    private Stage stage;

    /**
     * Injecte la fenetre principale pour les dialogues et la navigation.
     *
     * @param stage la fenetre principale de l'application
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Lance le mode tutoriel (5 niveaux simples).
     */
    @FXML
    private void handleTutoriel() {
        SoundManager.playClick();
        JeuSokoban jeu = new JeuSokoban(
                NiveauxTutoriel.getNiveaux(), NiveauxTutoriel.getNoms(), 0);
        lancerJeu(jeu);
    }

    /**
     * Lance le mode normal (10 niveaux de difficulte croissante).
     */
    @FXML
    private void handleNormal() {
        SoundManager.playClick();
        JeuSokoban jeu = new JeuSokoban(0);
        lancerJeu(jeu);
    }

    /**
     * Ouvre un DirectoryChooser pour charger des niveaux depuis un dossier .xsb.
     * Filtre les niveaux trop petits ou trop grands pour l'affichage.
     */
    @FXML
    private void handleXSB() {
        SoundManager.playClick();

        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choisir un dossier de niveaux XSB");
        File dossier = chooser.showDialog(stage);

        if (dossier == null) {
            return;
        }

        LoaderNiveauxXSB.Banque banque;
        try {
            banque = LoaderNiveauxXSB.chargerDepuisDossier(Path.of(dossier.toURI()));
        } catch (SokobanException e) {
            showErreur("Chargement impossible",
                    "Aucun fichier .xsb valide trouve dans ce dossier.\n\n" + e.getMessage());
            return;
        }


        List<String> niveauxValides = new ArrayList<>();
        List<String> nomsValides = new ArrayList<>();
        int exclus = 0;

        for (int i = 0; i < banque.niveaux.length; i++) {
            int[] dims = getDimensions(banque.niveaux[i]);
            int lignes = dims[0];
            int cols   = dims[1];

            if (lignes * cols < TAILLE_MIN) {
                exclus++;
                continue; // Surface trop petite (joueur + caisse + cible minimum)
            }
            if (lignes > TAILLE_MAX || cols > TAILLE_MAX) {
                exclus++;
                continue; // Trop grand pour l'affichage
            }

            niveauxValides.add(banque.niveaux[i]);
            nomsValides.add(banque.noms[i]);
        }

        if (niveauxValides.isEmpty()) {
            showErreur("Aucun niveau compatible",
                    "Tous les niveaux du dossier ont ete exclus.\n\n"
                    + "Regles : grille minimum " + TAILLE_MIN + "x" + TAILLE_MIN
                    + ", maximum " + TAILLE_MAX + "x" + TAILLE_MAX + ".\n\n"
                    + exclus + " niveau(x) exclu(s).");
            return;
        }

        if (exclus > 0) {
            showInfo("Niveaux charges avec avertissement",
                    niveauxValides.size() + " niveau(x) charge(s).\n"
                    + exclus + " niveau(x) exclu(s) car hors limites ("
                    + TAILLE_MIN + "x" + TAILLE_MIN + " min, "
                    + TAILLE_MAX + "x" + TAILLE_MAX + " max).");
        }

        JeuSokoban jeu = new JeuSokoban(
                niveauxValides.toArray(new String[0]),
                nomsValides.toArray(new String[0]),
                0);
        lancerJeu(jeu);
    }

    /**
     * Ouvre l'editeur de niveaux en chargeant son FXML.
     */
    @FXML
    private void handleEditeur() {
        SoundManager.playClick();
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/ihm/sokoban/fxml/editeur.fxml"));
            Parent root = loader.load();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            showErreur("Erreur", "Impossible de charger l'editeur.");
        }
    }

    /**
     * Ferme l'application.
     */
    @FXML
    private void handleQuitter() {
        SoundManager.playClick();
        if (stage != null) {
            stage.close();
        }
    }

    /**
     * Charge la vue du jeu, injecte le modele et configure le clavier.
     *
     * @param jeu l'instance du moteur de jeu a utiliser
     */
    private void lancerJeu(JeuSokoban jeu) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/ihm/sokoban/fxml/sokoban.fxml"));
            Parent root = loader.load();

            SokobanController controller = loader.getController();
            controller.setJeu(jeu);

            Scene scene = stage.getScene();
            scene.setRoot(root);

            controller.setupClavier(scene);

            stage.setOnCloseRequest(event -> {
                event.consume();
                controller.handleQuitter();
            });

        } catch (Exception e) {
            showErreur("Erreur", "Impossible de charger le jeu.");
        }
    }

    /**
     * Calcule les dimensions (lignes x colonnes) d'un niveau au format texte.
     *
     * @param niveau le niveau en chaine de caracteres (lignes separees par \n)
     * @return un tableau {nbLignes, nbColonnes}
     */
    private int[] getDimensions(String niveau) {
        String[] lignes = niveau.split("\n");
        int nbLignes = lignes.length;
        int nbCols = 0;
        for (String ligne : lignes) {
            nbCols = Math.max(nbCols, ligne.length());
        }
        return new int[]{nbLignes, nbCols};
    }

    /**
     * Affiche une popup d'erreur.
     *
     * @param titre   le titre de la fenetre
     * @param message le message d'erreur
     */
    private void showErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Affiche une popup d'information.
     *
     * @param titre   le titre de la fenetre
     * @param message le message d'information
     */
    private void showInfo(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
