package com.example.myapplication.engine.systems;

import com.example.myapplication.model.GameEntity;

public class CollisionSystem {
    public boolean isColliding(GameEntity p, GameEntity e) {
        if (p == null || e == null) return false;
        float pMarginW = p.rect.width() * 0.15f;
        float pMarginH = p.rect.height() * 0.15f;
        float eMarginW = e.rect.width() * 0.15f;
        float eMarginH = e.rect.height() * 0.15f;
        
        return p.rect.left + pMarginW < e.rect.right - eMarginW &&
               p.rect.right - pMarginW > e.rect.left + eMarginW &&
               p.rect.top + pMarginH < e.rect.bottom - eMarginH &&
               p.rect.bottom - pMarginH > e.rect.top + eMarginH;
    }
}
