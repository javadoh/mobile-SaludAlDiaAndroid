package com.orugga.yapp;


import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.crashlytics.android.answers.CustomEvent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.makeramen.roundedimageview.RoundedImageView;
import com.orugga.yapp.database.RecordatorioToma;
import com.orugga.yapp.database.RecordatorioVisita;
import com.orugga.yapp.fragments.AboutUsFragment;
import com.orugga.yapp.fragments.BuscarProductosFragment;
import com.orugga.yapp.fragments.BuscarProgramaPacienteFragment;
import com.orugga.yapp.fragments.HomeFragment;
import com.orugga.yapp.fragments.TermsAndConditionsFragment;
import com.orugga.yapp.fragments.favoritos.MisFavoritosFragment;
import com.orugga.yapp.fragments.login.RegisterAccountFragment;
import com.orugga.yapp.fragments.recordatorios.MisRecordatoriosTomaFragment;
import com.orugga.yapp.fragments.recordatorios.MisRecordatoriosVisitaFragment;
import com.orugga.yapp.helpers.IdleHelper;
import com.orugga.yapp.helpers.SessionHelper;
import com.orugga.yapp.interfaces.ActivityUpdateBackToolbar;
import com.orugga.yapp.interfaces.GoogleApiClientActivity;
import com.orugga.yapp.interfaces.JsonArrayResponse;
import com.orugga.yapp.interfaces.OnFragmentUpdateToolbarIntefrace;
import com.orugga.yapp.requests.DownloadImageRequest;
import com.orugga.yapp.views.MovableFloatingActionButton;

import static com.orugga.yapp.Constants.ANSWERS_CONSTANTS.CONTENT_TYPE_VIEW;
import static com.orugga.yapp.Constants.ANSWERS_CONSTANTS.VIEW_ABOUT_US_ID;
import static com.orugga.yapp.Constants.ANSWERS_CONSTANTS.VIEW_BUSCAR_PROGRAMA_PACIENTE_ID;
import static com.orugga.yapp.Constants.ANSWERS_CONSTANTS.VIEW_MENU_ID;
import static com.orugga.yapp.Constants.ANSWERS_CONSTANTS.VIEW_MY_FAVOURITES_ID;
import static com.orugga.yapp.Constants.ANSWERS_CONSTANTS.VIEW_MY_PROFILE_ID;
import static com.orugga.yapp.Constants.ANSWERS_CONSTANTS.VIEW_MY_TAKES_REMINDERS_ID;
import static com.orugga.yapp.Constants.ANSWERS_CONSTANTS.VIEW_MY_VISIT_REMINDERS_ID;
import static com.orugga.yapp.Constants.FragmentsNames.FRAGMENT_ABOUT_US;
import static com.orugga.yapp.Constants.FragmentsNames.FRAGMENT_BUSCAR_PRODUCTO;
import static com.orugga.yapp.Constants.FragmentsNames.FRAGMENT_BUSCAR_PROGRAMA_PACIENTE;
import static com.orugga.yapp.Constants.FragmentsNames.FRAGMENT_FAVORITOS;
import static com.orugga.yapp.Constants.FragmentsNames.FRAGMENT_HOME;
import static com.orugga.yapp.Constants.FragmentsNames.FRAGMENT_MIS_RECORDATORIOS_TOMA;
import static com.orugga.yapp.Constants.FragmentsNames.FRAGMENT_MIS_RECORDATORIOS_VISITA;
import static com.orugga.yapp.Constants.FragmentsNames.FRAGMENT_REGISTER_ACCOUNT;
import static com.orugga.yapp.Constants.FragmentsNames.FRAGMENT_TYC;
import static com.orugga.yapp.Constants.IntentFilters.ACTION_COMPLETE_INFO;
import static com.orugga.yapp.Constants.IntentFilters.ACTION_UPDATE;
import static com.orugga.yapp.Constants.IntentFilters.FRAGMENT_MANAGEMENT;
import static com.orugga.yapp.Constants.IntentFilters.OPERATION_OPEN_FRAGMENT;
import static com.orugga.yapp.Constants.IntentFilters.OPERATION_OPEN_FRAGMENT_BUSQUEDA_PRODUCTO;
import static com.orugga.yapp.Constants.IntentFilters.OPERATION_OPEN_FRAGMENT_BUSQUEDA_PROGRAMA_PACIENTE;
import static com.orugga.yapp.helpers.SessionHelper.getAccessToken;
import static com.orugga.yapp.helpers.SessionHelper.isUserLogedIn;
import static com.orugga.yapp.requests.FavouritesRequests.getMyFavouritesPharmacies;
import static com.orugga.yapp.requests.FavouritesRequests.getMyFavouritesProducts;

