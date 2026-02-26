package com.example.myapplication.engine;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import com.example.myapplication.model.Bullet;
import com.example.myapplication.model.GameData;
import com.example.myapplication.model.GameEntity;

public class GameRenderer {
    private final Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
    private final Paint textPaint = new Paint();
    private final Paint buttonPaint = new Paint();
    private final Paint hpBarPaint = new Paint();
    private final Paint bulletPaint = new Paint();

    public GameRenderer() {
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(70);
        textPaint.setFakeBoldText(true);
        textPaint.setTextAlign(Paint.Align.CENTER);

        buttonPaint.setColor(Color.GREEN);
        buttonPaint.setStyle(Paint.Style.FILL);

        hpBarPaint.setStyle(Paint.Style.FILL);
        
        bulletPaint.setColor(Color.RED);
        bulletPaint.setStyle(Paint.Style.FILL);
    }

    public void render(Canvas canvas, GameData data) {
        canvas.drawColor(Color.WHITE);

        if (data.currentState == GameData.GameState.MENU) {
            drawMenu(canvas, data);
        } else if (data.currentState == GameData.GameState.PLAYING || 
                   data.currentState == GameData.GameState.BOSS_STATE || 
                   data.currentState == GameData.GameState.GAME_OVER) {
            drawGame(canvas, data);
            if (data.currentState == GameData.GameState.PLAYING) {
                drawLevelUp(canvas, data);
            }
        }
    }

    private void drawLevelUp(Canvas canvas, GameData data) {
        if (data.levelMessage != null && !data.levelMessage.isEmpty()) {
            if (System.currentTimeMillis() < data.levelMessageEndTime) {
                textPaint.setColor(Color.RED);
                textPaint.setTextAlign(Paint.Align.CENTER);
                textPaint.setTextSize(100);
                canvas.drawText(data.levelMessage, canvas.getWidth() / 2f, canvas.getHeight() / 2f - 300, textPaint);
                textPaint.setTextSize(70);
            }
        }
    }

    private void drawMenu(Canvas canvas, GameData data) {
        float centerX = canvas.getWidth() / 2f;
        float centerY = canvas.getHeight() / 2f;

        float btnWidth = 400;
        float btnHeight = 150;
        data.startButtonRect.set(centerX - btnWidth/2, centerY - btnHeight/2, 
                                centerX + btnWidth/2, centerY + btnHeight/2);
        
        canvas.drawRect(data.startButtonRect, buttonPaint);
        
        textPaint.setColor(Color.WHITE);
        canvas.drawText("START", centerX, centerY + 25, textPaint);
        
        textPaint.setColor(Color.BLACK);
        canvas.drawText("Main Menu", centerX, centerY - 200, textPaint);
    }

    private void drawGame(Canvas canvas, GameData data) {
        // Игрок
        if (data.player != null) {
            canvas.drawBitmap(data.player.bitmap, null, data.player.rect, paint);
        }

        // Враги
        synchronized (data.enemies) {
            for (GameEntity enemy : data.enemies) {
                canvas.drawBitmap(enemy.bitmap, null, enemy.rect, paint);
            }
        }

        // Босс
        if (data.currentState == GameData.GameState.BOSS_STATE && data.boss != null) {
            canvas.drawBitmap(data.boss.bitmap, null, data.boss.rect, paint);
            drawBossHP(canvas, data);
        }

        // Пули
        for (Bullet bullet : data.bullets) {
            canvas.drawRect(bullet.rect, bulletPaint);
        }

        // Интерфейс
        textPaint.setColor(Color.BLACK);
        textPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("Score: " + data.score, 50, 100, textPaint);
        if (data.currentState != GameData.GameState.BOSS_STATE) {
            canvas.drawText("Level: " + data.currentLevel, 50, 180, textPaint);
        } else {
            textPaint.setColor(Color.RED);
            canvas.drawText("BOSS BATTLE!", 50, 180, textPaint);
        }
    }

    private void drawBossHP(Canvas canvas, GameData data) {
        float barWidth = data.boss.rect.width();
        float barHeight = 20;
        float x = data.boss.rect.left;
        float y = data.boss.rect.top - 40;

        // Фон полоски (серый)
        hpBarPaint.setColor(Color.LTGRAY);
        canvas.drawRect(x, y, x + barWidth, y + barHeight, hpBarPaint);

        // Текущее HP (красный)
        hpBarPaint.setColor(Color.RED);
        float hpProgress = (float) data.bossHP / 20f;
        canvas.drawRect(x, y, x + (barWidth * hpProgress), y + barHeight, hpBarPaint);
    }
}
