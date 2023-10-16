package com.orugga.yapp.fragments;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.orugga.yapp.R;
import com.orugga.yapp.interfaces.ActivityUpdateBackToolbar;
import com.orugga.yapp.interfaces.OnFragmentUpdateToolbarIntefrace;
import com.orugga.yapp.views.HtmlTextView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static com.orugga.yapp.requests.DownloadImageRequest.downloadImage;

/**
 * A simple {@link Fragment} subclass.
 */
public class PromosFragment extends Fragment implements OnFragmentUpdateToolbarIntefrace {

    private Timer mTimer;

    private ViewPager mPromosViewPager;
    private PromosPagerAdapter mPromosPagerAdapter;
    private PromoCategoryButtonsAdapter mPromoCategoryButtonsAdapter;

    private JsonArray mPromosCategories;

    private TextView mCategoryDescription;

    public PromosFragment() {
        // Required empty public constructor
    }

    public static PromosFragment newInstance(JsonArray promos) {
        PromosFragment fragment = new PromosFragment();
        Bundle args = new Bundle();
        args.putString("promos", promos.toString());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPromosCategories = new JsonParser().parse(getArguments().getString("promos")).getAsJsonArray();
        }
        setRetainInstance(true);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_promos, container, false);

        mCategoryDescription = view.findViewById(R.id.txtCategoryDescription);

        //Instanciando ViewPager
        mPromosViewPager = view.findViewById(R.id.promosViewPager);
        mPromosPagerAdapter = new PromosPagerAdapter(getContext(), mPromosCategories.get(0).getAsJsonObject().getAsJsonArray("promotions"));
        mPromosViewPager.setAdapter(mPromosPagerAdapter);
        mPromosViewPager.setCurrentItem(mPromosPagerAdapter.getFirstPosition());
        mPromosViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mTimer.cancel();
                mTimer = new Timer();
                runTimer(5000); //Delay after touching the ViewPager 5 seconds
                v.performClick();
                return false;
            }
        });
        //Creo el adapter de la lista de botones
        mPromoCategoryButtonsAdapter = new PromoCategoryButtonsAdapter(getContext(), createPromoCategoryButtonsArray());
        ((ListView) view.findViewById(R.id.promoCategoryButtonsListView)).setAdapter(mPromoCategoryButtonsAdapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final ViewTreeObserver viewTreeObserver = view.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (mPromoCategoryButtonsAdapter.getItem(0).categoryButton != null)
                    mPromoCategoryButtonsAdapter.getItem(0).categoryButton.callOnClick();
                viewTreeObserver.removeOnGlobalLayoutListener(this);
            }
        });
    }

    private ArrayList<PromoCategoryButton> createPromoCategoryButtonsArray() {
        ArrayList<PromoCategoryButton> promoCategoryButtons = new ArrayList<>();
        for (int i = 0; i < mPromosCategories.size(); i++) {
            promoCategoryButtons.add(new PromoCategoryButton(mPromosCategories.get(i).getAsJsonObject()));
        }
        return promoCategoryButtons;
    }


    private void runTimer(int delay) {
        mTimer = new Timer();
        int bannerDelay = 5000;
        mTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (getActivity() == null || getActivity().isFinishing()) return;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int currentPage = mPromosViewPager.getCurrentItem();
                        int nextPage = (currentPage + 1) % mPromosPagerAdapter.getCount();
                        mPromosViewPager.setCurrentItem(nextPage, true);
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
    public void onFragmentUpdateToolbar() {
        getActivity().setTitle(getString(R.string.action_bar_title_promos));
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

    private class PromosPagerAdapter extends PagerAdapter {

        private static final int LOOPS = 1;
        private Context mContext;
        private JsonArray mPromos;

        private PromosPagerAdapter(Context context, JsonArray promos) {
            mContext = context;
            this.mPromos = promos;
        }

        private int getFirstPosition() {
            return 0;
        }

        private void setNewPromos(JsonArray promos) {
            this.mPromos = promos;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mPromos.size() * LOOPS;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.pager_item, container, false);

            ImageView imageView = itemView.findViewById(R.id.imageView);
            final JsonObject currentPromos = mPromos.get(position % mPromos.size()).getAsJsonObject();
            downloadImage(mContext, currentPromos.get("full_path_image_800").getAsString(),
                    imageView, R.drawable.place_holder_promo);
            if (!currentPromos.get("link").isJsonNull())
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String url = currentPromos.get("link").getAsString();
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        getActivity().startActivity(i);
                    }
                });
            container.addView(itemView);

            return itemView;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((LinearLayout) object);
        }
    }

    private class PromoCategoryButtonsAdapter extends ArrayAdapter<PromoCategoryButton> {

        private PromoCategoryButtonsAdapter(Context context, ArrayList<PromoCategoryButton> promoCategoryButtons) {
            super(context, R.layout.promos_category_button_item, promoCategoryButtons);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).jsonPromosCategory.get("id").getAsLong();
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            final PromoCategoryButton promoCategoryButton = getItem(position);
            promoCategoryButton.categoryButton = (HtmlTextView) super.getView(position, convertView, parent);
            promoCategoryButton.categoryButton.setText(promoCategoryButton.jsonPromosCategory.get("button_label").getAsString());

            if (!promoCategoryButton.isClicked) {
                promoCategoryButton.setNormalColor();
            } else {
                promoCategoryButton.setDarkerColor();
            }

            promoCategoryButton.categoryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!promoCategoryButton.isClicked) {
                        mPromosPagerAdapter.setNewPromos(promoCategoryButton.jsonPromosCategory.getAsJsonArray("promotions"));
                        mPromosViewPager.setAdapter(mPromosPagerAdapter);
                        desClickAll();
                        promoCategoryButton.isClicked = true;
                        promoCategoryButton.setDarkerColor();
                        mCategoryDescription.setText(promoCategoryButton.jsonPromosCategory.get("description").getAsString());
                    }
                }
            });

            return promoCategoryButton.categoryButton;
        }

        private void desClickAll() {
            for (int i = 0; i < getCount(); i++) {
                PromoCategoryButton promoCategoryButton = getItem(i);
                if (promoCategoryButton.isClicked) {
                    promoCategoryButton.setNormalColor();
                    promoCategoryButton.isClicked = false;
                }
            }
        }
    }

    private class PromoCategoryButton {
        private HtmlTextView categoryButton;
        private JsonObject jsonPromosCategory;
        private boolean isClicked;

        private int normalColor;
        private int darkerColor;

        private PromoCategoryButton(JsonObject jsonPromosCategory) {
            this.jsonPromosCategory = jsonPromosCategory;
            this.normalColor = Color.parseColor(jsonPromosCategory.get("color").getAsString());
            this.darkerColor = Color.parseColor(jsonPromosCategory.get("color_darker").getAsString());
            this.isClicked = false;
        }

        private void setNormalColor() {
            this.categoryButton.setBackgroundColor(normalColor);
        }

        private void setDarkerColor() {
            this.categoryButton.setBackgroundColor(darkerColor);
        }
    }
}
