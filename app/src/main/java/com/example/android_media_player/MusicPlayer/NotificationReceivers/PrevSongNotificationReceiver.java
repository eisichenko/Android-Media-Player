package com.example.android_media_player.MusicPlayer.NotificationReceivers;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.android_media_player.Helpers.DatabaseHelper;
import com.example.android_media_player.MusicPlayer.MusicActivity;
import com.example.android_media_player.MusicPlayer.Models.Song;
import com.example.android_media_player.R;

public class PrevSongNotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);

        if (MusicActivity.currentSong == null) return;

        Song prevSong;

        MusicActivity.handler.removeCallbacks(MusicActivity.runnable);

        if (MusicActivity.songList.size() == 0) return;

        if (MusicActivity.playedSongs.size() == 0) {
            MusicActivity.selectedPosition--;
            if (MusicActivity.selectedPosition < 0) MusicActivity.selectedPosition = MusicActivity.songList.size() - 1;
            prevSong = MusicActivity.songList.get(MusicActivity.selectedPosition);
            MusicActivity.playedSongs.push(prevSong);
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

        if (prevSong != null) {
            try {
                Song dbSong = dbHelper.findSong(prevSong.getName());
                prevSong.setLaunchedTimes(dbSong.getLaunchedTimes() + 1);
                prevSong.setPlayedTime(dbSong.getPlayedTime());
                dbHelper.modifyLaunchedTimes(prevSong, prevSong.getLaunchedTimes());
            }
            catch (Exception e) {
                prevSong.setLaunchedTimes(1);
                dbHelper.add(prevSong);
            }
        }

        if (MusicActivity.currentSong != null) {
            dbHelper.modifyPlayedTime(MusicActivity.currentSong, MusicActivity.currentSong.getPlayedTime());
        }

        MusicActivity.currentSong = prevSong;
        System.out.println("PREV " + prevSong.getName());

        try {
            MusicActivity.mediaPlayer.reset();
            MusicActivity.mediaPlayer.setDataSource(prevSong.getPath());
            MusicActivity.mediaPlayer.prepare();
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        MusicActivity.mediaPlayer.start();
        MusicActivity.handler.post(MusicActivity.runnable);

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
