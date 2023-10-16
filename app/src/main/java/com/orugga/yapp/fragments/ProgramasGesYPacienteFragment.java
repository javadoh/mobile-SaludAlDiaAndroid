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
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.livefront.bridge.Bridge;
import com.orugga.yapp.BuildConfig;
import com.orugga.yapp.R;
import com.orugga.yapp.adapters.PacientProgramListAdapter;
import com.orugga.yapp.helpers.SessionHelper;
import com.orugga.yapp.interfaces.JsonObjectResponse;
import com.orugga.yapp.requests.LogsRequests;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import icepick.State;

import static com.orugga.yapp.Constants.FragmentsNames.FRAGMENT_DETALLE_PROGRAMA_PACIENTE;
import static com.orugga.yapp.helpers.IdleHelper.ocultarTeclado;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProgramasGesYPacienteFragment extends Fragment {

    @State
    String programaPacientes, mProductString;
    @State
    boolean withoutPP;
    @State
    double lat, lon;
    private JsonObject mProduct;
    private LatLng mLocation;
    private JsonArray mProgramaPacientes;

    public ProgramasGesYPacienteFragment() {
        // Required empty public constructor
    }

    public static ProgramasGesYPacienteFragment newInstance(JsonArray programaPacientes, boolean withoutPP, String productString, double lat, double lon) {
        ProgramasGesYPacienteFragment fragment = new ProgramasGesYPacienteFragment();
        fragment.programaPacientes = programaPacientes.toString();
        fragment.lat = lat;
        fragment.lon = lon;
        fragment.withoutPP = withoutPP;
        fragment.mProductString = productString;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bridge.restoreInstanceState(this, savedInstanceState);
        setRetainInstance(true);
        mProgramaPacientes = new JsonParser().parse(programaPacientes).getAsJsonArray();
        mLocation = new LatLng(this.lat, this.lon);
        if (mProductString != null)
            mProduct = new JsonParser().parse(mProductString).getAsJsonObject();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Bridge.saveInstanceState(this, outState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_programas_ges_ypaciente, container, false);

        if (withoutPP) {
            ((TextView) view.findViewById(R.id.txt_conocer_precios_PP)).setText(R.string.error_sin_programa_ges);
        } else if (mProgramaPacientes.size() != 0) {
            Answers.getInstance().logCustom(new CustomEvent("Resultados Yapp - Ver detalle"));
            view.findViewById(R.id.txt_conocer_precios_PP).setVisibility(View.GONE);
            ListView listView = view.findViewById(R.id.listView);
            final PacientProgramListAdapter adapter = new PacientProgramListAdapter(getContext(), mProgramaPacientes);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ocultarTeclado(getActivity());
                    String myFormat = "dd/MM/yyyy HH:mm"; //In which you need put here
                    SimpleDateFormat format = new SimpleDateFormat(myFormat, Locale.getDefault());
                    String date = format.format(Calendar.getInstance().getTime());

                    JsonObject user = SessionHelper.getUser(getContext());
                    JsonObject programaPaciente = adapter.getItem(position);
                    LogsRequests.logPacientProgramDetail(getContext(), date, mLocation.latitude, mLocation.longitude,
                            programaPaciente != null ? programaPaciente.get("id").getAsLong() : -1,
                            mProduct != null ? mProduct.get("id").getAsLong() : -1,
                            user != null ? user.get("id").getAsLong() : -1,
                            new JsonObjectResponse() {
                                @Override
                                public void onSuccess(JsonObject response) {
                                    if (getContext() == null) return;
                                    if (BuildConfig.DEBUG)
                                        Toast.makeText(getContext(), "Log enviado", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onError(JsonObject response, Exception e) {
                                    if (getContext() == null) return;
                                    if (BuildConfig.DEBUG)
                                        Toast.makeText(getContext(), "Log no enviado", Toast.LENGTH_SHORT).show();
                                }
                            });
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .add(R.id.coordinatorLayout, PacientProgramDetailsFragment.newInstance(programaPaciente))
                            .addToBackStack(FRAGMENT_DETALLE_PROGRAMA_PACIENTE).commitAllowingStateLoss();
                }
            });
            listView.setAdapter(adapter);
        }

        return view;
    }

}
