package org.jitu.wagtailwiki;

import android.content.Intent;
import android.net.Uri;

import java.io.InputStream;

public abstract class StorageChan {
    protected ActivityMain activity;

    public StorageChan(ActivityMain activity) {
        this.activity = activity;
    }

    public abstract void loadHistory(String str);
    public abstract boolean onOpen();
    public abstract void onActivityResult(int requestCode, int resultCode, Intent data);
    public abstract void startEditorActivity();
    public abstract InputStream getInputStream();
    public abstract String getPageName();
    public abstract boolean hasHistory();
    public abstract void openPage(Uri uri);
    public abstract void removePage();
}
