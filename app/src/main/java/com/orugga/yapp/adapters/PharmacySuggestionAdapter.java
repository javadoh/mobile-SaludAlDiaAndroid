package com.orugga.yapp.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.orugga.yapp.R;

/**
 * Created by Alexis on 19/10/2017.
 */

public class PharmacySuggestionAdapter extends android.support.v4.widget.CursorAdapter {

    public PharmacySuggestionAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.place_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView textView1 = (TextView) view.findViewById(R.id.placeItemPrimaryText);
        TextView textView2 = (TextView) view.findViewById(R.id.placeItemSecondaryText);
        textView1.setText(cursor.getString(1));
        textView2.setText(cursor.getString(2));
    }


}
