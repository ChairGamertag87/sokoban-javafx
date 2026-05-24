package ihm.sokoban.controller;

import ihm.sokoban.model.Direction;
import ihm.sokoban.model.JeuSokoban;
import ihm.sokoban.model.TypeCase;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Optional;

/**
 * Controleur principal de l'IHM Sokoban.
 * Regle d'or : ZERO logique metier ici.
 */
public class SokobanController {

    private static final double TAILLE_CASE = 64;

    @FXML private GridPane grid_plateau;
    @FXML private Label label_mouvements;
    @FXML private Label label_poussees;
    @FXML private Label label_caisses;
    @FXML private Label label_niveau;
    @FXML private Label label_message;
    @FXML private MenuBar menuBar;
    @FXML private Menu menuNiveaux;
    @FXML private StackPane overlay_totem;
    @FXML private ImageView img_totem;

    private static final int NB_FRAMES_TOTEM = 38;
    private static final Image[] TOTEM_FRAMES = new Image[NB_FRAMES_TOTEM];

    static {
        for (int i = 0; i < NB_FRAMES_TOTEM; i++) {
            TOTEM_FRAMES[i] = new Image(
                    SokobanController.class.getResourceAsStream(
                            String.format("/ihm/sokoban/images/totem/frame_%03d.png", i)));
        }
    }

    private JeuSokoban jeu;
    private boolean popupAffiche = false;
    private final ScoreManager scoreManager = new ScoreManager();

    @FXML
    private void initialize() {
        // Le jeu est injecte par setJeu() depuis le MenuController
    }

    public void setJeu(JeuSokoban jeu) {
        this.jeu = jeu;
        construireMenuNiveaux();
        chargerNiveau();
    }

