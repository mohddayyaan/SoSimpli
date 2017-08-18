package com.think360.sosimpli.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.BottomBarTab;
import com.roughike.bottombar.OnTabReselectListener;
import com.roughike.bottombar.OnTabSelectListener;
import com.think360.sosimpli.AppController;
import com.think360.sosimpli.FragmentDrawer;
import com.think360.sosimpli.R;
import com.think360.sosimpli.adapter.PagerAdapter;
import com.think360.sosimpli.manager.ApiService;
import com.think360.sosimpli.model.logout.LogoutResponse;
import com.think360.sosimpli.model.sos.count.SosCountResponse;
import com.think360.sosimpli.ui.activities.login.LoginActivity;
import com.think360.sosimpli.ui.fragments.AvailabilityFragment;
import com.think360.sosimpli.ui.fragments.ProfileFragment;
import com.think360.sosimpli.ui.fragments.ScheduleFragment;
import com.think360.sosimpli.ui.fragments.SoSFragment;
import com.think360.sosimpli.utils.AppConstants;
import com.think360.sosimpli.widgets.NonSwipeableViewPager;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class HomeActivity extends BaseActivity implements AvailabilityFragment.OnFragmentInteractionListener, ScheduleFragment.OnFragmentInteractionListener, ProfileFragment.OnFragmentInteractionListener, SoSFragment.OnFragmentInteractionListener, FragmentDrawer.FragmentDrawerListener {

    @Inject
    ApiService apiService;
/*    @BindView(R.id.drawerLayout)
    DrawerLayout drawerLayout;*/

    @BindView(R.id.toolbar)
    protected Toolbar toolbar;

    @BindView(R.id.viewPager)
    protected NonSwipeableViewPager viewPager;


    @BindView(R.id.bottomBar)
    protected BottomBar bottomBar;

    @BindView(R.id.cl)
    protected CoordinatorLayout cl;

    private TextView title;
    private FragmentDrawer drawerFragment;
    private BottomBarTab nearby;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((AppController) getApplication()).getComponent().inject(this);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        // Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_menu, null);
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, Color.WHITE);
        getSupportActionBar().setHomeAsUpIndicator(drawable);

      //  getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
      //  getSupportActionBar().setCustomView(R.layout.abs_layout);

        getSupportActionBar().setHomeButtonEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);





        drawerFragment = (FragmentDrawer)
                getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), toolbar);
        drawerFragment.setDrawerListener(this);


        title = (TextView) findViewById(getResources().getIdentifier("action_bar_title", "id", getPackageName()));


        viewPager.setAdapter(new PagerAdapter(getSupportFragmentManager(), getFragmentArrrayList()));
        viewPager.setOffscreenPageLimit(3);
        // toolbar.setTitle("Historico");
  /*      bottomBar.setOnMenuItemClickListener(new BottomNavigation.OnMenuItemSelectionListener() {
            @Override
            public void onMenuItemSelect(@IdRes int i, int i1, boolean b) {

                switch (i) {
                    case R.id.bbn_item1:
                        activityHomeBinding.toolbar.toolBarTitle.setText("Historico");
                        activityHomeBinding.viewPager.setCurrentItem(0);
                        break;
                    case R.id.bbn_item2:
                        activityHomeBinding.toolbar.toolBarTitle.setText("Trabajo");
                        activityHomeBinding.viewPager.setCurrentItem(1);
                        break;
                    case R.id.bbn_item3:
                        activityHomeBinding.toolbar.toolBarTitle.setText("Ajustes");
                        activityHomeBinding.viewPager.setCurrentItem(2);
                        break;

                }
            }

            @Override
            public void onMenuItemReselect(@IdRes int i, int i1, boolean b) {

            }
        });*/

        // We're doing nothing with this listener here this time. Check example usage
        // from ThreeTabsActivity on how to use it.
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {

                switch (tabId) {
                    case R.id.tab_favorites:
                        viewPager.setCurrentItem(0);
                        toolbar.setTitle("Schedule List");
                        break;
                    case R.id.tab_nearby:
                        toolbar.setTitle("Assigned Schedule");
                        viewPager.setCurrentItem(1);
                        break;
                    case R.id.tab_friends1:
                        toolbar.setTitle("SOS Notifications");
                        viewPager.setCurrentItem(2);

                        apiService.readSosCount(driverId).enqueue(new Callback<SosCountResponse>() {
                            @Override
                            public void onResponse(Call<SosCountResponse> call, Response<SosCountResponse> response) {
                                if (response.isSuccessful()) {
                                    nearby.setBadgeCount(0);
                                } else {
                                    nearby.setBadgeCount(0);
                                }
                            }

                            @Override
                            public void onFailure(Call<SosCountResponse> call, Throwable t) {

                            }
                        });

                        break;
                    case R.id.tab_friend:
                        toolbar.setTitle("Profile Settings");
                        viewPager.setCurrentItem(3);
                        break;
                }
            }
        });

        bottomBar.setOnTabReselectListener(new OnTabReselectListener() {
            @Override
            public void onTabReSelected(@IdRes int tabId) {
                if (tabId == R.id.tab_friends1) {
                    // The tab with id R.id.tab_favorites was reselected,
                    // change your content accordingly.
                    // Remove the badge when you're done with it.
                    BottomBarTab nearby = bottomBar.getTabWithId(R.id.tab_friends1);
                    nearby.removeBadge();
                }
            }
        });

        nearby = bottomBar.getTabWithId(R.id.tab_friends1);

        apiService.getSosCount(driverId).enqueue(new Callback<SosCountResponse>() {
            @Override
            public void onResponse(Call<SosCountResponse> call, Response<SosCountResponse> response) {
                if (response.isSuccessful()) {
                    nearby.setBadgeCount(Integer.parseInt(response.body().getData().getCount()));
                } else {
                    nearby.setBadgeCount(0);
                }
            }

            @Override
            public void onFailure(Call<SosCountResponse> call, Throwable t) {

            }
        });




        /*bottomBar.setOnMenuItemClickListener(new BottomNavigation.OnMenuItemSelectionListener() {
            @Override
            public void onMenuItemSelect(@IdRes int i, int i1, boolean b) {

                switch (i) {
                    case R.id.bbn_item1:
                       *//* activityHomeBinding.toolbar.toolBarTitle.setText("Historico");
                        activityHomeBinding.viewPager.setCurrentItem(0);*//*
                        break;
                    case R.id.bbn_item2:
                      *//*  activityHomeBinding.toolbar.toolBarTitle.setText("Trabajo");
                        activityHomeBinding.viewPager.setCurrentItem(1);*//*
                        break;
                    case R.id.bbn_item3:
                    *//*    activityHomeBinding.toolbar.toolBarTitle.setText("Ajustes");
                        activityHomeBinding.viewPager.setCurrentItem(2);*//*
                        break;

                }
            }

            @Override
            public void onMenuItemReselect(@IdRes int i, int i1, boolean b) {

            }
        });*/


    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private ArrayList<Fragment> getFragmentArrrayList() {
        ArrayList<Fragment> fragmentSparseArray = new ArrayList<>();
        fragmentSparseArray.add(new AvailabilityFragment());
        fragmentSparseArray.add(ScheduleFragment.newInstance("", ""));
        fragmentSparseArray.add(SoSFragment.newInstance("", ""));
        fragmentSparseArray.add(ProfileFragment.newInstance("", ""));
        return fragmentSparseArray;

    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        if (uri.equals(Uri.parse("http://www.google.com"))) {
            bottomBar.selectTabWithId(R.id.tab_nearby);
        }
    }

    @Override
    public void onDrawerItemSelected(View view, int position) {

        switch (position) {
            case 0:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(HomeActivity.this, AddAvailabilityActivity.class));
                        overridePendingTransition(R.anim.zoom_exit, 0);
                    }
                }, 200);


                break;
            case 1:

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(HomeActivity.this, CompletedSchedulesActivity.class));
                        overridePendingTransition(R.anim.zoom_exit, 0);
                    }
                }, 200);

                break;
            case 2:

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(HomeActivity.this, ContactToOperatorActivity.class));
                        overridePendingTransition(R.anim.zoom_exit, 0);
                    }
                }, 200);

                break;
            case 3:

                apiService.logoutDriver(AppController.sharedPreferencesCompat.getInt(AppConstants.DRIVER_ID, 0)).enqueue(new Callback<LogoutResponse>() {
                    @Override
                    public void onResponse(Call<LogoutResponse> call, Response<LogoutResponse> response) {
                        if (response.isSuccessful() && response.body().getStatus()) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    AppController.getSharedPrefEditor().putBoolean(AppConstants.IS_LOGIN, false).apply();
                                    AppController.getSharedPrefEditor().putBoolean(AppConstants.IS_REMEMBER_TAPPED, false).apply();
                                    startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                                    overridePendingTransition(R.anim.zoom_exit, 0);
                                    finish();
                                }
                            }, 200);
                        } else {
                            Snackbar.make(cl,response.body().getMessage(),Snackbar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<LogoutResponse> call, Throwable t) {
                        Snackbar.make(cl,t.getMessage(),Snackbar.LENGTH_SHORT).show();

                        t.printStackTrace();
                    }
                });


                break;
        }


    }
}
