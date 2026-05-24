package ihm.sokoban.controller;

import ihm.sokoban.model.JeuSokoban;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Controleur de l'editeur de niveaux.
 * Permet de creer, modifier, tester et sauvegarder des niveaux Sokoban.
 */
public class EditeurController {

    private static final double TAILLE_CASE = 64;
    private static final int MIN_DIM = 3;
    private static final int MAX_DIM = 15;

    @FXML private GridPane grille;
    @FXML private Spinner<Integer> spinnerLignes;
    @FXML private Spinner<Integer> spinnerColonnes;
    @FXML private Button btnMur;
    @FXML private Button btnSol;
    @FXML private Button btnCible;
    @FXML private Button btnCaisse;
    @FXML private Button btnJoueur;
    @FXML private Button btnVide;

    private char[][] plateau;

    private char outilActif = '#';

    private Button boutonActif;

    private Button[] boutonsPalette;
    private char[] caracteresPalette;

    /**
     * Initialise l'editeur : configure les spinners, la palette d'outils
     * et genere une grille par defaut 7x7.
     */
    @FXML
    private void initialize() {

        spinnerLignes.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(MIN_DIM, MAX_DIM, 7));
        spinnerColonnes.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(MIN_DIM, MAX_DIM, 7));

        boutonsPalette = new Button[]{btnMur, btnSol, btnCible, btnCaisse, btnJoueur, btnVide};
        caracteresPalette = new char[]{'#', ' ', '.', '$', '@', '-'};

        activerBouton(btnMur);

        genererGrille(7, 7);
    }

    /**
     * Gere la selection d'un outil dans la palette lors du clic sur un bouton.
     *
     * @param event l'evenement du clic sur le bouton de la palette
     */
    @FXML
    private void handleSelectOutil(javafx.event.ActionEvent event) {
        SoundManager.playClick();
        Button btn = (Button) event.getSource();
        activerBouton(btn);
    }

    /**
     * Active visuellement un bouton de la palette et met a jour l'outil actif.
     *
     * @param btn le bouton a activer (bordure jaune)
     */
    private void activerBouton(Button btn) {
        if (boutonActif != null) {
            boutonActif.setStyle("");
        }
        boutonActif = btn;
        boutonActif.setStyle("-fx-border-color: #fcfc00; -fx-border-width: 3;");

        for (int i = 0; i < boutonsPalette.length; i++) {
            if (boutonsPalette[i] == btn) {
                outilActif = caracteresPalette[i];
                break;
            }
        }
    }

    /**
     * Genere une nouvelle grille vide aux dimensions choisies dans les spinners.
     */
    @FXML
    private void handleGenerer() {
        SoundManager.playClick();
        genererGrille(spinnerLignes.getValue(), spinnerColonnes.getValue());
    }

    /**
     * Genere une grille vide avec des murs sur les bords et du sol a l'interieur.
     *
     * @param nbLignes le nombre de lignes de la grille
     * @param nbCols   le nombre de colonnes de la grille
     */
    private void genererGrille(int nbLignes, int nbCols) {
        plateau = new char[nbLignes][nbCols];
        for (int l = 0; l < nbLignes; l++) {
            for (int c = 0; c < nbCols; c++) {
                if (l == 0 || l == nbLignes - 1 || c == 0 || c == nbCols - 1) {
                    plateau[l][c] = '#';
                } else {
                    plateau[l][c] = ' ';
                }
            }
        }
        reconstruireGrille();
    }

    /**
     * Reconstruit entierement le GridPane a partir du tableau plateau.
     */
    private void reconstruireGrille() {
        grille.getChildren().clear();
        grille.getRowConstraints().clear();
        grille.getColumnConstraints().clear();

        int nbLignes = plateau.length;
        int nbCols = plateau[0].length;

        for (int i = 0; i < nbLignes; i++) {
            grille.getRowConstraints().add(new RowConstraints(TAILLE_CASE));
        }
        for (int j = 0; j < nbCols; j++) {
            grille.getColumnConstraints().add(new ColumnConstraints(TAILLE_CASE));
        }

        for (int l = 0; l < nbLignes; l++) {
            for (int c = 0; c < nbCols; c++) {
                Region cellule = creerCellule(l, c);
                grille.add(cellule, c, l);
            }
        }
    }

    /**
     * Cree une cellule cliquable pour l'editeur.
     * Clic gauche place l'outil actif, clic droit efface (sol).
     *
     * @param ligne la ligne de la cellule dans le plateau
     * @param col   la colonne de la cellule dans le plateau
     * @return une Region configuree avec les gestionnaires de clic
     */
    private Region creerCellule(int ligne, int col) {
        Region cellule = new Region();
        cellule.setPrefSize(TAILLE_CASE, TAILLE_CASE);
        cellule.getStyleClass().add("case");
        cellule.getStyleClass().add(getCssClass(plateau[ligne][col]));
        cellule.setCursor(javafx.scene.Cursor.HAND);

        cellule.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                placerOutil(ligne, col);
            } else if (event.getButton() == MouseButton.SECONDARY) {
                plateau[ligne][col] = ' ';
                rafraichirCellule(cellule, ligne, col);
            }
        });

        cellule.setOnMouseDragEntered(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                placerOutil(ligne, col);
            }
        });

        return cellule;
    }

    /**
     * Place l'outil actif sur une cellule du plateau.
     * Si l'outil est le joueur, retire l'ancien joueur avant de placer le nouveau.
     *
     * @param ligne la ligne cible
     * @param col   la colonne cible
     */
    private void placerOutil(int ligne, int col) {
        if (outilActif == '@') {
            retirerAncienJoueur();
        }
        plateau[ligne][col] = outilActif;
        Region cellule = getCellule(ligne, col);
        if (cellule != null) {
            rafraichirCellule(cellule, ligne, col);
        }
    }

    /**
     * Retire l'ancien joueur du plateau (un seul joueur autorise).
     * Remplace sa case par du sol et rafraichit l'affichage.
     */
    private void retirerAncienJoueur() {
        for (int l = 0; l < plateau.length; l++) {
            for (int c = 0; c < plateau[0].length; c++) {
                if (plateau[l][c] == '@') {
                    plateau[l][c] = ' ';
                    Region cell = getCellule(l, c);
                    if (cell != null) rafraichirCellule(cell, l, c);
                }
            }
        }
    }

    /**
     * Met a jour les classes CSS d'une cellule apres modification.
     *
     * @param cellule la Region a rafraichir
     * @param ligne   la ligne de la cellule
     * @param col     la colonne de la cellule
     */
    private void rafraichirCellule(Region cellule, int ligne, int col) {
        cellule.getStyleClass().clear();
        cellule.getStyleClass().add("case");
        cellule.getStyleClass().add(getCssClass(plateau[ligne][col]));
    }

    /**
     * Recupere la Region correspondant a une position dans la grille.
     *
     * @param ligne la ligne de la cellule
     * @param col   la colonne de la cellule
     * @return la Region a cette position, ou null si hors limites
     */
    private Region getCellule(int ligne, int col) {
        int index = ligne * plateau[0].length + col;
        if (index < grille.getChildren().size()) {
            return (Region) grille.getChildren().get(index);
        }
        return null;
    }

    /**
     * Retourne le nom de la classe CSS associee a un caractere du plateau.
     *
     * @param c le caractere representant la case ('#', ' ', '.', '$', '@', etc.)
     * @return le nom de la classe CSS correspondante
     */
    private String getCssClass(char c) {
        return switch (c) {
            case '#' -> "case-mur";
            case ' ' -> "case-sol";
            case '.' -> "case-cible";
            case '$' -> "case-caisse";
            case '*' -> "case-caisse-cible";
            case '@' -> "case-joueur";
            case '+' -> "case-joueur-cible";
            default  -> "case-vide";
        };
    }


    /**
     * Efface la grille et la regenere avec les memes dimensions.
     */
    @FXML
    private void handleEffacer() {
        SoundManager.playClick();
        genererGrille(plateau.length, plateau[0].length);
    }

    /**
     * Retourne au menu principal en rechargeant le FXML du menu.
     */
    @FXML
    private void handleRetour() {
        SoundManager.playClick();
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/ihm/sokoban/fxml/menu.fxml"));
            Parent root = loader.load();

            MenuController menuController = loader.getController();
            Stage stage = (Stage) grille.getScene().getWindow();
            menuController.setStage(stage);
            grille.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Valide le niveau puis le lance dans le jeu pour le tester.
     * Exporte le plateau en XSB et cree un JeuSokoban temporaire.
     */
    @FXML
    private void handleTester() {
        SoundManager.playClick();

        String erreur = validerNiveau();
        if (erreur != null) {
            showErreur("Niveau invalide", erreur);
            return;
        }

        String niveauXSB = exporterXSB();

        JeuSokoban jeu = new JeuSokoban(
                new String[]{niveauXSB},
                new String[]{"Niveau personnalise"},
                0);

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/ihm/sokoban/fxml/sokoban.fxml"));
            Parent root = loader.load();

            SokobanController controller = loader.getController();
            controller.setJeu(jeu);

            Stage stage = (Stage) grille.getScene().getWindow();
            Scene scene = stage.getScene();
            scene.setRoot(root);
            controller.setupClavier(scene);

            stage.setOnCloseRequest(event -> {
                event.consume();
                controller.handleQuitter();
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Valide le niveau puis ouvre un FileChooser pour le sauvegarder en .xsb.
     */
    @FXML
    private void handleSauvegarder() {
        SoundManager.playClick();

        String erreur = validerNiveau();
        if (erreur != null) {
            showErreur("Niveau invalide", erreur);
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Sauvegarder le niveau");
        chooser.setInitialFileName("niveau.xsb");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichier XSB", "*.xsb"));

        Stage stage = (Stage) grille.getScene().getWindow();
        File fichier = chooser.showSaveDialog(stage);

        if (fichier != null) {
            try {
                Files.writeString(fichier.toPath(), exporterXSB());
                showInfo("Sauvegarde reussie",
                        "Niveau sauvegarde dans :\n" + fichier.getAbsolutePath());
            } catch (IOException e) {
                showErreur("Erreur de sauvegarde", e.getMessage());
            }
        }
    }

    /**
     * Valide que le niveau respecte les regles Sokoban.
     * Verifie : exactement 1 joueur, au moins 1 caisse, nombre caisses = nombre cibles.
     *
     * @return un message d'erreur si invalide, null si valide
     */
    private String validerNiveau() {
        int joueurs = 0, caisses = 0, cibles = 0;

        for (char[] ligne : plateau) {
            for (char c : ligne) {
                switch (c) {
                    case '@' -> joueurs++;
                    case '+' -> { joueurs++; cibles++; }
                    case '$' -> caisses++;
                    case '*' -> { caisses++; cibles++; }
                    case '.' -> cibles++;
                }
            }
        }

        if (joueurs == 0) return "Il faut placer un joueur (@).";
        if (joueurs > 1) return "Un seul joueur autorise.";
        if (caisses == 0) return "Il faut au moins une caisse.";
        if (cibles == 0) return "Il faut au moins une cible.";
        if (caisses != cibles) {
            return "Le nombre de caisses (" + caisses
                    + ") doit etre egal au nombre de cibles (" + cibles + ").";
        }
        return null;
    }


    /**
     * Exporte le plateau au format XSB (texte standard Sokoban).
     * Convertit les caracteres internes ('-' vide) en espaces.
     *
     * @return le niveau au format XSB
     */
    private String exporterXSB() {
        StringBuilder sb = new StringBuilder();
        for (int l = 0; l < plateau.length; l++) {
            if (l > 0) sb.append('\n');
            for (int c = 0; c < plateau[0].length; c++) {
                char ch = plateau[l][c];
                // '-' (vide editeur) -> ' ' (espace XSB standard)
                sb.append(ch == '-' ? ' ' : ch);
            }
        }
        return sb.toString();
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
