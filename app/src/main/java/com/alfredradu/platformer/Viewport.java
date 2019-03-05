package com.alfredradu.platformer;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;

import com.alfredradu.platformer.entities.Entity;

import java.util.Locale;

public class Viewport {
    private final PointF _lookAt = new PointF(0f,0f);
    private float lerp = 0.1f;
    private int _pixelsPerMeterX; //viewport "density"
    private int _pixelsPerMeterY;
    private int _screenWidth; //resolution
    private int _screenHeight;
    private int _screenCenterY; //center screen
    private int _screenCenterX;
    private float _metersToShowX; //Field of View
    private float _metersToShowY;
    private float _halfDistX; //cached value (0.5*FOV)
    private float _halfDistY;
    private RectF _bounds;

    private final static float BUFFER = 2f; //overdraw, to avoid visual gaps

    public Viewport(final int screenWidth, final int screenHeight, final float metersToShowX, final float metersToShowY){
        _screenWidth = screenWidth;
        _screenHeight = screenHeight;
        _screenCenterX = _screenWidth / 2;
        _screenCenterY = _screenHeight / 2;
        setMetersToShow(metersToShowX, metersToShowY);
        _lookAt.x = _halfDistX;
        _lookAt.y = 0.0f;

    }

    @Override
    public String toString(){
        return String.format(Locale.getDefault(), "Viewport[%dpx, %dpx / %.1fm, %.1fm]", _screenWidth, _screenHeight, _metersToShowX, _metersToShowY);
    }
    //setMetersToShow calculates the number of physical pixels per meters
    //so that we can translate our game world (meters) to the screen (pixels)
    //provide the dimension(s) you want to lock. The viewport will automatically
    // size the other axis to fill the screen perfectly.
    private void setMetersToShow(float metersToShowX, float metersToShowY){
        if (metersToShowX <= 0f && metersToShowY <= 0f) throw new IllegalArgumentException("One of the dimensions must be provided!");
        //formula: new height = (original height / original width) x new width
        _metersToShowX = metersToShowX;
        _metersToShowY = metersToShowY;
        if(metersToShowX == 0f || metersToShowY == 0f){
            if(metersToShowY > 0f) { //if Y is configured, calculate X
                _metersToShowX = ((float) _screenWidth / _screenHeight) * metersToShowY;
            }else { //if X is configured, calculate Y
                _metersToShowY = ((float) _screenHeight / _screenWidth) * metersToShowX;
            }
        }
        _halfDistX = (_metersToShowX +BUFFER) * 0.5f;
        _halfDistY = (_metersToShowY +BUFFER) * 0.5f;
        _pixelsPerMeterX = (int)(_screenWidth / _metersToShowX);
        _pixelsPerMeterY = (int)(_screenHeight / _metersToShowY);
    }

    public float getHorizontalView(){
        return _metersToShowX;
    }
    public float getVerticalView(){
        return _metersToShowY;
    }
    public int getScreenWidth() {
        return _screenWidth;
    }
    public int getScreenHeight(){
        return _screenHeight;
    }
    public int getPixelsPerMeterX(){
        return _pixelsPerMeterX;
    }
    public int getPixelsPerMeterY(){
        return _pixelsPerMeterY;
    }

    public void lookAt(final float x, final float y){ //TODO: clean up bounds
        if (inViewLeft(_bounds.left)){
            if (x > _halfDistX){
                _lookAt.x += (x - _lookAt.x) * lerp;
            }
        }
        else if (inViewRight(_bounds.right)){
            if (x < _bounds.right- _halfDistX){
                _lookAt.x += (x - _lookAt.x) * lerp;
            }
        } else {
            _lookAt.x += (x - _lookAt.x) * lerp;
        }
        _lookAt.y = y;
    }
    public void lookAt(final Entity obj){
        lookAt(obj.centerX(), obj.centerY());
    }
    public void lookAt(final PointF pos){
        lookAt(pos.x, pos.y);
    }

    public void worldToScreen(final float worldPosX, final float worldPosY, Point screenPos){
        screenPos.x = (int) (_screenCenterX - ((_lookAt.x - worldPosX) * _pixelsPerMeterX));
        screenPos.y = (int) (_screenCenterY - ((_lookAt.y - worldPosY) * _pixelsPerMeterY));
    }
    public void worldToScreen(final PointF worldPos, Point screenPos){
        worldToScreen(worldPos.x, worldPos.y, screenPos);
    }
    public void worldToScreen(final Entity e, final Point screenPos){
        worldToScreen(e._x, e._y, screenPos);
    }
    public boolean inView(final Entity e) {
        final float maxX = (_lookAt.x + _halfDistX);
        final float minX = (_lookAt.x - _halfDistX)-e._width;
        final float maxY = (_lookAt.y + _halfDistY);
        final float minY  = (_lookAt.y - _halfDistY)-e._height;
        return (e._x > minX && e._x < maxX)
                && (e._y > minY && e._y < maxY);
    }

    public boolean inView(final RectF bounds) {
        final float right = (_lookAt.x + _halfDistX);
        final float left = (_lookAt.x - _halfDistX);
        final float bottom = (_lookAt.y + _halfDistY);
        final float top  = (_lookAt.y - _halfDistY);
        return (bounds.left < right && bounds.right > left)
                && (bounds.top < bottom && bounds.bottom > top);
    }

    public boolean inViewLeft(final float bound){
            return (bound > _lookAt.x- _halfDistX);
    }

    public boolean inViewRight(final float bound){
        return (bound < _lookAt.x+ _halfDistX);
    }

    public void setBounds(final RectF bounds){
        _bounds = bounds;
    }
    public RectF getBounds(){
        return _bounds;
    }
}
