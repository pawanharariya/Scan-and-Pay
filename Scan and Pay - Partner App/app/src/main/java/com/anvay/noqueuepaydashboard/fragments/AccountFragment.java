package com.anvay.noqueuepaydashboard.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.anvay.noqueuepaydashboard.MainActivity;
import com.anvay.noqueuepaydashboard.R;

public class AccountFragment extends Fragment {
    private FragmentManager fragmentManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_account, container, false);
        Button profileButton = rootView.findViewById(R.id.profile_button);
        Button storeIdQR = rootView.findViewById(R.id.store_id_qr);
        Button generateProductQR = rootView.findViewById(R.id.generate_product_qr);
        Button customerCareButton = rootView.findViewById(R.id.customer_care_button);
        Button tacButton = rootView.findViewById(R.id.tac_button);
        fragmentManager = getChildFragmentManager();
        profileButton.setOnClickListener(view -> setFragment(new ProfileFragment(), "Profile"));
        customerCareButton.setOnClickListener(view -> setFragment(new CustomerCareFragment(), "Customer Care"));
        tacButton.setOnClickListener(view -> setFragment(new TermsFragment(), "Terms and Conditions"));
        storeIdQR.setOnClickListener(view -> setFragment(new StoreQRFragment(), "Store's QR Code"));
        // generateProductQR.setOnClickListener(view -> setFragment(new ProductQRFragment()));
        return rootView;
    }

    private void setFragment(Fragment fragment, String tag) {
        fragmentManager.beginTransaction()
                .add(R.id.account_fragment_host, fragment)
                .addToBackStack(tag)
                .commit();
    }
}