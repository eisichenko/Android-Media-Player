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
import java.util.Collections;

public class DatabaseHelper extends SQLiteOpenHelper {
    public enum SortType {
        ASCENDING,
        DESCENDING
    }

    final Context context;
    public static final int CURRENT_DB_VERSION = 2;
    public static final String STATISTICS_TABLE = "statistics";
    public static final String SONG_NAME_COLUMN = "song_name";
    public static final String LAUNCHED_TIMES_COLUMN = "launched_times";
    public static final String PLAYED_TIME_COLUMN = "played_time";
    public static final String ARTIST_COLUMN = "artist";

    public static final String TIME_PER_LAUNCH_COLUMN = "time_per_launch";
    public static final String NUMBER_OF_SONGS_COLUMN = "number_of_songs";

    public DatabaseHelper(@Nullable Context context) {
        super(context, "statistics.db", null, CURRENT_DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = String.format("CREATE TABLE %s (\n" +
                "%s TEXT UNIQUE NOT NULL,\n" +
                "%s TEXT,\n" +
                "%s INTEGER NOT NULL,\n" +
                "%s INTEGER NOT NULL)",
                STATISTICS_TABLE,
                SONG_NAME_COLUMN,
                ARTIST_COLUMN,
                LAUNCHED_TIMES_COLUMN,
                PLAYED_TIME_COLUMN);

        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Toast.makeText(context,
                String.format("Upgrading database from version %d to %d...", oldVersion, newVersion),
                Toast.LENGTH_SHORT).show();

        if (oldVersion == 1 && newVersion == 2) {
            final String OLD_TABLE_NAME = "STATISTICS_TABLE";
            final String OLD_SONG_NAME_COLUMN = "NAME";

            db.beginTransaction();
            try {
                ArrayList<Song> songList = new ArrayList<>();

                String query = String.format("SELECT %s, %s, %s, %s\n" +
                                "FROM %s\n" +
                                "ORDER BY LOWER(%s)",
                        OLD_SONG_NAME_COLUMN, ARTIST_COLUMN, LAUNCHED_TIMES_COLUMN, PLAYED_TIME_COLUMN,
                        OLD_TABLE_NAME,
                        OLD_SONG_NAME_COLUMN);

                Cursor cursor = db.rawQuery(query, null);

                while (cursor.moveToNext()) {
                    String name = cursor.getString(0);
                    String artist = cursor.getString(1);
                    Integer launchedTimes = cursor.getInt(2);
                    Long playedTime = cursor.getLong(3);

                    songList.add(new Song(null, name, artist, launchedTimes, playedTime));
                }

                cursor.close();

                query = String.format("DROP TABLE %s", OLD_TABLE_NAME);
                db.execSQL(query);

                query = String.format("CREATE TABLE %s (\n" +
                                "%s TEXT UNIQUE NOT NULL,\n" +
                                "%s TEXT,\n" +
                                "%s INTEGER NOT NULL,\n" +
                                "%s INTEGER NOT NULL)",
                        STATISTICS_TABLE,
                        SONG_NAME_COLUMN,
                        ARTIST_COLUMN,
                        LAUNCHED_TIMES_COLUMN,
                        PLAYED_TIME_COLUMN);
                db.execSQL(query);

                for (Song song : songList) {
                    ContentValues cv = new ContentValues();

                    fetchSongArtist(song);

                    cv.put(SONG_NAME_COLUMN, song.getName());
                    cv.put(LAUNCHED_TIMES_COLUMN, song.getLaunchedTimes());
                    cv.put(PLAYED_TIME_COLUMN, song.getPlayedTime());
                    cv.put(ARTIST_COLUMN, song.getArtist());

                    db.insert(STATISTICS_TABLE, null, cv);
                }

                db.setTransactionSuccessful();

                Toast.makeText(context, "Database was upgraded successfully", Toast.LENGTH_SHORT).show();
            }
            catch (Exception e) {
                Toast.makeText(context,
                        "ERROR: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();

                Toast.makeText(context,
                        "All changes were declined",
                        Toast.LENGTH_SHORT).show();

                e.printStackTrace();
            }
            finally {
                db.endTransaction();
            }
        }
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

        cv.put(SONG_NAME_COLUMN, song.getName());
        cv.put(LAUNCHED_TIMES_COLUMN, song.getLaunchedTimes());
        cv.put(PLAYED_TIME_COLUMN, song.getPlayedTime());
        cv.put(ARTIST_COLUMN, song.getArtist());

        db.insert(STATISTICS_TABLE, null, cv);

        db.close();
    }

    public ArrayList<Song> selectAllSongs(SortType sortType, String sortColumn) {
        ArrayList<Song> res = new ArrayList<>();

        String query = String.format("SELECT %s, %s, %s, %s\n" +
                "FROM %s",
                SONG_NAME_COLUMN, ARTIST_COLUMN, LAUNCHED_TIMES_COLUMN, PLAYED_TIME_COLUMN,
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

            res.add(new Song(null, name, artist, launchedTimes, playedTime));
        }

        cursor.close();
        db.close();

        return res;
    }

    public ArrayList<Artist> selectAllArtists(SortType sortType, String sortColumn) {
        ArrayList<Artist> artists = new ArrayList<>();

        String query = String.format("SELECT %s, SUM(%s) as time, SUM(%s) as launches, COUNT(%s) as number_of_songs\n" +
                "FROM %s\n" +
                "GROUP BY %s\n",
                ARTIST_COLUMN, PLAYED_TIME_COLUMN, LAUNCHED_TIMES_COLUMN, SONG_NAME_COLUMN,
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
            Integer numberOfSongs = cursor.getInt(3);
            artists.add(new Artist(artist, playedTime, launchedTimes, numberOfSongs));
        }

        if (sortColumn.equals(DatabaseHelper.TIME_PER_LAUNCH_COLUMN)) {
            if (sortType == DatabaseHelper.SortType.ASCENDING) {
                Collections.sort(artists, (artist1, artist2) -> artist1.getPlayedTimePerLaunch().compareTo(artist2.getPlayedTimePerLaunch()));
            }
            else {
                Collections.sort(artists, (artist1, artist2) -> artist2.getPlayedTimePerLaunch().compareTo(artist1.getPlayedTimePerLaunch()));
            }
        }
        else if (sortColumn.equals(DatabaseHelper.NUMBER_OF_SONGS_COLUMN)) {
            if (sortType == DatabaseHelper.SortType.ASCENDING) {
                Collections.sort(artists, (artist1, artist2) -> artist1.getNumberOfSongs().compareTo(artist2.getNumberOfSongs()));
            }
            else {
                Collections.sort(artists, (artist1, artist2) -> artist2.getNumberOfSongs().compareTo(artist1.getNumberOfSongs()));
            }
        }

        cursor.close();

        return artists;
    }

    public ArrayList<Artist> getArtistsBySubstring(String substring, SortType sortType, String sortColumn) {
        ArrayList<Artist> artists = new ArrayList<>();

        String query = String.format("SELECT %s, SUM(%s) as time, SUM(%s) as launches, COUNT(%s) as number_of_songs\n" +
                "FROM %s\n" +
                "WHERE %s LIKE '%%%s%%'\n" +
                "GROUP BY %s\n",
                DatabaseHelper.ARTIST_COLUMN, DatabaseHelper.PLAYED_TIME_COLUMN, LAUNCHED_TIMES_COLUMN, SONG_NAME_COLUMN,
                STATISTICS_TABLE,
                ARTIST_COLUMN, substring.replace("'", "''"),
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
            Integer numberOfSongs = cursor.getInt(3);
            artists.add(new Artist(artist, playedTime, launchedTimes, numberOfSongs));
        }

        if (sortColumn.equals(DatabaseHelper.TIME_PER_LAUNCH_COLUMN)) {
            if (sortType == DatabaseHelper.SortType.ASCENDING) {
                Collections.sort(artists, (artist1, artist2) -> artist1.getPlayedTimePerLaunch().compareTo(artist2.getPlayedTimePerLaunch()));
            }
            else {
                Collections.sort(artists, (artist1, artist2) -> artist2.getPlayedTimePerLaunch().compareTo(artist1.getPlayedTimePerLaunch()));
            }
        }
        else if (sortColumn.equals(DatabaseHelper.NUMBER_OF_SONGS_COLUMN)) {
            if (sortType == DatabaseHelper.SortType.ASCENDING) {
                Collections.sort(artists, (artist1, artist2) -> artist1.getNumberOfSongs().compareTo(artist2.getNumberOfSongs()));
            }
            else {
                Collections.sort(artists, (artist1, artist2) -> artist2.getNumberOfSongs().compareTo(artist1.getNumberOfSongs()));
            }
        }

        cursor.close();

        return artists;
    }

    public ArrayList<Song> getSongsBySubstring(String substring, SortType sortType, String sortColumn) {
        ArrayList<Song> res = new ArrayList<>();

        String query = String.format("SELECT %s, %s, %s, %s\n" +
                "FROM %s\n" +
                "WHERE %s LIKE '%%%s%%'",
                SONG_NAME_COLUMN, ARTIST_COLUMN, LAUNCHED_TIMES_COLUMN, PLAYED_TIME_COLUMN,
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
            res.add(new Song(null, name, artist, launchedTimes, playedTime));
        }

        cursor.close();
        db.close();

        return res;
    }

    public Boolean exists(Song song) {
        String query = String.format("SELECT EXISTS(SELECT %s FROM %s WHERE %s = '%s')",
                SONG_NAME_COLUMN, STATISTICS_TABLE, SONG_NAME_COLUMN, song.getName().replace("'", "''"));

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        boolean res = cursor.getInt(0) == 1;
        cursor.close();
        db.close();
        return res;
    }

    public void clearAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + STATISTICS_TABLE);
        db.close();
    }

