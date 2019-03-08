package com.alfredradu.platformer.entities;

public class Coin extends DynamicEntity {

    public Coin(String spriteName, int xpos, int ypos) {
        super(spriteName, xpos, ypos);
        _width = DEFAULT_DIMENSION/2;
        _height = DEFAULT_DIMENSION/2;
        loadBitmap(spriteName,xpos,ypos);
    }

    @Override
    public void update(final double dt) {
        super.update(dt);
    }

    @Override
    public void onCollision(Entity that) {
        Entity.getOverlap(this, that, Entity.overlap);
        _y += Entity.overlap.y;
        if (Entity.overlap.y != 0) {
            _velY = 0;
        }
        if (that instanceof Player){
            //score ++
            //coins left --
            _game.getLevelManager().removeEntity(this);
        }
    }
}
