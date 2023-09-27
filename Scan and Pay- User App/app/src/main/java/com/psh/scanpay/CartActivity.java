	package com.psh.scanpay;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.psh.scanpay.adapters.CartItemsAdapter;
import com.psh.scanpay.models.Order;
import com.psh.scanpay.models.ProductItem;
import com.psh.scanpay.utils.AppDatabase;
import com.psh.scanpay.utils.Constants;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.WriterException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class CartActivity extends AppCompatActivity implements CartItemsAdapter.CartItemClickListener {
    final private int REQUEST_CODE_ASK_PERMISSIONS = 111;
    private RecyclerView cartItemsRecycler;
    private TextView totalCostDisplay, totalQuantityDisplay;
    private Button purchaseButton, deleteEmptyCart, downloadInvoice;
    private FloatingActionButton scanProduct;
    private AppDatabase database;
    private List<ProductItem> cartItemsList = new ArrayList<>();
    private CartItemsAdapter adapter;
    private double totalPrice;
    private int totalQuantity;
    private View emptyCartLayout, successQrLayout, loadingLayout, cartSummaryView;
    private ImageView successQrDisplay;
    private TextView storeNameLabel, orderIdLabel, infoLabel, referenceNumberLabel, paidAmountLabel;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ActivityResultLauncher<Intent> paymentActivityLauncher;
    private String userFirebaseId, storeId, storeName, userName, orderDate;
    private Order currentOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        initUI();
        loadingLayout.setVisibility(View.VISIBLE);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle("Your Cart");
        actionBar.setDisplayHomeAsUpEnabled(true);
        sharedPreferences = getSharedPreferences(Constants.SHARED_PREF, MODE_PRIVATE);
        userFirebaseId = sharedPreferences.getString(Constants.USER_FIREBASE_ID, "default");
        storeId = sharedPreferences.getString(Constants.STORE_ID, "default");
        storeName = sharedPreferences.getString(Constants.STORE_NAME, "default");
        userName = sharedPreferences.getString(Constants.USER_NAME, "");
        editor = sharedPreferences.edit();
        ActivityResultLauncher<Intent> scanningActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent intent = result.getData();
                        assert intent != null;
                        if (intent.hasExtra(Constants.KEY_PRODUCT_RAW_DATA))
                            processScanResult(intent.getStringExtra(Constants.KEY_PRODUCT_RAW_DATA));
                        else
                            Toast.makeText(this, "Cannot process this QR Code", Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(this, "Cannot process this QR Code", Toast.LENGTH_SHORT).show();
                });
        paymentActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent intent = result.getData();
                        assert intent != null;
                        if (intent.getBooleanExtra(Constants.IS_PAYMENT_DONE, false)) {
                            String transactionNumber = intent.getStringExtra(Constants.TRANSACTION_NUMBER);
                            String orderId = intent.getStringExtra(Constants.ORDER_ID);
                            processSuccessfulPayment(orderId, transactionNumber);
                        }
                    }
                });
        database = AppDatabase.getInstance(this);
        cartItemsList = database.appDao().getCartItems();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        adapter = new CartItemsAdapter(cartItemsList, this);
        cartItemsRecycler.setLayoutManager(linearLayoutManager);
        cartItemsRecycler.setAdapter(adapter);
        checkEmptyRecycler();
        calculateSummary();
        updateSummary();
        scanProduct.setOnClickListener(view -> {
            Intent i = new Intent(this, ScanningActivity.class);
            i.putExtra(Constants.IS_CART_ACTIVE, true);
            scanningActivityLauncher.launch(i);
        });
        deleteEmptyCart.setOnClickListener(view -> {
            database.appDao().deleteAllCartItems();
            editor.putBoolean(Constants.IS_CART_ACTIVE, false);
            editor.apply();
            finish();
        });
        purchaseButton.setOnClickListener(view -> {
            initiatePayment();
        });
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                checkEmptyRecycler();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                checkEmptyRecycler();
            }

            @Override
            public void onChanged() {
                super.onChanged();
                checkEmptyRecycler();
            }
        });
        loadingLayout.setVisibility(View.INVISIBLE);
        downloadInvoice.setOnClickListener(view -> generateInvoice(currentOrder));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private void processSuccessfulPayment(String orderId, String transactionNumber) {
        loadingLayout.setVisibility(View.VISIBLE);
        Order order = new Order(orderId, storeId, storeName, userFirebaseId, totalQuantity, totalPrice,
                cartItemsList, transactionNumber, userName);
        currentOrder = order;
        database.appDao().deleteAllCartItems();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.BASE_ORDER_URL)
                .add(order)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                        generateQRCode(orderId, transactionNumber, storeName, String.valueOf(totalPrice),
                                task.getResult().getId());
                });

        database.appDao().insertOrder(order);
    }

    private void generateInvoice(Order order) {
        try {
            createPdfWrapper(order);
        } catch (FileNotFoundException | DocumentException e) {
            e.printStackTrace();
        }
    }

    private void resetAll() {
        editor.putBoolean(Constants.IS_CART_ACTIVE, false);
        editor.remove(Constants.IS_PAYMENT_DONE);
        editor.remove(Constants.STORE_NAME);
        editor.remove(Constants.STORE_UPI_ID);
        editor.remove(Constants.STORE_ID);
        editor.remove(Constants.STORE_IMAGE_URL);
        editor.remove(Constants.ORDER_ID);
        editor.remove(Constants.PAID_AMOUNT);
        editor.remove(Constants.TRANSACTION_NUMBER);
        editor.apply();
    }

    @SuppressLint("SetTextI18n")
    private void generateQRCode(String orderId, String transactionNumber, String storeName, String paidAmount, String docId) {
        storeNameLabel.setText(storeName);
        orderIdLabel.setText("Order Id: " + orderId);
        referenceNumberLabel.setText("Reference Number: " + transactionNumber);
        infoLabel.setText("Your bill payment of amount \u20B9" + paidAmount + " with " + storeName + " has been successfully processed." +
                " In case of any queries, please contact " + storeName + " with reference number given below.");
        successQrLayout.setVisibility(View.VISIBLE);
        paidAmountLabel.setText("\u20B9 " + paidAmount);
        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int width = point.x;
        int height = point.y;
        int dimen = Math.min(width, height);
        dimen = dimen * 3 / 4;
        QRGEncoder qrgEncoder = new QRGEncoder(docId, null, QRGContents.Type.TEXT, dimen);
        try {
            Bitmap bitmap = qrgEncoder.encodeAsBitmap();
            successQrDisplay.setImageBitmap(bitmap);
        } catch (WriterException e) {
            Toast.makeText(this, "Error generating QR Code", Toast.LENGTH_SHORT).show();
        }
        resetAll();
        loadingLayout.setVisibility(View.INVISIBLE);
    }

    private void initiatePayment() {
        Intent i = new Intent(this, PaymentActivity.class);
        i.putExtra(Constants.KEY_TOTAL_PRICE, totalPrice);
        i.putExtra(Constants.KEY_TOTAL_QUANTITY, totalQuantity);
        paymentActivityLauncher.launch(i);
    }

    private void updateSummary() {
        totalCostDisplay.setText(String.valueOf(totalPrice));
        totalQuantityDisplay.setText(String.valueOf(totalQuantity));
    }

    private void calculateSummary() {
        for (ProductItem item : cartItemsList) {
            int quantity = item.getProductQuantity();
            totalQuantity += quantity;
            totalPrice += item.getProductPrice() * quantity;
        }
    }

    private void checkEmptyRecycler() {
        int count = adapter.getItemCount();
        cartItemsRecycler.setVisibility(count == 0 ? View.GONE : View.VISIBLE);
        emptyCartLayout.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
        cartSummaryView.setVisibility(count == 0 ? View.INVISIBLE : View.VISIBLE);
    }

    private void processScanResult(String productRawData) {
        loadingLayout.setVisibility(View.VISIBLE);
        ProductItem item = new ProductItem();
        item.createProductFromString(productRawData);
        database.appDao().insertProduct(item);
        totalPrice += item.getProductPrice();
        totalQuantity++;
        updateSummary();
        cartItemsList.add(item);
        adapter.notifyDataSetChanged();
        loadingLayout.setVisibility(View.INVISIBLE);
    }

    private void initUI() {
        cartItemsRecycler = findViewById(R.id.cart_items_recycler);
        purchaseButton = findViewById(R.id.purchase_button);
        totalCostDisplay = findViewById(R.id.total_cost_display);
        scanProduct = findViewById(R.id.scan_product);
        totalQuantityDisplay = findViewById(R.id.total_quantity_display);
        emptyCartLayout = findViewById(R.id.empty_cart_layout);
        deleteEmptyCart = findViewById(R.id.delete_empty_cart);
        loadingLayout = findViewById(R.id.loading_layout);
        successQrLayout = findViewById(R.id.success_qr_layout);
        successQrDisplay = findViewById(R.id.success_qr_display);
        cartSummaryView = findViewById(R.id.cart_summary_view);
        infoLabel = findViewById(R.id.info_label);
        storeNameLabel = findViewById(R.id.store_name_label);
        referenceNumberLabel = findViewById(R.id.reference_number_label);
        orderIdLabel = findViewById(R.id.order_id_label);
        paidAmountLabel = findViewById(R.id.paid_amount_label);
        downloadInvoice = findViewById(R.id.viewInvoice);
    }

    @Override
    public void onIncrementQuantityClickListener(int id, int position) {
        database.appDao().incrementQuantity(id);
        adapter.notifyItemChanged(position);
        ProductItem productItem = cartItemsList.get(position);
        totalPrice += productItem.getProductPrice();
        totalQuantity++;
        updateSummary();
    }

    @Override
    public void onDecrementQuantityClickListener(int id, int position) {
        database.appDao().decrementQuantity(id);
        adapter.notifyItemChanged(position);
        ProductItem productItem = cartItemsList.get(position);
        totalPrice -= productItem.getProductPrice();
        totalQuantity--;
        updateSummary();
    }

    @Override
    public void onDeleteItemClickListener(int id, int position) {
        Dialog dialog = new Dialog(this);
        ProductItem productItem = cartItemsList.get(position);
        dialog.setContentView(R.layout.dialog_confirmation);
        int width = WindowManager.LayoutParams.MATCH_PARENT;
        int height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setLayout(width, height);
        dialog.show();
        TextView infoText = dialog.findViewById(R.id.info_text);
        Button confirm = dialog.findViewById(R.id.confirm_button);
        Button cancel = dialog.findViewById(R.id.cancel_button);
        infoText.setText(R.string.delete_item_from_cart);
        confirm.setOnClickListener(view -> {
            loadingLayout.setVisibility(View.VISIBLE);
            database.appDao().deleteProduct(id);
            totalPrice -= productItem.getProductQuantity() * productItem.getProductPrice();
            totalQuantity -= productItem.getProductQuantity();
            updateSummary();
            cartItemsList.remove(position);
            adapter.notifyItemRemoved(position);
            adapter.notifyItemChanged(position, cartItemsList.size());
            dialog.dismiss();
            loadingLayout.setVisibility(View.INVISIBLE);
            Toast.makeText(this, "Item Removed", Toast.LENGTH_SHORT).show();
        });
        cancel.setOnClickListener(view -> dialog.dismiss());
    }

    private void createPdfWrapper(Order order) throws FileNotFoundException, DocumentException {
        int hasWriteStoragePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED) {
            storagePermission();
        } else {
            createPdf(order);
        }
    }

    private void showMessageOKCancel(DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage("You need to allow access to Storage to generate Invoice")
                .setPositiveButton("OK", okListener)
                .create()
                .show();
    }

    private void createPdf(Order order) throws FileNotFoundException, DocumentException {
        loadingLayout.setVisibility(View.VISIBLE);
        File docsFolder = new File(Environment.getExternalStorageDirectory() + "/Documents");
        if (!docsFolder.exists()) {
            docsFolder.mkdir();
            Log.i("cartActivity", "Created a new directory for PDF");
        }
        String pdfName = order.getOrderId() + ".pdf";
        List<ProductItem> items = order.getItemsList();
        File pdfFile = new File(docsFolder.getAbsolutePath(), pdfName);
        OutputStream output = new FileOutputStream(pdfFile);
        Document document = new Document(PageSize.A4);
        PdfPTable table = new PdfPTable(new float[]{3, 5, 3, 3});
        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        table.getDefaultCell().setFixedHeight(28);
        table.setTotalWidth(PageSize.A4.getWidth());
        table.setWidthPercentage(100);
        table.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell("Product Id");
        table.addCell("Name");
        table.addCell("Quantity");
        table.addCell("Price");
        table.setHeaderRows(1);
        PdfPCell[] cells = table.getRow(0).getCells();
        for (PdfPCell cell : cells) {
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        }
        for (ProductItem item : items) {
            table.addCell(String.valueOf(item.getProductId()));
            table.addCell(String.valueOf(item.getProductName()));
            int quantity = item.getProductQuantity();
            table.addCell(String.valueOf(quantity));
            table.addCell(String.valueOf(item.getProductPrice() * quantity));
        }
        table.addCell("");
        table.addCell("Total");
        table.addCell(String.valueOf(order.getTotalQuantity()));
        table.addCell(String.valueOf(order.getTotalPrice()));
        PdfWriter.getInstance(document, output);
        document.open();
        Font catFont = new Font(Font.FontFamily.TIMES_ROMAN, 18, Font.BOLD);
        Font normalFont = new Font(Font.FontFamily.TIMES_ROMAN, 18, Font.NORMAL);
        Font titleFont = new Font(Font.FontFamily.TIMES_ROMAN, 28, Font.BOLD, BaseColor.BLUE);
        Paragraph title = new Paragraph();
        title.setFont(titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.add("INVOICE\n\n\n\n");
        document.add(title);
        Paragraph storeName = new Paragraph();
        storeName.setFont(catFont);
        storeName.setAlignment(Element.ALIGN_CENTER);
        storeName.add(order.getStoreName());
        document.add(storeName);
        document.add(new Paragraph("Customer Name: " +
                sharedPreferences.getString(Constants.USER_NAME, ""), normalFont));
        document.add(new Paragraph("Date: " + order.getDate(), normalFont));
        document.add(new Paragraph("Order Id: " + order.getOrderId(), normalFont));
        document.add(new Paragraph("Reference No: " + order.getTransactionNumber() + "\n\n\n", normalFont));
        document.add(table);
        document.add(new Paragraph("\n\nNOTE: In case of any queries, please contact " + order.getStoreName() + " with reference number provided."));
        document.close();
        loadingLayout.setVisibility(View.INVISIBLE);
        previewPdf(pdfFile);
    }

    private void previewPdf(File file) {
        Intent testIntent = new Intent(Intent.ACTION_VIEW);
        testIntent.setType("application/pdf");
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Uri uri = FileProvider
                .getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", file);
        intent.setDataAndType(uri, "application/pdf");
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Download a PDF Viewer to see the generated PDF", Toast.LENGTH_SHORT).show();
        }
    }

    private void storagePermission() {
        int hasWriteStoragePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CONTACTS)) {
                    showMessageOKCancel(
                            (dialog, which) -> requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    REQUEST_CODE_ASK_PERMISSIONS));
                    return;
                }
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_ASK_PERMISSIONS);
            }
        }
    }
}