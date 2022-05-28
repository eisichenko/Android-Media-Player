package com.example.android_media_player.MusicPlayer;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_media_player.Helpers.ColorPickerHelper;
import com.example.android_media_player.Helpers.DatabaseHelper;
import com.example.android_media_player.Helpers.MediaPlayerHelper;
import com.example.android_media_player.Helpers.MediaStoreHelper;
import com.example.android_media_player.Helpers.PathHelper;
import com.example.android_media_player.MainActivity;
import com.example.android_media_player.MusicPlayer.Adapters.RecyclerViewAdapter;
import com.example.android_media_player.MusicPlayer.Models.Song;
import com.example.android_media_player.MusicPlayer.NotificationReceivers.HeadSetBroadcastReceiver;
import com.example.android_media_player.MusicPlayer.NotificationReceivers.NextSongNotificationReceiver;
import com.example.android_media_player.MusicPlayer.NotificationReceivers.PlayNotificationReceiver;
import com.example.android_media_player.MusicPlayer.NotificationReceivers.PrevSongNotificationReceiver;
import com.example.android_media_player.MusicPlayer.NotificationReceivers.SettingsContentObserver;
import com.example.android_media_player.R;
import com.example.android_media_player.ThemeType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Stack;

public class MusicActivity extends AppCompatActivity {

    TextView songNameTextView;
    TextView currentTimeTextView;
    TextView totalTimeTextView;
    TextView noneTextView;
    TextView nowPlayingTextView;
    TextView hiddenTextView;
    TextView currentVolumeTextView;
    SeekBar musicSeekBar;
    RecyclerView songsRecyclerView;
    ImageView prevSongImageView;
    ImageView back5ImageView;
    ImageView playImageView;
    ImageView forward5ImageView;
    ImageView nextSongImageView;

    public static MediaPlayer mediaPlayer;
    public static final Handler handler = new Handler();
    public static Runnable runnable;

    public static ArrayList<Song> songList;
    public static Stack<Song> playedSongs;
    public static Integer selectedPosition;
    public static Song currentSong;

    public static final int NOTIFICATION_CODE = 0;
    public static final int OPEN_MUSIC_CODE = 1;
    public static final int PLAY_NOTIFICATION_CODE = 2;
    public static final int PREV_NOTIFICATION_CODE = 3;
    public static final int NEXT_NOTIFICATION_CODE = 4;

    public static Boolean isAutoplayEnabled = true;
    public static Boolean isListHidden = false;
    public static Boolean isRepeatEnabled = false;
    public static Boolean isActivityPaused = false;
    public static Boolean isVolumeMuted = false;

    MenuItem hideListItem;

    public final DatabaseHelper dbHelper = new DatabaseHelper(this);
    public static final long BACKGROUND_DB_UPDATE_INTERVAL_MS = 15_000L;


    public static boolean isBackPressed = false;

    public AudioManager audioManager;
    public static int volumeBeforeMuting = 0;
    public MenuItem muteMenuItem;

    public SharedPreferences settings;

