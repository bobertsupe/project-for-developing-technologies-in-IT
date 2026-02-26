package com.example.myapplication.model;

import android.graphics.RectF;
import java.util.ArrayList;
import java.util.List;

/**
 * Хранилище всех динамических данных игры.
 */
public class GameData {
    public enum GameState {
        MENU, PLAYING, BOSS_STATE, GAME_OVER
    }

    public int score = 0;
    public int currentLevel = 1;
    public int nextBossScore = 100;
    public String levelMessage = "";
    public long levelMessageEndTime = 0;

    public GameState currentState = GameState.MENU;
    public GameEntity player;
    public final List<GameEntity> enemies = new ArrayList<>();
    public final List<GameEntity> enemyPool = new ArrayList<>();
    
    // Босс и пули
    public GameEntity boss;
    public int bossHP = 20;
    public final List<Bullet> bullets = new ArrayList<>();
    
    // Вспомогательный прямоугольник для кнопки START
    public final RectF startButtonRect = new RectF();

    public void reset() {
        score = 0;
        currentLevel = 1;
        nextBossScore = 10; //для тестов сейчас параметер 10 100
        levelMessage = "";
        levelMessageEndTime = 0;
        currentState = GameState.PLAYING;
        boss = null;
        bossHP = 20;
        bullets.clear();
        synchronized (enemies) {
            enemyPool.addAll(enemies);
            enemies.clear();
        }
    }
}
