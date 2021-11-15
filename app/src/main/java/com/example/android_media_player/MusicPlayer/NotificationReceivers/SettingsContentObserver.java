package com.example.android_media_player.MusicPlayer.NotificationReceivers;

import android.app.Activity;
import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.android_media_player.MusicPlayer.MusicActivity;
import com.example.android_media_player.R;

public class SettingsContentObserver extends ContentObserver {
    private final AudioManager audioManager;
    TextView currentVolumeTextView;
    MenuItem muteMenuItem;
    private final Context context;

    public SettingsContentObserver(Context context, Handler handler, MenuItem muteMenuItem) {
        super(handler);
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        currentVolumeTextView = ((Activity) context).findViewById(R.id.currentVolumeTextView);
        this.context = context;
        this.muteMenuItem = muteMenuItem;
    }

    @Override
    public boolean deliverSelfNotifications() {
        return false;
    }

    @Override
    public void onChange(boolean selfChange) {
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        if (MusicActivity.isVolumeMuted && currentVolume > 0) {
            MusicActivity.isVolumeMuted = false;
            muteMenuItem.setIcon(ContextCompat.getDrawable(context, R.drawable.ic_volume_mute));
        }

        if (MusicActivity.isVolumeMuted) {
            currentVolumeTextView.setText("Volume: Muted");
        }
        else {
            currentVolumeTextView.setText(String.format("Volume: %d%%", Math.round((float) currentVolume / maxVolume * 100.0)));
        }

        System.out.println("Volume now " + currentVolume);
    }
}