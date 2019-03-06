package com.alfredradu.platformer.entities;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;

import com.alfredradu.platformer.Game;

public abstract class Entity {
    static final String TAG = "Entity";
    static final float DEFAULT_DIMENSION = 1.0f; //in meters

    public static Game _game = null; //shared ref, managed by the Game-class!
    public float _x = 0;
    public float _y = 0;
    public float _width = DEFAULT_DIMENSION;
    public float _height = DEFAULT_DIMENSION;

    public void update(final double dt) { }
    public void render(final Canvas canvas, final Matrix transform, final Paint paint) {}
    public void onCollision(final Entity that) {}
    public void destroy() {}

    public float left() {
        return _x;
    }
    public float right() {
        return _x + _width;
    }
    public float top() {
        return _y;
    }
    public float bottom() {
        return _y + _height;
    }
    public float centerX() {
        return _x + (_width * 0.5f);
    }
    public float centerY() {
        return _y + (_height * 0.5f);
    }

    public boolean isDynamic(){ return false;}
    public void setLeft(final float leftEdgePosition) {
        _x = leftEdgePosition;
    }
    public void setRight(final float rightEdgePosition) {
        _x = rightEdgePosition - _width;
    }
    public void setTop(final float topEdgePosition) {
        _y = topEdgePosition;
    }
    public void setBottom(final float bottomEdgePosition) {
        _y = bottomEdgePosition - _height;
    }
    public void setCenter(final float x, final float y) {
        _x = x - (_width * 0.5f);
        _y = y - (_height * 0.5f);
    }

    public boolean isColliding(final Entity that) {
        if (this == that) {
            throw new AssertionError("isColliding: You shouldn't test Entities against themselves!");
        }
        return Entity.isAABBOverlapping(this, that);
    }

    //Some good reading on bounding-box intersection tests:
    //https://gamedev.stackexchange.com/questions/586/what-is-the-fastest-way-to-work-out-2d-bounding-box-intersection
    static boolean isAABBOverlapping( final Entity a, final Entity b) {
        return !(a.right() <= b.left()
                || b.right() <= a.left()
                || a.bottom() <= b.top()
                || b.bottom() <= a.top());
    }

    //SAT intersection test. http://www.metanetsoftware.com/technique/tutorialA.html
    //returns true on intersection, and sets the least intersecting axis in overlap
    static final PointF overlap = new PointF( 0 , 0 ); //Q&D PointF pool for collision detection. Assumes single threading.
    @SuppressWarnings ( "UnusedReturnValue" )
    static boolean getOverlap( final Entity a, final Entity b, final PointF overlap) {
        overlap.x = 0.0f;
        overlap.y = 0.0f;
        final float centerDeltaX = a.centerX() - b.centerX();
        final float halfWidths = (a._width + b._width) * 0.5f;
        float dx = Math.abs(centerDeltaX); //cache the abs, we need it twice

        if (dx > halfWidths) return false ; //no overlap on x == no collision

        final float centerDeltaY = a.centerY() - b.centerY();
        final float halfHeights = (a._height + b._height) * 0.5f;
        float dy = Math.abs(centerDeltaY);

        if (dy > halfHeights) return false ; //no overlap on y == no collision

        dx = halfWidths - dx; //overlap on x
        dy = halfHeights - dy; //overlap on y
        if (dy < dx) {
            overlap.y = (centerDeltaY < 0 ) ? -dy : dy;
        } else if (dy > dx) {
            overlap.x = (centerDeltaX < 0 ) ? -dx : dx;
        } else {
            overlap.x = (centerDeltaX < 0 ) ? -dx : dx;
            overlap.y = (centerDeltaY < 0 ) ? -dy : dy;
        }
        return true ;
    }

}