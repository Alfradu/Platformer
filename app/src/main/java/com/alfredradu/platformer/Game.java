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
import android.view.MotionEvent;
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
    static final float textSize = 24f;
    private final Matrix _transform = new Matrix();

    private Canvas _canvas = null;
    private Viewport _camera = null;
    private static final float METERS_TO_SHOW_X = 0f;
    private static final float METERS_TO_SHOW_Y = 6f;

    static int STAGE_WIDTH = 1280;
    static int STAGE_HEIGHT = 720;
    static float  _halfWidth = 0;
    static float _halfHeight = 0;
    private static final double NANO_TO_SEC = 1.0 / 1000000000;

    private ArrayList<Entity> _visibleEntities = new ArrayList<>();
    public BitmapPool _pool = null;
    private LevelManager _level = null;
    private LevelData test = null;
    private InputManager _controls = new InputManager();

    private final int STARTING_LIVES = 3;
    private int _lives = STARTING_LIVES;
    private int _score = 0;
    private int _totalScore;
    private static int _coinsRemaining;
    private int _currentLevel = 0;
    private boolean _gameOver = false;
    private boolean _levelComplete = false;
    private boolean _onLastLevel = false;
    private boolean _reset = false;

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
        _halfWidth = (float)STAGE_WIDTH/2;
        _halfHeight = (float)STAGE_HEIGHT/2;
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
        if (!_onLastLevel){
            _level = new LevelManager(getLevel(), _pool);
            setCoinsLeft(_level._coinCount);
            _camera.setBounds(new RectF(0f,0f,_level._levelWidth, _level._levelHeight));
            if (_gameOver){
                setLife(STARTING_LIVES);
                setScore(_totalScore);
                _gameOver = false;
            }
            if (_levelComplete){
                _totalScore = _score;
                setLife(STARTING_LIVES);
                _levelComplete = false;
            }
            _reset = true;
        } else {
            //TODO: load menu
        }
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

    public void playMusic(){

    }
    public void playSound(){

    }

    public void checkGameStatus(){
        if (_lives < 1){
            Log.d(TAG, "Game over!");
            _gameOver = true;
        }
        if (_coinsRemaining < 1){
            Log.d(TAG, "Level completed!");
            _levelComplete = true;
        }
    }

    public LevelData getLevel() throws IOException {
        if (_gameOver) {
            _currentLevel--;
            _score = _totalScore;
        }
        switch (_currentLevel) {
            case 0:
                return new Level1();
            case 1:
                return new Level2();
            case 2:
                return new Level3();
            default:
                return null;
        }

    }

    private void nextLevel(){
        _currentLevel++;
        if (_currentLevel > 2){
            _onLastLevel = true;
        }
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
            checkGameStatus();
        }
    }

    private void update(final double dt) {
        if (_reset) {
            _level.update(dt);
            _camera.lookAt(_level._player);
        }
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
            renderHUD(_canvas, _paint);
            if (_gameOver){
                renderGameOverHUD(_canvas, _paint);
            }
            else if (_onLastLevel && _levelComplete){
                renderGameClearHUD(_canvas, _paint);
            }
            else if (_levelComplete){
                renderLevelClearHUD(_canvas, _paint);
            }
            _holder.unlockCanvasAndPost(_canvas);
        }
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event){
        if (event.getAction() == MotionEvent.ACTION_DOWN && getCoinsLeft() < 1 || event.getAction() == MotionEvent.ACTION_DOWN && _lives < 1) {
            nextLevel();
            _reset = false;
            try {
                reset();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
    private boolean lockCanvas() {
        if (!_holder.getSurface().isValid()) {
            return false;
        }
        _canvas = _holder.lockCanvas();
        return (_canvas != null);

    }

    private void renderHUD(final Canvas canvas, final Paint paint){
        paint.setColor(Color.WHITE);
        paint.setTextSize(textSize);
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(String.format("%1$s %2$s",getResources().getString(R.string.lives), _lives), 10, textSize, paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(String.format("%1$s %2$s",getResources().getString(R.string.score), _score), STAGE_WIDTH, textSize*2, paint);
        canvas.drawText(getResources().getString(R.string.remaining) +" "+ _coinsRemaining, STAGE_WIDTH, textSize, paint);
    }

    private void renderGameOverHUD(final Canvas canvas, final Paint paint){
        paint.setColor(Color.BLACK);
        paint.setTextSize(textSize);
        paint.setAntiAlias(true);

        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(String.format("%1$s",getResources().getString(R.string.game_over)), _halfWidth, _halfHeight, paint);
        paint.setTextSize(textSize);
        canvas.drawText(String.format("%1$s",getResources().getString(R.string.restart_level)), _halfWidth, _halfHeight+textSize, paint);
    }

    private void renderLevelClearHUD(final Canvas canvas, final Paint paint){
        paint.setColor(Color.BLACK);
        paint.setTextSize(textSize);
        paint.setAntiAlias(true);

        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(String.format("%1$s",getResources().getString(R.string.level_clear)), _halfWidth, _halfHeight, paint);
        paint.setTextSize(textSize-4);
        canvas.drawText(String.format("%1$s",getResources().getString(R.string.next_level)), _halfWidth, _halfHeight+textSize, paint);
    }

    private void renderGameClearHUD(final Canvas canvas, final Paint paint){
        paint.setColor(Color.BLACK);
        paint.setTextSize(textSize-4);
        paint.setAntiAlias(true);

        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(String.format("%1$s",getResources().getString(R.string.game_clear)), _halfWidth, _halfHeight, paint);
        paint.setTextSize(textSize-4);
        canvas.drawText(String.format("%1$s",getResources().getString(R.string.finish)), _halfWidth, _halfHeight+textSize, paint);
    }

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
        while(_gameThread.getState() != Thread.State.TERMINATED) {
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
