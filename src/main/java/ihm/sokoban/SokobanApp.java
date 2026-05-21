package ihm.sokoban;

import ihm.sokoban.controller.SokobanController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Application JavaFX principale pour le jeu Sokoban.
 * Charge le FXML, crée la Scene, branche le clavier.
 */
public class SokobanApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/ihm/sokoban/fxml/sokoban.fxml"));
        Parent root = loader.load();

        SokobanController controller = loader.getController();

        Scene scene = new Scene(root, 700, 600);
        scene.getStylesheets().add(
                getClass().getResource("/ihm/sokoban/css/sokoban.css").toExternalForm());

        controller.setupClavier(scene);

        primaryStage.setOnCloseRequest(event -> {
            event.consume();
            controller.handleQuitter();
        });

        primaryStage.setTitle("Sokoban");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main2(String[] args) {
        launch(args);
    }
}
