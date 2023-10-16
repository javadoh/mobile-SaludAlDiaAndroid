package com.orugga.yapp.fragments.recordatorios;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.google.gson.JsonObject;
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

import java.util.List;

import static com.orugga.yapp.Constants.FragmentsNames.FRAGMENT_AGREGAR_RECORDATORIO_VISITA;
import static com.orugga.yapp.helpers.DateHelper.getDisplayiableDate;
import static com.orugga.yapp.helpers.SessionHelper.isUserLogedIn;
import static com.orugga.yapp.requests.ReminderRequests.deleteVisitReminder;

/**
 * A simple {@link Fragment} subclass.
 */
public class MisRecordatoriosVisitaFragment extends Fragment implements OnFragmentUpdateToolbarIntefrace{

    private List<RecordatorioVisita> mMisRecordatoriosVisita;

    private MisRecordatoriosVisitaAdapter mMisRecordatoriosVisitaAdapter;

    public MisRecordatoriosVisitaFragment() {
        // Required empty public constructor
    }

    public static MisRecordatoriosVisitaFragment newInstance() {
        return new MisRecordatoriosVisitaFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ListView listView = (ListView) inflater.inflate(R.layout.fragment_mis_recordatorios, container, false);

        mMisRecordatoriosVisita = RecordatorioVisita.listAll(RecordatorioVisita.class);
        mMisRecordatoriosVisitaAdapter = new MisRecordatoriosVisitaAdapter(getContext(), mMisRecordatoriosVisita);
        listView.setAdapter(mMisRecordatoriosVisitaAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Answers.getInstance().logCustom(new CustomEvent("Visita al médico - Ver detalle"));
                getActivity().getSupportFragmentManager().beginTransaction()
                        .add(R.id.coordinatorLayout, AddRecordatorioVisitaFragment.newInstance(mMisRecordatoriosVisitaAdapter.getItem(position)))
                        .addToBackStack(FRAGMENT_AGREGAR_RECORDATORIO_VISITA).commitAllowingStateLoss();
            }
        });

        return listView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.simple_add_reminder_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_item_add_reminder){
            if (isUserLogedIn(getContext())) {
                Answers.getInstance().logCustom(new CustomEvent("Visita al médico - Agregar"));
                getActivity().getSupportFragmentManager().beginTransaction()
                        .add(R.id.coordinatorLayout, AddRecordatorioVisitaFragment.newInstance())
                        .addToBackStack(FRAGMENT_AGREGAR_RECORDATORIO_VISITA).commitAllowingStateLoss();
            } else {
                ((MainHomeActivity) getActivity()).needHaveAnAccount(getString(R.string.must_be_loged_to_create_reminder));
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onFragmentUpdateToolbar() {
        getActivity().setTitle(R.string.action_bar_title_mis_recordatorios_de_visita);
        if (getActivity() instanceof ActivityUpdateBackToolbar){
            ((ActivityUpdateBackToolbar) getActivity()).showBackNavigationIcon();
        }
        mMisRecordatoriosVisita = RecordatorioVisita.listAll(RecordatorioVisita.class);
        mMisRecordatoriosVisitaAdapter.setNewContent(mMisRecordatoriosVisita);
        if (mMisRecordatoriosVisitaAdapter != null){
            mMisRecordatoriosVisitaAdapter.notifyDataSetChanged();
        }
    }



    private class MisRecordatoriosVisitaAdapter extends ArrayAdapter<RecordatorioVisita> {

        private List<RecordatorioVisita> mRecordatoriosVisita;

        MisRecordatoriosVisitaAdapter(@NonNull Context context, List<RecordatorioVisita> recordatorioVisitas) {
            super(context, R.layout.pacient_program_item);
            this.mRecordatoriosVisita = recordatorioVisitas;
        }

        void setNewContent(List<RecordatorioVisita> recordatorioVisitas) {
            this.mRecordatoriosVisita = recordatorioVisitas;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mRecordatoriosVisita.size();
        }

        @Override
        public RecordatorioVisita getItem(int position) {
            return mRecordatoriosVisita.get(position);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).getReminderJson().get("id").getAsLong();
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
            if (convertView == null)
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.recordatorio_visita_list_item, parent, false);

            JsonObject recordatorioVisita = getItem(position).getReminderJson();

            ((TextView) convertView.findViewById(R.id.txtDoctorName)).setText(recordatorioVisita.get("doctor_name").getAsString());
            ((TextView) convertView.findViewById(R.id.txtEspecialidad)).setText(recordatorioVisita.get("specialty").getAsString());
            String date = recordatorioVisita.get("date").getAsString();
            ((TextView) convertView.findViewById(R.id.txtDate)).setText(getDisplayiableDate(date));
            ((TextView) convertView.findViewById(R.id.txtTime)).setText(new StringBuilder().append(recordatorioVisita.get("hour").getAsString()).append(" hs").toString());
            convertView.findViewById(R.id.btnDeleteRow).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(getContext()).setMessage("¿Está seguro de querer eliminar este recordatorio?")
                            .setPositiveButton(R.string.dialog_option_yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    final long reminderId = getItemId(position);
                                    deleteVisitReminder(getActivity(), SessionHelper.getAccessToken(getContext()), reminderId, new JsonObjectResponse() {
                                        @Override
                                        public void onSuccess(JsonObject response) {
                                            RecordatorioVisita.findById(RecordatorioVisita.class, getItem(position).getId()).delete();
                                            setNewContent(RecordatorioVisita.listAll(RecordatorioVisita.class));
                                        }

                                        @Override
                                        public void onError(JsonObject response, Exception e) {
                                            if (getActivity() == null || getActivity().isFinishing()) return;
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
                            }).show();
                }
            });
            return convertView;
        }

    }
}
