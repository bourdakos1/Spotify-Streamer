package com.xlythe.spotifysteamer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.util.ArrayList;

public class PlayerService extends Service {
    public final static String ACTION_PLAY_TOGGLE = "com.xlythe.spotifysteamer.action.PLAY_TOGGLE";
    public final static String ACTION_PLAYBACK_POSITION = "com.xlythe.spotifysteamer.action.PLAYBACK_POSITION";
    public final static String ACTION_STATUS = "com.xlythe.spotifysteamer.action.STATUS";
    public final static String ACTION_DETAILS = "com.xlythe.spotifysteamer.action.DETAILS";
    public final static String ACTION_SEEK_TO = "com.xlythe.spotifysteamer.action.SEEK_TO";
    public final static String ACTION_NEW_TRACK = "com.xlythe.spotifysteamer.action.NEW_TRACK";
    public final static String IS_PLAYING_EXTRA = "is_playing";
    public final static String HAS_STARTED_EXTRA = "has_started";
    public final static String PLAYBACK_POSITION_EXTRA = "playback_position";
    public final static String DURATION_EXTRA = "duration";
    public final static String IMAGE_EXTRA = "image";
    public final static String ALBUM_EXTRA = "album";
    public final static String TRACK_EXTRA = "track";
    public final static String ARTIST_EXTRA = "artist";
    public final static String URL_EXTRA = "url";
    public final static String SEEK_TO_EXTRA = "seek_to";
    public final static String FORWARD_EXTRA = "forward";
    private final static int NOTIFICATION_TAG = 1001;

    private MediaPlayer mMediaPlayer;
    private Thread mThread;
    private ArrayList<TopTracksParcelable> mList;
    private int mCurrentTrack;
    private Bitmap mAlbumBitmap;

