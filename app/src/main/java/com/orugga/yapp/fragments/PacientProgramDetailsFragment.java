package com.orugga.yapp.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.orugga.yapp.R;
import com.orugga.yapp.interfaces.ActivityUpdateBackToolbar;
import com.orugga.yapp.interfaces.OnFragmentUpdateToolbarIntefrace;
import com.orugga.yapp.views.HtmlTextView;

import static com.orugga.yapp.requests.DownloadImageRequest.downloadImage;

/**
 * A simple {@link Fragment} subclass.
 */
public class PacientProgramDetailsFragment extends Fragment implements OnFragmentUpdateToolbarIntefrace {

    JsonObject mPacientProgram;

    public PacientProgramDetailsFragment() {
        // Required empty public constructor
    }

    public static PacientProgramDetailsFragment newInstance(JsonObject pacientProgram) {
        PacientProgramDetailsFragment fragment = new PacientProgramDetailsFragment();
        Bundle args = new Bundle();
        args.putString("pacient_program", pacientProgram.toString());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPacientProgram = new JsonParser().parse(getArguments().getString("pacient_program")).getAsJsonObject();
        }
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final ScrollView view = (ScrollView) inflater.inflate(R.layout.fragment_pacient_program_details, container, false);
        downloadImage(getContext(), mPacientProgram.get("full_path_banner_800").getAsString(),
                (ImageView) view.findViewById(R.id.pacientProgramBanner), R.drawable.place_holder_pp);
        downloadImage(getContext(), mPacientProgram.getAsJsonObject("labs").get("full_path_image_400").getAsString(),
                (ImageView) view.findViewById(R.id.labImage), R.drawable.placeholder_lab);

        String description = mPacientProgram.get("description").getAsString();
        HtmlTextView shortTextDescription = view.findViewById(R.id.shortPacientProgramDescription);
        shortTextDescription.setMovementMethod(LinkMovementMethod.getInstance());
        shortTextDescription.setText(description);

        HtmlTextView longTextDescription = view.findViewById(R.id.longPacientProgramDescription);
        longTextDescription.setMovementMethod(LinkMovementMethod.getInstance());
        longTextDescription.setText(description);

        view.findViewById(R.id.btnVerMas).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.findViewById(R.id.linearLayoutVerMas).setVisibility(View.GONE);
                view.findViewById(R.id.linearLayoutVerMenos).setVisibility(View.VISIBLE);
                view.smoothScrollTo(0, view.findViewById(R.id.btnVerMenos).getBottom());
            }
        });
        view.findViewById(R.id.btnVerMenos).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.smoothScrollTo(0, view.getTop());
                view.findViewById(R.id.linearLayoutVerMenos).setVisibility(View.GONE);
                view.findViewById(R.id.linearLayoutVerMas).setVisibility(View.VISIBLE);
            }
        });

//        final TextView btnQuieroSubscribirme = (TextView) view.findViewById(R.id.btnQuieroSubscribirme);
//        btnQuieroSubscribirme.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (isUserLogedIn(getActivity())) {
//                    subscibeToPacientProgram(getActivity(), getAccessToken(getActivity()), mPacientProgram.get("id").getAsInt(),
//                            new JsonObjectResponse() {
//                                @Override
//                                public void onSuccess(JsonObject response) {
//                                    showSubscriptionConfirmation(getActivity());
//                                    btnQuieroSubscribirme.setText(R.string.already_subscribed);
//                                    btnQuieroSubscribirme.setClickable(false);
//                                }
//
//                                @Override
//                                public void onError(JsonObject response, Exception e) {
//                                    Toast.makeText(getContext(), R.string.error_message_internet_conection, Toast.LENGTH_SHORT).show();
//                                }
//                            });
//
//                } else {
//                    ((MainHomeActivity) getActivity()).needHaveAnAccount(getString(R.string.must_be_loged_to_subscribe));
//                }
//            }
//        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (menu.findItem(R.id.menu_item_share) != null)
            menu.findItem(R.id.menu_item_share).setVisible(false);

        if (mPacientProgram.get("share_url") != JsonNull.INSTANCE)
            inflater.inflate(R.menu.simple_share_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Answers.getInstance().logCustom(new CustomEvent("Programa Paciente detalle - Compartir"));
        Intent sharingIntent = new Intent();
        sharingIntent.setAction(Intent.ACTION_SEND);
        sharingIntent.setType("text/html");
        sharingIntent.putExtra(Intent.EXTRA_TEXT, mPacientProgram.get("share_url").getAsString());
        startActivity(Intent.createChooser(sharingIntent, "Compartir usando"));
        return false;
    }

    @Override
    public void onFragmentUpdateToolbar() {
        getActivity().setTitle(getString(R.string.action_bar_title_programa_paciente));
        if (getActivity() instanceof ActivityUpdateBackToolbar) {
            ((ActivityUpdateBackToolbar) getActivity()).showBackNavigationIcon();
        }
    }
}
