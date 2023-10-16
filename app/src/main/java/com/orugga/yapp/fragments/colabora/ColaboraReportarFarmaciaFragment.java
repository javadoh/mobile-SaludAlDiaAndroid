package com.orugga.yapp.fragments.colabora;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonObject;
import com.orugga.yapp.AlarmBroadcastReceiver;
import com.orugga.yapp.LoginActivity;
import com.orugga.yapp.R;
import com.orugga.yapp.database.RecordatorioToma;
import com.orugga.yapp.database.RecordatorioVisita;
import com.orugga.yapp.fragments.BuscarFarmaciaFragment;
import com.orugga.yapp.helpers.IdleHelper;
import com.orugga.yapp.helpers.SessionHelper;
import com.orugga.yapp.interfaces.JsonObjectResponse;
import com.orugga.yapp.interfaces.OnFragmentUpdateToolbarIntefrace;
import com.orugga.yapp.interfaces.PharmacyCallback;
import com.orugga.yapp.requests.ReportesRequests;

import static com.orugga.yapp.Constants.ApiFields.PHARMACY_ID;
import static com.orugga.yapp.Constants.FragmentsNames.FRAGMENT_BUSCAR_FARMACIA;
import static com.orugga.yapp.Constants.FragmentsNames.FRAGMENT_COLABORA_THANKS;
import static com.orugga.yapp.Constants.IntentFilters.ACTION_SELECT_LOCATION;
import static com.orugga.yapp.Constants.IntentFilters.ACTION_SELECT_PHARMACY;

/**
 * A simple {@link Fragment} subclass.
 */
@SuppressWarnings("ConstantConditions")
public class ColaboraReportarFarmaciaFragment extends Fragment implements OnFragmentUpdateToolbarIntefrace {

    private int btnIndex;

    private AutoCompleteTextView mCadenaFarmaciaView;
    private EditText mCorreccionView;
    private EditText mInformacionFarmaciaView;
    private ConstraintLayout mUbicacionSelectorLayout;
    private LatLng mSelectedLocation;
    private ConstraintLayout mPharmacySelectorLayout;
    private JsonObject mSelectedPharmacy;
    private ConstraintLayout mSpinnerRazonLayout;
    private ConstraintLayout mSpinnerEstrellasLayout;

    private static final String CATEGORY = "3";

    public ColaboraReportarFarmaciaFragment() {
        // Required empty public constructor
    }

