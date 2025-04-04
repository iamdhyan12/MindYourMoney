package edu.northeastern.mindyourmoneyapp;

import static edu.northeastern.mindyourmoneyapp.Constants.setCategories;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import edu.northeastern.mindyourmoneyapp.databinding.ActivityTransactionBinding;

public class TransactionActivity extends AppCompatActivity implements TransactionsAdapter.OnTransactionDeletedListener {

    ActivityTransactionBinding binding;
    Calendar calendar;
    int selectedTab = 0;
    String currentDisplayDate;
    private DatabaseReference mindYourMoneyRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTransactionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mindYourMoneyRef = FirebaseDatabase.getInstance().getReference("transactionHistory");
        calendar = Calendar.getInstance();
        currentDisplayDate = updateDate();
        setCategories();
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

        binding.navView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                if(item.getItemId() == R.id.addexpense){
                    new AddTransactionFragment().show(getSupportFragmentManager(),null);
                }
                else if(item.getItemId() == R.id.stats){
                    Intent intent = new Intent(TransactionActivity.this,StatsActivity.class);
                    startActivity(intent);
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
                        TransactionsAdapter transactionsAdapter = new TransactionsAdapter(TransactionActivity.this, transactions, TransactionActivity.this);
                        binding.transactionList.setLayoutManager(new LinearLayoutManager(TransactionActivity.this));
                        binding.transactionList.setAdapter(transactionsAdapter);
                        if(transactions.size()==0){
                            binding.emptyState.setVisibility(View.VISIBLE);
                        }else {
                            binding.emptyState.setVisibility(View.GONE);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("TransactionActivity", "Failed to read data", error.toException());
                    }
                });
    }

    public void fetchTransactions() {
        String selectedDate = updateDate();
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

                        TransactionsAdapter transactionsAdapter = new TransactionsAdapter(TransactionActivity.this, transactions, TransactionActivity.this);
                        binding.transactionList.setLayoutManager(new LinearLayoutManager(TransactionActivity.this));
                        binding.transactionList.setAdapter(transactionsAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("TransactionActivity", "Failed to read data", error.toException());
                    }
                });
    }

    @Override
    public void onTransactionDeleted() {
        fetchTransactions();
    }
}