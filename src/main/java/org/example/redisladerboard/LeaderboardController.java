package org.example.redisladerboard;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.application.Platform;
import javafx.util.Duration;

public class LeaderboardController {

    @FXML
    private TableView<PlayerScore> tableView;
    @FXML
    private TableColumn<PlayerScore, String> playerColumn;
    @FXML
    private TableColumn<PlayerScore, Double> scoreColumn;

    private RedisManager redisManager;

    @FXML
    public void initialize() {
        // Configurar las columnas del TableView
        playerColumn.setCellValueFactory(new PropertyValueFactory<>("player"));
        scoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));

        // Conectarse a Redis
        redisManager = new RedisManager("localhost", 6379);

        // (Opcional) Insertar datos de ejemplo, si aún no están
        for (int i = 1; i <= 10; i++) {
            String player = "Jugador" + i;
            double score = 1000 * i;
            redisManager.updateScore(player, score);
        }

        // Cargar el leaderboard inicialmente
        refreshLeaderboard();

        // Configurar una actualización periódica cada 2 segundos
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(2), event -> refreshLeaderboard()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void refreshLeaderboard() {
        // Obtener la lista de jugadores desde Redis (ordenados de mayor a menor)
        ObservableList<PlayerScore> data = FXCollections.observableArrayList();
        for (String player : redisManager.getTopPlayers(10)) {
            Double score = redisManager.getScore(player);
            data.add(new PlayerScore(player, score != null ? score : 0));
        }
        // Actualizar el TableView en el hilo de la UI
        Platform.runLater(() -> tableView.setItems(data));
    }
}
