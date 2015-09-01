package com.xlythe.spotifysteamer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
                    break;
                case PlayerFragment.ACTION_DETAILS:
                    Picasso.with(getActivity()).load(intent.getStringExtra(PlayerFragment.IMAGE_EXTRA)).into(mImage);
                    mTrack.setText(intent.getStringExtra(PlayerFragment.TRACK_EXTRA));
                    mArtist.setText(intent.getStringExtra(PlayerFragment.ARTIST_EXTRA));
                    break;
            }
        }
    };

    public final static String ARTIST_EXTRA = "artist";
    private final static String TRACK_KEY = "track";
    private final static String TIME = "m:ss";

    private Map<String, Object> mQuery = new HashMap<>();
    private ArrayList<TopTracksParcelable> mList = new ArrayList<>();
    private Toast mToast;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private boolean mIsPlaying;
    private int mMediaPosition;
    private int mMediaDuration;

    @Bind(R.id.recycler_view) RecyclerView mRecyclerView;
    @Bind(R.id.image) ImageView mImageView;
    @Bind(R.id.collapsing_toolbar) CollapsingToolbarLayout mCollapsingToolbarLayout;
    @Bind(R.id.fabBtn) FloatingActionButton mFab;
    @Bind(R.id.now_album_image) ImageView mImage;
    @Bind(R.id.now_track_name) TextView mTrack;
    @Bind(R.id.now_artist_name) TextView mArtist;

    public TopTracksFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);
        ButterKnife.bind(this, rootView);

        SpotifyApi api = new SpotifyApi();
        final SpotifyService spotify = api.getService();

        ArtistParcelable artist = getArguments().getParcelable(ARTIST_EXTRA);

        mQuery.put(spotify.COUNTRY, "US");

        Picasso.with(getActivity()).load(artist.getArtistImage()).into(mImageView);

        mCollapsingToolbarLayout.setTitle(artist.getArtistName());

        mToast = Toast.makeText(getActivity(), "No results found.", Toast.LENGTH_SHORT);

        if(savedInstanceState == null || !savedInstanceState.containsKey(TRACK_KEY)) {
            if (isNetworkAvailable()) {
                prepareTopTracks(spotify, artist);
            }
        }
        else{
            mList = savedInstanceState.getParcelableArrayList(TRACK_KEY);
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(PlayerService.ACTION_STATUS);
        filter.addAction(PlayerService.ACTION_POSITION);
        filter.addAction(PlayerService.ACTION_DURATION);
        filter.addAction(PlayerFragment.ACTION_DETAILS);
        getActivity().registerReceiver(mReceiver, filter);

        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new TrackAdapter(mList, getActivity(), new TrackAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {
                Log.d("frag", position+"");
                ((MainActivity) getActivity()).addFragmentPlayer(mList, position);
            }
        });
        mRecyclerView.setAdapter(mAdapter);

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mList.size() > 0) {
                    ((MainActivity) getActivity()).addFragmentPlayer(mList, 0);
                }
            }
        });
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(TRACK_KEY, mList);
        super.onSaveInstanceState(outState);
    }

    //Based on a stackoverflow snippet
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void prepareTopTracks(SpotifyService spotify, ArtistParcelable artist){
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
