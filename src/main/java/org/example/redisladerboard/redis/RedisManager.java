package org.example.redisladerboard.redis;

import redis.clients.jedis.Jedis;
import java.util.List;

public class RedisManager {
    private Jedis jedis;

    public RedisManager(String host, int port) {
        jedis = new Jedis(host, port);
    }

    // Actualiza la puntuación de un jugador en el sorted set leaderboard
    public void updateScore(String player, double score) {
        jedis.zadd("leaderboard", score, player);
    }

    // Recupera los mejores jugadores, ordenados de mayor a menor puntuación
    public List<String> getTopPlayers(int count) {
        return jedis.zrevrange("leaderboard", 0, count - 1);
    }

    public Double getScore(String player) {
        return jedis.zscore("leaderboard", player);
    }

    public void close() {
        if (jedis != null) {
            jedis.close();
        }
    }
}
