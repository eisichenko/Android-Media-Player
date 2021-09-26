package com.example.android_media_player.MusicPlayer;

import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;

public class Song {
    private String name;
    private String path;

    public Song(String path, String name) {
        this.path = path;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }
}
