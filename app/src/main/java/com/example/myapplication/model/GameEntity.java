package com.example.myapplication.model;

import android.graphics.Bitmap;
import android.graphics.Rect;

/**
 * Базовый объект игры с поддержкой дробных координат для плавности.
 */
public class GameEntity {
    public float x, y;
    public final Bitmap bitmap;
    public final Rect rect;

    public GameEntity(Bitmap bitmap, float x, float y) {
        this.bitmap = bitmap;
        this.x = x;
        this.y = y;
        this.rect = new Rect((int) x, (int) y, (int) x + bitmap.getWidth(), (int) y + bitmap.getHeight());
    }

    public void set(float x, float y) {
        this.x = x;
        this.y = y;
        updateRect();
    }

    public void updateRect() {
        rect.offsetTo((int) x, (int) y);
    }
}
