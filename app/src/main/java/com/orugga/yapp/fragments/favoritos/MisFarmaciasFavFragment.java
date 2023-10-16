package com.orugga.yapp.fragments.favoritos;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.orugga.yapp.R;
import com.orugga.yapp.fragments.BuscarFarmaciaFragment;
import com.orugga.yapp.interfaces.ActivityUpdateBackToolbar;
import com.orugga.yapp.interfaces.JsonObjectResponse;
import com.orugga.yapp.interfaces.OnFragmentUpdateToolbarIntefrace;

import static com.orugga.yapp.Constants.ApiFields.PHARMACY_LATITUDE;
import static com.orugga.yapp.Constants.ApiFields.PHARMACY_LONGITUDE;
import static com.orugga.yapp.Constants.FragmentsNames.FRAGMENT_BUSCAR_FARMACIA;
import static com.orugga.yapp.Constants.IntentFilters.ACTION_SEE_PHARMACY;
import static com.orugga.yapp.Constants.REQUESTS_ACTIONS.ACTION_DELETE;
import static com.orugga.yapp.helpers.SessionHelper.getAccessToken;
import static com.orugga.yapp.requests.DownloadImageRequest.downloadImage;
import static com.orugga.yapp.requests.FavouritesRequests.AddDeleteFavouritePharmacy;

/**
 * A simple {@link Fragment} subclass.
 */
public class MisFarmaciasFavFragment extends Fragment implements OnFragmentUpdateToolbarIntefrace {


    private JsonArray farmaciasFavoritas;
    private View mView;

    public MisFarmaciasFavFragment() {
        // Required empty public constructor
    }

    public static MisFarmaciasFavFragment newInstance(JsonArray farmaciasFavoritas) {
        MisFarmaciasFavFragment fragment = new MisFarmaciasFavFragment();
        Bundle args = new Bundle();
        args.putString("farmacias", farmaciasFavoritas.toString());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
            farmaciasFavoritas = new JsonParser().parse(getArguments().getString("farmacias")).getAsJsonArray();
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_mis_farmacias_fav, container, false);
        ((TextView) mView.findViewById(R.id.cantFarmaciasFavoritas)).setText(new StringBuilder().append("(").append(farmaciasFavoritas.size()).append(" guardados)").toString());
        ListView favouritesPharmaciesList = mView.findViewById(R.id.listMisFarmaciasFav);
        final FarmaciasFavoritosAdapter farmaciasFavoritosAdapter = new FarmaciasFavoritosAdapter(getContext());
        favouritesPharmaciesList.setAdapter(farmaciasFavoritosAdapter);
        favouritesPharmaciesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                JsonObject pharmacy = farmaciasFavoritosAdapter.getItem(position);
                JsonArray pharmacies = new JsonArray();
                pharmacies.add(pharmacy);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .add(R.id.coordinatorLayout, BuscarFarmaciaFragment.newInstance(pharmacies,
                                new LatLng(pharmacy.get(PHARMACY_LATITUDE).getAsDouble(), pharmacy.get(PHARMACY_LONGITUDE).getAsDouble()),
                                ACTION_SEE_PHARMACY))
                        .addToBackStack(FRAGMENT_BUSCAR_FARMACIA).commitAllowingStateLoss();
            }
        });

        return mView;
    }

    @Override
    public void onFragmentUpdateToolbar() {
        if (getActivity() instanceof ActivityUpdateBackToolbar) {
            ((ActivityUpdateBackToolbar) getActivity()).showBackNavigationIcon();
        }
    }


    private class FarmaciasFavoritosAdapter extends ArrayAdapter<JsonObject> {

        private FarmaciasFavoritosAdapter(Context context) {
            super(context, R.layout.farmacia_fav_item);
        }

        @Override
        public int getCount() {
            return farmaciasFavoritas.size();
        }

        @Nullable
        @Override
        public JsonObject getItem(int position) {
            return farmaciasFavoritas.get(position).getAsJsonObject();
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).get("id").getAsLong();
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null)
                convertView = getLayoutInflater().inflate(R.layout.farmacia_fav_item, parent, false);
            final JsonObject pharmacy = getItem(position);
            downloadImage(getContext(), pharmacy.getAsJsonObject("pharmacychain").get("full_path_logo_400").getAsString(), (ImageView) convertView.findViewById(R.id.imgPharmacyPhoto), R.drawable.placeholder_farmacia);
            ((TextView) convertView.findViewById(R.id.txtPharmacyName)).setText(pharmacy.get("name").getAsString());
            ((TextView) convertView.findViewById(R.id.txtPharmacyAddress)).setText(pharmacy.get("address").getAsString());
            convertView.findViewById(R.id.btnDelete).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AddDeleteFavouritePharmacy(getContext(), getAccessToken(getContext()), pharmacy.get("id").getAsLong(), ACTION_DELETE, new JsonObjectResponse() {
                        @Override
                        public void onSuccess(JsonObject response) {
                            farmaciasFavoritas.remove(position);
                            notifyDataSetChanged();
                            ((TextView) mView.findViewById(R.id.cantFarmaciasFavoritas)).setText(new StringBuilder().append("(").append(farmaciasFavoritas.size()).append(" guardados)").toString());
                        }

                        @Override
                        public void onError(JsonObject response, Exception e) {
                            Toast.makeText(getContext(), R.string.error_message_internet_conection, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

            return convertView;
        }
    }
}
