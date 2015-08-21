package com.xlythe.spotifysteamer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class PlayerActivity extends Activity {
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(PlayerService.ACTION_STATUS)) {
                mIsPlaying = intent.getBooleanExtra(PlayerService.IS_PLAYING_EXTRA, true);
            }
            else if(intent.getAction().equals(PlayerService.ACTION_POSITION)){
                mMediaPosition = intent.getIntExtra(PlayerService.POSITION_EXTRA, 0);
            }
            else if(intent.getAction().equals(PlayerService.ACTION_DURATION)){
                mMediaDuration = intent.getIntExtra(PlayerService.DURATION_EXTRA, 0);
                mDuration.setText(DateFormat.format(mTime, mMediaDuration));
                mSeekBar.setMax(0);
                mSeekBar.setMax(mMediaDuration);
            }
        }
    };

    public static String TRACK_LIST_EXTRA = "track_list";
    public static String POSITION_EXTRA = "position";

    @InjectView(R.id.play) Button mPlay;
    @InjectView(R.id.next) Button mNext;
    @InjectView(R.id.previous) Button mPrevious;
    @InjectView(R.id.duration) TextView mDuration;
    @InjectView(R.id.position) TextView mPosition;
    @InjectView(R.id.seekBar) SeekBar mSeekBar;
    @InjectView(R.id.artist_name) TextView mArtist;
    @InjectView(R.id.album_name) TextView mAlbum;
    @InjectView(R.id.album_image) ImageView mImageView;
    @InjectView(R.id.track_name) TextView mTrack;

    private boolean mIsPlaying;
    private String mAlbumName;
    private String mAlbumArt;
    private String mTrackName;
    private int mTrackNumber;
    private int mMediaPosition;
    private int mMediaDuration;
    private String mTime = "m:ss";
    private ArrayList<TopTracksParcelable> mList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        ButterKnife.inject(this);

        mList = getIntent().getParcelableArrayListExtra(TRACK_LIST_EXTRA);
        mTrackNumber = getIntent().getIntExtra(POSITION_EXTRA, 0);

        mTrackName = mList.get(mTrackNumber).getTrackName();
        mAlbumName = mList.get(mTrackNumber).getAlbumName();
        mAlbumArt = mList.get(mTrackNumber).getAlbumImage();

        Intent serviceIntent = new Intent(getBaseContext(), PlayerService.class);
        serviceIntent.putExtra(PlayerService.URL_EXTRA, mList.get(mTrackNumber).getPreviewUrl());
        startService(serviceIntent);

        IntentFilter filter = new IntentFilter();
        filter.addAction(PlayerService.ACTION_STATUS);
        filter.addAction(PlayerService.ACTION_POSITION);
        filter.addAction(PlayerService.ACTION_DURATION);
        getApplicationContext().registerReceiver(mReceiver, filter);

        mArtist.setText(mList.get(mTrackNumber).getArtistName());
        mAlbum.setText(mAlbumName);
        Picasso.with(getBaseContext()).load(mAlbumArt).into(mImageView);
        mTrack.setText(mTrackName);

        new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(100);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mPosition.setText(DateFormat.format(mTime, mMediaPosition));
                                mSeekBar.setProgress(mMediaPosition);
                            }
                        });
                    }
                } catch (InterruptedException e) {

                }
            }
        }.start();

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Intent broadcastIntent = new Intent(PlayerService.ACTION_PLAY_TRACK);
                sendBroadcast(broadcastIntent);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Intent broadcastIntent = new Intent(PlayerService.ACTION_PAUSE_TRACK);
                sendBroadcast(broadcastIntent);
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    Intent broadcastIntent = new Intent(PlayerService.ACTION_SEEK_TO);
                    broadcastIntent.putExtra(PlayerService.PROGRESS_EXTRA, progress);
                    sendBroadcast(broadcastIntent);
                }

            }
        });

        mPlay.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mIsPlaying) {
                    Intent broadcastIntent = new Intent(PlayerService.ACTION_PAUSE_TRACK);
                    sendBroadcast(broadcastIntent);
                    mPlay.setBackgroundResource(android.R.drawable.ic_media_play);
                } else {
                    Intent broadcastIntent = new Intent(PlayerService.ACTION_PLAY_TRACK);
                    sendBroadcast(broadcastIntent);
                    mPlay.setBackgroundResource(android.R.drawable.ic_media_pause);
                }
            }
        });

        mNext.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mTrackNumber<mList.size()-1)
                    mTrackNumber++;
                Intent i = getIntent();
                i.putExtra(TRACK_LIST_EXTRA, mList);
                i.putExtra(POSITION_EXTRA, mTrackNumber);
                finish();
                startActivity(i);
            }
        });

        mPrevious.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mTrackNumber>0)
                    mTrackNumber--;
                Intent i = getIntent();
                i.putExtra(TRACK_LIST_EXTRA, mList);
                i.putExtra(POSITION_EXTRA, mTrackNumber);
                finish();
                startActivity(i);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_player, menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