    /**
     * Broadcast receiver that toggles play/pause, changes tracks, and seeks.
     */
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch(action){
                case ACTION_PLAY_TOGGLE:
                    if (mMediaPlayer.isPlaying()){
                        mMediaPlayer.pause();
                    }
                    else{
                        mMediaPlayer.start();
                    }
                    sendBroadcast();
                    break;
                case ACTION_NEW_TRACK:
                    if (intent.getBooleanExtra(FORWARD_EXTRA, true) && mCurrentTrack < mList.size() - 1) {
                        mCurrentTrack++;
                        Log.d("","NEXT: " + intent.getBooleanExtra(FORWARD_EXTRA, true));
                    }
                    else if (mCurrentTrack > 0) {
                        mCurrentTrack--;
                        Log.d("","PREV: " + intent.getBooleanExtra(FORWARD_EXTRA, true));
                    }
                    Log.d("","FAIL: " + intent.getBooleanExtra(FORWARD_EXTRA, true));
                    playMedia();
                    break;
                case ACTION_SEEK_TO:
                    mMediaPlayer.seekTo(intent.getIntExtra(SEEK_TO_EXTRA, 0));
                    break;

            }
        }
    };

    /**
     * broadcasts whether or not the the player is playing.
     */
    private void sendBroadcast(){
        Intent broadcastIntent = new Intent(ACTION_STATUS);
        broadcastIntent.putExtra(IS_PLAYING_EXTRA, mMediaPlayer.isPlaying());
        broadcastIntent.putExtra(HAS_STARTED_EXTRA, true);
        sendStickyBroadcast(broadcastIntent);
    }

    /**
     * Register the broadcast receiver.
     */
    private void init(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_PLAY_TOGGLE);
        filter.addAction(ACTION_NEW_TRACK);
        filter.addAction(ACTION_SEEK_TO);
        getApplicationContext().registerReceiver(mReceiver, filter);
    }

    /**
     * Destroy broadcast receiver.
     */
    private void destroy(){
        Intent broadcastIntent = new Intent(ACTION_STATUS);
        broadcastIntent.putExtra(IS_PLAYING_EXTRA, false);
        broadcastIntent.putExtra(HAS_STARTED_EXTRA, false);
        sendStickyBroadcast(broadcastIntent);
        Log.d("", "end service");
        getApplicationContext().unregisterReceiver(mReceiver);
    }

    /**
     * On start command.
     * @param intent intent.
     * @param flags flags.
     * @param startId start id.
     * @return Int
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            mList = intent.getParcelableArrayListExtra(TopTracksFragment.TRACK_LIST_EXTRA);
            mCurrentTrack = intent.getIntExtra(TopTracksFragment.CURRENT_TRACK_EXTRA,0);
            playMedia();
            mThread = new Thread() {
                @Override
                public void run() {
                    try {
                        while (!isInterrupted()) {
                            Thread.sleep(100);
                            if (mMediaPlayer!=null) {
                                Intent broadcastIntent = new Intent(ACTION_PLAYBACK_POSITION);
                                broadcastIntent.putExtra(PLAYBACK_POSITION_EXTRA, mMediaPlayer.getCurrentPosition());
                                sendStickyBroadcast(broadcastIntent);
                            }
                        }
                    } catch (InterruptedException e) {

                    }
                }
            };
            mThread.start();
        }
        else{
            stopService(new Intent(this, PlayerService.class));
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Plays media and broadcasts info.
     */
    private void playMedia(){
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
        }
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mMediaPlayer.setDataSource(mList.get(mCurrentTrack).getPreviewUrl());
            mMediaPlayer.prepareAsync();
        } catch (IOException ioe) {
            Log.d("PlayerActivity", "Media not found");
        }
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mMediaPlayer.start();

                Intent broadcastIntent = new Intent(ACTION_STATUS);
                broadcastIntent.putExtra(IS_PLAYING_EXTRA, true);
                broadcastIntent.putExtra(HAS_STARTED_EXTRA, true);
                sendStickyBroadcast(broadcastIntent);

                broadcastIntent = new Intent(ACTION_DETAILS);
                broadcastIntent.putExtra(DURATION_EXTRA, mMediaPlayer.getDuration());
                broadcastIntent.putExtra(IMAGE_EXTRA, mList.get(mCurrentTrack).getAlbumImage());
                broadcastIntent.putExtra(ALBUM_EXTRA, mList.get(mCurrentTrack).getAlbumName());
                broadcastIntent.putExtra(TRACK_EXTRA, mList.get(mCurrentTrack).getTrackName());
                broadcastIntent.putExtra(ARTIST_EXTRA, mList.get(mCurrentTrack).getArtistName());
                broadcastIntent.putExtra(URL_EXTRA, mList.get(mCurrentTrack).getPreviewUrl());
                sendStickyBroadcast(broadcastIntent);
                NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                manager.notify(NOTIFICATION_TAG, buildNotification());
            }
        });
    }

    /**
     * On create.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    /**
     * On destroy.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mThread!=null)
            mThread.interrupt();
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(NOTIFICATION_TAG);
        destroy();
    }

    /**
     * Notification builder
     * @return Notification
     */
    private Notification buildNotification() {
        Notification.Builder notificationBuilder = new Notification.Builder(this);

        // Last track intent.
        Intent back = new Intent(ACTION_NEW_TRACK);
        back.putExtra(FORWARD_EXTRA, false);
        PendingIntent backwardIntent = PendingIntent.getBroadcast(this, 0, back, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.addAction(R.drawable.backward, "", backwardIntent);

        // Play toggle intent.
        int play;
        if (mMediaPlayer.isPlaying()){
            play = R.drawable.notification_pause;
        }
        else{
            play = R.drawable.notification_play;
        }
        Intent playToggle = new Intent(ACTION_PLAY_TOGGLE);
        PendingIntent playToggleIntent = PendingIntent.getBroadcast(this, 1, playToggle, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.addAction(play, "", playToggleIntent);

        // Next track intent.
        Intent next = new Intent(ACTION_NEW_TRACK);
        next.putExtra(FORWARD_EXTRA, true);
        PendingIntent forwardIntent = PendingIntent.getBroadcast(this, 2, next, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.addAction(R.drawable.forward, "", forwardIntent);

        Picasso.with(this).load(mList.get(mCurrentTrack).getAlbumImage())
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                        mAlbumBitmap = bitmap;
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {
                    }
                });

        notificationBuilder
                .setSmallIcon(R.drawable.spotify)
                .setColor(getResources().getColor(R.color.green))
                .setContentTitle(mList.get(mCurrentTrack).getTrackName())
                .setContentText(mList.get(mCurrentTrack).getArtistName())
                .setLargeIcon(mAlbumBitmap);

        notificationBuilder.setOngoing(true);

        return notificationBuilder.build();
    }

    /**
     * Binder.
     * @param intent intent.
     * @return IBinder
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
