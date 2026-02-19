package com.example.myapplication;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.core.content.ContextCompat;

import com.example.myapplication.engine.GamePhysics;
import com.example.myapplication.engine.GameRenderer;
import com.example.myapplication.model.GameData;
import com.example.myapplication.model.GameEntity;

import java.util.function.Consumer;

public class GameView extends SurfaceView implements Runnable {

    private Thread thread;
    private volatile boolean isPlaying;
    private final SurfaceHolder surfaceHolder;
    private final Activity activity;

    // Модули архитектуры
    private final GameData gameData;
    private final GamePhysics physics;
    private final GameRenderer renderer;

    private int targetX;
    private final Consumer<Integer> onGameOverCallback;

    public GameView(Context context, int screenX, int screenY, Consumer<Integer> onGameOverCallback) {
        super(context);
        this.activity = (Activity) context;
        this.surfaceHolder = getHolder();
        this.onGameOverCallback = onGameOverCallback;

        // Инициализация моделей
        this.gameData = new GameData();
        int entitySize = 140;
        Bitmap playerBitmap = getBitmapFromVectorDrawable(context, R.drawable.player_svg, entitySize, entitySize);
        Bitmap enemyBitmap = getBitmapFromVectorDrawable(context, R.drawable.enemy_svg, entitySize, entitySize);

        float startX = screenX / 2f - entitySize / 2f;
        gameData.player = new GameEntity(playerBitmap, startX, screenY - 350);
        targetX = (int) (startX + entitySize / 2f);

        // Инициализация движков
        this.physics = new GamePhysics(gameData, screenX, screenY, enemyBitmap);
        this.renderer = new GameRenderer();
    }

    @Override
    public void run() {
        long lastFrameTime = System.nanoTime();
        while (isPlaying) {
            long currentFrameTime = System.nanoTime();
            float deltaTime = (currentFrameTime - lastFrameTime) / 1_000_000_000f;
            lastFrameTime = currentFrameTime;

            // 1. Физика (Логика)
            physics.update(deltaTime, targetX);

            // 2. Проверка Game Over
            if (gameData.isGameOver) {
                isPlaying = false;
                activity.runOnUiThread(() -> onGameOverCallback.accept(gameData.score));
            }

            // 3. Рендеринг (Графика)
            drawFrame();
            
            limitFPS();
        }
    }

    private void drawFrame() {
        if (!surfaceHolder.getSurface().isValid()) return;
        Canvas canvas = surfaceHolder.lockCanvas();
        if (canvas == null) return;
        try {
            renderer.render(canvas, gameData);
        } finally {
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    public void restartGame() {
        gameData.reset();
        gameData.player.x = getWidth() / 2f - gameData.player.rect.width() / 2f;
        gameData.player.updateRect();
        targetX = (int) gameData.player.rect.centerX();
        resume();
    }

    public void resume() {
        if (thread != null && thread.isAlive()) return;
        isPlaying = true;
        thread = new Thread(this);
        thread.start();
    }

    public void pause() {
        isPlaying = false;
        try { if (thread != null) thread.join(); } catch (InterruptedException e) {}
        thread = null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gameData.isGameOver) return false;
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            targetX = (int) event.getX();
        }
        return true;
    }

    private void limitFPS() {
        try { Thread.sleep(10); } catch (InterruptedException ignored) {}
    }

    private Bitmap getBitmapFromVectorDrawable(Context context, int drawableId, int width, int height) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (drawable == null) return null;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
