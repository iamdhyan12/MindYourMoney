package edu.northeastern.mindyourmoney;

import static edu.northeastern.mindyourmoney.Constants.setCategories;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.anychart.AnyChart;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Pie;
import com.anychart.enums.Align;
import com.anychart.enums.LegendLayout;
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

import edu.northeastern.mindyourmoney.databinding.ActivityStatsBinding;

public class StatsActivity extends AppCompatActivity {

    ActivityStatsBinding binding;
    Calendar calendar;
    int selectedTab = 0;
    int selectedType = 0; // 0 for category, 1 for payment mode
    String currentDisplayDate;
    private DatabaseReference mindYourMoneyRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStatsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mindYourMoneyRef = FirebaseDatabase.getInstance().getReference("transactionHistory");
        calendar = Calendar.getInstance();
        setCategories();

        // Initialize the selected type and tab
        selectedType = 0; // Default to category view
        selectedTab = 0;  // Default to daily view

        // Set UI state for the category button as selected
        updateButtonState();

        binding.bottomNavigation.setSelectedItemId(R.id.stats);

        // Initialize date and fetch data
        currentDisplayDate = updateDate();

        // Set up navigation buttons
        binding.nextButton.setOnClickListener(view -> {
            if (selectedTab == 0) {
                calendar.add(Calendar.DATE, 1);
            } else if (selectedTab == 1) {
                calendar.add(Calendar.MONTH, 1);
            } else {
                calendar.add(Calendar.YEAR, 1);
            }
            currentDisplayDate = updateDate();
        });

        binding.prevButton.setOnClickListener(view -> {
            if (selectedTab == 0) {
                calendar.add(Calendar.DATE, -1);
            } else if (selectedTab == 1) {
                calendar.add(Calendar.MONTH, -1);
            } else {
                calendar.add(Calendar.YEAR, -1);
            }
            currentDisplayDate = updateDate();
        });

        // Set up type selection buttons
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

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.addexpense) {
                new AddTransactionFragment().show(getSupportFragmentManager(), null);
            } else if (item.getItemId() == R.id.stats) {
                return true;
            }
            return true;
        });

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getText().equals("Monthly")) {
                    selectedTab = 1;
                } else if (tab.getText().equals("Daily")) {
                    selectedTab = 0;
                } else {
                    selectedTab = 2;
                }
                updateDate();
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Refresh the chart when orientation changes
        binding.anyChart.clear();
        fetchTransactionsForDate(currentDisplayDate);
    }

    // Update button UI state based on selection
    private void updateButtonState() {
        if (selectedType == 0) {
            binding.category.setBackgroundColor(getResources().getColor(R.color.blue));
            binding.category.setTextColor(getResources().getColor(R.color.white));
            binding.mode.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            binding.mode.setTextColor(getResources().getColor(R.color.blue));
        } else {
            binding.mode.setBackgroundColor(getResources().getColor(R.color.blue));
            binding.mode.setTextColor(getResources().getColor(R.color.white));
            binding.category.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            binding.category.setTextColor(getResources().getColor(R.color.blue));
        }
    }

    String updateDate() {
        SimpleDateFormat simpleDateFormat;
        if (selectedTab == 2) {
            simpleDateFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
        } else if (selectedTab == 1) {
            simpleDateFormat = new SimpleDateFormat("MMMM, yyyy", Locale.getDefault());
        } else {
            simpleDateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        }

        String formattedDate = simpleDateFormat.format(calendar.getTime());
        binding.currentDate.setText(formattedDate);
        fetchTransactionsForDate(formattedDate);
        return formattedDate;
    }

    private void fetchTransactionsForDate(String selectedDate) {
        String path;
        if (selectedTab == 0) {
            path = "date";
        } else if (selectedTab == 1) {
            path = "monthYear";
        } else {
            path = "year";
        }

        mindYourMoneyRef.orderByChild(path).equalTo(selectedDate)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Transaction> transactions = new ArrayList<>();

                        for (DataSnapshot transactionSnapshot : snapshot.getChildren()) {
                            Transaction transaction = transactionSnapshot.getValue(Transaction.class);
                            if (transaction != null) {
                                transactions.add(transaction);
                            }
                        }

                        if (transactions.isEmpty()) {
                            binding.emptyState.setVisibility(View.VISIBLE);
                            binding.anyChart.setVisibility(View.GONE);
                        } else {
                            binding.emptyState.setVisibility(View.GONE);
                            binding.anyChart.setVisibility(View.VISIBLE);
                            updatePieChart(transactions);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("StatsActivity", "Failed to read data", error.toException());
                    }
                });
    }

    private void updatePieChart(List<Transaction> transactions) {
        // Clear any existing chart first
        binding.anyChart.clear();

        // Create a new pie chart
        Pie pie = AnyChart.pie();

        // Set up the chart based on selected type
        if (selectedType == 0) {
            setupCategoryChart(pie, transactions);
        } else {
            setupPaymentModeChart(pie, transactions);
        }

        // Apply the chart to the view
        binding.anyChart.setChart(pie);
    }

    private void setupCategoryChart(Pie pie, List<Transaction> transactions) {
        Map<String, Float> categoryTotals = new HashMap<>();

        for (Transaction t : transactions) {
            String category = t.getCategory();
            float amount = (float) t.getAmount();
            categoryTotals.put(category, categoryTotals.getOrDefault(category, 0f) + amount);
        }

        List<DataEntry> dataEntries = new ArrayList<>();
        for (Map.Entry<String, Float> entry : categoryTotals.entrySet()) {
            dataEntries.add(new ValueDataEntry(entry.getKey(), entry.getValue()));
        }

        pie.title("Expenses by Category");
        pie.labels().position("outside");

        // Configure legend
        pie.legend().title().enabled(true);
        pie.legend().title().text("Categories").padding(0d, 0d, 10d, 0d);

        // Adjust legend position based on orientation
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            pie.legend().position("right");
            pie.legend().itemsLayout(LegendLayout.VERTICAL);
        } else {
            pie.legend().position("center-bottom");
            pie.legend().itemsLayout(LegendLayout.HORIZONTAL);
        }

        pie.legend().align(Align.CENTER);

        // Set the data
        pie.data(dataEntries);
    }

    private void setupPaymentModeChart(Pie pie, List<Transaction> transactions) {
        Map<String, Float> modeTotals = new HashMap<>();

        for (Transaction t : transactions) {
            String mode = t.getAccount();
            float amount = (float) t.getAmount();
            modeTotals.put(mode, modeTotals.getOrDefault(mode, 0f) + amount);
        }

        List<DataEntry> dataEntries = new ArrayList<>();
        for (Map.Entry<String, Float> entry : modeTotals.entrySet()) {
            dataEntries.add(new ValueDataEntry(entry.getKey(), entry.getValue()));
        }

        pie.title("Expenses by Payment Mode");
        pie.labels().position("outside");

        // Configure legend
        pie.legend().title().enabled(true);
        pie.legend().title().text("Payment Mode").padding(0d, 0d, 10d, 0d);

        // Adjust legend position based on orientation
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            pie.legend().position("right");
            pie.legend().itemsLayout(LegendLayout.VERTICAL);
        } else {
            pie.legend().position("center-bottom");
            pie.legend().itemsLayout(LegendLayout.HORIZONTAL);
        }

        pie.legend().align(Align.CENTER);

        // Set the data
        pie.data(dataEntries);
    }
}