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

    /**
     * Demarre l'application : charge la police, le menu principal,
     * applique le CSS et affiche la fenetre en plein ecran.
     *
     * @param primaryStage la fenetre principale fournie par JavaFX
     * @throws Exception si le chargement du FXML echoue
     */
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

    /**
     * Point d'entree alternatif pour lancer l'application JavaFX.
     *
     * @param args les arguments de la ligne de commande
     */
    public static void main2(String[] args) {
        launch(args);
    }
}
