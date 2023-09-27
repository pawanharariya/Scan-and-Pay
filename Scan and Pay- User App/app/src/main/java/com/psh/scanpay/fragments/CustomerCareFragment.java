package com.psh.scanpay.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.psh.scanpay.MainActivity;
import com.psh.scanpay.R;
import com.psh.scanpay.models.Complaint;
import com.psh.scanpay.utils.Constants;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class CustomerCareFragment extends Fragment {
    private View loadingLayout;
    private Dialog dialog;
    private EditText descriptionTextBox;

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        ((MainActivity) requireActivity()).setActionBarTitle("Customer Care");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_customer_care, container, false);
        Spinner titleSpinner = root.findViewById(R.id.title_spinner);
        loadingLayout = root.findViewById(R.id.loading_layout);
        descriptionTextBox = root.findViewById(R.id.description_text_box);
        Button submitButton = root.findViewById(R.id.submit_button);
        SharedPreferences sharedPreferences = requireActivity()
                .getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString(Constants.USER_FIREBASE_ID, "default");
        submitButton.setOnClickListener(view -> {
            String title = titleSpinner.getSelectedItem().toString();
            String description = descriptionTextBox.getText().toString();
            if (title.isEmpty() || description.isEmpty())
                Toast.makeText(getContext(), "Fill all fields", Toast.LENGTH_SHORT).show();
            else
                uploadData(userId, title, description);
        });
        return root;
    }

    private void uploadData(String userId, String title, String description) {
        loadingLayout.setVisibility(View.VISIBLE);
        Random rnd = new Random();
        String ticketNumber = String.valueOf(100000 + rnd.nextInt(900000));
        Complaint complaint = new Complaint(title, description, userId, ticketNumber);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.COMPLAINTS_BASE_URL)
                .add(complaint)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Successfully Submitted", Toast.LENGTH_SHORT).show();
                    showInfoDialog(ticketNumber);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error uploading data", Toast.LENGTH_SHORT).show();
                    loadingLayout.setVisibility(View.INVISIBLE);
                });
    }

    private void showInfoDialog(String ticketNumber) {
        loadingLayout.setVisibility(View.GONE);
        descriptionTextBox.setText("");
        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_info);
        int width = WindowManager.LayoutParams.MATCH_PARENT;
        int height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setLayout(width, height);
        dialog.show();
        TextView infoText = dialog.findViewById(R.id.info_text);
        TextView boldText = dialog.findViewById(R.id.bold_text);
        Button confirm = dialog.findViewById(R.id.confirm_button);
        infoText.setText(R.string.complaint_ticket_text);
        boldText.setText("Ticket Number: " + ticketNumber);
        boldText.setVisibility(View.VISIBLE);
        confirm.setOnClickListener(view -> {
            dialog.dismiss();
            requireActivity().onBackPressed();
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (dialog != null)
            dialog.dismiss();
    }
}