package com.example.android_media_player.MusicPlayer.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_media_player.Helpers.DatabaseHelper;
import com.example.android_media_player.MusicPlayer.Models.Song;
import com.example.android_media_player.MusicPlayer.MusicActivity;
import com.example.android_media_player.R;

import java.util.ArrayList;

public class SongStatisticsRecyclerViewAdapter extends RecyclerView.Adapter<SongStatisticsRecyclerViewAdapter.ViewHolder> {
    private final ArrayList<Song> songList;
    private Context context;

    public DatabaseHelper dbHelper;

    public SongStatisticsRecyclerViewAdapter(ArrayList<Song> songList) {
        this.songList = songList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final TextView statsSongNameTextView;
        final TextView statsTotalLaunchTextView;
        final TextView totalTimeListenedTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            statsSongNameTextView = itemView.findViewById(R.id.statsSongNameTextView);
            statsTotalLaunchTextView = itemView.findViewById(R.id.statsTotalLaunchTextView);
            totalTimeListenedTextView = itemView.findViewById(R.id.totalTimeListenedTextView);

            itemView.setOnLongClickListener(v -> {
                showPopupMenu(v);
                return true;
            });
        }

        private void showPopupMenu(View v) {
            PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
            popupMenu.inflate(R.menu.db_popup_menu);
            Song selectedSong = songList.get(getAdapterPosition());

            boolean songFileExists = false;

            for (Song song : MusicActivity.songList) {
                if (song.getName().equals(selectedSong.getName())) {
                    songFileExists = true;
                    break;
                }
            }

            String deleteMsg;

            if (songFileExists) {
                deleteMsg = "Song \"" + selectedSong.getName() + "\" EXISTS in your files. Are you sure to delete it from database?";
            }
            else {
                deleteMsg = "Are you sure to delete from database song \"" + selectedSong.getName() + "\"? It DOES NOT EXIST in your files";
            }

            final EditText editText = new EditText(context);
            editText.setHint("New song name");
            editText.setText(selectedSong.getName());

            popupMenu.setOnMenuItemClickListener(menuItem -> {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.deleteMenuItem) {
                    new AlertDialog.Builder(context)
                            .setTitle("Delete")
                            .setMessage(deleteMsg)
                            .setPositiveButton("Delete", (dialogInterface, i) -> {
                                int position = getAdapterPosition();
                                songList.remove(position);

                                dbHelper.delete(selectedSong);

                                notifyItemChanged(getAdapterPosition());

                                TextView noneTextView = itemView.getRootView().findViewById(R.id.noneTextView);
                                RecyclerView statisticsRecyclerView = itemView.getRootView().findViewById(R.id.statisticsRecyclerView);

                                if (statisticsRecyclerView != null && noneTextView != null) {
                                    if (songList.size() == 0) {
                                        noneTextView.setVisibility(View.VISIBLE);
                                        statisticsRecyclerView.setVisibility(View.GONE);
                                    }
                                    else {
                                        statisticsRecyclerView.setVisibility(View.VISIBLE);
                                        noneTextView.setVisibility(View.GONE);
                                    }
                                }

                                Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                    return true;
                }
                else if (itemId == R.id.renameMenuItem) {
                    new AlertDialog.Builder(context)
                            .setTitle("Rename")
                            .setView(editText)
                            .setPositiveButton("Rename", (dialogInterface, i) -> {
                                String newName = editText.getText().toString();

                                if (newName.length() == 0) {
                                    Toast.makeText(context, "Name should not be empty", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                if (dbHelper.exists(newName)) {
                                    Toast.makeText(context, "Name already exists", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                boolean existsNewName = false;

                                for (Song song : MusicActivity.songList) {
                                    if (song.getName().equals(newName)) {
                                        existsNewName = true;
                                        break;
                                    }
                                }

                                String renameMsg;

                                if (existsNewName) {
                                    renameMsg = "New name \"" + newName + "\" EXISTS in your files. Are you sure to rename song in database?";
                                }
                                else {
                                    renameMsg = "New name \"" + newName + "\" DOES NOT EXIST in you files. Are you sure to rename song in database?";
                                }

                                new AlertDialog.Builder(context)
                                        .setTitle("Rename")
                                        .setMessage(renameMsg)
                                        .setPositiveButton("Rename", (dialogInterface1, i1) -> {
                                            dbHelper.rename(selectedSong, newName);
                                            selectedSong.setName(newName);

                                            notifyItemChanged(getAdapterPosition());

                                            Toast.makeText(context, "Renamed successfully", Toast.LENGTH_SHORT).show();
                                        })
                                        .setNegativeButton("Cancel", null)
                                        .show();
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                    return true;
                }
                return false;
            });

            popupMenu.show();
        }
    }

    @NonNull
    @Override
    public SongStatisticsRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        dbHelper = new DatabaseHelper(context);
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
