
package com.psh.scanpay.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.psh.scanpay.R;
import com.psh.scanpay.models.Order;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.ViewHolder> {
    private final List<Order> orderList;
    private final Context context;

    public OrderHistoryAdapter(List<Order> orders, Context context) {
        this.orderList = orders;
        this.context = context;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        TextView storeNameDisplay, orderPriceDisplay, orderIdDisplay, orderTimestampDisplay;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            storeNameDisplay = itemView.findViewById(R.id.store_name_display);
            orderIdDisplay = itemView.findViewById(R.id.order_id_display);
            orderPriceDisplay = itemView.findViewById(R.id.order_price_display);
            orderTimestampDisplay = itemView.findViewById(R.id.order_timestamp_display);
        }

        @SuppressLint("SetTextI18n")
        public void bind(int position) {
            Order order = orderList.get(position);
            storeNameDisplay.setText(order.getStoreName());
            orderIdDisplay.setText("Order ID: " + order.getOrderId());
            orderPriceDisplay.setText("\u20B9 " + order.getTotalPrice());
            orderTimestampDisplay.setText(order.getDate());
        }
    }
}
