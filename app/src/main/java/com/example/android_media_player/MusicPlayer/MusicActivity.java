package com.example.android_media_player.MusicPlayer;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_media_player.MainActivity;
import com.example.android_media_player.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;
import java.util.Stack;

public class MusicActivity extends AppCompatActivity {

    TextView songNameTextView;
    TextView currentTimeTextView;
    TextView totalTimeTextView;
    TextView noneTextView;
    SeekBar musicSeekBar;
    RecyclerView songsRecyclerView;
    ImageView prevSongImageView;
    ImageView back5ImageView;
    ImageView playImageView;
    ImageView forward5ImageView;
    ImageView nextSongImageView;

    static MediaPlayer mediaPlayer;
    static Handler handler = new Handler();
    static Runnable runnable;

    static ArrayList<Song> songList;
    static Stack<Song> playedSongs;
    static Integer selectedPosition;
    static Song currentSong;

    static final int NOTIFICATION_CODE = 0;
    static final int OPEN_MUSIC_CODE = 1;
    static final int PLAY_NOTIFICATION_CODE = 2;
    static final int PREV_NOTIFICATION_CODE = 3;
    static final int NEXT_NOTIFICATION_CODE = 4;

    float xDown, yDown, xUp, yUp;

    static Boolean isAutoplayEnabled = true;

