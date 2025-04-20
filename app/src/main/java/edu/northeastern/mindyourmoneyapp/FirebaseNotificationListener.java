package edu.northeastern.mindyourmoneyapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class FirebaseNotificationListener {
    private final Context context;
    int notificationId = 101;
    public FirebaseNotificationListener(Context context) {
        this.context = context;
    }
    public void startListening(String username) {
        DatabaseReference transactionRef = FirebaseDatabase.getInstance().getReference("transactionHistory");

        transactionRef.orderByChild("username").equalTo(username)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot newTransaction, String previousChildName) {
                        String monthYear = newTransaction.child("monthYear").getValue(String.class);
                        String transUser = newTransaction.child("username").getValue(String.class);
                        Double newAmount = newTransaction.child("amount").getValue(Double.class);

                        String currentMonthYear = new SimpleDateFormat("MMMM, yyyy", Locale.getDefault())
                                .format(Calendar.getInstance().getTime());

                        if (transUser == null || monthYear == null || newAmount == null) return;
                        if (!transUser.equals(username) || !monthYear.equals(currentMonthYear)) return;

                        DatabaseReference allRef = FirebaseDatabase.getInstance().getReference("transactionHistory");
                        allRef.orderByChild("username").equalTo(username)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot snapshot) {
                                        double totalSum = 0.0;

                                        for (DataSnapshot snap : snapshot.getChildren()) {
                                            String mY = snap.child("monthYear").getValue(String.class);
                                            Double amt = snap.child("amount").getValue(Double.class);

                                            if (mY != null && mY.equals(currentMonthYear) && amt != null) {
                                                totalSum += amt;
                                            }
                                        }

                                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(username);
                                        double finalTotalSum = totalSum;
                                        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot userSnap) {
                                                Integer budget = userSnap.child("budget").getValue(Integer.class);

                                                if (budget != null && finalTotalSum > budget) {
                                                    showNotification("⚠ Over Budget Alert",
                                                            "You’ve spent more then your set monthly budget!");
                                                }
                                            }

                                            @Override public void onCancelled(DatabaseError error) {}
                                        });
                                    }

                                    @Override public void onCancelled(DatabaseError error) {}
                                });
                    }

                    @Override public void onChildChanged(DataSnapshot snapshot, String previousChildName) {}
                    @Override public void onChildRemoved(DataSnapshot snapshot) {}
                    @Override public void onChildMoved(DataSnapshot snapshot, String previousChildName) {}
                    @Override public void onCancelled(DatabaseError error) {}
                });
    }

    private void showNotification(String title, String message) {
        String channelId = "firebase_realtime";
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId, "Realtime Firebase Alerts", NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(context, TransactionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        manager.notify(notificationId, builder.build());
    }
}
