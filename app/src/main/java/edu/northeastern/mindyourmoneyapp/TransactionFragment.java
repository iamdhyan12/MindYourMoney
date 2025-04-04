package edu.northeastern.mindyourmoneyapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import edu.northeastern.mindyourmoneyapp.databinding.FragmentTransactionBinding;

public class TransactionFragment extends Fragment implements TransactionsAdapter.OnTransactionDeletedListener {

    private FragmentTransactionBinding binding;
    private DatabaseReference mindYourMoneyRef;
    private Calendar calendar;
    private int selectedTab = 0;
    private String currentDisplayDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTransactionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mindYourMoneyRef = FirebaseDatabase.getInstance().getReference("transactionHistory");
        calendar = Calendar.getInstance();

        setupDateNavigation();
        setupTabLayout();

        // Set initial tab based on default selection
        TabLayout.Tab tab = binding.tabLayout.getTabAt(selectedTab);
        if (tab != null) {
            tab.select();
        }

        currentDisplayDate = updateDate();
    }

    private void setupDateNavigation() {
        binding.nextButton.setOnClickListener(view -> {
            if(selectedTab==0) {
                calendar.add(Calendar.DATE, 1);
            }else if(selectedTab==1){
                calendar.add(Calendar.MONTH,1);
            }else if(selectedTab==2){
                calendar.add(Calendar.YEAR,1);
            }
            currentDisplayDate = updateDate();
        });

        binding.prevButton.setOnClickListener(view -> {
            if(selectedTab==0) {
                calendar.add(Calendar.DATE, -1);
            }else if(selectedTab==1){
                calendar.add(Calendar.MONTH,-1);
            }else if(selectedTab==2){
                calendar.add(Calendar.YEAR,-1);
            }
            currentDisplayDate = updateDate();
        });
    }

    private void setupTabLayout() {
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
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private String updateDate(){
        SimpleDateFormat simpleDateFormat;
        if(selectedTab==2) {
            simpleDateFormat = new SimpleDateFormat("YYYY");
        }else if(selectedTab==1){
            simpleDateFormat = new SimpleDateFormat("MMMM, YYYY");
        }else{
            simpleDateFormat = new SimpleDateFormat("MMMM dd, YYYY");
        }
        String formattedDate = simpleDateFormat.format(calendar.getTime());
        binding.currentDate.setText(formattedDate);
        fetchTransactionsForDate(formattedDate);
        return formattedDate;
    }

    public void fetchTransactionsForDate(String selectedDate) {
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

                        TransactionsAdapter transactionsAdapter = new TransactionsAdapter(getContext(), transactions, TransactionFragment.this);
                        binding.transactionList.setLayoutManager(new LinearLayoutManager(getContext()));
                        binding.transactionList.setAdapter(transactionsAdapter);

                        if(transactions.size()==0){
                            binding.emptyState.setVisibility(View.VISIBLE);
                        }else {
                            binding.emptyState.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("TransactionsFragment", "Failed to read data", error.toException());
                    }
                });
    }

    @Override
    public void onTransactionDeleted() {
        fetchTransactionsForDate(currentDisplayDate);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}