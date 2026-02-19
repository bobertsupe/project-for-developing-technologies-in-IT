package com.example.myapplication.engine;

import android.graphics.Bitmap;
import android.graphics.Rect;
import com.example.myapplication.model.GameData;
import com.example.myapplication.model.GameEntity;
import java.util.Random;

public class GamePhysics {
    private final GameData data;
    private final int screenX, screenY;
    private final Random random = new Random();
    private final Bitmap enemyBitmap;

    private static final float PLAYER_SPEED = 1600f;
    private static final float ENEMY_SPEED = 850f;
    private static final int SPAWN_INTERVAL = 350;
    private long lastSpawnTime = 0;

    public GamePhysics(GameData data, int x, int y, Bitmap enemyBitmap) {
        this.data = data;
        this.screenX = x;
        this.screenY = y;
        this.enemyBitmap = enemyBitmap;
    }

    public void update(float deltaTime, int targetX) {
        if (data.isGameOver) return;

        // Игрок
        float playerCenterX = data.player.rect.centerX();
        if (Math.abs(playerCenterX - targetX) > 10) {
            if (playerCenterX < targetX) data.player.x += PLAYER_SPEED * deltaTime;
            else data.player.x -= PLAYER_SPEED * deltaTime;
            data.player.updateRect();
        }

        // Враги
        synchronized (data.enemies) {
            long now = System.currentTimeMillis();
            if (now - lastSpawnTime > SPAWN_INTERVAL) {
                spawnEnemy();
                lastSpawnTime = now;
            }

            for (int i = data.enemies.size() - 1; i >= 0; i--) {
                GameEntity enemy = data.enemies.get(i);
                enemy.y += ENEMY_SPEED * deltaTime;
                enemy.updateRect();

                if (enemy.y > screenY) {
                    data.enemies.remove(i);
                    data.score++;
                } else if (isColliding(data.player, enemy)) {
                    data.isGameOver = true;
                }
            }
        }
    }

    private void spawnEnemy() {
        int x = random.nextInt(screenX - enemyBitmap.getWidth());
        data.enemies.add(new GameEntity(enemyBitmap, x, -enemyBitmap.getHeight()));
    }

    private boolean isColliding(GameEntity p, GameEntity e) {
        float scale = 0.7f;
        int pw = (int) (p.rect.width() * (1 - scale) / 2);
        int ph = (int) (p.rect.height() * (1 - scale) / 2);
        int ew = (int) (e.rect.width() * (1 - scale) / 2);
        int eh = (int) (e.rect.height() * (1 - scale) / 2);

        return p.rect.left + pw < e.rect.right - ew &&
               p.rect.right - pw > e.rect.left + ew &&
               p.rect.top + ph < e.rect.bottom - eh &&
               p.rect.bottom - ph > e.rect.top + eh;
    }
}
