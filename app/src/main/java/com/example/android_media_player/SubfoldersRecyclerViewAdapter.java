package com.example.android_media_player;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_media_player.Helpers.LocalFolder;
import com.example.android_media_player.MusicPlayer.MusicActivity;

import java.util.ArrayList;

public class SubfoldersRecyclerViewAdapter extends RecyclerView.Adapter<SubfoldersRecyclerViewAdapter.ViewHolder> {
    private final ArrayList<LocalFolder> subfolderList;
    private Context context;

    public SubfoldersRecyclerViewAdapter(ArrayList<LocalFolder> subfolderList) {
        this.subfolderList = subfolderList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final TextView subfolderNameTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            subfolderNameTextView = itemView.findViewById(R.id.subfolderNameTextView);
            subfolderNameTextView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                MainActivity.lastChosenSubfolderName = subfolderList.get(pos).getName();
                MainActivity.chosenFile = subfolderList.get(pos).getDocumentFile();
                MainActivity.chosenUri = subfolderList.get(pos).getDocumentFile().getUri();

                Toast.makeText(v.getContext(), "Opening folder: " + MainActivity.chosenFile.getName(), Toast.LENGTH_SHORT).show();

                itemView.getContext().startActivity(new Intent(v.getContext(), MusicActivity.class));
            });
        }
    }

    @NonNull
    @Override
    public SubfoldersRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.subfolders_recycler_view_item, parent, false);
        return new SubfoldersRecyclerViewAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SubfoldersRecyclerViewAdapter.ViewHolder holder, int position) {
        LocalFolder folder = subfolderList.get(position);

        TypedValue typedValue = new TypedValue();

        if (folder.getName().equals(MainActivity.lastChosenSubfolderName)) {
            context.getTheme().resolveAttribute(R.attr.main_text_color, typedValue, true);
            holder.subfolderNameTextView.setTextColor(typedValue.data);
            holder.subfolderNameTextView.setTypeface(Typeface.DEFAULT_BOLD);
        }
        else {
            context.getTheme().resolveAttribute(R.attr.blurred_text_color, typedValue, true);
            holder.subfolderNameTextView.setTextColor(typedValue.data);
            holder.subfolderNameTextView.setTypeface(Typeface.DEFAULT);
        }

        holder.subfolderNameTextView.setText(folder.getName());
    }

    @Override
    public int getItemCount() {
        return subfolderList.size();
    }
}
