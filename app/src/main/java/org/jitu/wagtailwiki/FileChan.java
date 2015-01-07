package org.jitu.wagtailwiki;

import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;
import java.util.Vector;

public class FileChan extends StorageChan {
    private Vector<File> history = new Vector<File>();

    public FileChan(ActivityMain activity) {
        super(activity);
    }

    public boolean hasHistory() {
        return !history.isEmpty();
    }

    private File getLastPage() {
        if (history.isEmpty()) {
            return null;
        }
        return history.lastElement();
    }

    private void addPage(File file) {
        history.add(file);
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

    private Vector<File> stringToHistory(String str) {
        Vector<File> hist = new Vector<>();
        StringTokenizer tokens = new StringTokenizer(str, "\n");
        while (tokens.hasMoreTokens()) {
            String path = tokens.nextToken().trim();
            if (path.isEmpty()) {
                continue;
            }
            File file = new File(path);
            hist.add(file);
        }
        return hist;
    }

    private void addPageToHistory(File file) {
        File last = getLastPage();
        if (last == null || !last.equals(file)) {
            addPage(file);
            String str = getHistoryString();
            activity.saveHistoryString(str);
        }
    }

    private String getHistoryString() {
        StringBuilder buf = new StringBuilder();
        for (File file : history) {
            buf.append(file.getAbsolutePath());
            buf.append("\n");
        }
        return buf.toString();
    }

    public InputStream getInputStream() {
        File file = getLastPage();
        if (file == null) {
            return null;
        }
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    public String getPageName() {
        File file = getLastPage();
        if (file == null) {
            return "";
        }
        return file.getName();
    }

    public void onActionGetContent(Intent data) {
        String path = data.getData().getPath();
        showFile(new File(path));
    }

    private void showFile(File file) {
        addPageToHistory(file);
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
        File file = getLastPage();
        if (file == null) {
            return;
        }
        if (!file.exists()) {
            if (!createEmptyFile(file)) {
                return;
            }
        }
        Intent intent = new Intent(Intent.ACTION_EDIT);
        Uri uri = Uri.fromFile(file);
        intent.setDataAndType(uri, "text/plain");
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
            return;
        }
        intent = new Intent(activity, ActivityEditor.class);
        intent.setDataAndType(uri, "text/plain");
        activity.startActivity(intent);
    }

    private boolean createEmptyFile(File file) {
        try {
            FileOutputStream output = new FileOutputStream(file);
            output.close();
            return true;
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            Toast.makeText(activity, "fail to close file", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    public void openPage(Uri uri) {
        File file = getLastPage();
        File dir = file.getParentFile();
        File target = new File(dir, uri.getPath());
        showFile(target);
    }
}
