package com.orugga.yapp.views;

/**
 * Created by Alexis on 18/10/2017.
 */

import android.annotation.TargetApi;
import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.text.Html;
import android.util.AttributeSet;

public class HtmlTextView extends AppCompatTextView {
    public HtmlTextView(Context context) {
        super(context);
    }

    public HtmlTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HtmlTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public HtmlTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setText(CharSequence s,BufferType b){
        super.setText(Html.fromHtml(s.toString()),b);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }
}
