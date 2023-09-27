package com.psh.scanpay.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.psh.scanpay.MainActivity;
import com.psh.scanpay.R;

import org.jetbrains.annotations.NotNull;

public class TermsFragment extends Fragment {

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        ((MainActivity) requireActivity()).setActionBarTitle("Terms and Conditions");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_terms, container, false);
    }
}