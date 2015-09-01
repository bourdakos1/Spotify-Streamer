package com.xlythe.spotifysteamer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    public final static String ARTIST_EXTRA = "artist";
    private final static String PLAYER_KEY = "player_visible";

    private boolean mTwoPane;
    private boolean mPlayerVisible;

    /**
     * Broadcast receiver that gets play/pause info, track details, and playback position.
     */
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch(action) {
                case PlayerService.ACTION_STATUS:
                    if (intent.getBooleanExtra(PlayerService.HAS_STARTED_EXTRA, false)){
                        findViewById(R.id.now_playing).setVisibility(View.VISIBLE);
                    }
                    else {
                        findViewById(R.id.now_playing).setVisibility(View.GONE);
                    }
                    removeStickyBroadcast(intent);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IntentFilter filter = new IntentFilter();
        filter.addAction(PlayerService.ACTION_STATUS);
        getApplicationContext().registerReceiver(mReceiver, filter);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.now_playing, new NowPlayingFragment())
                .commit();
        findViewById(R.id.now_playing).setVisibility(View.GONE);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_search, new SearchFragment())
                    .commit();
        }
        else {
            mPlayerVisible = savedInstanceState.getBoolean(PLAYER_KEY);
            if (mPlayerVisible && (findViewById(R.id.tablet) != null)) {
                findViewById(R.id.fragment_player).setVisibility(View.VISIBLE);
            }
        }
        if (findViewById(R.id.tablet) != null) {
            mTwoPane = true;
        } else {
            mTwoPane = false;
        }
    }

    public void replaceFragment(ArtistParcelable artist){
        Bundle args = new Bundle();
        args.putParcelable(ARTIST_EXTRA, artist);

        TopTracksFragment fragment = new TopTracksFragment();
        fragment.setArguments(args);
        if(!mTwoPane) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_search, fragment).addToBackStack(null)
                    .commit();
        }
        else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_top_tracks, fragment).addToBackStack(null)
                    .commit();
        }
    }

    public void addFragmentPlayer(){
        PlayerFragment fragment = new PlayerFragment();
        if(!mTwoPane) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_search, fragment).addToBackStack(null)
                    .commit();
        }
        else {
            mPlayerVisible = true;
            findViewById(R.id.fragment_player).setVisibility(View.VISIBLE);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_player, fragment).addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(PLAYER_KEY, mPlayerVisible);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        MainActivity.super.onBackPressed();
        if(mTwoPane) {
            mPlayerVisible = false;
            findViewById(R.id.fragment_player).setVisibility(View.GONE);
        }
    }
}
