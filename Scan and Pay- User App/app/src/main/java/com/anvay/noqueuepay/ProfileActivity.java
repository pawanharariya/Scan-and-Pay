package com.anvay.noqueuepay;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.anvay.noqueuepay.models.User;
import com.anvay.noqueuepay.utils.Constants;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {
    private EditText name, mobileNumber, city, zipcode, email;
    private Button createProfile;
    private View loadingLayout;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Objects.requireNonNull(this.getSupportActionBar()).setTitle("Profile");
        initUI();
        sharedPreferences = getSharedPreferences(Constants.SHARED_PREF, MODE_PRIVATE);
        String mobileNo = sharedPreferences.getString(Constants.MOBILE_NUMBER, "");
        mobileNumber.setText(mobileNo);
        createProfile.setOnClickListener(view -> {
            String nameString = name.getText().toString();
            String cityString = city.getText().toString();
            String zipcodeString = zipcode.getText().toString();
            String emailString = email.getText().toString();
            if (nameString.isEmpty() || cityString.isEmpty() || zipcodeString.isEmpty() || emailString.isEmpty())
                Toast.makeText(this, "Fill all details", Toast.LENGTH_SHORT).show();
            else
                create(nameString, cityString, zipcodeString, mobileNo, emailString);
        });
    }

    private void create(String name, String city, String zipcode, String mobileNumber, String email) {
        loadingLayout.setVisibility(View.VISIBLE);
        String firebaseId = sharedPreferences.getString(Constants.USER_FIREBASE_ID, "");
        User user = new User(name, mobileNumber, city, zipcode, email);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.BASE_USERS_URL)
                .document(firebaseId)
                .set(user)
                .addOnSuccessListener(unused -> completeSetup(user))
                .addOnFailureListener(e -> {
                    Log.e("profile", e.getMessage());
                    Toast.makeText(this, "Error creating profile", Toast.LENGTH_SHORT).show();
                    loadingLayout.setVisibility(View.INVISIBLE);
                });
    }

    private void completeSetup(User user) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.USER_ZIPCODE, user.getZipcode());
        editor.putString(Constants.USER_NAME, user.getName());
        editor.putString(Constants.USER_CITY, user.getCity());
        editor.putString(Constants.USER_EMAIL, user.getEmail());
        editor.putBoolean(Constants.IS_PROFILE_DONE, true);
        editor.apply();
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }

    private void initUI() {
        name = findViewById(R.id.name);
        mobileNumber = findViewById(R.id.mobile_number);
        city = findViewById(R.id.city);
        zipcode = findViewById(R.id.zipcode);
        loadingLayout = findViewById(R.id.loading_layout);
        createProfile = findViewById(R.id.create_profile);
        email = findViewById(R.id.email);
    }
}