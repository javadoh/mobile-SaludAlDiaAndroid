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

import static com.orugga.yapp.Constants.ApiFields.PRODUCT_FULL_NAME;

/**
 * Created by Alexis on 26/10/2017.
 */

public class AutoCompleteBuscarProductosAdapter extends ArrayAdapter<JsonObject> {


    /**
     * Current results returned by this adapter.
     */
    private JsonArray mJsonResults;

    /**
     * Initializes with a resource for text rows and autocomplete query bounds.
     *
     * @see android.widget.ArrayAdapter#ArrayAdapter(android.content.Context, int)
     */

    private String mProductKeyword;
    public AutoCompleteBuscarProductosAdapter(Context context) {
        super(context, R.layout.search_product_item, R.id.productItemPrimaryText);
        mJsonResults = new JsonArray();
    }

    public void setResultsFromJson(JsonArray jsonArray, String producto) {
        mJsonResults = jsonArray;
        mProductKeyword = producto;
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
        String productName = getItem(position).get(PRODUCT_FULL_NAME).getAsString();

        TextView txtProductName = row.findViewById(R.id.productItemPrimaryText);

        int start = productName.toUpperCase().indexOf(mProductKeyword.trim().toUpperCase());
        if (start < 0) start = 0;
        int end = start + mProductKeyword.length() >= productName.length() ? productName.length() - 1 : start + mProductKeyword.length();
        SpannableStringBuilder str = new SpannableStringBuilder(productName);
        str.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        txtProductName.setText(str);

        return row;

    }


}