    @Override
    protected void onStart() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.cancelAll();
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.music_menu_layout, menu);

        muteMenuItem = menu.findItem(R.id.volumeMuteMenuItem);

        getApplicationContext().getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true,
                new SettingsContentObserver(this,
                        new Handler(),
                        muteMenuItem));

        IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        HeadSetBroadcastReceiver headsetReceiver = new HeadSetBroadcastReceiver();
        registerReceiver(headsetReceiver, filter);

        if (isVolumeMuted) {
            muteMenuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_volume_unmute));
        }
        else {
            muteMenuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_volume_mute));
        }

        MenuItem autoplayItem = menu.findItem(R.id.autoplayMenuItem);
        if (isAutoplayEnabled) {
            autoplayItem.setTitle("Autoplay: ON");
        }
        else {
            autoplayItem.setTitle("Autoplay: OFF");
        }

        hideListItem = menu.findItem(R.id.hideListMenuItem);
        if (isListHidden) {
            hideListItem.setTitle("Show song list");
        }
        else {
            hideListItem.setTitle("Hide song list");
        }

        MenuItem repeatItem = menu.findItem(R.id.repeatMenuItem);
        if (isRepeatEnabled) {
            repeatItem.setTitle("Repeat: ON");
        }
        else {
            repeatItem.setTitle("Repeat: OFF");
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            isBackPressed = true;
            return super.onOptionsItemSelected(item);
        }
        else if (itemId == R.id.shuffleMenuItem) {
            if (songList.size() > 0) {
                boolean wasPlaying = mediaPlayer.isPlaying();

                if (!wasPlaying) {
                    handler.removeCallbacks(runnable);
                }

                int prevPos = selectedPosition;
                Song prevSong = null;

                if (prevPos >= 0 && prevPos < songList.size()) {
                    prevSong = songList.get(prevPos);
                }

                Collections.shuffle(songList);

                if (wasPlaying){
                    if (prevSong != null) {
                        int index = songList.indexOf(prevSong);
                        Song firstSong = songList.get(0);
                        Song playingSong = songList.get(index);
                        songList.set(0, playingSong);
                        songList.set(index, firstSong);
                        selectedPosition = 0;

                        nowPlayingTextView.setText("Now playing (" + (selectedPosition + 1) + "/" + songList.size() + "):");
                    }
                    else {
                        selectedPosition = -1;
                    }
                }
                else {
                    selectedPosition = -1;
                    currentSong = null;
                }

                setAdapter(songList);

                if (!wasPlaying) {
                    try {
                        MediaPlayerHelper.resetMediaPlayer(MusicActivity.mediaPlayer);

                        if (currentSong != null){
                            MusicActivity.mediaPlayer.setDataSource(currentSong.getPath());
                            MusicActivity.mediaPlayer.prepare();
                        }

                        playImageView.setImageResource(R.drawable.ic_play);
                    } catch (Exception e) {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }

                    musicSeekBar.setProgress(0);
                    if (currentSong != null) {
                        musicSeekBar.setMax(MusicActivity.mediaPlayer.getDuration());

                        nowPlayingTextView.setText("Now playing (" + (selectedPosition + 1) + "/" + songList.size() + "):");

                        currentTimeTextView.setText("00:00");
                        totalTimeTextView.setText(MusicActivity.convertMusicTime(MusicActivity.mediaPlayer.getDuration()));
                        songNameTextView.setText(MusicActivity.currentSong.getName());
                    }
                    else {
                        nowPlayingTextView.setText("Now playing: ");

                        currentTimeTextView.setText("00:00");
                        totalTimeTextView.setText("00:00");
                        songNameTextView.setText("None");
                    }
                }
            }

            isListHidden = true;

            hideListItem.setTitle("Show song list");
            songsRecyclerView.setVisibility(View.INVISIBLE);
            hiddenTextView.setVisibility(View.VISIBLE);

            Toast.makeText(this, "Songs were shuffled", Toast.LENGTH_SHORT).show();

            return true;
        }
        else if (itemId == R.id.sortMenuItem) {
            if (songList.size() > 0) {
                boolean wasPlaying = mediaPlayer.isPlaying();

                if (!wasPlaying) {
                    handler.removeCallbacks(runnable);
                }

                int prevPos = selectedPosition;
                Song prevSong = null;

                if (prevPos >= 0 && prevPos < songList.size()) {
                    prevSong = songList.get(prevPos);
                }

                Collections.sort(songList, (song1, song2) -> song1.getName().toLowerCase().compareTo(song2.getName().toLowerCase()));

                if (wasPlaying){
                    if (prevSong != null) {
                        selectedPosition = songList.indexOf(prevSong);
                        nowPlayingTextView.setText("Now playing (" + (selectedPosition + 1) + "/" + songList.size() + "):");
                    }
                    else {
                        selectedPosition = -1;
                    }
                }
                else {
                    selectedPosition = -1;
                    currentSong = null;
                }

                setAdapter(songList);

                if (!wasPlaying) {
                    try {
                        MediaPlayerHelper.resetMediaPlayer(MusicActivity.mediaPlayer);

                        if (currentSong != null) {
                            MusicActivity.mediaPlayer.setDataSource(currentSong.getPath());
                            MusicActivity.mediaPlayer.prepare();
                        }

                        playImageView.setImageResource(R.drawable.ic_play);
                    } catch (Exception e) {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }

                    musicSeekBar.setProgress(0);

                    if (currentSong != null) {
                        musicSeekBar.setMax(MusicActivity.mediaPlayer.getDuration());

                        nowPlayingTextView.setText("Now playing (" + (selectedPosition + 1) + "/" + songList.size() + "):");

                        currentTimeTextView.setText("00:00");
                        totalTimeTextView.setText(MusicActivity.convertMusicTime(MusicActivity.mediaPlayer.getDuration()));
                        songNameTextView.setText(MusicActivity.currentSong.getName());
                    }
                    else {
                        nowPlayingTextView.setText("Now playing: ");

                        currentTimeTextView.setText("00:00");
                        totalTimeTextView.setText("00:00");
                        songNameTextView.setText("None");
                    }
                }
            }

            isListHidden = false;

            hideListItem.setTitle("Hide song list");
            songsRecyclerView.setVisibility(View.VISIBLE);
            hiddenTextView.setVisibility(View.GONE);

            Toast.makeText(this, "Songs were sorted", Toast.LENGTH_SHORT).show();

            return true;
        }
        else if (itemId == R.id.autoplayMenuItem) {
            isAutoplayEnabled = !isAutoplayEnabled;
            settings.edit().putBoolean(MainActivity.AUTOPLAY_CACHE_NAME, isAutoplayEnabled).apply();

            if (isAutoplayEnabled) {
                item.setTitle("Autoplay: ON");
                Toast.makeText(this, "Autoplay: ON", Toast.LENGTH_SHORT).show();
            }
            else {
                item.setTitle("Autoplay: OFF");
                Toast.makeText(this, "Autoplay: OFF", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        else if (itemId == R.id.repeatMenuItem) {
            isRepeatEnabled = !isRepeatEnabled;
            settings.edit().putBoolean(MainActivity.REPEAT_CACHE_NAME, isRepeatEnabled).apply();

            if (isRepeatEnabled) {
                item.setTitle("Repeat: ON");
                Toast.makeText(this, "Repeat: ON", Toast.LENGTH_SHORT).show();
            }
            else {
                item.setTitle("Repeat: OFF");
                Toast.makeText(this, "Autoplay: OFF", Toast.LENGTH_SHORT).show();
            }
        }
        else if (itemId == R.id.hideListMenuItem) {
            isListHidden = !isListHidden;

            if (isListHidden) {
                item.setTitle("Show song list");
                songsRecyclerView.setVisibility(View.INVISIBLE);
                hiddenTextView.setVisibility(View.VISIBLE);
            }
            else {
                item.setTitle("Hide song list");
                songsRecyclerView.setVisibility(View.VISIBLE);
                hiddenTextView.setVisibility(View.GONE);
            }
        }
        else if (itemId == R.id.statisticsMenuItem) {
            startActivity(new Intent(this, AllStatisticsActivity.class));
        }
        else if (itemId == R.id.volumeUpMenuItem) {
            if (isVolumeMuted) {
                isVolumeMuted = false;
                muteMenuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_volume_mute));
            }
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0);
            int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

            System.out.println("UP " + currentVolume + " " + maxVolume);

            currentVolumeTextView.setText(String.format("Volume: %d%%", Math.round((float) currentVolume / maxVolume * 100.0)));
        }
        else if (itemId == R.id.volumeDownMenuItem) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, 0);
            int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            System.out.println("DOWN " + currentVolume + " " + maxVolume);

            currentVolumeTextView.setText(String.format("Volume: %d%%", Math.round((float) currentVolume / maxVolume * 100.0)));
        }
        else if (itemId == R.id.volumeMuteMenuItem) {
            isVolumeMuted = !isVolumeMuted;

            if (isVolumeMuted) {
                volumeBeforeMuting = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_volume_unmute));
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);

                currentVolumeTextView.setText("Volume: Muted");
            }
            else {
                item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_volume_mute));
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volumeBeforeMuting, 0);

                int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                currentVolumeTextView.setText(String.format("Volume: %d%%", Math.round((float) currentVolume / maxVolume * 100.0)));
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressLint({"NotificationTrampoline", "UnspecifiedImmutableFlag"})
    void sendNotification() {
        Intent playBroadcastIntent = new Intent(this, PlayNotificationReceiver.class);
        PendingIntent playIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            playIntent = PendingIntent.getBroadcast(this,
                    PLAY_NOTIFICATION_CODE, playBroadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        }
        else {
            playIntent = PendingIntent.getBroadcast(this,
                    PLAY_NOTIFICATION_CODE, playBroadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        Intent previousBroadcastIntent = new Intent(this, PrevSongNotificationReceiver.class);
        PendingIntent previousIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            previousIntent = PendingIntent.getBroadcast(this,
                    PREV_NOTIFICATION_CODE, previousBroadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        }
        else {
            previousIntent = PendingIntent.getBroadcast(this,
                    PREV_NOTIFICATION_CODE, previousBroadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        Intent nextBroadcastIntent = new Intent(this, NextSongNotificationReceiver.class);
        PendingIntent nextIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            nextIntent = PendingIntent.getBroadcast(this,
                    NEXT_NOTIFICATION_CODE, nextBroadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        }
        else {
            nextIntent = PendingIntent.getBroadcast(this,
                    NEXT_NOTIFICATION_CODE, nextBroadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "Music notification");

        builder.setContentTitle("Music");
        if (selectedPosition >= 0 && selectedPosition < songList.size()) {
            builder.setContentText(currentSong.getName() + " (" + (selectedPosition + 1) + "/" + songList.size() + ")");
        }
        else {
            builder.setContentText(currentSong.getName());
        }
        builder.setColor(Color.parseColor("#0000ff"));
        builder.setSmallIcon(R.drawable.ic_notification);
        builder.addAction(R.mipmap.ic_launcher, "Previous", previousIntent);
        builder.addAction(R.mipmap.ic_launcher, getNotificationActionString(), playIntent);
        builder.addAction(R.mipmap.ic_launcher, "Next", nextIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_CODE, builder.build());
    }

    @Override
    protected void onPause() {
        isActivityPaused = true;

        if (currentSong != null) {
            try {
                handler.removeCallbacks(runnable);

                dbHelper.modifyPlayedTime(currentSong, currentSong.getPlayedTime());

                if (mediaPlayer.isPlaying()) {
                    handler.post(runnable);
                }

                System.out.println(currentSong + " played " + currentSong.getPlayedTime());
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            sendNotification();
        }

        super.onPause();
    }


    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onResume() {
        System.out.println("MUSIC RESUME");
        isActivityPaused = false;
        Objects.requireNonNull(songsRecyclerView.getAdapter()).notifyDataSetChanged();

        if (selectedPosition >= 0 && selectedPosition < songList.size()) {
            nowPlayingTextView.setText("Now playing (" + (selectedPosition + 1) + "/" + songList.size() + "):");
            Song selectedSong = songList.get(selectedPosition);
            if (selectedSong.getName().equals(currentSong.getName())) {
                songNameTextView.setText(currentSong.getName());
            }
        }
        else {
            nowPlayingTextView.setText("Now playing: ");
        }

        if (mediaPlayer.isPlaying()) {
            playImageView.setImageResource(R.drawable.ic_pause);
        }
        else {
            playImageView.setImageResource(R.drawable.ic_play);
        }

        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        if (isVolumeMuted && currentVolume > 0) {
            isVolumeMuted = false;
        }

        if (muteMenuItem != null) {
            if (isVolumeMuted) {
                muteMenuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_volume_unmute));
            }
            else {
                muteMenuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_volume_mute));
            }
        }

        if (!isVolumeMuted) {
            currentVolumeTextView.setText(String.format("Volume: %d%%", Math.round((float) currentVolume / maxVolume * 100.0)));
        }
        else {
            currentVolumeTextView.setText("Volume: Muted");
        }

        super.onResume();
    }

    @Override
    public void onBackPressed() {
        isBackPressed = true;
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("MUSIC DESTROY");
        System.out.println(isBackPressed);
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
        isAutoplayEnabled = settings.getBoolean(MainActivity.AUTOPLAY_CACHE_NAME, true);
        isRepeatEnabled = settings.getBoolean(MainActivity.REPEAT_CACHE_NAME, false);

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
        setContentView(R.layout.activity_music);

        ColorPickerHelper.setActionBarColor(getSupportActionBar(), settings);

        System.out.println("MUSIC CREATE");

        audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

        setTitle(MainActivity.chosenFile.getName());

        songNameTextView = findViewById(R.id.songNameTextView);
        nowPlayingTextView = findViewById(R.id.nowPlayingTextView);
        noneTextView = findViewById(R.id.noneTextView);
        hiddenTextView = findViewById(R.id.hiddenTextView);
        currentVolumeTextView = findViewById(R.id.currentVolumeTextView);
        playImageView = findViewById(R.id.playImageView);
        prevSongImageView = findViewById(R.id.prevSongImageView);
        nextSongImageView = findViewById(R.id.nextSongImageView);
        forward5ImageView = findViewById(R.id.forward5ImageView);
        back5ImageView = findViewById(R.id.back5ImageView);
        currentTimeTextView = findViewById(R.id.currentTimeTextView);
        totalTimeTextView = findViewById(R.id.totalTimeTextView);
        musicSeekBar = findViewById(R.id.musicSeekBar);

        ColorPickerHelper.setSeekBarColor(musicSeekBar, settings);

        ColorPickerHelper.setImageViewColor(prevSongImageView, settings);
        ColorPickerHelper.setImageViewColor(nextSongImageView, settings);
        ColorPickerHelper.setImageViewColor(forward5ImageView, settings);
        ColorPickerHelper.setImageViewColor(back5ImageView, settings);

        songsRecyclerView = findViewById(R.id.songsRecyclerView);
        songsRecyclerView.setFocusable(false);

        if (isListHidden) {
            songsRecyclerView.setVisibility(View.INVISIBLE);
            hiddenTextView.setVisibility(View.VISIBLE);
        }

        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        if (!isVolumeMuted) {
            currentVolumeTextView.setText(String.format("Volume: %d%%", Math.round((float) currentVolume / maxVolume * 100.0)));
        }
        else {
            currentVolumeTextView.setText("Volume: Muted");
        }

        songNameTextView.setText("None");

        long start = System.currentTimeMillis();

        System.out.println("URI " + MainActivity.chosenFile.getUri());
        String folderPath = PathHelper.getAbsolutePathStringFromUri(MainActivity.chosenFile.getUri());
        folderPath = PathHelper.addSlash(folderPath);

        System.out.println("MUSIC FOLDER PATH " + folderPath);

        ArrayList<Song> newSongList = MediaStoreHelper.getSongList(this, folderPath);

        System.out.println(newSongList);

        if (songList == null) {
            songList = newSongList;
            playedSongs = new Stack<>();
        }
        else {
            HashSet<Song> newSet = new HashSet<>(newSongList);
            HashSet<Song> oldSet = new HashSet<>(songList);
            if (!newSet.equals(oldSet)) {
                songList = newSongList;
                playedSongs = new Stack<>();
            }
        }

        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build());
        }

        if (selectedPosition == null || mediaPlayer == null) {
            selectedPosition = -1;
        }
        else {
            if (mediaPlayer.isPlaying()) {
                playImageView.setImageResource(R.drawable.ic_pause);
            }
            else {
                playImageView.setImageResource(R.drawable.ic_play);
            }

            if (currentSong == null) {
                songNameTextView.setText("None");
                musicSeekBar.setMax(0);
                musicSeekBar.setProgress(0);

                currentTimeTextView.setText("00:00");
                totalTimeTextView.setText("00:00");
            }
            else {
                handler.removeCallbacks(runnable);
                songNameTextView.setText(currentSong.getName());

                selectedPosition = songList.indexOf(currentSong);
                if (selectedPosition >= 0 && selectedPosition < songList.size()) {
                    nowPlayingTextView.setText("Now playing (" + (selectedPosition + 1) + "/" + songList.size() + "):");

                    songsRecyclerView.post(() -> songsRecyclerView.scrollToPosition(selectedPosition));
                }

                musicSeekBar.setMax(mediaPlayer.getDuration());
                musicSeekBar.setProgress(mediaPlayer.getCurrentPosition());

                currentTimeTextView.setText(convertMusicTime(mediaPlayer.getCurrentPosition()));
                totalTimeTextView.setText(convertMusicTime(mediaPlayer.getDuration()));
                handler.post(runnable);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel("Music notification", "Music notification", NotificationManager.IMPORTANCE_DEFAULT);
                NotificationManager manager = getSystemService(NotificationManager.class);
                manager.createNotificationChannel(channel);
            }
        }

        System.out.println("GET SONGS: " + (System.currentTimeMillis() - start));

        if (songList.size() == 0) {
            noneTextView.setVisibility(View.VISIBLE);
            songsRecyclerView.setVisibility(View.GONE);
        }

        DividerItemDecoration decoration = new DividerItemDecoration(getApplicationContext(),
                DividerItemDecoration.VERTICAL);
        decoration.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(this, R.drawable.list_divider)));

        songsRecyclerView.addItemDecoration(decoration);

        playImageView.setOnClickListener(v -> {
            if (currentSong == null) {
                Toast.makeText(this, "Nothing to play :(", Toast.LENGTH_SHORT).show();
                return;
            }
            if (mediaPlayer.isPlaying()) {
                dbHelper.modifyPlayedTime(currentSong, currentSong.getPlayedTime());

                mediaPlayer.pause();
                playImageView.setImageResource(R.drawable.ic_play);
                handler.removeCallbacks(runnable);
            }
            else {
                if (currentSong != null) {
                    try {
                        Song dbSong = dbHelper.findSong(currentSong.getName());
                        currentSong.setPlayedTime(dbSong.getPlayedTime());
                        dbHelper.modifyPlayedTime(currentSong, currentSong.getPlayedTime());
                    }
                    catch (Exception e) {
                        dbHelper.add(currentSong);
                    }
                }

                playImageView.setImageResource(R.drawable.ic_pause);
                musicSeekBar.setMax(mediaPlayer.getDuration());
                totalTimeTextView.setText(convertMusicTime(mediaPlayer.getDuration()));
                mediaPlayer.start();
                handler.post(runnable);
            }
        });

        prevSongImageView.setOnClickListener(v -> {
            Song prevSong;
            int prevPos = selectedPosition;

            if (songList.size() == 0) return;

            handler.removeCallbacks(runnable);

            System.out.println(playedSongs);

            if (playedSongs.size() == 0) {
                selectedPosition--;
                if (selectedPosition < 0) selectedPosition = songList.size() - 1;
                prevSong = songList.get(selectedPosition);
                playedSongs.push(prevSong);
            }
            else {
                prevSong = playedSongs.pop();
                int index = songList.indexOf(prevSong);
                while (index == selectedPosition && playedSongs.size() > 0) {
                    prevSong = playedSongs.pop();
                    index = songList.indexOf(prevSong);
                }
                if (selectedPosition == index) {
                    selectedPosition--;
                    if (selectedPosition < 0) selectedPosition = songList.size() - 1;
                    prevSong = songList.get(selectedPosition);
                }
                else {
                    selectedPosition = index;
                }
            }

            if (prevSong != null) {
                try {
                    Song dbSong = dbHelper.findSong(prevSong.getName());
                    prevSong.setLaunchedTimes(dbSong.getLaunchedTimes() + 1);
                    prevSong.setPlayedTime(dbSong.getPlayedTime());
                    System.out.println(prevSong.getName() + " played " + prevSong.getPlayedTime());
                    dbHelper.modifyLaunchedTimes(prevSong, prevSong.getLaunchedTimes());
                }
                catch (Exception e) {
                    prevSong.setLaunchedTimes(1);
                    dbHelper.add(prevSong);
                }
            }

            if (currentSong != null) {
                System.out.println(currentSong.getName() + " played " + currentSong.getPlayedTime());
                dbHelper.modifyPlayedTime(currentSong, currentSong.getPlayedTime());
            }

            currentSong = prevSong;

            try {
                MediaPlayerHelper.resetMediaPlayer(MusicActivity.mediaPlayer);
                MusicActivity.mediaPlayer.setDataSource(prevSong.getPath());
                MusicActivity.mediaPlayer.prepare();
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            nowPlayingTextView.setText("Now playing (" + (selectedPosition + 1) + "/" + songList.size() + "):");
            playImageView.setImageResource(R.drawable.ic_pause);
            musicSeekBar.setMax(MusicActivity.mediaPlayer.getDuration());
            totalTimeTextView.setText(MusicActivity.convertMusicTime(MusicActivity.mediaPlayer.getDuration()));
            songNameTextView.setText(prevSong.getName());

            if (prevPos >= 0) {
                Objects.requireNonNull(songsRecyclerView.getAdapter()).notifyItemChanged(prevPos);
            }
            Objects.requireNonNull(songsRecyclerView.getAdapter()).notifyItemChanged(selectedPosition);

            MusicActivity.mediaPlayer.start();
            MusicActivity.handler.post(MusicActivity.runnable);
        });

        nextSongImageView.setOnClickListener(v -> {
            if (songList.size() == 0) return;

            MusicActivity.handler.removeCallbacks(MusicActivity.runnable);

            int prevPos = selectedPosition;

            selectedPosition = (selectedPosition + 1) % songList.size();

            Song nextSong = songList.get(selectedPosition);

            if (nextSong != null) {
                try {
                    Song dbSong = dbHelper.findSong(nextSong.getName());
                    nextSong.setLaunchedTimes(dbSong.getLaunchedTimes() + 1);
                    nextSong.setPlayedTime(dbSong.getPlayedTime());
                    System.out.println(nextSong.getName() + " played " + nextSong.getPlayedTime());
                    dbHelper.modifyLaunchedTimes(nextSong, nextSong.getLaunchedTimes());
                }
                catch (Exception e) {
                    nextSong.setLaunchedTimes(1);
                    dbHelper.add(nextSong);
                }
            }

            if (currentSong != null) {
                System.out.println(currentSong.getName() + " played " + currentSong.getPlayedTime());
                dbHelper.modifyPlayedTime(currentSong, currentSong.getPlayedTime());
            }

            currentSong = nextSong;
            playedSongs.push(currentSong);

            try {
                MediaPlayerHelper.resetMediaPlayer(MusicActivity.mediaPlayer);
                MusicActivity.mediaPlayer.setDataSource(nextSong.getPath());
                MusicActivity.mediaPlayer.prepare();
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            nowPlayingTextView.setText("Now playing (" + (selectedPosition + 1) + "/" + songList.size() + "):");
            playImageView.setImageResource(R.drawable.ic_pause);
            musicSeekBar.setMax(MusicActivity.mediaPlayer.getDuration());
            totalTimeTextView.setText(MusicActivity.convertMusicTime(MusicActivity.mediaPlayer.getDuration()));
            songNameTextView.setText(nextSong.getName());

            if (prevPos >= 0) {
                Objects.requireNonNull(songsRecyclerView.getAdapter()).notifyItemChanged(prevPos);
            }
            Objects.requireNonNull(songsRecyclerView.getAdapter()).notifyItemChanged(selectedPosition);

            MusicActivity.mediaPlayer.start();
            MusicActivity.handler.post(MusicActivity.runnable);

            if (isActivityPaused && currentSong != null) {
                sendNotification();
            }
        });

        System.out.println("BEFORE ADAPTER: " + (System.currentTimeMillis() - start));

        setAdapter(songList);

        System.out.println("ADAPTER: " + (System.currentTimeMillis() - start));

        runnable = () -> {
            if (!mediaPlayer.isPlaying()) {
                handler.removeCallbacks(runnable);
                return;
            }

            if (currentSong != null) {
                currentSong.setPlayedTime(currentSong.getPlayedTime() + 1000);

                if (currentSong.getPlayedTime() - currentSong.dbTime > BACKGROUND_DB_UPDATE_INTERVAL_MS) {
                    dbHelper.modifyPlayedTime(currentSong, currentSong.getPlayedTime());
                }
            }

            musicSeekBar.setProgress(mediaPlayer.getCurrentPosition());
            currentTimeTextView.setText(convertMusicTime(mediaPlayer.getCurrentPosition()));
            handler.postDelayed(runnable, 1000);
        };

        musicSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }
                currentTimeTextView.setText(convertMusicTime(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        forward5ImageView.setOnClickListener(v -> {
            int pos = mediaPlayer.getCurrentPosition();
            pos = Math.min(pos + 5000, mediaPlayer.getDuration());
            currentTimeTextView.setText(convertMusicTime(pos));
            musicSeekBar.setProgress(pos);
            mediaPlayer.seekTo(pos);
        });

        back5ImageView.setOnClickListener(v -> {
            int pos = mediaPlayer.getCurrentPosition();
            pos = Math.max(pos - 5000, 0);
            currentTimeTextView.setText(convertMusicTime(pos));
            musicSeekBar.setProgress(pos);
            mediaPlayer.seekTo(pos);
        });

        mediaPlayer.setOnCompletionListener(mediaPlayer -> {
            if (isRepeatEnabled && songList.size() > 0) {
                handler.removeCallbacks(runnable);
                musicSeekBar.setProgress(0);
                currentTimeTextView.setText("00:00");

                if (currentSong != null) {
                    try {
                        Song dbSong = dbHelper.findSong(currentSong.getName());
                        currentSong.setLaunchedTimes(dbSong.getLaunchedTimes() + 1);
                        System.out.println(currentSong.getName() + " played " + currentSong.getPlayedTime());
                        dbHelper.modifyLaunchedTimes(currentSong, currentSong.getLaunchedTimes());
                        dbHelper.modifyPlayedTime(currentSong, currentSong.getPlayedTime());
                    }
                    catch (Exception e) {
                        currentSong.setLaunchedTimes(1);
                        dbHelper.add(currentSong);
                    }
                }

                try {
                    MediaPlayerHelper.resetMediaPlayer(MusicActivity.mediaPlayer);
                    mediaPlayer.setDataSource(currentSong.getPath());
                    mediaPlayer.prepare();
                } catch (Exception e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

                mediaPlayer.start();
                handler.post(runnable);
            }
            else if (isAutoplayEnabled && songList.size() > 0) {
                nextSongImageView.callOnClick();
            }
            else {
                playImageView.setImageResource(R.drawable.ic_play);
                handler.removeCallbacks(runnable);
                musicSeekBar.setProgress(0);
                currentTimeTextView.setText("00:00");
            }
        });

        System.out.println("END: " + (System.currentTimeMillis() - start));
    }

    public void setAdapter(ArrayList<Song> songs) {
        RecyclerViewAdapter songsAdapter = new RecyclerViewAdapter(songs);
        RecyclerView.LayoutManager songsLayoutManager = new LinearLayoutManager(getApplicationContext());

        songsRecyclerView.setLayoutManager(songsLayoutManager);
        songsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        songsRecyclerView.setAdapter(songsAdapter);
    }

    public static String convertMusicTime(int time) {
        int minutes = (int) Math.round(time / 1000.0) / 60;
        int seconds = (int) (Math.round(time / 1000.0) % 60);
        return String.format("%02d:%02d", minutes, seconds);
    }

    public static String convertStatisticsTime(long time) {
        int seconds = (int)(time / 1000) % 60;
        int minutes = (int)(time / (1000 * 60)) % 60;
        int hours = (int)(time / (1000 * 60 * 60)) % 24;
        int days = (int)(time / (1000 * 60 * 60 * 24));

        if (days > 0) {
            return String.format("%02dd %02dh:%02dm:%02ds", days, hours, minutes, seconds);
        }
        else if (hours > 0) {
            return String.format("%02dh:%02dm:%02ds", hours, minutes, seconds);
        }
        else if (minutes > 0) {
            return String.format("%02dm:%02ds", minutes, seconds);
        }
        else if (seconds > 0) {
            return String.format("%02ds", seconds);
        }
        else {
            return "0s";
        }
    }

    public static String getNotificationActionString() {
        if (mediaPlayer.isPlaying()) {
            return "Pause";
        }
        return "Play";
    }
}