package com.xlythe.spotifysteamer;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class TopTracksFragment extends Fragment {
    public final static String CURRENT_TRACK_EXTRA = "current_track";
    public final static String TRACK_LIST_EXTRA = "track_list";
    private final static String TRACK_KEY = "track";

    private ArrayList<TopTracksParcelable> mList = new ArrayList<>();
    private Toast mToast;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Bind(R.id.recycler_view) RecyclerView mRecyclerView;
    @Bind(R.id.image) ImageView mImageView;
    @Bind(R.id.collapsing_toolbar) CollapsingToolbarLayout mCollapsingToolbarLayout;
    @Bind(R.id.fabBtn) FloatingActionButton mFab;

    /**
     * Empty constructor.
     */
    public TopTracksFragment() {
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
        View rootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);
        ButterKnife.bind(this, rootView);

        // Set ui.
        ArtistParcelable artist = getArguments().getParcelable(MainActivity.ARTIST_EXTRA);
        Picasso.with(getActivity()).load(artist.getArtistImage()).into(mImageView);
        mCollapsingToolbarLayout.setTitle(artist.getArtistName());

        // Check for saved state and restore data.
        if(savedInstanceState == null || !savedInstanceState.containsKey(TRACK_KEY)) {
            if (isNetworkAvailable()) {
                prepareTopTracks(artist);
            }
        }
        else{
            mList = savedInstanceState.getParcelableArrayList(TRACK_KEY);
        }

        // Fill recycler view.
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new TrackAdapter(mList, getActivity(), new TrackAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                ((MainActivity) getActivity()).addFragmentPlayer();
                startService(position);
            }
        });
        mRecyclerView.setAdapter(mAdapter);

        // Catch on click for FAB.
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mList.size() > 0) {
                    ((MainActivity) getActivity()).addFragmentPlayer();
                    startService(0);
                }
            }
        });
        return rootView;
    }

    /**
     * Start player service.
     * @param position of the track to play.
     */
    public void startService(int position){
        Intent serviceIntent = new Intent(getActivity(), PlayerService.class);
        serviceIntent.putExtra(TRACK_LIST_EXTRA, mList);
        serviceIntent.putExtra(CURRENT_TRACK_EXTRA, position);
        getActivity().startService(serviceIntent);
    }

    /**
     * Save state.
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(TRACK_KEY, mList);
        super.onSaveInstanceState(outState);
    }

    //Based on a stackoverflow snippet
    /**
     * Check for network availability.
     * @return true if available
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Build list of tracks given artist id.
     * @param artist id for querying database.
     */
    public void prepareTopTracks(ArtistParcelable artist){
        SpotifyApi api = new SpotifyApi();
        final SpotifyService spotify = api.getService();
        Map<String, Object> mQuery = new HashMap<>();
        mQuery.put(spotify.COUNTRY, "US");
        mToast = Toast.makeText(getActivity(), "No results found.", Toast.LENGTH_SHORT);
        spotify.getArtistTopTrack(artist.getArtistId(), mQuery,new Callback<Tracks>() {
            @Override
            public void success(Tracks tracks, Response response) {
                mList.clear();
                for (Track track : tracks.tracks) {
                    mList.add(new TopTracksParcelable(track));
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                        if (mList.size() == 0) {
                            mToast.show();
                        } else {
                            mToast.cancel();
                        }
                    }
                });
            }
            @Override
            public void failure(RetrofitError error) {
                Log.d("TopTracksActivity", error.toString());
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "No tracks found.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
