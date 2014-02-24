package com.example.playmusic.app;

/**
 * Created by rjain on 2/24/14.
 */
public class SongDetails {
    private long songId;
    private String songTitle;

    public long getSongId() {
        return songId;
    }

    public void setSongId(long songId) {
        this.songId = songId;
    }

    public String getSongTitle() {
        return songTitle;
    }

    public void setSongTitle(String songTitle) {
        this.songTitle = songTitle;
    }
}