public class MainHomeActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,
        NavigationView.OnNavigationItemSelectedListener,
        ActivityUpdateBackToolbar,
        GoogleApiClientActivity {

    private GoogleApiClient mGoogleApiClient;

    private NavigationView mNavigationView;
    private ActionBarDrawerToggle mToggle;
    private DrawerLayout mDrawer;

    private MovableFloatingActionButton colaboraFab;

    private RoundedImageView mProfileImage;
    private BroadcastReceiver mIntentManager = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            switch (intent.getStringExtra(OPERATION_OPEN_FRAGMENT)) {
                case OPERATION_OPEN_FRAGMENT_BUSQUEDA_PRODUCTO:
                    transaction.add(R.id.coordinatorLayout, BuscarProductosFragment.newInstance())
                            .addToBackStack(FRAGMENT_BUSCAR_PRODUCTO).commitAllowingStateLoss();
                    break;
                case OPERATION_OPEN_FRAGMENT_BUSQUEDA_PROGRAMA_PACIENTE:
                    transaction.add(R.id.coordinatorLayout, BuscarProgramaPacienteFragment.newInstance())
                            .addToBackStack(FRAGMENT_BUSCAR_PROGRAMA_PACIENTE).commitAllowingStateLoss();
                    break;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_home);
        AppRater.app_launched(this);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this, this)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(LocationServices.API)
                    .build();
        }

        colaboraFab = findViewById(R.id.colaboraFab);
        colaboraFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IdleHelper.disableForAWhile(MainHomeActivity.this, v);
                openColabora();
            }
        });

        Toolbar actionBarToolbar = findViewById(R.id.action_bar_toolbar);
        setSupportActionBar(actionBarToolbar);

        mNavigationView = findViewById(R.id.nav_view);
        mProfileImage = mNavigationView.getHeaderView(0).findViewById(R.id.imgAvatar);
        TextView nameView = mNavigationView.getHeaderView(0).findViewById(R.id.txtNombre);
        TextView emailView = mNavigationView.getHeaderView(0).findViewById(R.id.txtEmail);

        mDrawer = findViewById(R.id.drawerLayout);
        mToggle = new ActionBarDrawerToggle(
                this, mDrawer, actionBarToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(mToggle);


        mNavigationView.setNavigationItemSelectedListener(this);
        mNavigationView.setCheckedItem(R.id.nav_menu_inicio);

        mDrawer.findViewById(R.id.btnRateUs).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Answers.getInstance().logCustom(new CustomEvent("Menu lateral - Evaluanos"));
                goToRateUs();
            }
        });

        mDrawer.findViewById(R.id.btnTermsAndConditions).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Answers.getInstance().logCustom(new CustomEvent("Menu lateral - Términos y condiciones"));
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.coordinatorLayout, TermsAndConditionsFragment.newInstance(TermsAndConditionsFragment.IN_DRAWER), FRAGMENT_TYC)
                        .addToBackStack(null).commitAllowingStateLoss();
            }
        });

        if (isUserLogedIn(getApplicationContext())) {
            JsonObject user = SessionHelper.getUser(getApplicationContext());
            nameView.setText(
                    new StringBuilder().append(user.get("name").getAsString())
            );
            emailView.setText(user.get("email").getAsString());
            JsonElement pictureElement = user.get("picture");
            if (pictureElement != JsonNull.INSTANCE)
                setProfileImage(pictureElement.getAsString());

            mDrawer.findViewById(R.id.btnLogout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SessionHelper.clearSession(getApplicationContext());
                    AlarmBroadcastReceiver.cancelAllAlarms(getApplicationContext());
                    RecordatorioVisita.deleteAll(RecordatorioVisita.class);
                    RecordatorioToma.deleteAll(RecordatorioToma.class);
                    startActivity(new Intent(MainHomeActivity.this, LoginActivity.class));
                    finish();
                }
            });
        } else {
            nameView.setText(getString(R.string.guest_name));
            emailView.setText("");

            View btnLogOut = mDrawer.findViewById(R.id.btnLogout);
            ImageView btnLogoutIcon = btnLogOut.findViewById(R.id.btnLogoutIcon);
            TextView btnLogoutText = btnLogOut.findViewById(R.id.btnLogoutText);
            btnLogoutIcon.setImageResource(R.drawable.ic_exit_to_app_black_24dp);
            btnLogoutText.setText(R.string.btn_ingresar);
            btnLogOut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Answers.getInstance().logCustom(new CustomEvent("Menu lateral - Cerrar sesión"));
                    logOut();
                }
            });
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.coordinatorLayout, HomeFragment.newInstance(), FRAGMENT_HOME).commit();
        setTitle(R.string.action_bar_title_home);
        LocalBroadcastManager.getInstance(this).registerReceiver(mIntentManager, new IntentFilter(FRAGMENT_MANAGEMENT));

        mToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                if (v != null) {
                    IdleHelper.ocultarTeclado(MainHomeActivity.this);
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

    private void logOut() {
        startActivity(new Intent(MainHomeActivity.this, LoginActivity.class));
        finish();
    }

    private void openColabora() {
        startActivity(new Intent(MainHomeActivity.this, ColaboraActivity.class));
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        mToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mIntentManager);
    }

    private Fragment getFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.coordinatorLayout);
    }

    @Override
    public void showBackNavigationIcon() {
        if (getSupportActionBar() == null) return;
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            mToggle.setDrawerIndicatorEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            colaboraFab.show();
        } else {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            mToggle.setDrawerIndicatorEnabled(true);
            mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            colaboraFab.hide();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection Failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(mNavigationView)) {
            mDrawer.closeDrawer(mNavigationView);
        } else {
            Fragment f = getFragment();
            if (f instanceof RegisterAccountFragment && ((RegisterAccountFragment) f).getAction() == ACTION_COMPLETE_INFO) {
                logOut();
            } else {
                super.onBackPressed();
            }
        }
    }

    public void needHaveAnAccount(String message) {
        Intent i = new Intent(this, LoginActivity.class);
        i.putExtra("needBackButton", true);
        startActivity(i);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        if (!item.isChecked()) {
            int id = item.getItemId();
            final FragmentManager fm = getSupportFragmentManager();
            String fragmentTag;
            switch (id) {
                case R.id.nav_menu_inicio:
                    Answers.getInstance().logContentView(new ContentViewEvent()
                            .putContentType(CONTENT_TYPE_VIEW)
                            .putContentId(VIEW_MENU_ID)
                            .putContentName("Menu lateral - Menu inicio"));
                    fm.beginTransaction()
                            .replace(R.id.coordinatorLayout, HomeFragment.newInstance(), FRAGMENT_HOME).commit();
                    setTitle(R.string.action_bar_title_home);
                    colaboraFab.show();
                    break;
                case R.id.nav_alerta_de_toma:
                    Answers.getInstance().logContentView(new ContentViewEvent()
                            .putContentType(CONTENT_TYPE_VIEW)
                            .putContentId(VIEW_MY_TAKES_REMINDERS_ID)
                            .putContentName("Menu lateral - Alertas de Toma"));
                    fm.beginTransaction()
                            .replace(R.id.coordinatorLayout, MisRecordatoriosTomaFragment.newInstance(), FRAGMENT_MIS_RECORDATORIOS_TOMA).commit();
                    setTitle(R.string.action_bar_title_mis_recordatorios_de_toma);
                    colaboraFab.show();
                    break;
                case R.id.nav_visitar_medico:
                    Answers.getInstance().logContentView(new ContentViewEvent()
                            .putContentType(CONTENT_TYPE_VIEW)
                            .putContentId(VIEW_MY_VISIT_REMINDERS_ID)
                            .putContentName("Menu lateral - Visita al Medico"));
                    fm.beginTransaction()
                            .replace(R.id.coordinatorLayout, MisRecordatoriosVisitaFragment.newInstance(), FRAGMENT_MIS_RECORDATORIOS_VISITA).commit();
                    setTitle(R.string.action_bar_title_mis_recordatorios_de_visita);
                    colaboraFab.show();
                    break;
                case R.id.nav_fav:
                    if (isUserLogedIn(getApplicationContext())) {
                        final ProgressDialog progressDialog = ProgressDialog.show(this, "", getString(R.string.loading));
                        Answers.getInstance().logContentView(new ContentViewEvent()
                                .putContentType(CONTENT_TYPE_VIEW)
                                .putContentId(VIEW_MY_FAVOURITES_ID)
                                .putContentName("Menu lateral - Favoritos"));
                        getMyFavouritesProducts(this, getAccessToken(getApplicationContext()), new JsonArrayResponse() {
                            @Override
                            public void onSuccess(final JsonArray products) {
                                getMyFavouritesPharmacies(MainHomeActivity.this, getAccessToken(getApplicationContext()), new JsonArrayResponse() {
                                    @Override
                                    public void onSuccess(JsonArray pharmacies) {
                                        progressDialog.dismiss();
                                        fm.popBackStack();
                                        fm.beginTransaction()
                                                .replace(R.id.coordinatorLayout, MisFavoritosFragment.newInstance(pharmacies, products), FRAGMENT_FAVORITOS).commit();
                                        setTitle(getString(R.string.action_bar_title_mis_favoritos));
                                        colaboraFab.show();
                                    }

                                    @Override
                                    public void onError(JsonObject response, Exception e) {
                                        progressDialog.dismiss();
                                        Toast.makeText(MainHomeActivity.this, R.string.error_message_internet_conection, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onError(JsonObject response, Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(MainHomeActivity.this, R.string.error_message_internet_conection, Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        needHaveAnAccount(getString(R.string.must_be_loged_to_have_favs));
                        return false;
                    }
                    break;
                case R.id.nav_my_account:
                    if (isUserLogedIn(getApplicationContext())) {
                        Answers.getInstance().logContentView(new ContentViewEvent()
                                .putContentType(CONTENT_TYPE_VIEW)
                                .putContentId(VIEW_MY_PROFILE_ID)
                                .putContentName("Menu lateral - Mi Perfil"));
                        fragmentTag = this.getSupportFragmentManager().getFragments().get(1).getTag();
                        fragmentTag = fragmentTag != null ? fragmentTag : "";
                        switch (fragmentTag) {
                            case FRAGMENT_HOME:
                                mNavigationView.setCheckedItem(R.id.nav_menu_inicio);
                                break;
                            case FRAGMENT_MIS_RECORDATORIOS_TOMA:
                                mNavigationView.setCheckedItem(R.id.nav_alerta_de_toma);
                                break;
                            case FRAGMENT_MIS_RECORDATORIOS_VISITA:
                                mNavigationView.setCheckedItem(R.id.nav_visitar_medico);
                                break;
                            case FRAGMENT_FAVORITOS:
                                mNavigationView.setCheckedItem(R.id.nav_fav);
                                break;
                            case FRAGMENT_BUSCAR_PROGRAMA_PACIENTE:
                                mNavigationView.setCheckedItem(R.id.nav_pp);
                                break;
                            case FRAGMENT_ABOUT_US:
                                mNavigationView.setCheckedItem(R.id.nav_about_us);
                                break;
                        }
                        fm.beginTransaction()
                                .add(R.id.coordinatorLayout, RegisterAccountFragment.newInstance(ACTION_UPDATE), FRAGMENT_REGISTER_ACCOUNT)
                                .addToBackStack(null).commitAllowingStateLoss();
                        mDrawer.closeDrawer(GravityCompat.START);
                    } else {
                        needHaveAnAccount(getString(R.string.must_be_loged_to_have_account));
                    }
                    return false;
                /*case R.id.nav_colabora:
                    String url = Constants.Urls.YAPP_COLABORA;
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                    break;*/
                case R.id.nav_pp:
                    Answers.getInstance().logContentView(new ContentViewEvent()
                            .putContentType(CONTENT_TYPE_VIEW)
                            .putContentId(VIEW_BUSCAR_PROGRAMA_PACIENTE_ID)
                            .putContentName("Menu lateral - Buscar Programas Paciente"));
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.coordinatorLayout, BuscarProgramaPacienteFragment.newInstance(), FRAGMENT_BUSCAR_PROGRAMA_PACIENTE).commit();
                    setTitle(R.string.action_bar_title_programa_paciente);
                    colaboraFab.show();
                    break;
                case R.id.nav_bioequivalencias:
                    Answers.getInstance().logCustom(new CustomEvent("Bioequivalencias"));
                    fragmentTag = this.getSupportFragmentManager().getFragments().get(1).getTag();
                    fragmentTag = fragmentTag != null ? fragmentTag : "";
                    switch (fragmentTag) {
                        case FRAGMENT_HOME:
                            mNavigationView.setCheckedItem(R.id.nav_menu_inicio);
                            break;
                        case FRAGMENT_MIS_RECORDATORIOS_TOMA:
                            mNavigationView.setCheckedItem(R.id.nav_alerta_de_toma);
                            break;
                        case FRAGMENT_MIS_RECORDATORIOS_VISITA:
                            mNavigationView.setCheckedItem(R.id.nav_visitar_medico);
                            break;
                        case FRAGMENT_FAVORITOS:
                            mNavigationView.setCheckedItem(R.id.nav_fav);
                            break;
                        case FRAGMENT_BUSCAR_PROGRAMA_PACIENTE:
                            mNavigationView.setCheckedItem(R.id.nav_pp);
                            break;
                        case FRAGMENT_ABOUT_US:
                            mNavigationView.setCheckedItem(R.id.nav_about_us);
                            break;
                    }
                    String url = Constants.Urls.YAPP_BIOEQUIVALENCIAS;
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                    return false;
                case R.id.nav_about_us:
                    Answers.getInstance().logContentView(new ContentViewEvent()
                            .putContentType(CONTENT_TYPE_VIEW)
                            .putContentId(VIEW_ABOUT_US_ID)
                            .putContentName("Menu lateral - Acerca de"));
                    fm.beginTransaction()
                            .replace(R.id.coordinatorLayout, AboutUsFragment.newInstance(), FRAGMENT_ABOUT_US).commit();
                    setTitle(R.string.action_bar_title_about_us);
                    colaboraFab.show();
                    break;

            }
        }
        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void goToRateUs() {
        Uri uri = Uri.parse("market://details?id=" + this.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + this.getPackageName())));
        }
    }

    public void setProfileImage(String imageUrl) {
        DownloadImageRequest.downloadProfilePhoto(getApplicationContext(), imageUrl, mProfileImage, R.drawable.avatar);
    }

    @Override
    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }
}
