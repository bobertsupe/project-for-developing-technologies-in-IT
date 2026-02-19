package com.example.myapplication.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Хранилище всех динамических данных игры.
 */
public class GameData {
    public int score = 0;
    public volatile boolean isGameOver = false;
    public GameEntity player;
    public final List<GameEntity> enemies = new ArrayList<>();

    public void reset() {
        score = 0;
        isGameOver = false;
        synchronized (enemies) {
            enemies.clear();
        }
    }
}
