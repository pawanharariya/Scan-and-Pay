package com.psh.scanpay.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.psh.scanpay.R;

public class AccountFragment extends Fragment {
    private View rootView;
    private FragmentManager fragmentManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_account, container, false);
        Button profileButton = rootView.findViewById(R.id.profile_button);
        Button purchaseHistoryButton = rootView.findViewById(R.id.purchase_history_button);
        Button customerCareButton = rootView.findViewById(R.id.customer_care_button);
        Button tacButton = rootView.findViewById(R.id.tac_button);
        fragmentManager = getChildFragmentManager();
        profileButton.setOnClickListener(view -> setFragment(new ProfileFragment()));
        purchaseHistoryButton.setOnClickListener(view -> setFragment(new PurchaseHistoryFragment()));
        customerCareButton.setOnClickListener(view -> setFragment(new CustomerCareFragment()));
        tacButton.setOnClickListener(view -> setFragment(new TermsFragment()));
        return rootView;
    }

    private void setFragment(Fragment fragment) {
        fragmentManager.beginTransaction()
                .add(R.id.account_fragment_host, fragment)
                .addToBackStack(null)
                .commit();
    }
}