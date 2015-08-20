package com.xlythe.spotifysteamer;

import android.os.Parcel;
import android.os.Parcelable;

import kaaes.spotify.webapi.android.models.Artist;

/**
 * Created by Niko on 6/9/15.
 */
public class ArtistParcelable implements Parcelable {

    private String mArtistName;
    private String mArtistId;
    private String mArtistImage;

    public String getArtistName(){
        return mArtistName;
    }
    public String getArtistId(){
        return mArtistId;
    }
    public String getArtistImage(){
        return mArtistImage;
    }

    public ArtistParcelable(Artist artist){
        mArtistName = artist.name;
        mArtistId = artist.id;
        if(!artist.images.isEmpty()) {
            mArtistImage = artist.images.get(0).url;
        }
    }

    private ArtistParcelable(Parcel in) {
        mArtistName = in.readString();
        mArtistId = in.readString();
        mArtistImage = in.readString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mArtistName);
        out.writeString(mArtistId);
        out.writeString(mArtistImage);
    }

    public static final Parcelable.Creator<ArtistParcelable> CREATOR = new Parcelable.Creator<ArtistParcelable>() {
        public ArtistParcelable createFromParcel(Parcel in) {
            return new ArtistParcelable(in);
        }

        public ArtistParcelable[] newArray(int size) {
            return new ArtistParcelable[size];
        }
    };
}