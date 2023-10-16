package com.orugga.yapp.fragments.recordatorios;


import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.orugga.yapp.AlarmBroadcastReceiver;
import com.orugga.yapp.LoginActivity;
import com.orugga.yapp.R;
import com.orugga.yapp.adapters.AutoCompleteBuscarProductosAdapter;
import com.orugga.yapp.database.RecordatorioToma;
import com.orugga.yapp.database.RecordatorioVisita;
import com.orugga.yapp.helpers.SessionHelper;
import com.orugga.yapp.interfaces.ActivityUpdateBackToolbar;
import com.orugga.yapp.interfaces.JsonArrayResponse;
import com.orugga.yapp.interfaces.JsonObjectResponse;
import com.orugga.yapp.interfaces.OnFragmentUpdateToolbarIntefrace;
import com.orugga.yapp.requests.SearchProductsRequest;

import org.joda.time.LocalDate;

import java.util.Calendar;
import java.util.regex.Pattern;

import static com.orugga.yapp.helpers.DateHelper.getCalendarDate;
import static com.orugga.yapp.helpers.DateHelper.getSimpleDateFormat;
import static com.orugga.yapp.helpers.IdleHelper.ocultarTeclado;
import static com.orugga.yapp.helpers.SessionHelper.getAccessToken;
import static com.orugga.yapp.helpers.SessionHelper.getUserName;
import static com.orugga.yapp.helpers.ViewHelper.getSelectionPosition;
import static com.orugga.yapp.requests.ReminderRequests.addTakeReminder;
import static com.orugga.yapp.requests.ReminderRequests.updateTakeReminder;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddRecordatorioTomaFragment extends Fragment implements OnFragmentUpdateToolbarIntefrace {


    //    private EditText mAutoCompleteBuscarProducto;
    private EditText mEditTextStartDate;
    private EditText mEditTextEndDate;
    private EditText mEditTextDosis;
    private EditText mEditTextNota;
    private EditText mEditTextRepurchaseDate;
    private Spinner mSpinnerStartTime;
    private Spinner mSpinnerPosologiaToma;
    private Spinner mSpinnerUnidad;

    private Calendar mStartDateCalendar = Calendar.getInstance();
    private Calendar mEndDateCalendar = Calendar.getInstance();
    private Calendar mRepurchaseDateCalendar = Calendar.getInstance();


    private AutoCompleteTextView mAutoCompleteBuscarProducto;
    private AutoCompleteBuscarProductosAdapter mAutoCompleteBuscarProductosAdapter;

    private int mProductId = -1;
    private String mCurrentProductNameSelection = "";

    private RecordatorioToma reminderToUpdate;

    private View mView;
    private AdapterView.OnItemClickListener mAutocompleteProductItemClickListener =
            new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    JsonObject product = mAutoCompleteBuscarProductosAdapter.getItem(position);
                    mCurrentProductNameSelection = product.get("full_name").getAsString();
                    mAutoCompleteBuscarProducto.setText(mCurrentProductNameSelection);
                    mProductId = product.get("id").getAsInt();
                    mEditTextDosis.requestFocus();
                    ocultarTeclado(getActivity());
                }
            };

    public AddRecordatorioTomaFragment() {
        // Required empty public constructor
    }

    public static AddRecordatorioTomaFragment newInstance() {
        return new AddRecordatorioTomaFragment();
    }

    public static AddRecordatorioTomaFragment newInstance(RecordatorioToma reminderToUpdate) {
        AddRecordatorioTomaFragment fragment = new AddRecordatorioTomaFragment();
        fragment.reminderToUpdate = reminderToUpdate;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.menu_item_add_reminder).setVisible(false);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_add_recordatorio_toma, container, false);

        mAutoCompleteBuscarProducto = mView.findViewById(R.id.autoCompleteBuscarProducto);
        mEditTextStartDate = mView.findViewById(R.id.editTextStartDate);
        mEditTextEndDate = mView.findViewById(R.id.editTextEndDate);
        mEditTextDosis = mView.findViewById(R.id.editTextDosis);
        mEditTextNota = mView.findViewById(R.id.editTextNota);
        mEditTextRepurchaseDate = mView.findViewById(R.id.editTextRepurchaseDate);
        mSpinnerStartTime = mView.findViewById(R.id.spinnerStartTime);
        mSpinnerPosologiaToma = mView.findViewById(R.id.spinnerPosologiaToma);
        mSpinnerUnidad = mView.findViewById(R.id.spinnerUnidad);

        ArrayAdapter<CharSequence> spinnerStartTimeAdapter = ArrayAdapter.createFromResource(getContext(), R.array.hora_list, android.R.layout.simple_spinner_item);
        spinnerStartTimeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerStartTime.setAdapter(spinnerStartTimeAdapter);

        ArrayAdapter<CharSequence> spinnerPosologiaTomaAdapter = ArrayAdapter.createFromResource(getContext(), R.array.posologia_toma_list, android.R.layout.simple_spinner_item);
        spinnerPosologiaTomaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerPosologiaToma.setAdapter(spinnerPosologiaTomaAdapter);

        ArrayAdapter<CharSequence> spinnerUnidadAdapter = ArrayAdapter.createFromResource(getContext(), R.array.unidad_list, android.R.layout.simple_spinner_item);
        spinnerUnidadAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerUnidad.setAdapter(spinnerUnidadAdapter);

        mEditTextStartDate.setText(getSimpleDateFormat().format(mStartDateCalendar.getTime()));
        mEditTextEndDate.setText(getSimpleDateFormat().format(mEndDateCalendar.getTime()));


        mEditTextStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        mStartDateCalendar = getCalendarDate(year, monthOfYear + 1, dayOfMonth);
                        mEditTextStartDate.setText(getSimpleDateFormat().format(mStartDateCalendar.getTime()));
                        if (new LocalDate(mStartDateCalendar).isAfter(new LocalDate(mEndDateCalendar))) {
                            mEndDateCalendar = mStartDateCalendar;
                            mEditTextEndDate.setText(getSimpleDateFormat().format(mEndDateCalendar.getTime()));
                        }
                    }
                },
                        mStartDateCalendar.get(Calendar.YEAR),
                        mStartDateCalendar.get(Calendar.MONTH),
                        mStartDateCalendar.get(Calendar.DAY_OF_MONTH)
                ).show();
            }
        });

        mEditTextEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatePickerDialog endDatePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        mEndDateCalendar = getCalendarDate(year, monthOfYear + 1, dayOfMonth);
                        mEditTextEndDate.setText(getSimpleDateFormat().format(mEndDateCalendar.getTime()));
                    }
                },
                        mEndDateCalendar.get(Calendar.YEAR),
                        mEndDateCalendar.get(Calendar.MONTH),
                        mEndDateCalendar.get(Calendar.DAY_OF_MONTH)
                );
                endDatePickerDialog.getDatePicker().setMinDate(mStartDateCalendar.getTimeInMillis());
                endDatePickerDialog.show();
            }
        });

        mEditTextRepurchaseDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog repurchaseDatePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        mRepurchaseDateCalendar = getCalendarDate(year, monthOfYear + 1, dayOfMonth);
                        mEditTextRepurchaseDate.setText(getSimpleDateFormat().format(mRepurchaseDateCalendar.getTime()));
                    }
                },
                        mRepurchaseDateCalendar.get(Calendar.YEAR),
                        mRepurchaseDateCalendar.get(Calendar.MONTH),
                        mRepurchaseDateCalendar.get(Calendar.DAY_OF_MONTH)
                );
