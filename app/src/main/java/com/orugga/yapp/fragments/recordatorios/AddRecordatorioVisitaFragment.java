package com.orugga.yapp.fragments.recordatorios;


import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.orugga.yapp.AlarmBroadcastReceiver;
import com.orugga.yapp.LoginActivity;
import com.orugga.yapp.R;
import com.orugga.yapp.database.RecordatorioToma;
import com.orugga.yapp.database.RecordatorioVisita;
import com.orugga.yapp.helpers.SessionHelper;
import com.orugga.yapp.interfaces.ActivityUpdateBackToolbar;
import com.orugga.yapp.interfaces.JsonObjectResponse;
import com.orugga.yapp.interfaces.OnFragmentUpdateToolbarIntefrace;

import java.util.Calendar;
import java.util.regex.Pattern;

import static com.orugga.yapp.helpers.DateHelper.getCalendarDate;
import static com.orugga.yapp.helpers.DateHelper.getSimpleDateFormat;
import static com.orugga.yapp.helpers.IdleHelper.ocultarTeclado;
import static com.orugga.yapp.helpers.SessionHelper.getUserName;
import static com.orugga.yapp.helpers.ViewHelper.getSelectionPosition;
import static com.orugga.yapp.requests.ReminderRequests.addVisitReminder;
import static com.orugga.yapp.requests.ReminderRequests.updateVisitReminder;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddRecordatorioVisitaFragment extends Fragment implements OnFragmentUpdateToolbarIntefrace {


    private EditText mEditTextDoctorName;
    private EditText mEditTextDoctorSpecialty;
    private EditText mEditTextDate;
    private EditText mEditTextNota;
    private Spinner mSpinnerTime;

    private Calendar mDateCalendar = Calendar.getInstance();

    private RecordatorioVisita reminderToUpdate;

    public AddRecordatorioVisitaFragment() {
        // Required empty public constructor
    }

    public static AddRecordatorioVisitaFragment newInstance() {
        return new AddRecordatorioVisitaFragment();
    }

    public static AddRecordatorioVisitaFragment newInstance(RecordatorioVisita reminderToUpdate) {
        AddRecordatorioVisitaFragment fragment = new AddRecordatorioVisitaFragment();
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
        View view = inflater.inflate(R.layout.fragment_add_recordatorio_visita, container, false);

        mEditTextDoctorName = view.findViewById(R.id.editTextDoctorName);
        mEditTextDoctorSpecialty = view.findViewById(R.id.editTextEspecialidad);
        mEditTextDate = view.findViewById(R.id.editTextDate);
        mEditTextNota = view.findViewById(R.id.editTextNota);
        mSpinnerTime = view.findViewById(R.id.spinnerTime);

        ArrayAdapter<CharSequence> spinnerTimeAdapter = ArrayAdapter.createFromResource(getContext(), R.array.hora_list, android.R.layout.simple_spinner_item);
        spinnerTimeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ((Spinner) view.findViewById(R.id.spinnerTime)).setAdapter(spinnerTimeAdapter);

        mEditTextDate.setText(getSimpleDateFormat().format(Calendar.getInstance().getTime()));
        mEditTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        mDateCalendar = getCalendarDate(year, monthOfYear + 1, dayOfMonth);
                        mEditTextDate.setText(getSimpleDateFormat().format(mDateCalendar.getTime()));
                    }
                },
                        mDateCalendar.get(Calendar.YEAR),
                        mDateCalendar.get(Calendar.MONTH),
                        mDateCalendar.get(Calendar.DAY_OF_MONTH)
                );
                datePickerDialog.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis());
                datePickerDialog.show();
            }
        });


        if (reminderToUpdate != null) {
            JsonObject reminderToUpdateJson = reminderToUpdate.getReminderJson();
            mEditTextDoctorName.setText(reminderToUpdateJson.get("doctor_name").getAsString());
            mEditTextDoctorSpecialty.setText(reminderToUpdateJson.get("specialty").getAsString());
            String[] dateSplit = reminderToUpdateJson.get("date").getAsString().split("-");
            int day = Integer.valueOf(dateSplit[2]);
            int month = Integer.valueOf(dateSplit[1]);
            int year = Integer.valueOf(dateSplit[0]);
            mDateCalendar = getCalendarDate(year, month, day);
            mEditTextDate.setText(getSimpleDateFormat().format(mDateCalendar.getTime()));
            mSpinnerTime.setSelection(getSelectionPosition(mSpinnerTime, reminderToUpdateJson.get("hour").getAsString().substring(0, 5)));
            mEditTextNota.setText(reminderToUpdateJson.get("note") != JsonNull.INSTANCE ? reminderToUpdateJson.get("note").getAsString() : "");
        }
        view.findViewById(R.id.btnOk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ocultarTeclado(getActivity());
                if (isValidReminder()) {
                    if (reminderToUpdate != null) {
                        Answers.getInstance().logCustom(new CustomEvent("Visita médico detalle - Actualizar")
                                .putCustomAttribute("userName", getUserName(getContext())));
                        updateReminder();
                    } else {
                        Answers.getInstance().logCustom(new CustomEvent("Visita médico detalle - Agregar")
                                .putCustomAttribute("userName", getUserName(getContext())));
                        createReminder();
                    }
                }
            }
        });


        return view;
    }

    private void createReminder() {
        final ProgressDialog progressDialog = ProgressDialog.show(getContext(), "", getString(R.string.loading));
        addVisitReminder(getActivity(), SessionHelper.getAccessToken(getContext()), mEditTextDoctorName.getText().toString(), mEditTextDoctorSpecialty.getText().toString(),
                mEditTextDate.getText().toString(), mSpinnerTime.getSelectedItem().toString(), mEditTextNota.getText().toString(), new JsonObjectResponse() {
                    @Override
                    public void onSuccess(JsonObject response) {
                        progressDialog.dismiss();
                        RecordatorioVisita recordatorioVisita = new RecordatorioVisita(response);
                        recordatorioVisita.save();
                        AlarmBroadcastReceiver.setRecordatorioVisitaAlarma(getContext(), recordatorioVisita);
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

    private void updateReminder() {
        final ProgressDialog progressDialog = ProgressDialog.show(getContext(), "", getString(R.string.loading));
        updateVisitReminder(getActivity(), SessionHelper.getAccessToken(getContext()), reminderToUpdate.getReminderJson().get("id").getAsLong(), mEditTextDoctorName.getText().toString(), mEditTextDoctorSpecialty.getText().toString(),
                mEditTextDate.getText().toString(), mSpinnerTime.getSelectedItem().toString(), mEditTextNota.getText().toString(), new JsonObjectResponse() {
                    @Override
                    public void onSuccess(JsonObject response) {
                        progressDialog.dismiss();
                        reminderToUpdate.setReminderJson(response);
                        reminderToUpdate.save();
                        AlarmBroadcastReceiver.cancelVisitaAlarm(getContext(), reminderToUpdate);
                        AlarmBroadcastReceiver.setRecordatorioVisitaAlarma(getContext(), reminderToUpdate);
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


    private boolean isValidReminder() {
        View focusView = null;
        boolean cancel = false;

        String note = mEditTextNota.getText().toString();
        if (!note.equals("") && !isNoteValid(note)) {
            cancel = true;
            focusView = mEditTextNota;
            mEditTextNota.setError(getString(R.string.error_invalid_note));
        }

        if (mEditTextDoctorSpecialty.getText().toString().equals("")) {
            cancel = true;
            focusView = mEditTextDoctorSpecialty;
            mEditTextDoctorSpecialty.setError(getString(R.string.error_field_required));
        }

        String doctorsName = mEditTextDoctorName.getText().toString();
        if (!doctorsName.equals("") && !isNameValid(doctorsName)) {
            cancel = true;
            focusView = mEditTextDoctorName;
            mEditTextDoctorName.setError(getString(R.string.error_invalid_name));
        }

        if (!cancel) {
            return true;

        } else {
            focusView.requestFocus();
            return false;
        }
    }


    private boolean isNameValid(String name) {
        return Pattern.compile("[a-zA-Z0-9\\s]{1,256}").matcher(name).matches();
    }

    private boolean isNoteValid(String note) {
        return Pattern.compile("[a-zA-Z0-9\\s\\¿\\?\\¡\\(\\)\\!\\@\\#\\$\\-\\+\\_\\*]{1,256}").matcher(note).matches();
    }

    @Override
    public void onFragmentUpdateToolbar() {
        getActivity().setTitle(R.string.action_bar_title_add_recordatorio_de_visita);
        if (getActivity() instanceof ActivityUpdateBackToolbar) {
            ((ActivityUpdateBackToolbar) getActivity()).showBackNavigationIcon();
        }
    }
}
