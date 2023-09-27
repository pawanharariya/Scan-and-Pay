package com.psh.scanpay;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.psh.scanpay.utils.Constants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class PaymentActivity extends AppCompatActivity {
    private ActivityResultLauncher<Intent> upiAppLauncher;
    private Button proceedToPay;
    private TextView storeNameDisplay, orderIdDisplay, totalPriceDisplay, totalQuantityDisplay, dateDisplay;
    private String orderId, totalPrice;
    private View loadingLayout;
    private SharedPreferences sharedPreferences;

    public static boolean isConnectionAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnected()
                    && netInfo.isConnectedOrConnecting()
                    && netInfo.isAvailable();
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle("Payment");
        actionBar.setDisplayHomeAsUpEnabled(true);
        initUI();
        Random rnd = new Random();
        orderId = String.valueOf(100000 + rnd.nextInt(900000));
        sharedPreferences = getSharedPreferences(Constants.SHARED_PREF, MODE_PRIVATE);
        String userFirebaseId = sharedPreferences.getString(Constants.USER_FIREBASE_ID, "default");
        String storeId = sharedPreferences.getString(Constants.STORE_ID, "default");
        String storeUpiId = sharedPreferences.getString(Constants.STORE_UPI_ID, "default");
        String storeName = sharedPreferences.getString(Constants.STORE_NAME, "default");
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy hh.mm aa");
        String formattedDate = dateFormat.format(new Date());
        dateDisplay.setText(formattedDate);
        Intent i = getIntent();
        totalPrice = String.valueOf(i.getDoubleExtra(Constants.KEY_TOTAL_PRICE, 0.0));
        int totalQuantity = i.getIntExtra(Constants.KEY_TOTAL_QUANTITY, 0);
        storeNameDisplay.setText(storeName);
        orderIdDisplay.setText(orderId);
        totalQuantityDisplay.setText(String.valueOf(totalQuantity));
        totalPriceDisplay.setText("\u20B9 " + totalPrice);
        upiAppLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), this::processResult);
