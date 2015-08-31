package com.xlythe.spotifysteamer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    private boolean mTwoPane;
    private boolean mPlayerVisible;
    private final static String PLAYER_KEY = "player_visible";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        args.putParcelable(TopTracksFragment.ARTIST_EXTRA, artist);

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

    public void addFragmentPlayer(ArrayList<TopTracksParcelable> tracks, int position){
        Bundle args = new Bundle();
        args.putParcelableArrayList(PlayerFragment.TRACK_LIST_EXTRA, tracks);
        args.putInt(PlayerFragment.POSITION_EXTRA, position);

        PlayerFragment fragment = new PlayerFragment();
        fragment.setArguments(args);
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
