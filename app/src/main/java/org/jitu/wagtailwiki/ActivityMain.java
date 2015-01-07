package org.jitu.wagtailwiki;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.rjeschke.txtmark.Processor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;
import java.util.Vector;

public class ActivityMain extends Activity {
    private static final int REQUEST_ACTION_GET_CONTENT = 11;
    private static final String PREF_HISTORY = "WagtailWikiHistory";

    private Vector<File> history = new Vector<File>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getTextView().setOnTouchListener(new TouchListener(this));
        loadHistory();
    }

    private void loadHistory() {
        SharedPreferences settings = getSharedPreferences(PREF_HISTORY, MODE_PRIVATE);
        String str = settings.getString(PREF_HISTORY, "");
        if (str.isEmpty()) {
            return;
        }
        history = stringToHistory(str);
    }

    private Vector<File> stringToHistory(String str) {
        Vector<File> hist = new Vector<File>();
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

    protected void onResume() {
        super.onResume();
        updateScreen();
    }

    TextView getTextView() {
        return (TextView) findViewById(R.id.text_view);
    }

    private void showWelcome() {
        InputStream is = getResources().openRawResource(R.raw.welcome);
        showContent(is);
    }

    private boolean showContent(InputStream stream) {
        String html;
        try {
            html = Processor.process(stream);
        } catch (IOException e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            return false;
        }
        CharSequence seq = Html.fromHtml(html);
        getTextView().setText(seq);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
        case R.id.menu_open:
            return onOpen();
        case R.id.action_settings:
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private boolean onOpen() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("file/*");
        try {
            startActivityForResult(intent, REQUEST_ACTION_GET_CONTENT);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case REQUEST_ACTION_GET_CONTENT:
            if (resultCode == RESULT_OK) {
                String path = data.getData().getPath();
                showFile(new File(path));
            }
            break;
        default:
            break;
        }
    }

    private void showFile(File file) {
        try {
            InputStream is = new FileInputStream(file);
            if (!showContent(is)) {
                return;
            }
            addFileToHistory(file);
            updateTitle();
        } catch (FileNotFoundException e) {
            addFileToHistory(file);
            startEditorActivity();
        }
    }

    private void startEditorActivity() {
        if (history.isEmpty()) {
            return;
        }
        if (!history.lastElement().exists()) {
            if (!createEmptyFile(history.lastElement())) {
                return;
            }
        }
        Intent intent = new Intent(Intent.ACTION_EDIT);
        Uri uri = Uri.fromFile(history.lastElement());
        intent.setDataAndType(uri, "text/plain");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
            return;
        }
        intent = new Intent(this, ActivityEditor.class);
        intent.setDataAndType(uri, "text/plain");
        startActivity(intent);
    }

    private boolean createEmptyFile(File file) {
        try {
            FileOutputStream output = new FileOutputStream(file);
            output.close();
            return true;
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            Toast.makeText(this, "fail to close file", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private void addFileToHistory(File file) {
        if (history.isEmpty() || !history.lastElement().equals(file)) {
            history.add(file);
            saveHistory(history);
        }
    }

    private void saveHistory(Vector<File> history) {
        String str = historyToString(history);
        if (str.isEmpty()) {
            return;
        }
        SharedPreferences settings = getSharedPreferences(PREF_HISTORY, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PREF_HISTORY, str);
        editor.apply();
    }

    private String historyToString(Vector<File> hist) {
        StringBuilder buf = new StringBuilder();
        for (File file : hist) {
            buf.append(file.getAbsolutePath());
            buf.append("\n");
        }
        return buf.toString();
    }

    private void updateScreen() {
        if (history.isEmpty()) {
            showWelcome();
        } else {
            showFile(history.lastElement());
        }
        updateTitle();
        updateEditButton();
    }

    private void updateTitle() {
        if (history.isEmpty()) {
            setTitle(getString(R.string.app_name));
        } else {
            String name = history.lastElement().getName();
            setTitle(name);
        }
    }

    private void updateEditButton() {
        Button button = (Button) findViewById(R.id.button_edit);
        button.setEnabled(!history.isEmpty());
    }

    void openLink(String link) {
        if (history.isEmpty()) {
            return;
        }
        Uri uri = Uri.parse(link.trim());
        if (uri.getScheme() == null) {
            openFile(uri);
        } else {
            openWeb(uri);
        }
    }

    private void openFile(Uri uri) {
        File dir = history.lastElement().getParentFile();
        File target = new File(dir, uri.getPath());
        showFile(target);
    }

    private void openWeb(Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public void onEditButton(View view) {
        startEditorActivity();
    }

    public void onBackPressed() {
        if (history.isEmpty()) {
            super.onBackPressed();
            return;
        }
        history.remove(history.size() - 1);
        updateScreen();
    }
}
