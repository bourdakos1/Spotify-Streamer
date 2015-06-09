package com.xlythe.spotifysteamer;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;

/**
 * Created by Niko on 6/8/15.
 */
public class ArtistAdapter extends ArrayAdapter<Artist>{

    private Context mContext;
    private int mLayoutResourceId;
    private List<Artist> mArtists;

    public ArtistAdapter(Context context, int layoutResourceId, List<Artist> artists) {
        super(context, layoutResourceId, artists);
        mLayoutResourceId = layoutResourceId;
        mContext = context;
        mArtists = artists;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(mLayoutResourceId, parent, false);
        Artist artist = mArtists.get(position);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.image);
        TextView textView = (TextView) rowView.findViewById(R.id.text);
        rowView.setTag(artist.name);
        textView.setText(artist.name);
        if (artist.images.size()>0) {
            Picasso.with(mContext).load(artist.images.get(0).url).into(imageView);
        }
        return rowView;
    }
}
