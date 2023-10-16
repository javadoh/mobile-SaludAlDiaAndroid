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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.orugga.yapp.AlarmBroadcastReceiver;
import com.orugga.yapp.LoginActivity;
import com.orugga.yapp.R;
import com.orugga.yapp.adapters.AutoCompleteBuscarProductosAdapter;
import com.orugga.yapp.database.RecordatorioToma;
import com.orugga.yapp.database.RecordatorioVisita;
import com.orugga.yapp.fragments.BuscarFarmaciaFragment;
import com.orugga.yapp.helpers.IdleHelper;
import com.orugga.yapp.helpers.SessionHelper;
import com.orugga.yapp.interfaces.JsonArrayResponse;
import com.orugga.yapp.interfaces.JsonObjectResponse;
import com.orugga.yapp.interfaces.OnFragmentUpdateToolbarIntefrace;
import com.orugga.yapp.interfaces.PharmacyCallback;
import com.orugga.yapp.requests.ReportesRequests;
import com.orugga.yapp.requests.SearchProductsRequest;

import java.io.File;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;
import static com.orugga.yapp.Constants.ApiFields.PHARMACY_ID;
import static com.orugga.yapp.Constants.ApiFields.PRODUCT_NAME;
import static com.orugga.yapp.Constants.FragmentsNames.FRAGMENT_BUSCAR_FARMACIA;
import static com.orugga.yapp.Constants.FragmentsNames.FRAGMENT_COLABORA_THANKS;
import static com.orugga.yapp.Constants.IntentFilters.ACTION_SELECT_PHARMACY;
import static com.orugga.yapp.Constants.IntentFilters.REQUEST_IMAGE_CAPTURE;
import static com.orugga.yapp.Constants.IntentFilters.REQUEST_SELECT_IMAGE;
import static com.orugga.yapp.Constants.RequestPermission.REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE;
import static com.orugga.yapp.helpers.FileChooser.getPath;
import static com.orugga.yapp.helpers.IdleHelper.ocultarTeclado;
import static com.orugga.yapp.helpers.SessionHelper.getAccessToken;

/**
 * A simple {@link Fragment} subclass.
 */
public class ColaboraReportarProductoFragment extends Fragment implements OnFragmentUpdateToolbarIntefrace {

    private static final String CATEGORY = "1";
    private int btnIndex;
    private AutoCompleteTextView mNombreView;
    private AutoCompleteTextView mLaboratorioView;
    private EditText mGramajeView;
    private EditText mCantidadView;
    private AutoCompleteTextView mCadenaFarmaciaView;
    private EditText mPrecioView;
    private EditText mCorreccionView;
    private ConstraintLayout mPharmacySelectorLayout;
    private JsonObject mSelectedPharmacy;
    private ConstraintLayout mProductPhotoSelectorLayout;
    private File mFotoProd;
    private CheckBox mStockView;
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

    public ColaboraReportarProductoFragment() {
        // Required empty public constructor
    }

