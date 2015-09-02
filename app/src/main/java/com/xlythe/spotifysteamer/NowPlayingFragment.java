package com.xlythe.spotifysteamer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

public class NowPlayingFragment extends Fragment {
    private boolean mIsPlaying;
    private int mMediaPosition;
    private int mMediaDuration;

    @Bind(R.id.now_album_image) ImageView mImage;
    @Bind(R.id.now_track_name) TextView mTrack;
    @Bind(R.id.now_artist_name) TextView mArtist;
    @Bind(R.id.now_playing_bar) RelativeLayout mNowPlayingBar;
    @Bind(R.id.now_play) ImageButton mPlay;
    @Bind(R.id.progress) ProgressBar mProgress;

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
                    if (!mIsPlaying) {
                        mPlay.setBackgroundResource(R.drawable.now_play);
                    } else {
                        mPlay.setBackgroundResource(R.drawable.now_pause);
                    }
                    break;
                case PlayerService.ACTION_PLAYBACK_POSITION:
                    mMediaPosition = intent.getIntExtra(PlayerService.PLAYBACK_POSITION_EXTRA, 0);
                    mProgress.setProgress(mMediaPosition);
                    break;
                case PlayerService.ACTION_DETAILS:
                    Picasso.with(getActivity()).load(intent.getStringExtra(PlayerService.IMAGE_EXTRA)).into(mImage);
                    mMediaDuration = intent.getIntExtra(PlayerService.DURATION_EXTRA, 0);
                    mProgress.setMax(mMediaDuration);
                    mTrack.setText(intent.getStringExtra(PlayerService.TRACK_EXTRA));
                    mArtist.setText(intent.getStringExtra(PlayerService.ARTIST_EXTRA));
                    break;
            }
        }
    };

    /**
     * Empty constructor.
     */
    public NowPlayingFragment() {
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
        View rootView = inflater.inflate(R.layout.fragment_now_playing, container, false);
        ButterKnife.bind(this, rootView);

        // Register broadcast receiver.
        IntentFilter filter = new IntentFilter();
        filter.addAction(PlayerService.ACTION_STATUS);
        filter.addAction(PlayerService.ACTION_PLAYBACK_POSITION);
        filter.addAction(PlayerService.ACTION_DETAILS);
        getActivity().registerReceiver(mReceiver, filter);

        // Toggle play/pause.
        mPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent broadcastIntent = new Intent(PlayerService.ACTION_PLAY_TOGGLE);
                getActivity().sendBroadcast(broadcastIntent);
            }
        });

        // Open player fragment.
        mNowPlayingBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).addFragmentPlayer();
            }
        });

        return rootView;
    }
}
