package com.anvay.noqueuepaydashboard.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anvay.noqueuepaydashboard.R;
import com.anvay.noqueuepaydashboard.models.Order;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PendingOrdersAdapter extends RecyclerView.Adapter<PendingOrdersAdapter.ViewHolder> {
    private final List<Order> pendingOrdersList;
    private final OrderClickListener listener;

    public PendingOrdersAdapter(List<Order> list, OrderClickListener listener) {
        this.pendingOrdersList = list;
        this.listener = listener;
    }

    @NonNull
    @NotNull
    @Override
    public PendingOrdersAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_pending_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull PendingOrdersAdapter.ViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return pendingOrdersList == null ? 0 : pendingOrdersList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView customerName, orderId, orderPrice, orderQuantity, orderTimestamp;
        ImageButton scanQr;
        Button downloadInvoice;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            customerName = itemView.findViewById(R.id.customer_name);
            orderId = itemView.findViewById(R.id.order_id);
            orderPrice = itemView.findViewById(R.id.order_price);
            orderQuantity = itemView.findViewById(R.id.order_quantity);
            orderTimestamp = itemView.findViewById(R.id.order_timestamp);
            scanQr = itemView.findViewById(R.id.scan_qr);
            downloadInvoice = itemView.findViewById(R.id.download_invoice);
        }

        @SuppressLint("SetTextI18n")
        public void bind(int position) {
            Order order = pendingOrdersList.get(position);
            customerName.setText(order.getCustomerName());
            orderId.setText("Order Id: " + order.getOrderId());
            orderPrice.setText("\u20B9 " + order.getTotalPrice());
            orderTimestamp.setText(order.getDate());
            orderQuantity.setText(order.getTotalQuantity() + " items");
            scanQr.setOnClickListener(view -> listener.onScanQrClicked(position));
            downloadInvoice.setOnClickListener(view -> listener.onDownloadInvoiceClicked(position));
        }
    }

    public interface OrderClickListener {
        void onScanQrClicked(int position);

        void onDownloadInvoiceClicked(int position);
    }
}
