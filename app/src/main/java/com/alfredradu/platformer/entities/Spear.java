package com.alfredradu.platformer.entities;

public class Spear extends DynamicEntity {
    public Spear(String spriteName, int xpos, int ypos) {
        super(spriteName, xpos, ypos);
        _width = DEFAULT_DIMENSION;
        _height = DEFAULT_DIMENSION/4f;
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
        if (Entity.overlap.y > 0) {
            _velY = 0;
        }
    }
}
