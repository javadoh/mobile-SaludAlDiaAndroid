package com.orugga.yapp.fragments.colabora;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.orugga.yapp.AlarmBroadcastReceiver;
import com.orugga.yapp.LoginActivity;
import com.orugga.yapp.R;
import com.orugga.yapp.adapters.AutoCompleteConvenioAdapter;
import com.orugga.yapp.database.RecordatorioToma;
import com.orugga.yapp.database.RecordatorioVisita;
import com.orugga.yapp.helpers.IdleHelper;
import com.orugga.yapp.helpers.SessionHelper;
import com.orugga.yapp.interfaces.JsonObjectResponse;
import com.orugga.yapp.interfaces.OnFragmentUpdateToolbarIntefrace;
import com.orugga.yapp.requests.GetRegisterDataRequest;
import com.orugga.yapp.requests.ReportesRequests;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.orugga.yapp.Constants.FragmentsNames.FRAGMENT_COLABORA_THANKS;
import static com.orugga.yapp.Constants.IntentFilters.REQUEST_IMAGE_CAPTURE;
import static com.orugga.yapp.Constants.IntentFilters.REQUEST_SELECT_IMAGE;
import static com.orugga.yapp.Constants.RequestPermission.REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE;
import static com.orugga.yapp.helpers.FileChooser.getPath;

/**
 * A simple {@link Fragment} subclass.
 */
public class ColaboraReportarConveniosFragment extends Fragment implements OnFragmentUpdateToolbarIntefrace {

    private static final String CATEGORY = "4";
    private int btnIndex;
    private AutoCompleteTextView mConvenioView;
    private EditText mCorreccionView;
    private EditText mDescripcionView;
    private EditText mSitioWebView;
    private ConstraintLayout mPhotoSelectorLayout;
    private File mFoto;
    private JsonArray mConvenios;
    private AutoCompleteConvenioAdapter mConvenioAdapter;

    public ColaboraReportarConveniosFragment() {
        // Required empty public constructor
    }

