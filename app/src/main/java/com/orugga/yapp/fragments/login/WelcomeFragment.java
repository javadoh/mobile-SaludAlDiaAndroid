package com.orugga.yapp.fragments.login;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.orugga.yapp.R;
import com.orugga.yapp.interfaces.ActivityUpdateBackToolbar;
import com.orugga.yapp.interfaces.OnFragmentUpdateToolbarIntefrace;

import static com.orugga.yapp.Constants.FragmentsNames.FRAGMENT_PROFILE;
import static com.orugga.yapp.Constants.IntentFilters.ACTION_CREATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class WelcomeFragment extends Fragment implements OnFragmentUpdateToolbarIntefrace {


    public WelcomeFragment() {
        // Required empty public constructor
    }

    private JsonObject mUserData;
    private JsonObject mRegisterData;

    public static WelcomeFragment newInstance(JsonObject userData, JsonObject registerData) {
        WelcomeFragment fragment = new WelcomeFragment();
        Bundle args = new Bundle();
        args.putString("userData", userData.toString());
        args.putString("registerData", registerData.toString());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUserData = new JsonParser().parse(getArguments().getString("userData")).getAsJsonObject();
            mRegisterData = new JsonParser().parse(getArguments().getString("registerData")).getAsJsonObject();
        }
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_welcome, container, false);
        view.findViewById(R.id.btnCompletarMiPerfil).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .add(R.id.coordinatorLayout, ProfileFragment.newInstance(mUserData, mRegisterData, ACTION_CREATE))
                        .addToBackStack(FRAGMENT_PROFILE).commitAllowingStateLoss();
            }
        });

        return view;
    }

    @Override
    public void onFragmentUpdateToolbar() {
        if (getActivity() instanceof ActivityUpdateBackToolbar) {
            ((ActivityUpdateBackToolbar) getActivity()).showBackNavigationIcon();
        }
        getActivity().setTitle(getString(R.string.action_bar_title_bienvenido));
    }
}
