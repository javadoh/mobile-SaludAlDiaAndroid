package com.orugga.yapp.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alexis on 26/10/2017.
 */

public class AutoCompleteConvenioAdapter extends ArrayAdapter<String> {


    /**
     * Initializes with a resource for text rows and autocomplete query bounds.
     *
     * @see android.widget.ArrayAdapter#ArrayAdapter(android.content.Context, int)
     */
    private String mDealKeyword = "";
    /**
     * Current results returned by this adapter.
     */
    private List<String> convenios, convenioSuggestion;
    private Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if (constraint != null) {
                ArrayList<String> tempItems = new ArrayList<>();
                for (String deal : convenios) {
                    if (deal.toLowerCase().contains(constraint.toString().toLowerCase())) {
                        tempItems.add(deal);
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = tempItems;
                filterResults.count = tempItems.size();
                return filterResults;
            } else {
                return new FilterResults();
            }
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            List<String> filterList = (ArrayList<String>) results.values;
            if (results != null && results.count > 0) {
                if (constraint != null)
                    mDealKeyword = constraint.toString();
                convenioSuggestion = filterList;
                notifyDataSetChanged();
            }
        }
    };

    public AutoCompleteConvenioAdapter(Context context, List<String> convenios) {
        super(context, android.R.layout.simple_spinner_dropdown_item, 0);

        this.convenios = new ArrayList<>(convenios); // this makes the difference.
        this.convenioSuggestion = new ArrayList<>();
    }

    /**
     * Returns the number of results received in the last autocomplete query.
     */
    @Override
    public int getCount() {
        return convenioSuggestion.size();
    }

    /**
     * Returns an item from the last autocomplete query.
     */
    @Override
    public String getItem(int position) {
        return convenioSuggestion.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        TextView txtDealName = (TextView) super.getView(position, convertView, parent);
        String dealName = getItem(position);

        int start = dealName.toUpperCase().indexOf(mDealKeyword.trim().toUpperCase());
        if (start < 0) start = 0;
        int end = start + mDealKeyword.length();
        SpannableStringBuilder str = new SpannableStringBuilder(dealName);
        str.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        txtDealName.setText(str);

        return txtDealName;

    }

    @NonNull
    @Override
    public Filter getFilter() {
        return filter;
    }
}

