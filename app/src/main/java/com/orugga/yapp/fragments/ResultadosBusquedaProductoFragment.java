package com.orugga.yapp.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.livefront.bridge.Bridge;
import com.orugga.yapp.R;
import com.orugga.yapp.adapters.FragmentProductPagerAdapter;
import com.orugga.yapp.interfaces.ActivityUpdateBackToolbar;

import java.lang.reflect.Field;

import icepick.State;

import static com.orugga.yapp.Constants.FragmentsNames.FRAGMENT_BUSCAR_FARMACIA;
import static com.orugga.yapp.Constants.IntentFilters.ACTION_SEE_RESULTS;

/**
 * A simple {@link Fragment} subclass.
 */
public class ResultadosBusquedaProductoFragment extends Fragment
        implements com.orugga.yapp.interfaces.OnFragmentUpdateToolbarIntefrace {


    @State
    String mPharmaciesString, mProgramaPacientesString, mProductString;
    @State
    double latitude, longitude;
    @State
    boolean withoutPP;
    private TabLayout mTabs;
    private JsonArray mProgramaPacientes;
    private JsonArray mPharmacies;
    private LatLng mLocation;

    private PrecioFarmaciasFragment mPrecioFarmaciasFragment;

    public ResultadosBusquedaProductoFragment() {
        // Required empty public constructor
    }

    public static ResultadosBusquedaProductoFragment newInstance(JsonArray pharmacies, JsonArray pps, LatLng location, boolean withoutPP, @Nullable JsonObject product) {
        ResultadosBusquedaProductoFragment fragment = new ResultadosBusquedaProductoFragment();
        fragment.mPharmaciesString = pharmacies.toString();
        fragment.mProgramaPacientesString = pps.toString();
        fragment.latitude = location.latitude;
        fragment.longitude = location.longitude;
        fragment.withoutPP = withoutPP;
        if (product != null)
            fragment.mProductString = product.toString();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Bridge.restoreInstanceState(this, savedInstanceState);
        mPharmacies = new JsonParser().parse(mPharmaciesString).getAsJsonArray();
        mProgramaPacientes = new JsonParser().parse(mProgramaPacientesString).getAsJsonArray();
        mLocation = new LatLng(latitude, longitude);
        setRetainInstance(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Bridge.saveInstanceState(this, outState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_resultados_busqueda_producto, container, false);
        mTabs = view.findViewById(R.id.resultProductTabLayout);
        ViewPager viewPager = view.findViewById(R.id.resultProductViewPager);
//        mMasCercanasFragment = MasCercanasFragment.newInstance(mPharmacies, mLocation);
        mPrecioFarmaciasFragment = PrecioFarmaciasFragment.newInstance(mPharmacies);
        ProgramasGesYPacienteFragment mProgramasgesYPacienteFragment = ProgramasGesYPacienteFragment.newInstance(mProgramaPacientes, withoutPP, mProductString, mLocation.latitude, mLocation.longitude);
        FragmentProductPagerAdapter pagerAdapter = new FragmentProductPagerAdapter(getChildFragmentManager(),
                new Fragment[]{
                        mPrecioFarmaciasFragment,
                        mProgramasgesYPacienteFragment
//                        mMasCercanasFragment
                });
        mTabs.setupWithViewPager(viewPager);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(2);
        setUpTabTitles();
        view.findViewById(R.id.btnVerTodosEnMapa).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPharmacies.size() == 0) {
                    Toast.makeText(getContext(), R.string.toast_response_no_tienes_farmacias_cercanas, Toast.LENGTH_SHORT).show();
                    return;
                }
                getActivity().getSupportFragmentManager().beginTransaction()
                        .add(R.id.coordinatorLayout, BuscarFarmaciaFragment.newInstance(mPharmacies, mLocation, ACTION_SEE_RESULTS))
                        .addToBackStack(FRAGMENT_BUSCAR_FARMACIA).commitAllowingStateLoss();
            }
        });
        ((CheckBox) view.findViewById(R.id.checkboxSoloFarmaciasAbiertas)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mPrecioFarmaciasFragment.showOnlyOpenPharmacies(true);
                } else {
                    mPrecioFarmaciasFragment.showOnlyOpenPharmacies(false);
                }
            }
        });

        return view;
    }


    @SuppressWarnings("ConstantConditions")
    private void setUpTabTitles() {
        mTabs.getTabAt(0).setText(getText(R.string.resultados_busqueda_producto_precio_farmacias));
        mTabs.getTabAt(1).setText(getText(R.string.resultados_busqueda_producto_programa_ges));
    }

    @Override
    public void onDetach() {
        super.onDetach();
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
    public void onFragmentUpdateToolbar() {
        getActivity().setTitle(getString(R.string.action_bar_title_resultado_busqueda_producto));
        if (getActivity() instanceof ActivityUpdateBackToolbar) {
            ((ActivityUpdateBackToolbar) getActivity()).showBackNavigationIcon();
        }
    }
}
