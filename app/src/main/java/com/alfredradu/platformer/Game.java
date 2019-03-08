package com.alfredradu.platformer;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.alfredradu.platformer.entities.Entity;
import com.alfredradu.platformer.input.InputManager;
import com.alfredradu.platformer.levels.*;
import com.alfredradu.platformer.utils.BitmapPool;

import java.io.IOException;
import java.util.ArrayList;

public class Game extends SurfaceView implements Runnable, SurfaceHolder.Callback {
    public static final String TAG = "Game";
    private Thread _gameThread = null;
    private static Context cont = null;
    private volatile boolean _isRunning = false;
    private SurfaceHolder _holder = null;
    private Paint _paint = new Paint();
    private static final Point _renderPos = new Point();

    private static final int BG_COLOR = Color.rgb(135,206,235);
    private final Matrix _transform = new Matrix();

    private Canvas _canvas = null;
    private Viewport _camera = null;
    private static final float METERS_TO_SHOW_X = 0f; //set the value you want fixed
    private static final float METERS_TO_SHOW_Y = 6f;  //the other is calculated at runtime!

    static int STAGE_WIDTH = 1280;
    static int STAGE_HEIGHT = 720;
    private static final double NANO_TO_SEC = 1.0 / 1000000000;

    private ArrayList<Entity> _visibleEntities = new ArrayList<>();
    public BitmapPool _pool = null;
    private LevelManager _level = null;
    private InputManager _controls = new InputManager();

    private int _lives = 3;
    private int _score = 0;
    private int _coinsRemaining;
    private int _currentLevel = 2;

