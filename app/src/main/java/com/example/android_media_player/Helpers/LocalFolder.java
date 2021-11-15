package com.example.android_media_player.Helpers;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;

public class LocalFolder {
    private DocumentFile documentFile;
    private String name;

    public LocalFolder(DocumentFile documentFile, String name) {
        this.documentFile = documentFile;
        this.name = name;
    }

    public DocumentFile getDocumentFile() {
        return documentFile;
    }

    public void setDocumentFile(DocumentFile documentFile) {
        this.documentFile = documentFile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }
}
