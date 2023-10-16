package com.orugga.yapp.fragments.login;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.orugga.yapp.AlarmBroadcastReceiver;
import com.orugga.yapp.LoginActivity;
import com.orugga.yapp.MainHomeActivity;
import com.orugga.yapp.R;
import com.orugga.yapp.database.RecordatorioToma;
import com.orugga.yapp.database.RecordatorioVisita;
import com.orugga.yapp.helpers.SessionHelper;
import com.orugga.yapp.interfaces.ActivityUpdateBackToolbar;
import com.orugga.yapp.interfaces.JsonObjectResponse;
import com.orugga.yapp.interfaces.OnFragmentUpdateToolbarIntefrace;

import java.util.ArrayList;

import static com.orugga.yapp.Constants.IntentFilters.ACTION_CREATE;
import static com.orugga.yapp.Constants.IntentFilters.ACTION_UPDATE;
import static com.orugga.yapp.requests.UpdateUserRequest.updateUser;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileDealsFragment extends Fragment
        implements OnFragmentUpdateToolbarIntefrace {


    private JsonObject mUserData;
    private JsonObject mRegisterData;
    private Bundle mUserChanges;

    private DealsAdapter mDealsAdapter;

    private int mAction;

    public ProfileDealsFragment() {
        // Required empty public constructor
    }

    public static ProfileDealsFragment newInstance(JsonObject userData, JsonObject registerData, Bundle userChanges, int mAction) {
        ProfileDealsFragment fragment = new ProfileDealsFragment();
        Bundle args = new Bundle();
        args.putString("userData", userData.toString());
        args.putString("registerData", registerData.toString());
        args.putBundle("userChanges", userChanges);
        args.putInt("action", mAction);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUserData = new JsonParser().parse(getArguments().getString("userData")).getAsJsonObject();
            mRegisterData = new JsonParser().parse(getArguments().getString("registerData")).getAsJsonObject();
            mUserChanges = getArguments().getBundle("userChanges");
            mAction = getArguments().getInt("action");
        }
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_deals, container, false);
        ListView listViewDeals = view.findViewById(R.id.listViewDeals);
        mDealsAdapter = new DealsAdapter(getContext(), createDealCheckBoxArray(mRegisterData.getAsJsonArray("deals")));
        if (mAction == ACTION_UPDATE) {
            setDeals();
        }
        listViewDeals.setAdapter(mDealsAdapter);

        if (mAction == ACTION_UPDATE) {
            setDeals();
            view.findViewById(R.id.btnOk).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Answers.getInstance().logCustom(new CustomEvent("Perfil de descuentos - Actualiza"));
                    attempt();
                }
            });
        } else {
            view.findViewById(R.id.btnOk).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Answers.getInstance().logCustom(new CustomEvent("Perfil de descuentos - Siguiente"));
                    final Dialog dialog = new Dialog(getActivity(), R.style.CustomDialog);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.popup_create_account);
                    dialog.findViewById(R.id.btn_close_dialog).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (dialog != null && dialog.isShowing())
                                dialog.dismiss();
                            attempt();
                        }
                    });
                    dialog.show();
                }
            });
        }

        return view;
    }

    private void attempt() {
        ArrayList<Long> deals = mDealsAdapter.getAllCheckedIds();
        if (deals.size() != 0) {
            if (deals.size() == 1 && deals.get(0) == 5) {
                Answers.getInstance().logCustom(new CustomEvent("Seleccionar convenios - Sin convenios"));
            } else {
                Answers.getInstance().logCustom(new CustomEvent("Seleccionar convenios - Con convenios"));
            }
            final ProgressDialog progressDialog = ProgressDialog.show(getContext(), "", getString(R.string.loading));
            String apiToken = mUserData.get("api_token").getAsString();
            String email = mUserData.get("email").getAsString();
            String name = mUserData.get("name").getAsString();
            String birthDay = mUserChanges.getString("birth_day").equals("") ? null : mUserChanges.getString("birth_day");
            String gender = mUserChanges.getString("gender");
            long region = mUserChanges.getLong("region");
            long comunne = mUserChanges.getLong("comunne");
            long healthInsurances = mUserChanges.getLong("health_insurance");
            updateUser(getActivity(), apiToken, email, name, "" /*lastName*/, birthDay, gender, region, comunne,
                    healthInsurances, deals,
                    new JsonObjectResponse() {
                        @Override
                        public void onSuccess(JsonObject userData) {
                            progressDialog.dismiss();
                            if (mAction == ACTION_CREATE) {
                                SessionHelper.createSession(getContext(), userData.get("api_token").getAsString(), userData);
                                startActivity(new Intent(getActivity(), MainHomeActivity.class));
                                getActivity().finish();
                            } else {
                                SessionHelper.setUser(getContext(), userData);
                                FragmentManager sfm = getActivity().getSupportFragmentManager();
                                sfm.popBackStack();
                                sfm.popBackStack();
                                sfm.popBackStack();
                                Toast.makeText(getContext(), R.string.user_updated, Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(JsonObject response, Exception e) {
                            if (getActivity() == null || getActivity().isFinishing()) return;
                            progressDialog.dismiss();
                            if (e != null) {
                                Crashlytics.logException(e);
                            } else if (response != null) {
                                if (response.get("error") != null && response.get("error") != JsonNull.INSTANCE) {
                                    Toast.makeText(getContext(), response.get("error").toString(), Toast.LENGTH_SHORT).show();
                                } else {
                                    if (response.get("rta").getAsString().equals("Unauthorised")) {
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
                            } else {
                                Toast.makeText(getContext(), R.string.error_message_internet_conection, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            Toast.makeText(getContext(), R.string.must_select_at_least_one_deal, Toast.LENGTH_SHORT).show();
        }
    }

    private void setDeals() {
        JsonArray deals = mUserData.getAsJsonArray("deals");
        for (int i = 0; i < deals.size(); i++) {
            JsonObject deal = deals.get(i).getAsJsonObject();
            mDealsAdapter.getItem(getDealPosition(deal.get("id").getAsInt())).mChecked = true;
        }
    }

    private int getDealPosition(int id) {
        int i = 0;
        while (i < mDealsAdapter.getCount() && mDealsAdapter.getItem(i).mDeal.get("id").getAsInt() != id) {
            i++;
        }
        return i == mDealsAdapter.getCount() ? 0 : i;
    }

    private ArrayList<DealCheckBox> createDealCheckBoxArray(JsonArray deals) {
        ArrayList<DealCheckBox> dealCheckBoxes = new ArrayList<>();
        for (int i = 0; i < deals.size(); i++) {
            dealCheckBoxes.add(new DealCheckBox(deals.get(i).getAsJsonObject(), false));
        }
        return dealCheckBoxes;
    }

    @Override
    public void onFragmentUpdateToolbar() {
        if (getActivity() instanceof ActivityUpdateBackToolbar) {
            ((ActivityUpdateBackToolbar) getActivity()).showBackNavigationIcon();
        }
        getActivity().setTitle(getString(R.string.action_bar_title_perfil_de_descuentos));
    }

    private class DealsAdapter extends ArrayAdapter<DealCheckBox> {

        private DealsAdapter(Context context, ArrayList<DealCheckBox> dealCheckBoxes) {
            super(context, R.layout.deal_selector_item, dealCheckBoxes);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).mDeal.get("id").getAsLong();
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null)
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.deal_selector_item, parent, false);
            CheckBox dealCheckBox = convertView.findViewById(R.id.chkConvenio);
            final DealCheckBox currentCheckBox = getItem(position);
            dealCheckBox.setText(currentCheckBox.mDeal.get("name").getAsString());
            dealCheckBox.setChecked(currentCheckBox.mChecked);
            dealCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!currentCheckBox.mChecked) {
                        if (currentCheckBox.mDeal.get("id").getAsInt() == 5) {
                            unCheckAll();
                        } else {
                            setCheckSinConvenio(false);
                        }
                        currentCheckBox.mChecked = !currentCheckBox.mChecked;
                    } else {
                        currentCheckBox.mChecked = !currentCheckBox.mChecked;
                        if (getAllCheckedIds().size() == 0) {
                            setCheckSinConvenio(true);
                        }
                    }
                }
            });
            return convertView;
        }

        private void unCheckAll() {
            for (int i = 0; i < getCount(); i++) {
                DealCheckBox dealCheckBox = getItem(i);
                dealCheckBox.mChecked = false;
            }
            notifyDataSetChanged();
        }

        private void setCheckSinConvenio(boolean check) {
            for (int i = 0; i < getCount(); i++) {
                DealCheckBox dealCheckBox = getItem(i);
                if (dealCheckBox.mDeal.get("id").getAsInt() == 5)
                    dealCheckBox.mChecked = check;
            }
            notifyDataSetChanged();
        }

        private ArrayList<Long> getAllCheckedIds() {
            ArrayList<Long> ids = new ArrayList<>();
            for (int i = 0; i < getCount(); i++) {
                if (getItem(i).mChecked) {
                    ids.add(getItemId(i));
                }
            }
            return ids;
        }
    }

    private class DealCheckBox {
        private JsonObject mDeal;
        private boolean mChecked;

        private DealCheckBox(JsonObject deal, boolean checked) {
            this.mDeal = deal;
            this.mChecked = checked;
        }
    }
}
