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

public class PrevSongNotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (MusicActivity.currentSong == null) return;

        Song prevSong;

        if (MusicActivity.songList.size() == 0) return;

        if (MusicActivity.playedSongs.size() == 0) {
            MusicActivity.selectedPosition--;
            if (MusicActivity.selectedPosition < 0) MusicActivity.selectedPosition = MusicActivity.songList.size() - 1;
            prevSong = MusicActivity.songList.get(MusicActivity.selectedPosition);
        }
        else {
            prevSong = MusicActivity.playedSongs.pop();
            int index = MusicActivity.songList.indexOf(prevSong);
            while (index == MusicActivity.selectedPosition && MusicActivity.playedSongs.size() > 0) {
                prevSong = MusicActivity.playedSongs.pop();
                index = MusicActivity.songList.indexOf(prevSong);
            }
            if (index == MusicActivity.selectedPosition) {
                MusicActivity.selectedPosition--;
                if (MusicActivity.selectedPosition < 0) MusicActivity.selectedPosition = MusicActivity.songList.size() - 1;
                prevSong = MusicActivity.songList.get(MusicActivity.selectedPosition);
            }
            else {
                MusicActivity.selectedPosition = index;
            }
        }

        MusicActivity.currentSong = prevSong;
        System.out.println("PREV " + prevSong.getName());

        try {
            MusicActivity.mediaPlayer.reset();
            MusicActivity.mediaPlayer.setDataSource(context, prevSong.getUri());
            MusicActivity.mediaPlayer.prepare();
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        MusicActivity.mediaPlayer.start();

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
        builder.setContentText(MusicActivity.currentSong.getName());
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
