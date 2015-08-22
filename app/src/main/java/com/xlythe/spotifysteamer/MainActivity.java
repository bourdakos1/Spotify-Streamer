package com.xlythe.spotifysteamer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.tablet) != null) {
            mTwoPane = true;
        } else {
            mTwoPane = false;
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_search, new SearchFragment())
                    .commit();
        }
    }

    public void replaceFragment(ArtistParcelable artist){
        Bundle args = new Bundle();
        args.putParcelable(TopTracksFragment.ARTIST_EXTRA, artist);

        TopTracksFragment fragment = new TopTracksFragment();
        fragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_search, fragment).addToBackStack(null)
                .commit();
    }

    public void addFragmentPlayer(ArrayList<TopTracksParcelable> tracks, int position){
        Bundle args = new Bundle();
        args.putParcelableArrayList(PlayerFragment.TRACK_LIST_EXTRA, tracks);
        args.putInt(PlayerFragment.POSITION_EXTRA, position);

        PlayerFragment fragment = new PlayerFragment();
        fragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_search, fragment).addToBackStack(null)
                .commit();
    }
}
