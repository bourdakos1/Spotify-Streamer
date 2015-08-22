package com.xlythe.spotifysteamer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import kaaes.spotify.webapi.android.models.Artist;

/**
 * Created by Niko on 6/8/15.
 */
public class ArtistAdapter extends ArrayAdapter<ArtistParcelable>{

    private Context mContext;
    private int mLayoutResourceId;
    private ArrayList<ArtistParcelable> mArtists;

    public ArtistAdapter(Context context, int layoutResourceId, ArrayList<ArtistParcelable> artists) {
        super(context, layoutResourceId, artists);
        mLayoutResourceId = layoutResourceId;
        mContext = context;
        mArtists = artists;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewHolder holder;
        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            view = inflater.inflate(mLayoutResourceId, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }

        ArtistParcelable artist = mArtists.get(position);

        holder.artist.setText(artist.getArtistName());
        Picasso.with(mContext).load(artist.getArtistImage()).into(holder.image);

        return view;
    }

    static class ViewHolder {
        @Bind(R.id.image) ImageView image;
        @Bind(R.id.text) TextView artist;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
