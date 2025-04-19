package edu.northeastern.mindyourmoneyapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DailyExpenseWorker extends Worker {

    public DailyExpenseWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String username = prefs.getString("username", null);

        if (username == null) {
            Log.e("ExpenseWorker", "User not logged in");
            return Result.failure();
        }

        String currentMonth = new SimpleDateFormat("MMMM, yyyy", Locale.getDefault())
                .format(Calendar.getInstance().getTime());

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("transactionHistory");

        ref.orderByChild("username").equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        double sum = 0.0;
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            String monthYear = snap.child("monthYear").getValue(String.class);
                            Double amount = snap.child("amount").getValue(Double.class);
                            if (monthYear != null && monthYear.equals(currentMonth) && amount != null) {
                                sum += amount;
                            }
                        }
                        sendNotification(sum, username);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("ExpenseWorker", "Database error: " + error.getMessage());
                    }
                });

        return Result.success();
    }

    private void sendNotification(double sum, String username) {
        Context context = getApplicationContext();
        String channelId = "expense_channel";

        Intent intent = new Intent(context, TransactionActivity.class);
        intent.putExtra("username", username);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("💸 Monthly Expense Update")
                .setContentText("You've spent $" + (int) sum + " so far this month.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
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
