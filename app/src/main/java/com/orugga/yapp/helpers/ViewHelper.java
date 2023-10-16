package com.orugga.yapp.helpers;

import android.widget.Spinner;

/**
 * Created by Alexis on 25/12/2017.
 */

public class ViewHelper {
    public static int getSelectionPosition(Spinner spinner, String selection) {
        int position = 0;
        while (position < spinner.getCount() && !spinner.getItemAtPosition(position).equals(selection)) {
            position++;
        }
        return position < spinner.getCount() ? position : 0;
    }
}
