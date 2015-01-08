package org.jitu.wagtailwiki;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class SafChan extends StorageChan {
    private final static String[] PROJECTION = new String[] {
            DocumentsContract.Document.COLUMN_DISPLAY_NAME
    };

    public SafChan(ActivityMain activity) {
        super(activity);
    }

    @Override
    public InputStream getInputStream() {
        Uri uri = (Uri) getLastPage();
        if (uri == null) {
            return null;
        }
        if (StorageChan.EXTERNALSTORAGE.equals(uri.getAuthority())) {
            return getInputStreamFromExternalStorage(uri);
        }
        try {
            String path = uri.getPath();
            return new FileInputStream(path);
        } catch (IOException e) {
            return null;
        }
    }

    private InputStream getInputStreamFromExternalStorage(Uri uri) {
        ContentResolver cr = activity.getContentResolver();
        try {
            return cr.openInputStream(uri);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    @Override
    public String getPageName() {
        Uri uri = (Uri) getLastPage();
        if (uri == null) {
            return "";
        }
        if (StorageChan.EXTERNALSTORAGE.equals(uri.getAuthority())) {
            return getPageNameFromExternalStorage(uri);
        }
        File file = new File(uri.getPath());
        return file.getName();
    }

    private String getPageNameFromExternalStorage(Uri uri) {
        ContentResolver cr = activity.getContentResolver();
        Cursor cursor = cr.query(uri, PROJECTION, null, null, null);
        if (cursor == null) {
            Toast.makeText(activity, "query disabled: " + uri.toString(), Toast.LENGTH_LONG).show();
            return "";
        }
        if (!cursor.moveToFirst()) {
            Toast.makeText(activity, "Cursor#moveToFirst failed: " + uri.toString(), Toast.LENGTH_LONG).show();
            return "";
        }
        return cursor.getString(0);
    }

    @Override
    public void openPage(Uri uri) {
        Uri last = (Uri) getLastPage();
        Uri dir = getDirectory(last);
        Uri target = dir.buildUpon().appendPath(uri.getPath()).build();
        showPage(target);
    }

    private Uri getDirectory(Uri uri) {
        if (StorageChan.EXTERNALSTORAGE.equals(uri.getAuthority())) {
            File file = getFileFromExternalStorage(uri);
            return Uri.fromFile(file.getParentFile());
        }
        File file = new File(uri.getPath());
        File dir = file.getParentFile();
        return Uri.fromFile(dir);
    }

    private File getFileFromExternalStorage(Uri uri) {
        String str = uri.getPath();
        int index = str.indexOf(':');
        String path = str.substring(index + 1);
        return new File(Environment.getExternalStorageDirectory(), path);
    }

    @Override
    protected Object deserializePage(String str) {
        return Uri.parse(str);
    }

    @Override
    protected String serializePage(Object page) {
        return page.toString();
    }

    @Override
    protected Object toPage(Intent data) {
        return data.getData();
    }

    @Override
    protected Uri getPageUri() {
        Uri uri = (Uri) getLastPage();
        if (uri == null) {
            return null;
        }
        if (StorageChan.EXTERNALSTORAGE.equals(uri.getAuthority())) {
            return getPageUriFromExternalStorage(uri);
        }
        File file = new File(uri.getPath());
        if (!file.exists()) {
            if (!createEmptyFile(file)) {
                return null;
            }
        }
        return Uri.fromFile(file);
    }

    private Uri getPageUriFromExternalStorage(Uri uri) {
        File file = getFileFromExternalStorage(uri);
        return Uri.fromFile(file);
    }
}
