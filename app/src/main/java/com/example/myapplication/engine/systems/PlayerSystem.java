package com.example.myapplication.engine.systems;

import com.example.myapplication.model.GameData;

public class PlayerSystem {
    public void update(GameData data, float deltaTime, int targetX, float playerSpeed) {
        if (data.player == null) return;
        
        float playerCenterX = data.player.rect.centerX();
        float deltaX = targetX - playerCenterX;
        
        if (Math.abs(deltaX) > 10) {
            float move = playerSpeed * deltaTime;
            if (Math.abs(deltaX) < move) {
                data.player.x = targetX - data.player.rect.width() / 2f;
            } else {
                data.player.x += Math.signum(deltaX) * move;
            }
            data.player.updateRect();
        }
    }
}