    private void construireMenuNiveaux() {
        menuNiveaux.getItems().clear();
        int total = jeu.getNbNiveaux();
        int sauvegarde = jeu.getNiveauCourant();
        String[] noms = new String[total];
        for (int i = 0; i < total; i++) {
            jeu.chargerNiveauParIndex(i);
            noms[i] = jeu.getNomNiveauCourant();
        }
        jeu.chargerNiveauParIndex(sauvegarde);

        for (int i = 0; i < total; i++) {
            final int index = i;
            MenuItem item = new MenuItem((i + 1) + " - " + noms[i]);
            item.setOnAction(e -> {
                SoundManager.playClick();
                jeu.chargerNiveauParIndex(index);
                chargerNiveau();
            });
            menuNiveaux.getItems().add(item);
        }
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
                case ENTER, SPACE -> {} // empeche d'activer un bouton par accident
                default    -> traite = false;
            }
            if (traite) event.consume();
        });
    }

    private void chargerNiveau() {
        grid_plateau.getChildren().clear();
        grid_plateau.getRowConstraints().clear();
        grid_plateau.getColumnConstraints().clear();

        int nbLignes = jeu.getNbLignes();
        int nbCols   = jeu.getNbColonnes();

        for (int i = 0; i < nbLignes; i++) {
            grid_plateau.getRowConstraints().add(new RowConstraints(TAILLE_CASE));
        }
        for (int j = 0; j < nbCols; j++) {
            grid_plateau.getColumnConstraints().add(new ColumnConstraints(TAILLE_CASE));
        }
        for (int l = 0; l < nbLignes; l++) {
            for (int c = 0; c < nbCols; c++) {
                grid_plateau.add(creerCellule(jeu.getCase(l, c)), c, l);
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

        int record = scoreManager.getMeilleurScore(jeu.getNomNiveauCourant());
        String txtNiveau = jeu.getNomNiveauCourant()
                + "  (" + (jeu.getNiveauCourant() + 1) + "/" + jeu.getNbNiveaux() + ")";
        if (record != -1) {
            txtNiveau += "  |  Record : " + record;
        }
        label_niveau.setText(txtNiveau);
        label_message.setText("");
    }

    private void rafraichirPlateau() {
        int nbLignes = jeu.getNbLignes();
        int nbCols   = jeu.getNbColonnes();
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
        if (!jeu.peutJouer() || popupAffiche) return;
        jeu.deplacer(direction);
        SoundManager.playWalk();
        rafraichirPlateau();
        rafraichirUI();
        checkEtat();
    }

    private void checkEtat() {
        if (jeu.isNiveauTermine()) {
            SoundManager.playLevelUp();
            popupAffiche = true;

            int mouvements = jeu.getNbMouvements();
            String nomNiveau = jeu.getNomNiveauCourant();
            boolean nouveauRecord = scoreManager.enregistrer(nomNiveau, mouvements);
            String msgRecord = nouveauRecord ? "\nNouveau record !" : "";

            if (jeu.estDernierNiveau()) {
                showAlert(Alert.AlertType.INFORMATION,
                        "Bravo !",
                        "Felicitations, vous avez termine le dernier niveau !"
                        + "\n" + mouvements + " mouvements." + msgRecord);
            } else {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Niveau termine !");
                alert.setHeaderText("Niveau reussi en " + mouvements + " mouvements !" + msgRecord);
                alert.setContentText("Passer au niveau suivant ?");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    jeu.niveauSuivant();
                    chargerNiveau();
                }
            }
            popupAffiche = false;
        } else if (jeu.isPerdu()) {
            label_message.setText("Caisse bloquee ! Annulez ou recommencez.");
            popupAffiche = true;
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Partie bloquee");
            alert.setHeaderText("Une caisse est coincee dans un coin !");
            ButtonType btnAnnuler     = new ButtonType("Annuler le coup");
            ButtonType btnRecommencer = new ButtonType("Recommencer");
            alert.getButtonTypes().setAll(btnAnnuler, btnRecommencer);
            Optional<ButtonType> result = alert.showAndWait();
            popupAffiche = false;
            if (result.isPresent()) {
                if (result.get() == btnAnnuler) handleAnnuler();
                else handleRecommencer();
            }
        }
    }

    @FXML
    private void handleAnnuler() {
        SoundManager.playClick();
        if (jeu.peutAnnuler()) {
            jeu.annuler();
            rafraichirPlateau();
            rafraichirUI();
        }
    }

    @FXML
    private void handleRecommencer() {
        SoundManager.playClick();
        SoundManager.playRestart();
        img_totem.setImage(TOTEM_FRAMES[0]);
        overlay_totem.setVisible(true);

        Timeline timeline = new Timeline();
        double fps = 25.0;
        for (int i = 0; i < NB_FRAMES_TOTEM; i++) {
            final Image frame = TOTEM_FRAMES[i];
            timeline.getKeyFrames().add(
                    new KeyFrame(Duration.seconds(i / fps), e -> img_totem.setImage(frame)));
        }
        timeline.getKeyFrames().add(
                new KeyFrame(Duration.seconds(NB_FRAMES_TOTEM / fps), e -> {
                    overlay_totem.setVisible(false);
                    jeu.reset();
                    chargerNiveau();
                }));
        timeline.play();
    }

    @FXML
    private void handleNiveauSuivant() {
        SoundManager.playClick();
        if (jeu.niveauSuivant()) chargerNiveau();
    }

    @FXML
    private void handleNiveauPrecedent() {
        SoundManager.playClick();
        if (jeu.niveauPrecedent()) chargerNiveau();
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
                "Sokoban JavaFX\nProjet IHM 2026\nDeveloppe avec JavaFX 21");
    }

    @FXML
    public void handleRaccourci() {
        showAlert(Alert.AlertType.INFORMATION,
                "Raccourcis clavier",
                "Deplacement\n"
                + "  Fleches directionnelles  ->  Deplacer le joueur\n\n"
                + "Actions\n"
                + "  R  ->  Recommencer le niveau\n"
                + "  Z  ->  Annuler le dernier coup\n\n"
                + "Navigation\n"
                + "  A  ->  Niveau precedent\n"
                + "  E  ->  Niveau suivant");
    }

    @FXML
    public void handleRetourMenu() {
        SoundManager.playClick();
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/ihm/sokoban/fxml/menu.fxml"));
            Parent root = loader.load();

            MenuController menuController = loader.getController();
            Stage stage = (Stage) grid_plateau.getScene().getWindow();
            menuController.setStage(stage);

            grid_plateau.getScene().setRoot(root);

            stage.setOnCloseRequest(event -> {
                event.consume();
                stage.close();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String titre, String contenu) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(contenu);
        alert.showAndWait();
    }


}
