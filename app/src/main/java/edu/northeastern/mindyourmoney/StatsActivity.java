package edu.northeastern.mindyourmoney;

import static edu.northeastern.mindyourmoney.Constants.setCategories;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Pie;
import com.anychart.enums.Align;
import com.anychart.enums.LegendLayout;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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

import edu.northeastern.mindyourmoney.databinding.ActivityMainBinding;
import edu.northeastern.mindyourmoney.databinding.ActivityStatsBinding;

public class StatsActivity extends AppCompatActivity {

    ActivityStatsBinding binding;
    Pie pie;
    Calendar calendar;
    int selectedTab = 0;

    int selectedType = 0;
    String currentDisplayDate;
    private DatabaseReference mindYourMoneyRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityStatsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mindYourMoneyRef = FirebaseDatabase.getInstance().getReference("transactionHistory");
        calendar = Calendar.getInstance();
        currentDisplayDate = updateDate();
        setCategories();
        binding.bottomNavigation.setSelectedItemId(R.id.stats);

        binding.nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedTab==0) {
                    calendar.add(Calendar.DATE, 1);
                }else if(selectedTab==1){
                    calendar.add(Calendar.MONTH,1);
                }else if(selectedTab==2){
                    calendar.add(Calendar.YEAR,1);
                }
                currentDisplayDate = updateDate();

            }
        });


        binding.category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedType = 0;
                updateDate();
            }
        });

        binding.mode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedType = 1;
                updateDate();
            }
        });

        binding.prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedTab==0) {
                    calendar.add(Calendar.DATE, -1);
                }else if(selectedTab==1){
                    calendar.add(Calendar.MONTH,-1);
                }else if(selectedTab==2){
                    calendar.add(Calendar.YEAR,-1);
                }
                currentDisplayDate = updateDate();
            }
        });
        binding.bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                if(item.getItemId() == R.id.addexpense){
                    new AddTransactionFragment().show(getSupportFragmentManager(),null);
                }else if(item.getItemId() == R.id.stats){
                    return true;
                }
                return true;
            }
        });
        fetchTransactionsForDate(currentDisplayDate);
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getText().equals("Monthly")){
                    selectedTab = 1;
                } else if (tab.getText().equals("Daily")) {
                    selectedTab = 0;
                }else {
                    selectedTab = 2;
                }
                updateDate();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    String updateDate(){
        SimpleDateFormat simpleDateFormat;
        if(selectedTab==2) {
            simpleDateFormat = new SimpleDateFormat("YYYY");
        }else if(selectedTab==1){
            simpleDateFormat = new SimpleDateFormat("MMMM, YYYY");
        }else{
            simpleDateFormat = new SimpleDateFormat("MMMM dd, YYYY");
        }
        binding.currentDate.setText(simpleDateFormat.format(calendar.getTime()));
        fetchTransactionsForDate(simpleDateFormat.format(calendar.getTime()));
        return simpleDateFormat.format(calendar.getTime());
    }
    private void fetchTransactionsForDate(String selectedDate) {
        String path = "";
        if(selectedTab == 0){
            path = "date";
        }else if(selectedTab == 1){
            path = "monthYear";
        }else if(selectedTab==2){
            path = "year";
        }
        mindYourMoneyRef.orderByChild(path).equalTo(selectedDate)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<Transaction> transactions = new ArrayList<>();

                        for (DataSnapshot transactionSnapshot : snapshot.getChildren()) {
                            Transaction transaction = transactionSnapshot.getValue(Transaction.class);
                            if (transaction != null) {
                                transactions.add(transaction);
                            }
                        }
                        if (transactions.isEmpty()) {

                            binding.emptyState.setVisibility(View.VISIBLE);
                            binding.anyChart.setVisibility(View.GONE);

                            if (pie != null) {
                                pie.data(new ArrayList<>());
                            }
                        } else {
                            binding.emptyState.setVisibility(View.GONE);
                            binding.anyChart.setVisibility(View.VISIBLE);
                            updatePieChart(transactions);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("MainActivity", "Failed to read data", error.toException());
                    }
                });
    }


    private void updatePieChart(List<Transaction> transactions) {
        binding.anyChart.clear();
        if(selectedType == 0) {
            displayByCategory(transactions);
        }else if(selectedType == 1){
            displayByMode(transactions);
        }

    }

    private void displayByCategory(List<Transaction> transactions) {
        binding.anyChart.clear();
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

        if (pie == null) {
            pie = AnyChart.pie();
        }

        pie.title("Expenses by Category");  // Ensure the title updates
        pie.legend().title().enabled(true);
        pie.legend().title().text("Categories").padding(0d, 0d, 10d, 0d);
        pie.legend().position("center-bottom").itemsLayout(LegendLayout.HORIZONTAL).align(Align.CENTER);

        pie.data(dataEntries);
        binding.anyChart.setChart(pie);
    }

    private void displayByMode(List<Transaction> transactions) {
        binding.anyChart.clear();
        Map<String, Float> categoryTotals = new HashMap<>();
        for (Transaction t : transactions) {
            String category = t.getAccount();
            float amount = (float) t.getAmount();
            categoryTotals.put(category, categoryTotals.getOrDefault(category, 0f) + amount);
        }
        List<DataEntry> dataEntries = new ArrayList<>();
        for (Map.Entry<String, Float> entry : categoryTotals.entrySet()) {
            dataEntries.add(new ValueDataEntry(entry.getKey(), entry.getValue()));
        }

        if (pie == null) {
            pie = AnyChart.pie();
        }

        pie.title("Expenses by Payment Mode");  // Ensure the title updates
        pie.legend().title().enabled(true);
        pie.legend().title().text("Payment Mode").padding(0d, 0d, 10d, 0d);
        pie.legend().position("center-bottom").itemsLayout(LegendLayout.HORIZONTAL).align(Align.CENTER);

        pie.data(dataEntries);
        binding.anyChart.setChart(pie);
    }



}