    public static ColaboraReportarFarmaciaFragment newInstance(int btnIndex) {
        Bundle args = new Bundle();
        ColaboraReportarFarmaciaFragment fragment = new ColaboraReportarFarmaciaFragment();
        args.putInt("btn_index", btnIndex);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            btnIndex = getArguments().getInt("btn_index");
        }
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_colabora_reportar_farmacia, container, false);

        mCadenaFarmaciaView = view.findViewById(R.id.txtCadenaFarmacia);
        mCorreccionView = view.findViewById(R.id.txtCorreccion);
        mInformacionFarmaciaView = view.findViewById(R.id.txtInformacionFarmacia);
        mUbicacionSelectorLayout = view.findViewById(R.id.ubicacionSelector);
        mPharmacySelectorLayout = view.findViewById(R.id.pharmacySelector);
        mSpinnerEstrellasLayout = view.findViewById(R.id.spinnerEstrellasLayout);
        mSpinnerRazonLayout = view.findViewById(R.id.spinnerRazonLayout);
        TextView mBtnOk = view.findViewById(R.id.btnOk);


        mBtnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSend();
            }
        });

        switch (btnIndex) {
            case 0:
                mPharmacySelectorLayout.setVisibility(View.VISIBLE);
                mCorreccionView.setVisibility(View.VISIBLE);
                mPharmacySelectorLayout.findViewById(R.id.btnSearchPharmacy).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openPharmacySelector();
                    }
                });
                break;
            case 1:
                mCadenaFarmaciaView.setVisibility(View.VISIBLE);
                mInformacionFarmaciaView.setVisibility(View.VISIBLE);
                mUbicacionSelectorLayout.setVisibility(View.VISIBLE);
                mUbicacionSelectorLayout.findViewById(R.id.btnSearchLocation).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openLocationSelector();
                    }
                });
                break;
            case 2:
                mPharmacySelectorLayout.setVisibility(View.VISIBLE);
                mSpinnerRazonLayout.setVisibility(View.VISIBLE);
                ((Spinner) mSpinnerRazonLayout.findViewById(R.id.spinnerRazon)).setAdapter(
                        new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, new String[]{getString(R.string.closed), getString(R.string.no_exist_more)}));
                mPharmacySelectorLayout.findViewById(R.id.btnSearchPharmacy).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openPharmacySelector();
                    }
                });
                break;
            case 3:
                mPharmacySelectorLayout.setVisibility(View.VISIBLE);
                mSpinnerEstrellasLayout.setVisibility(View.VISIBLE);
                ((Spinner) mSpinnerEstrellasLayout.findViewById(R.id.spinnerEstrellas)).setAdapter(
                        new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, new Integer[]{1, 2, 3, 4, 5}));
                mPharmacySelectorLayout.findViewById(R.id.btnSearchPharmacy).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openPharmacySelector();
                    }
                });
                break;
        }

        return view;
    }

    private void openPharmacySelector() {
        IdleHelper.ocultarTeclado(getActivity());
        final TextView pharmacyName = mPharmacySelectorLayout.findViewById(R.id.txtPharmacyName);
        pharmacyName.setText("");
        mSelectedPharmacy = null;
        mPharmacySelectorLayout.findViewById(R.id.checkView).setVisibility(View.GONE);
        getActivity().getSupportFragmentManager().beginTransaction()
                .add(R.id.coordinatorLayout, BuscarFarmaciaFragment.newInstance(ACTION_SELECT_PHARMACY, new PharmacyCallback() {
                    @Override
                    public void onGetLocation(LatLng location) {
                    }

                    @Override
                    public void onGetPharmacy(JsonObject pharmacy) {
                        if (pharmacy == null) return;
                        pharmacyName.setText(pharmacy.get("name").getAsString());
                        mSelectedPharmacy = pharmacy;
                        mPharmacySelectorLayout.findViewById(R.id.checkView).setVisibility(View.VISIBLE);
                    }
                })).addToBackStack(FRAGMENT_BUSCAR_FARMACIA).commitAllowingStateLoss();
    }

    private void openLocationSelector() {
        IdleHelper.ocultarTeclado(getActivity());
        final TextView selectedLatitude = mUbicacionSelectorLayout.findViewById(R.id.txtLatSelected);
        final TextView selectedLongitude = mUbicacionSelectorLayout.findViewById(R.id.txtLongSelected);
        selectedLatitude.setText("");
        selectedLongitude.setText("");
        mSelectedLocation = null;
        mUbicacionSelectorLayout.findViewById(R.id.checkView).setVisibility(View.GONE);
        getActivity().getSupportFragmentManager().beginTransaction()
                .add(R.id.coordinatorLayout, BuscarFarmaciaFragment.newInstance(ACTION_SELECT_LOCATION, new PharmacyCallback() {
                    @Override
                    public void onGetLocation(LatLng location) {
                        if (location == null) return;
                        selectedLatitude.setText(String.format("lat: %s", location.latitude));
                        selectedLongitude.setText(String.format("long: %s", location.longitude));
                        mSelectedLocation = location;
                        mUbicacionSelectorLayout.findViewById(R.id.checkView).setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onGetPharmacy(JsonObject pharmacy) {
                    }
                })).addToBackStack(FRAGMENT_BUSCAR_FARMACIA).commitAllowingStateLoss();
    }


    private void attemptSend() {
        boolean cancel = false;
        String error = "";
        View focusView = null;
        switch (btnIndex) {
            case 0:
                if (mCorreccionView.getText().toString().isEmpty()) {
                    focusView = mCorreccionView;
                    error = getString(R.string.error_field_required);
                    cancel = true;
                }
                if (mSelectedPharmacy == null) {
                    cancel = true;
                    focusView = null;
                    error = getString(R.string.error_select_pharmacy);
                }
                break;
            case 1:
                if (mSelectedLocation == null) {
                    focusView = null;
                    error = getString(R.string.error_select_a_location);
                    cancel = true;
                }
                if (mInformacionFarmaciaView.getText().toString().isEmpty()) {
                    focusView = mInformacionFarmaciaView;
                    error = getString(R.string.error_field_required);
                    cancel = true;
                }
                if (mCadenaFarmaciaView.getText().toString().isEmpty()) {
                    focusView = mCadenaFarmaciaView;
                    error = getString(R.string.error_field_required);
                    cancel = true;
                }
                break;
            case 2:
                if (mSelectedPharmacy == null) {
                    focusView = null;
                    error = getString(R.string.error_select_pharmacy);
                    cancel = true;
                }
                break;
            case 3:
                if (mSelectedPharmacy == null) {
                    focusView = null;
                    error = getString(R.string.error_select_pharmacy);
                    cancel = true;
                }
                break;
        }

        if (!cancel) {
            enviarFormulario();
        } else {
            if (focusView == null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            } else {
                focusView.requestFocus();
                ((TextView) focusView).setError(error);
            }
        }
    }

    private void enviarFormulario() {
        IdleHelper.ocultarTeclado(getActivity());
        final ProgressDialog dialog = ProgressDialog.show(getContext(), "", getString(R.string.loading));
        String pharmacyId = mSelectedPharmacy != null ? mSelectedPharmacy.get(PHARMACY_ID).getAsString() : "";
        String latitude = mSelectedLocation != null ? String.valueOf(mSelectedLocation.latitude) : "";
        String longitude = mSelectedLocation != null ? String.valueOf(mSelectedLocation.longitude) : "";
        String closed = btnIndex == 2 ? (String) ((Spinner) mSpinnerRazonLayout.findViewById(R.id.spinnerRazon)).getSelectedItem() : "";
        String star = btnIndex == 3 ? String.valueOf(((Spinner) mSpinnerEstrellasLayout.findViewById(R.id.spinnerEstrellas)).getSelectedItem()) : "";
        ReportesRequests.sendReporteFarmacia(getContext(), SessionHelper.getAccessToken(getContext()), CATEGORY, String.valueOf(btnIndex + 1),
                pharmacyId, mInformacionFarmaciaView.getText().toString(), mCadenaFarmaciaView.getText().toString(), closed, star, latitude, longitude, new JsonObjectResponse() {
                    @Override
                    public void onSuccess(JsonObject response) {
                        if (getActivity() == null || getActivity().isFinishing()) return;
                        dialog.dismiss();
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .add(R.id.coordinatorLayout, ColaboraThanksFragment.newInstance())
                                .addToBackStack(FRAGMENT_COLABORA_THANKS).commitAllowingStateLoss();
                        Answers.getInstance().logCustom(new CustomEvent("Envio Reporte Farmacia").putCustomAttribute("subCategoria", btnIndex));
                    }

                    @Override
                    public void onError(JsonObject response, Exception e) {
                        if (getActivity() == null || getActivity().isFinishing()) return;
                        dialog.dismiss();
                        if (e != null) {
                            Crashlytics.logException(e);
                        } else if (response.get("rta") != null && !response.get("rta").isJsonNull() &&
                                response.get("data") != null && response.get("data").isJsonObject() &&
                                response.getAsJsonObject("data").get("error") != null && !response.getAsJsonObject("data").get("error").isJsonNull() &&
                                response.getAsJsonObject("data").get("error").getAsString().equals("Unauthorised")) {
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
    public void onFragmentUpdateToolbar() {
        getActivity().setTitle(getString(R.string.action_bar_title_colabora_farmacias));
    }
}
