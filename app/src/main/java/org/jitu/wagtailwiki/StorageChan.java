package org.jitu.wagtailwiki;

import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import java.io.InputStream;

public abstract class StorageChan {
    public static final String EXTERNALSTORAGE = "com.android.externalstorage.documents";

    protected ActivityMain activity;

    public static StorageChan getInstance(ActivityMain activity, Uri uri) {
        if (!EXTERNALSTORAGE.equals(uri.getAuthority())) {
            return new FileChan(activity);
        }
        Toast.makeText(activity, "unknown authority: " + uri.getAuthority(), Toast.LENGTH_LONG).show();
        return null;
    }

    public StorageChan(ActivityMain activity) {
        this.activity = activity;
    }

    public abstract void loadHistory(String str);
    public abstract void onActionGetContent(Intent data);
    public abstract void startEditorActivity();
    public abstract InputStream getInputStream();
    public abstract String getPageName();
    public abstract boolean hasHistory();
    public abstract void openPage(Uri uri);
    public abstract void removePage();
}