//        proceedToPay.setOnClickListener(view -> payUsingUpi(totalPrice, storeUpiId, storeName, "Order No: " + orderId));
         proceedToPay.setOnClickListener(view -> processSuccessfulPayment("TN1123"));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private void initUI() {
        proceedToPay = findViewById(R.id.proceed_to_pay);
        orderIdDisplay = findViewById(R.id.order_id_display);
        totalPriceDisplay = findViewById(R.id.total_cost_display);
        totalQuantityDisplay = findViewById(R.id.total_quantity_display);
        storeNameDisplay = findViewById(R.id.store_name_display);
        loadingLayout = findViewById(R.id.loading_layout);
        dateDisplay = findViewById(R.id.date_display);
    }

    private void payUsingUpi(String amount, String upiId, String name, String note) {
//        Uri uri = Uri.parse("upi://pay").buildUpon()
//                .appendQueryParameter("pa", upiId)
//                .appendQueryParameter("pn", name)
//                .appendQueryParameter("tn", note)
//                .appendQueryParameter("am", amount)
//                .appendQueryParameter("cu", "INR")
//
//                //
//                .build();
        Uri uri = new Uri.Builder()
                .scheme("upi")
                .authority("pay")
                .appendQueryParameter("pa", upiId)       // virtual ID
                .appendQueryParameter("pn", name)          // name
                .appendQueryParameter("mc", "code11")          // optional
                .appendQueryParameter("tr", orderId)     // optional
                .appendQueryParameter("tn", note)       // any note about payment
                .appendQueryParameter("am", amount)           // amount
                .appendQueryParameter("cu", "INR")                         // currency
                .appendQueryParameter("url", "www.google.com")       // optional
                .build();

        Intent upiPayIntent = new Intent(Intent.ACTION_VIEW);
        upiPayIntent.setData(uri);
        Intent chooser = Intent.createChooser(upiPayIntent, "Pay with");
        if (chooser.resolveActivity(getPackageManager()) != null)
            upiAppLauncher.launch(chooser);
        else
            Toast.makeText(PaymentActivity.this, "No UPI app found, please install one to continue", Toast.LENGTH_SHORT).show();
    }

    private void processResult(ActivityResult result) {
        loadingLayout.setVisibility(View.VISIBLE);
        int resultCode = result.getResultCode();
        Intent data = result.getData();
        Log.e("UPI", "RESULT" + result.toString());
        if ((RESULT_OK == resultCode) || (resultCode == 11)) {
            if (data != null) {
                String text = data.getStringExtra("response");
                Log.e("UPI", "onActivityResult: " + text);
                ArrayList<String> dataList = new ArrayList<>();
                dataList.add(text);
                upiPaymentDataOperation(dataList);
            } else {
                Log.e("UPI", "onActivityResult: " + "Return data is null");
                ArrayList<String> dataList = new ArrayList<>();
                dataList.add("nothing");
                loadingLayout.setVisibility(View.INVISIBLE);
                upiPaymentDataOperation(dataList);
            }
        } else {
            Log.e("UPI", "onActivityResult: " + "Return data is null");
            ArrayList<String> dataList = new ArrayList<>();
            dataList.add("nothing");
            loadingLayout.setVisibility(View.INVISIBLE);
            upiPaymentDataOperation(dataList);
        }
    }

    private void upiPaymentDataOperation(ArrayList<String> data) {
        if (isConnectionAvailable(PaymentActivity.this)) {
            String str = data.get(0);
            Log.e("UPIPAY", "upiPaymentDataOperation: " + str);
            String paymentCancel = "";
            if (str == null) str = "discard";
            String status = "";
            String approvalRefNo = "";
            String[] response = str.split("&");
            for (String s : response) {
                String[] equalStr = s.split("=");
                if (equalStr.length >= 2) {
                    if (equalStr[0].equalsIgnoreCase("Status")) {
                        status = equalStr[1].toLowerCase();
                    } else if (equalStr[0].equalsIgnoreCase("ApprovalRefNo") || equalStr[0].toLowerCase().equals("txnRef".toLowerCase())) {
                        approvalRefNo = equalStr[1];
                    }
                } else {
                    paymentCancel = "Payment cancelled by user.";
                    loadingLayout.setVisibility(View.INVISIBLE);
                }
            }

            if (status.equals("success")) {
                processSuccessfulPayment(approvalRefNo);
            } else if ("Payment cancelled by user.".equals(paymentCancel)) {
                loadingLayout.setVisibility(View.INVISIBLE);
                Toast.makeText(PaymentActivity.this, "Payment cancelled by user.", Toast.LENGTH_SHORT).show();
            } else {
                loadingLayout.setVisibility(View.INVISIBLE);
                Toast.makeText(PaymentActivity.this, "Transaction failed.Please try again", Toast.LENGTH_SHORT).show();
            }
        } else {
            loadingLayout.setVisibility(View.INVISIBLE);
            Toast.makeText(PaymentActivity.this, "Internet connection is not available. Please check and try again", Toast.LENGTH_SHORT).show();
        }
    }

    private void processSuccessfulPayment(String approvalRefNo) {
        Toast.makeText(PaymentActivity.this, "Transaction successful.", Toast.LENGTH_SHORT).show();
        Log.e("UPI", "responseStr: " + approvalRefNo);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.TRANSACTION_NUMBER, approvalRefNo);
        editor.putString(Constants.ORDER_ID, orderId);
        editor.putString(Constants.PAID_AMOUNT, totalPrice);
        editor.putBoolean(Constants.IS_PAYMENT_DONE, true);
        editor.apply();
        Intent i = new Intent();
        i.putExtra(Constants.TRANSACTION_NUMBER, approvalRefNo);
        i.putExtra(Constants.ORDER_ID, orderId);
        i.putExtra(Constants.IS_PAYMENT_DONE, true);
        setResult(RESULT_OK, i);
        finish();
    }
}