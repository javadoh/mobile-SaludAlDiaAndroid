package com.orugga.yapp.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.livefront.bridge.Bridge;
import com.orugga.yapp.R;
import com.orugga.yapp.adapters.ResultPharmaciesListAdapter;

import java.util.ArrayList;

import icepick.State;

import static com.orugga.yapp.Constants.ApiFields.PHARMACY_STATUS;
import static com.orugga.yapp.Constants.FragmentsNames.FRAGMENT_DETALLE_RESULTADOS_BUSQUEDA_PRODUCTO;

public class PrecioFarmaciasFragment extends Fragment {

    private ListView mListView;
    @State String mPharmaciesString;
    private JsonArray mPharmacies;

    private ResultPharmaciesListAdapter mResultPharmaciesAdapter;
    private ResultPharmaciesListAdapter mResultPharmaciesOnlyOpenAdapter;


    public PrecioFarmaciasFragment() {
        // Required empty public constructor
    }

    public static PrecioFarmaciasFragment newInstance(@NonNull JsonArray pharmacies) {
        PrecioFarmaciasFragment fragment = new PrecioFarmaciasFragment();
//        Bundle args = new Bundle();
//        args.putString("pharmacies", pharmacies.toString());
//        fragment.setArguments(args);
        fragment.mPharmaciesString = pharmacies.toString();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mPharmacies = new JsonParser().parse(getArguments().getString("pharmacies")).getAsJsonArray();
//        }
        Bridge.restoreInstanceState(this, savedInstanceState);
        setRetainInstance(true);
        mPharmacies = new JsonParser().parse(mPharmaciesString).getAsJsonArray();
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
        for (int i = 0; i < mPharmacies.size(); i++) {
            mResultPharmaciesAdapter.add(mPharmacies.get(i).getAsJsonObject());
        }
        mListView.setAdapter(mResultPharmaciesAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Answers.getInstance().logCustom(new CustomEvent("Resultados Yapp - Ver detalle"));
                getActivity().getSupportFragmentManager().beginTransaction()
                        .add(R.id.coordinatorLayout, DetallesResultadoBusquedaProductoFragment.newInstance(mResultPharmaciesAdapter.getItem(position).getAsJsonObject()))
                        .addToBackStack(FRAGMENT_DETALLE_RESULTADOS_BUSQUEDA_PRODUCTO).commitAllowingStateLoss();
            }
        });
        return mListView;
    }

    public void showOnlyOpenPharmacies(boolean showOnlyOpen) {
        if (getActivity() == null || getActivity().isFinishing()) return;
        if (showOnlyOpen) {
            if (mResultPharmaciesOnlyOpenAdapter == null) {
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
        for (int i = 0; i < mPharmacies.size(); i++) {
            JsonObject pharmacy = mPharmacies.get(i).getAsJsonObject();
            String pharmacyStatus = pharmacy.get(PHARMACY_STATUS).getAsString().toLowerCase();
            if (pharmacyStatus.equals("open") || pharmacyStatus.equals("closing")) {
                openPharmacies.add(pharmacy);
            }
        }
        return openPharmacies;
    }


}
