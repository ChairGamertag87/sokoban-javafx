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
    private boolean animationEnCours = false;
    private final ScoreManager scoreManager = new ScoreManager();

    /**
     * Initialise le controleur FXML.
     * Le jeu est injecte apres par setJeu() depuis le MenuController.
     */
    @FXML
    private void initialize() {
        // Le jeu est injecte par setJeu() depuis le MenuController
    }

    /**
     * Injecte le modele du jeu et initialise l'affichage.
     * Construit le menu des niveaux et charge le niveau courant.
     *
     * @param jeu l'instance du moteur de jeu Sokoban
     */
    public void setJeu(JeuSokoban jeu) {
        this.jeu = jeu;
        construireMenuNiveaux();
        chargerNiveau();
    }

    /**
     * Construit dynamiquement le menu deroulant des niveaux.
     * Chaque MenuItem charge le niveau correspondant au clic.
     */
    private void construireMenuNiveaux() {
        menuNiveaux.getItems().clear();
        int total = jeu.getNbNiveaux();
        for (int i = 0; i < total; i++) {
            final int index = i;
            MenuItem item = new MenuItem("Niveau " + (i + 1));
            item.setOnAction(e -> {
                SoundManager.playClick();
                jeu.chargerNiveauParIndex(index);
                chargerNiveau();
            });
            menuNiveaux.getItems().add(item);
        }
    }

    /**
     * Configure les raccourcis clavier sur la scene via un EventFilter.
     * Bloque les entrees si un popup ou une animation est en cours.
     *
     * @param scene la scene JavaFX sur laquelle ecouter les evenements clavier
     */
    public void setupClavier(javafx.scene.Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (popupAffiche || animationEnCours) {
                event.consume();
                return;
            }
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

    /**
     * Charge et affiche le niveau courant dans le GridPane.
     * Reconstruit entierement la grille (contraintes + cellules).
     */
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

    /**
     * Cree une cellule graphique (Region) pour une case du plateau.
     * Applique la classe CSS correspondant au type de la case.
     *
     * @param typeCase le type de la case a representer
     * @return une Region configuree avec la bonne classe CSS
     */
    private Region creerCellule(TypeCase typeCase) {
        Region cellule = new Region();
        cellule.setPrefSize(TAILLE_CASE, TAILLE_CASE);
        cellule.getStyleClass().add("case");
        cellule.getStyleClass().add(getCssClass(typeCase));
        return cellule;
    }

    /**
     * Retourne le nom de la classe CSS associee a un TypeCase.
     *
     * @param tc le type de case du modele
     * @return le nom de la classe CSS correspondante
     */
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

    /**
     * Met a jour les labels de statistiques (mouvements, poussees, caisses, niveau, record).
     */
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

    /**
     * Rafraichit les classes CSS de toutes les cellules du plateau
     * sans reconstruire la grille (mise a jour rapide apres un deplacement).
     */
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

    /**
     * Deplace le joueur dans la direction donnee si la partie est en cours.
     * Joue un son de pas, rafraichit l'affichage et verifie l'etat du jeu.
     *
     * @param direction la direction du deplacement (HAUT, BAS, GAUCHE, DROITE)
     */
    private void deplacerJoueur(Direction direction) {
        if (!jeu.peutJouer() || popupAffiche || animationEnCours) return;
        jeu.deplacer(direction);
        SoundManager.playWalk();
        rafraichirPlateau();
        rafraichirUI();
        checkEtat();
    }

    /**
     * Verifie l'etat de la partie apres chaque deplacement.
     * Gere la victoire (popup + passage au niveau suivant ou message final)
     * et la defaite (popup avec choix annuler ou recommencer).
     */
    private void checkEtat() {
        if (jeu.isNiveauTermine() && jeu.getNbCaissesSurCible() == jeu.getNbCaisses()) {
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
                if (result.get() == btnAnnuler) {
                    handleAnnuler();
                } else {
                    handleRecommencer();
                }
            }
            // Apres annulation/recommencer, on force le rechargement de l'etat
            rafraichirPlateau();
            rafraichirUI();
        }
    }

    /**
     * Annule le dernier coup joue si possible.
     * Rafraichit le plateau et les statistiques apres l'annulation.
     */
    @FXML
    private void handleAnnuler() {
        SoundManager.playClick();
        if (jeu.peutAnnuler()) {
            jeu.annuler();
            rafraichirPlateau();
            rafraichirUI();
        }
    }

    /**
     * Recommence le niveau courant avec une animation totem (38 frames).
     * Bloque les entrees pendant l'animation puis reinitialise le niveau.
     */
    @FXML
    private void handleRecommencer() {
        if (animationEnCours) return;
        SoundManager.playClick();
        SoundManager.playRestart();
        animationEnCours = true;
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
                    animationEnCours = false;
                    jeu.reset();
                    chargerNiveau();
                }));
        timeline.play();
    }

    /**
     * Passe au niveau suivant si disponible et recharge la grille.
     */
    @FXML
    private void handleNiveauSuivant() {
        SoundManager.playClick();
        if (jeu.niveauSuivant()) chargerNiveau();
    }

    /**
     * Revient au niveau precedent si disponible et recharge la grille.
     */
    @FXML
    private void handleNiveauPrecedent() {
        SoundManager.playClick();
        if (jeu.niveauPrecedent()) chargerNiveau();
    }

    /**
     * Affiche une popup de confirmation avant de quitter l'application.
     * Ferme la fenetre si l'utilisateur confirme.
     */
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

    /**
     * Affiche la popup "A propos" avec des emojis Unicode.
     * Utilise la police Segoe UI Emoji pour le rendu des symboles.
     */
    @FXML
    private void handleAPropos() {
        String texte =
                "✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨\n\n"
                + "🎮  Sokoban JavaFX  🎮\n"
                + "📚  Projet IHM 2026  📚\n"
                + "☕  Developpe avec JavaFX 21  ☕\n\n"
                + "🧱🧱🧱 THEME MINECRAFT 🧱🧱🧱\n\n"
                + "🏆 Fonctionnalites 🏆\n"
                + "📦 10 niveaux + editeur integre\n"
                + "🔊 Sons Minecraft immersifs\n"
                + "🎵 6 bruits de pas aleatoires\n"
                + "📈 Sauvegarde des meilleurs scores\n"
                + "↩ Undo illimite\n"
                + "📂 Chargement de niveaux .xsb\n"
                + "🎨 Theme complet avec textures\n\n"
                + "🔥🔥🔥 MERCI D'AVOIR JOUE 🔥🔥🔥\n\n"
                + "✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨";

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("A propos");
        alert.setHeaderText(null);

        Label label = new Label(texte);
        label.setFont(javafx.scene.text.Font.font("Segoe UI Emoji", 14));
        label.setWrapText(true);

        alert.getDialogPane().setContent(label);
        alert.showAndWait();
    }

    /**
     * Affiche une popup listant tous les raccourcis clavier disponibles.
     */
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

    /**
     * Retourne au menu principal en rechargeant le FXML du menu.
     * Remplace la racine de la scene par la vue du menu.
     */
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

    /**
     * Affiche une popup d'alerte generique.
     *
     * @param type    le type d'alerte (INFORMATION, WARNING, etc.)
     * @param titre   le titre de la fenetre
     * @param contenu le message affiche dans la popup
     */
    private void showAlert(Alert.AlertType type, String titre, String contenu) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(contenu);
        alert.showAndWait();
    }


}
