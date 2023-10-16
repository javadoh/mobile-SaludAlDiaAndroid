package com.orugga.yapp.fragments.colabora;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orugga.yapp.R;
import com.orugga.yapp.interfaces.OnFragmentUpdateToolbarIntefrace;

/**
 * A simple {@link Fragment} subclass.
 */
public class ColaboraThanksFragment extends Fragment implements OnFragmentUpdateToolbarIntefrace {


    public ColaboraThanksFragment() {
        // Required empty public constructor
    }

    public static ColaboraThanksFragment newInstance() {
        return new ColaboraThanksFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_colabora_thanks, container, false);

        view.findViewById(R.id.btnOk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        return view;
    }

    @Override
    public void onFragmentUpdateToolbar() {
        getActivity().setTitle(getString(R.string.action_bar_title_colabora));
    }
}
