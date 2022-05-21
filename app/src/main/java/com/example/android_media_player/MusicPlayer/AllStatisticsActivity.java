package com.example.android_media_player.MusicPlayer;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.documentfile.provider.DocumentFile;

import com.example.android_media_player.Helpers.ColorPickerHelper;
import com.example.android_media_player.Helpers.DatabaseHelper;
import com.example.android_media_player.MainActivity;
import com.example.android_media_player.MusicPlayer.Models.Artist;
import com.example.android_media_player.MusicPlayer.Models.Song;
import com.example.android_media_player.R;
import com.example.android_media_player.ThemeType;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

public class AllStatisticsActivity extends AppCompatActivity {

    TextView totalTimeListenedTextView;
    TextView totalLaunchedTimesTextView;
    TextView favoriteSongTextView;
    TextView averageTimeListenedTextView;
    TextView averageLaunchedTimesTextView;
    TextView playedTimePerLaunchTextView;
    TextView mostUnpopularSongTextView;
    TextView favoriteArtistTextView;
    TextView mostUnpopularArtistTextView;
    TextView totalNumberOfSongsTextView;
    TextView statsSinceTextView;

    Button songStatsButton;
    Button artistStatsButton;

    public final static int REQUEST_CODE_CHOOSE_SAVE_FILE = 0;
    public final static int REQUEST_CODE_CHOOSE_LOAD_FILE = 1;

    public static Long totalPlayedTime = 0L;

    public final DatabaseHelper dbHelper = new DatabaseHelper(this);

    public SharedPreferences settings;

    public static boolean isBackPressed = false;

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