    public void deleteItem(Song song) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + STATISTICS_TABLE + " WHERE " + SONG_NAME_COLUMN + "='" +
                song.getName().replace("'", "''") + "'");
        db.close();
    }

    public Song findSong(String name) throws Exception {
        String query = String.format("SELECT %s, %s, %s, %s\n" +
                "FROM %s\n" +
                "WHERE %s = '%s'",
                SONG_NAME_COLUMN, ARTIST_COLUMN, LAUNCHED_TIMES_COLUMN, PLAYED_TIME_COLUMN,
                STATISTICS_TABLE,
                SONG_NAME_COLUMN, name.replace("'", "''"));

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            String curName = cursor.getString(0);
            String artist = cursor.getString(1);
            Integer launchedTimes = cursor.getInt(2);
            Long timePlayed = cursor.getLong(3);
            cursor.close();
            db.close();
            return new Song(null, curName, artist, launchedTimes, timePlayed);
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
                        SONG_NAME_COLUMN, dbSong.getName());

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
                        " WHERE " + SONG_NAME_COLUMN + "='" + song.getName().replace("'", "''") + "'";

                SQLiteDatabase db = this.getWritableDatabase();
                db.execSQL(query);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Song getMostPlayedSong() throws Exception {
        String query = String.format("SELECT %s, %s, %s, %s\n" +
                "FROM %s\n" +
                "WHERE %s = (SELECT MAX(%s) FROM %s)",
                SONG_NAME_COLUMN, ARTIST_COLUMN, LAUNCHED_TIMES_COLUMN, PLAYED_TIME_COLUMN,
                STATISTICS_TABLE,
                PLAYED_TIME_COLUMN, PLAYED_TIME_COLUMN, STATISTICS_TABLE);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            String curName = cursor.getString(0);
            String artist = cursor.getString(1);
            Integer highScore = cursor.getInt(2);
            Long timePlayed = cursor.getLong(3);
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
        String query = String.format("SELECT %s, %s, %s, %s\n" +
                "FROM %s\n" +
                "ORDER BY %s, %s ASC",
                SONG_NAME_COLUMN, ARTIST_COLUMN, LAUNCHED_TIMES_COLUMN, PLAYED_TIME_COLUMN,
                STATISTICS_TABLE,
                PLAYED_TIME_COLUMN, LAUNCHED_TIMES_COLUMN);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            String curName = cursor.getString(0);
            String artist = cursor.getString(1);
            Integer highScore = cursor.getInt(2);
            Long timePlayed = cursor.getLong(3);
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
