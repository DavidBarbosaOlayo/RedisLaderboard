package org.example.redisladerboard.scoreData;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.example.redisladerboard.redis.RedisManager;

import java.net.URL;
import java.util.Random;

public class LeaderboardController {

    @FXML
    private TableView<PlayerScore> tableView;

    @FXML
    private TableColumn<PlayerScore, String> playerColumn;

    @FXML
    private TableColumn<PlayerScore, Double> scoreColumn;

    @FXML
    private VBox topFiveContainer;

    private RedisManager redisManager;
    private final Random random = new Random();
    private Timeline timeline;

    @FXML
    public void initialize() {
        // Configuración de columnas
        playerColumn.setCellValueFactory(new PropertyValueFactory<>("player"));
        scoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));

        // Asignar clases CSS para el estilo (tema oscuro, textos blancos, etc.)
        tableView.getStyleClass().add("table-view");
        topFiveContainer.getStyleClass().add("top-five-container");

        // Conectarse a Redis
        redisManager = new RedisManager("localhost", 6379);

        // Insertar 50 registros si la base de datos está vacía
        if (redisManager.getTopPlayers(1).isEmpty()) {
            for (int i = 1; i <= 50; i++) {
                String player = "Jugador" + i;
                double score = 500 + random.nextInt(9501);
                redisManager.updateScore(player, score);
            }
        }

        refreshTopFive();
        refreshLeaderboard();

        // Actualización periódica cada 2 segundos
        timeline = new Timeline(new KeyFrame(Duration.seconds(0.5 + Math.random() * (2.5 - 0.5)), event -> {
            simulateUpdates();
            refreshTopFive();
            refreshLeaderboard();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    // Simula la actualización de la puntuación de jugadores aleatorios
    private void simulateUpdates() {
        int randomUpdate = (int) (15 + Math.random() * (30 - 15));
        for (int i = 0; i < randomUpdate; i++) {
            int playerNumber = 1 + random.nextInt(50);
            String player = "Jugador" + playerNumber;
            Double currentScore = redisManager.getScore(player);
            if (currentScore == null) {
                currentScore = 0.0;
            }
            double increment = 50 + random.nextInt(451);
            double newScore = currentScore + increment;
            redisManager.updateScore(player, newScore);
        }
    }

    // Actualiza el TableView con la lista completa de jugadores
    private void refreshLeaderboard() {
        ObservableList<PlayerScore> data = FXCollections.observableArrayList();
        for (String player : redisManager.getTopPlayers(50)) {
            Double score = redisManager.getScore(player);
            data.add(new PlayerScore(player, score != null ? score : 0));
        }
        Platform.runLater(() -> tableView.setItems(data));
    }

    // Actualiza el contenedor del Top 5 con iconos de medallas
    private void refreshTopFive() {
        Platform.runLater(() -> {
            topFiveContainer.getChildren().clear();
            int rank = 1;
            for (String player : redisManager.getTopPlayers(5)) {
                Double score = redisManager.getScore(player);
                HBox hbox = new HBox(10);
                hbox.getStyleClass().add("top-five-hbox");

                ImageView medalImageView = new ImageView();
                medalImageView.setPreserveRatio(true);
                URL medalUrl;
                switch (rank) {
                    case 1:
                        medalUrl = getClass().getResource("/org/example/redisladerboard/gold.png");
                        if (medalUrl != null) {
                            medalImageView.setImage(new Image(medalUrl.toExternalForm()));
                        }
                        break;
                    case 2:
                        medalUrl = getClass().getResource("/org/example/redisladerboard/silver.png");
                        if (medalUrl != null) {
                            medalImageView.setImage(new Image(medalUrl.toExternalForm()));
                        }
                        break;
                    case 3:
                        medalUrl = getClass().getResource("/org/example/redisladerboard/bronze.png");
                        if (medalUrl != null) {
                            medalImageView.setImage(new Image(medalUrl.toExternalForm()));
                        }
                        break;
                    default:
                        break;
                }
                if (rank <= 3) {
                    medalImageView.setFitWidth(24);
                    medalImageView.setFitHeight(24);
                }

                Label rankLabel = new Label(rank + ".");
                rankLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");

                Label nameLabel = new Label(player);
                nameLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");

                Label scoreLabel = new Label(String.valueOf(score != null ? score : 0));
                scoreLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");

                if (rank <= 3) {
                    hbox.getChildren().addAll(medalImageView, rankLabel, nameLabel, scoreLabel);
                } else {
                    hbox.getChildren().addAll(rankLabel, nameLabel, scoreLabel);
                }
                topFiveContainer.getChildren().add(hbox);
                rank++;
            }
        });
    }

    // Detiene la actualización y cierra la conexión
    public void shutdown() {
        if (timeline != null) {
            timeline.stop();
        }
        if (redisManager != null) {
            redisManager.close();
        }
    }
}