        if (itemId == android.R.id.home) {
            isBackPressed = true;
            return super.onOptionsItemSelected(item);
        }
        else if (itemId == R.id.clearDatabaseMenuItem) {
            new AlertDialog.Builder(this)
                    .setTitle("Clear database")
                    .setMessage("You will lose all your data, are you sure?")
                    .setPositiveButton("Clear", (dialogInterface, i) -> {
                        dbHelper.truncateTables();
                        Toast.makeText(this, "DB was cleared successfully", Toast.LENGTH_SHORT).show();

                        if (MusicActivity.songList != null) {
                            for (Song song : MusicActivity.songList) {
                                song.setLaunchedTimes(0);
                                song.setPlayedTime(0L);
                            }
                        }

                        recreate();
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
    protected void onDestroy() {
        super.onDestroy();

        System.out.println("ALL STATS DESTROY");
        System.out.println("ARTISTS BACK PRESSED " + ArtistStatsActivity.isBackPressed);
        System.out.println("SONGS BACK PRESSED " + SongStatsActivity.isBackPressed);
        System.out.println("ALL STATS BACK PRESSED " + isBackPressed);

        if (!ArtistStatsActivity.isBackPressed && !SongStatsActivity.isBackPressed && !isBackPressed) {
            NotificationManager nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nMgr.cancelAll();
        }

        if (isBackPressed) {
            isBackPressed = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        settings = getSharedPreferences(MainActivity.APP_PREFERENCES_NAME, Context.MODE_PRIVATE);
        String themeString = settings.getString(MainActivity.THEME_CACHE_NAME, null);

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

        ColorPickerHelper.setActionBarColor(getSupportActionBar(), settings);

        setTitle("Statistics");

        totalTimeListenedTextView = findViewById(R.id.totalTimeListenedTextView);
        totalLaunchedTimesTextView = findViewById(R.id.totalLaunchedTimesTextView);
        averageTimeListenedTextView = findViewById(R.id.averageTimeListenedTextView);
        averageLaunchedTimesTextView = findViewById(R.id.averageLaunchedTimesTextView);
        playedTimePerLaunchTextView = findViewById(R.id.playedTimePerLaunchTextView);
        mostUnpopularSongTextView = findViewById(R.id.mostUnpopularSongTextView);
        favoriteSongTextView = findViewById(R.id.favoriteSongTextView);
        songStatsButton = findViewById(R.id.songStatsButton);
        artistStatsButton = findViewById(R.id.artistStatsButton);
        favoriteArtistTextView = findViewById(R.id.favoriteArtistTextView);
        mostUnpopularArtistTextView = findViewById(R.id.mostUnpopularArtistTextView);
        totalNumberOfSongsTextView = findViewById(R.id.totalNumberOfSongsTextView);
        statsSinceTextView = findViewById(R.id.statsSinceTextView);

        long start = System.currentTimeMillis();

        try {
            Song mostPopularSong = dbHelper.getMostPopularSong();
            favoriteSongTextView.setText("The most popular song: " + mostPopularSong.getName());
        }
        catch (Exception e) {
            favoriteSongTextView.setText("The most popular song: None");
            e.printStackTrace();
        }

        System.out.println("MOST PLAYED: " + (System.currentTimeMillis() - start));

        try {
            Song mostUnpopularSong = dbHelper.getMostUnpopularSong();
            mostUnpopularSongTextView.setText("The most unpopular song: " + mostUnpopularSong.getName());
        }
        catch (Exception e) {
            mostUnpopularSongTextView.setText("The most unpopular song: None");
            e.printStackTrace();
        }

        totalPlayedTime = 0L;

        try {
            totalPlayedTime = dbHelper.getTotalPlayedTime();
            totalTimeListenedTextView.setText("Total listened time: " + MusicActivity.convertStatisticsTime(totalPlayedTime));
        }
        catch (Exception e) {
            totalTimeListenedTextView.setText("Total listened time: None");
            e.printStackTrace();
        }

        try {
            Integer totalNumberOfSongs = dbHelper.getTotalNumberOfSongs();
            totalNumberOfSongsTextView.setText("Total number of songs: " + String.format(Locale.US, "%,d", totalNumberOfSongs));
        }
        catch (Exception e) {
            totalNumberOfSongsTextView.setText("Total number of songs: 0");
        }

        System.out.println("TOTAL PLAYED: " + (System.currentTimeMillis() - start));

        Integer totalLaunchedTimes = 0;

        try {
            totalLaunchedTimes = dbHelper.getTotalLaunchedTimes();
            totalLaunchedTimesTextView.setText("Total launched times: " + String.format(Locale.US, "%,d", totalLaunchedTimes));
        }
        catch (Exception e) {
            totalLaunchedTimesTextView.setText("Total launched times: None");
            e.printStackTrace();
        }

        System.out.println("TOTAL LAUNCHED: " + (System.currentTimeMillis() - start));

        try {
            Long averagePlayedTime = dbHelper.getAveragePlayTime();
            averageTimeListenedTextView.setText("Average listened time: " + MusicActivity.convertStatisticsTime(averagePlayedTime));
        }
        catch (Exception e) {
            averageTimeListenedTextView.setText("Average listened time: None");
            e.printStackTrace();
        }

        System.out.println("AVERAGE PLAYED: " + (System.currentTimeMillis() - start));

        try {
            Float averageLaunchedTimes = dbHelper.getAverageLaunchTime();
            averageLaunchedTimesTextView.setText("Average launched times: " + String.format(Locale.US, "%,.2f", averageLaunchedTimes));
        }
        catch (Exception e) {
            averageLaunchedTimesTextView.setText("Average launched times: None");
            e.printStackTrace();
        }

        try {
            long playedTimePerLaunch = totalPlayedTime / totalLaunchedTimes;
            playedTimePerLaunchTextView.setText("Played time per launch: " + MusicActivity.convertStatisticsTime(playedTimePerLaunch));
        }
        catch (Exception e) {
            playedTimePerLaunchTextView.setText("Played time per launch: 0s");
            e.printStackTrace();
        }

        try {
            Artist mostPopularArtist = dbHelper.getMostPopularArtist();
            favoriteArtistTextView.setText(String.format("The most popular artist: %s", mostPopularArtist.getArtistName()));
        }
        catch (Exception e) {
            favoriteArtistTextView.setText(String.format("The most popular artist: %s", "None"));
            e.printStackTrace();
        }

        try {
            Artist mostUnpopularArtist = dbHelper.getMostUnpopularArtist();
            mostUnpopularArtistTextView.setText(String.format("The most unpopular artist: %s", mostUnpopularArtist.getArtistName()));
        }
        catch (Exception e) {
            mostUnpopularArtistTextView.setText(String.format("The most unpopular artist: %s", "None"));
            e.printStackTrace();
        }

        try {
            Song oldestSong = dbHelper.getTheOldestSong();
            statsSinceTextView.setText("Stats since: " + oldestSong.getLocalCreatedAt());
        }
        catch (Exception e) {
            statsSinceTextView.setText("Stats since: None");
            e.printStackTrace();
        }

        songStatsButton.setOnClickListener(v -> startActivity(new Intent(this, SongStatsActivity.class)));

        artistStatsButton.setOnClickListener(v -> startActivity(new Intent(this, ArtistStatsActivity.class)));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (resultCode == Activity.RESULT_OK && resultData != null) {
            switch (requestCode) {
                case REQUEST_CODE_CHOOSE_SAVE_FILE:
                    Uri saveUri = resultData.getData();

                    ArrayList<Song> listToSave = dbHelper.selectAllSongs(DatabaseHelper.SortType.ASCENDING,
                            DatabaseHelper.SONG_NAME_COLUMN);

                    DocumentFile chosenSaveFile = DocumentFile.fromSingleUri(this, saveUri);
                    String json = new Gson().toJson(listToSave);

                    OutputStream outputStream;

                    try {
                        if (chosenSaveFile == null) {
                            throw new Exception("ERROR: Couldn't get file to save");
                        }

                        outputStream = getContentResolver().openOutputStream(saveUri, "wt");
                        outputStream.write(json.getBytes(StandardCharsets.UTF_8));
                        outputStream.close();

                        Toast.makeText(this, "Statistics were saved to file " + chosenSaveFile.getName(), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }

                    break;
                case REQUEST_CODE_CHOOSE_LOAD_FILE:
                    Uri loadUri = resultData.getData();

                    DocumentFile chosenLoadFile = DocumentFile.fromSingleUri(this, loadUri);

                    try {
                        if (chosenLoadFile == null) {
                            throw new Exception("Couldn't get load file");
                        }

                        long start = System.currentTimeMillis();

                        InputStream inputStream = getContentResolver().openInputStream(loadUri);
                        Scanner s = new Scanner(inputStream).useDelimiter("\\A");
                        String loadedJson = s.hasNext() ? s.next() : "";
                        System.out.println(loadedJson);
                        ArrayList<Song> loadedList = new Gson().fromJson(loadedJson, new TypeToken<ArrayList<Song>>(){}.getType());

                        dbHelper.recreateDatabaseWithData(loadedList);

                        System.out.println(System.currentTimeMillis() - start + " LOAD FROM FILE TIME");

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