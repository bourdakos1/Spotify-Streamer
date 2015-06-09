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
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(mLayoutResourceId, parent, false);

        ImageView albumImage = (ImageView) rowView.findViewById(R.id.album_image);
        TextView albumName = (TextView) rowView.findViewById(R.id.album_name);
        TextView trackName = (TextView) rowView.findViewById(R.id.track_name);

        Track track = mTracks.get(position);
        rowView.setTag(track);

        albumName.setText(track.album.name);
        trackName.setText(track.name);
        if (track.album.images.size()>0) {
            Picasso.with(mContext).load(track.album.images.get(0).url).into(albumImage);
        }
        //track.preview_url; for part 2
        return rowView;
    }
}
