package com.orugga.yapp.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.crashlytics.android.answers.CustomEvent;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.orugga.yapp.R;
import com.orugga.yapp.adapters.ResultProductDetailsAdapter;
import com.orugga.yapp.requests.DownloadImageRequest;

import java.text.NumberFormat;
import java.util.Locale;

import static com.orugga.yapp.Constants.ANSWERS_CONSTANTS.CONTENT_TYPE_VIEW;
import static com.orugga.yapp.Constants.ANSWERS_CONSTANTS.VIEW_DETALLE_RESULTADO_BUSQUEDA_PRODUCTO_ID;
import static com.orugga.yapp.Constants.ApiFields.PHARMACY_LATITUDE;
import static com.orugga.yapp.Constants.ApiFields.PHARMACY_LONGITUDE;
import static com.orugga.yapp.Constants.ApiFields.PHARMACY_NAME;
import static com.orugga.yapp.Constants.ApiFields.PHARMACY_PHARMACYCHAIN;
import static com.orugga.yapp.Constants.ApiFields.PHARMACY_RESULT_TOTAL_PRODUCTS_PRICE;
import static com.orugga.yapp.Constants.ApiFields.PHARMACY_RESULT_TOTAL_PRODUCTS_PRICE_DISCOUNT;
import static com.orugga.yapp.Constants.ApiFields.PHARMACY_STATUS;
import static com.orugga.yapp.Constants.FragmentsNames.FRAGMENT_BUSCAR_FARMACIA;
import static com.orugga.yapp.Constants.IntentFilters.ACTION_SEE_RESULTS;


public class DetallesResultadoBusquedaProductoFragment extends Fragment {

    private JsonObject mResultadosBusqueda;
    private JsonArray mProducts;

    public DetallesResultadoBusquedaProductoFragment() {
        // Required empty public constructor
    }

