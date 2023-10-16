package com.orugga.yapp.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.livefront.bridge.Bridge;
import com.orugga.yapp.R;
import com.orugga.yapp.adapters.ResultPharmaciesListAdapter;
import com.orugga.yapp.objects.MapPointDistance;

import java.util.ArrayList;

import icepick.State;

import static com.orugga.yapp.Constants.ApiFields.PHARMACY_STATUS;
import static com.orugga.yapp.Constants.FragmentsNames.FRAGMENT_DETALLE_RESULTADOS_BUSQUEDA_PRODUCTO;
import static com.orugga.yapp.helpers.MapHelper.sortPharmaciesByDistances;

/**
 * A simple {@link Fragment} subclass.
 */
public class MasCercanasFragment extends Fragment {

    @State
    String mPharmaciesString;
    private JsonArray mPharmacies;
    @State
    double latitude;
    @State
    double longitude;
    private LatLng mLocation;

    private ListView mListView;

    private ResultPharmaciesListAdapter mResultPharmaciesAdapter;
    private ResultPharmaciesListAdapter mResultPharmaciesOnlyOpenAdapter;

    private MapPointDistance[] mMapPointDistances;

    public MasCercanasFragment() {
        // Required empty public constructor
    }

    public static MasCercanasFragment newInstance(JsonArray pharmacies, LatLng location) {
        MasCercanasFragment fragment = new MasCercanasFragment();
        fragment.mPharmaciesString = pharmacies.toString();
        fragment.latitude = location.latitude;
        fragment.longitude = location.longitude;
//        Bundle args = new Bundle();
//        args.putString("pharmacies", pharmacies.toString());
//        args.putDouble("lat", location.latitude);
//        args.putDouble("long", location.longitude);
//        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mPharmacies = new JsonParser().parse(getArguments().getString("pharmacies")).getAsJsonArray();
//            mLocation = new LatLng(getArguments().getDouble("lat"), getArguments().getDouble("long"));
//        }
        Bridge.restoreInstanceState(this, savedInstanceState);
        mPharmacies = new JsonParser().parse(mPharmaciesString).getAsJsonArray();
        mLocation = new LatLng(latitude, longitude);
        setRetainInstance(true);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Bridge.saveInstanceState(this, outState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mListView = (ListView) inflater.inflate(R.layout.simple_list_view, container, false);
        mResultPharmaciesAdapter = new ResultPharmaciesListAdapter(getContext(), R.layout.product_search_pharmacy_item, getActivity().getSupportFragmentManager());
        mMapPointDistances = sortPharmaciesByDistances(mLocation, mPharmacies);
        for (int i = 0; i < mPharmacies.size(); i++) {
            mResultPharmaciesAdapter.add(mMapPointDistances[i].getJsonObject());
        }
        mListView.setAdapter(mResultPharmaciesAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .add(R.id.coordinatorLayout, DetallesResultadoBusquedaProductoFragment.newInstance(mResultPharmaciesAdapter.getItem(position).getAsJsonObject()))
                        .addToBackStack(FRAGMENT_DETALLE_RESULTADOS_BUSQUEDA_PRODUCTO).commitAllowingStateLoss();
            }
        });
        return mListView;
    }

    public void showOnlyOpenPharmacies(boolean showOnlyOpen){
        if (showOnlyOpen){
            if (mResultPharmaciesOnlyOpenAdapter == null){
                mResultPharmaciesOnlyOpenAdapter = new ResultPharmaciesListAdapter(getContext(), R.layout.product_search_pharmacy_item, getActivity().getSupportFragmentManager());
                mResultPharmaciesOnlyOpenAdapter.addAll(createOpenPharmacyArray());
            }
            mListView.setAdapter(mResultPharmaciesOnlyOpenAdapter);
        } else {
            mListView.setAdapter(mResultPharmaciesAdapter);
        }
    }

    private ArrayList<JsonObject> createOpenPharmacyArray() {
        ArrayList<JsonObject> openPharmacies = new ArrayList<>();
        for (MapPointDistance mMapPointDistance : mMapPointDistances) {
            JsonObject pharmacy = mMapPointDistance.getJsonObject();
            String pharmacyStatus = pharmacy.get(PHARMACY_STATUS).getAsString().toLowerCase();
            if (pharmacyStatus.equals("open") || pharmacyStatus.equals("closing")) {
                openPharmacies.add(pharmacy);
            }
        }
        return openPharmacies;
    }

}
