package com.xlythe.spotifysteamer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PlayerFragment extends Fragment {
    private final static String TIME = "m:ss";

    @Bind(R.id.play) Button mPlay;
    @Bind(R.id.next) Button mNext;
    @Bind(R.id.previous) Button mPrevious;
    @Bind(R.id.duration) TextView mDuration;
    @Bind(R.id.position) TextView mPosition;
    @Bind(R.id.seekBar) SeekBar mSeekBar;
    @Bind(R.id.artist_name) TextView mArtist;
    @Bind(R.id.album_name) TextView mAlbum;
    @Bind(R.id.album_image) ImageView mImageView;
    @Bind(R.id.track_name) TextView mTrack;

    private boolean mIsPlaying;
    private String mAlbumName;
    private String mAlbumArt;
    private String mTrackName;
    private String mArtistName;
    private int mMediaPosition;
    private int mMediaDuration;

    /**
     * Broadcast receiver that gets play/pause info, track details, and playback position.
     */
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch(action) {
                case PlayerService.ACTION_STATUS:
                    mIsPlaying = intent.getBooleanExtra(PlayerService.IS_PLAYING_EXTRA, true);
                    invalidateUI();
                    break;
                case PlayerService.ACTION_PLAYBACK_POSITION:
                    mMediaPosition = intent.getIntExtra(PlayerService.PLAYBACK_POSITION_EXTRA, 0);
                    invalidateUI();
                    break;
                case PlayerService.ACTION_DETAILS:
                    mMediaDuration = intent.getIntExtra(PlayerService.DURATION_EXTRA, 0);
                    mAlbumArt = intent.getStringExtra(PlayerService.IMAGE_EXTRA);
                    mAlbumName = intent.getStringExtra(PlayerService.ALBUM_EXTRA);
                    mTrackName = intent.getStringExtra(PlayerService.TRACK_EXTRA);
                    mArtistName = intent.getStringExtra(PlayerService.ARTIST_EXTRA);
                    invalidateUI();
                    break;
            }
        }
    };

    /**
     * Empty constructor.
     */
    public PlayerFragment() {
    }

    /**
     * On create view.
     * @param inflater layout inflater.
     * @param container view group container.
     * @param savedInstanceState saved state.
     * @return View
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_player, container, false);
        ButterKnife.bind(this, rootView);

        // Register broadcast receiver.
        IntentFilter filter = new IntentFilter();
        filter.addAction(PlayerService.ACTION_STATUS);
        filter.addAction(PlayerService.ACTION_PLAYBACK_POSITION);
        filter.addAction(PlayerService.ACTION_DETAILS);
        getActivity().registerReceiver(mReceiver, filter);

        // Check for seek bar changes.
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (!mIsPlaying) {
                    Intent broadcastIntent = new Intent(PlayerService.ACTION_PLAY_TOGGLE);
                    getActivity().sendBroadcast(broadcastIntent);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (mIsPlaying) {
                    Intent broadcastIntent = new Intent(PlayerService.ACTION_PLAY_TOGGLE);
                    getActivity().sendBroadcast(broadcastIntent);
                }
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    Intent broadcastIntent = new Intent(PlayerService.ACTION_SEEK_TO);
                    broadcastIntent.putExtra(PlayerService.SEEK_TO_EXTRA, progress);
                    getActivity().sendBroadcast(broadcastIntent);
                }
            }
        });

        // Toggle play/pause.
        mPlay.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent broadcastIntent = new Intent(PlayerService.ACTION_PLAY_TOGGLE);
                getActivity().sendBroadcast(broadcastIntent);
            }
        });

        // Move to next track.
        mNext.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendBroadcast(true);
            }
        });

        // Move to previous track.
        mPrevious.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendBroadcast(false);
            }
        });
        return rootView;
    }

    /**
     * Reload the ui.
     */
    private void invalidateUI(){
        mDuration.setText(DateFormat.format(TIME, mMediaDuration));
        mSeekBar.setMax(mMediaDuration);

        Picasso.with(getActivity()).load(mAlbumArt).into(mImageView);
        mTrack.setText(mTrackName);
        mAlbum.setText(mAlbumName);
        mArtist.setText(mArtistName);

        if (!mIsPlaying) {
            mPlay.setBackgroundResource(android.R.drawable.ic_media_play);
        } else {
            mPlay.setBackgroundResource(android.R.drawable.ic_media_pause);
        }

        mPosition.setText(DateFormat.format(TIME, mMediaPosition));
        mSeekBar.setProgress(mMediaPosition);
    }

    /**
     * Move forward or backward a track.
     * @param forward boolean.
     */
    private void sendBroadcast(boolean forward){
        Intent broadcastIntent = new Intent(PlayerService.ACTION_NEW_TRACK);
        broadcastIntent.putExtra(PlayerService.FORWARD_EXTRA, forward);
        getActivity().sendBroadcast(broadcastIntent);
    }

    /**
     * Unregister receiver.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mReceiver);
    }

}
