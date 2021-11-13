package com.example.android_media_player.MusicPlayer;

import android.app.Activity;
import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;
import android.widget.TextView;

import com.example.android_media_player.R;

public class SettingsContentObserver extends ContentObserver {
    private AudioManager audioManager;
    TextView currentVolumeTextView;

    public SettingsContentObserver(Context context, Handler handler) {
        super(handler);
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        currentVolumeTextView = ((Activity) context).findViewById(R.id.currentVolumeTextView);
    }

    @Override
    public boolean deliverSelfNotifications() {
        return false;
    }

    @Override
    public void onChange(boolean selfChange) {
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        currentVolumeTextView.setText(String.format("Volume: %d%%", Math.round((float) currentVolume / maxVolume * 100.0)));

        System.out.println("Volume now " + currentVolume);
    }
}