//                repurchaseDatePickerDialog.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis());
                repurchaseDatePickerDialog.show();
            }
        });


        if (reminderToUpdate != null) {
            JsonObject reminderToUpdateJson = reminderToUpdate.getReminderJson();
            mProductId = reminderToUpdateJson.get("product_id").getAsInt();
            mCurrentProductNameSelection = reminderToUpdateJson.get("product_name").getAsString();
            mAutoCompleteBuscarProducto.setText(mCurrentProductNameSelection);
            String startDate = reminderToUpdateJson.get("date_start").getAsString();
            String startTime = reminderToUpdateJson.get("hour_to").getAsString();
            String[] startDateSplit = startDate.split("-");
            int startDay = Integer.valueOf(startDateSplit[2]);
            int startMonth = Integer.valueOf(startDateSplit[1]);
            int startYear = Integer.valueOf(startDateSplit[0]);
            mStartDateCalendar = getCalendarDate(startYear, startMonth, startDay);
            mEditTextStartDate.setText(getSimpleDateFormat().format(mStartDateCalendar.getTime()));
            String[] endDateSplit = reminderToUpdateJson.get("date_end").getAsString().split("-");
            int endDay = Integer.valueOf(endDateSplit[2]);
            int endMonth = Integer.valueOf(endDateSplit[1]);
            int endYear = Integer.valueOf(endDateSplit[0]);
            mEndDateCalendar = getCalendarDate(endYear, endMonth, endDay);
            mEditTextEndDate.setText(getSimpleDateFormat().format(mEndDateCalendar.getTime()));
            mSpinnerStartTime.setSelection(getSelectionPosition(mSpinnerStartTime, startTime.substring(0, 5)));
            mSpinnerPosologiaToma.setSelection(getSelectionPosition(mSpinnerPosologiaToma, reminderToUpdateJson.get("posology").getAsString() + " hs"));
            mEditTextDosis.setText(reminderToUpdateJson.get("dose").getAsString());
            mSpinnerUnidad.setSelection(getSelectionPosition(mSpinnerUnidad, reminderToUpdateJson.get("unity").getAsString()));
            mEditTextNota.setText(reminderToUpdateJson.get("note") == JsonNull.INSTANCE ? "" : reminderToUpdateJson.get("note").getAsString());
            if (reminderToUpdateJson.get("date_repurchase") != JsonNull.INSTANCE) {
                String[] repurchDateSplit = reminderToUpdateJson.get("date_repurchase").getAsString().split("-");
                int repurchDay = Integer.valueOf(repurchDateSplit[2]);
                int repurchMonth = Integer.valueOf(repurchDateSplit[1]);
                int repurchYear = Integer.valueOf(repurchDateSplit[0]);
                mRepurchaseDateCalendar = getCalendarDate(repurchYear, repurchMonth, repurchDay);
                mEditTextRepurchaseDate.setText(getSimpleDateFormat().format(mRepurchaseDateCalendar.getTime()));
            }
        }

        mView.findViewById(R.id.btnOk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ocultarTeclado(getActivity());
                if (isValidReminder()) {
                    if (reminderToUpdate != null) {
                        Answers.getInstance().logCustom(new CustomEvent("Alerta de Toma Detalle - Actualizar")
                                .putCustomAttribute("userName", getUserName(getContext())));
                        updateReminder();
                    } else {
                        Answers.getInstance().logCustom(new CustomEvent("Alerta de Toma Detalle - Crear")
                                .putCustomAttribute("userName", getUserName(getContext())));
                        createReminder();
                    }
                }
            }
        });


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
                    if (s.length() >= 3) {
                        buscarProducto(s.toString());
                    }
                    mProductId = -1;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

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


        return mView;
    }

    private boolean isValidReminder() {
        View focusView = null;
        boolean cancel = false;

        String note = mEditTextNota.getText().toString();
        if (!note.equals("") && !isNoteValid(note)) {
            cancel = true;
            focusView = mEditTextNota;
            mEditTextNota.setError(getString(R.string.error_invalid_note));
        }

        if (mEditTextDosis.getText().toString().equals("")) {
            cancel = true;
            focusView = mEditTextDosis;
            mEditTextDosis.setError(getString(R.string.error_field_required));
        }

        if (mAutoCompleteBuscarProducto.getText().toString().equals("")) {
            cancel = true;
            focusView = mAutoCompleteBuscarProducto;
            mAutoCompleteBuscarProducto.setError(getString(R.string.error_field_required));
        }

        if (!isProductValid()) {
            cancel = true;
            focusView = mAutoCompleteBuscarProducto;
            mAutoCompleteBuscarProducto.setError(getString(R.string.error_invalid_product));
        }


        if (!cancel) {
            return true;

        } else {
            focusView.requestFocus();
            return false;
        }
    }

    private void createReminder() {
        int posologiaMin = getPosologia();
        final ProgressDialog progressDialog = ProgressDialog.show(getContext(), "", getString(R.string.loading));
        addTakeReminder(getActivity(), SessionHelper.getAccessToken(getContext()), mCurrentProductNameSelection, mProductId, mEditTextStartDate.getText().toString(), mEditTextEndDate.getText().toString(),
                mSpinnerStartTime.getSelectedItem().toString(), posologiaMin, Float.valueOf(mEditTextDosis.getText().toString()), mSpinnerUnidad.getSelectedItem().toString(),
                mEditTextNota.getText().toString(), mEditTextRepurchaseDate.getText().toString(), new JsonObjectResponse() {
                    @Override
                    public void onSuccess(JsonObject response) {
                        progressDialog.dismiss();
                        RecordatorioToma recordatorioToma = new RecordatorioToma(response);
                        recordatorioToma.save();
                        AlarmBroadcastReceiver.setRecordatorioTomaAlarm(getContext(), recordatorioToma);
                        AlarmBroadcastReceiver.setRecordatorioRecompraAlarm(getContext(), recordatorioToma);
                        Toast.makeText(getContext(), R.string.reminder_saved, Toast.LENGTH_SHORT).show();
                        getActivity().getSupportFragmentManager().popBackStack();
                    }

                    @Override
                    public void onError(JsonObject response, Exception e) {
                        progressDialog.dismiss();
                        if (e != null) {
                            Crashlytics.logException(e);
                        } else if (response.get("rta") != null && !response.get("rta").isJsonNull() &&
                                response.get("rta").getAsString().equals("Unauthorised")) {
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
                });
    }

    private void updateReminder() {
        int posologiaMin = getPosologia();
        final ProgressDialog progressDialog = ProgressDialog.show(getContext(), "", getString(R.string.loading));
        updateTakeReminder(getActivity(), SessionHelper.getAccessToken(getContext()), reminderToUpdate.getReminderJson().get("id").getAsLong(), mCurrentProductNameSelection, mProductId, mEditTextStartDate.getText().toString(), mEditTextEndDate.getText().toString(),
                mSpinnerStartTime.getSelectedItem().toString(), posologiaMin, Float.valueOf(mEditTextDosis.getText().toString()), mSpinnerUnidad.getSelectedItem().toString(),
                mEditTextNota.getText().toString(), mEditTextRepurchaseDate.getText().toString(), new JsonObjectResponse() {
                    @Override
                    public void onSuccess(JsonObject response) {
                        progressDialog.dismiss();
                        reminderToUpdate.setReminderJson(response);
                        reminderToUpdate.setRepurchaseDateNotified(false);
                        reminderToUpdate.save();
                        AlarmBroadcastReceiver.cancelTomaAlarm(getContext(), reminderToUpdate);
                        AlarmBroadcastReceiver.cancelRecompraAlarm(getContext(), reminderToUpdate);
                        AlarmBroadcastReceiver.setRecordatorioTomaAlarm(getContext(), reminderToUpdate);
                        AlarmBroadcastReceiver.setRecordatorioRecompraAlarm(getContext(), reminderToUpdate);
                        Toast.makeText(getContext(), R.string.reminder_saved, Toast.LENGTH_SHORT).show();
                        getActivity().getSupportFragmentManager().popBackStack();
                    }

                    @Override
                    public void onError(JsonObject response, Exception e) {
                        if (getActivity() == null || getActivity().isFinishing()) return;
                        progressDialog.dismiss();
                        if (e != null) {
                            Crashlytics.logException(e);
                        } else if (response.get("rta") != null && !response.get("rta").isJsonNull() &&
                                response.get("rta").getAsString().equals("Unauthorised")) {
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
                });
    }

    private int getPosologia() {
        switch (mSpinnerPosologiaToma.getSelectedItem().toString()) {
            case "4 hs":
                return 4;
            case "8 hs":
                return 8;
            case "12 hs":
                return 12;
            case "24 hs":
                return 24;
            default:
                return 4;
        }
    }

    private void buscarProducto(final String producto) {
        SearchProductsRequest.fetch(getActivity(), getAccessToken(getActivity()), producto, new JsonArrayResponse() {
            @Override
            public void onSuccess(JsonArray response) {
                mAutoCompleteBuscarProductosAdapter.setResultsFromJson(response, producto);
            }

            @Override
            public void onError(JsonObject response, Exception e) {
                Toast.makeText(getActivity(), "Error en Buscar Productos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isProductValid() {
        return mProductId != -1;
    }

    private boolean isNoteValid(String note) {
        return Pattern.compile("[a-zA-Z0-9\\s\\¿\\?\\¡\\(\\)\\!\\@\\#\\$\\-\\+\\_\\*]{1,256}").matcher(note).matches();
    }

    @Override
    public void onFragmentUpdateToolbar() {
        getActivity().setTitle(R.string.action_bar_title_add_recordatorio_de_toma);
        if (getActivity() instanceof ActivityUpdateBackToolbar) {
            ((ActivityUpdateBackToolbar) getActivity()).showBackNavigationIcon();
        }
    }
}
