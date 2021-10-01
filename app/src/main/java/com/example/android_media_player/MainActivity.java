package com.example.android_media_player;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;

import com.example.android_media_player.MusicPlayer.MusicActivity;
import com.example.android_media_player.VideoPlayer.VideoActivity;

public class MainActivity extends AppCompatActivity {

    Button openMusicFolderButton;
    Button openVideoFileButton;
    Button openLastFolderButton;
    TextView lastFolderTextView;

    public final static int REQUEST_CODE_OPEN_MUSIC_FILE = 0;
    public final static int REQUEST_CODE_OPEN_VIDEO_FILE = 1;
    public final static int REQUEST_PERMISSIONS = 2;

    public static DocumentFile chosenFile;
    public static Uri chosenUri;

    public static SharedPreferences settings;
    public static final String APP_PREFERENCES_NAME = "media_player_settings";
    public static final String AUTOPLAY_CACHE_NAME = "autoplay";
    public static final String REPEAT_CACHE_NAME = "repeat";
    public static final String THEME_CACHE_NAME = "theme";
    public static final String FOLDER_URI_CACHE_NAME = "folder_uri";
    public static final String HIDE_LIST_CACHE_NAME = "hide_list";
    public static final String ORDER_CACHE_NAME = "order";
    public static final String LAST_COLUMN_CACHE_NAME = "last_column";

    public static ThemeType currentTheme = ThemeType.DAY;

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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.themeMenuItem) {
            if (currentTheme == ThemeType.DAY) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                settings.edit().putString(THEME_CACHE_NAME, ThemeType.NIGHT.toString()).apply();
                item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_day));
                currentTheme = ThemeType.NIGHT;
            }
            else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                settings.edit().putString(THEME_CACHE_NAME, ThemeType.DAY.toString()).apply();
                item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_night));
                currentTheme = ThemeType.DAY;
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        MenuItem themeMenuItem = menu.findItem(R.id.themeMenuItem);

        if (currentTheme == ThemeType.DAY) {
            themeMenuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_night));
        }
        else {
            themeMenuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_day));
        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        settings = getSharedPreferences(APP_PREFERENCES_NAME, Context.MODE_PRIVATE);

        String themeString = settings.getString(THEME_CACHE_NAME, null);

        String uriString = settings.getString(FOLDER_URI_CACHE_NAME, null);

        if (uriString != null) {
            chosenUri = Uri.parse(uriString);
            chosenFile = DocumentFile.fromTreeUri(this, chosenUri);
        }

        if (themeString != null) {
            if (themeString.equals(ThemeType.DAY.toString())) {
                currentTheme = ThemeType.DAY;
                setTheme(R.style.Theme_Day);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            else {
                currentTheme = ThemeType.NIGHT;
                setTheme(R.style.Theme_Night);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startService(new Intent(this, KillNotificationService.class));

        setTitle("Media player");

        openMusicFolderButton = findViewById(R.id.openMusicFolderButton);
        openVideoFileButton = findViewById(R.id.openVideoFileButton);
        openLastFolderButton = findViewById(R.id.openLastFolderButton);
        lastFolderTextView = findViewById(R.id.lastFolderTextView);

        if (chosenUri != null && chosenFile != null && chosenFile.getName() != null) {
            lastFolderTextView.setText("Last music folder: " + chosenFile.getName());
        }

        openMusicFolderButton.setOnClickListener(v -> {
            if (checkPermissions(REQUEST_PERMISSIONS)) {
                chooseMusicFileIntent();
            }
        });

        openVideoFileButton.setOnClickListener(v -> {
            chooseVideoFileIntent();
        });

        openLastFolderButton.setOnClickListener(v -> {
            if (chosenUri == null || chosenFile == null || chosenFile.getName() == null) {
                Toast.makeText(this, "No recent folder", Toast.LENGTH_SHORT).show();
            }
            else {
                chosenFile = DocumentFile.fromTreeUri(this, chosenUri);
                Intent intent = new Intent(this, MusicActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (resultCode == Activity.RESULT_OK && resultData != null) {
            chosenUri = resultData.getData();

            if (requestCode == REQUEST_CODE_OPEN_MUSIC_FILE) {
                chosenFile = DocumentFile.fromTreeUri(this, chosenUri);
                settings.edit().putString(FOLDER_URI_CACHE_NAME, chosenUri.toString()).apply();
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