    public Game(Context context) throws IOException {
        super(context);
        init();
    }
    public Game(Context context, AttributeSet attrs) throws IOException {
        super(context, attrs);
        init();
    }
    public Game(Context context, AttributeSet attrs, int defStyleAttr) throws IOException {
        super(context, attrs, defStyleAttr);
        init();
    }
    public Game(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) throws IOException {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() throws IOException {
        cont = getContext();
        final int TARGET_HEIGHT = 360;
        final int actualHeight = getScreenHeight();
        final float ratio = (TARGET_HEIGHT >= actualHeight) ? 1 : (float) TARGET_HEIGHT / actualHeight;
        STAGE_WIDTH = (int) (ratio * getScreenWidth());
        STAGE_HEIGHT = TARGET_HEIGHT;
        Entity._game = this;
        reset();
        _holder = getHolder();
        _holder.addCallback(this);
        _holder.setFixedSize(STAGE_WIDTH, STAGE_HEIGHT);
        Log.d(TAG, "Resolution: " + STAGE_WIDTH + " : " + STAGE_HEIGHT);
    }

    private void reset() throws IOException{
        _camera = null;
        _camera = new Viewport(STAGE_WIDTH, STAGE_HEIGHT, METERS_TO_SHOW_X, METERS_TO_SHOW_Y);
        Log.d(TAG, _camera.toString());
        _pool = new BitmapPool(this);
        _level = new LevelManager(getLevel(), _pool);
        setCoinsLeft(_level._coinCount);
        _camera.setBounds(new RectF(0f,0f,_level._levelWidth, _level._levelHeight));
    }

    public InputManager getControls(){
        return _controls;
    }
    public void setControls(final InputManager controls){
        _controls.onPause();
        _controls.onStop();
        _controls = controls;
    }

    public static Context getCont(){
        return cont;
    }
    public LevelManager getLevelManager() {return _level; }
    public float getWorldHeight(){ return _level._levelHeight; }
    public float getWorldWidth(){ return _level._levelWidth; }

    public int worldToScreenX(float worldDistance){ return (int) (worldDistance * _camera.getPixelsPerMeterX()); }
    public int worldToScreenY(float worldDistance){ return (int) (worldDistance * _camera.getPixelsPerMeterY()); }

    public float screenToWorldX(float pixelDistance){ return (float) (pixelDistance / _camera.getPixelsPerMeterX()); }
    public float screenToWorldY(float pixelDistance){ return (float) (pixelDistance / _camera.getPixelsPerMeterY()); }

    public static int getScreenWidth() { return Resources.getSystem().getDisplayMetrics().widthPixels; }
    public static int getScreenHeight() { return Resources.getSystem().getDisplayMetrics().heightPixels; }

    public void addLife(){ _lives++; }
    public void removeLife(){ _lives--; }
    public void addScore(){ _score++; }
    public void removeScore(){ _score--; }

    public int getLife(){ return _lives; }
    public int getScore(){ return _score; }
    public int getCoinsLeft(){ return _coinsRemaining; }
    public void setLife(int life){ _lives = life; }
    public void setScore(int score){ _score = score; }
    public void setCoinsLeft(int coinsRemaining){ _coinsRemaining = coinsRemaining; }
    public void updateCoins(int val){
        _coinsRemaining += val;
    }

    public void checkGameStatus() throws IOException {
        if (_lives < 1){
            //TODO: GAME OVER - restart current stage
            Log.d(TAG, "Game over!");
        }
        if (_coinsRemaining < 1){
            //TODO: STAGE CLEAR - load next stage - keep score
            Log.d(TAG, "Level completed!");
            reset();
        }
    }

    public LevelData getLevel() throws IOException {
        switch (_currentLevel){
            case 0:
                nextLevel();
                return new Level1();
            case 1:
                nextLevel();
                return new Level2();
            case 2:
                nextLevel();
                return new Level3();
            default:
                return null;
        }
    }

    private void nextLevel(){
        _currentLevel++;
    }

    @Override
    public void run() {
        long lastFrame = System.nanoTime();
        while (_isRunning) {
            final double deltaTime = (System.nanoTime()-lastFrame) * NANO_TO_SEC;
            lastFrame = System.nanoTime();
            update(deltaTime);
            buildVisibleSet();
            render(_camera, _visibleEntities);
            try {
                checkGameStatus();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void update(final double dt) {
        _camera.lookAt(_level._player);
        _level.update(dt);
    }

    private void buildVisibleSet(){
        _visibleEntities.clear();
        for (final Entity e : _level._entities){
            if(_camera.inView(e)){
                _visibleEntities.add(e);
            }
        }
    }

    private void render(final Viewport camera, final ArrayList<Entity> visibleEntities) {
        if (!lockCanvas()) {
            return;
        }
        try {
            _canvas.drawColor(BG_COLOR);
            for (final Entity e : visibleEntities) {
                _transform.reset();
                camera.worldToScreen(e, _renderPos);
                _transform.postTranslate(_renderPos.x, _renderPos.y);
                e.render(_canvas, _transform, _paint);
            }
        } finally {
            _holder.unlockCanvasAndPost(_canvas);
        }
    }

    private boolean lockCanvas() {
        if (!_holder.getSurface().isValid()) {
            return false;
        }
        _canvas = _holder.lockCanvas();
        return (_canvas != null);

    }

    //execute on ui thread below here

    protected void onResume() {
        Log.d(TAG, "onResume");
        _isRunning = true;
        _controls.onResume();
        _gameThread = new Thread(this);
    }

    protected void onPause() {
        Log.d(TAG, "onPause");
        _isRunning = false;
        _controls.onPause();
        while(true) { //_gameThread.getState() != Thread.State.TERMINATED
            try {
                _gameThread.join();
            } catch (InterruptedException e) {
                Log.d(TAG, Log.getStackTraceString(e.getCause()));
            }
        }
    }

    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        _gameThread = null;
        cont = null;
        _lives = 3;
        _score = 0;
        _currentLevel = 0;
        if (_level != null){
            _level.destroy();
            _level = null;
        }
        _controls = null;
        Entity._game = null;
        if(_pool != null){
            _pool.empty();
        }
        _holder.removeCallback(this);
    }

    @Override
    public void surfaceCreated(final SurfaceHolder holder) {
        Log.d(TAG,"Surface created!");
    }

    @Override
    public void surfaceChanged(final SurfaceHolder holder, final int format, final int width, final int height) {
        Log.d(TAG,"Surface changed!");
        Log.d(TAG,"\t Width: " + width + " Height: " + height);
        if(_gameThread != null && _isRunning){
           _gameThread.start();
           Log.d(TAG, "GameThread started!");
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG,"Surface destroyed!");
    }
}
