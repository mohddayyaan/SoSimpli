package com.think360.sosimpli.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.adapters.FooterAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter_extensions.drag.SimpleDragCallback;
import com.mikepenz.fastadapter_extensions.items.ProgressItem;
import com.mikepenz.fastadapter_extensions.scroll.EndlessRecyclerOnScrollListener;
import com.think360.sosimpli.AppController;
import com.think360.sosimpli.R;
import com.think360.sosimpli.manager.ApiService;
import com.think360.sosimpli.model.ApprovedNonResponse;
import com.think360.sosimpli.model.adapter_items.ScheduleItem;
import com.think360.sosimpli.ui.fragments.ScheduleFragment;
import com.think360.sosimpli.utils.AppConstants;
import com.think360.sosimpli.widgets.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NonApprovedActivity extends AppCompatActivity {

    @Inject
    ApiService apiService;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    //save our FastAdapter
    private FastItemAdapter<ScheduleItem> fastItemAdapter;
    private FooterAdapter<ProgressItem> footerAdapter;

    //drag & drop
    private SimpleDragCallback touchCallback;
    private ItemTouchHelper touchHelper;
    private SwipeRefreshLayout swipeLayout;
    private TextView tvNoData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_non_approved);
        ButterKnife.bind(this);
        ((AppController) getApplication()).getComponent().inject(NonApprovedActivity.this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Non Approved Shifts");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipeLayout);
        tvNoData = (TextView) findViewById(R.id.tvNoData);
        //create our FastAdapter which will manage everything
        fastItemAdapter = new FastItemAdapter<>();
        fastItemAdapter.withSelectable(true);

        //create our FooterAdapter which will manage the progress items
        footerAdapter = new FooterAdapter<>();

        //configure our fastAdapter
        fastItemAdapter.withOnClickListener(new FastAdapter.OnClickListener<ScheduleItem>() {
            @Override
            public boolean onClick(View v, IAdapter<ScheduleItem> adapter, ScheduleItem item, int position) {
                Toast.makeText(v.getContext(), (item).name.getText(v.getContext()), Toast.LENGTH_LONG).show();
                return false;
            }
        });


        //get our recyclerView and do basic setup
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(NonApprovedActivity.this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(footerAdapter.wrap(fastItemAdapter));
        recyclerView.addItemDecoration(new DividerItemDecoration(NonApprovedActivity.this, R.drawable.recyclerview_divider));


        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener(footerAdapter) {
            @Override
            public void onLoadMore(final int currentPage) {
                footerAdapter.clear();
                footerAdapter.add(new ProgressItem().withEnabled(false));
             /*   //simulate networking (2 seconds)
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        footerAdapter.clear();
                        for (int i = 1; i < 16; i++) {
                            fastItemAdapter.add(fastItemAdapter.getAdapterItemCount(), new ScheduleItem().withName("Item " + i + " Page " + currentPage));
                        }
                    }
                }, 2000);*/
            }
        });

        fetchDataFromRemote();
        //fill with some sample data (load the first page here)
    /*    List<ScheduleItem> items = new ArrayList<>();
        for (int i = 1; i < 16; i++) {
            items.add(new ScheduleItem().withName("Item " + i + " Page " + 1));
        }
        fastItemAdapter.add(items);*/

        //restore selections (this has to be done after the items were added
        fastItemAdapter.withSavedInstanceState(savedInstanceState);

        fastItemAdapter.withOnClickListener(new FastAdapter.OnClickListener<ScheduleItem>() {
            @Override
            public boolean onClick(View v, IAdapter<ScheduleItem> adapter, ScheduleItem item, int position) {

                startActivity(new Intent(NonApprovedActivity.this, AssignedScheduleDeatilActivity.class));

                return true;
            }
        });

        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchDataFromRemote();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    private void fetchDataFromRemote() {
        swipeLayout.setRefreshing(true);
        apiService.getAvailabilityApproveNonApprove(AppController.sharedPreferencesCompat.getInt(AppConstants.DRIVER_ID, 0), 0).enqueue(new Callback<ApprovedNonResponse>() {
            @Override
            public void onResponse(Call<ApprovedNonResponse> call, Response<ApprovedNonResponse> response) {
                swipeLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body().getStatus()) {

                    if (response.body().getData().size() > 0) {
                        tvNoData.setVisibility(View.GONE);
                        //fill with some sample data (load the first page here)
                        List<ScheduleItem> items = new ArrayList<>();
                        for (int i = 1; i < response.body().getData().size(); i++) {
                            items.add(new ScheduleItem().withItem(response.body().getData().get(i)));
                        }
                        fastItemAdapter.add(items);
                    }

                } else {
                    tvNoData.setVisibility(View.VISIBLE);
                    // Toast.makeText(getActivity(), response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d(ScheduleFragment.class.getSimpleName(), response.body().getMessage());
                }
            }

            @Override
            public void onFailure(Call<ApprovedNonResponse> call, Throwable t) {
                swipeLayout.setRefreshing(false);
                tvNoData.setVisibility(View.VISIBLE);
            }
        });
    }
}
