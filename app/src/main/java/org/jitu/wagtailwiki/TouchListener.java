package org.jitu.wagtailwiki;

import android.text.SpannedString;
import android.text.style.URLSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class TouchListener implements View.OnTouchListener {
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
