package com.orugga.yapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.orugga.yapp.fragments.colabora.ColaboraFragment;
import com.orugga.yapp.interfaces.GoogleApiClientActivity;
import com.orugga.yapp.interfaces.OnFragmentUpdateToolbarIntefrace;

import static com.orugga.yapp.Constants.FragmentsNames.FRAGMENT_HOME;

public class ColaboraActivity extends AppCompatActivity implements GoogleApiClientActivity, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this, this)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(LocationServices.API)
                    .build();
        }

        setContentView(R.layout.activity_colabora);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.coordinatorLayout);
                if (fragment instanceof OnFragmentUpdateToolbarIntefrace) {
                    ((OnFragmentUpdateToolbarIntefrace) fragment).onFragmentUpdateToolbar();
                }
            }
        });

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.coordinatorLayout, ColaboraFragment.newInstance(), FRAGMENT_HOME).commit();
        setTitle(R.string.action_bar_title_colabora);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection Failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }
}
