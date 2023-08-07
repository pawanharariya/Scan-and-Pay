package com.anvay.noqueuepay;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.util.Size;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.anvay.noqueuepay.models.ProductItem;
import com.anvay.noqueuepay.models.Store;
import com.anvay.noqueuepay.utils.Constants;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class ScanningActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CAMERA = 1;
    private PreviewView previewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private String scanResultString;
    private Boolean isImageCaptured = false, isCartActive = false;
    private Dialog dialog;
    private View loadingLayout;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanning);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle("Scan QR");
        actionBar.setDisplayHomeAsUpEnabled(true);
        previewView = findViewById(R.id.camera_preview_view);
        loadingLayout = findViewById(R.id.loading_layout);
        Intent intent = getIntent();
        sharedPreferences = getSharedPreferences(Constants.SHARED_PREF, MODE_PRIVATE);
        isCartActive = intent.getBooleanExtra(Constants.IS_CART_ACTIVE, false);
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        requestCamera();
    }

    private void requestCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(ScanningActivity.this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }

    private void startCamera() {
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "Error starting camera " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraPreview(@NonNull ProcessCameraProvider cameraProvider) {
        previewView.setPreferredImplementationMode(PreviewView.ImplementationMode.SURFACE_VIEW);

        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new ImageAnalyzer());
        preview.setSurfaceProvider(previewView.createSurfaceProvider());
        cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis, preview);
    }


    private class ImageAnalyzer implements ImageAnalysis.Analyzer {

        @Override
        public void analyze(ImageProxy imageProxy) {
            @SuppressLint("UnsafeOptInUsageError") Image mediaImage = imageProxy.getImage();
            if (mediaImage != null) {
                InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
                processImage(image, imageProxy);
            } else
                Toast.makeText(ScanningActivity.this, "Error Scanning QR code", Toast.LENGTH_SHORT).show();
        }
    }

    private void processImage(InputImage image, ImageProxy imageProxy) {
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build();
        BarcodeScanner scanner = BarcodeScanning.getClient(options);
        Task<List<Barcode>> result = scanner.process(image)
                .addOnSuccessListener(this::readBarCodeData)
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Cannot process the barcode. Try again.", Toast.LENGTH_SHORT).show();
                })
                .addOnCompleteListener(task -> {
                    imageProxy.close();
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private void readBarCodeData(List<Barcode> barcodes) {
        for (Barcode barcode : barcodes) {
            int valueType = barcode.getValueType();
            if (valueType == Barcode.TYPE_TEXT) {
                scanResultString = barcode.getDisplayValue();
                if (!isImageCaptured) {
                    if (isCartActive) {
                        ProductItem productItem = new ProductItem();
                        if (productItem.createProductFromString(scanResultString))
                            showScanResultDialog(productItem);
                        else
                            showInfoDialog();
                    } else {
                        processScanResult();
                    }
                }
            }
        }
    }

    private void processScanResult() {
        if(scanResultString == null){
            showInfoDialog();
            return;
        }
        isImageCaptured = true;
        loadingLayout.setVisibility(View.VISIBLE);
        String zipcode = sharedPreferences.getString(Constants.USER_ZIPCODE, "0000");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.STORES_BASE_URL)
                .document(scanResultString)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Store store = documentSnapshot.toObject(Store.class);
                        if (store != null)
                            showScanResultDialog(store);
                        else
                            showInfoDialog();
                    } else showInfoDialog();
                })
                .addOnFailureListener(e -> {
                    showInfoDialog();
                });
    }

    private void showScanResultDialog(Store store) {
        dialog = new Dialog(this);
        loadingLayout.setVisibility(View.INVISIBLE);
        dialog.setContentView(R.layout.dialog_confirmation);
        int width = WindowManager.LayoutParams.MATCH_PARENT;
        int height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setLayout(width, height);
        dialog.show();
        TextView infoText = dialog.findViewById(R.id.info_text);
        Button confirm = dialog.findViewById(R.id.confirm_button);
        Button cancel = dialog.findViewById(R.id.cancel_button);
        infoText.setText(store.getStoreName());
        confirm.setOnClickListener(view -> {
            saveDetails(store);
        });
        cancel.setOnClickListener(view -> {
            scanResultString = null;
            isImageCaptured = false;
            dialog.dismiss();
        });
    }

    private void saveDetails(Store store) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constants.IS_CART_ACTIVE, true);
        editor.putString(Constants.STORE_ID, store.getStoreId());
        editor.putString(Constants.STORE_NAME, store.getStoreName());
        editor.putString(Constants.STORE_UPI_ID, store.getStoreUpiId());
        editor.putString(Constants.STORE_IMAGE_URL, store.getImageUrl());
        editor.apply();
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    private void showScanResultDialog(ProductItem productItem) {
        isImageCaptured = true;
        loadingLayout.setVisibility(View.INVISIBLE);
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_confirmation);
        int width = WindowManager.LayoutParams.MATCH_PARENT;
        int height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setLayout(width, height);
        dialog.show();
        TextView infoText = dialog.findViewById(R.id.info_text);
        Button confirm = dialog.findViewById(R.id.confirm_button);
        Button cancel = dialog.findViewById(R.id.cancel_button);
        infoText.setText(productItem.getProductName());
        confirm.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.putExtra(Constants.KEY_PRODUCT_RAW_DATA, scanResultString);
            setResult(RESULT_OK, intent);
            finish();
        });
        cancel.setOnClickListener(view -> {
            scanResultString = null;
            isImageCaptured = false;
            dialog.dismiss();
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (dialog != null)
            dialog.dismiss();
    }

    private void showInfoDialog() {
        isImageCaptured = true;
        loadingLayout.setVisibility(View.INVISIBLE);
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_info);
        int width = WindowManager.LayoutParams.MATCH_PARENT;
        int height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setLayout(width, height);
        dialog.show();
        TextView infoText = dialog.findViewById(R.id.info_text);
        Button confirm = dialog.findViewById(R.id.confirm_button);
        infoText.setText(R.string.wrong_qr_info);
        confirm.setOnClickListener(view -> {
            isImageCaptured = false;
            dialog.dismiss();
        });
    }
}