package com.example.android_media_player.MusicPlayer.Models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Song {
    private String name;
    private final String path;
    private String artist;
    private Integer launchedTimes;
    private Long playedTime;
    public Long dbTime;

    public Song(String path, String name, String artist, Integer launchedTimes, Long playedTime) {
        this.path = path;
        this.name = name;
        this.artist = artist;
        this.launchedTimes = launchedTimes;
        this.playedTime = playedTime;
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
}
