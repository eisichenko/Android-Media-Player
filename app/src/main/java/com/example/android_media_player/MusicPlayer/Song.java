package com.example.android_media_player.MusicPlayer;

import android.net.Uri;

import androidx.annotation.NonNull;
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

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return name.equals(((Song)obj).getName());
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }
}
