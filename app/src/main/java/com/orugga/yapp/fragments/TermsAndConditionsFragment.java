package com.orugga.yapp.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.orugga.yapp.R;
import com.orugga.yapp.fragments.login.LogInOptionsFragment;
import com.orugga.yapp.helpers.SessionHelper;
import com.orugga.yapp.interfaces.ActivityUpdateBackToolbar;
import com.orugga.yapp.interfaces.OnFragmentUpdateToolbarIntefrace;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


/**
 * A simple {@link Fragment} subclass.
 */
@SuppressWarnings("ConstantConditions")
public class TermsAndConditionsFragment extends Fragment implements OnFragmentUpdateToolbarIntefrace {

    public static final int IN_DRAWER = 0x1;
    public static final int IN_TOUR = 0x2;

    private int mShowIn;

    public TermsAndConditionsFragment() {
        // Required empty public constructor
    }

    public static TermsAndConditionsFragment newInstance(int showIn) {
        TermsAndConditionsFragment f = new TermsAndConditionsFragment();
        f.mShowIn = showIn;
        return f;
    }

    public static String readRawTextFile(Context ctx, int resId) {
        InputStream inputStream = ctx.getResources().openRawResource(resId);

        InputStreamReader inputreader = new InputStreamReader(inputStream);
        BufferedReader buffreader = new BufferedReader(inputreader);
        String line;
        StringBuilder text = new StringBuilder();

        try {
            while ((line = buffreader.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
        } catch (IOException e) {
            return "";
        }
        return text.toString();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_terms_and_conditions, container, false);

        TextView textTyC = view.findViewById(R.id.textTyC);

        textTyC.setText(readRawTextFile(getContext(), R.raw.tyc_es));

        if (mShowIn == IN_TOUR) {
            TextView btnHeLeido = view.findViewById(R.id.btnHeLeidoYAcepto);
            btnHeLeido.setVisibility(View.VISIBLE);
            btnHeLeido.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SessionHelper.setTerms(getContext(), true);
                    getActivity().setTitle(getString(R.string.action_bar_title_ingresar));
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.coordinatorLayout, LogInOptionsFragment.newInstance())
                            .commit();
                }
            });
        }

        return view;
    }

    @Override
    public void onFragmentUpdateToolbar() {
        getActivity().setTitle(getString(R.string.action_bar_title_tyc));
        if (getActivity() instanceof ActivityUpdateBackToolbar) {
            ((ActivityUpdateBackToolbar) getActivity()).showBackNavigationIcon();
        }
    }
}
