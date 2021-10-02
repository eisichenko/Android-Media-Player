package com.example.android_media_player.Helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.android_media_player.MusicPlayer.Song;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {
    public enum SortType {
        ASCENDING,
        DESCENDING,
        NONE
    }

    public static final String STATISTICS_TABLE = "STATISTICS_TABLE";
    public static final String NAME_COLUMN = "NAME";
    public static final String LAUNCHED_TIMES_COLUMN = "LAUNCHED_TIMES";
    public static final String PLAYED_TIME_COLUMN = "PLAYED_TIME";

    public DatabaseHelper(@Nullable Context context) {
        super(context, "statistics.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String statement = "CREATE TABLE " + STATISTICS_TABLE +" (" +
                NAME_COLUMN + " TEXT UNIQUE, " +
                LAUNCHED_TIMES_COLUMN + " INTEGER, " +
                PLAYED_TIME_COLUMN + " INTEGER " +
                ")";

        db.execSQL(statement);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

    }

    public Boolean add(Song song) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(NAME_COLUMN, song.getName());
        cv.put(LAUNCHED_TIMES_COLUMN, song.getLaunchedTimes());
        cv.put(PLAYED_TIME_COLUMN, song.getPlayedTime());

        long insert = db.insert(STATISTICS_TABLE, null, cv);

        db.close();

        return insert != -1;
    }

    public ArrayList<Song> selectALl(SortType sortType, String sortColumn) {
        ArrayList<Song> res = new ArrayList<>();

        String query = "";

        if (sortType == SortType.NONE) {
            query = "SELECT * FROM " + STATISTICS_TABLE;
        }
        else if (sortType == SortType.ASCENDING) {
            if (sortColumn.equals(NAME_COLUMN)) {
                query = "SELECT * FROM " + STATISTICS_TABLE + " ORDER BY LOWER(" + sortColumn + ") ASC";
            }
            else {
                query = "SELECT * FROM " + STATISTICS_TABLE + " ORDER BY " + sortColumn + " ASC";
            }
        }
        else {
            if (sortColumn.equals(NAME_COLUMN)) {
                query = "SELECT * FROM " + STATISTICS_TABLE + " ORDER BY LOWER(" + sortColumn + ") DESC";
            }
            else {
                query = "SELECT * FROM " + STATISTICS_TABLE + " ORDER BY " + sortColumn + " DESC";
            }
        }

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(query, null);

        while (cursor.moveToNext()) {
            String name = cursor.getString(0);
            Integer launchedTimes = cursor.getInt(1);
            Long playedTime = cursor.getLong(2);
            res.add(new Song(null, name, launchedTimes, playedTime));
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
            cursor.close();
            db.close();
            return new Song(null, curName, highScore, timePlayed);
        }
        else {
            cursor.close();
            db.close();
            throw new Exception("Nickname " + name + " was not found");
        }
    }

    public Boolean modifyPlayedTime(Song song, Long newTime) {
        String query = "UPDATE " + STATISTICS_TABLE +
                " SET " + PLAYED_TIME_COLUMN + "=" + newTime +
                " WHERE " + NAME_COLUMN + "='" + song.getName().replace("'", "''") + "'";

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(query);
        return true;
    }

    public Boolean modifyLaunchedTimes(Song song, Integer launchedTimes) {
        String query = "UPDATE " + STATISTICS_TABLE +
                " SET " + LAUNCHED_TIMES_COLUMN + "=" + launchedTimes +
                " WHERE " + NAME_COLUMN + "='" + song.getName().replace("'", "''") + "'";

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(query);
        return true;
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
            cursor.close();
            db.close();
            return new Song(null, curName, highScore, timePlayed);
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

    public Integer getAverageLaunchTime() throws Exception {
        String query = "SELECT AVG(" + LAUNCHED_TIMES_COLUMN + ") FROM " + STATISTICS_TABLE;

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
