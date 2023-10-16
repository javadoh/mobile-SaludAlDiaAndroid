package com.orugga.yapp.fragments.login;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.crashlytics.android.answers.LoginEvent;
import com.google.gson.JsonObject;
import com.orugga.yapp.AlarmBroadcastReceiver;
import com.orugga.yapp.MainHomeActivity;
import com.orugga.yapp.R;
import com.orugga.yapp.helpers.SessionHelper;
import com.orugga.yapp.interfaces.ActivityUpdateBackToolbar;
import com.orugga.yapp.interfaces.JsonObjectResponse;
import com.orugga.yapp.interfaces.OnFragmentUpdateToolbarIntefrace;
import com.orugga.yapp.requests.LoginUserRequest;

import static com.orugga.yapp.Constants.ANSWERS_CONSTANTS.CONTENT_TYPE_VIEW;
import static com.orugga.yapp.Constants.ANSWERS_CONSTANTS.VIEW_FORGOT_PASSWORD_ID;
import static com.orugga.yapp.Constants.FragmentsNames.FRAGMENT_RECUPERAR_CONTRASEÑA;
import static com.orugga.yapp.Constants.FragmentsNames.FRAGMENT_REGISTER_ACCOUNT;
import static com.orugga.yapp.Constants.IntentFilters.ACTION_CREATE;
import static com.orugga.yapp.helpers.SessionHelper.createReminders;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment implements OnFragmentUpdateToolbarIntefrace{

    private EditText mEditTextUsuario;
    private EditText mEditTextPassword;

    public LoginFragment() {
        // Required empty public constructor
    }

    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setRetainInstance(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        mEditTextUsuario = (EditText) view.findViewById(R.id.editTxtUsuario);
        mEditTextPassword = (EditText) view.findViewById(R.id.editTxtPassword);


        view.findViewById(R.id.btnCreateAccount).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().beginTransaction()
                        .add(R.id.coordinatorLayout, RegisterAccountFragment.newInstance(ACTION_CREATE))
                        .addToBackStack(FRAGMENT_REGISTER_ACCOUNT).commitAllowingStateLoss();
            }
        });

        view.findViewById(R.id.btnLogin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progressDialog = ProgressDialog.show(getContext(), "", getString(R.string.loading));
                LoginUserRequest.loginUser(getActivity(), mEditTextUsuario.getText().toString(), mEditTextPassword.getText().toString(), new JsonObjectResponse() {
                    @SuppressLint("ApplySharedPref")
                    @Override
                    public void onSuccess(JsonObject response) {
                        progressDialog.dismiss();
                        SessionHelper.createSession(getContext(), response.get("api_token").getAsString(), response);
                        createReminders(response.getAsJsonArray("takes"), response.getAsJsonArray("visits"));
                        AlarmBroadcastReceiver.setAllAlarms(getContext());
                        Answers.getInstance().logLogin(new LoginEvent()
                                .putMethod("AppLogin")
                                .putSuccess(true));
                        startActivity(new Intent(getActivity(), MainHomeActivity.class));
                        getActivity().finish();
                    }

                    @Override
                    public void onError(JsonObject response, Exception e) {
                        progressDialog.dismiss();
                        if (response != null && response.get("error").getAsString().equals("Unauthorised")){
                            mEditTextPassword.setError(getString(R.string.error_incorrect_username_or_password));
                            mEditTextPassword.requestFocus();
                        } else {
                            Answers.getInstance().logLogin(new LoginEvent()
                                    .putMethod("AppLogin")
                                    .putSuccess(false));
                            Toast.makeText(getContext(), R.string.error_message_internet_conection, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        view.findViewById(R.id.btnForgotPassword).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Answers.getInstance().logContentView(new ContentViewEvent()
                        .putContentName("ForgotPassword")
                        .putContentId(VIEW_FORGOT_PASSWORD_ID)
                        .putContentType(CONTENT_TYPE_VIEW));
                getActivity().getSupportFragmentManager().beginTransaction()
                        .add(R.id.coordinatorLayout, RestorePasswordFragment.newInstance())
                        .addToBackStack(FRAGMENT_RECUPERAR_CONTRASEÑA).commitAllowingStateLoss();
            }
        });

        return view;
    }

    @Override
    public void onFragmentUpdateToolbar() {
        if (getActivity() instanceof ActivityUpdateBackToolbar) {
            ((ActivityUpdateBackToolbar) getActivity()).showBackNavigationIcon();
        }
        getActivity().setTitle(getString(R.string.action_bar_title_ingresar));
    }
}
