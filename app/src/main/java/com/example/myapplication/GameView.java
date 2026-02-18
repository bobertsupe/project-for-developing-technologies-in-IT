package com.example.myapplication;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public class GameView extends SurfaceView implements Runnable {

    private Thread thread;
    private volatile boolean isPlaying;
    private volatile boolean isGameOver = false; // Флаг для однократного срабатывания Game Over
    private final SurfaceHolder surfaceHolder;
    private final Paint paint;
    private final Paint scorePaint;
    private final Activity activity; // Контекст Activity для runOnUiThread

    private final int screenX, screenY;
    private final Rect playerRect;
    private final Bitmap playerBitmap;
    private final Bitmap enemyBitmap;
    private int targetX;

    private final List<Rect> enemies;
    private final int enemySpeed = 15;
    private final Random random;

    private int score = 0;
    private final Consumer<Integer> onGameOverCallback;

    public GameView(Context context, int screenX, int screenY, Consumer<Integer> onGameOverCallback) {
        super(context);
        this.activity = (Activity) context;
        this.screenX = screenX;
        this.screenY = screenY;
        this.surfaceHolder = getHolder();
        this.onGameOverCallback = onGameOverCallback;

        this.paint = new Paint();
        this.scorePaint = new Paint();
        this.scorePaint.setColor(Color.BLACK);
        this.scorePaint.setTextSize(70);
        this.scorePaint.setFakeBoldText(true);

        int entitySize = 120;
        playerBitmap = getBitmapFromVectorDrawable(context, R.drawable.player_svg, entitySize, entitySize);
        enemyBitmap = getBitmapFromVectorDrawable(context, R.drawable.enemy_svg, entitySize, entitySize);

        playerRect = new Rect(screenX / 2 - entitySize / 2, screenY - 250, screenX / 2 + entitySize / 2, screenY - 130);
        targetX = playerRect.centerX();

        enemies = new ArrayList<>();
        random = new Random();
    }

    @Override
    public void run() {
        while (isPlaying) {
            update();
            draw();
            control();
        }
    }

    private void update() {
        if (!isPlaying) return;

        int playerSpeed = 25;
        if (Math.abs(playerRect.centerX() - targetX) > playerSpeed) {
            if (playerRect.centerX() < targetX) playerRect.offset(playerSpeed, 0);
            else playerRect.offset(-playerSpeed, 0);
        }

        synchronized (enemies) {
            if (random.nextInt(100) < 4) {
                int x = random.nextInt(screenX - enemyBitmap.getWidth());
                enemies.add(new Rect(x, -enemyBitmap.getHeight(), x + enemyBitmap.getWidth(), 0));
            }

            List<Rect> toRemove = new ArrayList<>();
            for (Rect enemy : enemies) {
                enemy.offset(0, enemySpeed);

                if (enemy.top > screenY) {
                    toRemove.add(enemy);
                    score++;
                }

                if (Rect.intersects(playerRect, enemy) && !isGameOver) {
                    isGameOver = true; // Срабатываем один раз
                    isPlaying = false; // Останавливаем игровой цикл
                    // Вызываем UI-обновление в главном потоке
                    activity.runOnUiThread(() -> onGameOverCallback.accept(score));
                    return;
                }
            }
            enemies.removeAll(toRemove);
        }
    }

    public void restartGame() {
        score = 0;
        synchronized (enemies) {
            enemies.clear();
        }
        playerRect.offsetTo(screenX / 2 - playerRect.width() / 2, playerRect.top);
        targetX = playerRect.centerX();
        isGameOver = false;

        // Создаем и запускаем новый поток
        resume();
    }

    public void resume() {
        if (thread != null && thread.isAlive()) return;
        isPlaying = true;
        isGameOver = false;
        thread = new Thread(this);
        thread.start();
    }

    public void pause() {
        isPlaying = false;
        try {
            if (thread != null) thread.join(); // Ждем завершения потока
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        thread = null;
    }
    
    // ... остальные методы без изменений ...

    private Bitmap getBitmapFromVectorDrawable(Context context, int drawableId, int width, int height) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (drawable == null) return null;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private void draw() {
        if (surfaceHolder.getSurface().isValid()) {
            Canvas canvas = surfaceHolder.lockCanvas();
            if (canvas == null) return;

            canvas.drawColor(Color.WHITE);
            canvas.drawBitmap(playerBitmap, null, playerRect, paint);

            synchronized (enemies) {
                for (Rect enemy : enemies) {
                    canvas.drawBitmap(enemyBitmap, null, enemy, paint);
                }
            }

            canvas.drawText("Score: " + score, 50, 100, scorePaint);
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    private void control() {
        try {
            Thread.sleep(17);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isGameOver) return false; // Не обрабатываем ввод, если игра окончена
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            targetX = (int) event.getX();
        }
        return true;
    }
}
