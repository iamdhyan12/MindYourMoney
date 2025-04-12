package edu.northeastern.mindyourmoneyapp;

import static edu.northeastern.mindyourmoneyapp.Constants.setCategories;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import edu.northeastern.mindyourmoneyapp.databinding.ActivityTransactionBinding;

public class TransactionActivity extends AppCompatActivity {

    ActivityTransactionBinding binding;
    private boolean isStatsMode = false;
    private static final int NOTIFICATION_PERMISSION_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTransactionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setCategories();
        setupBottomNavigation();
        setRewardForUser();
        requestNotificationPermission();
        scheduleDailyReminder();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new TransactionFragment())
                    .commit();
        }

    }

    private void setupBottomNavigation() {
        binding.navView.getMenu().clear();
        binding.navView.inflateMenu(R.menu.menu);
        binding.navView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.addexpense) {
                new AddTransactionFragment().show(getSupportFragmentManager(), null);
                binding.navView.setSelectedItemId(R.id.transaction);
                return true;
            } else if (item.getItemId() == R.id.stats && !isStatsMode) {
                isStatsMode = true;
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new StatsFragment())
                        .commit();
                return true;
            } else if (item.getItemId() == R.id.transaction && isStatsMode) {
                isStatsMode = false;
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new TransactionFragment())
                        .commit();
                return true;
            } else if (item.getItemId() == R.id.more) {
                String userUsername = getIntent().getStringExtra("username");
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
                        intent.putExtra("rewards", rewards);
                        intent.putExtra("budget", budget);
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

    private void setRewardForUser() {
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_MONTH);
        int lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        if (today != lastDay) return;

        String username = getIntent().getStringExtra("username");

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

                if (amountSum[0] == 0.0) return;

                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot userSnap) {
                        Integer budget = userSnap.child("budget").getValue(Integer.class);
                        Integer rewards = userSnap.child("rewards").getValue(Integer.class);
                        if (budget == null || rewards == null) return;

                        int updatedRewards = rewards;

                        if (amountSum[0] > budget) {
                            updatedRewards += 100;
                        } else if (amountSum[0] < budget && rewards > 0) {
                            updatedRewards -= 50;
                        }

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

    private void scheduleDailyReminder() {
        Intent intent = new Intent(this, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
//        calendar.set(Calendar.HOUR_OF_DAY, 21); // 9 PM
//        calendar.set(Calendar.MINUTE, 0);
//        calendar.set(Calendar.SECOND, 0);
        calendar.add(Calendar.MINUTE, 1);

        if (alarmManager != null) {
            alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
            );
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == NOTIFICATION_PERMISSION_CODE && grantResults.length > 0) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
