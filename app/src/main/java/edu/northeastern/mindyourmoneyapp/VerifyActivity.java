package edu.northeastern.mindyourmoneyapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class VerifyActivity extends AppCompatActivity {

    String username, otp, email;
    private EditText otpInput;
    private Button verifyOtpButton;
    private Button resendOtpButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_verify);
        Intent intent = getIntent();

        username = intent.getStringExtra("username");
        otp = intent.getStringExtra("otp");
        email = intent.getStringExtra("email");

        otpInput = findViewById(R.id.otpInput);
        verifyOtpButton = findViewById(R.id.verifyOtpButton);
        resendOtpButton = findViewById(R.id.resendOtpButton);

        verifyOtpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredOtp = otpInput.getText().toString().trim();
                if (enteredOtp.isEmpty()) {
                    otpInput.setError("OTP cannot be empty");
                } else {
                    if (enteredOtp.equals(otp)) {
                        Intent intent = new Intent(VerifyActivity.this,ResetPasswordActivity.class);
                        intent.putExtra("username",username);
                        startActivity(intent);
                    } else {
                        Toast.makeText(VerifyActivity.this, "Incorrect Otp", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        resendOtpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resendingOtp();
            }
        });

    }

    private void resendingOtp() {
        otp = GmailSender.generateOTP();
        GmailSender.sendEmail(email,otp);
    }
}