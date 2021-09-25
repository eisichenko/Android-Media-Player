package com.example.android_media_player.MusicPlayer;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
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
import java.util.Objects;
import java.util.Stack;

public class MusicActivity extends AppCompatActivity {

    TextView songNameTextView;
    TextView currentTimeTextView;
    TextView totalTimeTextView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        setTitle("Playing music");

        songNameTextView = findViewById(R.id.songNameTextView);
        songsRecyclerView = findViewById(R.id.songsRecyclerView);
        prevSongImageView = findViewById(R.id.prevSongImageView);
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

            musicSeekBar.setMax(mediaPlayer.getDuration());
            musicSeekBar.setProgress(mediaPlayer.getCurrentPosition());

            currentTimeTextView.setText(convertTime(mediaPlayer.getCurrentPosition()));
            totalTimeTextView.setText(convertTime(mediaPlayer.getDuration()));
            songNameTextView.setText(currentSong.getName());

            handler.post(runnable);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel("Music notification", "Music notification", NotificationManager.IMPORTANCE_DEFAULT);
                NotificationManager manager = getSystemService(NotificationManager.class);
                manager.createNotificationChannel(channel);
            }
        }

        songList = new ArrayList<>();
        playedSongs = new Stack<>();

        for (DocumentFile file : MainActivity.chosenFile.listFiles()) {
            if (file.getType().startsWith("audio")) {
                songList.add(new Song(file));
            }
        }

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
                MusicActivity.mediaPlayer.setDataSource(this, prevSong.getUri());
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
                MusicActivity.mediaPlayer.setDataSource(this, nextSong.getUri());
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

        setAdapter(songList);

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
            playImageView.setImageResource(R.drawable.ic_play);
            handler.removeCallbacks(runnable);
            musicSeekBar.setProgress(0);
            currentTimeTextView.setText("00:00");
        });
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