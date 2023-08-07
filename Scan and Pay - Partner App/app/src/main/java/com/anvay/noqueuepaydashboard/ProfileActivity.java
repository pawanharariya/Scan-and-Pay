package com.anvay.noqueuepaydashboard;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.anvay.noqueuepaydashboard.models.Store;
import com.anvay.noqueuepaydashboard.utils.Constants;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {
    private EditText storeNameDisplay, mobileNumberDisplay, upiIdDisplay, storeAddressDisplay, zipCodeDisplay,
            extraInfoDisplay, emailDisplay, passwordDisplay;
    private Button createProfile;
    private ShapeableImageView storeImageDisplay;
    private ImageButton imageCapture;
    private View loadingLayout;
    private SharedPreferences sharedPreferences;
    private String imageUrl, storeId, mobileNumber, zipcode, storeName, storeAddress, email, upiId, extraInfo, password;
    private byte[] byteArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Objects.requireNonNull(this.getSupportActionBar()).setTitle("Profile");
        initUI();
        sharedPreferences = getSharedPreferences(Constants.SHARED_PREF, MODE_PRIVATE);
        mobileNumber = sharedPreferences.getString(Constants.MOBILE_NUMBER, "");
        storeId = sharedPreferences.getString(Constants.STORE_ID, "");
        mobileNumberDisplay.setText(mobileNumber);
        createProfile.setOnClickListener(view -> {
            loadingLayout.setVisibility(View.VISIBLE);
            storeName = storeNameDisplay.getText().toString();
            storeAddress = storeAddressDisplay.getText().toString();
            zipcode = zipCodeDisplay.getText().toString();
            upiId = upiIdDisplay.getText().toString();
            extraInfo = extraInfoDisplay.getText().toString();
            email = emailDisplay.getText().toString();
            password = passwordDisplay.getText().toString();
            if (!Constants.validateUPI(upiId)) {
                Toast.makeText(this, "Invalid UPI ID", Toast.LENGTH_SHORT).show();
                loadingLayout.setVisibility(View.INVISIBLE);
            } else if (storeName.isEmpty() || storeAddress.isEmpty() || zipcode.isEmpty() || upiId.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Fill all details", Toast.LENGTH_SHORT).show();
                loadingLayout.setVisibility(View.INVISIBLE);
            } else if (byteArray != null)
                postPicture();
            else
                createProfile();
        });
        ActivityResultLauncher<Intent> imageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK)
                        processResult(result);
                }
        );
        imageCapture.setOnClickListener(view -> {
            Intent gallery = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            gallery.setType("image/*");
            if (gallery.resolveActivity(getPackageManager()) != null) {
                imageLauncher.launch(gallery);
            }
        });
        storeImageDisplay.setOnClickListener(view -> imageCapture.performClick());
    }

    private void processResult(ActivityResult result) {
        Intent data = result.getData();
        if (data != null) {
            Uri photoUri = data.getData();
            try {
                Bitmap profileImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
                Bitmap img = getResizedBitmap(profileImage, 150);
                ByteArrayOutputStream bs = new ByteArrayOutputStream();
                img.compress(Bitmap.CompressFormat.JPEG, 70, bs);
                byteArray = bs.toByteArray();
                Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                storeImageDisplay.setImageBitmap(bmp);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Cannot load this image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void createProfile() {
        loadingLayout.setVisibility(View.VISIBLE);
        AuthCredential credential = EmailAuthProvider.getCredential(email, password);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;
        firebaseUser.linkWithCredential(credential)
                .addOnSuccessListener(authResult -> Log.e("auth", Objects.requireNonNull(authResult.getUser()).getEmail()))
                .addOnFailureListener(e -> Log.e("auth", e.getMessage() + e.getCause()));
        LatLng latLng = getLocationFromAddress(storeAddress);
        Store store = new Store(storeId, storeName, storeAddress, upiId, imageUrl, extraInfo,
                mobileNumber, email, zipcode, latLng.latitude, latLng.longitude);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.BASE_STORES_URL)
                .document(storeId)
                .set(store)
                .addOnSuccessListener(unused -> completeSetup(store))
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error creating profile", Toast.LENGTH_SHORT).show();
                    loadingLayout.setVisibility(View.INVISIBLE);
                });
    }

    private void completeSetup(Store store) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constants.IS_PROFILE_DONE, true);
        editor.apply();
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }

    private void initUI() {
        createProfile = findViewById(R.id.create_profile);
        imageCapture = findViewById(R.id.store_image_capture);
        loadingLayout = findViewById(R.id.loading_layout);
        storeNameDisplay = findViewById(R.id.store_name_display);
        storeAddressDisplay = findViewById(R.id.store_address_display);
        storeImageDisplay = findViewById(R.id.store_image_display);
        zipCodeDisplay = findViewById(R.id.zipcode_display);
        extraInfoDisplay = findViewById(R.id.extra_info_display);
        mobileNumberDisplay = findViewById(R.id.mobile_number_display);
        upiIdDisplay = findViewById(R.id.upi_id_display);
        emailDisplay = findViewById(R.id.email_display);
        passwordDisplay = findViewById(R.id.password_display);
    }

    public LatLng getLocationFromAddress(String strAddress) {
        Geocoder coder = new Geocoder(this);
        List<Address> address;
        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address != null && !address.isEmpty()) {
                Address location = address.get(0);
                return new LatLng(location.getLatitude(), location.getLongitude());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new LatLng(0, 0);
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    private void postPicture() {
        loadingLayout.setVisibility(View.VISIBLE);
        final StorageReference image = FirebaseStorage
                .getInstance().getReference().child("storeImages/" + storeId);
        UploadTask uploadTask = image.putBytes(byteArray);
        uploadTask.addOnSuccessListener(taskSnapshot -> image
                .getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    imageUrl = uri.toString();
                    createProfile();
                }));
    }
}