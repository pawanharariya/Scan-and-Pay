package com.anvay.noqueuepay.fragments;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anvay.noqueuepay.MainActivity;
import com.anvay.noqueuepay.R;
import com.anvay.noqueuepay.adapters.StoresAdapter;
import com.anvay.noqueuepay.models.Store;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SearchStoreFragment extends Fragment implements StoresAdapter.StoreLocationInterface,
        MainActivity.StoresUpdateListener {
    private List<Store> stores;
    private StoresAdapter adapter;
    private TextView emptyRecyclerView;

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        ((MainActivity) requireActivity()).initializeStoreListener(this);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_search_store, container, false);
        RecyclerView storesRecycler = root.findViewById(R.id.stores_recycler);
        emptyRecyclerView = root.findViewById(R.id.empty_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        stores = ((MainActivity) requireActivity()).getAllStores();
        storesRecycler.setLayoutManager(linearLayoutManager);
        checkEmptyRecycler();
        adapter = new StoresAdapter(stores, this);
        storesRecycler.setAdapter(adapter);
        SearchView searchBox = root.findViewById(R.id.search_box);
        searchBox.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null)
                    adapter.getFilter().filter(newText);
                return false;
            }
        });
        return root;
    }

    private void checkEmptyRecycler() {
        if (stores == null || stores.size() == 0)
            emptyRecyclerView.setVisibility(View.VISIBLE);
        else emptyRecyclerView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onStoreLocationClickListener(int position) {
        Store store = stores.get(position);
        double lat = store.getLatitude();
        double lng = store.getLongitude();
        Uri navigationIntentUri = Uri.parse("google.navigation:q=" + lat + "," + lng);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, navigationIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        try {
            startActivity(mapIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), "Google Maps app is not installed.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void updateStoreRecycler() {
        adapter.notifyDataSetChanged();
        checkEmptyRecycler();
    }
}