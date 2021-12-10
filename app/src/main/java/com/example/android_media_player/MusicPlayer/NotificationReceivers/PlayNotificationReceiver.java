package com.example.android_media_player.MusicPlayer.NotificationReceivers;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.android_media_player.Helpers.DatabaseHelper;
import com.example.android_media_player.MusicPlayer.MusicActivity;
import com.example.android_media_player.MusicPlayer.Models.Song;
import com.example.android_media_player.R;

public class PlayNotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);

        if (MusicActivity.currentSong == null) return;

        if (MusicActivity.mediaPlayer.isPlaying()) {
            MusicActivity.mediaPlayer.pause();

            MusicActivity.handler.removeCallbacks(MusicActivity.runnable);
            dbHelper.modifyPlayedTime(MusicActivity.currentSong,
                    MusicActivity.currentSong.getPlayedTime());
        }
        else {
            MusicActivity.handler.removeCallbacks(MusicActivity.runnable);
            if (MusicActivity.currentSong != null) {
                try {
                    Song dbSong = dbHelper.findSong(MusicActivity.currentSong.getName());
                    MusicActivity.currentSong.setPlayedTime(dbSong.getPlayedTime());
                    dbHelper.modifyPlayedTime(MusicActivity.currentSong, MusicActivity.currentSong.getPlayedTime());
                }
                catch (Exception e) {
                    dbHelper.add(MusicActivity.currentSong);
                }
            }

            MusicActivity.mediaPlayer.start();
            MusicActivity.handler.post(MusicActivity.runnable);
        }

        Intent activityIntent = new Intent(context, OpenMusicNotificationReceiver.class);
        PendingIntent contentIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            contentIntent = PendingIntent.getBroadcast(context, MusicActivity.OPEN_MUSIC_CODE,
                    activityIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }
        else {
            contentIntent = PendingIntent.getBroadcast(context, MusicActivity.OPEN_MUSIC_CODE,
                    activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        Intent playBroadcastIntent = new Intent(context, PlayNotificationReceiver.class);
        PendingIntent playIntent = PendingIntent.getBroadcast(context,
                MusicActivity.PLAY_NOTIFICATION_CODE, playBroadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent previousBroadcastIntent = new Intent(context, PrevSongNotificationReceiver.class);
        PendingIntent previousIntent = PendingIntent.getBroadcast(context,
                MusicActivity.PREV_NOTIFICATION_CODE, previousBroadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextBroadcastIntent = new Intent(context, NextSongNotificationReceiver.class);
        PendingIntent nextIntent = PendingIntent.getBroadcast(context,
                MusicActivity.NEXT_NOTIFICATION_CODE, nextBroadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "Music notification");

        builder.setContentTitle("Music");
        if (MusicActivity.selectedPosition >= 0 && MusicActivity.selectedPosition < MusicActivity.songList.size()) {
            builder.setContentText(MusicActivity.currentSong.getName() + " (" + (MusicActivity.selectedPosition + 1) + "/" + MusicActivity.songList.size() + ")");
        }
        else {
            builder.setContentText(MusicActivity.currentSong.getName());
        }
        builder.setColor(Color.parseColor("#0000ff"));
        builder.setSmallIcon(R.drawable.ic_notification);
        builder.addAction(R.mipmap.ic_launcher, "Previous", previousIntent);
        builder.addAction(R.mipmap.ic_launcher, MusicActivity.getNotificationActionString(), playIntent);
        builder.addAction(R.mipmap.ic_launcher, "Next", nextIntent);
        builder.setContentIntent(contentIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(MusicActivity.NOTIFICATION_CODE, builder.build());
    }
}
