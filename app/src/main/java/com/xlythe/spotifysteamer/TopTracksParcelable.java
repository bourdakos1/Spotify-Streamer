package com.xlythe.spotifysteamer;

import android.os.Parcel;
import android.os.Parcelable;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by Niko on 6/9/15.
 */
public class TopTracksParcelable implements Parcelable {
    private String mArtistName;
    private String mTrackName;
    private String mAlbumName;
    private String mAlbumImage;
    private String mPreviewUrl;

    public String getArtistName(){
        return mArtistName;
    }
    public String getTrackName(){
        return mTrackName;
    }
    public String getAlbumName(){
        return mAlbumName;
    }
    public String getAlbumImage(){
        return mAlbumImage;
    }
    public String getPreviewUrl(){
        return mPreviewUrl;
    }

    public TopTracksParcelable(Track track){
        for (int i = 0; i < track.artists.size(); i++) {
            if (i == track.artists.size()-1)
                mArtistName = track.artists.get(i).name;
            else
                mArtistName = track.artists.get(i).name + ", ";
        }
        mTrackName = track.name;
        mAlbumName = track.album.name;
        if(!track.album.images.isEmpty()) {
            mAlbumImage = track.album.images.get(0).url;
        }
        mPreviewUrl = track.preview_url;
    }

    private TopTracksParcelable(Parcel in) {
        mArtistName = in.readString();
        mTrackName = in.readString();
        mAlbumName = in.readString();
        mAlbumImage = in.readString();
        mPreviewUrl = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mArtistName);
        out.writeString(mTrackName);
        out.writeString(mAlbumName);
        out.writeString(mAlbumImage);
        out.writeString(mPreviewUrl);
    }

    public static final Parcelable.Creator<TopTracksParcelable> CREATOR = new Parcelable.Creator<TopTracksParcelable>() {
        public TopTracksParcelable createFromParcel(Parcel in) {
            return new TopTracksParcelable(in);
        }

        public TopTracksParcelable[] newArray(int size) {
            return new TopTracksParcelable[size];
        }
    };
}