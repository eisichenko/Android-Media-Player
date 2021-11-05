package com.example.android_media_player.Helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.MediaStore;

import androidx.annotation.Nullable;

import com.example.android_media_player.MusicPlayer.Artist;
import com.example.android_media_player.MusicPlayer.ArtistStatsActivity;
import com.example.android_media_player.MusicPlayer.Song;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    public enum SortType {
        ASCENDING,
        DESCENDING,
        NONE
    }

    Context context;
    public static final String STATISTICS_TABLE = "STATISTICS_TABLE";
    public static final String NAME_COLUMN = "NAME";
    public static final String LAUNCHED_TIMES_COLUMN = "LAUNCHED_TIMES";
    public static final String PLAYED_TIME_COLUMN = "PLAYED_TIME";
    public static final String ARTIST_COLUMN = "ARTIST";

    public DatabaseHelper(@Nullable Context context) {
        super(context, "statistics.db", null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String statement = "CREATE TABLE " + STATISTICS_TABLE +" (" +
                NAME_COLUMN + " TEXT UNIQUE, " +
                ARTIST_COLUMN + " TEXT, " +
                LAUNCHED_TIMES_COLUMN + " INTEGER, " +
                PLAYED_TIME_COLUMN + " INTEGER " +
                ")";

        db.execSQL(statement);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

    }

    public void fetchSongArtist(Song song) {
        if (song.getArtist() == null) {
            String[] projection = { MediaStore.Audio.Media.ARTIST };

            String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0 AND " +
                    MediaStore.Audio.Media.DISPLAY_NAME + " = '" + song.getName().replace("'", "''") + "'";

            Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection,
                    selection, null, MediaStore.Audio.Media.DISPLAY_NAME + " ASC");

            if (cursor != null && cursor.moveToFirst()) {
                String artist = cursor.getString(0);
                song.setArtist(artist);
            }

            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void add(Song song) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        fetchSongArtist(song);

        cv.put(NAME_COLUMN, song.getName());
        cv.put(LAUNCHED_TIMES_COLUMN, song.getLaunchedTimes());
        cv.put(PLAYED_TIME_COLUMN, song.getPlayedTime());
        cv.put(ARTIST_COLUMN, song.getArtist());

        db.insert(STATISTICS_TABLE, null, cv);

        db.close();
    }

    public ArrayList<Song> selectAllSongs(SortType sortType, String sortColumn) {
        ArrayList<Song> res = new ArrayList<>();

        String query = "SELECT * FROM " + STATISTICS_TABLE;

        if (sortColumn.equals(NAME_COLUMN)) {
            query += " ORDER BY LOWER(" + sortColumn + ")";
        }
        else {
            query += " ORDER BY " + sortColumn;
        }

        if (sortType == SortType.ASCENDING) {
            query += " ASC";
        }
        else {
            query += " DESC";
        }

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(query, null);

        while (cursor.moveToNext()) {
            String name = cursor.getString(0);
            Integer launchedTimes = cursor.getInt(1);
            Long playedTime = cursor.getLong(2);
            String artist = cursor.getString(3);
            res.add(new Song(null, name, artist, launchedTimes, playedTime));
        }

        cursor.close();
        db.close();

        return res;
    }

    public ArrayList<Artist> selectAllArtists(SortType sortType, String sortColumn) {
        ArrayList<Artist> artists = new ArrayList<>();

        String query = String.format("SELECT %s, SUM(%s) as time, SUM(%s) as launches\n" +
                "FROM %s\n" +
                "GROUP BY %s\n",
                ARTIST_COLUMN, PLAYED_TIME_COLUMN, LAUNCHED_TIMES_COLUMN,
                STATISTICS_TABLE,
                ARTIST_COLUMN);

        if (sortColumn.equals(ARTIST_COLUMN)) {
            query += " ORDER BY LOWER(" + sortColumn + ") ";
        }
        else if (sortColumn.equals(LAUNCHED_TIMES_COLUMN)) {
            query += " ORDER BY launches ";
        }
        else {
            query += " ORDER BY time ";
        }

        if (sortType == SortType.ASCENDING) {
            query += " ASC";
        }
        else {
            query += " DESC";
        }

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(query, null);

        while (cursor.moveToNext()) {
            String artist = cursor.getString(0);
            Long playedTime = cursor.getLong(1);
            Integer launchedTimes = cursor.getInt(2);
            artists.add(new Artist(artist, playedTime, launchedTimes));
        }

        return artists;
    }

    public ArrayList<Song> getSongsBySubstring(String substring, SortType sortType, String sortColumn) {
        ArrayList<Song> res = new ArrayList<>();

        String query = "SELECT * FROM " + STATISTICS_TABLE +
                " WHERE " + NAME_COLUMN + " LIKE '%" + substring.replace("'", "''") + "%' ";

        if (sortColumn.equals(NAME_COLUMN)) {
            query += " ORDER BY LOWER(" + sortColumn + ")";
        }
        else {
            query += " ORDER BY " + sortColumn;
        }

        if (sortType == SortType.ASCENDING) {
            query += " ASC";
        }
        else {
            query += " DESC";
        }

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(query, null);

        while (cursor.moveToNext()) {
            String name = cursor.getString(0);
            Integer launchedTimes = cursor.getInt(1);
            Long playedTime = cursor.getLong(2);
            String artist = cursor.getString(3);
            res.add(new Song(null, name, artist, launchedTimes, playedTime));
        }

        cursor.close();
        db.close();

        return res;
    }

    public Boolean exists(Song song) {
        String query = "SELECT EXISTS (SELECT * FROM " + STATISTICS_TABLE + " WHERE " + NAME_COLUMN + "='" +
                song.getName().replace("'", "''") + "')";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        boolean res = cursor.getInt(0) == 1;
        cursor.close();
        db.close();
        return res;
    }

    public Boolean clearAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + STATISTICS_TABLE);
        db.close();
        return true;
    }

    public Boolean deleteItem(Song song) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + STATISTICS_TABLE + " WHERE " + NAME_COLUMN + "='" +
                song.getName().replace("'", "''") + "'");
        db.close();
        return true;
    }

    public Song findSong(String name) throws Exception {
        String query = "SELECT * FROM " + STATISTICS_TABLE + " WHERE " + NAME_COLUMN + "='" +
                name.replace("'", "''") + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            String curName = cursor.getString(0);
            Integer highScore = cursor.getInt(1);
            Long timePlayed = cursor.getLong(2);
            String artist = cursor.getString(3);
            cursor.close();
            db.close();
            return new Song(null, curName, artist, highScore, timePlayed);
        }
        else {
            cursor.close();
            db.close();
            throw new Exception("Song name " + name + " was not found");
        }
    }

    public void modifyPlayedTime(Song song, Long newTime) {
        try {
            Song dbSong = findSong(song.getName());

            if (dbSong.getPlayedTime() < newTime) {
                String query = "UPDATE " + STATISTICS_TABLE +
                        " SET " + PLAYED_TIME_COLUMN + "=" + newTime +
                        " WHERE " + NAME_COLUMN + "='" + song.getName().replace("'", "''") + "'";

                SQLiteDatabase db = this.getWritableDatabase();
                db.execSQL(query);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateArtist(Song song) {
        try {
            Song dbSong = findSong(song.getName());

            if ((dbSong.getArtist() == null && song.getArtist() != null) ||
                    (!dbSong.getArtist().equals(song.getArtist()))) {
                String query  = String.format("UPDATE %s\n" +
                                "SET %s = '%s'\n" +
                                "WHERE %s='%s'",
                        STATISTICS_TABLE,
                        ARTIST_COLUMN, song.getArtist(),
                        NAME_COLUMN, dbSong.getName());

                SQLiteDatabase db = this.getWritableDatabase();
                db.execSQL(query);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void modifyLaunchedTimes(Song song, Integer launchedTimes) {
        try {
            Song dbSong = findSong(song.getName());

            if (dbSong.getLaunchedTimes() < launchedTimes) {
                String query = "UPDATE " + STATISTICS_TABLE +
                        " SET " + LAUNCHED_TIMES_COLUMN + "=" + launchedTimes +
                        " WHERE " + NAME_COLUMN + "='" + song.getName().replace("'", "''") + "'";

                SQLiteDatabase db = this.getWritableDatabase();
                db.execSQL(query);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Song getMostPlayedSong() throws Exception {
        String query = "SELECT * FROM " + STATISTICS_TABLE +
            " WHERE " + PLAYED_TIME_COLUMN + "= (SELECT MAX(" + PLAYED_TIME_COLUMN + ") FROM " + STATISTICS_TABLE + ")";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            String curName = cursor.getString(0);
            Integer highScore = cursor.getInt(1);
            Long timePlayed = cursor.getLong(2);
            String artist = cursor.getString(3);
            cursor.close();
            db.close();
            return new Song(null, curName, artist, highScore, timePlayed);
        }
        else {
            cursor.close();
            db.close();
            throw new Exception("No songs");
        }
    }

    public String getFavoriteArtist() throws Exception {
        String query = String.format("SELECT %s, SUM(%s) as time\n" +
                "FROM %s\n" +
                "GROUP BY %s\n" +
                "ORDER BY time DESC",
                ARTIST_COLUMN, PLAYED_TIME_COLUMN,
                STATISTICS_TABLE,
                ARTIST_COLUMN);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            String artist = cursor.getString(0);
            cursor.close();
            db.close();
            return artist;
        }
        else {
            cursor.close();
            db.close();
            throw new Exception("No artists");
        }
    }

    public String getMostUnpopularArtist() throws Exception {
        String query = String.format("SELECT %s, sum(%s) as time, sum(%s) as launches\n" +
                        "from %s\n" +
                        "group by %s\n" +
                        "order by time, launches",
                ARTIST_COLUMN, PLAYED_TIME_COLUMN, LAUNCHED_TIMES_COLUMN,
                STATISTICS_TABLE,
                ARTIST_COLUMN);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            String artist = cursor.getString(0);
            cursor.close();
            db.close();
            return artist;
        }
        else {
            cursor.close();
            db.close();
            throw new Exception("No artists");
        }
    }

    public Song getMostUnpopularSong() throws Exception {
        String query = "SELECT * FROM " + STATISTICS_TABLE +
                " ORDER BY " + PLAYED_TIME_COLUMN + ", " + LAUNCHED_TIMES_COLUMN + " ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            String curName = cursor.getString(0);
            Integer highScore = cursor.getInt(1);
            Long timePlayed = cursor.getLong(2);
            String artist = cursor.getString(3);
            cursor.close();
            db.close();
            return new Song(null, curName, artist, highScore, timePlayed);
        }
        else {
            cursor.close();
            db.close();
            throw new Exception("No songs");
        }
    }

    public Long getTotalPlayedTime() throws Exception {
        String query = "SELECT SUM(" + PLAYED_TIME_COLUMN + ") FROM " + STATISTICS_TABLE;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            Long res = cursor.getLong(0);
            cursor.close();
            db.close();
            return res;
        }
        else {
            cursor.close();
            db.close();
            throw new Exception("No songs");
        }
    }

    public Integer getTotalLaunchedTimes() throws Exception {
        String query = "SELECT SUM(" + LAUNCHED_TIMES_COLUMN + ") FROM " + STATISTICS_TABLE;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            Integer res = cursor.getInt(0);
            cursor.close();
            db.close();
            return res;
        }
        else {
            cursor.close();
            db.close();
            throw new Exception("No songs");
        }
    }

    public Long getAveragePlayTime() throws Exception {
        String query = "SELECT AVG(" + PLAYED_TIME_COLUMN + ") FROM " + STATISTICS_TABLE;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            Long res = cursor.getLong(0);
            cursor.close();
            db.close();
            return res;
        }
        else {
            cursor.close();
            db.close();
            throw new Exception("No songs");
        }
    }

    public Float getAverageLaunchTime() throws Exception {
        String query = "SELECT AVG(" + LAUNCHED_TIMES_COLUMN + ") FROM " + STATISTICS_TABLE;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            Float res = cursor.getFloat(0);
            cursor.close();
            db.close();
            return res;
        }
        else {
            cursor.close();
            db.close();
            throw new Exception("No songs");
        }
    }
}
