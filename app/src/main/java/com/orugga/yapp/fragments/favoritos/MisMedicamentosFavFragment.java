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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.orugga.yapp.R;
import com.orugga.yapp.fragments.BuscarProductosFragment;
import com.orugga.yapp.interfaces.ActivityUpdateBackToolbar;
import com.orugga.yapp.interfaces.JsonObjectResponse;
import com.orugga.yapp.interfaces.OnFragmentUpdateToolbarIntefrace;

import static com.orugga.yapp.Constants.FragmentsNames.FRAGMENT_BUSCAR_PRODUCTO;
import static com.orugga.yapp.Constants.REQUESTS_ACTIONS.ACTION_DELETE;
import static com.orugga.yapp.helpers.SessionHelper.getAccessToken;
import static com.orugga.yapp.requests.DownloadImageRequest.downloadImage;
import static com.orugga.yapp.requests.FavouritesRequests.AddDeleteFavouriteProduct;

/**
 * A simple {@link Fragment} subclass.
 */
public class MisMedicamentosFavFragment extends Fragment implements OnFragmentUpdateToolbarIntefrace {

    private JsonArray medicamentosFavoritos;
    private View mView;

    public MisMedicamentosFavFragment() {
        // Required empty public constructor
    }

    public static MisMedicamentosFavFragment newInstance(JsonArray productosFavoritos) {
        MisMedicamentosFavFragment fragment = new MisMedicamentosFavFragment();
        fragment.medicamentosFavoritos = productosFavoritos;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_mis_medicamentos_fav, container, false);

        ((TextView) mView.findViewById(R.id.cantMedicamentosFavoritos)).setText(new StringBuilder().append("(").append(medicamentosFavoritos.size()).append(" guardados)").toString());
        ListView favouritesProductsList = mView.findViewById(R.id.listMisMedicamentosFav);
        final MedicamentosFavoritosAdapter medicamentosFavoritosAdapter = new MedicamentosFavoritosAdapter(getContext());
        favouritesProductsList.setAdapter(medicamentosFavoritosAdapter);
        favouritesProductsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                JsonArray products = new JsonArray();
                products.add(medicamentosFavoritosAdapter.getItem(position));
                getActivity().getSupportFragmentManager().beginTransaction()
                        .add(R.id.coordinatorLayout, BuscarProductosFragment.newInstance(products))
                        .addToBackStack(FRAGMENT_BUSCAR_PRODUCTO).commitAllowingStateLoss();
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

    private class MedicamentosFavoritosAdapter extends ArrayAdapter<JsonObject> {

        private MedicamentosFavoritosAdapter(Context context) {
            super(context, R.layout.medicamento_fav_item);
        }

        @Override
        public int getCount() {
            return medicamentosFavoritos.size();
        }

        @Nullable
        @Override
        public JsonObject getItem(int position) {
            return medicamentosFavoritos.get(position).getAsJsonObject();
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).get("id").getAsLong();
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null)
                convertView = getLayoutInflater().inflate(R.layout.medicamento_fav_item, parent, false);
            final JsonObject product = getItem(position);
            downloadImage(getContext(), product.get("full_path_image_400").getAsString(), (ImageView) convertView.findViewById(R.id.imgProduct), R.drawable.placeholder_producto);
            ((TextView) convertView.findViewById(R.id.txtProductName)).setText(product.get("full_name").getAsString());
            ((TextView) convertView.findViewById(R.id.txtProductLab)).setText(product.getAsJsonObject("labs").get("name").getAsString());
            ((TextView) convertView.findViewById(R.id.txtActivePrinciple)).setText(product.getAsJsonObject("active_principles").get("name").getAsString());
            convertView.findViewById(R.id.btnDelete).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AddDeleteFavouriteProduct(getContext(), getAccessToken(getContext()), product.get("id").getAsLong(), ACTION_DELETE, new JsonObjectResponse() {
                        @Override
                        public void onSuccess(JsonObject response) {
                            medicamentosFavoritos.remove(position);
                            notifyDataSetChanged();
                            ((TextView) mView.findViewById(R.id.cantMedicamentosFavoritos)).setText(new StringBuilder().append("(").append(medicamentosFavoritos.size()).append(" guardados)").toString());
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
