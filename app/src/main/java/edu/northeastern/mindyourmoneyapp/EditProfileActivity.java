package edu.northeastern.mindyourmoneyapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import edu.northeastern.mindyourmoneyapp.R;

public class EditProfileActivity extends AppCompatActivity {

    EditText editName, editEmail,  editPassword, editBudget;
    Button saveButton, doneButton;
    String nameUser, emailUser, usernameUser, passwordUser;
    int budgetUser;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        reference = FirebaseDatabase.getInstance().getReference("users");
        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        editBudget = findViewById(R.id.editBudget);
        saveButton = findViewById(R.id.saveButton);
        doneButton = findViewById(R.id.doneButton);

        showData();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isBudgetChanged()){
                    Toast.makeText(EditProfileActivity.this, "New Budget Saved", Toast.LENGTH_SHORT).show();
                }
                if(isNameChanged()){
                    Toast.makeText(EditProfileActivity.this, "New Name Saved", Toast.LENGTH_SHORT).show();
                }
                if(isPasswordChanged()){
                    Toast.makeText(EditProfileActivity.this, "New Password Saved", Toast.LENGTH_SHORT).show();
                }
                if (isEmailChanged()) {
                    Toast.makeText(EditProfileActivity.this, "New Email Saved", Toast.LENGTH_SHORT).show();
                }
            }
        });

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
                Query checkUserDatabase = reference.orderByChild("username").equalTo(usernameUser);
                checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                                String nameFromDB = snapshot.child(usernameUser).child("name").getValue(String.class);
                                String emailFromDB = snapshot.child(usernameUser).child("email").getValue(String.class);
                                String usernameFromDB = snapshot.child(usernameUser).child("username").getValue(String.class);
                                Integer rewards = snapshot.child(usernameUser).child("rewards").getValue(Integer.class);
                                Integer budget = snapshot.child(usernameUser).child("budget").getValue(Integer.class);
                                String passwordFromDB = snapshot.child(usernameUser).child("password").getValue(String.class);
                                Intent intent = new Intent(EditProfileActivity.this, TransactionActivity.class);
                                intent.putExtra("name", nameFromDB);
                                intent.putExtra("email", emailFromDB);
                                intent.putExtra("username", usernameFromDB);
                                intent.putExtra("password", passwordFromDB);
                                intent.putExtra("rewards",rewards);
                                intent.putExtra("budget",budget);
                                startActivity(intent);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }
    public boolean isNameChanged(){
        if (!nameUser.equals(editName.getText().toString())){
            reference.child(usernameUser).child("name").setValue(editName.getText().toString());
            nameUser = editName.getText().toString();
            return true;
        } else{
            return false;
        }
    }
    public boolean isEmailChanged(){
        if (!emailUser.equals(editName.getText().toString())){
            reference.child(usernameUser).child("email").setValue(editEmail.getText().toString());
            emailUser = editEmail.getText().toString();
            return true;
        } else{
            return false;
        }
    }
    public boolean isPasswordChanged(){
        if (!passwordUser.equals(editPassword.getText().toString())){
            reference.child(usernameUser).child("password").setValue(editPassword.getText().toString());
            passwordUser = editPassword.getText().toString();
            return true;
        } else{
            return false;
        }
    }
    public boolean isBudgetChanged(){
        Toast.makeText(EditProfileActivity.this,"here start",Toast.LENGTH_LONG).show();
        String budgetStr = editBudget.getText().toString();
        int budget = Integer.parseInt(budgetStr);
        Toast.makeText(EditProfileActivity.this,"here 1",Toast.LENGTH_LONG).show();
        if (budgetUser != budget){
            reference.child(usernameUser).child("budget").setValue(budget);
            budgetUser = budget;
            return true;
        } else{
            return false;
        }
    }
    public void showData(){
        Intent intent = getIntent();
        nameUser = intent.getStringExtra("name");
        emailUser = intent.getStringExtra("email");
        usernameUser = intent.getStringExtra("username");
        passwordUser = intent.getStringExtra("password");
        budgetUser = intent.getIntExtra("budget",0);
        editName.setText(nameUser);
        editEmail.setText(emailUser);
        editPassword.setText(passwordUser);
        editBudget.setText(""+budgetUser);
    }
}