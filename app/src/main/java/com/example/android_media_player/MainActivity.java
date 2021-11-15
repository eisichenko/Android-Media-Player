package com.example.android_media_player;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_media_player.Helpers.DatabaseHelper;
import com.example.android_media_player.Helpers.LocalFolder;
import com.example.android_media_player.MusicPlayer.MusicActivity;
import com.example.android_media_player.MusicPlayer.Models.Song;
import com.example.android_media_player.MusicPlayer.Adapters.SongStatisticsRecyclerViewAdapter;
import com.example.android_media_player.VideoPlayer.VideoActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    Button openMusicFolderButton;
    Button openVideoFileButton;
    Button openLastFolderButton;
    TextView lastFolderTextView;
    TextView noneTextView;
    RecyclerView subfoldersRecyclerView;

    public final static int REQUEST_CODE_OPEN_MUSIC_FILE = 0;
    public final static int REQUEST_CODE_OPEN_VIDEO_FILE = 1;
    public final static int REQUEST_PERMISSIONS = 2;
    public final static int REQUEST_PERMISSIONS_WHEN_OPEN_FOLDER = 3;

    public static DocumentFile chosenFile;
    public static Uri chosenUri;
    public static String lastChosenSubfolderName = null;

    public static SharedPreferences settings;
    public static final String APP_PREFERENCES_NAME = "media_player_settings";
    public static final String AUTOPLAY_CACHE_NAME = "autoplay";
    public static final String REPEAT_CACHE_NAME = "repeat";
    public static final String THEME_CACHE_NAME = "theme";
    public static final String FOLDER_URI_CACHE_NAME = "folder_uri";
    public static final String SONG_SORT_ORDER_CACHE_NAME = "song_sort_order";
    public static final String ARTIST_SORT_ORDER_CACHE_NAME = "artist_sort_order";
    public static final String SONG_SORT_LAST_COLUMN_CACHE_NAME = "song_last_column";
    public static final String ARTIST_SORT_LAST_COLUMN_CACHE_NAME = "artist_last_column";

    public static final String channelName = "Media Player Channel";
    public static final String channelDescription = "Cool Media Player";
    public static final String CHANNEL_ID = "1";

    public static ThemeType currentTheme = ThemeType.DAY;

    public ArrayList<LocalFolder> subfolders;

    public void chooseMusicFileIntent() {
        Intent intent = new Intent().setAction(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(Intent.createChooser(intent, "Choose file"), REQUEST_CODE_OPEN_MUSIC_FILE);
    }

    public void chooseVideoFileIntent() {
        Intent intent = new Intent().setType("video/*").setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Choose file"), REQUEST_CODE_OPEN_VIDEO_FILE);
    }

    public Boolean checkPermissions(final Integer requestCode) {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                    requestCode);
            return false;
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.themeMenuItem) {
            System.out.println(currentTheme.toString());
            if (currentTheme == ThemeType.DAY) {
                setTheme(R.style.Theme_Night);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                settings.edit().putString(THEME_CACHE_NAME, ThemeType.NIGHT.toString()).apply();
                item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_day));
                currentTheme = ThemeType.NIGHT;
            }
            else {
                setTheme(R.style.Theme_Day);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                settings.edit().putString(THEME_CACHE_NAME, ThemeType.DAY.toString()).apply();
                item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_night));
                currentTheme = ThemeType.DAY;
            }

            recreate();

            return true;
        }
        else if (itemId == R.id.aboutMenuItem) {
            new AlertDialog.Builder(this)
                    .setTitle("About")
                    .setMessage("• Written by Egor Isichenko, 2021\n\n" +
                            "• Icon of style\n\n" +
                            "• Icon of minimalism\n\n" +
                            "• Icon of functionality")
                    .setPositiveButton("OK", (dialog, whichButton) -> {

                    })
                    .show();
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

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, importance);
            channel.setDescription(channelDescription);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        System.out.println("MAIN DESTROY");
        System.out.println(MusicActivity.isBackPressed);
        if (!MusicActivity.isBackPressed) {
            NotificationManager nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nMgr.cancelAll();
        }
    }

    public static ArrayList<LocalFolder> getSubfolders(DocumentFile rootFolder, String currentRootPath) {
        ArrayList<LocalFolder> res = new ArrayList<>();

        for (DocumentFile file : rootFolder.listFiles()) {
            if (file.isDirectory()) {
                ArrayList<String> pathParts = new ArrayList<>();
                pathParts.add(currentRootPath);
                pathParts.add(file.getName());
                String newRootPath = MusicActivity.pathCombine(pathParts);
                res.add(new LocalFolder(file, newRootPath));
                res.addAll(getSubfolders(file, newRootPath));
            }
        }

        return res;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        settings = getSharedPreferences(APP_PREFERENCES_NAME, Context.MODE_PRIVATE);

        String themeString = settings.getString(THEME_CACHE_NAME, null);

        String uriString = settings.getString(FOLDER_URI_CACHE_NAME, null);

        if (uriString != null) {
            chosenUri = Uri.parse(uriString);
            String path = MusicActivity.getAbsolutePathStringFromUri(chosenUri);
            chosenFile = DocumentFile.fromFile(new File(path));
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

        createNotificationChannel();

        System.out.println("SDK INT " + Build.VERSION.SDK_INT);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            startService(new Intent(this, KillNotificationService.class));
        }

        setTitle("Media player");

        checkPermissions(REQUEST_PERMISSIONS);

        openMusicFolderButton = findViewById(R.id.openMusicFolderButton);
        openVideoFileButton = findViewById(R.id.openVideoFileButton);
        openLastFolderButton = findViewById(R.id.openLastFolderButton);
        noneTextView = findViewById(R.id.noneTextView);
        lastFolderTextView = findViewById(R.id.lastFolderTextView);
        subfoldersRecyclerView = findViewById(R.id.subfoldersRecyclerView);

        DividerItemDecoration decoration = new DividerItemDecoration(getApplicationContext(),
                DividerItemDecoration.VERTICAL);
        decoration.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(this, R.drawable.list_divider)));

        subfoldersRecyclerView.addItemDecoration(decoration);

        subfolders = new ArrayList<>();

        if (chosenUri != null && chosenFile != null && chosenFile.getName() != null && chosenFile.isDirectory()) {
            lastFolderTextView.setText("Last music folder: " + chosenFile.getName());
            subfolders = getSubfolders(chosenFile, "");
        }

        if (subfolders.size() == 0) {
            noneTextView.setVisibility(View.VISIBLE);
            subfoldersRecyclerView.setVisibility(View.GONE);
        }
        else {
            noneTextView.setVisibility(View.GONE);
            subfoldersRecyclerView.setVisibility(View.VISIBLE);
        }

        openMusicFolderButton.setOnClickListener(v -> {
            if (checkPermissions(REQUEST_PERMISSIONS_WHEN_OPEN_FOLDER)) {
                chooseMusicFileIntent();
            }
        });

        openVideoFileButton.setOnClickListener(v -> {
            chooseVideoFileIntent();
        });

        openLastFolderButton.setOnClickListener(v -> {
            if (chosenUri == null || chosenFile == null || chosenFile.getName() == null || !chosenFile.isDirectory()) {
                Toast.makeText(this, "No recent folder", Toast.LENGTH_SHORT).show();
            }
            else {
                String path = MusicActivity.getAbsolutePathStringFromUri(chosenUri);
                chosenFile = DocumentFile.fromFile(new File(path));
                Intent intent = new Intent(this, MusicActivity.class);
                startActivity(intent);
            }
        });

        setAdapter(subfolders);
    }

    public void setAdapter(ArrayList<LocalFolder> subfolderList) {
        SubfoldersRecyclerViewAdapter subfoldersAdapter = new SubfoldersRecyclerViewAdapter(subfolderList);
        RecyclerView.LayoutManager subfoldersLayoutManager = new LinearLayoutManager(getApplicationContext());

        subfoldersRecyclerView.setLayoutManager(subfoldersLayoutManager);
        subfoldersRecyclerView.setItemAnimator(new DefaultItemAnimator());
        subfoldersRecyclerView.setAdapter(subfoldersAdapter);
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

            super.onActivityResult(requestCode, resultCode, resultData);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS_WHEN_OPEN_FOLDER) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Access was given successfully", Toast.LENGTH_SHORT).show();
                chooseMusicFileIntent();

            } else {
                Toast.makeText(this, "App won't work without access", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        else if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Access was given successfully", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(this, "App won't work without access", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}