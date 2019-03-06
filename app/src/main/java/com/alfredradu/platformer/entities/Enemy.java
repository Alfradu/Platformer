package com.alfredradu.platformer.entities;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

public class Enemy extends DynamicEntity {
    static final String TAG = "Player";
    float ENEMY_RUN_SPEED = -1.0f; // m/s
    private final int LEFT = 1;
    private final int RIGHT = -1;
    private int _facing = LEFT; //initial facing dir

    public Enemy(final String spriteName, final int xpos, final int ypos) {
        super(spriteName, xpos, ypos);
        _width = DEFAULT_DIMENSION/2;
        _height = DEFAULT_DIMENSION/2;
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
        super.update(dt);
    }

    private void updateFacingDirection(final float controlDirection){
        if(controlDirection < 0){ _facing = LEFT; }
        else if(controlDirection > 0){ _facing = RIGHT; }
    }

    @Override
    public void onCollision(Entity that) {
        Entity.getOverlap(this, that, Entity.overlap);
        _x += Entity.overlap.x;
        if (Entity.overlap.x != 0){
            ENEMY_RUN_SPEED *= -1f;
            updateFacingDirection(ENEMY_RUN_SPEED);
        }
    }
}
