package edu.northeastern.mindyourmoneyapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        checkTransactionLoggedToday(context);
    }

    private void checkTransactionLoggedToday(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String username = prefs.getString("username", null);
        if (username == null) return;

        String today = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                .format(Calendar.getInstance().getTime());

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("transactionHistory");

        ref.orderByChild("username").equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean loggedToday = false;

                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Transaction t = snap.getValue(Transaction.class);
                            if (t != null && today.equals(t.getDate())) {
                                loggedToday = true;
                                break;
                            }
                        }

                        if (!loggedToday) {
                            showNotification(context);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void showNotification(Context context) {
        String channelId = "daily_reminder_channel";
        String channelName = "Daily Reminder";
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setContentTitle("Daily Expense Reminder")
                .setContentText("Don't forget to log today's expenses and stay on top of your budget!")
                .setSmallIcon(R.drawable.ic_notification)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
//                .setAutoCancel(true);


        manager.notify(1001, builder.build());
    }
}
