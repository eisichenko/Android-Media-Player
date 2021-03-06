package com.example.android_media_player.MusicPlayer;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_media_player.Helpers.ColorPickerHelper;
import com.example.android_media_player.Helpers.DatabaseHelper;
import com.example.android_media_player.Helpers.MediaStoreHelper;
import com.example.android_media_player.Helpers.StringHelper;
import com.example.android_media_player.MainActivity;
import com.example.android_media_player.MusicPlayer.Adapters.ArtistStatisticsRecyclerViewAdapter;
import com.example.android_media_player.MusicPlayer.Models.Artist;
import com.example.android_media_player.MusicPlayer.Models.Song;
import com.example.android_media_player.R;
import com.example.android_media_player.ThemeType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

public class ArtistStatsActivity extends AppCompatActivity {
    TextView noneTextView;
    RecyclerView statisticsRecyclerView;

    ArrayList<Artist> statisticsList;

    public static DatabaseHelper.SortType currentSortType = DatabaseHelper.SortType.DESCENDING;
    public String lastColumnName = DatabaseHelper.POPULARITY_COLUMN;
    public String currentFilterSubstring = "";

    public final DatabaseHelper dbHelper = new DatabaseHelper(this);

    public static boolean isBackPressed = false;

    public SharedPreferences settings;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.artist_stats_menu, menu);

        MenuItem orderItem = menu.findItem(R.id.sortOrderMenuItem);

        if (currentSortType == DatabaseHelper.SortType.ASCENDING) {
            orderItem.setTitle("Order: Ascending");
        }
        else {
            orderItem.setTitle("Order: Descending");
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            isBackPressed = true;
            return super.onOptionsItemSelected(item);
        }
        else if (itemId == R.id.sortOrderMenuItem) {
            if (currentSortType == DatabaseHelper.SortType.ASCENDING) {
                item.setTitle("Order: Descending");
                currentSortType = DatabaseHelper.SortType.DESCENDING;
            }
            else {
                item.setTitle("Order: Ascending");
                currentSortType = DatabaseHelper.SortType.ASCENDING;
            }
            settings.edit().putString(MainActivity.ARTIST_SORT_ORDER_CACHE_NAME, currentSortType.toString()).apply();

            if (currentFilterSubstring.length() == 0) {
                statisticsList = dbHelper.selectAllArtists(currentSortType, lastColumnName);
            }
            else {
                statisticsList = dbHelper.getArtistsBySubstring(currentFilterSubstring, currentSortType, lastColumnName);
            }

            setAdapter(statisticsList);
        }
        else if (itemId == R.id.sortByNameMenuItem) {
            if (statisticsList.size() > 0) {
                lastColumnName = DatabaseHelper.ARTIST_NAME_COLUMN;

                settings.edit().putString(MainActivity.ARTIST_SORT_LAST_COLUMN_CACHE_NAME, lastColumnName).apply();

                if (currentFilterSubstring.length() == 0) {
                    statisticsList = dbHelper.selectAllArtists(currentSortType, lastColumnName);
                }
                else {
                    statisticsList = dbHelper.getArtistsBySubstring(currentFilterSubstring, currentSortType, lastColumnName);
                }

                setAdapter(statisticsList);
            }
        }
        else if (itemId == R.id.sortByLaunchTimesMenuItem) {
            if (statisticsList.size() > 0) {
                lastColumnName = DatabaseHelper.LAUNCHED_TIMES_COLUMN;

                settings.edit().putString(MainActivity.ARTIST_SORT_LAST_COLUMN_CACHE_NAME, lastColumnName).apply();

                if (currentFilterSubstring.length() == 0) {
                    statisticsList = dbHelper.selectAllArtists(currentSortType, lastColumnName);
                }
                else {
                    statisticsList = dbHelper.getArtistsBySubstring(currentFilterSubstring, currentSortType, lastColumnName);
                }
                setAdapter(statisticsList);
            }
        }
        else if (itemId == R.id.sortByTimeMenuItem) {
            if (statisticsList.size() > 0) {
                lastColumnName = DatabaseHelper.PLAYED_TIME_COLUMN;

                settings.edit().putString(MainActivity.ARTIST_SORT_LAST_COLUMN_CACHE_NAME, lastColumnName).apply();

                if (currentFilterSubstring.length() == 0) {
                    statisticsList = dbHelper.selectAllArtists(currentSortType, lastColumnName);
                }
                else {
                    statisticsList = dbHelper.getArtistsBySubstring(currentFilterSubstring, currentSortType, lastColumnName);
                }

                setAdapter(statisticsList);
            }
        }
        else if (itemId == R.id.sortByPopularityMenuItem) {
            if (statisticsList.size() > 0) {
                lastColumnName = DatabaseHelper.POPULARITY_COLUMN;

                settings.edit().putString(MainActivity.ARTIST_SORT_LAST_COLUMN_CACHE_NAME, lastColumnName).apply();

                if (currentFilterSubstring.length() == 0) {
                    statisticsList = dbHelper.selectAllArtists(currentSortType, lastColumnName);
                }
                else {
                    statisticsList = dbHelper.getArtistsBySubstring(currentFilterSubstring, currentSortType, lastColumnName);
                }

                setAdapter(statisticsList);
            }
        }
        else if (itemId == R.id.sortByPlayedTimePerLaunchMenuItem) {
            if (statisticsList.size() > 0) {
                lastColumnName = DatabaseHelper.TIME_PER_LAUNCH_COLUMN;

                settings.edit().putString(MainActivity.ARTIST_SORT_LAST_COLUMN_CACHE_NAME, lastColumnName).apply();

                if (currentSortType == DatabaseHelper.SortType.DESCENDING) {
                    Collections.sort(statisticsList, (artist1, artist2) -> artist2.getPlayedTimePerLaunch().compareTo(artist1.getPlayedTimePerLaunch()));
                }
                else {
                    Collections.sort(statisticsList, (artist1, artist2) -> artist1.getPlayedTimePerLaunch().compareTo(artist2.getPlayedTimePerLaunch()));
                }
                setAdapter(statisticsList);
            }
        }
        else if (itemId == R.id.sortByNumberOfSongsMenuItem) {
            if (statisticsList.size() > 0) {
                lastColumnName = DatabaseHelper.NUMBER_OF_SONGS_COLUMN;

                settings.edit().putString(MainActivity.ARTIST_SORT_LAST_COLUMN_CACHE_NAME, lastColumnName).apply();

                if (currentSortType == DatabaseHelper.SortType.DESCENDING) {
                    Collections.sort(statisticsList, (artist1, artist2) -> artist2.getNumberOfSongs().compareTo(artist1.getNumberOfSongs()));
                }
                else {
                    Collections.sort(statisticsList, (artist1, artist2) -> artist1.getNumberOfSongs().compareTo(artist2.getNumberOfSongs()));
                }
                setAdapter(statisticsList);
            }
        }
        else if (itemId == R.id.filterBySubstringMenuItem) {
            final EditText editText = new EditText(this);
            editText.setHint("Filter string");

            new AlertDialog.Builder(this)
                    .setTitle("Filter string")
                    .setView(editText)
                    .setPositiveButton("Filter", (dialog, whichButton) -> {
                        currentFilterSubstring = editText.getText().toString();

                        statisticsList = dbHelper.getArtistsBySubstring(
                                currentFilterSubstring,
                                currentSortType, lastColumnName);

                        if (statisticsList.size() == 0) {
                            noneTextView.setVisibility(View.VISIBLE);
                            statisticsRecyclerView.setVisibility(View.GONE);
                        }
                        else {
                            noneTextView.setVisibility(View.GONE);
                            statisticsRecyclerView.setVisibility(View.VISIBLE);
                        }

                        setAdapter(statisticsList);
                        if (currentFilterSubstring.length() == 0) {
                            Toast.makeText(this, "Empty filter", Toast.LENGTH_SHORT).show();
                            setTitle("Artist statistics");
                        }
                        else {
                            Toast.makeText(this, "Filter was applied", Toast.LENGTH_SHORT).show();
                            setTitle("Filtered by \"" + currentFilterSubstring + "\"");
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
        else if (itemId == R.id.resetFilterMenuItem) {
            currentFilterSubstring = "";
            statisticsList = dbHelper.selectAllArtists(currentSortType, lastColumnName);

            if (statisticsList.size() == 0) {
                noneTextView.setVisibility(View.VISIBLE);
                statisticsRecyclerView.setVisibility(View.GONE);
            }
            else {
                noneTextView.setVisibility(View.GONE);
                statisticsRecyclerView.setVisibility(View.VISIBLE);
            }

            setAdapter(statisticsList);
            setTitle("Artist statistics");
            Toast.makeText(this, "Filter reset was successful", Toast.LENGTH_SHORT).show();
        }

        setTitle(StringHelper.capitalize(lastColumnName));

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        isBackPressed = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("ARTIST STATS DESTROY");
        System.out.println(ArtistStatsActivity.isBackPressed);

        if (!isBackPressed) {
            NotificationManager nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nMgr.cancelAll();
        }
        else {
            isBackPressed = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        settings = getSharedPreferences(MainActivity.APP_PREFERENCES_NAME, Context.MODE_PRIVATE);
        String themeString = settings.getString(MainActivity.THEME_CACHE_NAME, null);

        String sortTypeStr = settings.getString(MainActivity.ARTIST_SORT_ORDER_CACHE_NAME, DatabaseHelper.SortType.DESCENDING.toString());
        currentSortType = DatabaseHelper.SortType.valueOf(sortTypeStr);

        lastColumnName = settings.getString(MainActivity.ARTIST_SORT_LAST_COLUMN_CACHE_NAME, DatabaseHelper.POPULARITY_COLUMN);

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
        setContentView(R.layout.activity_artist_stats);

        ColorPickerHelper.setActionBarColor(getSupportActionBar(), settings);

        setTitle(StringHelper.capitalize(lastColumnName));

        noneTextView = findViewById(R.id.noneTextView);
        statisticsRecyclerView = findViewById(R.id.statisticsRecyclerView);
        statisticsRecyclerView.setFocusable(false);

        DividerItemDecoration decoration = new DividerItemDecoration(getApplicationContext(),
                DividerItemDecoration.VERTICAL);
        decoration.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(this, R.drawable.list_divider)));

        statisticsRecyclerView.addItemDecoration(decoration);

        statisticsList = new ArrayList<>();

        long start = System.currentTimeMillis();

        ArrayList<Song> dbSongs = dbHelper.selectAllSongs(DatabaseHelper.SortType.DESCENDING, DatabaseHelper.PLAYED_TIME_COLUMN);

        ArrayList<Song> localSongs = MediaStoreHelper.getAllSongs(this);

        HashMap<Song, String> dbSongsWithNames = new HashMap<>();

        for (Song dbSong : dbSongs) {
            dbSongsWithNames.put(dbSong, dbSong.getArtistName());
        }

        boolean showWarningToast = false;

        for (Song localSong : localSongs) {
            if (localSong.getArtistName() != null
                    && dbSongsWithNames.containsKey(localSong)
                    && !Objects.equals(dbSongsWithNames.get(localSong), localSong.getArtistName())) {
                dbHelper.modifyArtist(localSong, localSong.getArtistName());
                if (!showWarningToast) {
                    Toast.makeText(this, "Syncing artists. It may take some time...", Toast.LENGTH_LONG).show();
                    showWarningToast = true;
                }
            }
        }

        System.out.println("CHECK ARTIST: " + (System.currentTimeMillis() - start));

        statisticsList = dbHelper.selectAllArtists(currentSortType, lastColumnName);

        System.out.println("SELECT ALL: " + (System.currentTimeMillis() - start));

        if (statisticsList.size() == 0) {
            noneTextView.setVisibility(View.VISIBLE);
            statisticsRecyclerView.setVisibility(View.GONE);
        }
        else {
            noneTextView.setVisibility(View.GONE);
            statisticsRecyclerView.setVisibility(View.VISIBLE);
        }

        setAdapter(statisticsList);

        System.out.println("SET ADAPTER: " + (System.currentTimeMillis() - start));
    }


    public void setAdapter(ArrayList<Artist> artists) {
        ArtistStatisticsRecyclerViewAdapter statisticsAdapter = new ArtistStatisticsRecyclerViewAdapter(artists);
        RecyclerView.LayoutManager statisticsLayoutManager = new LinearLayoutManager(getApplicationContext());

        statisticsRecyclerView.setLayoutManager(statisticsLayoutManager);
        statisticsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        statisticsRecyclerView.setAdapter(statisticsAdapter);
    }
}