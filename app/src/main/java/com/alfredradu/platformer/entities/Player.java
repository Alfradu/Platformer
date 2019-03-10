package com.alfredradu.platformer.entities;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import com.alfredradu.platformer.Game;
import com.alfredradu.platformer.input.InputManager;

public class Player extends DynamicEntity {
    private static final String TAG = "Player";
    private static final String _flash1 = "player_flash";
    private static final String _flash2 = "player_flash2";
    private static String _sprite1 = "";
    private static String _sprite2 = "";
    private String _loadedSprite = "";
    private String _playerSprite;
    private static final float PLAYER_RUN_SPEED = 6.0f; // m/s
    private static final float PLAYER_JUMP_FORCE = -(GRAVITY/2.3f);
    private static final float MIN_INPUT_TO_TURN = 0.05f; //5% joy input before we start turning animations
    private static final int TIMER = 80;
    private final int LEFT = 1;
    private final int RIGHT = -1;
    private int _facing = RIGHT; //initial facing dir
    private boolean _invincible = false;
    private static int _resetCounter = 0;
    private int _steps = 2;

    public Player (final String spriteName, final int xpos, final int ypos){
        super(spriteName, xpos, ypos);
        _sprite1 = spriteName;
        _sprite2 = spriteName+"2";
        _width = DEFAULT_DIMENSION/1.5f; //set different values for player size
        _height = DEFAULT_DIMENSION;
        _playerSprite = spriteName;
        loadBitmap(_playerSprite,xpos,ypos);
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
        final InputManager controls = _game.getControls();
        final float direction = controls._horizontalFactor;
        _velX = direction * PLAYER_RUN_SPEED;
        if (_velX != 0 && _isOnGround){
            walk();
        }
        updateFacingDirection(direction);
        if(controls._isJumping && _isOnGround){
            _game.onGameEvent(Game.GameEvent.Jump, this);
            _velY = PLAYER_JUMP_FORCE;
            _isOnGround = false;
        }
        if (_invincible) {
            _resetCounter--;
            if (_resetCounter%10==0){
                flash();
            }
            if (_resetCounter < 0) {
                _invincible = false;
                _resetCounter = 0;
                loadBitmap(_playerSprite,(int)_x,(int)_y);
            }
        }
        super.update(dt);
    }

    private void updateFacingDirection(final float controlDirection){
        if(Math.abs(controlDirection) < MIN_INPUT_TO_TURN){ return; }
        if(controlDirection < 0){ _facing = LEFT; }
        else if(controlDirection > 0){ _facing = RIGHT; }
    }

    @Override
    public void onCollision(Entity that) {
        Entity.getOverlap(this, that, Entity.overlap);
        _x += Entity.overlap.x;
        _y += Entity.overlap.y;
        if (Entity.overlap.y != 0){
            _velY = 0;
            if (Entity.overlap.y < 0){
                _isOnGround = true;
            }
        }
        if (that instanceof Enemy || that instanceof Spear){
            if (!_invincible){
                knockBack();
                _game.removeLife();
                _resetCounter = TIMER;
            }
        }
    }

    private void knockBack(){
        _game.onGameEvent(Game.GameEvent.Hurt, this);
        _invincible = true;
        _velX += _facing*2;
        _velY += PLAYER_JUMP_FORCE/1.5f;
    }

    private void flash(){
        if (_resetCounter%20==0){
            loadBitmap(_flash1,(int)_x,(int)_y);
        } else {
            loadBitmap(_flash2,(int)_x,(int)_y);
        }
    }

    private void walk(){
        _steps++;
        if (_steps%10==0) {
            if (_loadedSprite.equals(_sprite1)) {
                loadBitmap(_sprite2, (int) _x, (int) _y);
                _loadedSprite = _sprite2;
            } else {
                loadBitmap(_sprite1, (int) _x, (int) _y);
                _loadedSprite = _sprite1;
            }
        }
        if (_steps < 1){
            _steps = 20;
        }
    }
}
