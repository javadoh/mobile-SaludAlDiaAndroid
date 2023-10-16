package com.orugga.yapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.orugga.yapp.R;
import com.orugga.yapp.fragments.BuscarFarmaciaFragment;
import com.orugga.yapp.requests.DownloadImageRequest;
import java.text.NumberFormat;
import java.util.Locale;
import static com.orugga.yapp.Constants.ApiFields.PHARMACY_NAME;
import static com.orugga.yapp.Constants.ApiFields.PHARMACY_PHARMACYCHAIN;
import static com.orugga.yapp.Constants.ApiFields.PHARMACY_RESULT_TOTAL_PRODUCTS_PRICE;
import static com.orugga.yapp.Constants.ApiFields.PHARMACY_RESULT_TOTAL_PRODUCTS_PRICE_DISCOUNT;
import static com.orugga.yapp.Constants.ApiFields.PHARMACY_RESULT_URL_WEB;
import static com.orugga.yapp.Constants.ApiFields.PHARMACY_STATUS;
import static com.orugga.yapp.Constants.FragmentsNames.FRAGMENT_BUSCAR_FARMACIA;
import static com.orugga.yapp.Constants.IntentFilters.ACTION_SEE_RESULTS;

/**
 * Created by Alexis on 02/11/2017.
 */

public class ResultPharmaciesListAdapter extends ArrayAdapter<JsonObject> {

    private Context mContext;
    private FragmentManager fm;

    public ResultPharmaciesListAdapter(Context context, int resource, FragmentManager fragmentManager){
        super(context, resource);
        mContext = context;
        fm = fragmentManager;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(mContext).inflate(R.layout.product_search_pharmacy_item, parent, false);

        JsonObject pharmacy = getItem(position);

        JsonObject pharmacyChain = pharmacy.get(PHARMACY_PHARMACYCHAIN).getAsJsonObject();
        DownloadImageRequest.downloadImage(mContext, pharmacyChain.get("full_path_logo_400").getAsString(), (ImageView) convertView.findViewById(R.id.imgPharmacyPhoto), R.drawable.placeholder_farmacia);

        ((TextView) convertView.findViewById(R.id.txtPharmacyName)).setText(pharmacy.get(PHARMACY_NAME).getAsString());
        NumberFormat format = NumberFormat.getNumberInstance(Locale.GERMAN);
        format.setMaximumFractionDigits(0);
        String precioRegular = "$" + format.format(pharmacy.get(PHARMACY_RESULT_TOTAL_PRODUCTS_PRICE).getAsDouble());
        String precioConDescuento = "$" + format.format(pharmacy.get(PHARMACY_RESULT_TOTAL_PRODUCTS_PRICE_DISCOUNT).getAsDouble());
        ((TextView) convertView.findViewById(R.id.txtPrecioRegular)).setText(precioRegular);
        if (precioConDescuento.equals(precioRegular)){
            convertView.findViewById(R.id.imgTachar).setVisibility(View.INVISIBLE);
            convertView.findViewById(R.id.lblPrecioConDescuento).setVisibility(View.INVISIBLE);
            convertView.findViewById(R.id.txtPrecioConDescuento).setVisibility(View.INVISIBLE);
            ((TextView) convertView.findViewById(R.id.txtPrecioRegular)).setTextSize(16);
            ((TextView) convertView.findViewById(R.id.txtPrecioRegular)).setTypeface(null, Typeface.BOLD);
        } else {
            ((TextView) convertView.findViewById(R.id.txtPrecioConDescuento)).setText(precioConDescuento);
        }

        TextView flagStatus = convertView.findViewById(R.id.txtFlagPharmacyStatus);
        String status = pharmacy.get(PHARMACY_STATUS).getAsString().toLowerCase();
        switch (status){
            case "open":
                flagStatus.setBackground(mContext.getResources().getDrawable(R.drawable.flag_abierto));
                flagStatus.setText(R.string.pharmacy_flag_status_open);
                break;
            case "closed":
                flagStatus.setBackground(mContext.getResources().getDrawable(R.drawable.flag_cerrado));
                flagStatus.setText(R.string.pharmacy_flag_status_closed);
                break;
            case "closing":
                flagStatus.setBackground(mContext.getResources().getDrawable(R.drawable.flag_cierre_proximo));
                flagStatus.setText(R.string.pharmacy_flag_status_closing);
                break;
        }

        /** LUIS **/
        final Object urlObject = pharmacy.get(PHARMACY_RESULT_URL_WEB);
        final boolean isNullUrlJsonValue = ((JsonElement) urlObject).isJsonNull();

        if(isNullUrlJsonValue){
            convertView.findViewById(R.id.verDetalleFarmacia).setBackgroundResource(R.drawable.ic_mapa_pointer);
        }else{
            convertView.findViewById(R.id.verDetalleFarmacia).setBackgroundResource(R.drawable.avatar);
        }

        convertView.findViewById(R.id.verDetalleFarmacia).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isNullUrlJsonValue) {
                    JsonArray pharmacy = new JsonArray();
                    pharmacy.add(getItem(position));
                    fm.beginTransaction()
                            .add(R.id.coordinatorLayout, BuscarFarmaciaFragment.newInstance(pharmacy, ACTION_SEE_RESULTS))
                            .addToBackStack(FRAGMENT_BUSCAR_FARMACIA).commitAllowingStateLoss();
                }else{
                    Intent abrirWebFarmaciaOnline = new Intent(Intent.ACTION_VIEW, Uri.parse(((JsonElement) urlObject).getAsString()));
                    getContext().startActivity(abrirWebFarmaciaOnline);
                }
            }
        });

        return convertView;
    }
}
