package com.example.android_media_player;

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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;

import com.example.android_media_player.MusicPlayer.MusicActivity;
import com.example.android_media_player.VideoPlayer.VideoActivity;

public class MainActivity extends AppCompatActivity {

    Button openMusicFolderButton;
    Button openVideoFileButton;

    public final static int REQUEST_CODE_OPEN_MUSIC_FILE = 0;
    public final static int REQUEST_CODE_OPEN_VIDEO_FILE = 1;
    public final static int REQUEST_PERMISSIONS = 2;

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

    public Boolean checkPermissions(final Integer requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            startActivityForResult(Intent.createChooser(intent, "Check Settings"), requestCode);
            return false;
        }
        else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R &&
                (ContextCompat.checkSelfPermission( this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                                PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE},
                    requestCode);
            return false;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("Media player");

        openMusicFolderButton = findViewById(R.id.openMusicFolderButton);
        openVideoFileButton = findViewById(R.id.openVideoFileButton);

        openMusicFolderButton.setOnClickListener(v -> {
            if (checkPermissions(REQUEST_PERMISSIONS)) {
                chooseMusicFileIntent();
            }
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
                Intent intent = new Intent(this, VideoActivity.class);
                startActivity(intent);
            }
            else if (requestCode == REQUEST_PERMISSIONS) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                        Toast.makeText(this, "Access was given successfully", Toast.LENGTH_SHORT).show();
                        chooseMusicFileIntent();
                    }
                    else {
                        Toast.makeText(this, "App won't work without access", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                else {
                    Toast.makeText(this, "Access was given successfully", Toast.LENGTH_SHORT).show();
                    chooseMusicFileIntent();
                }
            }

            super.onActivityResult(requestCode, resultCode, resultData);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Access was given successfully", Toast.LENGTH_SHORT).show();
                chooseMusicFileIntent();

            } else {
                Toast.makeText(this, "App won't work without access", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}