    public static DetallesResultadoBusquedaProductoFragment newInstance(JsonObject resultadosBusqueda) {
        DetallesResultadoBusquedaProductoFragment fragment = new DetallesResultadoBusquedaProductoFragment();
        Bundle args = new Bundle();
        args.putString("results", resultadosBusqueda.toString());
        args.putString("products", resultadosBusqueda.getAsJsonArray("products").toString());
        fragment.setArguments(args);
        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName("Detalle Resultado Busqueda Producto")
                .putContentId(VIEW_DETALLE_RESULTADO_BUSQUEDA_PRODUCTO_ID)
                .putContentType(CONTENT_TYPE_VIEW));
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mResultadosBusqueda = new JsonParser().parse(getArguments().getString("results")).getAsJsonObject();
            mProducts = new JsonParser().parse(getArguments().getString("products")).getAsJsonArray();
        }
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detalles_resultado_busqueda_producto, container, false);

        DownloadImageRequest.downloadImage(getContext(),
                mResultadosBusqueda.getAsJsonObject(PHARMACY_PHARMACYCHAIN).get("full_path_logo_400").getAsString(),
                (ImageView) view.findViewById(R.id.imgPharmacyPhoto),
                R.drawable.placeholder_farmacia);

        if (!mResultadosBusqueda.get(PHARMACY_PHARMACYCHAIN).isJsonNull() &&
                !mResultadosBusqueda.getAsJsonObject(PHARMACY_PHARMACYCHAIN).get("number_telephone").isJsonNull()) {
            ImageView btnLlamarFarmacia = view.findViewById(R.id.btnLlamarFarmacia);
            btnLlamarFarmacia.setVisibility(View.VISIBLE);
            btnLlamarFarmacia.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    String number = mResultadosBusqueda.getAsJsonObject(PHARMACY_PHARMACYCHAIN).get("number_telephone").getAsString();
                    intent.setData(Uri.parse("tel:" + number));
                    startActivity(intent);
                    Crashlytics.getInstance().answers.logCustom(new CustomEvent("Venta telefonica"));
                }
            });
        }
        ((TextView) view.findViewById(R.id.txtPharmacyName)).setText(mResultadosBusqueda.get(PHARMACY_NAME).getAsString());
        NumberFormat format = NumberFormat.getNumberInstance(Locale.GERMAN);
        format.setMaximumFractionDigits(0);
        String precioRegular = "$" + format.format(mResultadosBusqueda.get(PHARMACY_RESULT_TOTAL_PRODUCTS_PRICE).getAsDouble());
        String precioConDescuento = "$" + format.format(mResultadosBusqueda.get(PHARMACY_RESULT_TOTAL_PRODUCTS_PRICE_DISCOUNT).getAsDouble());
        ((TextView) view.findViewById(R.id.txtPrecioRegular)).setText(precioRegular);
        if (precioConDescuento.equals(precioRegular)) {
            view.findViewById(R.id.imgTachar).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.lblPrecioConDescuento).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.txtPrecioConDescuento).setVisibility(View.INVISIBLE);
        } else {
            ((TextView) view.findViewById(R.id.txtPrecioConDescuento)).setText(precioConDescuento);
        }

        TextView flagStatus = view.findViewById(R.id.txtFlagPharmacyStatus);
        String status = mResultadosBusqueda.get(PHARMACY_STATUS).getAsString().toLowerCase();
        switch (status) {
            case "open":
                flagStatus.setBackground(getContext().getResources().getDrawable(R.drawable.flag_abierto));
                flagStatus.setText(R.string.pharmacy_flag_status_open);
                break;
            case "closed":
                flagStatus.setBackground(getContext().getResources().getDrawable(R.drawable.flag_cerrado));
                flagStatus.setText(R.string.pharmacy_flag_status_closed);
                break;
            case "closing":
                flagStatus.setBackground(getContext().getResources().getDrawable(R.drawable.flag_cierre_proximo));
                flagStatus.setText(R.string.pharmacy_flag_status_closing);
                break;
        }

        view.findViewById(R.id.verDetalleFarmacia).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JsonArray pharmacy = new JsonArray();
                pharmacy.add(mResultadosBusqueda);
                getFragmentManager().beginTransaction()
                        .add(R.id.coordinatorLayout, BuscarFarmaciaFragment.newInstance(pharmacy, ACTION_SEE_RESULTS))
                        .addToBackStack(FRAGMENT_BUSCAR_FARMACIA).commitAllowingStateLoss();
            }
        });


        ResultProductDetailsAdapter mResultProductDetailsAdapter = new ResultProductDetailsAdapter(getActivity(), getActivity().getSupportFragmentManager(), mProducts);
        ListView listViewProductDetails = view.findViewById(R.id.listViewProductResultDetails);
        listViewProductDetails.setAdapter(mResultProductDetailsAdapter);

        return view;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.simple_share_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Answers.getInstance().logCustom(new CustomEvent("Resultados Yapp - Compartir"));
        Intent sharingIntent = new Intent();
        sharingIntent.setAction(Intent.ACTION_SEND);
        sharingIntent.setType("text/html");

        StringBuilder text = new StringBuilder().append("https://www.google.com/maps/search/?api=1&query=")
                .append(mResultadosBusqueda.get(PHARMACY_LATITUDE).getAsString()).append(",").append(mResultadosBusqueda.get(PHARMACY_LONGITUDE).getAsString()).append("<br>")
                .append(mResultadosBusqueda.get(PHARMACY_NAME).getAsString()).append("<br>");
        for (int i = 0; i < mProducts.size(); i++) {
            text.append(mProducts.get(i).getAsJsonObject().get("full_name").getAsString());
            if (i < mProducts.size() - 1)
                text.append(", ");
        }
        text.append("<br>");

        text.append("Precio con Descuento: $").append(mResultadosBusqueda.get("total_price_products_discount").getAsString());

        sharingIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(text.toString()).toString());

        startActivity(Intent.createChooser(sharingIntent, "Compartir usando"));
        return false;
    }
}
