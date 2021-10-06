package com.example.android_media_player.MusicPlayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.android_media_player.Helpers.DatabaseHelper;
import com.example.android_media_player.MainActivity;
import com.example.android_media_player.R;
import com.example.android_media_player.ThemeType;

import java.util.ArrayList;
import java.util.Objects;

public class SongStatsActivity extends AppCompatActivity {
    TextView noneTextView;
    RecyclerView statisticsRecyclerView;

    ArrayList<Song> statisticsList;

    public static DatabaseHelper.SortType currentSortType = DatabaseHelper.SortType.DESCENDING;
    public String lastColumnName = DatabaseHelper.PLAYED_TIME_COLUMN;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.song_stats_menu, menu);

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

        if (itemId == R.id.sortOrderMenuItem) {
            if (currentSortType == DatabaseHelper.SortType.ASCENDING) {
                item.setTitle("Order: Descending");
                currentSortType = DatabaseHelper.SortType.DESCENDING;
            }
            else {
                item.setTitle("Order: Ascending");
                currentSortType = DatabaseHelper.SortType.ASCENDING;
            }
            MainActivity.settings.edit().putString(MainActivity.ORDER_CACHE_NAME, currentSortType.toString()).apply();
            statisticsList = MusicActivity.dbHelper.selectALl(currentSortType, lastColumnName);
            setAdapter(statisticsList);
        }
        else if (itemId == R.id.sortByNameMenuItem) {
            if (statisticsList.size() > 0) {
                lastColumnName = DatabaseHelper.NAME_COLUMN;

                MainActivity.settings.edit().putString(MainActivity.LAST_COLUMN_CACHE_NAME, lastColumnName).apply();

                statisticsList = MusicActivity.dbHelper.selectALl(currentSortType, lastColumnName);
                setAdapter(statisticsList);
            }
        }
        else if (itemId == R.id.sortByLaunchTimesMenuItem) {
            if (statisticsList.size() > 0) {
                lastColumnName = DatabaseHelper.LAUNCHED_TIMES_COLUMN;

                MainActivity.settings.edit().putString(MainActivity.LAST_COLUMN_CACHE_NAME, lastColumnName).apply();

                statisticsList = MusicActivity.dbHelper.selectALl(currentSortType, lastColumnName);
                setAdapter(statisticsList);
            }
        }
        else if (itemId == R.id.sortByTimeMenuItem) {
            if (statisticsList.size() > 0) {
                lastColumnName = DatabaseHelper.PLAYED_TIME_COLUMN;

                MainActivity.settings.edit().putString(MainActivity.LAST_COLUMN_CACHE_NAME, lastColumnName).apply();

                statisticsList = MusicActivity.dbHelper.selectALl(currentSortType, lastColumnName);
                setAdapter(statisticsList);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String themeString = MainActivity.settings.getString(MainActivity.THEME_CACHE_NAME, null);

        String sortTypeStr = MainActivity.settings.getString(MainActivity.ORDER_CACHE_NAME, DatabaseHelper.SortType.DESCENDING.toString());
        currentSortType = DatabaseHelper.SortType.valueOf(sortTypeStr);

        lastColumnName = MainActivity.settings.getString(MainActivity.LAST_COLUMN_CACHE_NAME, DatabaseHelper.PLAYED_TIME_COLUMN);

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
        setContentView(R.layout.activity_song_stats);

        setTitle("Song statistics");

        noneTextView = findViewById(R.id.noneTextView);
        statisticsRecyclerView = findViewById(R.id.statisticsRecyclerView);
        statisticsRecyclerView.setFocusable(false);

        DividerItemDecoration decoration = new DividerItemDecoration(getApplicationContext(),
                DividerItemDecoration.VERTICAL);
        decoration.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(this, R.drawable.list_divider)));

        statisticsRecyclerView.addItemDecoration(decoration);

        statisticsList = new ArrayList<>();

        long start = System.currentTimeMillis();

        statisticsList = MusicActivity.dbHelper.selectALl(currentSortType, lastColumnName);

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


    public void setAdapter(ArrayList<Song> songs) {
        StatisticsRecyclerViewAdapter statisticsAdapter = new StatisticsRecyclerViewAdapter(songs);
        RecyclerView.LayoutManager statisticsLayoutManager = new LinearLayoutManager(getApplicationContext());

        statisticsRecyclerView.setLayoutManager(statisticsLayoutManager);
        statisticsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        statisticsRecyclerView.setAdapter(statisticsAdapter);
    }

}