package com.coolweather.android.gson;

public class Music {
    private String name;
    //音乐文件的作者
    private String artist;
    //音乐文件的路径
    private String url;
    //音乐播放的时间
    private int time;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

}
