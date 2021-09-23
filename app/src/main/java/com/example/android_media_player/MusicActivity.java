package com.example.android_media_player;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Objects;

public class MusicActivity extends AppCompatActivity {

    TextView songNameTextView;
    RecyclerView songsRecyclerView;

    static ArrayList<Song> songList;
    static Integer selectedPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        setTitle("Playing music");

        songNameTextView = findViewById(R.id.songNameTextView);
        songsRecyclerView = findViewById(R.id.songsRecyclerView);

        songNameTextView.setText("None");

        songList = new ArrayList<>();

        for (DocumentFile file : MainActivity.chosenFile.listFiles()) {
            if (file.getType().startsWith("audio")) {
                songList.add(new Song(file));
            }
        }

        DividerItemDecoration decoration = new DividerItemDecoration(getApplicationContext(),
                DividerItemDecoration.VERTICAL);
        decoration.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(getApplicationContext(), R.drawable.list_divider)));

        songsRecyclerView.addItemDecoration(decoration);

        setAdapter(songList);
    }

    public void setAdapter(ArrayList<Song> songs) {
        RecyclerViewAdapter songsAdapter = new RecyclerViewAdapter(songs);
        RecyclerView.LayoutManager songsLayoutManager = new LinearLayoutManager(getApplicationContext());

        songsRecyclerView.setLayoutManager(songsLayoutManager);
        songsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        songsRecyclerView.setAdapter(songsAdapter);
    }
}