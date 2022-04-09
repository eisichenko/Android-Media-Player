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

import com.example.android_media_player.MusicPlayer.AllStatisticsActivity;
import com.example.android_media_player.MusicPlayer.Models.Artist;
import com.example.android_media_player.MusicPlayer.MusicActivity;
import com.example.android_media_player.R;

import java.util.ArrayList;
import java.util.Locale;

public class ArtistStatisticsRecyclerViewAdapter extends RecyclerView.Adapter<ArtistStatisticsRecyclerViewAdapter.ViewHolder> {
    private final ArrayList<Artist> artistList;
    private Context context;

    public ArtistStatisticsRecyclerViewAdapter(ArrayList<Artist> artistList) {
        this.artistList = artistList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView statsArtistNameTextView;
        final TextView statsTotalLaunchTextView;
        final TextView totalTimeListenedTextView;
        final TextView listenedTimePerLaunchTextView;
        final TextView numberOfSongsTextView;
        final TextView popularityTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            statsArtistNameTextView = itemView.findViewById(R.id.statsArtistNameTextView);
            statsTotalLaunchTextView = itemView.findViewById(R.id.statsTotalLaunchTextView);
            totalTimeListenedTextView = itemView.findViewById(R.id.totalTimeListenedTextView);
            listenedTimePerLaunchTextView = itemView.findViewById(R.id.listenedTimePerLaunchTextView);
            numberOfSongsTextView = itemView.findViewById(R.id.numberOfSongsTextView);
            popularityTextView = itemView.findViewById(R.id.popularityTextView);
        }
    }

    @NonNull
    @Override
    public ArtistStatisticsRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.artist_statistics_recycler_view_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtistStatisticsRecyclerViewAdapter.ViewHolder holder, int position) {
        Artist artist = artistList.get(position);

        TypedValue typedValue = new TypedValue();

        context.getTheme().resolveAttribute(R.attr.main_text_color, typedValue, true);

        holder.statsArtistNameTextView.setTextColor(typedValue.data);
        holder.statsArtistNameTextView.setTypeface(Typeface.DEFAULT_BOLD);
        holder.statsArtistNameTextView.setText("#" + (position + 1) + " Name: " + artist.getArtistName());

        holder.popularityTextView.setTextColor(typedValue.data);
        holder.popularityTextView.setTypeface(Typeface.DEFAULT);
        holder.popularityTextView.setText("Popularity: " + String.format(Locale.US, "%,.2f", artist.getPopularity()));

        holder.numberOfSongsTextView.setTextColor(typedValue.data);
        holder.numberOfSongsTextView.setTypeface(Typeface.DEFAULT);
        holder.numberOfSongsTextView.setText(String.format("Number of songs: %,d",
                artist.getNumberOfSongs()));

        holder.statsTotalLaunchTextView.setTextColor(typedValue.data);
        holder.statsTotalLaunchTextView.setTypeface(Typeface.DEFAULT);
        holder.statsTotalLaunchTextView.setText("Launches: " + String.format("%,d", artist.getLaunchedTimes()));

        holder.totalTimeListenedTextView.setTextColor(typedValue.data);
        holder.totalTimeListenedTextView.setTypeface(Typeface.DEFAULT);
        holder.totalTimeListenedTextView.setText(String.format("Total time listened: %s (%.2f%%)",
                MusicActivity.convertStatisticsTime(artist.getPlayedTime()),
                artist.getPlayedTime() / (double) AllStatisticsActivity.totalPlayedTime * 100.0));

        holder.listenedTimePerLaunchTextView.setTextColor(typedValue.data);
        holder.listenedTimePerLaunchTextView.setTypeface(Typeface.DEFAULT);
        if (artist.getLaunchedTimes() == 0) {
            holder.listenedTimePerLaunchTextView.setText("Played time per launch: 0s");
        }
        else {
            holder.listenedTimePerLaunchTextView.setText("Played time per launch: " + MusicActivity.convertStatisticsTime(artist.getPlayedTimePerLaunch()));
        }
    }

    @Override
    public int getItemCount() {
        return artistList.size();
    }
}
