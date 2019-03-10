package com.alfredradu.platformer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.alfredradu.platformer.utils.Jukebox;

public class MenuActivity extends AppCompatActivity implements View.OnClickListener{
    private Jukebox _jukebox = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        final Button startBtn = findViewById(R.id.start);
        final Button toggleSound = findViewById(R.id.toggle_sound);
        startBtn.setOnClickListener(this);
        toggleSound.setOnClickListener(this);
        _jukebox = new Jukebox(this, "menu");
    }

    @Override
    protected void onPause(){
        super.onPause();
        _jukebox.pauseBgMusic();
    }
    @Override
    protected void onResume(){
        super.onResume();
        _jukebox.resumeBgMusic();
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if (v == findViewById(R.id.toggle_sound)){
            _jukebox.toggleMusicStatus();
            _jukebox.toggleSoundStatus();
        } else {
            final Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            finish();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(!hasFocus) {
            return;
        }
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }
}
