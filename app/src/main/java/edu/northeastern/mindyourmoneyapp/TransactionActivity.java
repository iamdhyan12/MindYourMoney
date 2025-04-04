package edu.northeastern.mindyourmoneyapp;

import static edu.northeastern.mindyourmoneyapp.Constants.setCategories;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.navigation.NavigationBarView;

import edu.northeastern.mindyourmoneyapp.databinding.ActivityTransactionBinding;

public class TransactionActivity extends AppCompatActivity {

    ActivityTransactionBinding binding;
    private boolean isStatsMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTransactionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Load default fragment
        setCategories();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new TransactionFragment())
                    .commit();
        }

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        binding.navView.getMenu().clear();
        binding.navView.inflateMenu(R.menu.menu);
        binding.navView.setOnItemSelectedListener(item -> {
            if(item.getItemId() == R.id.addexpense){
                new AddTransactionFragment().show(getSupportFragmentManager(), null);
                binding.navView.setSelectedItemId(R.id.transaction);
                return true;
            }
            else if(item.getItemId() == R.id.stats && !isStatsMode){
                isStatsMode = true;
//                binding.navView.getMenu().clear();
//                binding.navView.inflateMenu(R.menu.menu2); // Stats menu with 4 buttons

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new StatsFragment())
                        .commit();
                return true;
            }
            else if(item.getItemId() == R.id.transaction && isStatsMode){
                isStatsMode = false;
//                binding.navView.getMenu().clear();
//                binding.navView.inflateMenu(R.menu.menu); // Transaction menu with 5 buttons

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new TransactionFragment())
                        .commit();
                return true;
            }
            return false;
        });
    }


//    @Override
//    protected void onResume() {
//        super.onResume();
//        binding.navView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
//            @Override
//            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//                if(item.getItemId() == R.id.addexpense){
//                    isStatsMode = false;
//                    binding.navView.setSelectedItemId(R.id.transaction);
//                }
//                return true;
//            }
//        });
//    }
}