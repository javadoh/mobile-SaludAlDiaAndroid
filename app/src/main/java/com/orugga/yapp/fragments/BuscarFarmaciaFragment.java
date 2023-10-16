package com.orugga.yapp.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.orugga.yapp.AlarmBroadcastReceiver;
import com.orugga.yapp.Constants;
import com.orugga.yapp.LoginActivity;
import com.orugga.yapp.R;
import com.orugga.yapp.adapters.AutoCompleteUbicacionActualAdapter;
import com.orugga.yapp.database.RecordatorioToma;
import com.orugga.yapp.database.RecordatorioVisita;
import com.orugga.yapp.helpers.DialogHelper;
import com.orugga.yapp.helpers.SessionHelper;
import com.orugga.yapp.interfaces.ActivityUpdateBackToolbar;
import com.orugga.yapp.interfaces.GoogleApiClientActivity;
import com.orugga.yapp.interfaces.JsonArrayResponse;
import com.orugga.yapp.interfaces.OnFragmentUpdateToolbarIntefrace;
import com.orugga.yapp.interfaces.PharmacyCallback;
import com.orugga.yapp.requests.DownloadImageRequest;
import com.orugga.yapp.requests.SearchNearPharmaciesRequest;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static com.orugga.yapp.Constants.ApiFields.PHARMACY_LATITUDE;
import static com.orugga.yapp.Constants.ApiFields.PHARMACY_LONGITUDE;
import static com.orugga.yapp.Constants.ApiFields.PHARMACY_PHARMACYCHAIN;
import static com.orugga.yapp.Constants.IntentFilters.ACTION_SEARCH;
import static com.orugga.yapp.Constants.IntentFilters.ACTION_SEE_PHARMACY;
import static com.orugga.yapp.Constants.IntentFilters.ACTION_SELECT_LOCATION;
import static com.orugga.yapp.Constants.IntentFilters.ACTION_SELECT_PHARMACY;
import static com.orugga.yapp.Constants.RequestPermission.REQUEST_PERMISSION_LOCATION;
import static com.orugga.yapp.helpers.IdleHelper.ocultarTeclado;
import static com.orugga.yapp.helpers.MapHelper.buildAlertMessageNoGps;
import static com.orugga.yapp.helpers.MapHelper.createCameraUpdateFromPharmacies;
import static com.orugga.yapp.helpers.MapHelper.distanceFromTo;
import static com.orugga.yapp.helpers.SessionHelper.getAccessToken;

/**
 * A simple {@link Fragment} subclass.
 */
