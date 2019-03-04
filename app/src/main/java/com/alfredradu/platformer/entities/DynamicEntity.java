package com.alfredradu.platformer.entities;

import com.alfredradu.platformer.utils.Utils;

public class DynamicEntity extends StaticEntity {
    static final float GRAVITY = 40f;
    private static final float MAX_DELTA = 0.48f;
    public float _velX = 0;
    public float _velY = 0;
    public float _gravity = GRAVITY;
    boolean _isOnGround = false;

    public DynamicEntity(String spriteName, int xpos, int ypos){
        super(spriteName, xpos, ypos);
    }

    @Override
    public void update(double dt) {
        _x += Utils.clamp((float)(_velX * dt),-MAX_DELTA, MAX_DELTA);

        if (!_isOnGround){
            final float gravityThisTick = (float) (_gravity * dt);
            _velY += gravityThisTick;
        }
        _y += Utils.clamp((float)(_velY * dt),-MAX_DELTA, MAX_DELTA);
        if(_y > _game.getWorldHeight()){
            _y = 0f;
        }
        _isOnGround = false;
    }

    @Override
    public void onCollision(Entity that) {
        Entity.getOverlap(this, that, Entity.overlap);
        _x += Entity.overlap.x;
        _y += Entity.overlap.y;
        if (Entity.overlap.y != 0){
            _velY = 0;
            if (Entity.overlap.y < 0){
                //overlapy.y < 0 = hit feet
                _isOnGround = true;
            } else {
                //overlapy.y > 0 = hit head
            }
        }
    }
}
