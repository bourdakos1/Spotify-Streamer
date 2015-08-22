package com.xlythe.spotifysteamer;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
    public final static String ARTIST_EXTRA = "artist";
    private final static String TRACK_KEY = "track";

    private Map<String, Object> mQuery = new HashMap<>();
    private ArrayList<TopTracksParcelable> mList = new ArrayList<>();

    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    @Bind(R.id.recycler_view) RecyclerView mRecyclerView;
    @Bind(R.id.image) ImageView mImageView;
    @Bind(R.id.collapsing_toolbar) CollapsingToolbarLayout mCollapsingToolbarLayout;

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

        if(savedInstanceState == null || !savedInstanceState.containsKey(TRACK_KEY)) {
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
        else{
            mList = savedInstanceState.getParcelableArrayList(TRACK_KEY);
        }

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
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(TRACK_KEY, mList);
        super.onSaveInstanceState(outState);
    }
}
