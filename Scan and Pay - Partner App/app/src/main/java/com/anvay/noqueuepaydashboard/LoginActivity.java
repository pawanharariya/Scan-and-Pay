package com.anvay.noqueuepaydashboard;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.anvay.noqueuepaydashboard.utils.Constants;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private View loadingLayout;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Objects.requireNonNull(this.getSupportActionBar()).setTitle("Login");
        Button loginButton = findViewById(R.id.login_button);
        Button scanOnlyButton = findViewById(R.id.scan_only_button);
        loadingLayout = findViewById(R.id.loading_layout);
        sharedPreferences = getSharedPreferences(Constants.SHARED_PREF, MODE_PRIVATE);
        List<AuthUI.IdpConfig> providers = Collections.singletonList(new AuthUI.IdpConfig.PhoneBuilder().build());
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setLogo(R.drawable.logo)
                .build();
        ActivityResultLauncher<Intent> scanLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::processScanResult);
        loginButton.setOnClickListener(view -> signInLauncher.launch(signInIntent));
        Intent scanIntent = new Intent(this, ScanningActivity.class);
        scanOnlyButton.setOnClickListener(view -> scanLauncher.launch(scanIntent));
    }

    private void processScanResult(ActivityResult result) {
        loadingLayout.setVisibility(View.VISIBLE);
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent intent = result.getData();
            assert intent != null;
            String storeId = intent.getStringExtra(Constants.STORE_ID);
            SharedPreferences sharedPreferences = getSharedPreferences("app", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(Constants.STORE_ID, storeId);
            editor.putBoolean(Constants.IS_SCAN_ONLY_MODE, true);
            editor.putBoolean(Constants.IS_LOGGED_IN, true);
            editor.putBoolean(Constants.IS_PROFILE_DONE, true);
            editor.apply();
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            finish();
        }
    }

    private void processSignInResult(FirebaseAuthUIAuthenticationResult result) {
        loadingLayout.setVisibility(View.VISIBLE);
        if (result.getResultCode() == Activity.RESULT_OK) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            assert user != null;
            editor.putString(Constants.MOBILE_NUMBER, user.getPhoneNumber());
            editor.putString(Constants.STORE_ID, user.getUid());
            editor.putBoolean(Constants.IS_LOGGED_IN, true);
            editor.apply();
            checkIfUserExists(user.getUid());
        } else {
            Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show();
            loadingLayout.setVisibility(View.GONE);
        }
    }

    private void checkIfUserExists(String storeId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.BASE_STORES_URL)
                .document(storeId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot snapshot = task.getResult();
                        if (snapshot.exists()) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean(Constants.IS_PROFILE_DONE, true);
                            editor.apply();
                        }
                    }
                    if (sharedPreferences.getBoolean(Constants.IS_PROFILE_DONE, false)) {
                        Intent i = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(i);
                    } else {
                        Intent i = new Intent(LoginActivity.this, ProfileActivity.class);
                        startActivity(i);
                    }
                    finish();
                });
    }

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(), this::processSignInResult);
}