package ihm.sokoban;

import ihm.sokoban.controller.MenuController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * Application JavaFX principale pour le jeu Sokoban.
 * Affiche le menu principal au demarrage.
 */
public class SokobanApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        Font.loadFont(getClass().getResourceAsStream("/ihm/sokoban/fonts/Minecraftia-Regular.ttf"), 16);

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/ihm/sokoban/fxml/menu.fxml"));
        Parent root = loader.load();

        MenuController controller = loader.getController();
        controller.setStage(primaryStage);

        Scene scene = new Scene(root, 700, 600);
        scene.getStylesheets().add(
                getClass().getResource("/ihm/sokoban/css/sokoban.css").toExternalForm());

        primaryStage.setTitle("Sokoban JavaFX");
        primaryStage.getIcons().add(new javafx.scene.image.Image(getClass().getResourceAsStream("/ihm/sokoban/images/logo.png")));
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public static void main2(String[] args) {
        launch(args);
    }
}
