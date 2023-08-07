package com.anvay.noqueuepay;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.anvay.noqueuepay.databinding.ActivityMainBinding;
import com.anvay.noqueuepay.databinding.LayoutLoadingBinding;
import com.anvay.noqueuepay.models.Store;
import com.anvay.noqueuepay.utils.AppDatabase;
import com.anvay.noqueuepay.utils.Constants;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView navView;
    private View navigationStore;
    private View loadingView;
    private final List<Store> allStores = new ArrayList<>();
    private String zipcode;
    private StoresUpdateListener storesUpdateListener;
    private MapUpdateListener mapUpdateListener;
    private ActionBar actionBar;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() == 0 && navView.getSelectedItemId() == R.id.navigation_account)
            this.setActionBarTitle("Account");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        actionBar = getSupportActionBar();
        assert actionBar != null;
        LayoutLoadingBinding loadingLayout = binding.loadingLayout;
        loadingView = loadingLayout.getRoot();
        navView = findViewById(R.id.nav_view);
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREF, MODE_PRIVATE);
        if (!sharedPreferences.getBoolean(Constants.IS_LOGGED_IN, false)) {
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
            finish();
        } else if (!sharedPreferences.getBoolean(Constants.IS_PROFILE_DONE, false)) {
            Intent i = new Intent(this, ProfileActivity.class);
            startActivity(i);
            finish();
        }
        zipcode = sharedPreferences.getString(Constants.USER_ZIPCODE, "263139");
        if (!sharedPreferences.getBoolean(Constants.IS_CART_ACTIVE, true))
            AppDatabase.getInstance(this).appDao().deleteAllCartItems();
        getStoresFromFirestore();
        navigationStore = navView.findViewById(R.id.navigation_store);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder()
                .setFallbackOnNavigateUpListener(() -> {
                    onBackPressed();
                    return true;
                })
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private void getStoresFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
        db.collection(Constants.STORES_BASE_URL)
                .whereEqualTo(Constants.KEY_STORE_ZIPCODE, zipcode)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Store store = doc.toObject(Store.class);
                        allStores.add(store);
                        setupListeners();
                        loadingView.setVisibility(View.INVISIBLE);
                    }
                })
                .addOnFailureListener(e -> loadingView.setVisibility(View.INVISIBLE));
    }

    private void setupListeners() {
        int id = navView.getSelectedItemId();
        if (id == R.id.navigation_store && storesUpdateListener != null)
            storesUpdateListener.updateStoreRecycler();
        else if (id == R.id.navigation_home && mapUpdateListener != null)
            mapUpdateListener.updateHomeFragmentUI();
    }

    public void onSearchBoxClicked(View view) {
        navigationStore.performClick();
    }

    public List<Store> getAllStores() {
        return allStores;
    }

    public interface MapUpdateListener {
        void updateHomeFragmentUI();
    }

    public interface StoresUpdateListener {
        void updateStoreRecycler();
    }

    public void initializeMapListener(MapUpdateListener mapUpdateListener) {
        this.mapUpdateListener = mapUpdateListener;
    }

    public void initializeStoreListener(StoresUpdateListener storesUpdateListener) {
        this.storesUpdateListener = storesUpdateListener;
    }

    public void setActionBarTitle(String title) {
        actionBar.setTitle(title);
    }
}