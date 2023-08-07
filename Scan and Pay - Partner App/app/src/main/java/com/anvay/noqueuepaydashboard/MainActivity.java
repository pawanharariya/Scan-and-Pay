package com.anvay.noqueuepaydashboard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.anvay.noqueuepaydashboard.databinding.ActivityMainBinding;
import com.anvay.noqueuepaydashboard.utils.Constants;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView navView;
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
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREF, MODE_PRIVATE);
        actionBar = Objects.requireNonNull(this.getSupportActionBar());
        actionBar.setDisplayShowCustomEnabled(true);
        if (!sharedPreferences.getBoolean(Constants.IS_LOGGED_IN, false)) {
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
            finish();
        } else if (!sharedPreferences.getBoolean(Constants.IS_PROFILE_DONE, false)) {
            Intent i = new Intent(this, ProfileActivity.class);
            startActivity(i);
            finish();
        }
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        navView = findViewById(R.id.nav_view);
        if (sharedPreferences.getBoolean(Constants.IS_SCAN_ONLY_MODE, false))
            navView.setVisibility(View.INVISIBLE);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder()
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

    public void setActionBarTitle(String title) {
        actionBar.setTitle(title);
    }
}