package org.jitu.wagtailwiki;

import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import java.io.InputStream;
import java.util.StringTokenizer;
import java.util.Vector;

public abstract class StorageChan {
    public static final String EXTERNALSTORAGE = "com.android.externalstorage.documents";

    protected ActivityMain activity;
    protected Vector history = new Vector();

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

    public abstract InputStream getInputStream();
    public abstract String getPageName();
    public abstract void openPage(Uri uri);
    protected abstract Object deserializePage(String str);
    protected abstract String serializePage(Object page);
    protected abstract Object toPage(Intent data);
    protected abstract Uri getPageUri();

    public boolean hasHistory() {
        return !history.isEmpty();
    }

    protected Object getLastPage() {
        if (history.isEmpty()) {
            return null;
        }
        return history.lastElement();
    }

    protected void addPage(Object page) {
        history.add(page);
    }

    public void removePage() {
        history.remove(history.size() - 1);
    }

    public void loadHistory(String str) {
        if (str == null || str.isEmpty()) {
            return;
        }
        history = stringToHistory(str);
    }

    protected void addPageToHistory(Object page) {
        Object last = getLastPage();
        if (last == null || !last.equals(page)) {
            addPage(page);
            String str = getHistoryString();
            activity.saveHistoryString(str);
        }
    }

    protected Vector stringToHistory(String str) {
        Vector hist = new Vector();
        StringTokenizer tokens = new StringTokenizer(str, "\n");
        while (tokens.hasMoreTokens()) {
            String line = tokens.nextToken().trim();
            if (line.isEmpty()) {
                continue;
            }
            Object page = deserializePage(line);
            hist.add(page);
        }
        return hist;
    }

    protected String getHistoryString() {
        StringBuilder buf = new StringBuilder();
        for (Object page : history) {
            String str = serializePage(page);
            buf.append(str);
            buf.append("\n");
        }
        return buf.toString();
    }

    public void onActionGetContent(Intent data) {
        Object page = toPage(data);
        showPage(page);
    }

    protected void showPage(Object page) {
        addPageToHistory(page);
        InputStream is = getInputStream();
        if (is == null) {
            startEditorActivity();
            return;
        }
        if (!activity.showContent(is)) {
            return;
        }
        activity.updateTitle();
    }

    public void startEditorActivity() {
        Uri uri = getPageUri();
        Intent intent = new Intent(Intent.ACTION_EDIT);
        intent.setDataAndType(uri, "text/plain");
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
            return;
        }
        intent = new Intent(activity, ActivityEditor.class);
        intent.setDataAndType(uri, "text/plain");
        activity.startActivity(intent);
    }
}
