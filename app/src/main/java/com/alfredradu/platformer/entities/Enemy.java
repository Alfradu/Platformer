package com.alfredradu.platformer.entities;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;

public class Enemy extends DynamicEntity {
    static final String TAG = "Enemy";
    private static String _sprite1 = "";
    private static String _sprite2 = "";
    private String _loadedSprite = "";
    private float ENEMY_RUN_SPEED = -1.0f; // m/s
    private final int LEFT = 1;
    private final int RIGHT = -1;
    private int _facing = LEFT; //initial facing dir
    private int _steps = 40;

    public Enemy(final String spriteName, final int xpos, final int ypos) {
        super(spriteName, xpos, ypos);
        _sprite1 = spriteName;
        _sprite2 = spriteName+"2";
        _width = DEFAULT_DIMENSION/1.5f;
        _height = DEFAULT_DIMENSION;
        loadBitmap(spriteName,xpos,ypos);
    }

    @Override
    public void render(Canvas canvas, Matrix transform, Paint paint) {
        transform.preScale(_facing, 1.0f);
        if(_facing == RIGHT){
            final float offset = _game.worldToScreenX(_width);
            transform.postTranslate(offset, 0);
        }
        super.render(canvas, transform, paint);
    }

    @Override
    public void update(final double dt) {
        _velX = ENEMY_RUN_SPEED;
        walk();
        super.update(dt);
    }

    private void updateFacingDirection(final float controlDirection){
        if(controlDirection < 0){ _facing = LEFT; }
        else if(controlDirection > 0){ _facing = RIGHT; }
    }

    private void walk(){
        _steps++;
        if (_steps%20==0) {
            if (_loadedSprite.equals(_sprite1)) {
                loadBitmap(_sprite2, (int) _x, (int) _y);
                _loadedSprite = _sprite2;
            } else {
                loadBitmap(_sprite1, (int) _x, (int) _y);
                _loadedSprite = _sprite1;
            }
        }
        if (_steps < 1){
            _steps = 40;
        }
    }

    @Override
    public void onCollision(Entity that) {
        Entity.getOverlap(this, that, Entity.overlap);
        _x += Entity.overlap.x;
        _y += Entity.overlap.y;
        if (Entity.overlap.x != 0f && that._y < _y){
            ENEMY_RUN_SPEED *= -1f;
            updateFacingDirection(ENEMY_RUN_SPEED);
        }
        if (Entity.overlap.y != 0){
            _velY = 0;
        }
    }
}
