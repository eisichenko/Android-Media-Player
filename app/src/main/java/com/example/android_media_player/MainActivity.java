package com.example.android_media_player;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Button openMusicFolderButton;
    Button openVideoFileButton;

    public final static int REQUEST_CODE_OPEN_MUSIC_FILE = 0;
    public final static int REQUEST_CODE_OPEN_VIDEO_FILE = 1;

    public static DocumentFile chosenFile;
    public static Uri chosenUri;

    public void chooseMusicFileIntent() {
        Intent intent = new Intent().setAction(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(Intent.createChooser(intent, "Choose file"), REQUEST_CODE_OPEN_MUSIC_FILE);
    }

    public void chooseVideoFileIntent() {
        Intent intent = new Intent().setType("video/*").setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Choose file"), REQUEST_CODE_OPEN_VIDEO_FILE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("Media player");

        openMusicFolderButton = findViewById(R.id.openMusicFolderButton);
        openVideoFileButton = findViewById(R.id.openVideoFileButton);

        openMusicFolderButton.setOnClickListener(v -> {
            chooseMusicFileIntent();
        });

        openVideoFileButton.setOnClickListener(v -> {
            chooseVideoFileIntent();
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (resultCode == Activity.RESULT_OK && resultData != null) {
            chosenUri = resultData.getData();

            if (requestCode == REQUEST_CODE_OPEN_MUSIC_FILE) {
                chosenFile = DocumentFile.fromTreeUri(this, chosenUri);
                Intent intent = new Intent(this, MusicActivity.class);
                startActivity(intent);
            }
            else if (requestCode == REQUEST_CODE_OPEN_VIDEO_FILE) {
                chosenFile = DocumentFile.fromSingleUri(this, chosenUri);
                System.out.println("VIDEO");
                System.out.println(resultData.getData());
            }

            super.onActivityResult(requestCode, resultCode, resultData);
        }
    }
}