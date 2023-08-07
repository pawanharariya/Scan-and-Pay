package com.anvay.noqueuepay.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.anvay.noqueuepay.MainActivity;
import com.anvay.noqueuepay.R;
import com.anvay.noqueuepay.models.User;
import com.anvay.noqueuepay.utils.Constants;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class ProfileFragment extends Fragment {
    private View root, loadingLayout;
    private EditText name, mobileNumber, city, zipcode, email;
    private Button editProfile, updateProfile;
    private SharedPreferences sharedPreferences;
    private String nameString, mobileNoString, cityString, zipcodeString, emailString;

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        ((MainActivity) requireActivity()).setActionBarTitle("Profile");
    }

    @Override
    public void onStop() {
        Log.e("Profile Fragment","On Stop called");
        super.onStop();
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_profile, container, false);
        initUI();
        sharedPreferences = requireContext().getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE);
        setupFields(false);
        nameString = sharedPreferences.getString(Constants.USER_NAME, "");
        mobileNoString = sharedPreferences.getString(Constants.MOBILE_NUMBER, "");
        cityString = sharedPreferences.getString(Constants.USER_CITY, "");
        zipcodeString = sharedPreferences.getString(Constants.USER_ZIPCODE, "");
        emailString = sharedPreferences.getString(Constants.USER_EMAIL, "");
        name.setText(nameString);
        mobileNumber.setText(mobileNoString);
        city.setText(cityString);
        zipcode.setText(zipcodeString);
        email.setText(emailString);
        editProfile.setOnClickListener(view -> setupFields(true));
        updateProfile.setOnClickListener(view -> {
            nameString = name.getText().toString();
            cityString = city.getText().toString();
            zipcodeString = zipcode.getText().toString();
            emailString = email.getText().toString();
            if (nameString.isEmpty() || cityString.isEmpty() || zipcodeString.isEmpty() || emailString.isEmpty())
                Toast.makeText(getContext(), "Fill all details", Toast.LENGTH_SHORT).show();
            else
                updateFirebase(nameString, cityString, zipcodeString, mobileNoString, emailString);
        });
        return root;
    }

    private void setupFields(boolean editMode) {
        name.setEnabled(editMode);
        city.setEnabled(editMode);
        zipcode.setEnabled(editMode);
        email.setEnabled(editMode);
        if (editMode) {
            editProfile.setVisibility(View.INVISIBLE);
            updateProfile.setVisibility(View.VISIBLE);
        } else {
            editProfile.setVisibility(View.VISIBLE);
            updateProfile.setVisibility(View.INVISIBLE);
        }
    }

    private void initUI() {
        name = root.findViewById(R.id.name);
        mobileNumber = root.findViewById(R.id.mobile_number);
        city = root.findViewById(R.id.city);
        zipcode = root.findViewById(R.id.zipcode);
        editProfile = root.findViewById(R.id.edit_profile);
        loadingLayout = root.findViewById(R.id.loading_layout);
        updateProfile = root.findViewById(R.id.update_profile);
        email = root.findViewById(R.id.email);
    }

    private void updateLocal(User user) {
        setupFields(false);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.USER_ZIPCODE, user.getZipcode());
        editor.putString(Constants.USER_NAME, user.getName());
        editor.putString(Constants.USER_CITY, user.getCity());
        editor.putString(Constants.USER_EMAIL, user.getEmail());
        editor.apply();
        loadingLayout.setVisibility(View.INVISIBLE);
    }

    private void updateFirebase(String name, String city, String zipcode, String mobileNumber, String email) {
        loadingLayout.setVisibility(View.VISIBLE);
        String firebaseId = sharedPreferences.getString(Constants.USER_FIREBASE_ID, "");
        User user = new User(name, mobileNumber, city, zipcode, email);
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("city", city);
        map.put("zipcode", zipcode);
        map.put("email", email);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.BASE_USERS_URL)
                .document(firebaseId)
                .update(map)
                .addOnSuccessListener(unused -> updateLocal(user))
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error updating profile", Toast.LENGTH_SHORT).show();
                    loadingLayout.setVisibility(View.INVISIBLE);
                });
    }
}