package com.xlythe.spotifysteamer;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

public class PlayerService extends Service {
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch(action){
                case ACTION_PLAY_TRACK:
                    mMediaPlayer.start();
                    sendBroadcast();
                    break;
                case ACTION_PAUSE_TRACK:
                    mMediaPlayer.pause();
                    sendBroadcast();
                    break;
                case ACTION_NEW_TRACK:
                    String url = intent.getStringExtra(URL_EXTRA);
                    playMedia(url);
                    break;
                case ACTION_SEEK_TO:
                    mMediaPlayer.seekTo(intent.getIntExtra(PROGRESS_EXTRA, 0));
                    break;

            }
        }
    };

    private MediaPlayer mMediaPlayer = new MediaPlayer();
    private Thread mThread;

    public static final String ACTION_PLAY_TRACK = "com.xlythe.spotifysteamer.action.PLAY";
    public static final String ACTION_PAUSE_TRACK = "com.xlythe.spotifysteamer.action.STOP";
    public static final String ACTION_NEW_TRACK = "com.xlythe.spotifysteamer.action.NEW";
    public static final String ACTION_SEEK_TO = "com.xlythe.spotifysteamer.action.SEEK_TO";
    public static final String ACTION_STATUS = "com.xlythe.spotifysteamer.action.STATUS";
    public static final String ACTION_POSITION = "com.xlythe.spotifysteamer.action.POSITION";
    public static final String ACTION_DURATION = "com.xlythe.spotifysteamer.action.DURATION";
    public static final String IS_PLAYING_EXTRA = "is_playing";
    public static final String PROGRESS_EXTRA = "progress";
    public static final String POSITION_EXTRA = "position";
    public static final String DURATION_EXTRA = "duration";
    public static final String URL_EXTRA = "url";

    private void sendBroadcast(){
        Intent broadcastIntent = new Intent(PlayerService.ACTION_STATUS);
        broadcastIntent.putExtra(IS_PLAYING_EXTRA, mMediaPlayer.isPlaying());
        sendStickyBroadcast(broadcastIntent);
    }

    private void init(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_PLAY_TRACK);
        filter.addAction(ACTION_PAUSE_TRACK);
        filter.addAction(ACTION_NEW_TRACK);
        filter.addAction(ACTION_SEEK_TO);
        getApplicationContext().registerReceiver(mReceiver, filter);
    }

    private void destroy(){
        getApplicationContext().unregisterReceiver(mReceiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String url = intent.getStringExtra(URL_EXTRA);
        playMedia(url);

        Intent broadcastIntent = new Intent(PlayerService.ACTION_STATUS);
        broadcastIntent.putExtra(IS_PLAYING_EXTRA, true);
        sendStickyBroadcast(broadcastIntent);

        broadcastIntent = new Intent(PlayerService.ACTION_DURATION);
        broadcastIntent.putExtra(DURATION_EXTRA, mMediaPlayer.getDuration());
        sendStickyBroadcast(broadcastIntent);

        mThread = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(100);
                        Intent broadcastIntent = new Intent(PlayerService.ACTION_POSITION);
                        broadcastIntent.putExtra(POSITION_EXTRA, mMediaPlayer.getCurrentPosition());
                        sendStickyBroadcast(broadcastIntent);
                    }
                } catch (InterruptedException e) {

                }
            }
        };
        mThread.start();
        return super.onStartCommand(intent, flags, startId);
    }

    private void playMedia(String url){
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = new MediaPlayer();
        }
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mMediaPlayer.setDataSource(url);
            mMediaPlayer.prepare();
        } catch (IOException ioe) {
            Log.d("PlayerActivity", "Media not found");
        }
        mMediaPlayer.start();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("","destroyed");
        mThread.interrupt();
        mMediaPlayer.stop();
        mMediaPlayer.release();
        destroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