    public static ColaboraReportarConveniosFragment newInstance(int btnIndex) {
        Bundle args = new Bundle();
        ColaboraReportarConveniosFragment fragment = new ColaboraReportarConveniosFragment();
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
        View view = inflater.inflate(R.layout.fragment_colabora_reportar_convenios, container, false);

        mConvenioView = view.findViewById(R.id.txtConvenio);
        mCorreccionView = view.findViewById(R.id.txtCorreccion);
        mDescripcionView = view.findViewById(R.id.txtDescripcion);
        mPhotoSelectorLayout = view.findViewById(R.id.photoSelector);
        mSitioWebView = view.findViewById(R.id.txtSitioWeb);
        TextView mBtnOk = view.findViewById(R.id.btnOk);

        mPhotoSelectorLayout.findViewById(R.id.btnSelectPhoto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!runtime_permissions()) {
                    mFoto = null;
                    ((TextView) mPhotoSelectorLayout.findViewById(R.id.txtPhotoSelectedName)).setText("");
                    mPhotoSelectorLayout.findViewById(R.id.checkView).setVisibility(View.GONE);
                    selectPhoto();
                }
            }
        });

        mBtnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSend();
            }
        });

        switch (btnIndex) {
            case 0:
                final ProgressDialog progressDialog = ProgressDialog.show(getContext(), "", getString(R.string.loading));
                GetRegisterDataRequest.getRegisterData(getActivity(), new JsonObjectResponse() {
                    @Override
                    public void onSuccess(JsonObject response) {
                        if (getActivity() == null || getActivity().isFinishing()) return;
                        progressDialog.dismiss();
                        mConvenios = response.getAsJsonArray("deals");
                        mConvenioAdapter = new AutoCompleteConvenioAdapter(getContext(), toList(mConvenios));
                        mConvenioView.setAdapter(mConvenioAdapter);
                    }

                    @Override
                    public void onError(JsonObject response, Exception e) {
                        if (getActivity() == null || getActivity().isFinishing()) return;
                        progressDialog.dismiss();
                        if (e != null) {
                            Crashlytics.logException(e);
                        }
                        Toast.makeText(getContext(), R.string.error_message_internet_conection, Toast.LENGTH_SHORT).show();
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                });
                mCorreccionView.setVisibility(View.VISIBLE);

                break;
            case 1:
                mDescripcionView.setVisibility(View.VISIBLE);
                mSitioWebView.setVisibility(View.VISIBLE);
                mPhotoSelectorLayout.setVisibility(View.VISIBLE);
                ((TextView) mPhotoSelectorLayout.findViewById(R.id.txtPhotoDesc)).setText(R.string.sube_foto_beneficio);
                break;
        }

        return view;
    }


    private List<String> toList(JsonArray convenios) {
        List<String> responseList = new ArrayList<>();

        for (int i = 0; i < convenios.size(); i++) {
            final JsonObject convenio = convenios.get(i).getAsJsonObject();
            String name = convenio.get("name").getAsString();
            responseList.add(name);
        }
        return responseList;
    }

    private void attemptSend() {
        boolean cancel = false;
        String error = "";
        View focusView = null;
        switch (btnIndex) {
            case 0:
                if (mCorreccionView.getText().toString().isEmpty()) {
                    cancel = true;
                    focusView = mCorreccionView;
                    error = getString(R.string.error_field_required);
                }
                if (mConvenioView.getText().toString().isEmpty()) {
                    focusView = mConvenioView;
                    error = getString(R.string.error_field_required);
                    cancel = true;
                }
                break;
            case 1:
                if (mDescripcionView.getText().toString().isEmpty()) {
                    focusView = mDescripcionView;
                    error = getString(R.string.error_field_required);
                    cancel = true;
                }
                if (mConvenioView.getText().toString().isEmpty()) {
                    focusView = mConvenioView;
                    error = getString(R.string.error_field_required);
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
        ReportesRequests.sendReporteConvenio(getContext(), SessionHelper.getAccessToken(getContext()), CATEGORY, String.valueOf(btnIndex + 1),
                mConvenioView.getText().toString(), mDescripcionView.getText().toString(), mSitioWebView.getText().toString(), mFoto, new JsonObjectResponse() {
                    @Override
                    public void onSuccess(JsonObject response) {
                        if (getActivity() == null || getActivity().isFinishing()) return;
                        dialog.dismiss();
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .add(R.id.coordinatorLayout, ColaboraThanksFragment.newInstance())
                                .addToBackStack(FRAGMENT_COLABORA_THANKS).commitAllowingStateLoss();
                        Answers.getInstance().logCustom(new CustomEvent("Envio Reporte Convenio").putCustomAttribute("subCategoria", btnIndex));
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean error = false;
        if (resultCode == RESULT_OK) {
            File imageFile = null;
            String imageName = "";
            switch (requestCode) {
                case REQUEST_IMAGE_CAPTURE:
                    try {
                        File outputDir = getContext().getCacheDir(); // context being the Activity pointer
                        imageFile = File.createTempFile("tempImage", ".jpeg", outputDir);
                        imageName = imageFile.getName();
                    } catch (IOException e) {
                        Log.e("RegisterAccountFragment", e.getMessage());
                        error = true;
                    }
                    break;
                case REQUEST_SELECT_IMAGE:
                    Uri selectedImageUri = data.getData();
                    imageFile = new File(getPath(getContext(), selectedImageUri));
                    imageName = selectedImageUri.getLastPathSegment();
                    break;
            }
            mFoto = imageFile;
            ((TextView) mPhotoSelectorLayout.findViewById(R.id.txtPhotoSelectedName)).setText(imageName);
            mPhotoSelectorLayout.findViewById(R.id.checkView).setVisibility(View.VISIBLE);
            if (error)
                Toast.makeText(getContext(), "Error cargando imagen", Toast.LENGTH_SHORT).show();
        }
    }

    public void selectPhoto() {
        IdleHelper.ocultarTeclado(getActivity());
        final CharSequence[] items = {getString(R.string.take_photo), getString(R.string.choose_from_gallery), getString(R.string.cancel)};
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setTitle(R.string.add_photo);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals(getString(R.string.take_photo))) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                } else if (items[item].equals(getString(R.string.choose_from_gallery))) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, REQUEST_SELECT_IMAGE);
                } else if (items[item].equals(getString(R.string.cancel))) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }


    private boolean runtime_permissions() {
        if (Build.VERSION.SDK_INT >= 23 && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
            return true;
        }
        return false;
    }

    @Override
    public void onFragmentUpdateToolbar() {
        getActivity().setTitle(getString(R.string.action_bar_title_colabora_convenio));
    }
}
