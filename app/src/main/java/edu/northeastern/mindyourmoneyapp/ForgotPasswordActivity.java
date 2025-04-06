package edu.northeastern.mindyourmoneyapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText userName;
    private Button sendOtpButton;
    private TextView redirectLoginText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);


        userName = findViewById(R.id.userName);
        sendOtpButton = findViewById(R.id.sendOtpButton);
        redirectLoginText = findViewById(R.id.redirectLoginText);


        sendOtpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = userName.getText().toString().trim();

                if (username.isEmpty()) {
                    Toast.makeText(ForgotPasswordActivity.this, "Please enter username", Toast.LENGTH_SHORT).show();
                } else {
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
                    Query checkUserDatabase = reference.orderByChild("username").equalTo(username);

                    checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String emailFromDB = snapshot.child(username).child("email").getValue(String.class);
                                String otp = GmailSender.generateOTP();
                                GmailSender.sendEmail(emailFromDB,otp);
                                Intent intent = new Intent(ForgotPasswordActivity.this, VerifyActivity.class);
                                intent.putExtra("otp", otp);
                                intent.putExtra("username",username);
                                intent.putExtra("email",emailFromDB);
                                startActivity(intent);

                            } else {
                                Toast.makeText(ForgotPasswordActivity.this, "Please enter valid username", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(ForgotPasswordActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        redirectLoginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }


}