package com.xlythe.spotifysteamer;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Niko on 6/9/15.
 */
public class MyParcelable implements Parcelable {
    private int mData;
    private String mColor;
    private String mNumber;

    public MyParcelable(int data, String number, String color) {
        mData = data;
        mColor = color;
        mNumber = number;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(mData);
        out.writeString(mColor);
        out.writeString(mNumber);
    }

    public static final Parcelable.Creator<MyParcelable> CREATOR = new Parcelable.Creator<MyParcelable>() {
        public MyParcelable createFromParcel(Parcel in) {
            return new MyParcelable(in);
        }

        public MyParcelable[] newArray(int size) {
            return new MyParcelable[size];
        }
    };

    private MyParcelable(Parcel in) {
        mData = in.readInt();
        mColor = in.readString();
        mNumber = in.readString();
    }
}