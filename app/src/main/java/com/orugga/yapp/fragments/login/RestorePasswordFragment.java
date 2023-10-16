package com.orugga.yapp.fragments.login;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.orugga.yapp.R;
import com.orugga.yapp.interfaces.ActivityUpdateBackToolbar;
import com.orugga.yapp.interfaces.JsonObjectResponse;
import com.orugga.yapp.interfaces.OnFragmentUpdateToolbarIntefrace;

import static com.orugga.yapp.requests.PasswordRequests.restorePassword;


/**
 * A simple {@link Fragment} subclass.
 */
public class RestorePasswordFragment extends Fragment implements OnFragmentUpdateToolbarIntefrace {


    public RestorePasswordFragment() {
        // Required empty public constructor
    }

    public static RestorePasswordFragment newInstance() {
        return new RestorePasswordFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_restore_password, container, false);
        final EditText editTextEmail = view.findViewById(R.id.editTxtEmail);
        view.findViewById(R.id.btnSendPassword).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = editTextEmail.getText().toString();
                if (TextUtils.isEmpty(email)) {
                    editTextEmail.requestFocus();
                    editTextEmail.setError(getString(R.string.error_field_required));
                } else if (isEmailValid(email)) {
                    final ProgressDialog dialog = ProgressDialog.show(getContext(), null, getString(R.string.loading));
                    restorePassword(getActivity(), email, new JsonObjectResponse() {
                        @Override
                        public void onSuccess(JsonObject response) {
                            dialog.dismiss();
                            Toast.makeText(getContext(), R.string.password_restored_confirmation, Toast.LENGTH_SHORT).show();
                            getActivity().getSupportFragmentManager().popBackStack();
                        }

                        @Override
                        public void onError(JsonObject response, Exception e) {
                            dialog.dismiss();
                            if (response != null) {
                                Toast.makeText(getContext(), response.get("error").getAsString(), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), R.string.error_message_internet_conection, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    editTextEmail.requestFocus();
                    editTextEmail.setError(getString(R.string.error_invalid_email));
                }
            }
        });
        return view;
    }

    private boolean isEmailValid(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }


    @Override
    public void onFragmentUpdateToolbar() {
        if (getActivity() instanceof ActivityUpdateBackToolbar) {
            ((ActivityUpdateBackToolbar) getActivity()).showBackNavigationIcon();
        }
        getActivity().setTitle(getString(R.string.action_bar_title_restablecer_contraseña));
    }
}
