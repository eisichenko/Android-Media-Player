package com.example.android_media_player.Helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.android_media_player.MusicPlayer.Models.Artist;
import com.example.android_media_player.MusicPlayer.Models.Song;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {
    public enum SortType {
        ASCENDING,
        DESCENDING
    }

    final Context context;
    public static final int CURRENT_DB_VERSION = 1;
    public static final String STATISTICS_TABLE = "statistics";
    public static final String SONG_NAME_COLUMN = "song_name";
    public static final String LAUNCHED_TIMES_COLUMN = "launched_times";
    public static final String PLAYED_TIME_COLUMN = "played_time";
    public static final String ARTIST_NAME_COLUMN = "artist_name";
    public static final String CREATED_AT_COLUMN = "created_at";

    public static final String POPULARITY_COLUMN = "popularity";

    public static final String TIME_PER_LAUNCH_COLUMN = "time_per_launch";
    public static final String NUMBER_OF_SONGS_COLUMN = "number_of_songs";

    public DatabaseHelper(@Nullable Context context) {
        super(context, "statistics.db", null, CURRENT_DB_VERSION);
        this.context = context;
    }

    public void createTables(SQLiteDatabase db) {
        String query = String.format("CREATE TABLE %s (\n" +
                        "%s TEXT UNIQUE NOT NULL,\n" +
                        "%s TEXT,\n" +
                        "%s INTEGER NOT NULL,\n" +
                        "%s INTEGER NOT NULL,\n" +
                        "%s DATETIME NOT NULL DEFAULT(datetime('now')))",
                STATISTICS_TABLE,
                SONG_NAME_COLUMN,
                ARTIST_NAME_COLUMN,
                LAUNCHED_TIMES_COLUMN,
                PLAYED_TIME_COLUMN,
                CREATED_AT_COLUMN);

        db.execSQL(query);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void fetchSongArtist(Song song) {
        if (song.getArtistName() == null) {
            String[] projection = { MediaStore.Audio.Media.ARTIST };

            String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0 AND " +
                    MediaStore.Audio.Media.DISPLAY_NAME + " = '" + song.getName().replace("'", "''") + "'";

            Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection,
                    selection, null, MediaStore.Audio.Media.DISPLAY_NAME + " ASC");

            if (cursor != null && cursor.moveToFirst()) {
                String artist = cursor.getString(0);
                song.setArtistName(artist);
            }

            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void recreateDatabaseWithData(ArrayList<Song> songList) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.beginTransaction();

        try {
            truncateTables(db);
            bulkInsert(songList, db);
            db.setTransactionSuccessful();
        }
        catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        finally {
            db.endTransaction();
        }

        db.close();
    }

    public void bulkInsert(ArrayList<Song> songList, SQLiteDatabase db) throws Exception {
        for (Song song : songList) {
            if (song.getName() == null ||
                    song.getPlayedTime() == null ||
                    song.getLaunchedTimes() == null) {
                throw new Exception("Invalid JSON file");
            }

            fetchSongArtist(song);

            ContentValues cv = new ContentValues();
            cv.put(SONG_NAME_COLUMN, song.getName());
            cv.put(LAUNCHED_TIMES_COLUMN, song.getLaunchedTimes());
            cv.put(PLAYED_TIME_COLUMN, song.getPlayedTime());
            cv.put(ARTIST_NAME_COLUMN, song.getArtistName());

            if (song.getUtcCreatedAt() != null && !song.getUtcCreatedAt().isEmpty()) {
                cv.put(CREATED_AT_COLUMN, song.getUtcCreatedAt());
            }

            db.insert(STATISTICS_TABLE, null, cv);
        }
    }

    public void add(Song song) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        fetchSongArtist(song);

        cv.put(SONG_NAME_COLUMN, song.getName());
        cv.put(LAUNCHED_TIMES_COLUMN, song.getLaunchedTimes());
        cv.put(PLAYED_TIME_COLUMN, song.getPlayedTime());
        cv.put(ARTIST_NAME_COLUMN, song.getArtistName());

        if (song.getUtcCreatedAt() != null && !song.getUtcCreatedAt().isEmpty()) {
            cv.put(CREATED_AT_COLUMN, song.getUtcCreatedAt());
        }

        db.insert(STATISTICS_TABLE, null, cv);

        db.close();
    }

    public ArrayList<Song> selectAllSongs(SortType sortType, String sortColumn) {
        ArrayList<Song> res = new ArrayList<>();

        String query = String.format("SELECT %s, \n" +
                        "%s, \n" +
                        "%s, \n" +
                        "%s, \n" +
                        "%s / (julianday(datetime('now')) - julianday(%s)) as %s, \n" +
                        "%s\n" +
                "FROM %s",
                SONG_NAME_COLUMN,
                ARTIST_NAME_COLUMN,
                LAUNCHED_TIMES_COLUMN,
                PLAYED_TIME_COLUMN,
                PLAYED_TIME_COLUMN, CREATED_AT_COLUMN, POPULARITY_COLUMN,
                CREATED_AT_COLUMN,
                STATISTICS_TABLE);

        if (sortColumn.equals(SONG_NAME_COLUMN)) {
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
            String artist = cursor.getString(1);
            Integer launchedTimes = cursor.getInt(2);
            Long playedTime = cursor.getLong(3);
            Double popularity = cursor.getDouble(4);
            String createdAt = cursor.getString(5);

            res.add(new Song(null, name, artist, launchedTimes, playedTime, popularity, createdAt));
        }

        cursor.close();
        db.close();

        return res;
    }

    public ArrayList<Artist> selectAllArtists(SortType sortType, String sortColumn) {
        ArrayList<Artist> artists = new ArrayList<>();

        String query = String.format("SELECT %s, \n" +
                        "SUM(%s) as %s, \n" +
                        "SUM(%s) as %s, \n" +
                        "COUNT(%s) as %s, \n" +
                        "SUM(%s) / SUM(%s) as %s, \n" +
                        "SUM(%s / (julianday(datetime('now')) - julianday(%s))) as %s\n" +
                "FROM %s\n" +
                "GROUP BY %s\n",
                ARTIST_NAME_COLUMN,
                PLAYED_TIME_COLUMN, PLAYED_TIME_COLUMN,
                LAUNCHED_TIMES_COLUMN, LAUNCHED_TIMES_COLUMN,
                SONG_NAME_COLUMN, NUMBER_OF_SONGS_COLUMN,
                PLAYED_TIME_COLUMN, LAUNCHED_TIMES_COLUMN, TIME_PER_LAUNCH_COLUMN,
                PLAYED_TIME_COLUMN, CREATED_AT_COLUMN, POPULARITY_COLUMN,
                STATISTICS_TABLE,
                ARTIST_NAME_COLUMN);

        if (sortColumn.equals(ARTIST_NAME_COLUMN)) {
            query += " ORDER BY LOWER(" + sortColumn + ") ";
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
            String artist = cursor.getString(0);
            Long playedTime = cursor.getLong(1);
            Integer launchedTimes = cursor.getInt(2);
            Integer numberOfSongs = cursor.getInt(3);
            Long playedTimePerLaunch = cursor.getLong(4);
            Double popularity = cursor.getDouble(5);
            artists.add(new Artist(artist, playedTime, launchedTimes, numberOfSongs, playedTimePerLaunch, popularity));
        }

        cursor.close();

        return artists;
    }

    public ArrayList<Artist> getArtistsBySubstring(String substring, SortType sortType, String sortColumn) {
        ArrayList<Artist> artists = new ArrayList<>();

        String query = String.format("SELECT %s, \n" +
                        "SUM(%s) as %s, \n" +
                        "SUM(%s) as %s, \n" +
                        "COUNT(%s) as %s, \n" +
                        "SUM(%s) / SUM(%s) as %s, \n" +
                        "SUM(%s / (julianday(datetime('now')) - julianday(%s))) as %s\n" +
                "FROM %s\n" +
                "WHERE %s LIKE '%%%s%%'\n" +
                "GROUP BY %s\n",
                ARTIST_NAME_COLUMN,
                PLAYED_TIME_COLUMN, PLAYED_TIME_COLUMN,
                LAUNCHED_TIMES_COLUMN, LAUNCHED_TIMES_COLUMN,
                SONG_NAME_COLUMN, NUMBER_OF_SONGS_COLUMN,
                PLAYED_TIME_COLUMN, LAUNCHED_TIMES_COLUMN, TIME_PER_LAUNCH_COLUMN,
                PLAYED_TIME_COLUMN, CREATED_AT_COLUMN, POPULARITY_COLUMN,
                STATISTICS_TABLE,
                ARTIST_NAME_COLUMN, substring.replace("'", "''"),
                ARTIST_NAME_COLUMN);

        if (sortColumn.equals(ARTIST_NAME_COLUMN)) {
            query += " ORDER BY LOWER(" + sortColumn + ") ";
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
            String artist = cursor.getString(0);
            Long playedTime = cursor.getLong(1);
            Integer launchedTimes = cursor.getInt(2);
            Integer numberOfSongs = cursor.getInt(3);
            Long playedTimePerLaunch = cursor.getLong(4);
            Double popularity = cursor.getDouble(5);
            artists.add(new Artist(artist, playedTime, launchedTimes, numberOfSongs, playedTimePerLaunch, popularity));
        }

        cursor.close();

        return artists;
    }

    public ArrayList<Song> getSongsBySubstring(String substring, SortType sortType, String sortColumn) {
        ArrayList<Song> res = new ArrayList<>();

        String query = String.format("SELECT %s, \n" +
                        "%s, \n" +
                        "%s, \n" +
                        "%s, \n" +
                        "%s / (julianday(datetime('now')) - julianday(%s)) as %s, \n" +
                        "%s\n" +
                "FROM %s\n" +
                "WHERE %s LIKE '%%%s%%'",
                SONG_NAME_COLUMN,
                ARTIST_NAME_COLUMN,
                LAUNCHED_TIMES_COLUMN,
                PLAYED_TIME_COLUMN,
                PLAYED_TIME_COLUMN, CREATED_AT_COLUMN, POPULARITY_COLUMN,
                CREATED_AT_COLUMN,
                STATISTICS_TABLE,
                SONG_NAME_COLUMN, substring.replace("'", "''"));

        if (sortColumn.equals(SONG_NAME_COLUMN)) {
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
            String artist = cursor.getString(1);
            Integer launchedTimes = cursor.getInt(2);
            Long playedTime = cursor.getLong(3);
            Double popularity = cursor.getDouble(4);
            String createdAt = cursor.getString(5);
            res.add(new Song(null, name, artist, launchedTimes, playedTime, popularity, createdAt));
        }

        cursor.close();
        db.close();

        return res;
    }

    public Boolean exists(String songName) {
        String query = String.format("SELECT EXISTS(SELECT %s FROM %s WHERE %s = '%s')",
                SONG_NAME_COLUMN, STATISTICS_TABLE, SONG_NAME_COLUMN, songName.replace("'", "''"));

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        boolean res = cursor.getInt(0) == 1;
        cursor.close();
        db.close();
        return res;
    }

    public void truncateTables(SQLiteDatabase db) {
        db.execSQL("DELETE FROM " + STATISTICS_TABLE);
    }

    public void truncateTables() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + STATISTICS_TABLE);
        db.close();
    }

    public void delete(Song song) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + STATISTICS_TABLE + " WHERE " + SONG_NAME_COLUMN + "='" +
                song.getName().replace("'", "''") + "'");
        db.close();
    }

    public void rename(Song song, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = String.format("UPDATE %s\n" +
                "SET %s='%s'\n" +
                "WHERE %s='%s'",
                STATISTICS_TABLE, SONG_NAME_COLUMN, newName.replace("'", "''"),
                SONG_NAME_COLUMN, song.getName().replace("'", "''"));
        db.execSQL(query);
        db.close();
    }

    public Song findSong(String name) throws Exception {
        String query = String.format("SELECT %s, \n" +
                        "%s, \n" +
                        "%s, \n" +
                        "%s, \n" +
                        "%s / (julianday(datetime('now')) - julianday(%s)) as %s, \n" +
                        "%s\n" +
                "FROM %s\n" +
                "WHERE %s = '%s'",
                SONG_NAME_COLUMN,
                ARTIST_NAME_COLUMN,
                LAUNCHED_TIMES_COLUMN,
                PLAYED_TIME_COLUMN,
                PLAYED_TIME_COLUMN, CREATED_AT_COLUMN, POPULARITY_COLUMN,
                CREATED_AT_COLUMN,
                STATISTICS_TABLE,
                SONG_NAME_COLUMN, name.replace("'", "''"));

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            String curName = cursor.getString(0);
            String artist = cursor.getString(1);
            Integer launchedTimes = cursor.getInt(2);
            Long timePlayed = cursor.getLong(3);
            Double popularity = cursor.getDouble(4);
            String createdAt = cursor.getString(5);
            cursor.close();
            db.close();
            return new Song(null, curName, artist, launchedTimes, timePlayed, popularity, createdAt);
        }
        else {
            cursor.close();
            db.close();
            throw new Exception("Song name " + name + " was not found");
        }
    }

    public void modifyPlayedTime(Song song, Long newTime) {
        if (song != null) {
            try {
                Song dbSong = findSong(song.getName());

                if (dbSong.getPlayedTime() < newTime) {
                    String query = "UPDATE " + STATISTICS_TABLE +
                            " SET " + PLAYED_TIME_COLUMN + "=" + newTime +
                            " WHERE " + SONG_NAME_COLUMN + "='" + song.getName().replace("'", "''") + "'";

                    song.dbTime = newTime;

                    SQLiteDatabase db = this.getWritableDatabase();
                    db.execSQL(query);
                }
            }
            catch (Exception e) {
                add(song);
                e.printStackTrace();
            }
        }
    }

    public void modifyArtist(Song song, String artistName) {
        try {
            Song dbSong = findSong(song.getName());

            String query  = String.format("UPDATE %s\n" +
                            "SET %s = '%s'\n" +
                            "WHERE %s='%s'",
                    STATISTICS_TABLE,
                    ARTIST_NAME_COLUMN, artistName.replace("'", "''"),
                    SONG_NAME_COLUMN, dbSong.getName().replace("'", "''"));

            SQLiteDatabase db = this.getWritableDatabase();
            db.execSQL(query);
        }
        catch (Exception e) {
            song.setArtistName(artistName);
            add(song);
            e.printStackTrace();
        }
    }

    public void modifyLaunchedTimes(Song song, Integer launchedTimes) {
        try {
            Song dbSong = findSong(song.getName());

            if (dbSong.getLaunchedTimes() < launchedTimes) {
                String query = "UPDATE " + STATISTICS_TABLE +
                        " SET " + LAUNCHED_TIMES_COLUMN + "=" + launchedTimes +
                        " WHERE " + SONG_NAME_COLUMN + "='" + song.getName().replace("'", "''") + "'";

                SQLiteDatabase db = this.getWritableDatabase();
                db.execSQL(query);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Song getMostPopularSong() throws Exception {
        String query = String.format("SELECT %s, \n" +
                        "%s, \n" +
                        "%s, \n" +
                        "%s, \n" +
                        "%s / (julianday(datetime('now')) - julianday(%s)) as %s, \n" +
                        "%s\n" +
                "FROM %s\n" +
                "ORDER BY %s DESC, %s DESC, %s DESC",
                SONG_NAME_COLUMN,
                ARTIST_NAME_COLUMN,
                LAUNCHED_TIMES_COLUMN,
                PLAYED_TIME_COLUMN,
                PLAYED_TIME_COLUMN, CREATED_AT_COLUMN, POPULARITY_COLUMN,
                CREATED_AT_COLUMN,
                STATISTICS_TABLE,
                POPULARITY_COLUMN, PLAYED_TIME_COLUMN, LAUNCHED_TIMES_COLUMN);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            String curName = cursor.getString(0);
            String artist = cursor.getString(1);
            Integer highScore = cursor.getInt(2);
            Long timePlayed = cursor.getLong(3);
            Double popularity = cursor.getDouble(4);
            String createdAt = cursor.getString(5);
            cursor.close();
            db.close();
            return new Song(null, curName, artist, highScore, timePlayed, popularity, createdAt);
        }
        else {
            cursor.close();
            db.close();
            throw new Exception("No songs");
        }
    }

    public Artist getMostPopularArtist() throws Exception {
        String query = String.format("SELECT %s, \n" +
                        "SUM(%s) as %s, \n" +
                        "SUM(%s) as %s, \n" +
                        "COUNT(%s) as %s, \n" +
                        "SUM(%s) / SUM(%s) as %s, \n" +
                        "SUM(%s / (julianday(datetime('now')) - julianday(%s))) as %s\n" +
                        "FROM %s\n" +
                        "GROUP BY %s\n" +
                        "ORDER BY %s DESC, %s DESC, %s DESC\n",
                ARTIST_NAME_COLUMN,
                PLAYED_TIME_COLUMN, PLAYED_TIME_COLUMN,
                LAUNCHED_TIMES_COLUMN, LAUNCHED_TIMES_COLUMN,
                SONG_NAME_COLUMN, NUMBER_OF_SONGS_COLUMN,
                PLAYED_TIME_COLUMN, LAUNCHED_TIMES_COLUMN, TIME_PER_LAUNCH_COLUMN,
                PLAYED_TIME_COLUMN, CREATED_AT_COLUMN, POPULARITY_COLUMN,
                STATISTICS_TABLE,
                ARTIST_NAME_COLUMN,
                POPULARITY_COLUMN, PLAYED_TIME_COLUMN, LAUNCHED_TIMES_COLUMN);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            String artist_name = cursor.getString(0);
            Long playedTime = cursor.getLong(1);
            Integer launchedTimes = cursor.getInt(2);
            Integer numberOfSongs = cursor.getInt(3);
            Long playedTimePerLaunch = cursor.getLong(4);
            Double popularity = cursor.getDouble(5);
            Artist artist = new Artist(artist_name, playedTime, launchedTimes, numberOfSongs, playedTimePerLaunch, popularity);
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

    public Artist getMostUnpopularArtist() throws Exception {
        String query = String.format("SELECT %s, \n" +
                        "SUM(%s) as %s, \n" +
                        "SUM(%s) as %s, \n" +
                        "COUNT(%s) as %s, \n" +
                        "SUM(%s) / SUM(%s) as %s, \n" +
                        "SUM(%s / (julianday(datetime('now')) - julianday(%s))) as %s\n" +
                        "FROM %s\n" +
                        "GROUP BY %s\n" +
                        "ORDER BY %s ASC, %s ASC, %s ASC\n",
                ARTIST_NAME_COLUMN,
                PLAYED_TIME_COLUMN, PLAYED_TIME_COLUMN,
                LAUNCHED_TIMES_COLUMN, LAUNCHED_TIMES_COLUMN,
                SONG_NAME_COLUMN, NUMBER_OF_SONGS_COLUMN,
                PLAYED_TIME_COLUMN, LAUNCHED_TIMES_COLUMN, TIME_PER_LAUNCH_COLUMN,
                PLAYED_TIME_COLUMN, CREATED_AT_COLUMN, POPULARITY_COLUMN,
                STATISTICS_TABLE,
                ARTIST_NAME_COLUMN,
                POPULARITY_COLUMN, PLAYED_TIME_COLUMN, LAUNCHED_TIMES_COLUMN);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            String artist_name = cursor.getString(0);
            Long playedTime = cursor.getLong(1);
            Integer launchedTimes = cursor.getInt(2);
            Integer numberOfSongs = cursor.getInt(3);
            Long playedTimePerLaunch = cursor.getLong(4);
            Double popularity = cursor.getDouble(5);
            Artist artist = new Artist(artist_name, playedTime, launchedTimes, numberOfSongs, playedTimePerLaunch, popularity);
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
        String query = String.format("SELECT %s, \n" +
                        "%s, \n" +
                        "%s, \n" +
                        "%s, \n" +
                        "%s / (julianday(datetime('now')) - julianday(%s)) as %s, \n" +
                        "%s\n" +
                "FROM %s\n" +
                "ORDER BY %s ASC, %s ASC, %s ASC",
                SONG_NAME_COLUMN,
                ARTIST_NAME_COLUMN,
                LAUNCHED_TIMES_COLUMN,
                PLAYED_TIME_COLUMN,
                PLAYED_TIME_COLUMN, CREATED_AT_COLUMN, POPULARITY_COLUMN,
                CREATED_AT_COLUMN,
                STATISTICS_TABLE,
                POPULARITY_COLUMN, PLAYED_TIME_COLUMN, LAUNCHED_TIMES_COLUMN);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            String curName = cursor.getString(0);
            String artist = cursor.getString(1);
            Integer highScore = cursor.getInt(2);
            Long timePlayed = cursor.getLong(3);
            Double popularity = cursor.getDouble(4);
            String createdAt = cursor.getString(5);
            cursor.close();
            db.close();
            return new Song(null, curName, artist, highScore, timePlayed, popularity, createdAt);
        }
        else {
            cursor.close();
            db.close();
            throw new Exception("No songs");
        }
    }

    public Song getTheOldestSong() throws Exception {
        String query = String.format("SELECT %s, \n" +
                        "%s, \n" +
                        "%s, \n" +
                        "%s, \n" +
                        "%s / (julianday(datetime('now')) - julianday(%s)) as %s, \n" +
                        "%s\n" +
                        "FROM %s\n" +
                        "ORDER BY %s ASC",
                SONG_NAME_COLUMN,
                ARTIST_NAME_COLUMN,
                LAUNCHED_TIMES_COLUMN,
                PLAYED_TIME_COLUMN,
                PLAYED_TIME_COLUMN, CREATED_AT_COLUMN, POPULARITY_COLUMN,
                CREATED_AT_COLUMN,
                STATISTICS_TABLE,
                CREATED_AT_COLUMN);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            String curName = cursor.getString(0);
            String artist = cursor.getString(1);
            Integer highScore = cursor.getInt(2);
            Long timePlayed = cursor.getLong(3);
            Double popularity = cursor.getDouble(4);
            String createdAt = cursor.getString(5);
            cursor.close();
            db.close();
            return new Song(null, curName, artist, highScore, timePlayed, popularity, createdAt);
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

    public Integer getTotalNumberOfSongs() throws Exception {
        String query = "SELECT COUNT(" + SONG_NAME_COLUMN + ") FROM " + STATISTICS_TABLE;

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
}
