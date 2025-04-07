package org.example.redisladerboard.scoreData;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.application.Platform;
import javafx.util.Duration;
import org.example.redisladerboard.redis.RedisManager;

import java.util.Random;

public class LeaderboardController {

    // Tabla principal del leaderboard
    @FXML
    private TableView<PlayerScore> tableView;
    @FXML
    private TableColumn<PlayerScore, String> playerColumn;
    @FXML
    private TableColumn<PlayerScore, Double> scoreColumn;

    // Contenedor para mostrar el Top 5 con estilo destacado
    @FXML
    private VBox topFiveContainer;

    private RedisManager redisManager;
    private final Random random = new Random();
    private Timeline timeline; // Referencia al Timeline para actualizaciones periódicas

    @FXML
    public void initialize() {
        // Configurar columnas de la tabla principal y establecer anchos fijos para una mejor visualización
        playerColumn.setCellValueFactory(new PropertyValueFactory<>("player"));
        playerColumn.setPrefWidth(250);
        scoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));
        scoreColumn.setPrefWidth(250);

        // Opcional: Estilo global para la tabla
        tableView.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc;");

        // Aplicar un estilo al contenedor del Top 5
        topFiveContainer.setStyle("-fx-background-color: #f0f8ff; -fx-padding: 10px; -fx-border-color: #cccccc; -fx-border-width: 2px; -fx-border-radius: 5px;");

        // Conectarse a Redis
        redisManager = new RedisManager("localhost", 6379);

        // Insertar 50 registros si la base de datos está vacía
        if (redisManager.getTopPlayers(1).isEmpty()) {
            for (int i = 1; i <= 50; i++) {
                String player = "Jugador" + i;
                double score = 500 + random.nextInt(9501); // Puntuación aleatoria entre 500 y 10,000
                redisManager.updateScore(player, score);
            }
        }

        // Cargar tanto el top 5 personalizado como la tabla completa inicialmente
        refreshTopFive();
        refreshLeaderboard();

        // Actualización periódica cada 2 segundos:
        timeline = new Timeline(new KeyFrame(Duration.seconds(2), event -> {
            simulateUpdates();
            refreshTopFive();
            refreshLeaderboard();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void simulateUpdates() {
        // Actualizar aleatoriamente 15 jugadores cada 2 segundos, siempre aumentando la puntuación
        for (int i = 0; i < 15; i++) {
            int playerNumber = 1 + random.nextInt(50);
            String player = "Jugador" + playerNumber;
            Double currentScore = redisManager.getScore(player);
            if (currentScore == null) {
                currentScore = 0.0;
            }
            // Generar un incremento aleatorio entre 50 y 500
            double increment = 50 + random.nextInt(451);
            double newScore = currentScore + increment;
            redisManager.updateScore(player, newScore);
        }
    }

    private void refreshLeaderboard() {
        // Obtener la lista de jugadores desde Redis (ordenados de mayor a menor)
        ObservableList<PlayerScore> data = FXCollections.observableArrayList();
        for (String player : redisManager.getTopPlayers(50)) {
            Double score = redisManager.getScore(player);
            data.add(new PlayerScore(player, score != null ? score : 0));
        }
        // Actualizar el TableView en el hilo de la UI
        Platform.runLater(() -> tableView.setItems(data));
    }

    private void refreshTopFive() {
        // Actualizar el contenedor con el Top 5 de jugadores usando labels y estilos para destacar la sección
        Platform.runLater(() -> {
            topFiveContainer.getChildren().clear();
            int rank = 1;
            for (String player : redisManager.getTopPlayers(5)) {
                Double score = redisManager.getScore(player);

                // Crear un HBox para cada jugador del top 5 con espaciado de 10px
                HBox hbox = new HBox(10);
                hbox.setStyle("-fx-alignment: center-left; -fx-padding: 5px; -fx-background-radius: 5px;");

                // Establecer un fondo distintivo según la posición
                switch (rank) {
                    case 1:
                        hbox.setStyle(hbox.getStyle() + " -fx-background-color: gold;");
                        break;
                    case 2:
                        hbox.setStyle(hbox.getStyle() + " -fx-background-color: silver;");
                        break;
                    case 3:
                        hbox.setStyle(hbox.getStyle() + " -fx-background-color: #cd7f32;"); // Bronze
                        break;
                    default:
                        hbox.setStyle(hbox.getStyle() + " -fx-background-color: #e0e0e0;");
                        break;
                }

                // Label para la posición (más grande y en negrita)
                Label rankLabel = new Label(rank + ".");
                rankLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #333333;");

                // Label para el nombre del jugador
                Label nameLabel = new Label(player);
                nameLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #333333;");

                // Label para la puntuación
                Label scoreLabel = new Label(String.valueOf(score != null ? score : 0));
                scoreLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #333333;");

                hbox.getChildren().addAll(rankLabel, nameLabel, scoreLabel);
                topFiveContainer.getChildren().add(hbox);
                rank++;
            }
        });
    }

    public void shutdown() {
        if (timeline != null) {
            timeline.stop();
        }
        if (redisManager != null) {
            redisManager.close();
        }
    }
}
