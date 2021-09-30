package com.example.android_media_player.MusicPlayer;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
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
    private TextView nowPlayingTextView;

    public RecyclerViewAdapter(ArrayList<Song> songList) {
        this.songList = songList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView songItemTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            songItemTextView = itemView.findViewById(R.id.songItemTextView);

            itemView.setOnClickListener(v -> {
                MusicActivity.handler.removeCallbacks(MusicActivity.runnable);

                int prevPos = MusicActivity.selectedPosition;
                Song prevSong = null;

                if (prevPos >= 0 && prevPos < songList.size()) {
                    prevSong = songList.get(prevPos);
                }

                MusicActivity.currentSong = songList.get(getAdapterPosition());

                if (prevSong != null) {
                    System.out.println(prevSong.getName() + " played " + prevSong.getPlayedTime());
                    MusicActivity.dbHelper.modifyPlayedTime(prevSong, prevSong.getPlayedTime());
                }

                if (MusicActivity.currentSong != null) {
                    try {
                        Song dbSong = MusicActivity.dbHelper.findSong(MusicActivity.currentSong.getName());
                        MusicActivity.currentSong.setLaunchedTimes(dbSong.getLaunchedTimes() + 1);
                        MusicActivity.currentSong.setPlayedTime(dbSong.getPlayedTime());
                        System.out.println(MusicActivity.currentSong.getName() + " played " + MusicActivity.currentSong.getPlayedTime());
                        MusicActivity.dbHelper.modifyLaunchedTimes(MusicActivity.currentSong, MusicActivity.currentSong.getLaunchedTimes());
                    }
                    catch (Exception e) {
                        MusicActivity.currentSong.setLaunchedTimes(1);
                        MusicActivity.dbHelper.add(MusicActivity.currentSong);
                    }
                }

                MusicActivity.playedSongs.push(MusicActivity.currentSong);
                MusicActivity.selectedPosition = getAdapterPosition();
                try {
                    MusicActivity.mediaPlayer.reset();
                    MusicActivity.mediaPlayer.setDataSource(MusicActivity.currentSong.getPath());
                    MusicActivity.mediaPlayer.prepare();
                } catch (Exception e) {
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

                nowPlayingTextView.setText("Now playing (" + (MusicActivity.selectedPosition + 1) + "/" + songList.size() + "):");
                playImageView.setImageResource(R.drawable.ic_pause);
                musicSeekBar.setMax(MusicActivity.mediaPlayer.getDuration());

                totalTimeTextView.setText(MusicActivity.convertMusicTime(MusicActivity.mediaPlayer.getDuration()));
                songNameTextView.setText(MusicActivity.currentSong.getName());

                MusicActivity.mediaPlayer.start();
                MusicActivity.handler.post(MusicActivity.runnable);
                notifyItemChanged(prevPos);
                notifyItemChanged(MusicActivity.selectedPosition);
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
        nowPlayingTextView = parent.getRootView().findViewById(R.id.nowPlayingTextView);
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
        Song song = songList.get(position);
        holder.songItemTextView.setText(song.getName());

        TypedValue typedValue = new TypedValue();

        if (MusicActivity.selectedPosition != null && position == MusicActivity.selectedPosition) {
            context.getTheme().resolveAttribute(R.attr.main_text_color, typedValue, true);

            holder.songItemTextView.setTextColor(typedValue.data);
            holder.songItemTextView.setTypeface(Typeface.DEFAULT_BOLD);
        }
        else {
            context.getTheme().resolveAttribute(R.attr.blurred_text_color, typedValue, true);

            holder.songItemTextView.setTextColor(typedValue.data);
            holder.songItemTextView.setTypeface(Typeface.DEFAULT);
        }
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }
}
