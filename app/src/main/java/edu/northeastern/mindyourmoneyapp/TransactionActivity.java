package edu.northeastern.mindyourmoneyapp;

import static edu.northeastern.mindyourmoneyapp.Constants.setCategories;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import edu.northeastern.mindyourmoneyapp.databinding.ActivityTransactionBinding;

public class TransactionActivity extends AppCompatActivity {

    ActivityTransactionBinding binding;
    private boolean isStatsMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTransactionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new StatsFragment())
                        .commit();
                return true;
            }
            else if(item.getItemId() == R.id.transaction && isStatsMode){
                isStatsMode = false;
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new TransactionFragment())
                        .commit();
                return true;
            }else if(item.getItemId() == R.id.more){
                Intent intent = getIntent();
                String nameUser, emailUser, userName, passwordUser;
                int budget, rewards;

                nameUser = intent.getStringExtra("name");
                emailUser = intent.getStringExtra("email");
                userName = intent.getStringExtra("username");
                passwordUser = intent.getStringExtra("password");
                rewards = intent.getIntExtra("rewards",0);
                budget = intent.getIntExtra("budget",0);

                Intent i = new Intent(TransactionActivity.this,ProfileActivity.class);
                i.putExtra("name", nameUser);
                i.putExtra("email", emailUser);
                i.putExtra("username", userName);
                i.putExtra("password", passwordUser);
                i.putExtra("budget",budget);
                i.putExtra("rewards",rewards);

                startActivity(i);
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