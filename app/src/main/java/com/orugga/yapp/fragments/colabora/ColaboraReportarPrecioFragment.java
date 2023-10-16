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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.orugga.yapp.AlarmBroadcastReceiver;
import com.orugga.yapp.LoginActivity;
import com.orugga.yapp.R;
import com.orugga.yapp.adapters.AutoCompleteBuscarProductosAdapter;
import com.orugga.yapp.database.RecordatorioToma;
import com.orugga.yapp.database.RecordatorioVisita;
import com.orugga.yapp.fragments.login.ProfileFragment;
import com.orugga.yapp.helpers.IdleHelper;
import com.orugga.yapp.helpers.SessionHelper;
import com.orugga.yapp.interfaces.JsonArrayResponse;
import com.orugga.yapp.interfaces.JsonObjectResponse;
import com.orugga.yapp.interfaces.OnFragmentUpdateToolbarIntefrace;
import com.orugga.yapp.requests.ReportesRequests;
import com.orugga.yapp.requests.SearchProductsRequest;

import java.io.File;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;
import static com.orugga.yapp.Constants.ApiFields.PRODUCT_NAME;
import static com.orugga.yapp.Constants.FragmentsNames.FRAGMENT_COLABORA_THANKS;
import static com.orugga.yapp.Constants.IntentFilters.REQUEST_IMAGE_CAPTURE;
import static com.orugga.yapp.Constants.IntentFilters.REQUEST_SELECT_IMAGE;
import static com.orugga.yapp.Constants.RequestPermission.REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE;
import static com.orugga.yapp.helpers.FileChooser.getPath;
import static com.orugga.yapp.helpers.IdleHelper.ocultarTeclado;
import static com.orugga.yapp.helpers.SessionHelper.getAccessToken;

/**
 * A simple {@link Fragment} subclass.
 */
public class ColaboraReportarPrecioFragment extends Fragment implements OnFragmentUpdateToolbarIntefrace {

    private static final String CATEGORY = "2";
    private int btnIndex;
    private JsonObject mResponse;
    private AutoCompleteTextView mNombreView;
    private AutoCompleteTextView mLaboratorioView;
    private EditText mGramajeView;
    private EditText mCantidadView;
    private AutoCompleteTextView mCadenaFarmaciaView;
    private EditText mPrecioPagadoView;
    private EditText mPrecioDescuentoView;
    private Spinner mConvenioView;
    private Spinner mIsapreView;
    private ConstraintLayout mPhotoSelectorLayout;
    private boolean selectingPhotoBoleta = false;
    private File mFotoBoleta;
    private ConstraintLayout mProductPhotoSelectorLayout;
    private boolean selectingPhotoProduct = false;
    private File mFotoProd;
    private ProfileFragment.RegisterSpinnerAdapter mConvenioAdapter;
    private ProfileFragment.RegisterSpinnerAdapter mIsapreAdapter;

