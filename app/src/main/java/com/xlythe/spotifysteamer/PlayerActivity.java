package com.xlythe.spotifysteamer;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class PlayerActivity extends Activity {
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

    MediaPlayer mediaPlayer;

    private String mAlbumName;
    private String mAlbumArt;
    private String mTrackName;
    private String mUrl;
    private int mTrackNumber;
    private String mTime = "m:ss";
    private ArrayList<TopTracksParcelable> mList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        ButterKnife.inject(this);

        mList = getIntent().getParcelableArrayListExtra(TRACK_LIST_EXTRA);
        mTrackNumber = getIntent().getIntExtra(POSITION_EXTRA, 0);

        if(savedInstanceState == null || !savedInstanceState.containsKey("key")) {

        }
        else{
            mList = savedInstanceState.getParcelableArrayList("key");
        }

        mTrackName = mList.get(mTrackNumber).getTrackName();
        mAlbumName = mList.get(mTrackNumber).getAlbumName();
        mAlbumArt = mList.get(mTrackNumber).getAlbumImage();
        mUrl = mList.get(mTrackNumber).getPreviewUrl();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mArtist.setText(mList.get(mTrackNumber).getArtistName());
                mAlbum.setText(mAlbumName);
                Picasso.with(getBaseContext()).load(mAlbumArt).into(mImageView);
                mTrack.setText(mTrackName);

                mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                try {
                    mediaPlayer.setDataSource(mUrl);
                    mediaPlayer.prepare();
                } catch (IOException ioe) {
                    Log.d("PlayerActivity", "Media not found");
                }

                mediaPlayer.start();
                final int duration = mediaPlayer.getDuration();
                mDuration.setText(DateFormat.format(mTime, duration));
                mSeekBar.setMax(0);
                mSeekBar.setMax(duration);
                Thread t = new Thread() {
                    @Override
                    public void run() {
                        try {
                            while (!isInterrupted()) {
                                Thread.sleep(100);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        int position = mediaPlayer.getCurrentPosition();
                                        mPosition.setText(DateFormat.format(mTime, position));
                                        mSeekBar.setProgress(position);
                                    }
                                });
                            }
                        } catch (InterruptedException e) {

                        }
                    }
                };
                t.start();

                mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        mediaPlayer.start();
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        mediaPlayer.pause();
                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) {
                            mediaPlayer.seekTo(progress);
                        }

                    }
                });

                mPlay.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.pause();
                            mPlay.setBackgroundResource(android.R.drawable.ic_media_play);
                        } else {
                            mediaPlayer.start();
                            mPlay.setBackgroundResource(android.R.drawable.ic_media_pause);
                        }
                    }
                });

                mNext.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if (mTrackNumber<mList.size()-1)
                            mTrackNumber++;
                        Intent i = new Intent(getBaseContext(), PlayerActivity.class);
                        i.putExtra(TRACK_LIST_EXTRA, mList);
                        i.putExtra(POSITION_EXTRA, mTrackNumber);
                        startActivity(i);
                    }
                });

                mPrevious.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if (mTrackNumber>0)
                            mTrackNumber--;
                        Intent i = new Intent(getBaseContext(), PlayerActivity.class);
                        i.putExtra(TRACK_LIST_EXTRA, mList);
                        i.putExtra(POSITION_EXTRA, mTrackNumber);
                        startActivity(i);
                    }
                });
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
        outState.putParcelableArrayList("key", mList);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayer.stop();
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
