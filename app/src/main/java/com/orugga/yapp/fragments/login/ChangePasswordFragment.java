package com.orugga.yapp.fragments.login;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.orugga.yapp.R;
import com.orugga.yapp.interfaces.ActivityUpdateBackToolbar;
import com.orugga.yapp.interfaces.JsonObjectResponse;
import com.orugga.yapp.interfaces.OnFragmentUpdateToolbarIntefrace;

import java.util.regex.Pattern;

import static com.orugga.yapp.helpers.SessionHelper.getAccessToken;
import static com.orugga.yapp.requests.PasswordRequests.updatePassword;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChangePasswordFragment extends Fragment implements OnFragmentUpdateToolbarIntefrace {

    private EditText mCurrentPassword;
    private EditText mNewPassword;
    private EditText mConfirmNewPassword;

    public static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "[a-zA-Z0-9\\!\\@\\#\\$\\-\\+\\_\\*]{3,24}"
    );

    public ChangePasswordFragment() {
        // Required empty public constructor
    }

    public static ChangePasswordFragment newInstance() {
        return new ChangePasswordFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_password, container, false);
        mCurrentPassword = (EditText) view.findViewById(R.id.editTxtContraseñaActual);
        mNewPassword = (EditText) view.findViewById(R.id.editTxtContraseñaNueva);
        mConfirmNewPassword = (EditText) view.findViewById(R.id.editTxtConfirmContraseñaNueva);

        view.findViewById(R.id.btnChangePassword).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptChangePassword();
            }
        });
        return view;
    }

    private void attemptChangePassword() {
        // Reset errors.
        mCurrentPassword.setError(null);
        mNewPassword.setError(null);
        mConfirmNewPassword.setError(null);

        boolean cancel = false;
        View focusView = null;

        String currentPassword = mCurrentPassword.getText().toString();
        String newPassword = mNewPassword.getText().toString();
        String confirmNewPassword = mConfirmNewPassword.getText().toString();

        if (currentPassword.length() < 4) {
            cancel = true;
            focusView = mCurrentPassword;
            mCurrentPassword.setError(getString(R.string.error_weak_password));
        }  else if (!isPasswordValid(currentPassword)) {
            cancel = true;
            focusView = mCurrentPassword;
            mCurrentPassword.setError(getString(R.string.error_password_invalid));
        }
        if (newPassword.length() < 4) {
            cancel = true;
            focusView = mNewPassword;
            mNewPassword.setError(getString(R.string.error_weak_password));
        } else if (!isPasswordValid(newPassword)){
            cancel = true;
            focusView = mNewPassword;
            mNewPassword.setError(getString(R.string.error_password_invalid));
        }
        if (confirmNewPassword.length() < 4) {
            cancel = true;
            focusView = mConfirmNewPassword;
            mConfirmNewPassword.setError(getString(R.string.error_weak_password));
        } else if (!isPasswordValid(confirmNewPassword)){
            cancel = true;
            focusView = mConfirmNewPassword;
            mConfirmNewPassword.setError(getString(R.string.error_password_invalid));
        } else if (!newPassword.equals(confirmNewPassword)){
            cancel = true;
            focusView = mConfirmNewPassword;
            mConfirmNewPassword.setError(getString(R.string.error_passwords_not_match));
        }

        if (cancel){
            focusView.requestFocus();
        } else {
            updatePassword(getContext(), getAccessToken(getContext()), currentPassword, newPassword, confirmNewPassword, new JsonObjectResponse() {
                @Override
                public void onSuccess(JsonObject response) {
                    getActivity().getSupportFragmentManager().popBackStack();
                    Toast.makeText(getContext(), R.string.toast_password_updated, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(JsonObject response, Exception e) {
                    if (response != null && response.get("data") != JsonNull.INSTANCE) {
                        try {
                            String errorMessage = response.get("data").getAsString();
                            if (errorMessage.equals("El password actual no es correcto.")) {
                                mCurrentPassword.setError(getString(R.string.error_message_incorrect_password));
                            }
                        } catch (UnsupportedOperationException ex){
                            ex.printStackTrace();
                        }
                    }
                    Toast.makeText(getContext(), R.string.error_message_internet_conection, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private boolean isPasswordValid(String password) {
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    @Override
    public void onFragmentUpdateToolbar() {
        getActivity().setTitle(getString(R.string.action_bar_title_change_password));
        if (getActivity() instanceof ActivityUpdateBackToolbar) {
            ((ActivityUpdateBackToolbar) getActivity()).showBackNavigationIcon();
        }
    }
}
