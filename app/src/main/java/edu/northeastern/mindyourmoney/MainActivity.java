package edu.northeastern.mindyourmoney;

import static edu.northeastern.mindyourmoney.Constants.setCategories;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import edu.northeastern.mindyourmoney.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        calendar = Calendar.getInstance();
        updateDate();

        setCategories();

        binding.nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calendar.add(Calendar.DATE , 1);
                updateDate();
            }
        });

        binding.prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calendar.add(Calendar.DATE , -1);
                updateDate();
            }
        });

        binding.bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                if(item.getItemId() == R.id.addexpense){
                    new AddTransactionFragment().show(getSupportFragmentManager(),null);
                }
                return true;
            }
        });

        ArrayList<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction("Business","Cash","Some notes here", new Date(),500,2));
        transactions.add(new Transaction("Business","Cash","Some notes here", new Date(),500,2));
        transactions.add(new Transaction("Business","Cash","Some notes here", new Date(),500,2));
        transactions.add(new Transaction("Business","Cash","Some notes here", new Date(),500,2));

        TransactionsAdapter transactionsAdapter = new TransactionsAdapter(this,transactions);
        binding.transactionList.setLayoutManager(new LinearLayoutManager(this));
        binding.transactionList.setAdapter(transactionsAdapter);
    }

    void updateDate(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM dd, YYYY");
        binding.currentDate.setText(simpleDateFormat.format(calendar.getTime()));
    }


}