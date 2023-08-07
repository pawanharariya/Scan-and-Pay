package com.anvay.noqueuepaydashboard.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.anvay.noqueuepaydashboard.MainActivity;
import com.anvay.noqueuepaydashboard.R;
import com.anvay.noqueuepaydashboard.utils.Constants;
import com.anvay.noqueuepaydashboard.utils.QrGenerator;

public class ProductQRFragment extends Fragment {

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        ((MainActivity) requireActivity()).setActionBarTitle("Product's QR");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_product_qr, container, false);
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(Constants.SHARED_PREF,
                Context.MODE_PRIVATE);
        String storeId = sharedPreferences.getString(Constants.STORE_ID, "default");
        ImageView qrDisplay = root.findViewById(R.id.qr_display);
        Bitmap qr = QrGenerator.generateQRCode(storeId, requireContext());
        if (qr != null)
            qrDisplay.setImageBitmap(qr);
        else
            Toast.makeText(getContext(), "Error generating QR code", Toast.LENGTH_SHORT).show();
        return root;
    }
}