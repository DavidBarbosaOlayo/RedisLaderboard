package org.example.redisladerboard;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.application.Platform;

public class HelloController {
    @FXML
    private Label welcomeText;

    @FXML
    public void initialize() {
        // Inicializamos la base de datos con 10 registros
        RedisManager redisManager = new RedisManager("localhost", 6379);
        try {
            for (int i = 1; i <= 10; i++) {
                String player = "Jugador" + i;
                double score = 1000 * i; // Por ejemplo: 1000, 2000, 3000, etc.
                redisManager.updateScore(player, score);
            }
            // Puedes actualizar la UI para indicar que la inserción fue exitosa.
            Platform.runLater(() -> welcomeText.setText("10 registros insertados en Redis"));
        } catch (Exception e) {
            Platform.runLater(() -> welcomeText.setText("Error al insertar datos: " + e.getMessage()));
        } finally {
            redisManager.close();
        }
    }

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}
