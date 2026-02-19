package com.example.myapplication.engine;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import com.example.myapplication.model.GameData;
import com.example.myapplication.model.GameEntity;

public class GameRenderer {
    private final Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
    private final Paint scorePaint = new Paint();

    public GameRenderer() {
        scorePaint.setColor(Color.BLACK);
        scorePaint.setTextSize(70);
        scorePaint.setFakeBoldText(true);
    }

    public void render(Canvas canvas, GameData data) {
        canvas.drawColor(Color.WHITE);

        // Отрисовка игрока
        if (data.player != null) {
            canvas.drawBitmap(data.player.bitmap, null, data.player.rect, paint);
        }

        // Отрисовка врагов
        synchronized (data.enemies) {
            for (GameEntity enemy : data.enemies) {
                canvas.drawBitmap(enemy.bitmap, null, enemy.rect, paint);
            }
        }

        // Отрисовка счета
        canvas.drawText("Score: " + data.score, 50, 100, scorePaint);
    }
}
