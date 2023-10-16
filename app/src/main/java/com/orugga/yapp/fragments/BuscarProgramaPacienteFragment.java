package com.orugga.yapp.fragments;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.orugga.yapp.R;
import com.orugga.yapp.adapters.AutoCompleteBuscarProductosAdapter;
import com.orugga.yapp.adapters.PacientProgramListAdapter;
import com.orugga.yapp.interfaces.ActivityUpdateBackToolbar;
import com.orugga.yapp.interfaces.JsonArrayResponse;
import com.orugga.yapp.interfaces.OnFragmentUpdateToolbarIntefrace;
import com.orugga.yapp.requests.SearchProductsRequest;

import static com.orugga.yapp.Constants.FragmentsNames.FRAGMENT_DETALLE_PROGRAMA_PACIENTE;
import static com.orugga.yapp.helpers.IdleHelper.ocultarTeclado;
import static com.orugga.yapp.helpers.SessionHelper.getAccessToken;
import static com.orugga.yapp.requests.SearchPacientProgramsRequest.searchDetailPacientPrograms;
import static com.orugga.yapp.requests.SearchPacientProgramsRequest.searchPacientPrograms;


/**
 * A simple {@link Fragment} subclass.
 */
public class BuscarProgramaPacienteFragment extends Fragment implements OnFragmentUpdateToolbarIntefrace {

    private AutoCompleteTextView mAutoCompleteBuscarProducto;
    private AutoCompleteBuscarProductosAdapter mAutoCompleteBuscarProductosAdapter;

//    private AutoCompleteTextView mAutoCompleteBuscarLab;
//    private AutoCompleteBuscarLabsAdapter mAutoCompleteBuscarLabsAdapter;

    private PacientProgramListAdapter mPacientProgramAdapter;

    //    private int mLabId = -1;
    private int mProductId = -1;

    //    private String mCurrentLabNameSelection = "";
    private String mCurrentProductNameSelection = "";

    private JsonArray mAllPP;

    private View mView;

    private JsonObject product;
    private AdapterView.OnItemClickListener mAutocompleteProductItemClickListener =
            new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Answers.getInstance().logCustom(new CustomEvent("Programa Paciente - ver detalle"));
                    JsonObject product = mAutoCompleteBuscarProductosAdapter.getItem(position);
                    mCurrentProductNameSelection = product.get("name").getAsString();
                    mAutoCompleteBuscarProducto.setText(mCurrentProductNameSelection);
                    mProductId = product.get("id").getAsInt();
                    mView.requestFocus();
                    ocultarTeclado(getActivity());
                    if (mAutoCompleteBuscarProducto.getText().length() >= 3) {
                        buscarProgramasPaciente();
                    }
                }
            };
