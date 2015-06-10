package com.xlythe.spotifysteamer;

import android.os.Parcel;
import android.os.Parcelable;

import kaaes.spotify.webapi.android.models.Artist;

/**
 * Created by Niko on 6/9/15.
 */
public class ArtistParcelable extends Artist implements Parcelable {

    public ArtistParcelable(Artist artist){
        this.name = artist.name;
        this.id = artist.id;
        this.images = artist.images;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.name);
        out.writeString(this.id);
        if(!this.images.isEmpty()) {
            out.writeString(this.images.get(0).url);
        }
    }

    public static final Parcelable.Creator<ArtistParcelable> CREATOR = new Parcelable.Creator<ArtistParcelable>() {
        public ArtistParcelable createFromParcel(Parcel in) {
            return new ArtistParcelable(in);
        }

        public ArtistParcelable[] newArray(int size) {
            return new ArtistParcelable[size];
        }
    };

    private ArtistParcelable(Parcel in) {
        this.name = in.readString();
        this.id = in.readString();
        if(!this.images.isEmpty()) {
            this.images.get(0).url = in.readString();
        }
    }
}