    public static ColaboraReportarProductoFragment newInstance(int btnIndex) {
        Bundle args = new Bundle();
        ColaboraReportarProductoFragment fragment = new ColaboraReportarProductoFragment();
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
        View view = inflater.inflate(R.layout.fragment_colabora_reportar_producto, container, false);

        mNombreView = view.findViewById(R.id.txtNombre);
        mLaboratorioView = view.findViewById(R.id.txtLaboratorio);
        mGramajeView = view.findViewById(R.id.txtGramaje);
        mCantidadView = view.findViewById(R.id.txtCantidad);
        mCadenaFarmaciaView = view.findViewById(R.id.txtCadenaFarmacia);
        mPrecioView = view.findViewById(R.id.txtPrecio);
        mCorreccionView = view.findViewById(R.id.txtCorreccion);
        mPharmacySelectorLayout = view.findViewById(R.id.pharmacySelector);
        mProductPhotoSelectorLayout = view.findViewById(R.id.productPhotoSelector);
        mStockView = view.findViewById(R.id.stockView);
        TextView mBtnOk = view.findViewById(R.id.btnOk);

        mProductPhotoSelectorLayout.findViewById(R.id.btnSelectPhoto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!runtime_permissions())
                    selectPhoto();
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
                mLaboratorioView.setVisibility(View.VISIBLE);
                mGramajeView.setVisibility(View.VISIBLE);
                mCantidadView.setVisibility(View.VISIBLE);
                mProductPhotoSelectorLayout.setVisibility(View.VISIBLE);
                ((TextView) mProductPhotoSelectorLayout.findViewById(R.id.txtPhotoDesc)).setText(getString(R.string.sube_foto_producto));
                mCadenaFarmaciaView.setVisibility(View.VISIBLE);
                mPrecioView.setVisibility(View.VISIBLE);
                break;
            case 1:
                mLaboratorioView.setVisibility(View.VISIBLE);
                mGramajeView.setVisibility(View.VISIBLE);
                mCantidadView.setVisibility(View.VISIBLE);
                mProductAdapter = new AutoCompleteBuscarProductosAdapter(getContext());
                mNombreView.addTextChangedListener(mProductTextWatcher);
                mNombreView.setAdapter(mProductAdapter);
                mNombreView.setOnItemClickListener(mProductItemClickListener);
                break;
            case 2:
                mCorreccionView.setVisibility(View.VISIBLE);
                mProductAdapter = new AutoCompleteBuscarProductosAdapter(getContext());
                mNombreView.addTextChangedListener(mProductTextWatcher);
                mNombreView.setAdapter(mProductAdapter);
                mNombreView.setOnItemClickListener(mProductItemClickListener);
                break;
            case 3:
                mPharmacySelectorLayout.setVisibility(View.VISIBLE);
                mStockView.setVisibility(View.VISIBLE);
                mProductAdapter = new AutoCompleteBuscarProductosAdapter(getContext());
                mNombreView.addTextChangedListener(mProductTextWatcher);
                mNombreView.setAdapter(mProductAdapter);
                mNombreView.setOnItemClickListener(mProductItemClickListener);
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

    private void attemptSend() {
        boolean cancel = false;
        String error = "";
        View focusView = null;
        switch (btnIndex) {
            case 0:
                if (mCantidadView.getText().toString().isEmpty()) {
                    cancel = true;
                    focusView = mCantidadView;
                    error = getString(R.string.error_field_required);
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
            case 1:
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
                if (mCorreccionView.getText().toString().isEmpty()) {
                    focusView = mCorreccionView;
                    error = getString(R.string.error_field_required);
                    cancel = true;
                }
                if (mNombreView.getText().toString().isEmpty()) {
                    focusView = mNombreView;
                    error = getString(R.string.error_field_required);
                    cancel = true;
                }
                break;
            case 3:
                if (mSelectedPharmacy == null) {
                    cancel = true;
                    error = getString(R.string.error_select_pharmacy);
                    focusView = null;
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
        String pharmacy = mSelectedPharmacy != null ? mSelectedPharmacy.get(PHARMACY_ID).getAsString() : "";
        ReportesRequests.sendReporteProducto(getContext(), SessionHelper.getAccessToken(getContext()), CATEGORY, String.valueOf(btnIndex + 1),
                mNombreView.getText().toString(), mLaboratorioView.getText().toString(), mGramajeView.getText().toString(), mCantidadView.getText().toString(),
                mCadenaFarmaciaView.getText().toString(), mPrecioView.getText().toString(), mCorreccionView.getText().toString(), pharmacy, mStockView.isChecked(),
                mFotoProd, new JsonObjectResponse() {
                    @Override
                    public void onSuccess(JsonObject response) {
                        if (getActivity() == null || getActivity().isFinishing()) return;
                        dialog.dismiss();
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .add(R.id.coordinatorLayout, ColaboraThanksFragment.newInstance())
                                .addToBackStack(FRAGMENT_COLABORA_THANKS).commitAllowingStateLoss();
                        Answers.getInstance().logCustom(new CustomEvent("Envio Reporte Producto").putCustomAttribute("subCategoria", btnIndex));
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
            mFotoProd = imageFile;
            ((TextView) mProductPhotoSelectorLayout.findViewById(R.id.txtPhotoSelectedName)).setText(imageName);
            mProductPhotoSelectorLayout.findViewById(R.id.checkView).setVisibility(View.VISIBLE);
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
        getActivity().setTitle(getString(R.string.action_bar_title_colabora_producto));
    }
}
