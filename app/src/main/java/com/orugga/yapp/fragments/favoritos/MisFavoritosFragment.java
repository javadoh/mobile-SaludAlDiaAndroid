package com.orugga.yapp.fragments.favoritos;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.google.gson.JsonArray;
import com.orugga.yapp.R;
import com.orugga.yapp.interfaces.ActivityUpdateBackToolbar;
import com.orugga.yapp.interfaces.OnFragmentUpdateToolbarIntefrace;

import static com.orugga.yapp.Constants.FragmentsNames.FRAGMENT_MIS_FARMACIAS_FAVORITAS;
import static com.orugga.yapp.Constants.FragmentsNames.FRAGMENT_MIS_MEDICAMENTOS_FAVORITOS;

/**
 * A simple {@link Fragment} subclass.
 */
public class MisFavoritosFragment extends Fragment implements OnFragmentUpdateToolbarIntefrace{

    private JsonArray productosFavoritos;
    private JsonArray farmaciasFavoritas;

    public MisFavoritosFragment() {
        // Required empty public constructor
    }

    public static MisFavoritosFragment newInstance(JsonArray farmaciasFavoritas, JsonArray productosFavoritos) {
        MisFavoritosFragment fragment = new MisFavoritosFragment();
        fragment.farmaciasFavoritas = farmaciasFavoritas;
        fragment.productosFavoritos = productosFavoritos;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mis_favoritos, container, false);

        ((TextView) view.findViewById(R.id.cantFarmaciasFavoritas)).setText(new StringBuilder().append("(").append(farmaciasFavoritas.size()).append(" guardados)").toString());
        ((TextView) view.findViewById(R.id.cantMedicamentosFavoritos)).setText(new StringBuilder().append("(").append(productosFavoritos.size()).append(" guardados)").toString());

        view.findViewById(R.id.btnShowProducts).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Answers.getInstance().logCustom(new CustomEvent("Mis Favoritos - Ver Medicamentos"));
                getActivity().getSupportFragmentManager().beginTransaction()
                        .add(R.id.coordinatorLayout, MisMedicamentosFavFragment.newInstance(productosFavoritos))
                        .addToBackStack(FRAGMENT_MIS_MEDICAMENTOS_FAVORITOS).commitAllowingStateLoss();
            }
        });
        view.findViewById(R.id.btnShowPharmacies).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Answers.getInstance().logCustom(new CustomEvent("Mis Favoritos - Ver Farmacias"));
                getActivity().getSupportFragmentManager().beginTransaction()
                        .add(R.id.coordinatorLayout, MisFarmaciasFavFragment.newInstance(farmaciasFavoritas))
                        .addToBackStack(FRAGMENT_MIS_FARMACIAS_FAVORITAS).commitAllowingStateLoss();
            }
        });

        return view;
    }


    @Override
    public void onFragmentUpdateToolbar() {
        getActivity().setTitle(getString(R.string.action_bar_title_mis_favoritos));
        if (getActivity() instanceof ActivityUpdateBackToolbar){
            ((ActivityUpdateBackToolbar) getActivity()).showBackNavigationIcon();
        }
    }
}
