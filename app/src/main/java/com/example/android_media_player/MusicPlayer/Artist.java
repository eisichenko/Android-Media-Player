package com.example.android_media_player.MusicPlayer;

public class Artist {
    private Long playedTime;
    private Integer launchedTimes;
    private String artistName;

    public Artist(String artistName, Long playedTime, Integer launchedTimes) {
        this.playedTime = playedTime;
        this.launchedTimes = launchedTimes;
        this.artistName = artistName;
    }

    public Long getPlayedTime() {
        return playedTime;
    }

    public void setPlayedTime(Long playedTime) {
        this.playedTime = playedTime;
    }

    public Integer getLaunchedTimes() {
        return launchedTimes;
    }

    public void setLaunchedTimes(Integer launchedTimes) {
        this.launchedTimes = launchedTimes;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public Long getPlayedTimePerLaunch() {
        return playedTime / launchedTimes;
    }
}
