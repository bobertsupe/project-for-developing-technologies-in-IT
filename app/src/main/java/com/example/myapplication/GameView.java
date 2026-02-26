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
        final long targetFrameTime = 1_000_000_000 / 60; // 60 FPS

        while (isPlaying) {
            long currentFrameTime = System.nanoTime();
            float deltaTime = (currentFrameTime - lastFrameTime) / 1_000_000_000f;
            lastFrameTime = currentFrameTime;

            GameData.GameState prevState = gameData.currentState;
            
            // 1. Физика (Логика)
            physics.update(deltaTime, targetX);

            // Оповещение об окончании игры (смерть в обычном режиме или в битве с боссом)
            if ((prevState == GameData.GameState.PLAYING || prevState == GameData.GameState.BOSS_STATE) 
                    && gameData.currentState == GameData.GameState.GAME_OVER) {
                if (onGameOverCallback != null) {
                    onGameOverCallback.accept(gameData.score);
                }
            }

            // 2. Рендеринг (Графика)
            drawFrame();
            
            // Точное ограничение FPS
            long frameDuration = System.nanoTime() - currentFrameTime;
            long sleepTimeMs = (targetFrameTime - frameDuration) / 1_000_000;
            if (sleepTimeMs > 0) {
                try { Thread.sleep(sleepTimeMs); } catch (InterruptedException ignored) {}
            }
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
        physics.resetDifficulty();
        gameData.player.x = getWidth() / 2f - gameData.player.rect.width() / 2f;
        gameData.player.updateRect();
        targetX = (int) gameData.player.rect.centerX();
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
        float x = event.getX();
        float y = event.getY();

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (gameData.currentState == GameData.GameState.MENU) {
                if (gameData.startButtonRect.contains(x, y)) {
                    restartGame();
                }
                return true;
            }
            if (gameData.currentState == GameData.GameState.GAME_OVER) {
                gameData.currentState = GameData.GameState.MENU;
                return true;
            }
        }

        // Разрешаем управление в обычном режиме и во время битвы с боссом
        if (gameData.currentState == GameData.GameState.PLAYING || gameData.currentState == GameData.GameState.BOSS_STATE) {
            if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                targetX = (int) x;
            }
        }
        return true;
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
