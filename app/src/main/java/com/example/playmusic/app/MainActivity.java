package com.example.playmusic.app;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity implements MediaPlayer.OnCompletionListener{
    private MediaPlayer mMediaPlayer = new MediaPlayer();
    private int mCurrentCount = -1;
    private List<SongDetails> mSongDetailsList;
    private static int SONG_DURATION = 2 * 60 * 1000; // 2 minutes
    private String TAG = this.getClass().getSimpleName();
    private CountDownTimer mCountDownTimer;
    private int mSongDuration;
    private SharedPreferences.OnSharedPreferenceChangeListener mSharedPrefListener;
    private SharedPreferences mSharedPreferences;
    private MySharedPrefChangeListener mySharedPrefChangeListener = new MySharedPrefChangeListener();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSongDuration = Integer.parseInt(getString(R.string.pref_default_time_out_value));
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);

        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnCompletionListener(this);

        mCountDownTimer = new CountDownTimer(mSongDuration, mSongDuration) {
            @Override
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {
                playNextSong();
            }
        };

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        getAllMediaFiles();
    }

    private class MySharedPrefChangeListener implements SharedPreferences.OnSharedPreferenceChangeListener{

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
            mSongDuration = sharedPreferences.getInt(s,Integer.parseInt(getString(R.string.pref_default_time_out_value)));
            Log.d(TAG,"OnSharedPreferenceChangeListener");
        }
    }

    @Override
    protected void onStop() {
        super.onPause();
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(mySharedPrefChangeListener);
    }

    @Override
    protected void onStart() {
        super.onResume();
        mSharedPreferences.registerOnSharedPreferenceChangeListener(mySharedPrefChangeListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this,SettingsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private void getAllMediaFiles(){
        mSongDetailsList = new ArrayList<SongDetails>();
        ContentResolver contentResolver = getContentResolver();
        Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, null, null, null, null);
        if (cursor == null) {
            // query failed, handle error.
        } else if (!cursor.moveToFirst()) {
            // no media on the device
        } else {
            int titleColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
            do {
                long thisId = cursor.getLong(idColumn);
                String thisTitle = cursor.getString(titleColumn);
                SongDetails songDetails = new SongDetails();
                songDetails.setSongId(thisId);
                songDetails.setSongTitle(thisTitle);
                mSongDetailsList.add(songDetails);
            } while (cursor.moveToNext());
        }

        playNextSong();
    }

    private void playNextSong(){
        checkCount();
        mCountDownTimer.cancel();
        mCountDownTimer.start();
        long index = mSongDetailsList.get(mCurrentCount).getSongId();
        Uri contentUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, index);

        mMediaPlayer.reset();
        try{
            mMediaPlayer.setDataSource(getApplicationContext(), contentUri);
            mMediaPlayer.prepare();
        }catch (IOException e){
            e.printStackTrace();
        }
        mMediaPlayer.start();

        Log.d("TAG",mSongDetailsList.get(mCurrentCount).getSongTitle());
        Log.d("TAG","Duration: "+mMediaPlayer.getDuration());

        if(mMediaPlayer.getDuration() < 30* 1000){
            playNextSong();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        Log.d("TAG","onCompletion");
        playNextSong();
    }

    private void checkCount(){
        mCurrentCount++;
        if(mCurrentCount == mSongDetailsList.size()){
            mCurrentCount = 0;
        }
    }




}
