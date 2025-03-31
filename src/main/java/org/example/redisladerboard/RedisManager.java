package org.example.redisladerboard;

import redis.clients.jedis.Jedis;
import java.util.List;

public class RedisManager {
    private Jedis jedis;

    // Constructor: conecta a Redis usando el host y puerto indicados.
    public RedisManager(String host, int port) {
        jedis = new Jedis(host, port);
    }

    // Actualiza la puntuación de un jugador en el sorted set "leaderboard"
    public void updateScore(String player, double score) {
        jedis.zadd("leaderboard", score, player);
    }

    // Recupera los mejores jugadores (ordenados de mayor a menor puntuación)
    public List<String> getTopPlayers(int count) {
        return jedis.zrevrange("leaderboard", 0, count - 1);
    }

    // Obtiene la puntuación actual de un jugador
    public Double getScore(String player) {
        return jedis.zscore("leaderboard", player);
    }

    // Cierra la conexión con Redis
    public void close() {
        if (jedis != null) {
            jedis.close();
        }
    }
}
