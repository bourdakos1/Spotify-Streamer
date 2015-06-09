package com.xlythe.spotifysteamer;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class MainActivity extends Activity {

    private List<Artist> mList = new ArrayList<>();
    private ListView mListView;
    private ArtistAdapter mAdapter;
    private Toast mToast;
    private ImageView mNotFound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.options_menu, menu);

        mListView = (ListView) findViewById(R.id.list_view);
        mNotFound = (ImageView) findViewById(R.id.not_found);
        mToast = Toast.makeText(MainActivity.this, "No results found.", Toast.LENGTH_SHORT);

        SpotifyApi api = new SpotifyApi();
        final SpotifyService spotify = api.getService();

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            public boolean onQueryTextChange(String newText) {
                spotify.searchArtists(newText, new Callback<ArtistsPager>() {
                    @Override
                    public void success(ArtistsPager artistsPager, Response response) {
                        mList.clear();
                        mList.addAll(artistsPager.artists.items);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mAdapter.notifyDataSetChanged();
                                if (mList.size()==0) {
                                    mToast.show();
                                    mNotFound.setVisibility(View.VISIBLE);
                                    mListView.setVisibility(View.GONE);
                                }
                                else {
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
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mAdapter.notifyDataSetChanged();
                                mNotFound.setVisibility(View.GONE);
                                mListView.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                });
                return true;
            }

            public boolean onQueryTextSubmit(String query) {
                return true;
            }
        });
        mAdapter = new ArtistAdapter(this, R.layout.artist_item, mList);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> av, View v, int position, long l) {
                Intent i = new Intent(getBaseContext(), TopTracks.class);
                i.putExtra(TopTracks.ARTIST_NAME_EXTRA, ((Artist) v.getTag()).name);
                i.putExtra(TopTracks.ARTIST_ID_EXTRA, ((Artist) v.getTag()).id);
                startActivity(i);
            }
        });
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
