package com.orugga.yapp.fragments.login;


import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.makeramen.roundedimageview.RoundedImageView;
import com.orugga.yapp.R;
import com.orugga.yapp.helpers.DateHelper;
import com.orugga.yapp.interfaces.ActivityUpdateBackToolbar;
import com.orugga.yapp.interfaces.OnFragmentUpdateToolbarIntefrace;
import com.orugga.yapp.objects.FixedHoloDatePickerDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static com.orugga.yapp.Constants.FragmentsNames.FRAGMENT_PROFILE_DEALS;
import static com.orugga.yapp.Constants.IntentFilters.ACTION_UPDATE;
import static com.orugga.yapp.requests.DownloadImageRequest.downloadProfilePhoto;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment implements OnFragmentUpdateToolbarIntefrace {

    private CheckBox chkFonasa;
    private CheckBox chkIsapre;
    private Spinner spnRegion;
    private Spinner spnComuna;
    private Spinner spnMyIsapre;
    private RegisterSpinnerAdapter comunasAdapter;
    private CheckBox mMale;
    private CheckBox mFemale;
    private EditText mFechaNacimiento;
    private Calendar myCalendar = Calendar.getInstance();
    DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateLabel();
        }

    };
    private JsonObject mUser;
    private JsonObject mData;
    private int mAction;
    private boolean settingSpinners = false;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance(JsonObject user, JsonObject data, int mAction) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString("data", data.toString());
        args.putString("user", user.toString());
        args.putInt("action", mAction);
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mData = new JsonParser().parse(getArguments().getString("data")).getAsJsonObject();
            mUser = new JsonParser().parse(getArguments().getString("user")).getAsJsonObject();
            mAction = getArguments().getInt("action");
        }
        setRetainInstance(true);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        chkFonasa = view.findViewById(R.id.checkFonasa);
        chkIsapre = view.findViewById(R.id.checkIsapre);
        spnComuna = view.findViewById(R.id.spinnerComuna);
        spnRegion = view.findViewById(R.id.spinnerRegion);
        spnMyIsapre = view.findViewById(R.id.spinnerSeleccioneIsapre);
        RoundedImageView mProfileImage = view.findViewById(R.id.imgAvatar);
        mFechaNacimiento = view.findViewById(R.id.txtFechaNacimiento);
        mMale = view.findViewById(R.id.checkboxMale);
        mFemale = view.findViewById(R.id.checkboxFemale);

        ((TextView) view.findViewById(R.id.txtNombre)).setText(
                mUser.get("name").getAsString()
        );
        ((TextView) view.findViewById(R.id.txtEmail)).setText(mUser.get("email").getAsString());

        mFechaNacimiento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                @SuppressWarnings("deprecation")
                final Context themedContext = new ContextThemeWrapper(
                        getContext(),
                        android.R.style.Theme_Holo_Light_Dialog
                );

                final DatePickerDialog dialog = new FixedHoloDatePickerDialog(
                        themedContext,
                        datePickerListener,
                        myCalendar.get(Calendar.YEAR),
                        myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)
                );
                dialog.getDatePicker().setMaxDate(Calendar.getInstance().getTimeInMillis());
                dialog.show();
            }
        });

        View.OnClickListener chkGenderListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == mMale) {
                    if (mMale.isChecked())
                        mFemale.setChecked(false);
                } else {
                    if (mFemale.isChecked())
                        mMale.setChecked(false);
                }
            }
        };
        mMale.setOnClickListener(chkGenderListener);
        mFemale.setOnClickListener(chkGenderListener);

        final JsonArray regions = mData.getAsJsonArray("regions");
        RegisterSpinnerAdapter regionsAdapter = new RegisterSpinnerAdapter(inflater, regions, "name", "Region");
        comunasAdapter = new RegisterSpinnerAdapter(inflater, new JsonArray(), "name", "Comuna");
        final JsonArray isapreHealtInsurances = mData.getAsJsonArray("healthInsurances").get(1).getAsJsonObject().getAsJsonArray("health_insurances");
        RegisterSpinnerAdapter isapresAdapter = new RegisterSpinnerAdapter(inflater, isapreHealtInsurances, "name", "Seleccione su ISAPRE");

        spnComuna.setAdapter(comunasAdapter);
        spnRegion.setAdapter(regionsAdapter);
        spnMyIsapre.setAdapter(isapresAdapter);

        spnRegion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (settingSpinners) {
                    settingSpinners = false;
                    return;
                }
                if (position != 0) {
                    comunasAdapter.setItems(regions.get(position - 1).getAsJsonObject().getAsJsonArray("comunnes"));
                } else {
                    comunasAdapter.setItems(new JsonArray());
                }
                spnComuna.setSelection(0);
            }
        });

        chkFonasa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spnMyIsapre.setVisibility(View.INVISIBLE);
                chkIsapre.setChecked(false);
            }
        });

        chkIsapre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (chkIsapre.isChecked()) {
                    spnMyIsapre.setVisibility(View.VISIBLE);
                } else {
                    spnMyIsapre.setVisibility(View.INVISIBLE);
                }
                chkFonasa.setChecked(false);
            }
        });

        view.findViewById(R.id.btnCreateAccount).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() == null || getActivity().isFinishing()) return;
                if (isHealthInsuranceSelected()) {
                    if (isGenderSelected()) {
                        if (isIsapreWellSelected()) {
                            Bundle userChanges = createBundle();
                            getActivity().getSupportFragmentManager().beginTransaction()
                                    .add(R.id.coordinatorLayout, ProfileDealsFragment.newInstance(mUser, mData, userChanges, mAction))
                                    .addToBackStack(FRAGMENT_PROFILE_DEALS).commitAllowingStateLoss();
                        } else {
                            Toast.makeText(getActivity(), R.string.toast_error_isapre_not_selected, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), R.string.must_select_a_gender, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), R.string.must_select_a_healthinsurance, Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (mUser.get("picture") != JsonNull.INSTANCE)
            downloadProfilePhoto(getContext(), mUser.get("picture").getAsString(), mProfileImage, R.drawable.avatar);
        if (mAction == ACTION_UPDATE) {
            if (mUser.get("birth_day") != JsonNull.INSTANCE) {
                String date = mUser.get("birth_day").getAsString();
                String[] dateSplit = date.split("-");
                myCalendar = DateHelper.getCalendarDate(Integer.valueOf(dateSplit[0]), Integer.valueOf(dateSplit[1]), Integer.valueOf(dateSplit[2]));
                updateDateLabel();
            }
            if (mUser.get("gender") != JsonNull.INSTANCE) {
                String gender = mUser.get("gender").getAsString();
                if (gender.equals("M")) {
                    mMale.setChecked(true);
                } else if (gender.equals("F")) {
                    mFemale.setChecked(true);
                }
            }
            if (mUser.get("region_id") != JsonNull.INSTANCE) {
                int position = getItemPosition(regionsAdapter.mData, mUser.get("region_id").getAsInt());
                spnRegion.setSelection(position + 1);
                comunasAdapter.setItems(regions.get(position).getAsJsonObject().getAsJsonArray("comunnes"));
                if (mUser.get("comunne_id") != JsonNull.INSTANCE) {
                    spnComuna.setSelection(getItemPosition(comunasAdapter.mData, mUser.get("comunne_id").getAsInt()) + 1);
                    settingSpinners = true;
                }
            }
            if (mUser.get("healthinsurances").getAsJsonArray().size() > 0) {
                JsonObject healthInsurance = mUser.get("healthinsurances").getAsJsonArray().get(0).getAsJsonObject();
                if (healthInsurance.get("name").getAsString().equals("FONASA")) {
                    chkFonasa.setChecked(true);
                } else {
                    chkIsapre.setChecked(true);
                    spnMyIsapre.setVisibility(View.VISIBLE);
                    JsonObject isapres = mData.getAsJsonArray("healthInsurances").get(1).getAsJsonObject();
                    spnMyIsapre.setSelection(getItemPosition(isapres.getAsJsonArray("health_insurances"), healthInsurance.get("id").getAsInt()) + 1);
                }
            }
        }

        return view;
    }

    private void updateDateLabel() {
        String myFormat = "dd-MM-yy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        mFechaNacimiento.setText(sdf.format(myCalendar.getTime()));
    }

    private int getItemPosition(JsonArray array, int id) {
        for (int i = 0; i < array.size(); i++) {
            JsonObject data = array.get(i).getAsJsonObject();
            if (data.get("id").getAsInt() == id)
                return i;
        }
        return -1;
    }

    private boolean isIsapreWellSelected() {
        return !(chkIsapre.isChecked() && spnMyIsapre.getSelectedItemId() == -1);
    }

    private boolean isHealthInsuranceSelected() {
        return chkFonasa.isChecked() || chkIsapre.isChecked();
    }

    private boolean isGenderSelected() {
        return mFemale.isChecked() || mMale.isChecked();
    }

    private Bundle createBundle() {
        Bundle userBundle = new Bundle();
        if (!mFechaNacimiento.getText().toString().isEmpty()) {
            String myFormat = "yyyy-MM-dd";
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
            userBundle.putString("birth_day", sdf.format(myCalendar.getTime()));
        } else {
            userBundle.putString("birth_day", "");
        }
        userBundle.putString("gender", getGender());
        userBundle.putLong("region", spnRegion.getSelectedItemId());
        userBundle.putLong("comunne", spnComuna.getSelectedItemId());
        if (chkIsapre.isChecked()) {
            userBundle.putLong("health_insurance", spnMyIsapre.getSelectedItemId());
        } else if (chkFonasa.isChecked()) {
            userBundle.putLong("health_insurance", 1);
        } else {
            userBundle.putLong("health_insurance", -1);
        }
        return userBundle;
    }

    private String getGender() {
        if (mMale.isChecked()) {
            return "M";
        } else if (mFemale.isChecked()) {
            return "F";
        } else {
            return null;
        }
    }

    @Override
    public void onFragmentUpdateToolbar() {
        if (getActivity() == null || getActivity().isFinishing()) return;
        if (getActivity() instanceof ActivityUpdateBackToolbar) {
            ((ActivityUpdateBackToolbar) getActivity()).showBackNavigationIcon();
        }
        getActivity().setTitle(getString(R.string.action_bar_title_perfil_de_descuentos));
    }

    public static class RegisterSpinnerAdapter extends BaseAdapter implements SpinnerAdapter {
        private JsonArray mData;

        private String mPropertyName;
        private String mInitialSelection;

        private LayoutInflater mLayoutInflater;

        public RegisterSpinnerAdapter(LayoutInflater layoutInflater, JsonArray data, String propertyName, String initialSelection) {
            this.mData = data;
            this.mLayoutInflater = layoutInflater;
            this.mPropertyName = propertyName;
            this.mInitialSelection = initialSelection;
        }

        @Override
        public int getCount() {
            return mData.size() + 1;
        }

        @Override
        public String getItem(int position) {
            if (position != 0) {
                return mData.get(position - 1).getAsJsonObject().get(mPropertyName).getAsString();
            } else {
                return mInitialSelection;
            }
        }

        @Override
        public long getItemId(int position) {
            if (position != 0) {
                return mData.get(position - 1).getAsJsonObject().get("id").getAsLong();
            } else {
                return -1;
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = mLayoutInflater.inflate(R.layout.support_simple_spinner_dropdown_item, parent, false);
            TextView text = convertView.findViewById(android.R.id.text1);
            text.setText(getItem(position));
            return convertView;
        }

        void setItems(JsonArray items) {
            mData = items;
            notifyDataSetChanged();
        }

    }
}
