package com.anvay.noqueuepaydashboard.fragments;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.anvay.noqueuepaydashboard.MainActivity;
import com.anvay.noqueuepaydashboard.R;
import com.anvay.noqueuepaydashboard.models.Order;
import com.anvay.noqueuepaydashboard.utils.Constants;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class SalesFragment extends Fragment implements DatePickerDialog.OnDateSetListener {
    private TextView totalSalesDisplay, totalOrdersDisplay, averageSalesDisplay, totalQuantityDisplay,
            dateDisplayLabel;
    private Button dateDisplay;
    private String storeId;
    private View loadingLayout;
    private int offset = 0;
    private Date startDate;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
      //  ((MainActivity) requireActivity()).setActionBarTitle("Sales");
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_sales, container, false);
        Spinner categorySpinner = root.findViewById(R.id.sales_category_spinner);
        loadingLayout = root.findViewById(R.id.loading_layout);
        totalSalesDisplay = root.findViewById(R.id.total_sales);
        dateDisplay = root.findViewById(R.id.date_display);
        dateDisplayLabel = root.findViewById(R.id.date_display_label);
        totalOrdersDisplay = root.findViewById(R.id.total_orders);
        averageSalesDisplay = root.findViewById(R.id.average_sales);
        totalQuantityDisplay = root.findViewById(R.id.total_quantity);
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE);
        storeId = sharedPreferences.getString(Constants.STORE_ID, "default");
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i < 3) {
                    fetchData(getStartTimestamp(i), getEndTimestamp(startDate));
                    dateDisplay.setVisibility(View.INVISIBLE);
                    dateDisplayLabel.setVisibility(View.INVISIBLE);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    dateDisplay.setVisibility(View.VISIBLE);
                    dateDisplayLabel.setVisibility(View.VISIBLE);
                } else
                    Toast.makeText(getContext(), "This option is not available on your device", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                fetchData(getStartTimestamp(0), getEndTimestamp(startDate));
            }
        });
        dateDisplay.setOnClickListener(view -> getCustomDate());
        return root;
    }

    private Timestamp getStartTimestamp(int selectedIndex) {
        switch (selectedIndex) {
            case 0:
                offset = 0;
                break;
            case 1:
                offset = -7;
                break;
            case 2:
                offset = -30;
                break;
        }
        Calendar cal = Calendar.getInstance();
        startDate = cal.getTime();
        cal.add(Calendar.DATE, offset);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return new Timestamp(cal.getTime());
    }

    private void getCustomDate() {
        com.anvay.noqueuepaydashboard.utils.DatePicker datePicker = new com.anvay.noqueuepaydashboard.utils.DatePicker();
        datePicker.show(getChildFragmentManager(), "DATE PICK");
    }

    private Timestamp getEndTimestamp(Date startDate) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(startDate);
        cal.add(Calendar.DATE, +1);
        return new Timestamp(cal.getTime());
    }

    private void fetchData(Timestamp startTimestamp, Timestamp endTimestamp) {
        loadingLayout.setVisibility(View.VISIBLE);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Log.e("sales", "today's " + startTimestamp.toDate());
        db.collection(Constants.BASE_ORDER_URL)
                .whereEqualTo(Constants.KEY_STORE_ID, storeId)
                .whereEqualTo(Constants.KEY_IS_VERIFIED, true)
                .whereGreaterThan("timestamp", startTimestamp)
                .whereLessThan("timestamp", endTimestamp)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double totalSales = 0;
                    int totalOrders = queryDocumentSnapshots.size(), totalQuantity = 0;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Order order = doc.toObject(Order.class);
                        totalSales += order.getTotalPrice();
                        totalQuantity += order.getTotalQuantity();
                        Log.e("sales", order.getTimestamp().toDate() + "");
                    }
                    totalSalesDisplay.setText(String.valueOf(totalSales));
                    totalOrdersDisplay.setText(String.valueOf(totalOrders));
                    totalQuantityDisplay.setText(String.valueOf(totalQuantity));
                    double avg = totalSales / (offset == 0 ? 1 : Math.abs(offset));
                    DecimalFormat df = new DecimalFormat("#.##");
                    averageSalesDisplay.setText(df.format(avg));
                    loadingLayout.setVisibility(View.INVISIBLE);
                })
                .addOnFailureListener(e -> {
                    loadingLayout.setVisibility(View.INVISIBLE);
                    Log.e("sales", e.getMessage());
                    Toast.makeText(getContext(), "Failed to load data", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        startDate = cal.getTime();
        DateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy");
        dateDisplay.setText(formatter.format(startDate));
        fetchData(new Timestamp(startDate), getEndTimestamp(startDate));
    }
}