package com.example.android_media_player.Helpers;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.documentfile.provider.DocumentFile;

import com.example.android_media_player.MusicPlayer.Models.Song;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public class MediaStoreHelper {
    public static ArrayList<Song> getSongList(Activity activity, String rootFolderPath) {
        ArrayList<Song> newSongList = new ArrayList<>();

        String[] projection = { MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.ARTIST };

        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0 AND " +
                MediaStore.Audio.Media.DATA + " LIKE '" + rootFolderPath + "%'";

        Cursor cursor = activity.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection,
                selection, null, MediaStore.Audio.Media.DISPLAY_NAME + " COLLATE NOCASE ASC");

        if (cursor != null) {
            while(cursor.moveToNext()) {
                String path = cursor.getString(0);
                String name = cursor.getString(1);
                String artist = cursor.getString(2);

                Song newSong = new Song(path, name, artist, 0, 0L);

                newSongList.add(newSong);
            }
            cursor.close();
        }

        return newSongList;
    }

    public static ArrayList<LocalFolder> getSubfolders(Activity activity, Uri rootUri) {
        ArrayList<LocalFolder> subfolders = new ArrayList<>();

        String rootPath = PathHelper.getAbsolutePathStringFromUri(rootUri);

        ArrayList<Song> songList = getSongList(activity,
                PathHelper.getAbsolutePathStringFromUri(rootUri));

        HashSet<String> subfolderPaths = new HashSet<>();

        for (Song song : songList) {
            subfolderPaths.addAll(PathHelper.getSubfoldersFromFilePath(
                    rootPath,
                    song.getPath()));
        }

        for (String path : subfolderPaths) {
            ArrayList<String> pathParts = new ArrayList<>();
            pathParts.add(rootPath);
            pathParts.add(path);
            String absolutePath = PathHelper.pathCombine(pathParts);

            subfolders.add(new LocalFolder(DocumentFile.fromFile(new File(absolutePath)), path));
        }

        Collections.sort(subfolders, (folder1, folder2) -> folder1.getName().compareTo(folder2.getName()));
        return subfolders;
    }
}
