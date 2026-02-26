package com.example.myapplication.model;

import android.graphics.Rect;

public class Bullet {
    public float x, y;
    public final Rect rect;
    public final int width = 10;
    public final int height = 30;

    public Bullet(float x, float y) {
        this.x = x;
        this.y = y;
        this.rect = new Rect((int)x, (int)y, (int)x + width, (int)y + height);
    }

    public void update(float deltaTime) {
        float speed = 3600f;
        y -= speed * deltaTime;
        rect.offsetTo((int)x, (int)y);
    }
}
