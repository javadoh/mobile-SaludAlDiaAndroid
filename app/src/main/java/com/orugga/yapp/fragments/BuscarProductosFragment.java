package com.orugga.yapp.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.orugga.yapp.AlarmBroadcastReceiver;
import com.orugga.yapp.LoginActivity;
import com.orugga.yapp.R;
import com.orugga.yapp.adapters.AutoCompleteBuscarProductosAdapter;
import com.orugga.yapp.adapters.AutoCompleteUbicacionActualAdapter;
import com.orugga.yapp.adapters.ProductListAdapter;
import com.orugga.yapp.database.RecordatorioToma;
import com.orugga.yapp.database.RecordatorioVisita;
import com.orugga.yapp.helpers.DialogHelper;
import com.orugga.yapp.helpers.SessionHelper;
import com.orugga.yapp.interfaces.ActivityUpdateBackToolbar;
import com.orugga.yapp.interfaces.GoogleApiClientActivity;
import com.orugga.yapp.interfaces.JsonArrayResponse;
import com.orugga.yapp.interfaces.OnFragmentUpdateToolbarIntefrace;
import com.orugga.yapp.requests.DownloadImageRequest;
import com.orugga.yapp.requests.SearchNearPharmaciesRequest;
import com.orugga.yapp.requests.SearchNearPharmaciesWithProductsRequest;
import com.orugga.yapp.requests.SearchPacientProgramsRequest;
import com.orugga.yapp.requests.SearchProductsRequest;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import pl.droidsonroids.gif.GifImageView;

import static com.orugga.yapp.Constants.ApiFields.PHARMACY_LATITUDE;
import static com.orugga.yapp.Constants.ApiFields.PHARMACY_LONGITUDE;
import static com.orugga.yapp.Constants.ApiFields.PHARMACY_PHARMACYCHAIN;
import static com.orugga.yapp.Constants.ApiFields.PRODUCT_ID;
import static com.orugga.yapp.Constants.FragmentsNames.FRAGMENT_RESULTADOS_BUSQUEDA_PRODUCTO;
import static com.orugga.yapp.Constants.RequestPermission.REQUEST_PERMISSION_LOCATION;
import static com.orugga.yapp.helpers.IdleHelper.ocultarTeclado;
import static com.orugga.yapp.helpers.MapHelper.buildAlertMessageNoGps;
import static com.orugga.yapp.helpers.MapHelper.createCameraUpdateFromPharmacies;
import static com.orugga.yapp.helpers.SessionHelper.getAccessToken;
import static com.orugga.yapp.helpers.SessionHelper.getUserName;


