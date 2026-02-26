package com.example.myapplication.engine.systems;

import com.example.myapplication.model.GameData;

public class LevelSystem {
    private static final int MIN_SPAWN_INTERVAL_MS = 300;
    
    public void update(GameData data, DifficultyConfig config) {
        int calculatedLevel = (data.score / 10) + 1;
        if (calculatedLevel > data.currentLevel) {
            data.currentLevel = calculatedLevel;
            config.enemySpeed *= 1.06f;
            config.spawnInterval = Math.max(MIN_SPAWN_INTERVAL_MS, config.spawnInterval - 50);
            
            data.levelMessage = "LEVEL " + calculatedLevel;
            data.levelMessageEndTime = System.currentTimeMillis() + 1500;
        }
    }
}
