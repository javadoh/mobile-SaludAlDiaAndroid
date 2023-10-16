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
import com.google.gson.JsonNull;
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

import static com.orugga.yapp.Constants.FragmentsNames.FRAGMENT_AGREGAR_RECORDATORIO_TOMA;
import static com.orugga.yapp.helpers.SessionHelper.isUserLogedIn;
import static com.orugga.yapp.requests.ReminderRequests.deleteTakeReminder;

/**
 * A simple {@link Fragment} subclass.
 */
public class MisRecordatoriosTomaFragment extends Fragment implements OnFragmentUpdateToolbarIntefrace {

    private MisRecordatoriosTomaAdapter mMisRecordatoriosTomaAdapter;
    private List<RecordatorioToma> mMisRecordatoriosToma;

    public MisRecordatoriosTomaFragment() {
        // Required empty public constructor
    }

    public static MisRecordatoriosTomaFragment newInstance() {
        return new MisRecordatoriosTomaFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.simple_add_reminder_menu, menu);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ListView listView = (ListView) inflater.inflate(R.layout.fragment_mis_recordatorios, container, false);
        mMisRecordatoriosToma = RecordatorioToma.listAll(RecordatorioToma.class);
        mMisRecordatoriosTomaAdapter = new MisRecordatoriosTomaAdapter(getContext(), mMisRecordatoriosToma);
        listView.setAdapter(mMisRecordatoriosTomaAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Answers.getInstance().logCustom(new CustomEvent("Alerta de Toma - Ver detalle"));
                getActivity().getSupportFragmentManager().beginTransaction()
                        .add(R.id.coordinatorLayout, AddRecordatorioTomaFragment.newInstance(mMisRecordatoriosTomaAdapter.getItem(position)))
                        .addToBackStack(FRAGMENT_AGREGAR_RECORDATORIO_TOMA).commitAllowingStateLoss();
            }
        });
        return listView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_item_add_reminder) {
            if (isUserLogedIn(getContext())) {
                Answers.getInstance().logCustom(new CustomEvent("Alertas de Toma - Agregar"));
                getActivity().getSupportFragmentManager().beginTransaction()
                    .add(R.id.coordinatorLayout, AddRecordatorioTomaFragment.newInstance())
                    .addToBackStack(FRAGMENT_AGREGAR_RECORDATORIO_TOMA).commitAllowingStateLoss();
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
        getActivity().setTitle(R.string.action_bar_title_mis_recordatorios_de_toma);
        if (getActivity() instanceof ActivityUpdateBackToolbar) {
            ((ActivityUpdateBackToolbar) getActivity()).showBackNavigationIcon();
        }
        mMisRecordatoriosToma = RecordatorioToma.listAll(RecordatorioToma.class);
        mMisRecordatoriosTomaAdapter.setNewContent(mMisRecordatoriosToma);
        if (mMisRecordatoriosTomaAdapter != null) {
            mMisRecordatoriosTomaAdapter.notifyDataSetChanged();
        }
    }

    private class MisRecordatoriosTomaAdapter extends ArrayAdapter<RecordatorioToma> {

        private List<RecordatorioToma> mRecordatoriosToma;

        MisRecordatoriosTomaAdapter(@NonNull Context context, List<RecordatorioToma> recordatoriosToma) {
            super(context, R.layout.pacient_program_item);
            this.mRecordatoriosToma = recordatoriosToma;
        }

        void setNewContent(List<RecordatorioToma> recordatorioTomas) {
            this.mRecordatoriosToma = recordatorioTomas;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mRecordatoriosToma.size();
        }

        @Override
        public RecordatorioToma getItem(int position) {
            return mRecordatoriosToma.get(position);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).getReminderJson().get("id").getAsLong();
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
            if (convertView == null)
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.recordatorio_toma_list_item, parent, false);

            JsonObject recordatorioToma = getItem(position).getReminderJson();

            ((TextView) convertView.findViewById(R.id.txtMedicineName)).setText(recordatorioToma.get("product_name").getAsString());
            ((TextView) convertView.findViewById(R.id.txtMedicineDosisAndUnity)).setText(
                    new StringBuilder()
                            .append(recordatorioToma.get("dose").getAsString()).append(" ")
                            .append(recordatorioToma.get("unity").getAsString()).toString()
            );
            ((TextView) convertView.findViewById(R.id.txtReminderNote)).setText(recordatorioToma.get("note") == JsonNull.INSTANCE ? "" : recordatorioToma.get("note").getAsString());
            convertView.findViewById(R.id.btnDeleteRow).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(getContext()).setMessage("¿Está seguro de querer eliminar este recordatorio?")
                            .setPositiveButton(R.string.dialog_option_yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    final long reminderId = getItemId(position);
                                    deleteTakeReminder(getActivity(), SessionHelper.getAccessToken(getContext()), reminderId, new JsonObjectResponse() {
                                        @Override
                                        public void onSuccess(JsonObject response) {
                                            RecordatorioToma.findById(RecordatorioToma.class, getItem(position).getId()).delete();
                                            setNewContent(RecordatorioToma.listAll(RecordatorioToma.class));
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
