package org.jitu.wagtailwiki;

import android.content.Intent;
import android.net.Uri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class FileChan extends StorageChan {
    public FileChan(ActivityMain activity) {
        super(activity);
    }

    @Override
    protected Object deserializePage(String str) {
        return new File(str);
    }

    protected String serializePage(Object page) {
        return ((File) page).getAbsolutePath();
    }

    @Override
    public InputStream getInputStream() {
        File file = (File) getLastPage();
        if (file == null) {
            return null;
        }
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    @Override
    public String getPageName() {
        File file = (File) getLastPage();
        if (file == null) {
            return "";
        }
        return file.getName();
    }

    @Override
    public Object toPage(Intent data) {
        String path = data.getData().getPath();
        return new File(path);
    }

    @Override
    protected Uri getPageUri() {
        File file = (File) getLastPage();
        if (file == null) {
            return null;
        }
        if (!file.exists()) {
            if (!createEmptyFile(file)) {
                return null;
            }
        }
        return Uri.fromFile(file);
    }

    @Override
    public void openPage(Uri uri) {
        File file = (File) getLastPage();
        File dir = file.getParentFile();
        File target = new File(dir, uri.getPath());
        showPage(target);
    }
}
