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
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by Niko on 6/9/15.
 */
public class TrackAdapter extends ArrayAdapter<Track> {

    private Context mContext;
    private int mLayoutResourceId;
    private List<Track> mTracks;

    public TrackAdapter(Context context, int layoutResourceId, List<Track> tracks) {
        super(context, layoutResourceId, tracks);
        mLayoutResourceId = layoutResourceId;
        mContext = context;
        mTracks = tracks;
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

        Track track = mTracks.get(position);

        holder.albumName.setText(track.album.name);
        holder.trackName.setText(track.name);
        if (track.album.images.size()>0) {
            Picasso.with(mContext).load(track.album.images.get(0).url).into(holder.albumImage);
        }

        return view;
    }

    static class ViewHolder {
        @InjectView(R.id.album_image) ImageView albumImage;
        @InjectView(R.id.album_name) TextView albumName;
        @InjectView(R.id.track_name) TextView trackName;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
