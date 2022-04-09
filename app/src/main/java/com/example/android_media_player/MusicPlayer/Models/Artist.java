package com.example.android_media_player.MusicPlayer.Models;

public class Artist {
    private Long playedTime;
    private Integer launchedTimes;
    private String artistName;
    private Integer numberOfSongs;

    private Long playedTimePerLaunch;
    private Double popularity;

    public Artist(String artistName,
                  Long playedTime,
                  Integer launchedTimes,
                  Integer numberOfSongs,
                  Long playedTimePerLaunch,
                  Double popularity) {
        this.playedTime = playedTime;
        this.launchedTimes = launchedTimes;
        this.artistName = artistName;
        this.numberOfSongs = numberOfSongs;
        this.playedTimePerLaunch = playedTimePerLaunch;
        this.popularity = popularity;
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

    public Integer getNumberOfSongs() {
        return numberOfSongs;
    }

    public void setNumberOfSongs(Integer val) {
        numberOfSongs = val;
    }

    public Double getPopularity() {
        return popularity;
    }

    public void setPopularity(Double popularity) {
        this.popularity = popularity;
    }

    public Long getPlayedTimePerLaunch() {
        return playedTimePerLaunch;
    }

    public void setPlayedTimePerLaunch(Long playedTimePerLaunch) {
        this.playedTimePerLaunch = playedTimePerLaunch;
    }
}
