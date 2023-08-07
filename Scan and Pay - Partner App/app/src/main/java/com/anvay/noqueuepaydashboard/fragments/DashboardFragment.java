package com.anvay.noqueuepaydashboard.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anvay.noqueuepaydashboard.adapters.OrdersHistoryAdapter;
import com.anvay.noqueuepaydashboard.databinding.FragmentDashboardBinding;
import com.anvay.noqueuepaydashboard.databinding.LayoutLoadingBinding;
import com.anvay.noqueuepaydashboard.models.Order;
import com.anvay.noqueuepaydashboard.utils.Constants;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {
    private FragmentDashboardBinding binding;
    private View loadingLayout;
    private final List<Order> orderHistoryList = new ArrayList<>();
    private OrdersHistoryAdapter adapter;
    private TextView emptyRecyclerInfo;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        LayoutLoadingBinding layoutLoadingBinding = binding.loadingLayout;
        loadingLayout = layoutLoadingBinding.getRoot();
        emptyRecyclerInfo = binding.emptyRecyclerInfo;
        loadingLayout.setVisibility(View.VISIBLE);
        fetchData();
        RecyclerView recyclerView = binding.ordersHistoryRecycler;
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        adapter = new OrdersHistoryAdapter(orderHistoryList);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
        return root;
    }

    private void fetchData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE);
        String storeId = sharedPreferences.getString(Constants.STORE_ID, "default");
        Log.e("dashboard", "storeId " + storeId);
        db.collection(Constants.BASE_ORDER_URL)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .whereEqualTo(Constants.KEY_STORE_ID, storeId)
                .whereEqualTo(Constants.KEY_IS_VERIFIED, true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    orderHistoryList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Order order = doc.toObject(Order.class);
                        orderHistoryList.add(order);
                    }
                    adapter.notifyDataSetChanged();
                    checkEmptyRecycler();
                    loadingLayout.setVisibility(View.INVISIBLE);
                })
                .addOnFailureListener(e -> {
                    checkEmptyRecycler();
                    Log.e("dashboardFragment",e.getMessage());
                    loadingLayout.setVisibility(View.INVISIBLE);
                });
    }

    private void checkEmptyRecycler() {
        if (orderHistoryList.isEmpty())
            emptyRecyclerInfo.setVisibility(View.VISIBLE);
        else emptyRecyclerInfo.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}