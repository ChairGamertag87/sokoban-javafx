package ihm.sokoban;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Application JavaFX principale pour le jeu Sokoban.
 */
public class SokobanApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        BorderPane root = new BorderPane();

        Label temp = new Label("Projet IHM 2026");
        root.setCenter(temp);

        Scene scene = new Scene(root,320,200);

        primaryStage.setTitle("Bientot un jeu de Sokoban");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main2(String[] args) {
        launch(args);
    }
}
