package ihm.sokoban.controller;

import ihm.sokoban.model.JeuSokoban;
import ihm.sokoban.util.NiveauxSokoban;
import ihm.sokoban.util.NiveauxTutoriel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Controleur de l'ecran d'accueil (menu principal).
 */
public class MenuController {

    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void handleTutoriel() {
        JeuSokoban jeu = new JeuSokoban(
                NiveauxTutoriel.getNiveaux(), NiveauxTutoriel.getNoms(), 0);
        lancerJeu(jeu);
    }

    @FXML
    private void handleNormal() {
        JeuSokoban jeu = new JeuSokoban(0);
        lancerJeu(jeu);
    }

    @FXML
    private void handleQuitter() {
        Stage s = stage;
        if (s != null) {
            s.close();
        }
    }

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
            e.printStackTrace();
        }
    }
}
