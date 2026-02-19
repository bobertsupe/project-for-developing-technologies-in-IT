package com.example.myapplication;

import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private GameView gameView;
    private LinearLayout gameOverLayout;
    private TextView finalScoreText;
    private Button restartButton;
    private Button recordsButton;

    private static final String PREFS_NAME = "GameRecords";
    private static final String KEY_RECORDS = "records_list";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        FrameLayout gameContainer = findViewById(R.id.game_container);
        gameOverLayout = findViewById(R.id.game_over_layout);
        finalScoreText = findViewById(R.id.final_score_text);
        restartButton = findViewById(R.id.restart_button);
        recordsButton = findViewById(R.id.records_button);

        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);

        gameView = new GameView(this, size.x, size.y, this::showGameOverScreen);
        gameContainer.addView(gameView, 0);

        restartButton.setOnClickListener(v -> restartGame());
        recordsButton.setOnClickListener(v -> showRecordsDialog());
    }

    public void showGameOverScreen(int finalScore) {
        saveRecord(finalScore);
        runOnUiThread(() -> {
            finalScoreText.setText("Score: " + finalScore);
            gameOverLayout.setVisibility(View.VISIBLE);
            gameOverLayout.bringToFront();
        });
    }

    private void saveRecord(int score) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Set<String> recordsSet = prefs.getStringSet(KEY_RECORDS, new HashSet<>());
        // Копируем во временный список, так как getStringSet возвращает read-only или нестабильный сет
        Set<String> newRecords = new HashSet<>(recordsSet);
        newRecords.add(String.valueOf(score));
        
        prefs.edit().putStringSet(KEY_RECORDS, newRecords).apply();
    }

    private void showRecordsDialog() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Set<String> recordsSet = prefs.getStringSet(KEY_RECORDS, new HashSet<>());
        
        List<Integer> recordsList = new ArrayList<>();
        for (String s : recordsSet) {
            recordsList.add(Integer.parseInt(s));
        }
        
        // Сортируем по убыванию
        Collections.sort(recordsList, Collections.reverseOrder());

        StringBuilder sb = new StringBuilder();
        if (recordsList.isEmpty()) {
            sb.append("Рекордов пока нет");
        } else {
            for (int i = 0; i < Math.min(recordsList.size(), 10); i++) {
                sb.append(i + 1).append(". ").append(recordsList.get(i)).append("\n");
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Топ 10 рекордов")
                .setMessage(sb.toString())
                .setPositiveButton("OK", null)
                .show();
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
