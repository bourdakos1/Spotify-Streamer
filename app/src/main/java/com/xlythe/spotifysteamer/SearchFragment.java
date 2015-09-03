package com.xlythe.spotifysteamer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.Bind;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SearchFragment extends Fragment {
    private final static String ARTIST_KEY = "artist";

    private ArtistAdapter mAdapter;
    private Toast mToast;
    private ArrayList<ArtistParcelable> mList = new ArrayList<>();
    private Activity mActivity;

    @Bind(R.id.list_view) ListView mListView;
    @Bind(R.id.not_found) ImageView mNotFound;
    @Bind(R.id.search_view) EditText mSearchView;
    @Bind(R.id.clear) ImageButton mClear;
    @Bind(R.id.fabBtn) FloatingActionButton mFab;

    public SearchFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);
        ButterKnife.bind(this,rootView);

        if(savedInstanceState != null && savedInstanceState.containsKey(ARTIST_KEY)) {
            mList = savedInstanceState.getParcelableArrayList(ARTIST_KEY);
        }

        mAdapter = new ArtistAdapter(rootView.getContext(), R.layout.artist_item, mList);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> av, View v, int position, long l) {
                ((MainActivity) mActivity).replaceFragment(mList.get(position));
            }
        });

        mSearchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (isNetworkAvailable()) {
                    populateArtists(charSequence);
                } else {
                    Toast.makeText(mActivity, "No network connection.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        mClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSearchView.setText("");
            }
        });
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(mActivity, PreferenceActivity.class);
                startActivity(i);
            }
        });
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mSearchView.setFocusable(false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(ARTIST_KEY, mList);
        super.onSaveInstanceState(outState);
    }

    //Based on a stackoverflow snippet
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) mActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void populateArtists(CharSequence charSequence){
        SpotifyApi api = new SpotifyApi();
        final SpotifyService spotify = api.getService();
        mToast = Toast.makeText(mActivity, "No results found.", Toast.LENGTH_SHORT);
        spotify.searchArtists(charSequence.toString(), new Callback<ArtistsPager>() {
            @Override
            public void success(ArtistsPager artistsPager, Response response) {
                mList.clear();
                for (Artist artist : artistsPager.artists.items) {
                    mList.add(new ArtistParcelable(artist));
                }
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                        if (mList.size() == 0) {
                            mToast.show();
                            mNotFound.setVisibility(View.VISIBLE);
                            mListView.setVisibility(View.GONE);
                        } else {
                            mToast.cancel();
                            mNotFound.setVisibility(View.GONE);
                            mListView.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("Spotify", error.toString());
                mList.clear();
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                        mNotFound.setVisibility(View.GONE);
                        mListView.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }
}
