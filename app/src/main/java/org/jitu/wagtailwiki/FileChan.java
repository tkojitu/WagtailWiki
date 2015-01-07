package org.jitu.wagtailwiki;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.StringTokenizer;
import java.util.Vector;

public class FileChan {
    private ActivityMain activity;
    private Vector<File> history = new Vector<File>();

    public FileChan(ActivityMain activity) {
        this.activity = activity;
    }

    public boolean hasHistory() {
        return !history.isEmpty();
    }

    public File getLastPage() {
        if (history.isEmpty()) {
            return null;
        }
        return history.lastElement();
    }

    public void addPage(File file) {
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

    public void addPageToHistory(File file) {
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
}
