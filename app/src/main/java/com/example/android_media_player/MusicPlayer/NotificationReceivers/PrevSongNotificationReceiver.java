package com.example.android_media_player.MusicPlayer.NotificationReceivers;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.android_media_player.Helpers.DatabaseHelper;
import com.example.android_media_player.MusicPlayer.Models.Song;
import com.example.android_media_player.MusicPlayer.MusicActivity;
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

        Intent playBroadcastIntent = new Intent(context, PlayNotificationReceiver.class);
        PendingIntent playIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            playIntent = PendingIntent.getBroadcast(context,
                    MusicActivity.PLAY_NOTIFICATION_CODE, playBroadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        }
        else {
            playIntent = PendingIntent.getBroadcast(context,
                    MusicActivity.PLAY_NOTIFICATION_CODE, playBroadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        Intent previousBroadcastIntent = new Intent(context, PrevSongNotificationReceiver.class);

        PendingIntent previousIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            previousIntent = PendingIntent.getBroadcast(context,
                    MusicActivity.PREV_NOTIFICATION_CODE, previousBroadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        }
        else {
            previousIntent = PendingIntent.getBroadcast(context,
                    MusicActivity.PREV_NOTIFICATION_CODE, previousBroadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        Intent nextBroadcastIntent = new Intent(context, NextSongNotificationReceiver.class);

        PendingIntent nextIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            nextIntent = PendingIntent.getBroadcast(context,
                    MusicActivity.NEXT_NOTIFICATION_CODE, nextBroadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        }
        else {
            nextIntent = PendingIntent.getBroadcast(context,
                    MusicActivity.NEXT_NOTIFICATION_CODE, nextBroadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

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

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(MusicActivity.NOTIFICATION_CODE, builder.build());
    }
}
