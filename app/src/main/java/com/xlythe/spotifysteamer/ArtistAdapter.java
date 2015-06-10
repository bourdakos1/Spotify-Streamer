package com.xlythe.spotifysteamer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by Niko on 6/8/15.
 */
public class ArtistAdapter extends ArrayAdapter<Artist>{

    private Context mContext;
    private int mLayoutResourceId;
    private List<MyParcelable> mArtists;

    public ArtistAdapter(Context context, int layoutResourceId, List<MyParcelable> artists) {
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

        Artist artist = mArtists.get(position);

        holder.artist.setText(artist.name);
        if (artist.images.size()>0) {
            Picasso.with(mContext).load(artist.images.get(0).url).into(holder.image);
        }

        return view;
    }

    static class ViewHolder {
        @InjectView(R.id.image) ImageView image;
        @InjectView(R.id.text) TextView artist;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
