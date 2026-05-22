package ihm.sokoban.controller;

import ihm.sokoban.model.Direction;
import ihm.sokoban.model.JeuSokoban;
import ihm.sokoban.model.TypeCase;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Contrôleur principal de l'IHM Sokoban.
 *
 * Règle d'or : ZERO logique métier ici.
 * Tout passe par les méthodes de JeuSokoban.
 */
public class SokobanController {



    private static final double TAILLE_CASE = 50;

    @FXML private GridPane grid_plateau;
    @FXML private Label label_mouvements;
    @FXML private Label label_poussees;
    @FXML private Label label_caisses;
    @FXML private Label label_niveau;
    @FXML private Label label_message;
    @FXML private MenuBar menuBar;

    private JeuSokoban jeu;

    @FXML
    private void initialize() {
        jeu = new JeuSokoban(0);

        // Affiche le premier niveau
        chargerNiveau();
    }

    public void setupClavier(javafx.scene.Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            boolean traite = true;
            switch (event.getCode()) {
                case UP    -> deplacerJoueur(Direction.HAUT);
                case DOWN  -> deplacerJoueur(Direction.BAS);
                case LEFT  -> deplacerJoueur(Direction.GAUCHE);
                case RIGHT -> deplacerJoueur(Direction.DROITE);
                case R     -> handleRecommencer();
                case Z     -> handleAnnuler();
                case A     -> handleNiveauPrecedent();
                case E     -> handleNiveauSuivant();
                default    -> traite = false;
            }
            if (traite) {
                event.consume();
            }
        });
    }


    private void chargerNiveau() {
        grid_plateau.getChildren().clear();
        grid_plateau.getRowConstraints().clear();
        grid_plateau.getColumnConstraints().clear();

        int nbLignes = jeu.getNbLignes();
        int nbCols = jeu.getNbColonnes();

        for (int i = 0; i < nbLignes; i++) {
            RowConstraints rc = new RowConstraints(TAILLE_CASE);
            grid_plateau.getRowConstraints().add(rc);
        }

        for (int j = 0; j < nbCols; j++) {
            ColumnConstraints cc = new ColumnConstraints(TAILLE_CASE);
            grid_plateau.getColumnConstraints().add(cc);
        }

        for (int l = 0; l < nbLignes; l++) {
            for (int c = 0; c < nbCols; c++) {
                Region cellule = creerCellule(jeu.getCase(l, c));
                grid_plateau.add(cellule, c, l);
            }
        }

        rafraichirUI();
    }


    private Region creerCellule(TypeCase typeCase) {
        Region cellule = new Region();
        cellule.setPrefSize(TAILLE_CASE, TAILLE_CASE);
        cellule.getStyleClass().add("case");
        cellule.getStyleClass().add(getCssClass(typeCase));
        return cellule;
    }

    private String getCssClass(TypeCase tc) {
        return switch (tc) {
            case MUR              -> "case-mur";
            case SOL              -> "case-sol";
            case CIBLE            -> "case-cible";
            case CAISSE           -> "case-caisse";
            case CAISSE_SUR_CIBLE -> "case-caisse-cible";
            case JOUEUR           -> "case-joueur";
            case JOUEUR_SUR_CIBLE -> "case-joueur-cible";
            case VIDE             -> "case-vide";
        };
    }

    private void rafraichirUI() {
        label_mouvements.setText("Mouvements : " + jeu.getNbMouvements());
        label_poussees.setText("Poussees : " + jeu.getNbPoussees());
        label_caisses.setText("Caisses : " + jeu.getNbCaissesSurCible() + "/" + jeu.getNbCaisses());
        label_niveau.setText(jeu.getNomNiveauCourant()
                + "  (" + (jeu.getNiveauCourant() + 1) + "/" + jeu.getNbNiveaux() + ")");
        label_message.setText("");
    }

    private void rafraichirPlateau() {
        int nbLignes = jeu.getNbLignes();
        int nbCols = jeu.getNbColonnes();

        for (int l = 0; l < nbLignes; l++) {
            for (int c = 0; c < nbCols; c++) {
                Region cellule = (Region) grid_plateau.getChildren().get(l * nbCols + c);
                cellule.getStyleClass().clear();
                cellule.getStyleClass().add("case");
                cellule.getStyleClass().add(getCssClass(jeu.getCase(l, c)));
            }
        }
    }


    private void deplacerJoueur(Direction direction) {
        if (!jeu.peutJouer()) {
            return;
        }

        jeu.deplacer(direction);

        rafraichirPlateau();
        rafraichirUI();

        checkEtat();
    }

    private void checkEtat() {
        if (jeu.isNiveauTermine()) {
            if (jeu.estDernierNiveau()) {
                showAlert(Alert.AlertType.INFORMATION,
                        "Bravo !",
                        "Felicitations, vous avez termine le dernier niveau !");
            } else {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Niveau termine !");
                alert.setHeaderText("Niveau reussi en " + jeu.getNbMouvements() + " mouvements !");
                alert.setContentText("Passer au niveau suivant ?");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    jeu.niveauSuivant();
                    chargerNiveau();
                }
            }
        } else if (jeu.isPerdu()) {
            label_message.setText("Caisse bloquee ! Annulez ou recommencez.");

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Partie bloquee");
            alert.setHeaderText("Une caisse est coincée dans un coin !");

            ButtonType btnAnnuler = new ButtonType("Annuler le coup");
            ButtonType btnRecommencer = new ButtonType("Recommencer");
            alert.getButtonTypes().setAll(btnAnnuler, btnRecommencer);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent()) {
                if (result.get() == btnAnnuler) {
                    handleAnnuler();
                } else {
                    handleRecommencer();
                }
            }
        }
    }

    @FXML
    private void handleAnnuler() {
        if (jeu.peutAnnuler()) {
            jeu.annuler();
            rafraichirPlateau();
            rafraichirUI();
        }
    }

    @FXML
    private void handleRecommencer() {
        jeu.reset();
        chargerNiveau();
    }

    @FXML
    private void handleNiveauSuivant() {
        if (jeu.niveauSuivant()) {
            chargerNiveau();
        }
    }

    @FXML
    private void handleNiveauPrecedent() {
        if (jeu.niveauPrecedent()) {
            chargerNiveau();
        }
    }

    @FXML
    public void handleQuitter() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Quitter");
        alert.setHeaderText("Voulez-vous vraiment quitter ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Stage stage = (Stage) grid_plateau.getScene().getWindow();
            stage.close();
        }
    }

    @FXML
    private void handleAPropos() {
        showAlert(Alert.AlertType.INFORMATION,
                "A propos",
                "Sokoban JavaFX\nProjet IHM 2026\nDeveloppé avec JavaFX 21");
    }


    private void showAlert(Alert.AlertType type, String titre, String contenu) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(contenu);
        alert.showAndWait();
    }
}
