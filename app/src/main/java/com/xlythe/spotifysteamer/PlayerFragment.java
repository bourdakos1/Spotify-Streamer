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

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PlayerFragment extends Fragment {
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch(action) {
                case PlayerService.ACTION_STATUS:
                    mIsPlaying = intent.getBooleanExtra(PlayerService.IS_PLAYING_EXTRA, true);
                    break;
                case PlayerService.ACTION_POSITION:
                    mMediaPosition = intent.getIntExtra(PlayerService.POSITION_EXTRA, 0);
                    break;
                case PlayerService.ACTION_DURATION:
                    mMediaDuration = intent.getIntExtra(PlayerService.DURATION_EXTRA, 0);
                    mDuration.setText(DateFormat.format(mTime, mMediaDuration));
                    mSeekBar.setMax(0);
                    mSeekBar.setMax(mMediaDuration);
                    break;
            }
        }
    };

    public final static String TRACK_LIST_EXTRA = "track_list";
    public final static String POSITION_EXTRA = "position";
    private final static String TRACK_KEY = "track";
    private final static String POSITION_KEY = "position";
    private final static String mTime = "m:ss";

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
    private int mTrackNumber;
    private int mMediaPosition;
    private int mMediaDuration;
    private ArrayList<TopTracksParcelable> mList = new ArrayList<>();
    private Thread mThread;

    public PlayerFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_player, container, false);
        ButterKnife.bind(this, rootView);

        if(savedInstanceState != null && savedInstanceState.containsKey(TRACK_KEY) && savedInstanceState.containsKey(POSITION_KEY)) {
            mList = savedInstanceState.getParcelableArrayList(TRACK_KEY);
            mTrackNumber = savedInstanceState.getInt(POSITION_KEY);
        }
        else {
            mList = getArguments().getParcelableArrayList(TRACK_LIST_EXTRA);
            mTrackNumber = getArguments().getInt(POSITION_EXTRA, 0);
        }

        Intent serviceIntent = new Intent(getActivity(), PlayerService.class);
        serviceIntent.putExtra(PlayerService.URL_EXTRA, mList.get(mTrackNumber).getPreviewUrl());
        getActivity().startService(serviceIntent);

        IntentFilter filter = new IntentFilter();
        filter.addAction(PlayerService.ACTION_STATUS);
        filter.addAction(PlayerService.ACTION_POSITION);
        filter.addAction(PlayerService.ACTION_DURATION);
        getActivity().registerReceiver(mReceiver, filter);

        invalidateUI();

        mThread = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(100);
                        if (getActivity()!=null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mPosition.setText(DateFormat.format(mTime, mMediaPosition));
                                    mSeekBar.setProgress(mMediaPosition);
                                }
                            });
                        }
                    }
                } catch (InterruptedException e) {

                }
            }
        };
        mThread.start();

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Intent broadcastIntent = new Intent(PlayerService.ACTION_PLAY_TRACK);
                getActivity().sendBroadcast(broadcastIntent);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Intent broadcastIntent = new Intent(PlayerService.ACTION_PAUSE_TRACK);
                getActivity().sendBroadcast(broadcastIntent);
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    Intent broadcastIntent = new Intent(PlayerService.ACTION_SEEK_TO);
                    broadcastIntent.putExtra(PlayerService.PROGRESS_EXTRA, progress);
                    getActivity().sendBroadcast(broadcastIntent);
                }

            }
        });

        mPlay.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mIsPlaying) {
                    Intent broadcastIntent = new Intent(PlayerService.ACTION_PAUSE_TRACK);
                    getActivity().sendBroadcast(broadcastIntent);
                    mPlay.setBackgroundResource(android.R.drawable.ic_media_play);
                } else {
                    Intent broadcastIntent = new Intent(PlayerService.ACTION_PLAY_TRACK);
                    getActivity().sendBroadcast(broadcastIntent);
                    mPlay.setBackgroundResource(android.R.drawable.ic_media_pause);
                }
            }
        });

        mNext.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mTrackNumber<mList.size()-1)
                    mTrackNumber++;
                sendBroadcast();
            }
        });

        mPrevious.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mTrackNumber>0)
                    mTrackNumber--;
                sendBroadcast();
            }
        });

        return rootView;
    }

    private void invalidateUI(){
        mTrackName = mList.get(mTrackNumber).getTrackName();
        mAlbumName = mList.get(mTrackNumber).getAlbumName();
        mAlbumArt = mList.get(mTrackNumber).getAlbumImage();

        if (mIsPlaying) {
            mPlay.setBackgroundResource(android.R.drawable.ic_media_play);
        } else {
            mPlay.setBackgroundResource(android.R.drawable.ic_media_pause);
        }

        mArtist.setText(mList.get(mTrackNumber).getArtistName());
        mAlbum.setText(mAlbumName);
        Picasso.with(getActivity()).load(mAlbumArt).into(mImageView);
        mTrack.setText(mTrackName);
    }

    private void sendBroadcast(){
        invalidateUI();
        Intent broadcastIntent = new Intent(PlayerService.ACTION_NEW_TRACK);
        broadcastIntent.putExtra(PlayerService.URL_EXTRA, mList.get(mTrackNumber).getPreviewUrl());
        getActivity().sendBroadcast(broadcastIntent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(TRACK_KEY, mList);
        outState.putInt(POSITION_KEY, mTrackNumber);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mThread!=null)
            mThread.interrupt();
        getActivity().unregisterReceiver(mReceiver);
    }

}
