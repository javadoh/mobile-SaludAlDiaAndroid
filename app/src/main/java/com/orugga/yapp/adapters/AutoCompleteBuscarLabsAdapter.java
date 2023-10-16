package com.orugga.yapp.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.orugga.yapp.R;

/**
 * Created by Alexis on 26/10/2017.
 */

public class AutoCompleteBuscarLabsAdapter extends ArrayAdapter<JsonObject> {


    /**
     * Current results returned by this adapter.
     */
    private JsonArray mJsonResults;

    /**
     * Initializes with a resource for text rows and autocomplete query bounds.
     *
     * @see ArrayAdapter#ArrayAdapter(Context, int)
     */

    private String mLabKeyword;
    public AutoCompleteBuscarLabsAdapter(Context context) {
        super(context, R.layout.search_labs_item, R.id.labItemPrimaryText);

    }

    public void setResultsFromJson(JsonArray jsonArray, String lab) {
        mJsonResults = jsonArray;
        mLabKeyword = lab;
        this.notifyDataSetChanged();
    }

    /**
     * Returns the number of results received in the last autocomplete query.
     */
    @Override
    public int getCount() {
        return mJsonResults.size();
    }

    /**
     * Returns an item from the last autocomplete query.
     */
    @Override
    public JsonObject getItem(int position) {
        return mJsonResults.get(position).getAsJsonObject();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View row = super.getView(position, convertView, parent);
        String labName = getItem(position).get("name").getAsString();

        TextView txtLabName = row.findViewById(R.id.labItemPrimaryText);

        int start = labName.toUpperCase().indexOf(mLabKeyword.toUpperCase());
        if (start < 0) start = 0;
        int end = start + mLabKeyword.length() >= labName.length() ? labName.length() - 1 : start + mLabKeyword.length();
        SpannableStringBuilder str = new SpannableStringBuilder(labName);
        str.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        txtLabName.setText(str);

        return row;

    }


}
