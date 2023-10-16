package com.orugga.yapp.adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.orugga.yapp.MainHomeActivity;
import com.orugga.yapp.R;
import com.orugga.yapp.fragments.BuscarProgramaPacienteFragment;
import com.orugga.yapp.interfaces.JsonObjectResponse;
import com.orugga.yapp.requests.DownloadImageRequest;

import java.text.NumberFormat;
import java.util.Locale;

import static com.orugga.yapp.Constants.ApiFields.PRODUCT_ACTIVE_PRINCIPLES;
import static com.orugga.yapp.Constants.ApiFields.PRODUCT_ACTIVE_PRINCIPLES_NAME;
import static com.orugga.yapp.Constants.ApiFields.PRODUCT_FULL_NAME;
import static com.orugga.yapp.Constants.ApiFields.PRODUCT_ID;
import static com.orugga.yapp.Constants.ApiFields.PRODUCT_LABS;
import static com.orugga.yapp.Constants.ApiFields.PRODUCT_LABS_NAME;
import static com.orugga.yapp.Constants.FragmentsNames.FRAGMENT_DETALLE_PROGRAMA_PACIENTE;
import static com.orugga.yapp.Constants.REQUESTS_ACTIONS.ACTION_ADD;
import static com.orugga.yapp.Constants.REQUESTS_ACTIONS.ACTION_DELETE;
import static com.orugga.yapp.helpers.SessionHelper.getAccessToken;
import static com.orugga.yapp.helpers.SessionHelper.isUserLogedIn;
import static com.orugga.yapp.requests.FavouritesRequests.AddDeleteFavouriteProduct;

/**
 * Created by Alexis on 06/12/2017.
 */

public class ResultProductDetailsAdapter extends ArrayAdapter<JsonObject> {

    private JsonArray mProducts;
    private FragmentManager fm;
    private Activity mActivity;

    public ResultProductDetailsAdapter(@NonNull Activity activity, FragmentManager fm, JsonArray products) {
        super(activity.getApplicationContext(), R.layout.result_product_details_item);
        this.mActivity = activity;
        this.mProducts = products;
        this.fm = fm;
    }

    @Override
    public int getCount() {
        return mProducts.size();
    }

    @Nullable
    @Override
    public JsonObject getItem(int position) {
        return mProducts.get(position).getAsJsonObject();
    }

    @Override
    public long getItemId(int position) {
        return mProducts.get(position).getAsJsonObject().get(PRODUCT_ID).getAsLong();
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.result_product_details_item, parent, false);
        final JsonObject product = getItem(position);
        DownloadImageRequest.downloadImage(getContext(),
                product.get("full_path_image_400").getAsString(),
                (ImageView) convertView.findViewById(R.id.imgProduct),
                R.drawable.placeholder_producto);

        ((TextView) convertView.findViewById(R.id.txtProductName)).setText(product.get(PRODUCT_FULL_NAME).getAsString());
        ((TextView) convertView.findViewById(R.id.txtActivePrinciple)).setText(product.getAsJsonObject(PRODUCT_ACTIVE_PRINCIPLES).get(PRODUCT_ACTIVE_PRINCIPLES_NAME).getAsString());
        ((TextView) convertView.findViewById(R.id.txtProductLab)).setText(product.get(PRODUCT_LABS).getAsJsonObject().get(PRODUCT_LABS_NAME).getAsString());
        NumberFormat format = NumberFormat.getNumberInstance(Locale.GERMAN);
        format.setMaximumFractionDigits(0);
        String precioRegular = "$" + format.format(product.get("regular_price").getAsDouble());
        String precioConDescuento = "$" + format.format(product.get("discount_price").getAsDouble());
        ((TextView) convertView.findViewById(R.id.txtPrecioRegular)).setText(precioRegular);
        if (precioConDescuento.equals(precioRegular)) {
            convertView.findViewById(R.id.imgTachar).setVisibility(View.INVISIBLE);
            convertView.findViewById(R.id.lblPrecioConDescuento).setVisibility(View.INVISIBLE);
            convertView.findViewById(R.id.txtPrecioConDescuento).setVisibility(View.INVISIBLE);
        } else {
            ((TextView) convertView.findViewById(R.id.txtPrecioConDescuento)).setText(precioConDescuento);
        }
        StringBuilder dtoSources = new StringBuilder();
        JsonElement healthDto = getItem(position).get("healthInsurance");
        JsonElement dealDto = getItem(position).get("deals");
        if (healthDto.isJsonNull() && dealDto.isJsonNull()) {
            dtoSources.append("");
        } else if (dealDto != JsonNull.INSTANCE) {
            dtoSources.append("ocupando convenio ").append(dealDto.getAsJsonObject().get("name").getAsString());
        } else if (healthDto != JsonNull.INSTANCE) {
            dtoSources.append("ocupando seguro ").append(healthDto.getAsJsonObject().get("name").getAsString());
        }
        ((TextView) convertView.findViewById(R.id.txtDtoSources)).setText(dtoSources.toString());

        final CheckBox btnAddToFavourites = convertView.findViewById(R.id.btnAddToFavourites);
        btnAddToFavourites.setChecked(product.get("is_favorite").getAsBoolean());
        btnAddToFavourites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isUserLogedIn(getContext())) {
                    boolean isChecked = btnAddToFavourites.isChecked();
                    if (isChecked) {
                        AddDeleteFavouriteProduct(getContext(), getAccessToken(getContext()), product.get("id").getAsLong(), ACTION_ADD, new JsonObjectResponse() {
                            @Override
                            public void onSuccess(JsonObject response) {
                            }

                            @Override
                            public void onError(JsonObject response, Exception e) {
                                Toast.makeText(getContext(), R.string.error_message_internet_conection, Toast.LENGTH_SHORT).show();
                                btnAddToFavourites.setChecked(false);
                            }
                        });
                    } else {
                        AddDeleteFavouriteProduct(getContext(), getAccessToken(getContext()), product.get("id").getAsLong(), ACTION_DELETE, new JsonObjectResponse() {
                            @Override
                            public void onSuccess(JsonObject response) {
                            }

                            @Override
                            public void onError(JsonObject response, Exception e) {
                                Toast.makeText(getContext(), R.string.error_message_internet_conection, Toast.LENGTH_SHORT).show();
                                btnAddToFavourites.setChecked(true);
                            }
                        });
                    }
                } else {
                    btnAddToFavourites.setChecked(false);
                    ((MainHomeActivity) mActivity).needHaveAnAccount(getContext().getString(R.string.must_be_loged_to_have_favs));
                }
            }
        });
        /*final LinearLayout btnVerBeneficioEnProgramaPaciente = convertView.findViewById(R.id.btnVerBeneficioEnProgramaPaciente);
        final JsonArray pacientPrograms = product.getAsJsonArray("pacient_programs");
        if (pacientPrograms.size() > 0) {
            btnVerBeneficioEnProgramaPaciente.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    JsonObject pacientProgram = pacientPrograms.get(0).getAsJsonObject();
                    fm.beginTransaction()
                            .add(R.id.coordinatorLayout, BuscarProgramaPacienteFragment.newInstance(product))
                            .addToBackStack(FRAGMENT_DETALLE_PROGRAMA_PACIENTE).commitAllowingStateLoss();
                }
            });
        } else {
            btnVerBeneficioEnProgramaPaciente.setVisibility(View.GONE);
        }*/


        return convertView;
    }


}