public class BuscarProductosFragment extends Fragment
        implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMarkerClickListener,
        OnFragmentUpdateToolbarIntefrace {

    GoogleMap mMap;

    View mView;

    //GoogleApiClient
    private GoogleApiClient mGoogleApiClient;
    private LatLngBounds mBounds = new LatLngBounds(
            new LatLng(-34.698436, -58.531409), new LatLng(-34.498436, -58.331409));


    private LatLng mActualLocation;
    private Marker mMarkerActualLocation;
    private MapView mMapView;

    private ArrayList<Pair<Marker, JsonObject>> mPharmaciesByMarker;
    private AlertDialog alert;

    //Location things
    private FusedLocationProviderClient mFusedLocation;
    private LocationRequest mLocationRequest;

    private AutoCompleteUbicacionActualAdapter mAutoCompleteUbicacionAdapter;
    private AutoCompleteTextView mAutoCompleteUbicacionActual;
    private AutoCompleteBuscarProductosAdapter mAutoCompleteBuscarProductosAdapter;
    private AutoCompleteTextView mAutoCompleteBuscarProducto;

    private LinearLayout mProductList;
    private ProductListAdapter mProductListAdapter;

    private CheckBox mBtnUbicacion;

    private JsonArray mInitialProducts;

    private Bundle mBundle;
    private AdapterView.OnItemClickListener mAutocompleteProductItemClickListener =
            new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    JsonObject product = mAutoCompleteBuscarProductosAdapter.getItem(position);
                    if (!isViewAdded(product)) {
                        mProductListAdapter.add(product);
                        mProductListAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), R.string.toast_the_product_is_already_in_the_list, Toast.LENGTH_SHORT).show();
                    }
                    mAutoCompleteBuscarProducto.setText("");
                    ocultarTeclado(getActivity());
                    mAutoCompleteBuscarProducto.clearFocus();
                }
            };
    private ResultCallback<PlaceBuffer> mUpdateUbicacionActual = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(@NonNull PlaceBuffer places) {
            if (mMap == null || getActivity() == null || getActivity().isFinishing()) return;
            if (!places.getStatus().isSuccess()) {
                Log.e("BuscarProductosError", "Place query did not complete. Error: " + places.getStatus().toString());
                places.release();
                return;
            }
            if (places.getCount() == 0) return;
            final Place place = places.get(0);
            mMap.clear();
            String name = place.getName() != null ? place.getName().toString().split(",")[0] : "Sin direccion";
            mAutoCompleteUbicacionActual.setText(name);
            mActualLocation = place.getLatLng();
            if (mBtnUbicacion.isChecked()) {
                mBtnUbicacion.setChecked(false);
                if (mMarkerActualLocation != null)
                    mMarkerActualLocation.remove();
            }
            markAndMoveToLocation(place.getLatLng());
            buscarFarmaciasCercanas(mActualLocation.latitude, mActualLocation.longitude);
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
    private View.OnClickListener btnYappOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Answers.getInstance().logCustom(new CustomEvent("Click Yapp Button")
                    .putCustomAttribute("userName", getUserName(getContext())));
            if (mProductListAdapter.getCount() == 0) {
                Toast.makeText(getContext(), R.string.toast_response_debe_ingresar_almenos_un_producto, Toast.LENGTH_SHORT).show();
                return;
            }
            if (mActualLocation == null) {
                if (mAutoCompleteUbicacionActual.getText().length() <= 3) {
                    Toast.makeText(getContext(), R.string.toast_response_debe_ingresar_una_direccion, Toast.LENGTH_SHORT).show();
                } else {
                    if (mAutoCompleteUbicacionAdapter.getCount() == 0) {
                        Toast.makeText(getContext(), R.string.toast_response_debe_ingresar_una_direccion_valida, Toast.LENGTH_SHORT).show();
                    } else {
                        final AutocompletePrediction item = mAutoCompleteUbicacionAdapter.getItem(0);
                        final String placeId = item.getPlaceId();
                        PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                                .getPlaceById(mGoogleApiClient, placeId);
                        placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
                            @Override
                            public void onResult(@NonNull PlaceBuffer places) {
                                if (mMap == null) return;
                                if (!places.getStatus().isSuccess()) {
                                    Log.e("BuscarProductosError", "Place query did not complete. Error: " + places.getStatus().toString());
                                    places.release();
                                    return;
                                }
                                final Place place = places.get(0);
                                mMap.clear();
                                mActualLocation = place.getLatLng();
                                places.release();
                                searchProducts();
                            }
                        });
                    }
                }
            } else {
                searchProducts();
            }
        }
    };
    private View.OnClickListener btnUbicacionOnClickListener = new View.OnClickListener() {
        @SuppressLint("MissingPermission")
        @Override
        public void onClick(View v) {
            if (mMap == null) return;
            ocultarTeclado(getActivity());
            if (mBtnUbicacion.isChecked()) {
                if (!runtime_permissions()) {
                    final LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                    if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        alert = buildAlertMessageNoGps(getActivity());
                        mBtnUbicacion.setChecked(false);
                    } else {
                        mFusedLocation.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                            @SuppressLint("MissingPermission")
                            @Override
                            public void onSuccess(Location location) {
                                if (mMap == null) return;
                                if (location != null) {
                                    if (mMarkerActualLocation != null)
                                        mMarkerActualLocation.remove();
                                    mMap.clear();
                                    markAndMoveToLocation(location);
                                    buscarFarmaciasCercanas(location.getLatitude(), location.getLongitude());
                                } else {
                                    mLocationRequest = new LocationRequest();
                                    mLocationRequest.setInterval(2000);
                                    mLocationRequest.setFastestInterval(500);
                                    mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                                    mFusedLocation.requestLocationUpdates(mLocationRequest,
                                            new LocationCallback() {
                                                @Override
                                                public void onLocationResult(LocationResult locationResult) {
                                                    if (mMap == null || getActivity() == null || getActivity().isFinishing() || !isAdded())
                                                        return;
                                                    Location fetchedLocation = locationResult.getLastLocation();
                                                    if (fetchedLocation != null) {
                                                        mMap.clear();
                                                        markAndMoveToLocation(fetchedLocation);
                                                        buscarFarmaciasCercanas(fetchedLocation.getLatitude(), fetchedLocation.getLongitude());
                                                    } else {
                                                        mBtnUbicacion.setChecked(false);
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
                } else mBtnUbicacion.setChecked(false);
            } else {
                mActualLocation = null;
                if (mMarkerActualLocation != null)
                    mMarkerActualLocation.remove();
            }
        }
    };

    public BuscarProductosFragment() {
        // Required empty public constructor
    }

    public static BuscarProductosFragment newInstance() {
        return new BuscarProductosFragment();
    }

    public static BuscarProductosFragment newInstance(JsonArray products) {
        BuscarProductosFragment fragment = new BuscarProductosFragment();
        fragment.mInitialProducts = products;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBundle = savedInstanceState;
        setRetainInstance(true);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mFusedLocation = LocationServices.getFusedLocationProviderClient(getContext());

        mView = inflater.inflate(R.layout.fragment_buscar_productos, container, false);

        mMapView = mView.findViewById(R.id.mapView);
        mMapView.onCreate(mBundle);

        mMapView.onResume(); // needed to get the map to display immediately
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mMapView.getMapAsync(this);

        mAutoCompleteUbicacionActual = mView.findViewById(R.id.txtAutoCompleteUbicacionActual);
        mAutoCompleteUbicacionAdapter = new AutoCompleteUbicacionActualAdapter(getContext(),
                mGoogleApiClient,
                mBounds,
                new AutocompleteFilter.Builder().setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS).setCountry("CL").build());


        mAutoCompleteUbicacionActual.setAdapter(mAutoCompleteUbicacionAdapter);
        mAutoCompleteUbicacionActual.setOnItemClickListener(mAutocompleteUbicacionActualItemClickListener);

        mAutoCompleteBuscarProducto = mView.findViewById(R.id.txtAutoCompleteSearchProducts);

        mAutoCompleteBuscarProductosAdapter = new AutoCompleteBuscarProductosAdapter(getContext());
        mAutoCompleteBuscarProducto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 3 && s.length() < 256) {
                    buscarProducto(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mAutoCompleteBuscarProducto.setAdapter(mAutoCompleteBuscarProductosAdapter);
        mAutoCompleteBuscarProducto.setOnItemClickListener(mAutocompleteProductItemClickListener);

        mProductList = mView.findViewById(R.id.productListView);
        mProductListAdapter = new ProductListAdapter(getActivity(), getActivity().getSupportFragmentManager(), R.layout.product_item);
        mProductListAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                refreshProductList();
            }

            @Override
            public void onInvalidated() {
                refreshProductList();
            }
        });

        if (mInitialProducts != null) {
            for (int i = 0; i < mInitialProducts.size(); i++) {
                mProductListAdapter.add(mInitialProducts.get(i).getAsJsonObject());
            }
        }
        refreshProductList();

        setOnTouchListenerTransparentView();

        mBtnUbicacion = mView.findViewById(R.id.btnBuscarProductoUbicacion);
        mBtnUbicacion.setOnClickListener(btnUbicacionOnClickListener);

        LinearLayout btnYapp = mView.findViewById(R.id.btnBuscarProductoYAPP);
        btnYapp.setOnClickListener(btnYappOnClickListener);

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

    private void addMyLocationMarker(LatLng myLatLong) {
        mActualLocation = myLatLong;
        mMarkerActualLocation = mMap.addMarker(
                new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_mi_ubicacion_marker))
                        .position(myLatLong)
                        .title(getString(R.string.mi_ubicacion)));
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setOnTouchListenerTransparentView() {
        final ScrollView mainScrollView = mView.findViewById(R.id.scrollArea);
        ImageView transparentImageView = mView.findViewById(R.id.transparent_image);
        transparentImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        mainScrollView.requestDisallowInterceptTouchEvent(true);
                        // Disable touch on transparent view
                        return false;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        mainScrollView.requestDisallowInterceptTouchEvent(false);
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        mainScrollView.requestDisallowInterceptTouchEvent(true);
                        return false;

                    default:
                        return true;
                }
            }
        });
    }

    private void refreshProductList() {
        mProductList.removeAllViews();
        for (int i = 0; i < mProductListAdapter.getCount(); i++) {
            View row = mProductListAdapter.getView(i, null, mProductList);
            mProductList.addView(row);
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
//        Descomentar para habilitar el detalle de farmacia
//        mMap.setOnMarkerClickListener(this);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-33.440002, -70.644203), 14));
        if (!runtime_permissions()) {
            setActualLocation();
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
                    if (mMap == null) return;
                    if (location != null) {
                        mBtnUbicacion.setChecked(true);
                        markAndMoveToLocation(location);
                        buscarFarmaciasCercanas(location.getLatitude(), location.getLongitude());
                    } else {
                        mLocationRequest = new LocationRequest();
                        mLocationRequest.setInterval(2000);
                        mLocationRequest.setFastestInterval(500);
                        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                        mFusedLocation.requestLocationUpdates(mLocationRequest,
                                new LocationCallback() {
                                    @Override
                                    public void onLocationResult(LocationResult locationResult) {
                                        if (mMap == null || !isAdded()) return;
                                        Location fetchedLocation = locationResult.getLastLocation();
                                        if (fetchedLocation != null) {
                                            mBtnUbicacion.setChecked(true);
                                            markAndMoveToLocation(fetchedLocation);
                                            buscarFarmaciasCercanas(fetchedLocation.getLatitude(), fetchedLocation.getLongitude());
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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getActivity(), "Connection Failed", Toast.LENGTH_SHORT).show();
    }

    private void buscarProducto(final String producto) {
        SearchProductsRequest.fetch(getActivity(), getAccessToken(getActivity()), producto, new JsonArrayResponse() {
            @Override
            public void onSuccess(JsonArray response) {
                mAutoCompleteBuscarProductosAdapter.setResultsFromJson(response, producto);
            }

            @Override
            public void onError(JsonObject response, Exception e) {
                if (getActivity() == null || getActivity().isFinishing()) return;
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
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setActualLocation();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

    private boolean isViewAdded(JsonObject product) {
        int i = 0;
        while (i < mProductListAdapter.getCount() &&
                product.get("id").getAsLong() != mProductListAdapter.getItem(i).get("id").getAsLong()) {
            i++;
        }
        return i < mProductListAdapter.getCount();
    }

    private void searchProducts() {
//        final ProgressDialog progressDialog = ProgressDialog.show(getContext(), "", getString(R.string.loading));
        final Dialog dialog = new Dialog(getActivity(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setCancelable(false);
        GifImageView view = new GifImageView(getContext());
        view.setImageResource(R.drawable.animacion);
        dialog.setContentView(view);
        dialog.show();
        final ArrayList<String> productIds = new ArrayList<>();
        for (int i = 0; i < mProductListAdapter.getCount(); i++) {
            productIds.add(mProductListAdapter.getItem(i).get(PRODUCT_ID).getAsString());
        }
        if (productIds.size() == 1) {
            SearchPacientProgramsRequest.searchDetailPacientPrograms(getActivity(), Integer.valueOf(productIds.get(0)), new JsonArrayResponse() {
                @Override
                public void onSuccess(final JsonArray programaPacientes) {
                    SearchNearPharmaciesWithProductsRequest.fetch(getActivity(), mActualLocation.latitude, mActualLocation.longitude, 5, productIds, new JsonArrayResponse() {
                        @Override
                        public void onSuccess(final JsonArray pharmacies) {
                            if (getActivity() == null || getActivity().isFinishing()) return;
                            final Timer timer = new Timer();
                            getActivity().getSupportFragmentManager().beginTransaction()
                                    .add(R.id.coordinatorLayout, ResultadosBusquedaProductoFragment.newInstance(pharmacies, programaPacientes, new LatLng(mActualLocation.latitude, mActualLocation.longitude), programaPacientes.size() == 0, mProductListAdapter.getItem(0)))
                                    .addToBackStack(FRAGMENT_RESULTADOS_BUSQUEDA_PRODUCTO).commitAllowingStateLoss();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    if (dialog != null && dialog.isShowing())
                                        dialog.dismiss();
                                }
                            }, 3000);
                        }

                        @Override
                        public void onError(JsonObject response, Exception e) {
                            if (getActivity() == null || getActivity().isFinishing()) return;
                            dialog.dismiss();
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

                @Override
                public void onError(JsonObject response, Exception e) {
                    if (getActivity() == null || getActivity().isFinishing()) return;
                    dialog.dismiss();
                    if (e != null) {
                        Crashlytics.logException(e);
                    } else {
                        Toast.makeText(getContext(), R.string.error_message_internet_conection, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            SearchNearPharmaciesWithProductsRequest.fetch(getActivity(), mActualLocation.latitude, mActualLocation.longitude, 5, productIds, new JsonArrayResponse() {
                @Override
                public void onSuccess(final JsonArray response) {
                    if (getActivity() == null || getActivity().isFinishing()) return;
                    final Timer timer = new Timer();
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .add(R.id.coordinatorLayout, ResultadosBusquedaProductoFragment.newInstance(response, new JsonArray(), new LatLng(mActualLocation.latitude, mActualLocation.longitude), false, null))
                            .addToBackStack(FRAGMENT_RESULTADOS_BUSQUEDA_PRODUCTO).commitAllowingStateLoss();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (dialog != null && dialog.isShowing())
                                dialog.dismiss();
                        }
                    }, 3500);
                }

                @Override
                public void onError(JsonObject response, Exception e) {
                    if (getActivity() == null || getActivity().isFinishing()) return;
                    dialog.dismiss();
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
    }

    private void buscarFarmaciasCercanas(double latitude, double longitude) {
        SearchNearPharmaciesRequest.fetch(getActivity(), getAccessToken(getContext()), latitude, longitude, 1, new JsonArrayResponse() {
            @Override
            public void onSuccess(JsonArray pharmacies) {
                if (getActivity() != null && isAdded()) {
                    if (pharmacies.size() > 0) {
                        crearMarkers(pharmacies);
                    } else
                        Toast.makeText(getActivity(), R.string.error_message_results_not_found, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(JsonObject response, Exception e) {
                if (getActivity() == null || getActivity().isFinishing() || !isAdded()) return;
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
        JsonObject farmacia = buscarFarmaciaPorMarker(marker);
        if (farmacia == null) return false;
        DialogHelper.showPharmacyDetails(getActivity(), farmacia);
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
        getActivity().setTitle(R.string.action_bar_title_buscar_producto);
        if (getActivity() instanceof ActivityUpdateBackToolbar) {
            ((ActivityUpdateBackToolbar) getActivity()).showBackNavigationIcon();
        }
    }
}
