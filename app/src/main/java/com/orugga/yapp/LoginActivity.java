package com.orugga.yapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.orugga.yapp.fragments.TermsAndConditionsFragment;
import com.orugga.yapp.fragments.login.LogInOptionsFragment;
import com.orugga.yapp.helpers.SessionHelper;
import com.orugga.yapp.interfaces.ActivityUpdateBackToolbar;
import com.orugga.yapp.interfaces.OnFragmentUpdateToolbarIntefrace;

public class LoginActivity extends AppCompatActivity implements ActivityUpdateBackToolbar {

    Toolbar mActionBarToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();

        boolean needBackButton = getIntent().getBooleanExtra("needBackButton", false);

        if (!SessionHelper.isTermsAccepted(this)) {
            transaction.replace(R.id.coordinatorLayout, TermsAndConditionsFragment.newInstance(TermsAndConditionsFragment.IN_TOUR));
            setTitle(getString(R.string.action_bar_title_tyc));
        } else {
            transaction.replace(R.id.coordinatorLayout, LogInOptionsFragment.newInstance());
            setTitle(getString(R.string.action_bar_title_ingresar));
        }
        transaction.commit();

        mActionBarToolbar = findViewById(R.id.action_bar_toolbar);
        setSupportActionBar(mActionBarToolbar);
        if (needBackButton)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mActionBarToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                if (v != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        });


        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                Fragment fragment = getFragment();
                if (fragment instanceof OnFragmentUpdateToolbarIntefrace) {
                    ((OnFragmentUpdateToolbarIntefrace) fragment).onFragmentUpdateToolbar();
                }
            }
        });
    }

    private Fragment getFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.coordinatorLayout);
    }

    @Override
    public void showBackNavigationIcon() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } else {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }
}
