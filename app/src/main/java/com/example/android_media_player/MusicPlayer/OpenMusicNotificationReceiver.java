package com.example.android_media_player.MusicPlayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class OpenMusicNotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (MusicActivity.currentSong == null) return;

        context.startActivity(new Intent(context, MusicActivity.class));
    }
}
