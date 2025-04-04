package edu.northeastern.mindyourmoneyapp;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import edu.northeastern.mindyourmoneyapp.databinding.ActivityStatsBinding;

public class StatsActivity extends AppCompatActivity {

    ActivityStatsBinding binding;
    Calendar calendar;
    int selectedTab = 0;
    int selectedType = 0; // 0 for category, 1 for payment mode
    String currentDisplayDate;
    private DatabaseReference mindYourMoneyRef;
    private WebView chartWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStatsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mindYourMoneyRef = FirebaseDatabase.getInstance().getReference("transactionHistory");
        calendar = Calendar.getInstance();

        // Initialize WebView
        chartWebView = binding.chartWebView;
        chartWebView.getSettings().setJavaScriptEnabled(true);

        // Initialize UI state
        selectedType = 0;
        selectedTab = 0;
        updateButtonState();

        binding.navView.setSelectedItemId(R.id.stats);
        currentDisplayDate = updateDate();

        setupNavigationButtons();
        setupTypeSelection();
        setupTabLayout();
    }

    private void setupNavigationButtons() {
        binding.nextButton.setOnClickListener(view -> {
            adjustDate(1);
            currentDisplayDate = updateDate();
        });

        binding.prevButton.setOnClickListener(view -> {
            adjustDate(-1);
            currentDisplayDate = updateDate();
        });
    }

    private void adjustDate(int amount) {
        switch (selectedTab) {
            case 0:
                calendar.add(Calendar.DATE, amount);
                break;
            case 1:
                calendar.add(Calendar.MONTH, amount);
                break;
            case 2:
                calendar.add(Calendar.YEAR, amount);
                break;
        }
    }

    private void setupTypeSelection() {
        binding.category.setOnClickListener(view -> {
            if (selectedType != 0) {
                selectedType = 0;
                updateButtonState();
                fetchTransactionsForDate(currentDisplayDate);
            }
        });

        binding.mode.setOnClickListener(view -> {
            if (selectedType != 1) {
                selectedType = 1;
                updateButtonState();
                fetchTransactionsForDate(currentDisplayDate);
            }
        });

        binding.navView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.addexpense) {
                new AddTransactionFragment().show(getSupportFragmentManager(), null);
            } else if (item.getItemId() == R.id.transaction){
                finish();
            }
            return true;
        });
    }

    private void setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedTab = tab.getPosition();
                currentDisplayDate = updateDate();
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        fetchTransactionsForDate(currentDisplayDate);
    }

    private void updateButtonState() {
        binding.category.setBackgroundColor(getResources().getColor(
                selectedType == 0 ? R.color.blue : android.R.color.transparent));
        binding.category.setTextColor(getResources().getColor(
                selectedType == 0 ? R.color.white : R.color.blue));

        binding.mode.setBackgroundColor(getResources().getColor(
                selectedType == 1 ? R.color.blue : android.R.color.transparent));
        binding.mode.setTextColor(getResources().getColor(
                selectedType == 1 ? R.color.white : R.color.blue));
    }

    private String updateDate() {
        SimpleDateFormat format = new SimpleDateFormat(getDateFormatPattern(), Locale.getDefault());
        String formattedDate = format.format(calendar.getTime());
        binding.currentDate.setText(formattedDate);
        fetchTransactionsForDate(formattedDate);
        return formattedDate;
    }

    private String getDateFormatPattern() {
        switch (selectedTab) {
            case 2: return "yyyy";
            case 1: return "MMMM, yyyy";
            default: return "MMMM dd, yyyy";
        }
    }

    private void fetchTransactionsForDate(String selectedDate) {
        String path = getFirebasePath();
        mindYourMoneyRef.orderByChild(path).equalTo(selectedDate)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Transaction> transactions = new ArrayList<>();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Transaction t = ds.getValue(Transaction.class);
                            if (t != null) transactions.add(t);
                        }
                        updateChartVisibility(transactions);
                        if (!transactions.isEmpty()) updatePieChart(transactions);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("StatsActivity", "Database error: ", error.toException());
                    }
                });
    }

    private String getFirebasePath() {
        switch (selectedTab) {
            case 0: return "date";
            case 1: return "monthYear";
            default: return "year";
        }
    }

    private void updateChartVisibility(List<Transaction> transactions) {
        if (transactions.isEmpty()) {
            binding.emptyState.setVisibility(View.VISIBLE);
            chartWebView.setVisibility(View.GONE);
        } else {
            binding.emptyState.setVisibility(View.GONE);
            chartWebView.setVisibility(View.VISIBLE);
        }
    }

    private void updatePieChart(List<Transaction> transactions) {
        if (selectedType == 0) {
            generateCategoryChart(transactions);
        } else {
            generatePaymentModeChart(transactions);
        }
    }

    private void generateCategoryChart(List<Transaction> transactions) {
        Map<String, Float> data = new HashMap<>();
        for (Transaction t : transactions) {
            String category = t.getCategory();
            float amount = (float) t.getAmount();
            data.put(category, data.getOrDefault(category, 0f) + amount);
        }
        renderChart(data, "Expenses by Category");
    }

    private void generatePaymentModeChart(List<Transaction> transactions) {
        Map<String, Float> data = new HashMap<>();
        for (Transaction t : transactions) {
            String mode = t.getAccount();
            float amount = (float) t.getAmount();
            data.put(mode, data.getOrDefault(mode, 0f) + amount);
        }
        renderChart(data, "Expenses by Payment Mode");
    }

    private void renderChart(Map<String, Float> data, String title) {
        StringBuilder dataRows = new StringBuilder();
        for (Map.Entry<String, Float> entry : data.entrySet()) {
            dataRows.append(String.format(Locale.US, "['%s', %.2f],",
                    entry.getKey(), entry.getValue()));
        }

        String html = "<!DOCTYPE html><html><head>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                + "<script type='text/javascript' src='https://www.gstatic.com/charts/loader.js'></script>"
                + "<script type='text/javascript'>"
                + "google.charts.load('current', {packages:['corechart']});"
                + "google.charts.setOnLoadCallback(drawChart);"
                + "function drawChart() {"
                + "var data = new google.visualization.DataTable();"
                + "data.addColumn('string', 'Item');"
                + "data.addColumn('number', 'Amount');"
                + "data.addRows([" + dataRows.toString() + "]);"

                + "var options = {"
                + "pieHole: 0.5,"
                + "backgroundColor: 'transparent',"
                + "fontName: 'Segoe UI',"
                + "fontSize: 16,"
                + "tooltip: { text: 'percentage' },"
                + "legend: { position: 'top', alignment: 'center', textStyle: { fontSize: 16, color: '#333' } },"
                + "chartArea: { width: '90%', height: '75%' },"
                + "pieSliceText: 'percentage',"
                + "pieSliceTextStyle: { fontSize: 15, color: 'white' },"
                + "colors: ['#4e79a7', '#f28e2b', '#e15759', '#76b7b2', '#59a14f', '#edc948', '#b07aa1', '#ff9da7']"
                + "};"

                + "var chart = new google.visualization.PieChart(document.getElementById('chart'));"
                + "chart.draw(data, options);"
                + "}"
                + "</script></head>"
                + "<body style='margin:0;padding:0;background-color:transparent;'>"
                + "<div id='chart' style='width:100%; height:100vh;'></div>"
                + "</body></html>";

        chartWebView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
    }

    private String getLegendPosition() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
                ? "right" : "bottom";
    }
}