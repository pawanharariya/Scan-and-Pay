package com.psh.scanpay.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.psh.scanpay.R;
import com.psh.scanpay.models.Store;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class StoresAdapter extends RecyclerView.Adapter<StoresAdapter.ViewHolder> implements Filterable {
    private final List<Store> stores;
    private final List<Store> allStores;
    private final StoreLocationInterface storeLocationInterface;
    private final List<Store> filteredStores = new ArrayList<>();

    public StoresAdapter(List<Store> stores, StoreLocationInterface storeDirectionsInterface) {
        this.stores = stores;
        this.storeLocationInterface = storeDirectionsInterface;
        this.allStores = new ArrayList<>(stores);
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_store, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return stores.size();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView storeName, storeAddress, storeExtraInfo;
        ImageView storeImageView, storeLocationIcon;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            storeAddress = itemView.findViewById(R.id.store_address_display);
            storeName = itemView.findViewById(R.id.store_name_display);
            storeImageView = itemView.findViewById(R.id.store_image);
            storeLocationIcon = itemView.findViewById(R.id.store_location);
            storeExtraInfo = itemView.findViewById(R.id.store_extra_display);
        }

        public void bind(int position) {
            Store store = stores.get(position);
            Picasso.get()
                    .load(store.getImageUrl())
                    .placeholder(R.drawable.shop_icon)
                    .into(storeImageView);
            storeAddress.setText(store.getAddress());
            storeName.setText(store.getStoreName());
            storeExtraInfo.setText(store.getStoreExtraInfo());
            if (store.getLatitude() == 0 || store.getLongitude() == 0)
                storeLocationIcon.setVisibility(View.INVISIBLE);
            storeLocationIcon.setOnClickListener(view -> storeLocationInterface.onStoreLocationClickListener(position));
        }
    }

    Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            if (charSequence.toString().isEmpty())
                filteredStores.addAll(allStores);
            else for (Store store : allStores) {
                if (store.getStoreName().toLowerCase().contains(charSequence.toString().toLowerCase())
                        || store.getAddress().toLowerCase().contains(charSequence.toString().toLowerCase()))
                    filteredStores.add(store);
            }
            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredStores;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            stores.clear();
            stores.addAll(filteredStores);
            notifyDataSetChanged();
        }
    };

    public interface StoreLocationInterface {
        void onStoreLocationClickListener(int position);
    }
}
