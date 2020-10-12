package indi.ssuf1998.oscan.adapter;


import android.net.Uri;

public class Doc {
    private String title = "";
    private Uri thumbUri = Uri.EMPTY;

    public Doc(String title, Uri thumbUri) {
        this.title = title;
        this.thumbUri = thumbUri;
    }

    public Doc() {
    }

    public Doc(String title) {
        this(title, Uri.EMPTY);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Uri getThumbUri() {
        return thumbUri;
    }

    public void setThumbUri(Uri thumbUri) {
        this.thumbUri = thumbUri;
    }
}
