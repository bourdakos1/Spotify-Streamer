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
    private boolean mNowPlaying;

    /**
     * Broadcast receiver that gets play/pause info, track details, and playback position.
     */
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch(action) {
                case PlayerService.ACTION_STATUS:
                    // Music service is currently running.
                    mNowPlaying = intent.getBooleanExtra(PlayerService.HAS_STARTED_EXTRA, false);
                    break;
            }
        }
    };

    /**
     * On create.
     * @param savedInstanceState saved state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Register broadcast receiver.
        IntentFilter filter = new IntentFilter();
        filter.addAction(PlayerService.ACTION_STATUS);
        getApplicationContext().registerReceiver(mReceiver, filter);

        // Check if it is a tablet.
        if (findViewById(R.id.tablet) != null) {
            mTwoPane = true;
        } else {
            mTwoPane = false;
        }

        // If there isn't a saved state add the search and now playing fragment.
        if (savedInstanceState == null) {
            // Load search fragment.
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_search, new SearchFragment())
                    .commit();

            // Load now playing fragment and hide it.
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.now_playing, new NowPlayingFragment())
                    .commit();
            findViewById(R.id.now_playing).setVisibility(View.GONE);
        }

        // If saved state exists, check if player should be visible.
        else {
            mPlayerVisible = savedInstanceState.getBoolean(PLAYER_KEY);

            // If player should be visible and is two pain, show player fragment.
            if (mPlayerVisible && mTwoPane) {
                findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);
            }
        }

        showNowPlaying();
    }

    /**
     * Add Top Tracks fragment.
     * @param artist artist.
     */
    public void replaceFragment(ArtistParcelable artist){
        Bundle args = new Bundle();
        args.putParcelable(ARTIST_EXTRA, artist);

        TopTracksFragment fragment = new TopTracksFragment();
        fragment.setArguments(args);

        // If single pane replace search fragment.
        if(!mTwoPane) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_search, fragment).addToBackStack(null)
                    .commit();
        }

        // Else add it next to search fragment.
        else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_top_tracks, fragment).addToBackStack(null)
                    .commit();
        }

        showNowPlaying();
    }

    /**
     * Add player fragment.
     */
    public void addFragmentPlayer(){
        // Player should be visible
        mPlayerVisible = true;
        PlayerFragment fragment = new PlayerFragment();

        // If single pane replace top tracks.
        if(!mTwoPane) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_search, fragment).addToBackStack(null)
                    .commit();
        }

        // If dual pane show player.
        else {
            findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_player, fragment).addToBackStack(null)
                    .commit();
        }

        showNowPlaying();
    }

    /**
     * Save state.
     * @param outState out state.
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(PLAYER_KEY, mPlayerVisible);
        super.onSaveInstanceState(outState);
    }

    /**
     * Hide player when back pressed.
     */
    @Override
    public void onBackPressed() {
        MainActivity.super.onBackPressed();
        // Player should be gone.
        mPlayerVisible = false;
        if(mTwoPane) {
            findViewById(R.id.fragment_container).setVisibility(View.GONE);
        }
        showNowPlaying();
    }

    /**
     * Determine whether or not to show now playing.
     */
    public void showNowPlaying(){
        // If music service is active and the player is hidden show now playing.
        if (mNowPlaying && !mPlayerVisible){
            findViewById(R.id.now_playing).setVisibility(View.VISIBLE);
        }
        else {
            findViewById(R.id.now_playing).setVisibility(View.GONE);
        }
    }
}
