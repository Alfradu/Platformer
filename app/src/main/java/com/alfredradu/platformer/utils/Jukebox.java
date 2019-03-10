package com.alfredradu.platformer.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.preference.PreferenceManager;
import android.util.Log;

import com.alfredradu.platformer.Game;

import java.io.IOException;
import java.util.HashMap;

public class Jukebox {
    private static final String TAG = "Jukebox";
    private String _musicFile;
    private SoundPool _soundPool = null;
    private static final int MAX_STREAMS = 3;
    private HashMap<Game.GameEvent, Integer> _soundsMap;
    private static final String SOUNDS_PREF_KEY = "sounds_pref_key";
    private static final String MUSIC_PREF_KEY = "music_pref_key";
    private boolean _soundEnabled;
    private boolean _musicEnabled;
    private MediaPlayer _bgPlayer = null;
    private float DEFAULT_SFX_VOLUME = 0.4f;
    private float DEFAULT_MUSIC_VOLUME = 0.6f;
    private Context _context = null;

    public Jukebox(Context context, String music) {
        _musicFile = music;
        _context = context;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        _soundEnabled = prefs.getBoolean(SOUNDS_PREF_KEY, true);
        _musicEnabled = prefs.getBoolean(MUSIC_PREF_KEY, true);
        loadIfNeeded();
    }

    public void newSong(String music){
        _musicFile = music;
        if(!_musicEnabled){ return; }
        _bgPlayer.reset();
        loadMusic();
        _bgPlayer.start();
    }

    private void loadIfNeeded(){
        if (_soundEnabled){
            loadSounds();
        }
        if (_musicEnabled){
            loadMusic();
        }
    }

    private void createSoundPool() {
        AudioAttributes attr = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        _soundPool = new SoundPool.Builder()
                .setAudioAttributes(attr)
                .setMaxStreams(MAX_STREAMS)
                .build();
    }

    private void loadEventSound(final Game.GameEvent event, final String fileName){
        try {
            AssetFileDescriptor afd = _context.getAssets().openFd(fileName);
            int soundId = _soundPool.load(afd, 1);
            _soundsMap.put(event, soundId);
        } catch( IOException e){
            Log.e(TAG, "loadEventsound: error loading sound " + e.toString());
        }
    }

    private void loadSounds(){
        createSoundPool();
        _soundsMap = new HashMap<Game.GameEvent, Integer>();
        loadEventSound(Game.GameEvent.Jump, "sounds/jump.wav");
        loadEventSound(Game.GameEvent.Hurt, "sounds/hurt.wav");
        loadEventSound(Game.GameEvent.CoinPickup, "sounds/coin.wav");
        loadEventSound(Game.GameEvent.HeartPickup, "sounds/heart.wav");
        loadEventSound(Game.GameEvent.LevelStart, "sounds/game_start.wav");
        loadEventSound(Game.GameEvent.GameOver, "sounds/game_over.wav");
        loadEventSound(Game.GameEvent.LevelClear, "sounds/level_clear.wav");
    }

    private void unloadSounds(){
        if (_soundPool != null){
            _soundPool.release();
            _soundPool = null;
            _soundsMap.clear();
        }
    }

    public void playSoundForGameEvent(Game.GameEvent event){
        if(!_soundEnabled){return;}
        final float leftVolume = DEFAULT_SFX_VOLUME;
        final float rightVolume = DEFAULT_SFX_VOLUME;
        final int priority = 1;
        final int loop = 0; //-1 loop forever, 0 play once
        final float rate = 1.0f;
        final Integer soundID = _soundsMap.get(event);
        if(soundID != null){
            _soundPool.play(soundID, leftVolume, rightVolume, priority, loop, rate);
        }
    }

    private void loadMusic(){
        try{
            _bgPlayer = new MediaPlayer();
            AssetFileDescriptor afd = _context
                    .getAssets().openFd("music/"+_musicFile+".mp3");
            _bgPlayer.setDataSource(
                    afd.getFileDescriptor(),
                    afd.getStartOffset(),
                    afd.getLength());
            _bgPlayer.setLooping(true);
            _bgPlayer.setVolume(DEFAULT_MUSIC_VOLUME, DEFAULT_MUSIC_VOLUME);
            _bgPlayer.prepare();
        }catch(IOException e){
            _bgPlayer = null;
            _musicEnabled = false;
            Log.e(TAG, e.toString());
        }
    }

    private void unloadMusic(){
        if(_bgPlayer != null) {
            _bgPlayer.stop();
            _bgPlayer.release();
        }
    }

    public void pauseBgMusic(){
        if(!_musicEnabled){ return; }
        _bgPlayer.pause();
    }
    public void resumeBgMusic(){
        if(!_musicEnabled){ return; }
        _bgPlayer.start();
    }

    public void toggleSoundStatus(){
        _soundEnabled = !_soundEnabled;
        if(_soundEnabled){
            loadSounds();
        }else{
            unloadSounds();
        }
        PreferenceManager
                .getDefaultSharedPreferences(_context)
                .edit()
                .putBoolean(SOUNDS_PREF_KEY, _soundEnabled)
                .apply();
    }

    public void toggleMusicStatus(){
        _musicEnabled = !_musicEnabled;
        if(_musicEnabled){
            loadMusic();
            _bgPlayer.start();
        }else{
            unloadMusic();
        }
        PreferenceManager
                .getDefaultSharedPreferences(_context)
                .edit()
                .putBoolean(MUSIC_PREF_KEY, _musicEnabled)
                .apply();
    }
}
