package org.jitu.wagtailwiki;

import java.io.File;
import java.util.Vector;

public class FileChan {
    private ActivityMain activity;
    private Vector<File> history = new Vector<File>();

    public FileChan(ActivityMain activity) {
        this.activity = activity;
    }

    public Vector<File> getHistory() {
        return history;
    }

    public void setHistory(Vector<File> arg) {
        history = arg;
    }

    public boolean hasHistory() {
        return !history.isEmpty();
    }

    public File getLastItem() {
        if (history.isEmpty()) {
            return null;
        }
        return history.lastElement();
    }

    public void addItem(File file) {
        history.add(file);
    }

    public void removeItem() {
        history.remove(history.size() - 1);
    }
}
