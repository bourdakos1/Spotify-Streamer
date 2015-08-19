package com.xlythe.spotifysteamer;

import android.os.Parcel;
import android.os.Parcelable;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by Niko on 6/9/15.
 */
public class TopTracksParcelable extends Track implements Parcelable {

    public TopTracksParcelable(Track track){
        this.name = track.name;
        this.album = track.album;
        this.preview_url = track.preview_url;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.name);
        out.writeString(this.album.name);
        if(!this.album.images.isEmpty()) {
            out.writeString(this.album.images.get(0).url);
        }
        out.writeString(preview_url);
    }

    public static final Parcelable.Creator<TopTracksParcelable> CREATOR = new Parcelable.Creator<TopTracksParcelable>() {
        public TopTracksParcelable createFromParcel(Parcel in) {
            return new TopTracksParcelable(in);
        }

        public TopTracksParcelable[] newArray(int size) {
            return new TopTracksParcelable[size];
        }
    };

    private TopTracksParcelable(Parcel in) {
        this.name = in.readString();
        this.album.name = in.readString();
        if(!this.album.images.isEmpty()) {
            this.album.images.get(0).url = in.readString();
        }
        this.preview_url = in.readString();
    }
}