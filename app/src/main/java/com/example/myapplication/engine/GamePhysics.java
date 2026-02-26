package com.example.myapplication.engine;

import android.graphics.Bitmap;
import com.example.myapplication.engine.systems.*;
import com.example.myapplication.model.Bullet;
import com.example.myapplication.model.GameData;
import com.example.myapplication.model.GameEntity;
import java.util.Random;

public class GamePhysics {
    private final GameData data;
    private final int screenX, screenY;
    private final Random random = new Random();
    private final Bitmap enemyBitmap;

    // Системы
    private final DifficultyConfig config = new DifficultyConfig();
    private final LevelSystem levelSystem = new LevelSystem();
    private final PlayerSystem playerSystem = new PlayerSystem();
    private final CollisionSystem collisionSystem = new CollisionSystem();

    private long lastSpawnTime = 0;
    private long lastShotTime = 0;
    private float bossDirection = 1f;

    public GamePhysics(GameData data, int x, int y, Bitmap enemyBitmap) {
        this.data = data;
        this.screenX = x;
        this.screenY = y;
        this.enemyBitmap = enemyBitmap;
    }

    public void resetDifficulty() {
        config.reset();
    }

    public void update(float deltaTime, int targetX) {
        if (data.currentState == GameData.GameState.MENU || data.currentState == GameData.GameState.GAME_OVER) return;

        // 1. Управление игроком
        playerSystem.update(data, deltaTime, targetX, config.playerSpeed);

        // 2. Логика состояний
        if (data.currentState == GameData.GameState.PLAYING) {
            updateNormalMode(deltaTime);
        } else if (data.currentState == GameData.GameState.BOSS_STATE) {
            updateBossMode(deltaTime);
        }
    }

    private void updateNormalMode(float deltaTime) {
        // Проверка на босса (циклическое появление каждые 100 очков)
        if (data.score >= data.nextBossScore) {
            startBossBattle();
            return;
        }

        // Уровни и сложность
        levelSystem.update(data, config);

        // Спавн и движение врагов
        synchronized (data.enemies) {
            long now = System.currentTimeMillis();
            if (now - lastSpawnTime > config.spawnInterval) {
                spawnEnemy();
                lastSpawnTime = now;
            }

            for (int i = data.enemies.size() - 1; i >= 0; i--) {
                GameEntity enemy = data.enemies.get(i);
                enemy.y += config.enemySpeed * deltaTime;
                enemy.updateRect();

                if (enemy.y > screenY) {
                    recycleEnemy(i);
                    data.score++;
                } else if (collisionSystem.isColliding(data.player, enemy)) {
                    data.currentState = GameData.GameState.GAME_OVER;
                }
            }
        }
    }

    private void updateBossMode(float deltaTime) {
        if (data.boss == null) return;

        // Движение босса
        if (data.boss.y < 200) data.boss.y += 200 * deltaTime;
        else {
            data.boss.x += bossDirection * 400 * deltaTime;
            if (data.boss.x <= 0 || data.boss.x >= screenX - data.boss.rect.width()) bossDirection *= -1;
        }
        data.boss.updateRect();

        // Стрельба
        long now = System.currentTimeMillis();
        if (now - lastShotTime > 2000) {
            data.bullets.add(new Bullet(data.player.rect.centerX(), data.player.y));
            lastShotTime = now;
        }

        // Пули
        for (int i = data.bullets.size() - 1; i >= 0; i--) {
            Bullet b = data.bullets.get(i);
            b.update(deltaTime);
            if (b.y < -50) data.bullets.remove(i);
            else if (data.boss.rect.contains((int)b.x, (int)b.y)) {
                data.bullets.remove(i);
                data.bossHP--;
                if (data.bossHP <= 0) winBossBattle();
            }
        }

        if (collisionSystem.isColliding(data.player, data.boss)) {
            data.currentState = GameData.GameState.GAME_OVER;
        }
    }

    private void startBossBattle() {
        data.currentState = GameData.GameState.BOSS_STATE;
        synchronized (data.enemies) {
            data.enemyPool.addAll(data.enemies);
            data.enemies.clear();
        }
        data.boss = new GameEntity(enemyBitmap, screenX / 2f - enemyBitmap.getWidth(), -enemyBitmap.getHeight() * 2);
        data.bossHP = 2; // для тестов хп снижено до 2 (сеччас не трогать)изменить потом на 20
    }

    private void winBossBattle() {
        data.score += 50;
        // Устанавливаем порог для следующего босса (например, через каждые 150 очков после текущего счета)
        data.nextBossScore = data.score + 100; //парамет отвечающий за появление босс изменить потом на 100
        
        data.boss = null;
        data.bullets.clear();
        data.currentState = GameData.GameState.PLAYING;
        
        // Показываем сообщение о победе
        data.levelMessage = "BOSS DEFEATED!";
        data.levelMessageEndTime = System.currentTimeMillis() + 2000;
    }

    private void spawnEnemy() {
        int x = random.nextInt(Math.max(1, screenX - enemyBitmap.getWidth()));
        GameEntity enemy = data.enemyPool.isEmpty() ? 
            new GameEntity(enemyBitmap, x, -enemyBitmap.getHeight()) : 
            data.enemyPool.remove(data.enemyPool.size() - 1);
        enemy.set(x, -enemyBitmap.getHeight());
        data.enemies.add(enemy);
    }

    private void recycleEnemy(int index) {
        data.enemyPool.add(data.enemies.remove(index));
    }
}
