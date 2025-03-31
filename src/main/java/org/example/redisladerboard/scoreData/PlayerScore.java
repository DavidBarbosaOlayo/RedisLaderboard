package org.example.redisladerboard.scoreData;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

public class PlayerScore {
    private final SimpleStringProperty player;
    private final SimpleDoubleProperty score;

    public PlayerScore(String player, double score) {
        this.player = new SimpleStringProperty(player);
        this.score = new SimpleDoubleProperty(score);
    }

    public String getPlayer() {
        return player.get();
    }

    public void setPlayer(String player) {
        this.player.set(player);
    }

    public double getScore() {
        return score.get();
    }

    public void setScore(double score) {
        this.score.set(score);
    }
}
