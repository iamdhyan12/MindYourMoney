package edu.northeastern.mindyourmoneyapp;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class ProfileActivity extends AppCompatActivity {
    TextView profileEmail, profilePassword, monthlyBudget, rewardPoints;
    TextView titleName, titleUsername;
    String nameUser, emailUser, userName, passwordUser;
    int budget, rewards;
    Button editProfile,doneButton,logoutButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        profileEmail = findViewById(R.id.profileEmail);
        profilePassword = findViewById(R.id.profilePassword);
        titleName = findViewById(R.id.titleName);
        titleUsername = findViewById(R.id.titleUsername);
        editProfile = findViewById(R.id.editButton);
        monthlyBudget = findViewById(R.id.monthlyBudget);
        rewardPoints = findViewById(R.id.rewardPoints);
        doneButton = findViewById(R.id.doneButton);
        logoutButton = findViewById(R.id.logoutbutton);
        showUserData();
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                passUserData(1);
            }
        });
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                passUserData(2);
            }
        });
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });
    }

    private void logout() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        Toast.makeText(ProfileActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void showUserData(){
        Intent intent = getIntent();
        nameUser = intent.getStringExtra("name");
        emailUser = intent.getStringExtra("email");
        userName = intent.getStringExtra("username");
        passwordUser = intent.getStringExtra("password");
        rewards = intent.getIntExtra("rewards",0);
        budget = intent.getIntExtra("budget",0);
        titleName.setText(nameUser);
        titleUsername.setText(userName);
        profileEmail.setText(emailUser);
        profilePassword.setText(passwordUser);
        monthlyBudget.setText("$ "+budget);
        rewardPoints.setText(""+rewards);
    }
    public void passUserData(int type){
        Intent intent;
        if(type == 1){
            intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
        } else {
            intent = new Intent(ProfileActivity.this,TransactionActivity.class);
        }
        intent.putExtra("name", nameUser);
        intent.putExtra("email", emailUser);
        intent.putExtra("username", userName);
        intent.putExtra("password", passwordUser);
        intent.putExtra("budget",budget);
        startActivity(intent);
    }
}