public class BuscarFarmaciaFragment extends Fragment
        implements OnMapReadyCallback, OnFragmentUpdateToolbarIntefrace, GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnCameraIdleListener, GoogleMap.OnCameraMoveStartedListener, GoogleMap.OnMapClickListener {


    public static final int MIN_ZOOM = 12;
    public static final int MAX_ZOOM = 17;
    public static final int MAX_ADJUST_VIEW_RADIUS = 15;
    private GoogleMap mMap;
    private MapView mMapView;
    private AutoCompleteTextView mAutoCompleteUbicacionActual;
    private AutoCompleteUbicacionActualAdapter mAutoCompleteUbicacionAdapter;
    private CheckBox mBtnUbicacion;

    //Location things
    private FusedLocationProviderClient mFusedLocation;
    private LocationRequest mLocationRequest;
    private Marker mMarkerActualLocation;
    private LatLng myLocation;

    //GoogleApiClient
    private GoogleApiClient mGoogleApiClient;
    private LatLngBounds mBounds = new LatLngBounds(
            new LatLng(-34.698436, -58.531409), new LatLng(-34.498436, -58.331409));
    private ArrayList<Pair<Marker, JsonObject>> mPharmaciesByMarker;

    //Array de farmacias en caso que me las envien
    private JsonArray mPharmacies;
    private Bundle mBundle;
    private AlertDialog alert;
    private boolean onAdjustView = false;
    private int mAction;
    private PharmacyCallback pharmacyCallback;
    private ResultCallback<PlaceBuffer> mUpdateUbicacionActual = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(@NonNull PlaceBuffer places) {
            if (mMap == null) return;
            if (!places.getStatus().isSuccess()) {
                Log.e("BuscarFarmaciasError", "Place query did not complete. Error: " + places.getStatus().toString());
                places.release();
                return;
            }
            final Place place = places.get(0);
            mMap.clear();
            String name = place.getName() != null ? place.getName().toString().split(",")[0] : "Sin direccion";
            mAutoCompleteUbicacionActual.setText(name);
            if (mBtnUbicacion.isChecked()) {
                mBtnUbicacion.setChecked(false);
                if (mMarkerActualLocation != null) mMarkerActualLocation.remove();
            }
            markAndMoveToLocation(place.getLatLng());
            onAdjustView = true;
            buscarFarmaciasCercanas(place.getLatLng().latitude, place.getLatLng().longitude, 1);
            places.release();

        }
    };
    //Listeners
    private AdapterView.OnItemClickListener mAutocompleteUbicacionActualItemClickListener =
            new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    final AutocompletePrediction item = mAutoCompleteUbicacionAdapter.getItem(position);
                    final String placeId = item.getPlaceId();
                    PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                            .getPlaceById(mGoogleApiClient, placeId);
                    placeResult.setResultCallback(mUpdateUbicacionActual);
                    mAutoCompleteUbicacionActual.clearFocus();
                    ocultarTeclado(getActivity());
                }
            };
    private OnSuccessListener<Location> locationOnSuccessListener = new OnSuccessListener<Location>() {
        @SuppressLint("MissingPermission")
        @Override
        public void onSuccess(Location location) {
            if (getActivity() == null || getActivity().isFinishing() || mMap == null) return;
            onAdjustView = true;
            if (location != null) {
                if (mMarkerActualLocation != null)
                    mMarkerActualLocation.remove();
                mMap.clear();
                markAndMoveToLocation(location);
                buscarFarmaciasCercanas(location.getLatitude(), location.getLongitude(), 1);
            } else {
                mLocationRequest = new LocationRequest();
                mLocationRequest.setInterval(2000);
                mLocationRequest.setFastestInterval(500);
                mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                mFusedLocation.requestLocationUpdates(mLocationRequest,
                        new LocationCallback() {
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                if (getActivity() == null || getActivity().isFinishing() || mMap == null)
                                    return;
                                Location fetchedLocation = locationResult.getLastLocation();
                                if (fetchedLocation != null) {
                                    mMap.clear();
                                    markAndMoveToLocation(fetchedLocation);
                                    buscarFarmaciasCercanas(fetchedLocation.getLatitude(), fetchedLocation.getLongitude(), 1);
                                } else {
                                    mBtnUbicacion.setChecked(false);
                                    Toast.makeText(getContext(), R.string.error_no_ubicacion, Toast.LENGTH_SHORT).show();
                                }
                                mFusedLocation.removeLocationUpdates(this);
                            }
                        },
                        null);
            }
        }
    };
    private View.OnClickListener btnUbicacionOnClickListener = new View.OnClickListener() {
        @SuppressLint("MissingPermission")
        @Override
        public void onClick(View v) {
            ocultarTeclado(getActivity());
            if (mBtnUbicacion.isChecked()) {
                if (!runtime_permissions()) {
                    final LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                    if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        alert = buildAlertMessageNoGps(getActivity());
                        mBtnUbicacion.setChecked(false);
                    } else {
                        mFusedLocation.getLastLocation().addOnSuccessListener(getActivity(), locationOnSuccessListener);
                    }
                } else mBtnUbicacion.setChecked(false);
            } else {
                if (mMarkerActualLocation != null)
                    mMarkerActualLocation.remove();
            }
        }
    };
    private Timer timer;
    private Boolean isUserMovingTheMap = false;

    public BuscarFarmaciaFragment() {
        // Required empty public constructor
    }

    public static BuscarFarmaciaFragment newInstance(int mAction, PharmacyCallback callback) {
        BuscarFarmaciaFragment fragment = new BuscarFarmaciaFragment();
        fragment.mAction = mAction;
        fragment.pharmacyCallback = callback;
        return fragment;
    }

    public static BuscarFarmaciaFragment newInstance(int mAction) {
        BuscarFarmaciaFragment fragment = new BuscarFarmaciaFragment();
        fragment.mAction = mAction;
        return fragment;
    }

    public static BuscarFarmaciaFragment newInstance(JsonArray pharmacies, int mAction) {
        BuscarFarmaciaFragment fragment = new BuscarFarmaciaFragment();
        fragment.mPharmacies = pharmacies;
        fragment.mAction = mAction;
        return fragment;
    }

    public static BuscarFarmaciaFragment newInstance(JsonArray pharmacies, LatLng location, int mAction) {
        BuscarFarmaciaFragment fragment = new BuscarFarmaciaFragment();
        fragment.mPharmacies = pharmacies;
        fragment.myLocation = location;
        fragment.mAction = mAction;
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mFusedLocation = LocationServices.getFusedLocationProviderClient(getContext());

        View mView = inflater.inflate(R.layout.fragment_buscar_farmacia, container, false);

        mMapView = mView.findViewById(R.id.mapView);
        mMapView.onCreate(mBundle);

        mMapView.onResume(); // needed to get the map to display immediately
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mMapView.getMapAsync(this);
        mPharmaciesByMarker = new ArrayList<>();
        mAutoCompleteUbicacionActual = mView.findViewById(R.id.txtAutoCompleteUbicacionActual);
        mBtnUbicacion = mView.findViewById(R.id.btnBuscarFarmaciaUbicacion);

        if (mAction == ACTION_SEARCH || mAction == ACTION_SELECT_PHARMACY || mAction == ACTION_SELECT_LOCATION) {
            mAutoCompleteUbicacionAdapter = new AutoCompleteUbicacionActualAdapter(getContext(),
                    mGoogleApiClient,
                    mBounds,
                    new AutocompleteFilter.Builder().setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS).setCountry("CL").build());

            mAutoCompleteUbicacionActual.setAdapter(mAutoCompleteUbicacionAdapter);
            mAutoCompleteUbicacionActual.setOnItemClickListener(mAutocompleteUbicacionActualItemClickListener);

            mBtnUbicacion.setOnClickListener(btnUbicacionOnClickListener);
        } else {
            mView.findViewById(R.id.searchItemsLayout).setVisibility(View.GONE);
        }
        return mView;
    }

    private void markAndMoveToLocation(Location location) {
        LatLng actualLocation = new LatLng(location.getLatitude(), location.getLongitude());
        markAndMoveToLocation(actualLocation);
    }

    private void markAndMoveToLocation(LatLng location) {
        if (mMap == null) return;
        mMap.animateCamera(CameraUpdateFactory
                .newLatLngZoom(location, 15));
        addMyLocationMarker(location);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mBundle = savedInstanceState;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        onAdjustView = true;
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        mMap.setOnCameraIdleListener(this);
        mMap.setOnCameraMoveStartedListener(this);
//        mMap.setMinZoomPreference(MIN_ZOOM);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-33.440002, -70.644203), 12));
        if (mAction == ACTION_SELECT_LOCATION)
            mMap.setOnMapClickListener(this);
        if (mPharmacies == null) {
            if (!runtime_permissions()) {
                setActualLocation();
            } else {
                buscarFarmaciasCercanas(-33.440002, -70.644203, 1);
            }
        } else if (mPharmacies.size() == 1) {

            crearMarkers(mPharmacies);
            JsonObject pharmacy = mPharmacies.get(0).getAsJsonObject();
            double latitude = pharmacy.get(PHARMACY_LATITUDE).getAsDouble();
            double longitude = pharmacy.get(PHARMACY_LONGITUDE).getAsDouble();
            if (mAction == ACTION_SEE_PHARMACY)
                DialogHelper.showPharmacyDetails(getActivity(), pharmacy);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15));
        } else {
            if (mMarkerActualLocation != null)
                mMap.animateCamera(createCameraUpdateFromPharmacies(mMarkerActualLocation.getPosition(), mPharmacies, 3, getContext()));
            crearMarkers(mPharmacies);
            markAndMoveToLocation(myLocation);
            try {
                mMap.animateCamera(createCameraUpdateFromPharmacies(myLocation, mPharmacies, mPharmacies.size(), getContext()));
            } catch (Exception ignored) {
                Crashlytics.logException(ignored);
                //TODO: ver si se puede solucionar este bug
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void setActualLocation() {
        final LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            alert = buildAlertMessageNoGps(getActivity());
        } else {
            mFusedLocation.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (getActivity() == null || getActivity().isFinishing()) return;
                    if (location != null) {
                        mBtnUbicacion.setChecked(true);
                        markAndMoveToLocation(location);
                        buscarFarmaciasCercanas(location.getLatitude(), location.getLongitude(), 1);
                    } else {
                        mLocationRequest = new LocationRequest();
                        mLocationRequest.setInterval(2000);
                        mLocationRequest.setFastestInterval(500);
                        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                        mFusedLocation.requestLocationUpdates(mLocationRequest,
                                new LocationCallback() {
                                    @Override
                                    public void onLocationResult(LocationResult locationResult) {
                                        if (getActivity() == null || getActivity().isFinishing() || !isAdded()) return;
                                        Location fetchedLocation = locationResult.getLastLocation();
                                        if (fetchedLocation != null) {
                                            mBtnUbicacion.setChecked(true);
                                            markAndMoveToLocation(fetchedLocation);
                                            buscarFarmaciasCercanas(fetchedLocation.getLatitude(), fetchedLocation.getLongitude(), 1);
                                        } else {
                                            Toast.makeText(getContext(), "no se pudo conseguir su ubicación", Toast.LENGTH_SHORT).show();
                                        }
                                        mFusedLocation.removeLocationUpdates(this);
                                    }
                                },
                                null);
                    }
                }
            });
        }
    }

    private void buscarFarmaciasCercanas(final double latitude, final double longitude, final double radius) {
        if (mAction == ACTION_SELECT_LOCATION) return;
        SearchNearPharmaciesRequest.fetch(getActivity(), getAccessToken(getContext()), latitude, longitude, radius, new JsonArrayResponse() {
            @Override
            public void onSuccess(JsonArray pharmacies) {
                if (getActivity() == null || mMap == null || !isAdded()) return;
                if (onAdjustView) {
                    if (pharmacies.size() >= 3) {
                        onAdjustView = false;
                        crearMarkers(pharmacies);
                        mMap.animateCamera(createCameraUpdateFromPharmacies(new LatLng(latitude, longitude), pharmacies, pharmacies.size(), getContext()));
                    } else if (radius < MAX_ADJUST_VIEW_RADIUS) {
                        double newRadius = radius + 1;
                        buscarFarmaciasCercanas(latitude, longitude, newRadius);
                    } else if (pharmacies.size() > 0) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), MAX_ZOOM));
                    } else {
                        onAdjustView = false;
                        Toast.makeText(getActivity(), R.string.error_message_results_not_found, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (pharmacies.size() > 0) {
                        crearMarkers(pharmacies);
                    } else {
                        Toast.makeText(getActivity(), R.string.error_message_results_not_found, Toast.LENGTH_SHORT).show();
                    }
                }

            }

            @Override
            public void onError(JsonObject response, Exception e) {
                if (getActivity() == null || getActivity().isFinishing() || !isAdded()) return;
                onAdjustView = false;
                if (e != null) {
                    Crashlytics.logException(e);
                } else if (response.get("rta") != null && !response.get("rta").isJsonNull() &&
                        response.get("rta").getAsString().equals("Unauthorised")) {
                    Toast.makeText(getContext(), R.string.expired_session, Toast.LENGTH_SHORT).show();
                    SessionHelper.clearSession(getActivity().getApplicationContext());
                    AlarmBroadcastReceiver.cancelAllAlarms(getActivity().getApplicationContext());
                    RecordatorioVisita.deleteAll(RecordatorioVisita.class);
                    RecordatorioToma.deleteAll(RecordatorioToma.class);
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                    getActivity().finish();
                } else {
                    Toast.makeText(getContext(), R.string.error_message_internet_conection, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void crearMarkers(JsonArray pharmacies) {
        mPharmaciesByMarker = new ArrayList<>();
        if (mMarkerActualLocation != null) {
            LatLng myLatLong = mMarkerActualLocation.getPosition();
            mMap.clear();
            addMyLocationMarker(myLatLong);
        } else {
            mMap.clear();
        }
        for (int i = 0; i < pharmacies.size(); i++) {
            crearMarker(pharmacies.get(i).getAsJsonObject());
        }
    }

    private void crearMarker(final JsonObject farmacia) {
        final MarkerOptions markerOptions = new MarkerOptions()
                .position(new LatLng(farmacia.get(PHARMACY_LATITUDE).getAsDouble(), farmacia.get(PHARMACY_LONGITUDE).getAsDouble()));
        StringBuilder urlMarkerByDensity = new StringBuilder();
        urlMarkerByDensity.append("full_path_marker_")
                .append(getResources().getString(R.string.density_bucket));

        DownloadImageRequest.downloadMarker(getContext(), farmacia.getAsJsonObject(PHARMACY_PHARMACYCHAIN).get(urlMarkerByDensity.toString()).getAsString(), new FutureCallback<Bitmap>() {
            @Override
            public void onCompleted(Exception e, Bitmap result) {
                if (e == null && result != null) {
                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(result));
                } else {
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_generico));
                }
                Marker newMarker = mMap.addMarker(markerOptions);
                mPharmaciesByMarker.add(new Pair<>(newMarker, farmacia));
            }
        });
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (mAction == ACTION_SELECT_LOCATION && pharmacyCallback != null) {
            pharmacyCallback.onGetLocation(marker.getPosition());
            getActivity().getSupportFragmentManager().popBackStack();
        } else {
            JsonObject farmacia = buscarFarmaciaPorMarker(marker);
            if (farmacia == null) return true;
            if (mAction == ACTION_SELECT_PHARMACY && pharmacyCallback != null) {
                pharmacyCallback.onGetPharmacy(farmacia);
                getActivity().getSupportFragmentManager().popBackStack();
            } else {
                Answers.getInstance().logCustom(new CustomEvent("Farmacias abiertas - Ver Farmacia"));
                DialogHelper.showPharmacyDetails(getActivity(), farmacia);
            }
        }
        return false;
    }

    private JsonObject buscarFarmaciaPorMarker(Marker marker) {
        JsonObject farmacia = null;
        int i = 0;
        while (i < mPharmaciesByMarker.size() && farmacia == null) {
            Pair<Marker, JsonObject> parMarkerFarmacia = mPharmaciesByMarker.get(i);
            if (marker.equals(parMarkerFarmacia.first)) {
                farmacia = parMarkerFarmacia.second;
            }
            i++;
        }
        return farmacia;
    }

    @Override
    public void onFragmentUpdateToolbar() {
        getActivity().setTitle(getString(R.string.action_bar_title_buscar_farmacia));
        if (getActivity() instanceof ActivityUpdateBackToolbar) {
            ((ActivityUpdateBackToolbar) getActivity()).showBackNavigationIcon();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getActivity(), "Connection Failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        if (alert != null)
            alert.dismiss();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getActivity() instanceof GoogleApiClientActivity)
            mGoogleApiClient = ((GoogleApiClientActivity) getActivity()).getGoogleApiClient();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mGoogleApiClient = null;
        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);

        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    private void addMyLocationMarker(LatLng myLatLong) {
        mMarkerActualLocation = mMap.addMarker(
                new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_mi_ubicacion_marker))
                        .position(myLatLong)
                        .title(getString(R.string.mi_ubicacion)));
    }

    public boolean runtime_permissions() {
        if (Build.VERSION.SDK_INT >= 23 && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSION_LOCATION);
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setActualLocation();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

    @Override
    public void onCameraMoveStarted(int reason) {
        isUserMovingTheMap = reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE;
    }


    @Override
    public void onCameraIdle() {
        if ((mAction == ACTION_SEARCH || mAction == ACTION_SELECT_PHARMACY) && isUserMovingTheMap) {
            isUserMovingTheMap = false;
            if (timer == null)
                timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (!isAdded() || isUserMovingTheMap) return;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (MIN_ZOOM >= mMap.getCameraPosition().zoom || mMap.getCameraPosition().zoom >= MAX_ZOOM)
                                return;
                            VisibleRegion vr = mMap.getProjection().getVisibleRegion();
                            double distance = distanceFromTo(vr.farLeft, vr.farRight) * 100;
                            LatLng target = mMap.getCameraPosition().target;
                            buscarFarmaciasCercanas(target.latitude, target.longitude, distance);
                        }
                    });

                }
            }, 1500);
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        mMap.addMarker(new MarkerOptions().position(latLng));
    }
}
