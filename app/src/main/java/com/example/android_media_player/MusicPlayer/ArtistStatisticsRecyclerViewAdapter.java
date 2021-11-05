package com.example.android_media_player.MusicPlayer;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_media_player.R;

import java.util.ArrayList;

public class ArtistStatisticsRecyclerViewAdapter extends RecyclerView.Adapter<ArtistStatisticsRecyclerViewAdapter.ViewHolder> {
    private ArrayList<Artist> artistList;
    private Context context;

    public ArtistStatisticsRecyclerViewAdapter(ArrayList<Artist> artistList) {
        this.artistList = artistList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView statsArtistNameTextView;
        TextView statsTotalLaunchTextView;
        TextView totalTimeListenedTextView;
        TextView listenedTimePerLaunchTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            statsArtistNameTextView = itemView.findViewById(R.id.statsArtistNameTextView);
            statsTotalLaunchTextView = itemView.findViewById(R.id.statsTotalLaunchTextView);
            totalTimeListenedTextView = itemView.findViewById(R.id.totalTimeListenedTextView);
            listenedTimePerLaunchTextView = itemView.findViewById(R.id.listenedTimePerLaunchTextView);
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
        holder.statsArtistNameTextView.setTypeface(Typeface.DEFAULT);
        holder.statsArtistNameTextView.setText("#" + (position + 1) + " Name: " + artist.getArtistName());

        holder.statsTotalLaunchTextView.setTextColor(typedValue.data);
        holder.statsTotalLaunchTextView.setTypeface(Typeface.DEFAULT);
        holder.statsTotalLaunchTextView.setText("Total launched times: " + artist.getLaunchedTimes().toString());

        holder.totalTimeListenedTextView.setTextColor(typedValue.data);
        holder.totalTimeListenedTextView.setTypeface(Typeface.DEFAULT);
        holder.totalTimeListenedTextView.setText("Total time listened: " + MusicActivity.convertStatisticsTime(artist.getPlayedTime()));

        holder.listenedTimePerLaunchTextView.setTextColor(typedValue.data);
        holder.listenedTimePerLaunchTextView.setTypeface(Typeface.DEFAULT);
        if (artist.getLaunchedTimes() == 0) {
            holder.listenedTimePerLaunchTextView.setText("Listened time per launch: 0s");
        }
        else {
            holder.listenedTimePerLaunchTextView.setText("Listened time per launch: " + MusicActivity.convertStatisticsTime(artist.getPlayedTimePerLaunch()));
        }
    }

    @Override
    public int getItemCount() {
        return artistList.size();
    }
}