//    private AdapterView.OnItemClickListener mAutocompleteLabItemClickListener =
//            new AdapterView.OnItemClickListener() {
//                @Override
//                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//                    JsonObject lab = mAutoCompleteBuscarLabsAdapter.getItem(position);
//                    mCurrentLabNameSelection = lab.get("name").getAsString();
//                    mAutoCompleteBuscarLab.setText(mCurrentLabNameSelection);
//                    mLabId = lab.get("id").getAsInt();
//                    mAutoCompleteBuscarLab.clearFocus();
//                    ocultarTeclado(getActivity());
//                    if (mAutoCompleteBuscarLab.getText().length() >= 3) {
//                        buscarProgramasPaciente();
//                    }
//                }
//            };

    public BuscarProgramaPacienteFragment() {
        // Required empty public constructor
    }

    public static BuscarProgramaPacienteFragment newInstance() {
        return new BuscarProgramaPacienteFragment();
    }

    public static BuscarProgramaPacienteFragment newInstance(JsonObject product) {
        BuscarProgramaPacienteFragment f = new BuscarProgramaPacienteFragment();
        f.product = product;
        return f;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_buscar_programa_paciente, container, false);

        /*mAutoCompleteBuscarLab = mView.findViewById(R.id.txtAutoCompleteSearchLabs);
        mAutoCompleteBuscarLabsAdapter = new AutoCompleteBuscarLabsAdapter(getContext());
        mAutoCompleteBuscarLab.setAdapter(mAutoCompleteBuscarLabsAdapter);
        mAutoCompleteBuscarLab.setOnItemClickListener(mAutocompleteLabItemClickListener);
        mAutoCompleteBuscarLab.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!mCurrentLabNameSelection.contentEquals(s)) {
                    if (s.length() >= 3) {
                        buscarLab(s.toString());
                    }
                    mLabId = -1;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    mLabId = -1;
                    if (mAllPP != null) {
                        mPacientProgramAdapter.setNewContent(mAllPP);
                    } else {
                        buscarProgramasPaciente();
                    }
                }
            }
        });
        mAutoCompleteBuscarLab.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (mAutoCompleteBuscarLab.getText().toString().length() >= 3) {
                        buscarLab(mAutoCompleteBuscarLab.getText().toString());
                        mAutoCompleteBuscarLab.showDropDown();
                    }
                }
            }
        });*/

        mAutoCompleteBuscarProducto = mView.findViewById(R.id.txtAutoCompleteSearchProducts);
        mAutoCompleteBuscarProductosAdapter = new AutoCompleteBuscarProductosAdapter(getContext());
        mAutoCompleteBuscarProducto.setAdapter(mAutoCompleteBuscarProductosAdapter);
        mAutoCompleteBuscarProducto.setOnItemClickListener(mAutocompleteProductItemClickListener);
        mAutoCompleteBuscarProducto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!mCurrentProductNameSelection.contentEquals(s)) {
                    if (s.length() >= 3 && s.length() < 256) {
                        buscarProducto(s.toString());
                    }
                    mProductId = -1;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    mProductId = -1;
                    if (mAllPP != null) {
                        mPacientProgramAdapter.setNewContent(mAllPP);
                    } else {
                        buscarProgramasPaciente();
                    }
                }
            }
        });
        mAutoCompleteBuscarProducto.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (mAutoCompleteBuscarProducto.getText().toString().length() >= 3) {
                        buscarProducto(mAutoCompleteBuscarProducto.getText().toString());
                        mAutoCompleteBuscarProducto.showDropDown();
                    }
                }
            }
        });


        ListView pacientProgramListView = mView.findViewById(R.id.listViewPacientPrograms);

        mPacientProgramAdapter = new PacientProgramListAdapter(getContext(), new JsonArray());
        pacientProgramListView.setAdapter(mPacientProgramAdapter);
        pacientProgramListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ocultarTeclado(getActivity());
                getActivity().getSupportFragmentManager().beginTransaction()
                        .add(R.id.coordinatorLayout, PacientProgramDetailsFragment.newInstance(mPacientProgramAdapter.getItem(position)))
                        .addToBackStack(FRAGMENT_DETALLE_PROGRAMA_PACIENTE).commitAllowingStateLoss();
            }
        });

        if (product != null) {
            mAutoCompleteBuscarProducto.setText(product.get("name").getAsString());
            mProductId = product.get("id").getAsInt();
        }

        buscarProgramasPaciente();
        return mView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    private void buscarProgramasPaciente() {
        final ProgressDialog progressDialog = ProgressDialog.show(getContext(), "", getString(R.string.loading));

        if (mProductId != -1) {
            searchDetailPacientPrograms(getActivity(), mProductId, new JsonArrayResponse() {
                @Override
                public void onSuccess(JsonArray response) {
                    progressDialog.dismiss();
                    if (response.size() == 0)
                        Toast.makeText(getContext(), R.string.toast_not_found, Toast.LENGTH_SHORT).show();
                    mPacientProgramAdapter.setNewContent(response);
                }

                @Override
                public void onError(JsonObject response, Exception e) {
                    if (getActivity() == null || getActivity().isFinishing()) return;
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(), R.string.error_message_internet_conection, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            searchPacientPrograms(getActivity(), /*mLabId,*/ mProductId,
                    /*mAutoCompleteBuscarLab.getText().toString(),*/ mAutoCompleteBuscarProducto.getText().toString(),
                    new JsonArrayResponse() {
                        @Override
                        public void onSuccess(JsonArray response) {
                            progressDialog.dismiss();
                            if (/*mLabId == -1 &&*/ mAllPP == null && mProductId == -1) mAllPP = response;
                            if (response.size() == 0)
                                Toast.makeText(getContext(), R.string.toast_not_found, Toast.LENGTH_SHORT).show();
                            mPacientProgramAdapter.setNewContent(response);
                        }

                        @Override
                        public void onError(JsonObject response, Exception e) {
                            if (getActivity() == null || getActivity().isFinishing()) return;
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), R.string.error_message_internet_conection, Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void buscarProducto(final String producto) {
        SearchProductsRequest.fetch(getActivity(), getAccessToken(getActivity()), producto, new JsonArrayResponse() {
            @Override
            public void onSuccess(JsonArray response) {
                if (getActivity() == null || getActivity().isFinishing()) return;
                mAutoCompleteBuscarProductosAdapter.setResultsFromJson(response, producto);
            }

            @Override
            public void onError(JsonObject response, Exception e) {
                if (getActivity() == null || getActivity().isFinishing()) return;
                Toast.makeText(getActivity(), "Error en Buscar Productos", Toast.LENGTH_SHORT).show();
            }
        });
    }

//    private void buscarLab(final String lab) {
//        searchLabs(getActivity(), lab, new JsonArrayResponse() {
//            @Override
//            public void onSuccess(JsonArray response) {
//                mAutoCompleteBuscarLabsAdapter.setResultsFromJson(response, lab);
//            }
//
//            @Override
//            public void onError(JsonObject response, Exception e) {
//                Log.e("AutoCompleteLabs", "Error en Buscar Labs");
//            }
//        });
//    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (menu.findItem(R.id.menu_item_share) != null)
            menu.findItem(R.id.menu_item_share).setVisible(false);
    }

    @Override
    public void onFragmentUpdateToolbar() {
        getActivity().setTitle(getString(R.string.action_bar_title_programa_paciente));
        if (getActivity() instanceof ActivityUpdateBackToolbar) {
            ((ActivityUpdateBackToolbar) getActivity()).showBackNavigationIcon();
        }
    }
}
