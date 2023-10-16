package com.orugga.yapp.fragments.colabora;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.google.gson.JsonObject;
import com.orugga.yapp.AlarmBroadcastReceiver;
import com.orugga.yapp.LoginActivity;
import com.orugga.yapp.R;
import com.orugga.yapp.database.RecordatorioToma;
import com.orugga.yapp.database.RecordatorioVisita;
import com.orugga.yapp.helpers.IdleHelper;
import com.orugga.yapp.helpers.SessionHelper;
import com.orugga.yapp.interfaces.JsonObjectResponse;
import com.orugga.yapp.interfaces.OnFragmentUpdateToolbarIntefrace;
import com.orugga.yapp.requests.ReportesRequests;

import static com.orugga.yapp.Constants.FragmentsNames.FRAGMENT_COLABORA_THANKS;

/**
 * A simple {@link Fragment} subclass.
 */
public class ColaboraReportarOtroFragment extends Fragment implements OnFragmentUpdateToolbarIntefrace {

    private static final String CATEGORY = "6";
    private EditText mCorreoContactoView;
    private EditText mAsuntoView;
    private EditText mComentariosView;
    private TextView mBtnOk;

    public ColaboraReportarOtroFragment() {
        // Required empty public constructor
    }


    public static ColaboraReportarOtroFragment newInstance() {
        return new ColaboraReportarOtroFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_colabora_reportar_otro, container, false);


        mCorreoContactoView = view.findViewById(R.id.txtCorreo);
        mAsuntoView = view.findViewById(R.id.txtAsunto);
        mComentariosView = view.findViewById(R.id.txtComentario);
        mBtnOk = view.findViewById(R.id.btnOk);


        mBtnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSend();
            }
        });

        return view;
    }


    private void attemptSend() {
        boolean cancel = false;
        String error = "";
        View focusView = null;

        if (mComentariosView.getText().toString().isEmpty()) {
            cancel = true;
            focusView = mComentariosView;
            error = getString(R.string.error_field_required);
        }

        if (mAsuntoView.getText().toString().isEmpty()) {
            cancel = true;
            focusView = mAsuntoView;
            error = getString(R.string.error_field_required);
        }

        if (mCorreoContactoView.getText().toString().isEmpty()) {
            cancel = true;
            focusView = mCorreoContactoView;
            error = getString(R.string.error_field_required);
        } else if (!isEmailValid(mCorreoContactoView.getText().toString())) {
            cancel = true;
            focusView = mCorreoContactoView;
            error = getString(R.string.error_invalid_email);
        }

        if (!cancel) {
            enviarFormulario();
        } else {
            if (focusView == null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            } else {
                focusView.requestFocus();
                if (focusView instanceof TextView) {
                    ((TextView) focusView).setError(error);
                } else {
                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void enviarFormulario() {
        IdleHelper.ocultarTeclado(getActivity());
        final ProgressDialog dialog = ProgressDialog.show(getContext(), "", getString(R.string.loading));
        ReportesRequests.sendReporteOtros(getContext(), SessionHelper.getAccessToken(getContext()), CATEGORY, "1",
                mAsuntoView.getText().toString(), mCorreoContactoView.getText().toString(), mComentariosView.getText().toString(), new JsonObjectResponse() {
                    @Override
                    public void onSuccess(JsonObject response) {
                        if (getActivity() == null || getActivity().isFinishing()) return;
                        dialog.dismiss();
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .add(R.id.coordinatorLayout, ColaboraThanksFragment.newInstance())
                                .addToBackStack(FRAGMENT_COLABORA_THANKS).commitAllowingStateLoss();
                        Answers.getInstance().logCustom(new CustomEvent("Envio Reporte Otros").putCustomAttribute("subCategoria", "1"));
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

    private boolean isEmailValid(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }


    @Override
    public void onFragmentUpdateToolbar() {
        getActivity().setTitle(getString(R.string.action_bar_title_colabora_otro));
    }
}
