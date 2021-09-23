package com.example.android_media_player;

import androidx.documentfile.provider.DocumentFile;

public class Song {
    private DocumentFile file;

    public Song(DocumentFile file) {
        this.file = file;
    }

    public String getName() {
        return file.getName();
    }
}
