package com.example.android_media_player.MusicPlayer;

public class Artist {
    private Long playedTime;
    private Integer launchedTimes;
    private String artistName;
    private Integer numberOfSongs;

    public Artist(String artistName, Long playedTime, Integer launchedTimes, Integer numberOfSongs) {
        this.playedTime = playedTime;
        this.launchedTimes = launchedTimes;
        this.artistName = artistName;
        this.numberOfSongs = numberOfSongs;
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

    public Integer getNumberOfSongs() {
        return numberOfSongs;
    }

    public void setNumberOfSongs(Integer val) {
        numberOfSongs = val;
    }
}
