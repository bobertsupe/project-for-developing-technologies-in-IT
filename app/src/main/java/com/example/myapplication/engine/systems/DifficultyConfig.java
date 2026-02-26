package com.example.myapplication.engine.systems;

public class DifficultyConfig {
    public float enemySpeed = 850f;
    public int spawnInterval = 350;
    public float playerSpeed = 1600f;

    public void reset() {
        enemySpeed = 850f;
        spawnInterval = 350;
        playerSpeed = 1600f;
    }
}
