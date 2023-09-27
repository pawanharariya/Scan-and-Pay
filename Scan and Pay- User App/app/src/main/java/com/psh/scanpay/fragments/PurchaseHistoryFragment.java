package com.psh.scanpay.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.psh.scanpay.MainActivity;
import com.psh.scanpay.R;
import com.psh.scanpay.adapters.OrderHistoryAdapter;
import com.psh.scanpay.models.Order;
import com.psh.scanpay.utils.AppDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PurchaseHistoryFragment extends Fragment {

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        ((MainActivity) requireActivity()).setActionBarTitle("Purchase History");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_purchase_history, container, false);
        TextView emptyRecyclerView = root.findViewById(R.id.empty_recycler_view);
        RecyclerView ordersRecycler = root.findViewById(R.id.orders_recycler);
        AppDatabase database = AppDatabase.getInstance(getContext());
        List<Order> orderList = database.appDao().getAllOrders();
        OrderHistoryAdapter adapter = new OrderHistoryAdapter(orderList, getContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        ordersRecycler.setLayoutManager(linearLayoutManager);
        ordersRecycler.setAdapter(adapter);
        if (orderList == null || orderList.size() == 0)
            emptyRecyclerView.setVisibility(View.VISIBLE);
        else emptyRecyclerView.setVisibility(View.INVISIBLE);
        return root;
    }
}