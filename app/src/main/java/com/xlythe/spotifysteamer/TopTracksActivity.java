package com.xlythe.spotifysteamer;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class TopTracksActivity extends Activity {
    public static String ARTIST_NAME_EXTRA = "artist_name";
    public static String ARTIST_ID_EXTRA = "artist_id";

    private String mArtistName;
    private String mArtistId;
    private Map<String, Object> mQuery = new HashMap<>();
    private ArrayList<TopTracksParcelable> mList = new ArrayList<>();
    private TrackAdapter mAdapter;

    @InjectView(R.id.list_view) ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);
        ButterKnife.inject(this);

        SpotifyApi api = new SpotifyApi();
        final SpotifyService spotify = api.getService();

        mArtistName = getIntent().getStringExtra(ARTIST_NAME_EXTRA);
        mArtistId = getIntent().getStringExtra(ARTIST_ID_EXTRA);
        mQuery.put(spotify.COUNTRY, "US");

        getActionBar().setSubtitle(mArtistName);

        spotify.getArtistTopTrack(mArtistId, mQuery,new Callback<Tracks>() {
            @Override
            public void success(Tracks tracks, Response response) {
                mList.clear();
                for (Track track : tracks.tracks) {
                    mList.add(new TopTracksParcelable(track));
                }
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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(TopTracksActivity.this, "No tracks found.", Toast.LENGTH_SHORT).show();
                    }
                });
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
