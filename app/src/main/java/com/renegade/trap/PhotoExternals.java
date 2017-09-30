package com.renegade.trap;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;

/**
 * Created by brent on 9/28/17.
 */

public class PhotoExternals implements Parcelable {
    Uri photoUri;
    File photoFile;

    public PhotoExternals() {
    }

    public PhotoExternals(Parcel in) {
    }

    public Uri getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(Uri photoUri) {
        this.photoUri = photoUri;
    }

    public File getPhotoFile() {
        return photoFile;
    }

    public void setPhotoFile(File photoFile) {
        this.photoFile = photoFile;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public boolean hasPhoto() {
        return photoUri != null || photoFile != null;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
    public static final Parcelable.Creator<PhotoExternals> CREATOR
            = new Parcelable.Creator<PhotoExternals>() {
        public PhotoExternals createFromParcel(Parcel in) {
            return new PhotoExternals(in);
        }

        public PhotoExternals[] newArray(int size) {
            return new PhotoExternals[size];
        }
    };    
}
