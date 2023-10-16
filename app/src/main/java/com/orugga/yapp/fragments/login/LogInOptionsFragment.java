package com.orugga.yapp.fragments.login;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.crashlytics.android.answers.LoginEvent;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.gson.JsonObject;
import com.orugga.yapp.AlarmBroadcastReceiver;
import com.orugga.yapp.MainHomeActivity;
import com.orugga.yapp.R;
import com.orugga.yapp.interfaces.ActivityUpdateBackToolbar;
import com.orugga.yapp.interfaces.JsonObjectResponse;
import com.orugga.yapp.interfaces.OnFragmentUpdateToolbarIntefrace;

import java.util.Arrays;

import static com.orugga.yapp.Constants.FragmentsNames.FRAGMENT_LOGIN;
import static com.orugga.yapp.Constants.FragmentsNames.FRAGMENT_REGISTER_ACCOUNT;
import static com.orugga.yapp.Constants.IntentFilters.ACTION_CREATE;
import static com.orugga.yapp.helpers.SessionHelper.createReminders;
import static com.orugga.yapp.helpers.SessionHelper.createSession;
import static com.orugga.yapp.requests.LoginUserRequest.loginWithFacebook;


/**
 * A simple {@link Fragment} subclass.
 */
public class LogInOptionsFragment extends Fragment implements OnFragmentUpdateToolbarIntefrace {

    private CallbackManager callbackManager;

    public LogInOptionsFragment() {
        // Required empty public constructor
    }

    public static LogInOptionsFragment newInstance() {
        return new LogInOptionsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setRetainInstance(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_log_in_options, container, false);
        if (getActivity() == null || getActivity().isFinishing()) return view;
        view.findViewById(R.id.btnLogInWithMyAccount).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() == null || getActivity().isFinishing()) return;
                getActivity().getSupportFragmentManager().beginTransaction()
                        .add(R.id.coordinatorLayout, LoginFragment.newInstance())
                        .addToBackStack(FRAGMENT_LOGIN).commitAllowingStateLoss();
            }
        });

        view.findViewById(R.id.btnCreateAccount).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() == null || getActivity().isFinishing()) return;
                Answers.getInstance().logCustom(new CustomEvent("Ingreso - Crear cuenta"));
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.add(R.id.coordinatorLayout, RegisterAccountFragment.newInstance(ACTION_CREATE))
                        .addToBackStack(FRAGMENT_REGISTER_ACCOUNT)
                        .commitAllowingStateLoss();
            }
        });

        view.findViewById(R.id.btnLogInWithoutRegister).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() == null || getActivity().isFinishing()) return;
                Answers.getInstance().logLogin(new LoginEvent()
                        .putMethod("Login Sin Registrarse"));
                startActivity(new Intent(getActivity(), MainHomeActivity.class));
                getActivity().finish();
            }
        });

        // Login With Facebook Init
        LinearLayout btnFacebook = view.findViewById(R.id.btnLogInWithFacebook);
        callbackManager = CallbackManager.Factory.create();
        btnFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logInWithReadPermissions(LogInOptionsFragment.this, Arrays.asList("public_profile", "email", "user_birthday", "user_gender"));
            }
        });
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        String accessToken = loginResult.getAccessToken().getToken();
                        final ProgressDialog progressDialog = ProgressDialog.show(getContext(), "", getString(R.string.loading));
                        loginWithFacebook(getActivity(), accessToken, new JsonObjectResponse() {
                            @Override
                            public void onSuccess(JsonObject userData) {
                                if (getActivity() == null || getActivity().isFinishing()) return;
                                progressDialog.dismiss();
                                String api_token = userData.getAsJsonObject().get("api_token").getAsString();
                                createSession(getActivity(), api_token, userData);
                                createReminders(userData.getAsJsonArray("takes"), userData.getAsJsonArray("visits"));
                                AlarmBroadcastReceiver.setAllAlarms(getContext());
                                Answers.getInstance().logLogin(new LoginEvent()
                                        .putMethod("Facebook")
                                        .putSuccess(true)
                                        .putCustomAttribute("email", userData.get("email").getAsString()));
                                Intent i = new Intent(getActivity(), MainHomeActivity.class);
                                startActivity(i);
                                getActivity().finish();
                            }

                            @Override
                            public void onError(JsonObject response, Exception e) {
                                if (getActivity() == null || getActivity().isFinishing()) return;
                                progressDialog.dismiss();
                                Answers.getInstance().logLogin(new LoginEvent()
                                        .putMethod("Facebook")
                                        .putSuccess(false));
                                Toast.makeText(getContext(), R.string.error_message_internet_conection, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onCancel() {
                        Answers.getInstance().logLogin(new LoginEvent()
                                .putMethod("Facebook")
                                .putSuccess(false));
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Answers.getInstance().logLogin(new LoginEvent()
                                .putMethod("Facebook")
                                .putSuccess(false));
                        Toast.makeText(getContext(), R.string.error_message_internet_conection, Toast.LENGTH_LONG).show();
                    }
                });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onFragmentUpdateToolbar() {
        if (getActivity() == null || getActivity().isFinishing()) return;
        if (getActivity() instanceof ActivityUpdateBackToolbar) {
            ((ActivityUpdateBackToolbar) getActivity()).showBackNavigationIcon();
        }
        getActivity().setTitle(getString(R.string.action_bar_title_ingresar));
    }
}
