package com.xlythe.spotifysteamer;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class TopTracks extends Activity {
    public static String ARTIST_NAME_EXTRA = "artist_name";
    public static String ARTIST_ID_EXTRA = "artist_id";

    private String mArtistName;
    private String mArtistId;
    private Map<String, Object> mQuery = new HashMap<>();
    private List<Track> mList = new ArrayList<>();
    private ListView mListView;
    private TrackAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);

        SpotifyApi api = new SpotifyApi();
        final SpotifyService spotify = api.getService();

        mArtistName = getIntent().getStringExtra(ARTIST_NAME_EXTRA);
        mArtistId = getIntent().getStringExtra(ARTIST_ID_EXTRA);
        mListView = (ListView) findViewById(R.id.list_view);
        mQuery.put(spotify.COUNTRY, "US");

        getActionBar().setSubtitle(mArtistName);

        spotify.getArtistTopTrack(mArtistId, mQuery,new Callback<Tracks>() {
            @Override
            public void success(Tracks tracks, Response response) {
                mList.clear();
                mList.addAll(tracks.tracks);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("Spotify", error.toString());
            }
        });
        mAdapter = new TrackAdapter(this, R.layout.track_item, mList);
        mListView.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_top_tracks, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
