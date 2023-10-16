package com.orugga.yapp.fragments;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.crashlytics.android.answers.CustomEvent;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.orugga.yapp.ColaboraActivity;
import com.orugga.yapp.R;
import com.orugga.yapp.adapters.BannerPagerAdapter;
import com.orugga.yapp.fragments.login.RegisterAccountFragment;
import com.orugga.yapp.helpers.IdleHelper;
import com.orugga.yapp.interfaces.ActivityUpdateBackToolbar;
import com.orugga.yapp.interfaces.JsonArrayResponse;
import com.orugga.yapp.interfaces.OnFragmentUpdateToolbarIntefrace;

import java.lang.reflect.Field;
import java.util.Timer;
import java.util.TimerTask;

import static com.orugga.yapp.Constants.ANSWERS_CONSTANTS.CONTENT_TYPE_VIEW;
import static com.orugga.yapp.Constants.ANSWERS_CONSTANTS.VIEW_BUSCAR_DESCUENTOS_Y_PROMOS_ID;
import static com.orugga.yapp.Constants.ANSWERS_CONSTANTS.VIEW_BUSCAR_FARMACIAS_ID;
import static com.orugga.yapp.Constants.ANSWERS_CONSTANTS.VIEW_BUSCAR_PRODUCTOS_ID;
import static com.orugga.yapp.Constants.FragmentsNames.FRAGMENT_BUSCAR_FARMACIA;
import static com.orugga.yapp.Constants.FragmentsNames.FRAGMENT_BUSCAR_PRODUCTO;
import static com.orugga.yapp.Constants.FragmentsNames.FRAGMENT_PROMOS;
import static com.orugga.yapp.Constants.FragmentsNames.FRAGMENT_REGISTER_ACCOUNT;
import static com.orugga.yapp.Constants.IntentFilters.ACTION_COMPLETE_INFO;
import static com.orugga.yapp.Constants.IntentFilters.ACTION_SEARCH;
import static com.orugga.yapp.helpers.SessionHelper.getUser;
import static com.orugga.yapp.helpers.SessionHelper.requiredUserInformationMissing;
import static com.orugga.yapp.requests.GetPromotionsRequest.getPromotions;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment implements OnFragmentUpdateToolbarIntefrace {

    private static final int bannerDelay = 5000;
    private static int[] mResources = {
            R.drawable.b1,
            R.drawable.b2/*,
            R.drawable.b3,
            R.drawable.b4,
            R.drawable.b5,
            R.drawable.b6*/
    };
    private Timer mTimer;

    private ViewPager mBannerViewPager;
    private BannerPagerAdapter mBannerPagerAdapter;


    public HomeFragment() {

    }

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_home, container, false);
        if (getActivity() == null || getActivity().isFinishing()) return mView;
        onFragmentUpdateToolbar();

        //Instanciando ViewPager
        mBannerPagerAdapter = new BannerPagerAdapter(getContext(), mResources);
        mBannerViewPager = mView.findViewById(R.id.bannersViewPager);
        mBannerViewPager.setAdapter(mBannerPagerAdapter);
        mBannerViewPager.setCurrentItem(mBannerPagerAdapter.getFirstPosition());
        mBannerViewPager.setOffscreenPageLimit(3);
        mBannerViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                delayBannerMovement();
                return false;
            }
        });

        if (getUser(getContext()) != null && requiredUserInformationMissing(getUser(getContext()))) {
            getActivity().getSupportFragmentManager().beginTransaction().
                    add(R.id.coordinatorLayout, RegisterAccountFragment.newInstance(ACTION_COMPLETE_INFO))
                    .addToBackStack(FRAGMENT_REGISTER_ACCOUNT).commitAllowingStateLoss();
        }

        //Instanciando Botones
        LinearLayout btnDtoYPromos = mView.findViewById(R.id.btnHomeDtosYPromos);
        btnDtoYPromos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IdleHelper.disableForAWhile(getActivity(), v);
                Answers.getInstance().logContentView(new ContentViewEvent()
                        .putContentType(CONTENT_TYPE_VIEW)
                        .putContentId(VIEW_BUSCAR_DESCUENTOS_Y_PROMOS_ID)
                        .putContentName("Menu inicio - Buscar Descuentos y Promos"));
                final ProgressDialog progressDialog = ProgressDialog.show(getContext(), "", getString(R.string.loading));
                getPromotions(getActivity(), new JsonArrayResponse() {
                    @Override
                    public void onSuccess(JsonArray response) {
                        if (getActivity() == null || getActivity().isFinishing()) return;
                        progressDialog.dismiss();
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .add(R.id.coordinatorLayout, PromosFragment.newInstance(response))
                                .addToBackStack(FRAGMENT_PROMOS).commitAllowingStateLoss();
                    }

                    @Override
                    public void onError(JsonObject response, Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), R.string.error_message_internet_conection, Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
        LinearLayout btnBuscarFarmacia = mView.findViewById(R.id.btnHomeBuscarFarmacia);
        btnBuscarFarmacia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() == null || getActivity().isFinishing()) return;
                IdleHelper.disableForAWhile(getActivity(), v);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .add(R.id.coordinatorLayout, BuscarFarmaciaFragment.newInstance(ACTION_SEARCH))
                        .addToBackStack(FRAGMENT_BUSCAR_FARMACIA).commitAllowingStateLoss();
                Answers.getInstance().logContentView(new ContentViewEvent()
                        .putContentType(CONTENT_TYPE_VIEW)
                        .putContentId(VIEW_BUSCAR_FARMACIAS_ID)
                        .putContentName("Menu lateral - Buscar Farmacias"));
            }
        });
        LinearLayout btnBuscarProducto = mView.findViewById(R.id.btnHomeBuscarProducto);
        btnBuscarProducto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() == null || getActivity().isFinishing()) return;
                IdleHelper.disableForAWhile(getActivity(), v);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .add(R.id.coordinatorLayout, BuscarProductosFragment.newInstance())
                        .addToBackStack(FRAGMENT_BUSCAR_PRODUCTO).commitAllowingStateLoss();
                Answers.getInstance().logContentView(new ContentViewEvent()
                        .putContentType(CONTENT_TYPE_VIEW)
                        .putContentId(VIEW_BUSCAR_PRODUCTOS_ID)
                        .putContentName("Menu lateral - Buscar Productos"));
            }
        });

        LinearLayout btnColabora = mView.findViewById(R.id.btnHomeBuscarProgramaPaciente);
        btnColabora.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Answers.getInstance().logCustom(new CustomEvent("Menu inicio - Colabora"));
                IdleHelper.disableForAWhile(getActivity(), v);
                startActivity(new Intent(getActivity(), ColaboraActivity.class));
            }
        });

        mView.findViewById(R.id.banner_flecha_anterior).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position;
                if (mBannerViewPager.getCurrentItem() - 1 < 0) {
                    position = mBannerPagerAdapter.getCount();
                } else {
                    position = mBannerViewPager.getCurrentItem() - 1;
                }
                mBannerViewPager.setCurrentItem(position);
                delayBannerMovement();
            }
        });
        mView.findViewById(R.id.banner_flecha_siguiente).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position;
                if (mBannerViewPager.getCurrentItem() + 1 >= mBannerPagerAdapter.getCount()) {
                    position = 0;
                } else {
                    position = mBannerViewPager.getCurrentItem() + 1;
                }
                mBannerViewPager.setCurrentItem(position);
                delayBannerMovement();
            }
        });


        return mView;
    }

    private void delayBannerMovement() {
        mTimer.cancel();
        mTimer = new Timer();
        runTimer(3000);
    }

    private void runTimer(int delay) {
        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (getActivity() == null || getActivity().isFinishing()) return;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int currentPage = mBannerViewPager.getCurrentItem();
                        int nextPage = (currentPage + 1) % mBannerPagerAdapter.getCount();
                        mBannerViewPager.setCurrentItem(nextPage, true);
                    }
                });
            }
        }, delay, bannerDelay);
    }

    @Override
    public void onResume() {
        runTimer(1000); //First Delay 1 second
        super.onResume();
    }

    @Override
    public void onPause() {
        mTimer.cancel();
        mTimer.purge();
        super.onPause();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onFragmentUpdateToolbar() {
        if (getActivity() == null || getActivity().isFinishing()) return;
        getActivity().setTitle(getString(R.string.action_bar_title_home));
        if (getActivity() instanceof ActivityUpdateBackToolbar) {
            ((ActivityUpdateBackToolbar) getActivity()).showBackNavigationIcon();
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);

        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
