package com.orugga.yapp.fragments.colabora;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.gson.JsonObject;
import com.orugga.yapp.R;
import com.orugga.yapp.helpers.DialogHelper;
import com.orugga.yapp.interfaces.ActivityUpdateBackToolbar;
import com.orugga.yapp.interfaces.ButtonReporteCallback;
import com.orugga.yapp.interfaces.JsonObjectResponse;
import com.orugga.yapp.interfaces.OnFragmentUpdateToolbarIntefrace;
import com.orugga.yapp.requests.GetRegisterDataRequest;

import static com.orugga.yapp.Constants.FragmentsNames.FRAGMENT_COLABORA_CONVENIO;
import static com.orugga.yapp.Constants.FragmentsNames.FRAGMENT_COLABORA_FARMACIA;
import static com.orugga.yapp.Constants.FragmentsNames.FRAGMENT_COLABORA_OTRO;
import static com.orugga.yapp.Constants.FragmentsNames.FRAGMENT_COLABORA_PRECIO;
import static com.orugga.yapp.Constants.FragmentsNames.FRAGMENT_COLABORA_PROMOCION;
import static com.orugga.yapp.Constants.ReportesTypes.REPORTE_DESCUENTO_O_CONVENIO;
import static com.orugga.yapp.Constants.ReportesTypes.REPORTE_FARMACIA;
import static com.orugga.yapp.Constants.ReportesTypes.REPORTE_PRECIO;
import static com.orugga.yapp.Constants.ReportesTypes.REPORTE_PRODUCTO;
import static com.orugga.yapp.Constants.ReportesTypes.REPORTE_PROMOCION;


public class ColaboraFragment extends Fragment implements OnFragmentUpdateToolbarIntefrace {
    public ColaboraFragment() {
        // Required empty public constructor
    }

    public static ColaboraFragment newInstance() {
        return new ColaboraFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_colabora, container, false);

        view.findViewById(R.id.btnReportarPrecio).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogHelper.showReportSelector(getActivity(), getResources().getStringArray(R.array.reporte_strings_precio), REPORTE_PRECIO, new ButtonReporteCallback() {
                    @Override
                    public void onClick(final int buttonIndex) {
                        if (buttonIndex == 2) {
                            openReportePrecioFragment(buttonIndex, null);
                        } else {
                            final ProgressDialog progressDialog = ProgressDialog.show(getContext(), "", getString(R.string.loading));
                            GetRegisterDataRequest.getRegisterData(getActivity(), new JsonObjectResponse() {
                                @Override
                                public void onSuccess(JsonObject response) {
                                    if (getActivity() == null || getActivity().isFinishing()) return;
                                    progressDialog.dismiss();
                                    openReportePrecioFragment(buttonIndex, response);
                                }

                                @Override
                                public void onError(JsonObject response, Exception e) {
                                    if (getActivity() == null || getActivity().isFinishing()) return;
                                    progressDialog.dismiss();
                                    if (e != null) {
                                        Crashlytics.logException(e);
                                    }
                                    Toast.makeText(getContext(), R.string.error_message_internet_conection, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
            }
        });
        view.findViewById(R.id.btnReportarProducto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogHelper.showReportSelector(getActivity(), getResources().getStringArray(R.array.reporte_strings_producto), REPORTE_PRODUCTO, new ButtonReporteCallback() {
                    @Override
                    public void onClick(int buttonIndex) {
                        openReporteProductoFragment(buttonIndex);
                    }
                });
            }
        });
        view.findViewById(R.id.btnReportarDescuentoOConvenio).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogHelper.showReportSelector(getActivity(), getResources().getStringArray(R.array.reporte_strings_convenio), REPORTE_DESCUENTO_O_CONVENIO, new ButtonReporteCallback() {
                    @Override
                    public void onClick(int buttonIndex) {
                        openReporteConvenioFragment(buttonIndex);
                    }
                });
            }
        });
        view.findViewById(R.id.btnReportarFarmacia).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogHelper.showReportSelector(getActivity(), getResources().getStringArray(R.array.reporte_strings_farmacia), REPORTE_FARMACIA, new ButtonReporteCallback() {
                    @Override
                    public void onClick(int buttonIndex) {
                        openReporteFarmaciaFragment(buttonIndex);
                    }
                });
            }
        });
        view.findViewById(R.id.btnReportarPromocion).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogHelper.showReportSelector(getActivity(), getResources().getStringArray(R.array.reporte_strings_promociones), REPORTE_PROMOCION, new ButtonReporteCallback() {
                    @Override
                    public void onClick(int buttonIndex) {
                        openReportePromocionFragment(buttonIndex);
                    }
                });
            }
        });
        view.findViewById(R.id.btnReportarOtroMotivo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openReporteOtroFragment();
            }
        });


        return view;
    }

    private void openReporteOtroFragment() {
        getActivity().getSupportFragmentManager().beginTransaction()
                .add(R.id.coordinatorLayout, ColaboraReportarOtroFragment.newInstance())
                .addToBackStack(FRAGMENT_COLABORA_OTRO).commitAllowingStateLoss();
    }

    private void openReportePromocionFragment(int buttonIndex) {
        getActivity().getSupportFragmentManager().beginTransaction()
                .add(R.id.coordinatorLayout, ColaboraReportarPromocionFragment.newInstance(buttonIndex))
                .addToBackStack(FRAGMENT_COLABORA_PROMOCION).commitAllowingStateLoss();
    }

    private void openReporteFarmaciaFragment(int buttonIndex) {
        getActivity().getSupportFragmentManager().beginTransaction()
                .add(R.id.coordinatorLayout, ColaboraReportarFarmaciaFragment.newInstance(buttonIndex))
                .addToBackStack(FRAGMENT_COLABORA_FARMACIA).commitAllowingStateLoss();
    }

    private void openReporteConvenioFragment(int buttonIndex) {
        getActivity().getSupportFragmentManager().beginTransaction()
                .add(R.id.coordinatorLayout, ColaboraReportarConveniosFragment.newInstance(buttonIndex))
                .addToBackStack(FRAGMENT_COLABORA_CONVENIO).commitAllowingStateLoss();
    }

    private void openReporteProductoFragment(int buttonIndex) {
        getActivity().getSupportFragmentManager().beginTransaction()
                .add(R.id.coordinatorLayout, ColaboraReportarProductoFragment.newInstance(buttonIndex))
                .addToBackStack(FRAGMENT_COLABORA_PRECIO).commitAllowingStateLoss();
    }

    private void openReportePrecioFragment(int buttonIndex, JsonObject response) {
        getActivity().getSupportFragmentManager().beginTransaction()
                .add(R.id.coordinatorLayout, ColaboraReportarPrecioFragment.newInstance(buttonIndex, response == null ? new JsonObject() : response))
                .addToBackStack(FRAGMENT_COLABORA_PRECIO).commitAllowingStateLoss();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onFragmentUpdateToolbar() {
        if (getActivity() instanceof ActivityUpdateBackToolbar) {
            ((ActivityUpdateBackToolbar) getActivity()).showBackNavigationIcon();
        }
        getActivity().setTitle(getString(R.string.action_bar_title_colabora));
    }
}