    private AutoCompleteBuscarProductosAdapter mProductAdapter;
    private boolean isFromItemClick = false;
    private String productName = "";
    private TextWatcher mProductTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() >= 3 && s.length() < 256) {
                if (!isFromItemClick) {
                    productName = "";
                    buscarProducto(s.toString());
                } else {
                    isFromItemClick = false;
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };
    private AdapterView.OnItemClickListener mProductItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            JsonObject product = mProductAdapter.getItem(position);
            isFromItemClick = true;
            productName = product.get(PRODUCT_NAME).getAsString();
            mNombreView.setText(productName);
            ocultarTeclado(getActivity());
            mNombreView.clearFocus();
        }
    };

    public ColaboraReportarPrecioFragment() {
        // Required empty public constructor
    }

    public static ColaboraReportarPrecioFragment newInstance(int btnIndex, JsonObject response) {
        Bundle args = new Bundle();
        ColaboraReportarPrecioFragment fragment = new ColaboraReportarPrecioFragment();
        args.putInt("btn_index", btnIndex);
        args.putString("response", response.toString());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            btnIndex = getArguments().getInt("btn_index");
            mResponse = new JsonParser().parse(getArguments().getString("response")).getAsJsonObject();
        }
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_colabora_reportar_precio, container, false);

        mNombreView = view.findViewById(R.id.txtNombre);
        mLaboratorioView = view.findViewById(R.id.txtLaboratorio);
        mGramajeView = view.findViewById(R.id.txtGramaje);
        mCantidadView = view.findViewById(R.id.txtCantidad);
        mCadenaFarmaciaView = view.findViewById(R.id.txtCadenaFarmacia);
        mPrecioPagadoView = view.findViewById(R.id.txtPrecioPagado);
        mPrecioDescuentoView = view.findViewById(R.id.txtPrecioDescuento);
        mConvenioView = view.findViewById(R.id.spinnerConvenio);
        mIsapreView = view.findViewById(R.id.spinnerIsapre);
        mPhotoSelectorLayout = view.findViewById(R.id.photoSelector);
        mProductPhotoSelectorLayout = view.findViewById(R.id.productPhotoSelector);
        TextView mBtnOk = view.findViewById(R.id.btnOk);

        mPhotoSelectorLayout.findViewById(R.id.btnSelectPhoto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!runtime_permissions()) {
                    selectingPhotoBoleta = true;
                    selectPhoto();
                }
            }
        });

        mProductPhotoSelectorLayout.findViewById(R.id.btnSelectPhoto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!runtime_permissions()) {
                    selectingPhotoProduct = true;
                    selectPhoto();
                }
            }
        });


        switch (btnIndex) {
            case 0: {
                mCadenaFarmaciaView.setVisibility(View.VISIBLE);
                mPrecioPagadoView.setVisibility(View.VISIBLE);
                mConvenioView.setVisibility(View.VISIBLE);
                mIsapreView.setVisibility(View.VISIBLE);
                mPhotoSelectorLayout.setVisibility(View.VISIBLE);
                mProductAdapter = new AutoCompleteBuscarProductosAdapter(getContext());
                mNombreView.addTextChangedListener(mProductTextWatcher);
                mNombreView.setAdapter(mProductAdapter);
                mNombreView.setOnItemClickListener(mProductItemClickListener);
                JsonArray isapres = mResponse.getAsJsonArray("healthInsurances").get(1).getAsJsonObject().getAsJsonArray("health_insurances");
                mIsapreAdapter = new ProfileFragment.RegisterSpinnerAdapter(inflater, isapres, "name", "Isapre");
                mConvenioAdapter = new ProfileFragment.RegisterSpinnerAdapter(inflater, mResponse.getAsJsonArray("deals"), "name", "Convenio");
                mIsapreView.setAdapter(mIsapreAdapter);
                mConvenioView.setAdapter(mConvenioAdapter);
                break;
            }
            case 1: {
                mLaboratorioView.setVisibility(View.VISIBLE);
                mGramajeView.setVisibility(View.VISIBLE);
                mCantidadView.setVisibility(View.VISIBLE);
                mProductPhotoSelectorLayout.setVisibility(View.VISIBLE);
                ((TextView) mProductPhotoSelectorLayout.findViewById(R.id.txtPhotoDesc)).setText(R.string.sube_foto_producto);
                mCadenaFarmaciaView.setVisibility(View.VISIBLE);
                mPrecioDescuentoView.setVisibility(View.VISIBLE);
                mIsapreView.setVisibility(View.VISIBLE);
                mPrecioPagadoView.setVisibility(View.VISIBLE);
                mPrecioPagadoView.setHint("Precio");
                mPhotoSelectorLayout.setVisibility(View.VISIBLE);
                JsonArray isapres = mResponse.getAsJsonArray("healthInsurances").get(1).getAsJsonObject().getAsJsonArray("health_insurances");
                mIsapreAdapter = new ProfileFragment.RegisterSpinnerAdapter(inflater, isapres, "name", "Isapre");
                mIsapreView.setAdapter(mIsapreAdapter);
                break;
            }
            case 2:
                mCadenaFarmaciaView.setVisibility(View.VISIBLE);
                mPrecioPagadoView.setVisibility(View.VISIBLE);
                mProductAdapter = new AutoCompleteBuscarProductosAdapter(getContext());
                mNombreView.addTextChangedListener(mProductTextWatcher);
                mNombreView.setAdapter(mProductAdapter);
                mNombreView.setOnItemClickListener(mProductItemClickListener);
                break;
        }

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
        switch (btnIndex) {
            case 0:
                if (mFotoBoleta == null) {
                    cancel = true;
                    focusView = null;
                    error = getString(R.string.error_upload_ballot_photo);
                }
                if (mPrecioPagadoView.getText().toString().isEmpty()) {
                    focusView = mPrecioPagadoView;
                    error = getString(R.string.error_field_required);
                    cancel = true;
                }
                if (mCadenaFarmaciaView.getText().toString().isEmpty()) {
                    focusView = mCadenaFarmaciaView;
                    error = getString(R.string.error_field_required);
                    cancel = true;
                }
                if (mNombreView.getText().toString().isEmpty()) {
                    focusView = mNombreView;
                    error = getString(R.string.error_field_required);
                    cancel = true;
                }
                break;
            case 1:
                if (mFotoBoleta == null) {
                    cancel = true;
                    focusView = null;
                    error = getString(R.string.error_upload_ballot_photo);
                }
                if (mPrecioPagadoView.getText().toString().isEmpty()) {
                    focusView = mPrecioPagadoView;
                    error = getString(R.string.error_field_required);
                    cancel = true;
                }
                if (mCadenaFarmaciaView.getText().toString().isEmpty()) {
                    focusView = mCadenaFarmaciaView;
                    error = getString(R.string.error_field_required);
                    cancel = true;
                }
                if (mCantidadView.getText().toString().isEmpty()) {
                    focusView = mCantidadView;
                    error = getString(R.string.error_field_required);
                    cancel = true;
                }
                if (mGramajeView.getText().toString().isEmpty()) {
                    focusView = mGramajeView;
                    error = getString(R.string.error_field_required);
                    cancel = true;
                }
                if (mNombreView.getText().toString().isEmpty()) {
                    focusView = mNombreView;
                    error = getString(R.string.error_field_required);
                    cancel = true;
                }
                break;
            case 2:
                if (mPrecioPagadoView.getText().toString().isEmpty()) {
                    focusView = mPrecioPagadoView;
                    error = getString(R.string.error_field_required);
                    cancel = true;
                }
                if (mNombreView.getText().toString().isEmpty()) {
                    focusView = mNombreView;
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
        String convenio = mConvenioAdapter != null && mConvenioView.getSelectedItemId() != -1 ? (String) mConvenioView.getSelectedItem() : "";
        String isapre = mIsapreAdapter != null && mIsapreView.getSelectedItemId() != -1 ? (String) mIsapreView.getSelectedItem() : "";
        String precioPagado = btnIndex == 0 ? mPrecioPagadoView.getText().toString() : "";
        ReportesRequests.sendReportePrecio(getContext(), SessionHelper.getAccessToken(getContext()), CATEGORY, String.valueOf(btnIndex + 1),
                mNombreView.getText().toString(), mCadenaFarmaciaView.getText().toString(), convenio, isapre, mCantidadView.getText().toString(),
                precioPagado, mPrecioPagadoView.getText().toString(), mPrecioDescuentoView.getText().toString(), mGramajeView.getText().toString(),
                mLaboratorioView.getText().toString(), mFotoProd, mFotoBoleta, new JsonObjectResponse() {
                    @Override
                    public void onSuccess(JsonObject response) {
                        if (getActivity() == null || getActivity().isFinishing()) return;
                        dialog.dismiss();
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .add(R.id.coordinatorLayout, ColaboraThanksFragment.newInstance())
                                .addToBackStack(FRAGMENT_COLABORA_THANKS).commitAllowingStateLoss();
                        Answers.getInstance().logCustom(new CustomEvent("Envio Reporte Precio").putCustomAttribute("subCategoria", btnIndex));
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


    private void buscarProducto(final String producto) {
        SearchProductsRequest.fetch(getActivity(), getAccessToken(getActivity()), producto, new JsonArrayResponse() {
            @Override
            public void onSuccess(JsonArray response) {
                mProductAdapter.setResultsFromJson(response, producto);
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
            if (selectingPhotoProduct) {
                mFotoProd = imageFile;
                ((TextView) mProductPhotoSelectorLayout.findViewById(R.id.txtPhotoSelectedName)).setText(imageName);
                mProductPhotoSelectorLayout.findViewById(R.id.checkView).setVisibility(View.VISIBLE);
            } else if (selectingPhotoBoleta) {
                mFotoBoleta = imageFile;
                mPhotoSelectorLayout.findViewById(R.id.checkView).setVisibility(View.VISIBLE);
                ((TextView) mPhotoSelectorLayout.findViewById(R.id.txtPhotoSelectedName)).setText(imageName);
            }
        }
        if (error)
            Toast.makeText(getContext(), "Error cargando imagen", Toast.LENGTH_SHORT).show();
        selectingPhotoBoleta = false;
        selectingPhotoProduct = false;
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
        getActivity().setTitle(getString(R.string.action_bar_title_colabora_precio));
    }
}
