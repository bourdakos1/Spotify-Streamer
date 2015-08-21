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
            if (intent.getAction().equals(ACTION_PLAY_TRACK)) {
                Log.d("Receiver", "PLAY");
                mMediaPlayer.start();
                Intent broadcastIntent = new Intent(PlayerService.ACTION_STATUS);
                broadcastIntent.putExtra(IS_PLAYING_EXTRA, true);
                sendStickyBroadcast(broadcastIntent);
            }
            else if (intent.getAction().equals(ACTION_PAUSE_TRACK)){
                Log.d("Receiver", "PAUSE");
                mMediaPlayer.pause();
                Intent broadcastIntent = new Intent(PlayerService.ACTION_STATUS);
                broadcastIntent.putExtra(IS_PLAYING_EXTRA, false);
                sendStickyBroadcast(broadcastIntent);
            }
            else if (intent.getAction().equals(ACTION_NEXT_TRACK)){
                Log.d("Receiver", "NEXT");
            }
            else if (intent.getAction().equals(ACTION_PREV_TRACK)){
                Log.d("Receiver", "PREV");
            }
            else if (intent.getAction().equals(ACTION_SEEK_TO)){
                Log.d("Receiver", "SEEK_TO");
                mMediaPlayer.seekTo(intent.getIntExtra(PROGRESS_EXTRA,0));
            }
        }
    };

    private MediaPlayer mMediaPlayer = new MediaPlayer();;

    public static final String ACTION_PLAY_TRACK = "com.xlythe.spotifysteamer.action.PLAY";
    public static final String ACTION_PAUSE_TRACK = "com.xlythe.spotifysteamer.action.STOP";
    public static final String ACTION_NEXT_TRACK = "com.xlythe.spotifysteamer.action.NEXT";
    public static final String ACTION_PREV_TRACK = "com.xlythe.spotifysteamer.action.PREV";
    public static final String ACTION_SEEK_TO = "com.xlythe.spotifysteamer.action.SEEK_TO";
    public static final String ACTION_STATUS = "com.xlythe.spotifysteamer.action.STATUS";
    public static final String ACTION_POSITION = "com.xlythe.spotifysteamer.action.POSITION";
    public static final String ACTION_DURATION = "com.xlythe.spotifysteamer.action.DURATION";
    public static final String IS_PLAYING_EXTRA = "is_playing";
    public static final String PROGRESS_EXTRA = "progress";
    public static final String POSITION_EXTRA = "position";
    public static final String DURATION_EXTRA = "duration";
    public static final String URL_EXTRA = "url";

    private void init(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_PLAY_TRACK);
        filter.addAction(ACTION_PAUSE_TRACK);
        filter.addAction(ACTION_NEXT_TRACK);
        filter.addAction(ACTION_PREV_TRACK);
        filter.addAction(ACTION_SEEK_TO);
        getApplicationContext().registerReceiver(mReceiver, filter);
    }

    private void destroy(){
        getApplicationContext().unregisterReceiver(mReceiver);
    }

    public PlayerService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String mUrl = intent.getStringExtra(URL_EXTRA);
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mMediaPlayer.setDataSource(mUrl);
            mMediaPlayer.prepare();
        } catch (IOException ioe) {
            Log.d("PlayerActivity", "Media not found");
        }
        mMediaPlayer.start();
        Intent broadcastIntent = new Intent(PlayerService.ACTION_STATUS);
        broadcastIntent.putExtra(IS_PLAYING_EXTRA, true);
        sendStickyBroadcast(broadcastIntent);

        broadcastIntent = new Intent(PlayerService.ACTION_DURATION);
        broadcastIntent.putExtra(DURATION_EXTRA, mMediaPlayer.getDuration());
        sendStickyBroadcast(broadcastIntent);

        new Thread() {
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
        }.start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
