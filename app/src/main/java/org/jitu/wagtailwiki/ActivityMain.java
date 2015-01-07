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

import java.io.IOException;
import java.io.InputStream;

public class ActivityMain extends Activity {
    private static final int REQUEST_ACTION_GET_CONTENT = 11;
    private static final String PREF_HISTORY = "WagtailWikiHistory";

    private StorageChan storage = new FileChan(this);

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
        storage.loadHistory(str);
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

    public boolean showContent(InputStream stream) {
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

    public boolean onOpen() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
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
            if (resultCode == Activity.RESULT_OK) {
                storage.onActionGetContent(data);
            }
            break;
        default:
            break;
        }
    }

    public void saveHistoryString(String str) {
        SharedPreferences settings = getSharedPreferences(PREF_HISTORY, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PREF_HISTORY, str);
        editor.apply();
    }

    private void updateScreen() {
        InputStream is = storage.getInputStream();
        if (is == null) {
            showWelcome();
        } else {
            showContent(is);
        }
        updateTitle();
        updateEditButton();
    }

    public void updateTitle() {
        String title = storage.getPageName();
        if (title.isEmpty()) {
            title = getString(R.string.app_name);
        }
        setTitle(title);
    }

    private void updateEditButton() {
        Button button = (Button) findViewById(R.id.button_edit);
        button.setEnabled(storage.hasHistory());
    }

    void openLink(String link) {
        if (!storage.hasHistory()) {
            return;
        }
        Uri uri = Uri.parse(link.trim());
        if (uri.getScheme() == null) {
            storage.openPage(uri);
        } else {
            openWeb(uri);
        }
    }

    private void openWeb(Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public void onEditButton(View view) {
        storage.startEditorActivity();
    }

    public void onBackPressed() {
        if (!storage.hasHistory()) {
            super.onBackPressed();
            return;
        }
        storage.removePage();
        updateScreen();
    }
}
