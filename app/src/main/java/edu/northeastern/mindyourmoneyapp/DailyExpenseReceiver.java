package edu.northeastern.mindyourmoneyapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DailyExpenseReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Reschedule for next day first
        TransactionActivity.schedule5PMExpenseNotification(context);

        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String username = prefs.getString("username", null);

        if (username == null) {
            Log.e("ExpenseReceiver", "User not logged in");
            return;
        }

        String currentMonth = new SimpleDateFormat("MMMM, yyyy", Locale.getDefault())
                .format(Calendar.getInstance().getTime());

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("transactionHistory");

        ref.orderByChild("username").equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        double sum = 0.0;
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            String monthYear = snap.child("monthYear").getValue(String.class);
                            Double amount = snap.child("amount").getValue(Double.class);
                            if (monthYear != null && monthYear.equals(currentMonth) && amount != null) {
                                sum += amount;
                            }
                        }
                        sendNotification(context, sum, username);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e("ExpenseReceiver", "Database error: " + error.getMessage());
                    }
                });
    }

    private void sendNotification(Context context, double sum, String username) {
        String channelId = "expense_summary_channel";
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create intent to open app with username
        Intent appIntent = new Intent(context, TransactionActivity.class);
        appIntent.putExtra("username", username);
        appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                appIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("💸 Monthly Expense Update")
                .setContentText("You've spent $" + (int) sum + " so far this month.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Create notification channel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Expense Summary",
                    NotificationManager.IMPORTANCE_HIGH
            );
            manager.createNotificationChannel(channel);
        }

        manager.notify(1002, builder.build());
    }
}