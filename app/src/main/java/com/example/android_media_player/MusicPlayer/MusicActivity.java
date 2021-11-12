package com.example.android_media_player.MusicPlayer;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
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

import com.example.android_media_player.Helpers.DatabaseHelper;
import com.example.android_media_player.MainActivity;
import com.example.android_media_player.R;
import com.example.android_media_player.ThemeType;

import java.util.ArrayList;
import java.util.Arrays;
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

    static MediaPlayer mediaPlayer;
    static final Handler handler = new Handler();
    static Runnable runnable;

    static ArrayList<Song> songList;
    static Stack<Song> playedSongs;
    static Integer selectedPosition;
    public static Song currentSong;

    static final int NOTIFICATION_CODE = 0;
    static final int OPEN_MUSIC_CODE = 1;
    static final int PLAY_NOTIFICATION_CODE = 2;
    static final int PREV_NOTIFICATION_CODE = 3;
    static final int NEXT_NOTIFICATION_CODE = 4;

    static Boolean isAutoplayEnabled = true;
    public static Boolean isListHidden = false;
    static Boolean isRepeatEnabled = false;
    static Boolean isActivityPaused = false;
    static Boolean isVolumeMuted = false;

    MenuItem hideListItem;

    public final DatabaseHelper dbHelper = new DatabaseHelper(this);
    public static final long BACKGROUND_DB_UPDATE_INTERVAL_MS = 15_000L;


    public static boolean isBackPressed = false;

    public AudioManager audioManager;
    public static int volumeBeforeMuting = 0;
    public MenuItem muteMenuItem;

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
                    currentSong = null;
                }

                setAdapter(songList);

                if (!wasPlaying) {
                    try {
                        MusicActivity.mediaPlayer.reset();

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
                    selectedPosition = songList.indexOf(prevSong);
                    nowPlayingTextView.setText("Now playing (" + (selectedPosition + 1) + "/" + songList.size() + "):");
                }
                else {
                    selectedPosition = -1;
                    currentSong = null;
                }

                setAdapter(songList);

                if (!wasPlaying) {
                    try {
                        MusicActivity.mediaPlayer.reset();
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
            MainActivity.settings.edit().putBoolean(MainActivity.AUTOPLAY_CACHE_NAME, isAutoplayEnabled).apply();

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
            MainActivity.settings.edit().putBoolean(MainActivity.REPEAT_CACHE_NAME, isRepeatEnabled).apply();

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

                currentVolumeTextView.setText(String.format("Volume: Muted"));
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
        Intent activityIntent = new Intent(this, OpenMusicNotificationReceiver.class);
        PendingIntent contentIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            contentIntent = PendingIntent.getBroadcast(this, OPEN_MUSIC_CODE,
                    activityIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }
        else {
            contentIntent = PendingIntent.getBroadcast(this, OPEN_MUSIC_CODE,
                    activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        Intent playBroadcastIntent = new Intent(this, PlayNotificationReceiver.class);
        PendingIntent playIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            playIntent = PendingIntent.getBroadcast(this,
                    PLAY_NOTIFICATION_CODE, playBroadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }
        else {
            playIntent = PendingIntent.getBroadcast(this,
                    PLAY_NOTIFICATION_CODE, playBroadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        Intent previousBroadcastIntent = new Intent(this, PrevSongNotificationReceiver.class);
        PendingIntent previousIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            previousIntent = PendingIntent.getBroadcast(this,
                    PREV_NOTIFICATION_CODE, previousBroadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }
        else {
            previousIntent = PendingIntent.getBroadcast(this,
                    PREV_NOTIFICATION_CODE, previousBroadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        Intent nextBroadcastIntent = new Intent(this, NextSongNotificationReceiver.class);
        PendingIntent nextIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            nextIntent = PendingIntent.getBroadcast(this,
                    NEXT_NOTIFICATION_CODE, nextBroadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }
        else {
            nextIntent = PendingIntent.getBroadcast(this,
                    NEXT_NOTIFICATION_CODE, nextBroadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "Music notification");

        builder.setContentTitle("Music");
        builder.setContentText(currentSong.getName() + " (" + (selectedPosition + 1) + "/" + songList.size() + ")");
        builder.setColor(Color.parseColor("#0000ff"));
        builder.setSmallIcon(R.drawable.ic_notification);
        builder.addAction(R.mipmap.ic_launcher, "Previous", previousIntent);
        builder.addAction(R.mipmap.ic_launcher, getNotificationActionString(), playIntent);
        builder.addAction(R.mipmap.ic_launcher, "Next", nextIntent);
        builder.setContentIntent(contentIntent);

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

    public static String getAbsolutePathStringFromUri(Uri uri) {
        ArrayList<String> strings = new ArrayList<>(Arrays.asList(uri.toString().split("/")));

        String pathString = Uri.decode(strings.get(strings.size() - 1));

        ArrayList<String> pathStrings = new ArrayList<>(Arrays.asList(pathString.split(":")));

        if (pathStrings.get(0).equals("primary")) {
            pathStrings.set(0, Environment.getExternalStorageDirectory().getPath());
        }
        else {
            pathStrings.add(0, "/storage");
        }

        return pathCombine(pathStrings);
    }

    public static String pathCombine(ArrayList<String> pathStrings) {
        StringBuilder res = new StringBuilder();

        for (String str : pathStrings) {
            if (res.length() == 0) {
                res.append(str);
            }
            else if (res.charAt(res.length() - 1) == '/') {
                res.append(str);
            }
            else {
                res.append('/').append(str);
            }
        }

        return res.toString();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onResume() {
        isActivityPaused = false;
        songsRecyclerView.getAdapter().notifyDataSetChanged();
        if (selectedPosition >= 0) {
            nowPlayingTextView.setText("Now playing (" + (selectedPosition + 1) + "/" + songList.size() + "):");
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

        isAutoplayEnabled = MainActivity.settings.getBoolean(MainActivity.AUTOPLAY_CACHE_NAME, true);
        isRepeatEnabled = MainActivity.settings.getBoolean(MainActivity.REPEAT_CACHE_NAME, false);

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
        setContentView(R.layout.activity_music);

        audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

        setTitle("Playing music");

        songNameTextView = findViewById(R.id.songNameTextView);
        nowPlayingTextView = findViewById(R.id.nowPlayingTextView);
        prevSongImageView = findViewById(R.id.prevSongImageView);
        noneTextView = findViewById(R.id.noneTextView);
        hiddenTextView = findViewById(R.id.hiddenTextView);
        currentVolumeTextView = findViewById(R.id.currentVolumeTextView);
        back5ImageView = findViewById(R.id.back5ImageView);
        playImageView = findViewById(R.id.playImageView);
        forward5ImageView = findViewById(R.id.forward5ImageView);
        nextSongImageView = findViewById(R.id.nextSongImageView);
        currentTimeTextView = findViewById(R.id.currentTimeTextView);
        totalTimeTextView = findViewById(R.id.totalTimeTextView);
        musicSeekBar = findViewById(R.id.musicSeekBar);

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
            currentVolumeTextView.setText(String.format("Volume: Muted"));
        }

        songNameTextView.setText("None");

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

        long start = System.currentTimeMillis();

        ArrayList<Song> newSongList = new ArrayList<>();

        String[] projection = { MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.ARTIST };

        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0 AND " +
                MediaStore.Audio.Media.DATA + " LIKE '" + getAbsolutePathStringFromUri(MainActivity.chosenFile.getUri()) + "%'";

        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection,
                selection, null, MediaStore.Audio.Media.DISPLAY_NAME + " COLLATE NOCASE ASC");

        if (cursor != null) {
            while(cursor.moveToNext()) {
                String path = cursor.getString(0);
                String name = cursor.getString(1);
                String artist = cursor.getString(2);

                Song newSong = new Song(path, name, artist, 0, 0L);

                newSongList.add(newSong);
            }
            cursor.close();
        }

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
            if (selectedPosition == -1) {
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

            MusicActivity.handler.removeCallbacks(MusicActivity.runnable);

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
                MusicActivity.mediaPlayer.reset();
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
                songsRecyclerView.getAdapter().notifyItemChanged(prevPos);
            }
            songsRecyclerView.getAdapter().notifyItemChanged(selectedPosition);

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
                MusicActivity.mediaPlayer.reset();
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
                songsRecyclerView.getAdapter().notifyItemChanged(prevPos);
            }
            songsRecyclerView.getAdapter().notifyItemChanged(selectedPosition);

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
                    mediaPlayer.reset();
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
        return String.format("%02d:%02d", time / 60_000, (time / 1000) % 60);
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