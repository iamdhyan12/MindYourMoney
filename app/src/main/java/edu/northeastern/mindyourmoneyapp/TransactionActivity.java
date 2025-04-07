package edu.northeastern.mindyourmoneyapp;

import static edu.northeastern.mindyourmoneyapp.Constants.setCategories;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import edu.northeastern.mindyourmoneyapp.databinding.ActivityTransactionBinding;

public class TransactionActivity extends AppCompatActivity {

    ActivityTransactionBinding binding;
    private boolean isStatsMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTransactionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setCategories();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new TransactionFragment())
                    .commit();
        }
        setupBottomNavigation();
        setRewardForUser();
    }

    private void setupBottomNavigation() {
        binding.navView.getMenu().clear();
        binding.navView.inflateMenu(R.menu.menu);
        binding.navView.setOnItemSelectedListener(item -> {
            if(item.getItemId() == R.id.addexpense){
                new AddTransactionFragment().show(getSupportFragmentManager(), null);
                binding.navView.setSelectedItemId(R.id.transaction);
                return true;
            }
            else if(item.getItemId() == R.id.stats && !isStatsMode){
                isStatsMode = true;
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new StatsFragment())
                        .commit();
                return true;
            }
            else if(item.getItemId() == R.id.transaction && isStatsMode){
                isStatsMode = false;
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new TransactionFragment())
                        .commit();
                return true;
            }else if(item.getItemId() == R.id.more){
                Intent intent = getIntent();
                String userUsername = intent.getStringExtra("username");
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
                Query checkUserDatabase = reference.orderByChild("username").equalTo(userUsername);
                checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String nameFromDB = snapshot.child(userUsername).child("name").getValue(String.class);
                        String emailFromDB = snapshot.child(userUsername).child("email").getValue(String.class);
                        String usernameFromDB = snapshot.child(userUsername).child("username").getValue(String.class);
                        String passwordFromDB = snapshot.child(userUsername).child("password").getValue(String.class);
                        Integer rewards = snapshot.child(userUsername).child("rewards").getValue(Integer.class);
                        Integer budget = snapshot.child(userUsername).child("budget").getValue(Integer.class);
                        Intent intent = new Intent(TransactionActivity.this, ProfileActivity.class);
                        intent.putExtra("name", nameFromDB);
                        intent.putExtra("email", emailFromDB);
                        intent.putExtra("username", usernameFromDB);
                        intent.putExtra("password", passwordFromDB);
                        intent.putExtra("rewards",rewards);
                        intent.putExtra("budget",budget);
                        startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
            return false;
        });
    }

    private void setRewardForUser(){
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_MONTH);
        int lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        if(today!=lastDay) return;

        Intent intent = getIntent();
        String username = intent.getStringExtra("username");

        DatabaseReference transactionRef = FirebaseDatabase.getInstance().getReference("transactionHistory");
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(username);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM, YYYY", Locale.getDefault());
        String filterDate = dateFormat.format(calendar.getTime());

        final double[] amountSum = {0.0};

        transactionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot transactionSnap : snapshot.getChildren()) {
                    String transUser = transactionSnap.child("username").getValue(String.class);
                    String monthYear = transactionSnap.child("monthYear").getValue(String.class);
                    Double amount = transactionSnap.child("amount").getValue(Double.class);

                    if (transUser != null && monthYear != null &&
                            transUser.equals(username) &&
                            monthYear.equals(filterDate) &&
                            amount != null) {
                        amountSum[0] += amount;
                    }
                }
                if(amountSum[0]==0.0){
                    return;
                }

                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot userSnap) {
                        Integer budget = userSnap.child("budget").getValue(Integer.class);
                        Integer rewards = userSnap.child("rewards").getValue(Integer.class);

                        if (budget == null || rewards == null) {
                            Log.e("Firebase", "Budget or Rewards not found for user: " + username);
                            return;
                        }

                        int updatedRewards = rewards;

                        if (amountSum[0] > budget) {
                            updatedRewards += 100;
                        } else if (amountSum[0] < budget && rewards > 0) {
                            updatedRewards -= 50;
                        }

                        // Update the reward in the database
                        userRef.child("rewards").setValue(updatedRewards)
                                .addOnSuccessListener(aVoid -> Log.d("Firebase", "Rewards updated"))
                                .addOnFailureListener(e -> Log.e("Firebase", "Failed to update rewards", e));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Firebase", "Error fetching user data: " + error.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error fetching transactions: " + error.getMessage());
            }
        });

    }


}




