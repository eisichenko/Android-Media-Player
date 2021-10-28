package com.example.android_media_player.MusicPlayer;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.android_media_player.R;

public class NextSongNotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (MusicActivity.currentSong == null) return;

        if (MusicActivity.songList.size() == 0) return;

        MusicActivity.handler.removeCallbacks(MusicActivity.runnable);

        MusicActivity.selectedPosition = (MusicActivity.selectedPosition + 1) % MusicActivity.songList.size();

        Song nextSong = MusicActivity.songList.get(MusicActivity.selectedPosition);

        if (nextSong != null) {
            try {
                Song dbSong = MusicActivity.dbHelper.findSong(nextSong.getName());
                nextSong.setLaunchedTimes(dbSong.getLaunchedTimes() + 1);
                nextSong.setPlayedTime(dbSong.getPlayedTime());
                MusicActivity.dbHelper.modifyLaunchedTimes(nextSong, nextSong.getLaunchedTimes());
            }
            catch (Exception e) {
                nextSong.setLaunchedTimes(1);
                MusicActivity.dbHelper.add(nextSong);
            }
        }

        if (MusicActivity.currentSong != null) {
            MusicActivity.dbHelper.modifyPlayedTime(MusicActivity.currentSong, MusicActivity.currentSong.getPlayedTime());
        }

        MusicActivity.currentSong = nextSong;
        MusicActivity.playedSongs.push(MusicActivity.currentSong);

        try {
            MusicActivity.mediaPlayer.reset();
            MusicActivity.mediaPlayer.setDataSource(MusicActivity.currentSong.getPath());
            MusicActivity.mediaPlayer.prepare();
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        MusicActivity.mediaPlayer.start();
        MusicActivity.handler.post(MusicActivity.runnable);

        Intent activityIntent = new Intent(context, MusicActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, MusicActivity.OPEN_MUSIC_CODE,
                activityIntent, 0);

        Intent playBroadcastIntent = new Intent(context, PlayNotificationReceiver.class);
        PendingIntent playIntent = PendingIntent.getBroadcast(context,
                MusicActivity.PLAY_NOTIFICATION_CODE, playBroadcastIntent, 0);

        Intent previousBroadcastIntent = new Intent(context, PrevSongNotificationReceiver.class);
        PendingIntent previousIntent = PendingIntent.getBroadcast(context,
                MusicActivity.PREV_NOTIFICATION_CODE, previousBroadcastIntent, 0);

        Intent nextBroadcastIntent = new Intent(context, NextSongNotificationReceiver.class);
        PendingIntent nextIntent = PendingIntent.getBroadcast(context,
                MusicActivity.NEXT_NOTIFICATION_CODE, nextBroadcastIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "Music notification");

        builder.setContentTitle("Music");
        builder.setContentText(MusicActivity.currentSong.getName() + " (" + (MusicActivity.selectedPosition + 1) + "/" + MusicActivity.songList.size() + ")");
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
