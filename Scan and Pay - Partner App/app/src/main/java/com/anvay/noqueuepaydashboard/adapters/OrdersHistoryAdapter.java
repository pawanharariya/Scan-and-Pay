package com.anvay.noqueuepaydashboard.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anvay.noqueuepaydashboard.R;
import com.anvay.noqueuepaydashboard.models.Order;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class OrdersHistoryAdapter extends RecyclerView.Adapter<OrdersHistoryAdapter.ViewHolder> {
    List<Order> orderList;


    public OrdersHistoryAdapter(List<Order> orderList) {
        this.orderList = orderList;
    }

    @NonNull
    @NotNull
    @Override
    public OrdersHistoryAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_order_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull OrdersHistoryAdapter.ViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return orderList == null ? 0 : orderList.size();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        TextView orderTimestamp, orderId, orderPrice, orderQuantity;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            orderId = itemView.findViewById(R.id.order_id);
            orderTimestamp = itemView.findViewById(R.id.order_timestamp);
            orderPrice = itemView.findViewById(R.id.order_price);
            orderQuantity = itemView.findViewById(R.id.order_quantity);
        }

        @SuppressLint("SetTextI18n")
        public void bind(int position) {
            Order order = orderList.get(position);
            orderId.setText("Order ID: " + order.getOrderId());
            orderPrice.setText("\u20b9 " + order.getTotalPrice());
            orderQuantity.setText(order.getTotalQuantity() + " items");
            orderTimestamp.setText(order.getDate());
        }
    }
}
