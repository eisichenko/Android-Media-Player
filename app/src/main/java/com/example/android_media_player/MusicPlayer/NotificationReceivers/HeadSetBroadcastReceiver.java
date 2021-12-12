package com.example.android_media_player.MusicPlayer.NotificationReceivers;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.android_media_player.MusicPlayer.MusicActivity;
import com.example.android_media_player.R;

public class HeadSetBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
            int state = intent.getIntExtra("state", -1);
            System.out.println("HEADSET " + state);

            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

            int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

            TextView currentVolumeTextView = ((Activity)context).findViewById(R.id.currentVolumeTextView);
            MenuItem muteMenuItem = ((MusicActivity)context).muteMenuItem;

            if (MusicActivity.isVolumeMuted && currentVolume > 0) {
                MusicActivity.isVolumeMuted = false;
            }

            if (muteMenuItem != null) {
                if (MusicActivity.isVolumeMuted) {
                    muteMenuItem.setIcon(ContextCompat.getDrawable(context, R.drawable.ic_volume_unmute));
                }
                else {
                    muteMenuItem.setIcon(ContextCompat.getDrawable(context, R.drawable.ic_volume_mute));
                }
            }

            if (!MusicActivity.isVolumeMuted) {
                currentVolumeTextView.setText(String.format("Volume: %d%%", Math.round((float) currentVolume / maxVolume * 100.0)));
            }
            else {
                currentVolumeTextView.setText("Volume: Muted");
            }
        }
    }
}
