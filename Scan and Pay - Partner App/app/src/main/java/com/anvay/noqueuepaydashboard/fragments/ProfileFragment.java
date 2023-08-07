package com.anvay.noqueuepaydashboard.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.anvay.noqueuepaydashboard.MainActivity;
import com.anvay.noqueuepaydashboard.R;
import com.anvay.noqueuepaydashboard.models.Store;
import com.anvay.noqueuepaydashboard.utils.Constants;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class ProfileFragment extends Fragment {
    private View root, loadingLayout;
    private EditText storeNameDisplay, mobileNumberDisplay, upiIdDisplay, storeAddressDisplay, zipCodeDisplay,
            extraInfoDisplay, emailDisplay;
    private Button editProfile, updateProfile;
    private ShapeableImageView storeImageDisplay;
    private ImageButton imageCapture;
    private String storeName, storeAddress, imageUrl, storeId, mobileNumber, email, zipcode, extraInfo, upiId,
            imageUrlNew, storeAddressNew;
    private byte[] byteArray;
    private Store store;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        ((MainActivity) requireActivity()).setActionBarTitle("Profile");
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_profile, container, false);
        initUI();
        setupFields(false);
        loadingLayout.setVisibility(View.VISIBLE);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        storeId = user.getUid();
        fetchProfile(storeId);
        editProfile.setOnClickListener(view -> setupFields(true));
        updateProfile.setOnClickListener(view -> {
            storeName = storeNameDisplay.getText().toString();
            storeAddressNew = storeAddressDisplay.getText().toString();
            upiId = upiIdDisplay.getText().toString();
            email = emailDisplay.getText().toString();
            extraInfo = extraInfoDisplay.getText().toString();
            if (!Constants.validateUPI(upiId)) {
                Toast.makeText(getContext(), "Invalid UPI ID", Toast.LENGTH_SHORT).show();
                loadingLayout.setVisibility(View.INVISIBLE);
            }
            else if (storeName.isEmpty() || storeAddressNew.isEmpty() || upiId.isEmpty() || email.isEmpty())
                Toast.makeText(getContext(), "Fill all details", Toast.LENGTH_SHORT).show();
            else if (byteArray != null)
                postPicture();
            else
                updateOnFirestore();
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
            if (gallery.resolveActivity(requireActivity().getPackageManager()) != null) {
                imageLauncher.launch(gallery);
            }
        });
        storeImageDisplay.setOnClickListener(view -> imageCapture.performClick());
        return root;
    }

    private void fetchProfile(String storeId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.BASE_STORES_URL)
                .document(storeId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    store = documentSnapshot.toObject(Store.class);
                    updateUI();
                    loadingLayout.setVisibility(View.INVISIBLE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load details", Toast.LENGTH_SHORT).show();
                    loadingLayout.setVisibility(View.INVISIBLE);
                });
    }

    private void updateUI() {
        storeName = store.getStoreName();
        mobileNumber = store.getMobileNumber();
        storeAddress = store.getAddress();
        zipcode = store.getZipcode();
        email = store.getEmail();
        upiId = store.getStoreUpiId();
        extraInfo = store.getStoreExtraInfo();
        imageUrl = store.getImageUrl();
        Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.shop_icon)
                .into(storeImageDisplay);
        storeNameDisplay.setText(storeName);
        emailDisplay.setText(email);
        mobileNumberDisplay.setText(mobileNumber);
        storeAddressDisplay.setText(storeAddress);
        zipCodeDisplay.setText(zipcode);
        extraInfoDisplay.setText(extraInfo);
        upiIdDisplay.setText(upiId);
    }

    private void setupFields(boolean editMode) {
        storeNameDisplay.setEnabled(editMode);
        storeAddressDisplay.setEnabled(editMode);
        upiIdDisplay.setEnabled(editMode);
        storeImageDisplay.setEnabled(editMode);
        emailDisplay.setEnabled(editMode);
        extraInfoDisplay.setEnabled(editMode);
        if (editMode) {
            editProfile.setVisibility(View.INVISIBLE);
            imageCapture.setVisibility(View.VISIBLE);
            updateProfile.setVisibility(View.VISIBLE);
        } else {
            editProfile.setVisibility(View.VISIBLE);
            imageCapture.setVisibility(View.INVISIBLE);
            updateProfile.setVisibility(View.INVISIBLE);
        }
    }

    private void updateOnFirestore() {
        loadingLayout.setVisibility(View.VISIBLE);
        HashMap<String, Object> map = new HashMap<>();
        if (!storeAddressNew.equals(storeAddress)) {
            LatLng latLng = getLocationFromAddress(storeAddressNew);
            storeAddress = storeAddressNew;
            map.put(Constants.STORE_ADDRESS, storeAddressNew);
            map.put(Constants.LATITUDE, latLng.latitude);
            map.put(Constants.LONGITUDE, latLng.longitude);
        }
        map.put(Constants.STORE_UPI_ID, upiId);
        map.put(Constants.STORE_NAME, storeName);
        map.put(Constants.STORE_EXTRA_INFO, extraInfo);
        map.put(Constants.STORE_EMAIL, email);
        if (imageUrlNew != null) {
            map.put(Constants.STORE_IMAGE_URL, imageUrlNew);
            imageUrl = imageUrlNew;
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.BASE_STORES_URL)
                .document(storeId)
                .update(map)
                .addOnSuccessListener(unused -> {
                    setupFields(false);
                    loadingLayout.setVisibility(View.INVISIBLE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error updating profile", Toast.LENGTH_SHORT).show();
                    loadingLayout.setVisibility(View.INVISIBLE);
                });
    }

    private void initUI() {
        editProfile = root.findViewById(R.id.edit_profile);
        updateProfile = root.findViewById(R.id.update_profile);
        imageCapture = root.findViewById(R.id.store_image_capture);
        loadingLayout = root.findViewById(R.id.loading_layout);
        storeNameDisplay = root.findViewById(R.id.store_name_display);
        storeAddressDisplay = root.findViewById(R.id.store_address_display);
        storeImageDisplay = root.findViewById(R.id.store_image_display);
        zipCodeDisplay = root.findViewById(R.id.zipcode_display);
        extraInfoDisplay = root.findViewById(R.id.extra_info_display);
        emailDisplay = root.findViewById(R.id.email_display);
        mobileNumberDisplay = root.findViewById(R.id.mobile_number_display);
        upiIdDisplay = root.findViewById(R.id.upi_id_display);
    }

    public LatLng getLocationFromAddress(String strAddress) {
        Geocoder coder = new Geocoder(getContext());
        List<Address> address;
        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address != null) {
                Address location = address.get(0);
                return new LatLng(location.getLatitude(), location.getLongitude());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new LatLng(0, 0);
    }

    private void processResult(ActivityResult result) {
        Intent data = result.getData();
        if (data != null) {
            Uri photoUri = data.getData();
            try {
                Bitmap profileImage = MediaStore.Images.Media.getBitmap(requireActivity()
                        .getContentResolver(), photoUri);
                Bitmap img = getResizedBitmap(profileImage, 150);
                ByteArrayOutputStream bs = new ByteArrayOutputStream();
                img.compress(Bitmap.CompressFormat.JPEG, 70, bs);
                byteArray = bs.toByteArray();
                Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                storeImageDisplay.setImageBitmap(bmp);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Cannot load this image", Toast.LENGTH_SHORT).show();
            }
        }
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
                    imageUrlNew = uri.toString();
                    updateOnFirestore();
                }));
    }
}