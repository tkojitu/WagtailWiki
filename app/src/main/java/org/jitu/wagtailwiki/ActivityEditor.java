package org.jitu.wagtailwiki;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ActivityEditor extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        updateTitle();
        showText();
    }

    private void updateTitle() {
        File file = getFile();
        String title = (file == null) ? "" : file.getName();
        setTitle(title);
    }

    private File getFile() {
        Uri uri = getIntent().getData();
        if (uri == null) {
            return null;
        }
        return new File(uri.getPath());
    }

    private void showText() {
        try {
            File file = getFile();
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder buf = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                buf.append((char) c);
            }
            EditText edit = (EditText) findViewById(R.id.edit_text);
            edit.setText(buf.toString());
        } catch (FileNotFoundException e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void onBackPressed() {
        try {
            Uri uri = getIntent().getData();
            if (uri == null) {
                return;
            }
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new FileWriter(uri.getPath()));
                EditText edit = (EditText) findViewById(R.id.edit_text);
                writer.write(edit.getText().toString());
            } catch (FileNotFoundException e) {
                Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        } finally {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
