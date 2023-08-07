package com.anvay.noqueuepaydashboard.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anvay.noqueuepaydashboard.ScanningActivity;
import com.anvay.noqueuepaydashboard.adapters.PendingOrdersAdapter;
import com.anvay.noqueuepaydashboard.databinding.FragmentHomeBinding;
import com.anvay.noqueuepaydashboard.models.Order;
import com.anvay.noqueuepaydashboard.models.ProductItem;
import com.anvay.noqueuepaydashboard.utils.Constants;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
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

public class HomeFragment extends Fragment implements PendingOrdersAdapter.OrderClickListener {
    private FragmentHomeBinding binding;
    private PendingOrdersAdapter adapter;
    private TextView emptyRecyclerInfo;
    private final List<Order> pendingOrders = new ArrayList<>();
    private ActivityResultLauncher<Intent> scanLauncher;
    private boolean isLoggedIn;
    private Order currentOrder;
    private String storeId, storeName;
    private View loadingLayout;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 111;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        RecyclerView recyclerView = binding.pendingOrdersRecycler;
        com.anvay.noqueuepaydashboard.databinding.LayoutLoadingBinding loadingLayoutBinding = binding.loadingLayout;
        emptyRecyclerInfo = binding.emptyRecyclerInfo;
        CardView options = binding.options;
        Button allOrdersButton = binding.allOrdersButton;
        Button salesButton = binding.salesButton;
        loadingLayout = loadingLayoutBinding.getRoot();
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE);
        storeId = sharedPreferences.getString(Constants.STORE_ID, "default");
        storeName = sharedPreferences.getString(Constants.STORE_NAME, "default");
        isLoggedIn = sharedPreferences.getBoolean(Constants.IS_LOGGED_IN, true);
        boolean isScanOnlyMode = sharedPreferences.getBoolean(Constants.IS_SCAN_ONLY_MODE, false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        adapter = new PendingOrdersAdapter(pendingOrders, this);
        snapshotListener();
        checkEmptyRecycler();
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
        scanLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        processResult(result.getData());
                    } else
                        Toast.makeText(getContext(), "Failed to process QR code", Toast.LENGTH_SHORT).show();
                }
        );
        if (isScanOnlyMode) options.setVisibility(View.INVISIBLE);
        else options.setVisibility(View.VISIBLE);
        allOrdersButton.setOnClickListener(view -> openFragment(new DashboardFragment(), "Dashboard"));
        salesButton.setOnClickListener(view -> openFragment(new SalesFragment(), "Your Sales"));
        return root;
    }

    private void openFragment(Fragment fragment, String tag) {
        FragmentManager fragmentManager = getChildFragmentManager();
        fragmentManager.beginTransaction()
                .add(binding.fragmentHost.getId(), fragment)
                .addToBackStack(tag)
                .commit();
    }

    private void processResult(Intent intent) {
        loadingLayout.setVisibility(View.VISIBLE);
        if (intent != null) {
            String data = intent.getStringExtra(Constants.KEY_ORDER_DOC_ID);
            if (verifyData(data)) {
                updateOrderStatus();
            } else {
                loadingLayout.setVisibility(View.INVISIBLE);
                Toast.makeText(getContext(), "QR could not be verified", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateOrderStatus() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection(Constants.BASE_ORDER_URL)
                .document(currentOrder.getDocId());
        docRef.update("verified", true)
                .addOnSuccessListener(unused -> {
                    pendingOrders.remove(currentOrder);
                    loadingLayout.setVisibility(View.INVISIBLE);
                    Toast.makeText(getContext(), "Verified", Toast.LENGTH_SHORT).show();
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    loadingLayout.setVisibility(View.INVISIBLE);
                    Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                });
    }

    private boolean verifyData(String docId) {
        return currentOrder.getDocId().equals(docId);
    }

    private void checkEmptyRecycler() {
        if (pendingOrders.isEmpty())
            emptyRecyclerInfo.setVisibility(View.VISIBLE);
        else emptyRecyclerInfo.setVisibility(View.INVISIBLE);
    }

    private void snapshotListener() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.BASE_ORDER_URL)
                .whereEqualTo(Constants.KEY_STORE_ID, storeId)
                .whereEqualTo(Constants.KEY_IS_VERIFIED, false)
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        pendingOrders.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Order order = doc.toObject(Order.class);
                            order.setDocId(doc.getId());
                            pendingOrders.add(order);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    checkEmptyRecycler();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onScanQrClicked(int position) {
        currentOrder = pendingOrders.get(position);
        Intent intent = new Intent(getContext(), ScanningActivity.class);
        intent.putExtra(Constants.IS_LOGGED_IN, isLoggedIn);
        scanLauncher.launch(intent);
    }

    @Override
    public void onDownloadInvoiceClicked(int position) {
        Order order = pendingOrders.get(position);
        Log.e("homeFragment", "orderId = " + order.getOrderId());
        Log.e("homeFragment", "itemsList = " + order.getItemsList().toString());
        generateInvoice(order);
    }

    public void generateInvoice(Order order) {
        try {
            createPdfWrapper(order);
        } catch (FileNotFoundException | DocumentException e) {
            e.printStackTrace();
        }
    }

    private void createPdfWrapper(Order order) throws FileNotFoundException, DocumentException {
        int hasWriteStoragePermission = ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED) {
            storagePermission();
        } else {
            createPdf(order);
        }
    }

    private void showMessageOKCancel(DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(getContext())
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
        String pdfName = order.getOrderId() + "ST.pdf";
        List<ProductItem> items = order.getItemsList();
        Log.e("homeFragment", "itemsList = " + items.toString() + "size" + items.size());
        File pdfFile = new File(docsFolder.getAbsolutePath(), pdfName);
        OutputStream output = new FileOutputStream(pdfFile);
        Document document = new Document(PageSize.A4);
        Log.e("homeFragment", "new document");
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
            Log.e("homeFragment", "inside for each item = " + item.getProductName());
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
        Paragraph storeDetails = new Paragraph();
        storeDetails.setFont(catFont);
        storeDetails.setAlignment(Element.ALIGN_CENTER);
        storeDetails.add(storeName);
        document.add(storeDetails);
        document.add(new Paragraph("Customer Name: " + order.getCustomerName(), normalFont));
        document.add(new Paragraph("Date: " + order.getDate(), normalFont));
        document.add(new Paragraph("Order Id: " + order.getOrderId(), normalFont));
        document.add(new Paragraph("Reference No: " + order.getTransactionNumber() + "\n\n\n", normalFont));
        document.add(table);
        document.close();
        loadingLayout.setVisibility(View.INVISIBLE);
        previewPdf(pdfFile);
    }

    private void previewPdf(File file) {
        Log.e("homeFragment", "Inside preview" + file.getName());
        Intent testIntent = new Intent(Intent.ACTION_VIEW);
        testIntent.setType("application/pdf");
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Uri uri = FileProvider
                .getUriForFile(requireContext(), requireContext().getPackageName() + ".provider", file);
        intent.setDataAndType(uri, "application/pdf");
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), "Download a PDF Viewer to see the generated PDF", Toast.LENGTH_SHORT).show();
        }
    }

    private void storagePermission() {
        int hasWriteStoragePermission = ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
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