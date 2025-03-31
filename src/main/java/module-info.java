module org.example.redisladerboard {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires redis.clients.jedis;

    opens org.example.redisladerboard to javafx.fxml;
    exports org.example.redisladerboard;
    exports org.example.redisladerboard.redis;
    opens org.example.redisladerboard.redis to javafx.fxml;
    exports org.example.redisladerboard.scoreData;
    opens org.example.redisladerboard.scoreData to javafx.fxml;
}