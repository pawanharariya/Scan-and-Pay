package com.psh.scanpay.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.psh.scanpay.R;
import com.psh.scanpay.models.ProductItem;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CartItemsAdapter extends RecyclerView.Adapter<CartItemsAdapter.ViewHolder> {
    private List<ProductItem> itemsList;
    private final CartItemClickListener cartItemClickListener;

    public CartItemsAdapter(List<ProductItem> itemsList, CartItemClickListener cartItemClickListener) {
        this.itemsList = itemsList;
        this.cartItemClickListener = cartItemClickListener;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_cart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return itemsList != null ? itemsList.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView productName, productPrice, productQuantity;
        private final TextView incrementQuantity, decrementQuantity;
        private final ImageView deleteProduct;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.product_name);
            productPrice = itemView.findViewById(R.id.product_price);
            productQuantity = itemView.findViewById(R.id.product_quantity);
            incrementQuantity = itemView.findViewById(R.id.product_quantity_increment);
            decrementQuantity = itemView.findViewById(R.id.product_quantity_decrement);
            deleteProduct = itemView.findViewById(R.id.product_delete);
        }

        public void bind(int position) {
            ProductItem productItem = itemsList.get(position);
            int productId = productItem.getID();
            productName.setText(productItem.getProductName());
            productQuantity.setText(String.valueOf(productItem.getProductQuantity()));
            productPrice.setText(String.valueOf(productItem.getProductPrice()));
            incrementQuantity.setOnClickListener(view -> {
                productItem.incrementQuantity();
                cartItemClickListener.onIncrementQuantityClickListener(productId, position);
            });
            decrementQuantity.setOnClickListener(view -> {
                if (productItem.getProductQuantity() > 1) {
                    productItem.decrementQuantity();
                    cartItemClickListener.onDecrementQuantityClickListener(productId, position);
                }
            });
            deleteProduct.setOnClickListener(view ->
                    cartItemClickListener.onDeleteItemClickListener(productId, position));
        }
    }

    public interface CartItemClickListener {
        void onIncrementQuantityClickListener(int id, int position);

        void onDecrementQuantityClickListener(int id, int position);

        void onDeleteItemClickListener(int id, int position);
    }
}
