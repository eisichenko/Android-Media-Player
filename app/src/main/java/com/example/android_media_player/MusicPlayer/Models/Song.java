package com.example.android_media_player.MusicPlayer.Models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Song {
    private String name;
    private final String path;
    private String artist;
    private Integer launchedTimes;
    private Long playedTime;
    private Double popularity;

    private String createdAt;

    public Long dbTime;

    public Song(String path,
                String name,
                String artist,
                Integer launchedTimes,
                Long playedTime,
                Double popularity,
                String createdAt) {
        this.path = path;
        this.name = name;
        this.artist = artist;
        this.launchedTimes = launchedTimes;
        this.playedTime = playedTime;
        this.popularity = popularity;
        this.createdAt = createdAt;
        dbTime = 0L;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof Song)) {
            return false;
        }

        return name.equals(((Song)obj).getName());
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }

    public Integer getLaunchedTimes() {
        return launchedTimes;
    }

    public void setLaunchedTimes(Integer launchedTimes) {
        this.launchedTimes = launchedTimes;
    }

    public Long getPlayedTime() {
        return playedTime;
    }

    public void setPlayedTime(Long playedTime) {
        this.playedTime = playedTime;
    }

    public String getArtistName() {
        return this.artist;
    }

    public void setArtistName(String value) {
        this.artist = value;
    }

    public Double getPopularity() {
        return popularity;
    }

    public void setPopularity(Double popularity) {
        this.popularity = popularity;
    }

    public String getLocalCreatedAt() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat outputFormatter = new SimpleDateFormat("dd MMM yyyy HH:mm:ss z", Locale.getDefault());

        try {
            Date date = formatter.parse(createdAt);
            return outputFormatter.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return createdAt;
    }

    public String getUtcCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
