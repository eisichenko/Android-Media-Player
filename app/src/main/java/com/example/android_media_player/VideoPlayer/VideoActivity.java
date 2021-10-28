package com.example.android_media_player.VideoPlayer;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android_media_player.MainActivity;
import com.example.android_media_player.R;

public class VideoActivity extends AppCompatActivity {

    VideoView videoView;
    Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        setTitle("Video player");

        backButton = findViewById(R.id.backButton);
        videoView = findViewById(R.id.videoView);

        videoView.setVideoURI(MainActivity.chosenFile.getUri());

        MediaController mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);
        mediaController.setAnchorView(videoView);

        int pos = 0;
        if (savedInstanceState != null) {
            pos = savedInstanceState.getInt("pos");
        }
        videoView.seekTo(pos);

        if (!videoView.isPlaying()) {
            videoView.start();
        }

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (videoView != null) {
            outState.putInt("pos", videoView.getCurrentPosition());
        }
    }
}