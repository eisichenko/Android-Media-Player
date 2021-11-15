package com.example.android_media_player.MusicPlayer.Adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_media_player.MusicPlayer.MusicActivity;
import com.example.android_media_player.MusicPlayer.Models.Song;
import com.example.android_media_player.R;

import java.util.ArrayList;

public class SongStatisticsRecyclerViewAdapter extends RecyclerView.Adapter<SongStatisticsRecyclerViewAdapter.ViewHolder> {
    private final ArrayList<Song> songList;
    private Context context;

    public SongStatisticsRecyclerViewAdapter(ArrayList<Song> songList) {
        this.songList = songList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView statsSongNameTextView;
        final TextView statsTotalLaunchTextView;
        final TextView totalTimeListenedTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            statsSongNameTextView = itemView.findViewById(R.id.statsSongNameTextView);
            statsTotalLaunchTextView = itemView.findViewById(R.id.statsTotalLaunchTextView);
            totalTimeListenedTextView = itemView.findViewById(R.id.totalTimeListenedTextView);
        }
    }

    @NonNull
    @Override
    public SongStatisticsRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_statistics_recycler_view_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SongStatisticsRecyclerViewAdapter.ViewHolder holder, int position) {
        Song song = songList.get(position);

        TypedValue typedValue = new TypedValue();

        context.getTheme().resolveAttribute(R.attr.main_text_color, typedValue, true);

        holder.statsSongNameTextView.setTextColor(typedValue.data);
        holder.statsSongNameTextView.setTypeface(Typeface.DEFAULT);
        holder.statsSongNameTextView.setText("#" + (position + 1) + " Name: " + song.getName());

        holder.statsTotalLaunchTextView.setTextColor(typedValue.data);
        holder.statsTotalLaunchTextView.setTypeface(Typeface.DEFAULT);
        holder.statsTotalLaunchTextView.setText("Total launched times: " + song.getLaunchedTimes().toString());

        holder.totalTimeListenedTextView.setTextColor(typedValue.data);
        holder.totalTimeListenedTextView.setTypeface(Typeface.DEFAULT);
        holder.totalTimeListenedTextView.setText("Total time listened: " + MusicActivity.convertStatisticsTime(song.getPlayedTime()));
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }
}