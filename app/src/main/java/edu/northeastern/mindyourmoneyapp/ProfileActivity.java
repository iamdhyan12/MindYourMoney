package edu.northeastern.mindyourmoneyapp;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class ProfileActivity extends AppCompatActivity {
    TextView profileEmail, profilePassword, monthlyBudget, rewardPoints;
    TextView titleName, titleUsername;
    String nameUser, emailUser, userName, passwordUser;
    int budget, rewards;
    Button editProfile,doneButton;
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