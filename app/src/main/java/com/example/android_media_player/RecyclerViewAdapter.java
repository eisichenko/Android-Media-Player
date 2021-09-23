package com.example.android_media_player;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private ArrayList<Song> songList;
    private Context context;

    public RecyclerViewAdapter(ArrayList<Song> songList) {
        this.songList = songList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView songItemTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            songItemTextView = itemView.findViewById(R.id.songItemTextView);

            itemView.setOnClickListener(v -> {
                MusicActivity.selectedPosition = getAdapterPosition();
                notifyDataSetChanged();
            });
        }
    }

    @NonNull
    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
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
