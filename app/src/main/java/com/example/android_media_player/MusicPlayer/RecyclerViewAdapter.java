package com.example.android_media_player.MusicPlayer;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_media_player.R;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private ArrayList<Song> songList;
    private Context context;
    private ImageView playImageView;
    private SeekBar musicSeekBar;
    private TextView totalTimeTextView;
    private TextView songNameTextView;

    public RecyclerViewAdapter(ArrayList<Song> songList) {
        this.songList = songList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView songItemTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            songItemTextView = itemView.findViewById(R.id.songItemTextView);

            itemView.setOnClickListener(v -> {
                MusicActivity.currentSong = songList.get(getAdapterPosition());
                MusicActivity.playedSongs.push(MusicActivity.currentSong);
                MusicActivity.selectedPosition = getAdapterPosition();
                try {
                    MusicActivity.mediaPlayer.reset();
                    MusicActivity.mediaPlayer.setDataSource(context, MusicActivity.currentSong.getUri());
                    MusicActivity.mediaPlayer.prepare();
                } catch (Exception e) {
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                playImageView.setImageResource(R.drawable.ic_pause);
                musicSeekBar.setMax(MusicActivity.mediaPlayer.getDuration());

                totalTimeTextView.setText(MusicActivity.convertTime(MusicActivity.mediaPlayer.getDuration()));
                songNameTextView.setText(MusicActivity.currentSong.getName());

                MusicActivity.mediaPlayer.start();
                MusicActivity.handler.post(MusicActivity.runnable);
                notifyDataSetChanged();
            });
        }
    }

    @NonNull
    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        playImageView = parent.getRootView().findViewById(R.id.playImageView);
        musicSeekBar = parent.getRootView().findViewById(R.id.musicSeekBar);
        totalTimeTextView = parent.getRootView().findViewById(R.id.totalTimeTextView);
        songNameTextView = parent.getRootView().findViewById(R.id.songNameTextView);
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
        Song song = songList.get(position);
        holder.songItemTextView.setText(song.getName());
        if (MusicActivity.selectedPosition != null && position == MusicActivity.selectedPosition) {
            holder.songItemTextView.setTextColor(Color.parseColor("#000000"));
            holder.songItemTextView.setTypeface(Typeface.DEFAULT_BOLD);
        }
        else {
            holder.songItemTextView.setTextColor(Color.parseColor("#444444"));
            holder.songItemTextView.setTypeface(Typeface.DEFAULT);
        }
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }


}
