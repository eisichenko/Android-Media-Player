package com.example.android_media_player.MusicPlayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Song {
    private String name;
    private String path;
    private Integer launchedTimes;
    private Long playedTime;

    public Song(String path, String name, Integer launchedTimes, Long playedTime) {
        this.path = path;
        this.name = name;
        this.launchedTimes = launchedTimes;
        this.playedTime = playedTime;
    }

    public String getName() {
        return name;
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
}
