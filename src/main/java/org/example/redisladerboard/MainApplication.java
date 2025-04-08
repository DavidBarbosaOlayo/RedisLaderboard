package org.example.redisladerboard;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.redisladerboard.scoreData.LeaderboardController;

import java.io.IOException;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("leaderboard-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        // Agregar la hoja de estilos (ajusta la ruta según donde esté ubicado)
        String css = MainApplication.class.getResource("estilos.css").toExternalForm();
        scene.getStylesheets().add(css);

        stage.setTitle("Redis-Leaderboards!");
        stage.setScene(scene);

        LeaderboardController controller = fxmlLoader.getController();
        stage.setOnCloseRequest(event -> {
            controller.shutdown();
        });

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