    public static SharedPreferences settings;
    public final String APP_PREFERENCES_NAME = "media_player_settings";
    public final String AUTOPLAY_CACHE_NAME = "autoplay";


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                xDown = event.getX();
                yDown = event.getY();
                return true;
            case MotionEvent.ACTION_UP:
                xUp = event.getX();
                yUp = event.getY();
                if (xDown < xUp) {
                    prevSongImageView.callOnClick();
                }
                else {
                    nextSongImageView.callOnClick();
                }
                return true;
        }
        return super.onTouchEvent(event);
    }

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

        MenuItem autoplayItem = menu.findItem(R.id.autoplayMenuItem);
        if (isAutoplayEnabled) {
            autoplayItem.setTitle("Autoplay: ON");
        }
        else {
            autoplayItem.setTitle("Autoplay: OFF");
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.shuffleMenuItem) {
            if (songList.size() > 0) {
                Boolean wasPlaying = mediaPlayer.isPlaying();
                Collections.shuffle(songList);
                selectedPosition = 0;
                currentSong = songList.get(selectedPosition);
                setAdapter(songList);

                try {
                    MusicActivity.mediaPlayer.reset();
                    MusicActivity.mediaPlayer.setDataSource(currentSong.getPath());
                    MusicActivity.mediaPlayer.prepare();
                    if (wasPlaying) {
                        playImageView.setImageResource(R.drawable.ic_pause);
                        mediaPlayer.start();
                    }
                    else {
                        playImageView.setImageResource(R.drawable.ic_play);
                    }
                } catch (Exception e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

                musicSeekBar.setProgress(0);
                musicSeekBar.setMax(MusicActivity.mediaPlayer.getDuration());

                currentTimeTextView.setText("00:00");
                totalTimeTextView.setText(MusicActivity.convertTime(MusicActivity.mediaPlayer.getDuration()));
                songNameTextView.setText(MusicActivity.currentSong.getName());
            }
            return true;
        }
        else if (itemId == R.id.sortMenuItem) {
            if (songList.size() > 0) {
                Boolean wasPlaying = mediaPlayer.isPlaying();
                Collections.sort(songList, (song1, song2) -> song1.getName().compareTo(song2.getName()));
                selectedPosition = 0;
                currentSong = songList.get(selectedPosition);
                setAdapter(songList);

                try {
                    MusicActivity.mediaPlayer.reset();
                    MusicActivity.mediaPlayer.setDataSource(currentSong.getPath());
                    MusicActivity.mediaPlayer.prepare();
                    if (wasPlaying) {
                        playImageView.setImageResource(R.drawable.ic_pause);
                        mediaPlayer.start();
                    }
                    else {
                        playImageView.setImageResource(R.drawable.ic_play);
                    }
                } catch (Exception e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

                musicSeekBar.setProgress(0);
                musicSeekBar.setMax(MusicActivity.mediaPlayer.getDuration());

                currentTimeTextView.setText("00:00");
                totalTimeTextView.setText(MusicActivity.convertTime(MusicActivity.mediaPlayer.getDuration()));
                songNameTextView.setText(MusicActivity.currentSong.getName());
            }
            return true;
        }
        else if (itemId == R.id.autoplayMenuItem) {
            isAutoplayEnabled = !isAutoplayEnabled;
            settings.edit().putBoolean(AUTOPLAY_CACHE_NAME, isAutoplayEnabled).apply();

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

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        if (currentSong != null) {
            Intent activityIntent = new Intent(this, MusicActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(this, OPEN_MUSIC_CODE,
                    activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent playBroadcastIntent = new Intent(this, PlayNotificationReceiver.class);
            PendingIntent playIntent = PendingIntent.getBroadcast(this,
                    PLAY_NOTIFICATION_CODE, playBroadcastIntent, 0);

            Intent previousBroadcastIntent = new Intent(this, PrevSongNotificationReceiver.class);
            PendingIntent previousIntent = PendingIntent.getBroadcast(this,
                    PREV_NOTIFICATION_CODE, previousBroadcastIntent, 0);

            Intent nextBroadcastIntent = new Intent(this, NextSongNotificationReceiver.class);
            PendingIntent nextIntent = PendingIntent.getBroadcast(this,
                    NEXT_NOTIFICATION_CODE, nextBroadcastIntent, 0);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "Music notification");

            builder.setContentTitle("Music");
            builder.setContentText(currentSong.getName());
            builder.setColor(Color.parseColor("#0000ff"));
            builder.setSmallIcon(R.drawable.ic_notification);
            builder.addAction(R.mipmap.ic_launcher, "Previous", previousIntent);
            builder.addAction(R.mipmap.ic_launcher, getNotificationActionString(), playIntent);
            builder.addAction(R.mipmap.ic_launcher, "Next", nextIntent);
            builder.setContentIntent(contentIntent);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(NOTIFICATION_CODE, builder.build());
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        settings = getSharedPreferences(APP_PREFERENCES_NAME, Context.MODE_PRIVATE);
        isAutoplayEnabled = settings.getBoolean(AUTOPLAY_CACHE_NAME, true);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        setTitle("Playing music");

        songNameTextView = findViewById(R.id.songNameTextView);
        songsRecyclerView = findViewById(R.id.songsRecyclerView);
        prevSongImageView = findViewById(R.id.prevSongImageView);
        noneTextView = findViewById(R.id.noneTextView);
        back5ImageView = findViewById(R.id.back5ImageView);
        playImageView = findViewById(R.id.playImageView);
        forward5ImageView = findViewById(R.id.forward5ImageView);
        nextSongImageView = findViewById(R.id.nextSongImageView);
        currentTimeTextView = findViewById(R.id.currentTimeTextView);
        totalTimeTextView = findViewById(R.id.totalTimeTextView);
        musicSeekBar = findViewById(R.id.musicSeekBar);

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
                songNameTextView.setText(currentSong.getName());
                musicSeekBar.setMax(mediaPlayer.getDuration());
                musicSeekBar.setProgress(mediaPlayer.getCurrentPosition());

                currentTimeTextView.setText(convertTime(mediaPlayer.getCurrentPosition()));
                totalTimeTextView.setText(convertTime(mediaPlayer.getDuration()));
                handler.post(runnable);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel("Music notification", "Music notification", NotificationManager.IMPORTANCE_DEFAULT);
                NotificationManager manager = getSystemService(NotificationManager.class);
                manager.createNotificationChannel(channel);
            }
        }

        songList = new ArrayList<>();
        playedSongs = new Stack<>();

        long start = System.currentTimeMillis();

        String[] projection = { MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DISPLAY_NAME };
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0 AND " +
                MediaStore.Audio.Media.DATA + " LIKE '" + getAbsolutePathStringFromUri(MainActivity.chosenFile.getUri()) + "%'";

        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection,
                selection, null, MediaStore.Audio.Media.DISPLAY_NAME + " ASC");

        if (cursor != null) {
            while(cursor.moveToNext()) {
                String path = cursor.getString(0);
                String name = cursor.getString(1);
                System.out.println("================");
                System.out.println(path);
                System.out.println(name);
                songList.add(new Song(path, name));
            }
            cursor.close();
        }

        System.out.println("NEW FILTER: " + (System.currentTimeMillis() - start));

        if (songList.size() == 0) {
            noneTextView.setVisibility(View.VISIBLE);
            songsRecyclerView.setVisibility(View.GONE);
        }

        Collections.sort(songList, (song1, song2) -> song1.getName().compareTo(song2.getName()));

        DividerItemDecoration decoration = new DividerItemDecoration(getApplicationContext(),
                DividerItemDecoration.VERTICAL);
        decoration.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(getApplicationContext(), R.drawable.list_divider)));

        songsRecyclerView.addItemDecoration(decoration);

        playImageView.setOnClickListener(v -> {;
            if (selectedPosition == -1) {
                Toast.makeText(this, "Nothing to play :(", Toast.LENGTH_SHORT).show();
                return;
            }
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                playImageView.setImageResource(R.drawable.ic_play);
                handler.removeCallbacks(runnable);
            }
            else {
                playImageView.setImageResource(R.drawable.ic_pause);
                musicSeekBar.setMax(mediaPlayer.getDuration());
                totalTimeTextView.setText(convertTime(mediaPlayer.getDuration()));
                mediaPlayer.start();
                handler.post(runnable);
            }
        });

        prevSongImageView.setOnClickListener(v -> {
            Song prevSong;

            if (songList.size() == 0) return;

            if (playedSongs.size() == 0) {
                selectedPosition--;
                if (selectedPosition < 0) selectedPosition = songList.size() - 1;
                prevSong = songList.get(selectedPosition);
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

            currentSong = prevSong;

            try {
                MusicActivity.mediaPlayer.reset();
                MusicActivity.mediaPlayer.setDataSource(prevSong.getPath());
                MusicActivity.mediaPlayer.prepare();
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            playImageView.setImageResource(R.drawable.ic_pause);
            musicSeekBar.setMax(MusicActivity.mediaPlayer.getDuration());
            totalTimeTextView.setText(MusicActivity.convertTime(MusicActivity.mediaPlayer.getDuration()));
            songNameTextView.setText(prevSong.getName());

            songsRecyclerView.getAdapter().notifyDataSetChanged();

            MusicActivity.mediaPlayer.start();
            MusicActivity.handler.post(MusicActivity.runnable);
        });

        nextSongImageView.setOnClickListener(v -> {
            if (songList.size() == 0) return;

            selectedPosition = (selectedPosition + 1) % songList.size();

            Song nextSong = songList.get(selectedPosition);
            currentSong = nextSong;

            try {
                MusicActivity.mediaPlayer.reset();
                MusicActivity.mediaPlayer.setDataSource(nextSong.getPath());
                MusicActivity.mediaPlayer.prepare();
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            playImageView.setImageResource(R.drawable.ic_pause);
            musicSeekBar.setMax(MusicActivity.mediaPlayer.getDuration());
            totalTimeTextView.setText(MusicActivity.convertTime(MusicActivity.mediaPlayer.getDuration()));
            songNameTextView.setText(nextSong.getName());

            songsRecyclerView.getAdapter().notifyDataSetChanged();

            MusicActivity.mediaPlayer.start();
            MusicActivity.handler.post(MusicActivity.runnable);
        });

        System.out.println("BEFORE ADAPTER: " + (System.currentTimeMillis() - start));

        setAdapter(songList);

        System.out.println("ADAPTER: " + (System.currentTimeMillis() - start));

        runnable = () -> {
            musicSeekBar.setProgress(mediaPlayer.getCurrentPosition());
            currentTimeTextView.setText(convertTime(mediaPlayer.getCurrentPosition()));
            handler.postDelayed(runnable, 1000);
        };

        musicSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }
                currentTimeTextView.setText(convertTime(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        forward5ImageView.setOnClickListener(v -> {
            if (songList.size() == 0) return;

            if (mediaPlayer.isPlaying()) {
                int pos = mediaPlayer.getCurrentPosition();
                pos = Math.min(pos + 5000, mediaPlayer.getDuration());
                currentTimeTextView.setText(convertTime(pos));
                musicSeekBar.setProgress(pos);
                mediaPlayer.seekTo(pos);
            }
        });

        back5ImageView.setOnClickListener(v -> {
            if (songList.size() == 0) return;

            if (mediaPlayer.isPlaying()) {
                int pos = mediaPlayer.getCurrentPosition();
                pos = Math.max(pos - 5000, 0);
                currentTimeTextView.setText(convertTime(pos));
                musicSeekBar.setProgress(pos);
                mediaPlayer.seekTo(pos);
            }
        });

        mediaPlayer.setOnCompletionListener(mediaPlayer -> {
            if (isAutoplayEnabled) {
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

    public static String convertTime(int time) {
        return String.format("%02d:%02d", time / 60_000, (time / 1000) % 60);
    }

    public static String getNotificationActionString() {
        if (mediaPlayer.isPlaying()) {
            return "Pause";
        }
        return "Play";
    }
}