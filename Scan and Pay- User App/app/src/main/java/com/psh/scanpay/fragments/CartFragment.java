package com.psh.scanpay.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.psh.scanpay.CartActivity;
import com.psh.scanpay.R;
import com.psh.scanpay.ScanningActivity;
import com.psh.scanpay.utils.Constants;

public class CartFragment extends Fragment {
    private View loadingLayout;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_cart, container, false);
        loadingLayout = root.findViewById(R.id.loading_layout);
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean(Constants.IS_CART_ACTIVE, false)) {
            Intent i = new Intent(getContext(), CartActivity.class);
            startActivity(i);
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
            navController.popBackStack();
        }
        ActivityResultLauncher<Intent> scanningActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK)
                        processScanResult();
                    else
                        Toast.makeText(getContext(), "Cannot process this QR Code", Toast.LENGTH_SHORT).show();
                });
        Button scanShopButton = root.findViewById(R.id.scan_shop_button);
        scanShopButton.setOnClickListener(view -> {
            Intent i = new Intent(getContext(), ScanningActivity.class);
            i.putExtra(Constants.IS_CART_ACTIVE, false);
            scanningActivityLauncher.launch(i);
        });
        return root;
    }

    private void processScanResult() {
        loadingLayout.setVisibility(View.VISIBLE);
        Intent i = new Intent(getContext(), CartActivity.class);
        startActivity(i);
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
        navController.popBackStack();
    }
}