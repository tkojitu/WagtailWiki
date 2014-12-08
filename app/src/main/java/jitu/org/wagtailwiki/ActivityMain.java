package jitu.org.wagtailwiki;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannedString;
import android.text.style.URLSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.rjeschke.txtmark.Processor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ActivityMain extends Activity {
    private static final int REQUEST_ACTION_GET_CONTENT = 11;

    private File currentFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getTextView().setOnTouchListener(new TouchListener(this));
        showWelcome();
    }

    TextView getTextView() {
        return (TextView) findViewById(R.id.text_view);
    }

    private void showWelcome() {
        InputStream is = getResources().openRawResource(R.raw.welcome);
        showContent(is);
    }

    private void showContent(InputStream stream) {
        String html;
        try {
            html = Processor.process(stream);
        } catch (IOException e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            return;
        }
        CharSequence seq = Html.fromHtml(html);
        getTextView().setText(seq);
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
            showContent(is);
            currentFile = file;
        } catch (IOException e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }

    void openLink(String link) {
        if (currentFile == null) {
            return;
        }
        File dir = currentFile.getParentFile();
        File target = new File(dir, link);
        showFile(target);
    }

    public void onEditButton(View view) {
    }
}

class TouchListener implements View.OnTouchListener {
    private ActivityMain activity;

    TouchListener(ActivityMain activity) {
        this.activity = activity;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        TextView v = (TextView) view;
        int pos = v.getOffsetForPosition(event.getX(), event.getY());
        SpannedString str = (SpannedString) v.getText();
        URLSpan[] spans = str.getSpans(0, str.length(), URLSpan.class);
        for (URLSpan span : spans) {
            int st = str.getSpanStart(span);
            int ed = str.getSpanEnd(span);
            if (st <= pos && pos <= ed) {
                activity.openLink(span.getURL());
            }
        }
        return false;
    }
}
