package com.example.android_media_player.MusicPlayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android_media_player.Helpers.DatabaseHelper;
import com.example.android_media_player.MainActivity;
import com.example.android_media_player.R;
import com.example.android_media_player.ThemeType;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.Scanner;
import java.util.Stack;

public class StatisticsActivity extends AppCompatActivity {

    TextView totalTimeListenedTextView;
    TextView totalLaunchedTimesTextView;
    TextView favoriteSongTextView;
    TextView averageTimeListenedTextView;
    TextView averageLaunchedTimesTextView;

    Button songStatsButton;

    public final static int REQUEST_CODE_CHOOSE_SAVE_FILE = 0;
    public final static int REQUEST_CODE_CHOOSE_LOAD_FILE = 1;

    public void chooseLoadFileIntent() {
        Intent intent = new Intent().setType("*/*").setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Choose file"), REQUEST_CODE_CHOOSE_LOAD_FILE);
    }

    public void chooseSaveFileIntent() {
        Intent intent = new Intent().setType("*/*").setAction(Intent.ACTION_CREATE_DOCUMENT);
        startActivityForResult(Intent.createChooser(intent, "Choose file"), REQUEST_CODE_CHOOSE_SAVE_FILE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.statistics_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.clearDatabaseMenuItem) {
            new AlertDialog.Builder(this)
                    .setTitle("Clear database")
                    .setMessage("You will lose all your data, are you sure?")
                    .setPositiveButton("Clear", (dialogInterface, i) -> {
                        MusicActivity.dbHelper.clearAll();
                        Toast.makeText(this, "DB was cleared successfully", Toast.LENGTH_SHORT).show();

                        for (Song song : MusicActivity.songList) {
                            song.setLaunchedTimes(0);
                            song.setPlayedTime(0L);
                        }

                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
        else if (itemId == R.id.saveMenuItem) {
            chooseSaveFileIntent();
        }
        else if (itemId == R.id.loadMenuItem) {
            chooseLoadFileIntent();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String themeString = MainActivity.settings.getString(MainActivity.THEME_CACHE_NAME, null);

        if (themeString != null) {
            if (themeString.equals(ThemeType.DAY.toString())) {
                MainActivity.currentTheme = ThemeType.DAY;
                setTheme(R.style.Theme_Day);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            else {
                MainActivity.currentTheme = ThemeType.NIGHT;
                setTheme(R.style.Theme_Night);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        setTitle("Statistics");

        totalTimeListenedTextView = findViewById(R.id.totalTimeListenedTextView);
        totalLaunchedTimesTextView = findViewById(R.id.totalLaunchedTimesTextView);
        averageTimeListenedTextView = findViewById(R.id.averageTimeListenedTextView);
        averageLaunchedTimesTextView = findViewById(R.id.averageLaunchedTimesTextView);
        favoriteSongTextView = findViewById(R.id.favoriteSongTextView);
        songStatsButton = findViewById(R.id.songStatsButton);

        long start = System.currentTimeMillis();

        try {
            Song favoriteSong = MusicActivity.dbHelper.getMostPlayedSong();
            favoriteSongTextView.setText("Most played song: " + favoriteSong.getName());
        }
        catch (Exception e) {
            favoriteSongTextView.setText("Most played song: None");
        }

        System.out.println("MOST PLAYED: " + (System.currentTimeMillis() - start));

        try {
            Long totalPlayedTime = MusicActivity.dbHelper.getTotalPlayedTime();
            totalTimeListenedTextView.setText("Total listened time: " + MusicActivity.convertStatisticsTime(totalPlayedTime));
        }
        catch (Exception e) {
            totalTimeListenedTextView.setText("Total listened time: 0s");
        }

        System.out.println("TOTAL PLAYED: " + (System.currentTimeMillis() - start));

        try {
            Integer totalLaunchedTimes = MusicActivity.dbHelper.getTotalLaunchedTimes();
            totalLaunchedTimesTextView.setText("Total launched times: " + totalLaunchedTimes);
        }
        catch (Exception e) {
            totalLaunchedTimesTextView.setText("Total launched times: 0");
        }

        System.out.println("TOTAL LAUNCHED: " + (System.currentTimeMillis() - start));

        try {
            Long averagePlayedTime = MusicActivity.dbHelper.getAveragePlayTime();
            averageTimeListenedTextView.setText("Average listened time: " + MusicActivity.convertStatisticsTime(averagePlayedTime));
        }
        catch (Exception e) {
            averageTimeListenedTextView.setText("Average listened time: 0s");
        }

        System.out.println("AVERAGE PLAYED: " + (System.currentTimeMillis() - start));

        try {
            Integer averageLaunchedTimes = MusicActivity.dbHelper.getAverageLaunchTime();
            averageLaunchedTimesTextView.setText("Average launched times: " + averageLaunchedTimes);
        }
        catch (Exception e) {
            averageLaunchedTimesTextView.setText("Average launched times: 0");
        }

        songStatsButton.setOnClickListener(v -> {
            startActivity(new Intent(this, SongStatsActivity.class));
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (resultCode == Activity.RESULT_OK && resultData != null) {
            switch (requestCode) {
                case REQUEST_CODE_CHOOSE_SAVE_FILE:
                    Uri saveUri = resultData.getData();

                    ArrayList<Song> listToSave = MainActivity.dbHelper.selectALl(DatabaseHelper.SortType.ASCENDING,
                            DatabaseHelper.NAME_COLUMN);

                    DocumentFile chosenSaveFile = DocumentFile.fromSingleUri(this, saveUri);
                    String json = new Gson().toJson(listToSave);

                    try {
                        OutputStream outputStream = getContentResolver().openOutputStream(saveUri);
                        outputStream.write(json.getBytes(StandardCharsets.UTF_8));
                        outputStream.close();

                        Toast.makeText(this, "Statistics were saved to file " + chosenSaveFile.getName(), Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }

                    break;
                case REQUEST_CODE_CHOOSE_LOAD_FILE:
                    Uri loadUri = resultData.getData();

                    DocumentFile chosenLoadFile = DocumentFile.fromSingleUri(this, loadUri);

                    try {
                        InputStream inputStream = getContentResolver().openInputStream(loadUri);
                        Scanner s = new Scanner(inputStream).useDelimiter("\\A");
                        String loadedJson = s.hasNext() ? s.next() : "";
                        System.out.println(loadedJson);
                        ArrayList<Song> loadedList = new Gson().fromJson(loadedJson, new TypeToken<ArrayList<Song>>(){}.getType());

                        for (Song song : loadedList) {
                            if (song.getName() == null ||
                                song.getPlayedTime() == null ||
                                song.getLaunchedTimes() == null) {
                                throw new Exception("Invalid JSON file");
                            }
                        }

                        MusicActivity.dbHelper.clearAll();
                        for (Song song : loadedList) {
                            MusicActivity.dbHelper.add(song);
                        }

                        Toast.makeText(this, "Statistics were loaded from file " + chosenLoadFile.getName(), Toast.LENGTH_SHORT).show();

                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }

                    break;
            }
        }

        super.onActivityResult(requestCode, resultCode, resultData);
    }
}