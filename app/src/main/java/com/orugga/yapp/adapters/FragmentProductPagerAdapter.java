package com.orugga.yapp.adapters;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

/**
 * Created by Alexis on 23/10/2017.
 */

public class FragmentProductPagerAdapter extends android.support.v4.app.FragmentPagerAdapter {

    Fragment[] mFragments;

    public FragmentProductPagerAdapter(FragmentManager fm, Fragment[] fragments) {
        super(fm);
        mFragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments[position];
    }

    @Override
    public int getCount() {
        return mFragments.length;
    }

    @Override
    public Parcelable saveState() {
        Bundle bundle = super.saveState() != null ? (Bundle) super.saveState() : new Bundle();
        bundle.putParcelableArray("states", null); // Never maintain any states from the base class, just null it out
        return bundle;
    }
}
