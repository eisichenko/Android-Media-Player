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
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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

import com.example.android_media_player.Helpers.ColorPickerHelper;
import com.example.android_media_player.Helpers.LocalFolder;
import com.example.android_media_player.Helpers.MediaStoreHelper;
import com.example.android_media_player.Helpers.PathHelper;
import com.example.android_media_player.MusicPlayer.AllStatisticsActivity;
import com.example.android_media_player.MusicPlayer.MusicActivity;
import com.example.android_media_player.VideoPlayer.VideoActivity;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.flag.BubbleFlag;
import com.skydoves.colorpickerview.flag.FlagMode;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

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
    public final static int REQUEST_PERMISSIONS_WHEN_OPEN_FOLDER = 2;

    public static DocumentFile chosenFile;
    public static Uri chosenUri;

    public static String lastChosenSubfolderName = null;

    public SharedPreferences settings;
    public static final String APP_PREFERENCES_NAME = "media_player_settings";
    public static final String AUTOPLAY_CACHE_NAME = "autoplay";
    public static final String REPEAT_CACHE_NAME = "repeat";
    public static final String THEME_CACHE_NAME = "theme";
    public static final String FOLDER_URI_CACHE_NAME = "folder_uri";
    public static final String SONG_SORT_ORDER_CACHE_NAME = "song_sort_order";
    public static final String ARTIST_SORT_ORDER_CACHE_NAME = "artist_sort_order";
    public static final String SONG_SORT_LAST_COLUMN_CACHE_NAME = "song_last_column";
    public static final String ARTIST_SORT_LAST_COLUMN_CACHE_NAME = "artist_last_column";
    public static final String COLOR_PICKER_LAST_COLOR_CACHE_NAME = "color_picker_last_color";

    public static final String channelName = "Media Player Channel";
    public static final String channelDescription = "Cool Media Player";
    public static final String CHANNEL_ID = "1";

    public static ThemeType currentTheme = ThemeType.DAY;
    public static final String DEFAULT_APP_COLOR = "#FF0029F7";

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
            String versionName = "";
            try {
                versionName = getPackageManager()
                        .getPackageInfo(getPackageName(), 0).versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            new AlertDialog.Builder(this)
                    .setTitle("About")
                    .setMessage("• Written by Egor Isichenko, 2021\n\n" +
                            String.format("• Version %s", versionName))
                    .setPositiveButton("OK", (dialog, whichButton) -> {

                    })
                    .show();
        }
        else if (itemId == R.id.statsMenuItem) {
            startActivity(new Intent(this, AllStatisticsActivity.class));
        }
        else if (itemId == R.id.colorPickerItem) {
            BubbleFlag bubbleFlag = new BubbleFlag(this);
            bubbleFlag.setFlagMode(FlagMode.ALWAYS);

            ColorPickerDialog.Builder builder = new ColorPickerDialog.Builder(this)
                    .setTitle("Color Picker")
                    .setPreferenceName(COLOR_PICKER_LAST_COLOR_CACHE_NAME)
                    .setPositiveButton("Select",
                            (ColorEnvelopeListener) (envelope, fromUser) -> {
                                System.out.println("PICKED COLOR: #" + envelope.getHexCode());
                                ((MainActivity) this).getSupportActionBar().setBackgroundDrawable(new ColorDrawable(envelope.getColor()));
                                settings.edit().putString(COLOR_PICKER_LAST_COLOR_CACHE_NAME, "#" + envelope.getHexCode()).apply();
                            })
                    .setNegativeButton("Cancel",
                            (dialogInterface, i) -> {
                                dialogInterface.dismiss();
                            })
                    .attachAlphaSlideBar(false)
                    .attachBrightnessSlideBar(true)
                    .setBottomSpace(12);
            builder.getColorPickerView().setFlagView(bubbleFlag);
            builder.show();
        }
        else if (itemId == R.id.resetColorItem) {
            ((MainActivity) this).getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(DEFAULT_APP_COLOR)));
            settings.edit().remove(COLOR_PICKER_LAST_COLOR_CACHE_NAME).apply();
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

    @Override
    protected void onResume() {
        super.onResume();
        Objects.requireNonNull(subfoldersRecyclerView.getAdapter()).notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        settings = getSharedPreferences(APP_PREFERENCES_NAME, Context.MODE_PRIVATE);

        String themeString = settings.getString(THEME_CACHE_NAME, null);
        String uriString = settings.getString(FOLDER_URI_CACHE_NAME, null);

        if (uriString != null) {
            chosenUri = Uri.parse(uriString);
            String path = PathHelper.getAbsolutePathStringFromUri(chosenUri);
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

        ColorPickerHelper.setActionBarColor(getSupportActionBar(), settings);

        createNotificationChannel();

        System.out.println("SDK INT " + Build.VERSION.SDK_INT);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            startService(new Intent(this, KillNotificationService.class));
        }

        setTitle("Media player");

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

            long startTime = System.currentTimeMillis();
            subfolders = MediaStoreHelper.getSubfolders(this, chosenUri);
            System.out.printf("GOT SUBFOLDERS IN %.3fs%n", (double)(System.currentTimeMillis() - startTime) / 1000.0);

            System.out.println("SUBFOLDERS: " + subfolders);
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

        openVideoFileButton.setOnClickListener(v -> chooseVideoFileIntent());

        openLastFolderButton.setOnClickListener(v -> {
            if (chosenUri == null || chosenFile == null || chosenFile.getName() == null || !chosenFile.isDirectory()) {
                Toast.makeText(this, "No recent folder", Toast.LENGTH_SHORT).show();
            }
            else {
                String cachedUri = settings.getString(FOLDER_URI_CACHE_NAME, null);

                if (cachedUri != null) {
                    chosenUri = Uri.parse(cachedUri);
                    String path = PathHelper.getAbsolutePathStringFromUri(chosenUri);
                    chosenFile = DocumentFile.fromFile(new File(path));
                    lastChosenSubfolderName = chosenFile.getName();
                }
                else {
                    String path = PathHelper.getAbsolutePathStringFromUri(chosenUri);
                    chosenFile = DocumentFile.fromFile(new File(path));
                }
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

                subfolders = new ArrayList<>();
                if (chosenUri != null && chosenFile != null && chosenFile.getName() != null && chosenFile.isDirectory()) {
                    lastFolderTextView.setText("Last music folder: " + chosenFile.getName());

                    long startTime = System.currentTimeMillis();
                    subfolders = MediaStoreHelper.getSubfolders(this, chosenUri);
                    System.out.printf("GOT SUBFOLDERS IN %.5fs%n", (System.currentTimeMillis() - startTime) / 1000.0);

                    System.out.println("SUBFOLDERS: " + subfolders);
                }

                if (subfolders.size() == 0) {
                    noneTextView.setVisibility(View.VISIBLE);
                    subfoldersRecyclerView.setVisibility(View.GONE);
                }
                else {
                    noneTextView.setVisibility(View.GONE);
                    subfoldersRecyclerView.setVisibility(View.VISIBLE);
                }
                setAdapter(subfolders);

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

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}