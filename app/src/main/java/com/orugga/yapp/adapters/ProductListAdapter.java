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
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.orugga.yapp.MainHomeActivity;
import com.orugga.yapp.R;
import com.orugga.yapp.interfaces.JsonObjectResponse;
import com.orugga.yapp.requests.DownloadImageRequest;

import static com.orugga.yapp.Constants.ApiFields.PRODUCT_FULL_NAME;
import static com.orugga.yapp.Constants.ApiFields.PRODUCT_LABS;
import static com.orugga.yapp.Constants.ApiFields.PRODUCT_LABS_NAME;
import static com.orugga.yapp.Constants.ApiFields.PRODUCT_PICTURE;
import static com.orugga.yapp.Constants.REQUESTS_ACTIONS.ACTION_ADD;
import static com.orugga.yapp.Constants.REQUESTS_ACTIONS.ACTION_DELETE;
import static com.orugga.yapp.helpers.SessionHelper.getAccessToken;
import static com.orugga.yapp.helpers.SessionHelper.isUserLogedIn;
import static com.orugga.yapp.requests.FavouritesRequests.AddDeleteFavouriteProduct;


/**
 * Created by Alexis on 27/10/2017.
 */

public class ProductListAdapter extends ArrayAdapter<JsonObject> {


    private Activity mActivity;
    private FragmentManager fm;

    public ProductListAdapter(@NonNull Activity activity, FragmentManager fm, int resource) {
        super(activity, resource);
        mActivity = activity;
        this.fm = fm;
    }

    @Nullable
    @Override
    public JsonObject getItem(int position) {
        return super.getItem(position);
    }


    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mActivity.getApplicationContext()).inflate(R.layout.product_item, parent, false);
        }
        final JsonObject product = getItem(position);
        ImageView imgProductPhoto = convertView.findViewById(R.id.imgProduct);
        TextView txtProductFullName = convertView.findViewById(R.id.txtProductName);
        TextView txtProductActivePrinciple = convertView.findViewById(R.id.txtActivePrinciple);
        TextView txtProductLab = convertView.findViewById(R.id.txtProductLab);
        ImageView btnDelete = convertView.findViewById(R.id.btnDelete);
        final CheckBox btnAddToFavourite = convertView.findViewById(R.id.btnAddToFavourites);
//        final LinearLayout btnVerBeneficioEnProgramaPaciente = convertView.findViewById(R.id.btnVerBeneficioEnProgramaPaciente);

        if (!product.get(PRODUCT_PICTURE).isJsonNull())
            DownloadImageRequest.downloadImage(getContext(),
                    product.get("full_path_image_400").getAsString(),
                    imgProductPhoto,
                    R.drawable.placeholder_producto);
        txtProductFullName.setText(product.get(PRODUCT_FULL_NAME).getAsString());
        txtProductActivePrinciple.setText(product.getAsJsonObject("active_principles").get("name").getAsString());
        txtProductLab.setText(product.getAsJsonObject(PRODUCT_LABS).get(PRODUCT_LABS_NAME).getAsString());

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                remove(getItem(position));
                notifyDataSetChanged();
            }
        });

        btnAddToFavourite.setChecked(product.get("is_favorite").getAsBoolean());
        btnAddToFavourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isChecked = btnAddToFavourite.isChecked();
                if (isUserLogedIn(getContext())) {
                    if (isChecked) {
                        AddDeleteFavouriteProduct(getContext(), getAccessToken(getContext()), product.get("id").getAsLong(), ACTION_ADD, new JsonObjectResponse() {
                            @Override
                            public void onSuccess(JsonObject response) {
                                product.remove("is_favorite");
                                product.addProperty("is_favorite", true);
                            }

                            @Override
                            public void onError(JsonObject response, Exception e) {
                                Toast.makeText(mActivity.getApplicationContext(), R.string.error_message_internet_conection, Toast.LENGTH_SHORT).show();
                                btnAddToFavourite.setChecked(false);
                            }
                        });
                    } else {
                        AddDeleteFavouriteProduct(getContext(), getAccessToken(getContext()), product.get("id").getAsLong(), ACTION_DELETE, new JsonObjectResponse() {
                            @Override
                            public void onSuccess(JsonObject response) {
                                product.remove("is_favorite");
                                product.addProperty("is_favorite", false);
                            }

                            @Override
                            public void onError(JsonObject response, Exception e) {
                                Toast.makeText(mActivity.getApplicationContext(), R.string.error_message_internet_conection, Toast.LENGTH_SHORT).show();
                                btnAddToFavourite.setChecked(true);
                            }
                        });
                    }
                } else {
                    btnAddToFavourite.setChecked(false);
                    ((MainHomeActivity) mActivity).needHaveAnAccount(mActivity.getString(R.string.must_be_loged_to_have_favs));
                }
            }
        });

        /*final JsonArray pacientPrograms = product.getAsJsonArray("pacient_programs");
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

    @Override
    public int getPosition(@Nullable JsonObject item) {
        return super.getPosition(item);
    }
}
