package com.example.myapplication;

import android.graphics.Point;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private GameView gameView;
    private LinearLayout gameOverLayout;
    private TextView finalScoreText;
    private Button restartButton;

    private int lastScoreForFirebase = 0; // Переменная для будущего использования с Firebase

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Убираем заголовок и делаем приложение полноэкранным
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        FrameLayout gameContainer = findViewById(R.id.game_container);
        gameOverLayout = findViewById(R.id.game_over_layout);
        finalScoreText = findViewById(R.id.final_score_text);
        restartButton = findViewById(R.id.restart_button);

        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);

        gameView = new GameView(this, size.x, size.y, this::showGameOverScreen);
        gameContainer.addView(gameView);

        restartButton.setOnClickListener(v -> restartGame());
    }

    public void showGameOverScreen(int finalScore) {
        lastScoreForFirebase = finalScore;
        runOnUiThread(() -> {
            finalScoreText.setText("Score: " + finalScore);
            gameOverLayout.setVisibility(View.VISIBLE);
        });
    }

    private void restartGame() {
        gameOverLayout.setVisibility(View.GONE);
        gameView.restartGame();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gameView != null) {
            gameView.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameView != null) {
            gameView.pause();
        }
    }
}
