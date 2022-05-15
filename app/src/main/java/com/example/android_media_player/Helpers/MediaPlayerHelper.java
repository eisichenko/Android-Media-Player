package com.example.android_media_player.Helpers;

import android.media.MediaPlayer;

public class MediaPlayerHelper {
    public static void resetMediaPlayer(MediaPlayer mediaPlayer) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        mediaPlayer.reset();
    }
}
