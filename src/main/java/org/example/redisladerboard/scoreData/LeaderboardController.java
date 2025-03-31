package org.example.redisladerboard.scoreData;

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
import org.example.redisladerboard.redis.RedisManager;

import java.util.Random;

public class LeaderboardController {
    @FXML
    private TableView<PlayerScore> tableView;
    @FXML
    private TableColumn<PlayerScore, String> playerColumn;
    @FXML
    private TableColumn<PlayerScore, Double> scoreColumn;

    private RedisManager redisManager;
    private final Random random = new Random();

    @FXML
    public void initialize() {
        // columnas del TableView
        playerColumn.setCellValueFactory(new PropertyValueFactory<>("player"));
        scoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));

        // Conectamos con redis
        redisManager = new RedisManager("localhost", 6379);

        // Insertamos registros si la base de datos está vacía
        if (redisManager.getTopPlayers(1).isEmpty()) {
            for (int i = 1; i <= 50; i++) {
                String player = "Jugador" + i;
                double score = 500 + random.nextInt(9501);
                redisManager.updateScore(player, score);
            }
        }

        // Cargamos el leaderboard inicialmente
        refreshLeaderboard();

        // simulamos una actualización periódica cada 2 segundos:
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(2), event -> {
            simulateUpdates();
            refreshLeaderboard();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void simulateUpdates() {
        // Se actualizan aleatoriamente 15 jugadores cada 2 segundos
        for (int i = 0; i < 15; i++) {
            int playerNumber = 1 + random.nextInt(50);
            String player = "Jugador" + playerNumber;
            Double currentScore = redisManager.getScore(player);

            if (currentScore == null) {
                currentScore = 0.0;
            }
            // Generamos un incremento aleatorio entre 50 y 500
            double increment = 50 + random.nextInt(451);
            double newScore = currentScore + increment;
            redisManager.updateScore(player, newScore);
        }
    }


    private void refreshLeaderboard() {
        // Obtenemos la lista de jugadores desde Redis (ordenados de mayor a menor)
        ObservableList<PlayerScore> data = FXCollections.observableArrayList();

        for (String player : redisManager.getTopPlayers(50)) {
            Double score = redisManager.getScore(player);
            data.add(new PlayerScore(player, score != null ? score : 0));
        }
        // Actualizar el TableView en el hilo de la UI
        Platform.runLater(() -> tableView.setItems(data));
    }
}
