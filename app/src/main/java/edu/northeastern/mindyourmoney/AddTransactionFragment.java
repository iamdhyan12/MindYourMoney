package edu.northeastern.mindyourmoney;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import edu.northeastern.mindyourmoney.databinding.FragmentAddTransactionBinding;
import edu.northeastern.mindyourmoney.databinding.ListDialogBinding;

public class AddTransactionFragment extends BottomSheetDialogFragment {

    FragmentAddTransactionBinding binding;

    private DatabaseReference mindYourMoneyRef;

    String monthYear,year;

    public AddTransactionFragment() {

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddTransactionBinding.inflate(inflater);
        binding.date.setOnClickListener(view -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext());
            datePickerDialog.setOnDateSetListener((datePicker, i, i1, i2) -> {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
                calendar.set(Calendar.MONTH, datePicker.getMonth());
                calendar.set(Calendar.YEAR, datePicker.getYear());

                SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
                SimpleDateFormat dateFormatMonthYear = new SimpleDateFormat("MMMM, YYYY");
                monthYear = dateFormatMonthYear.format(calendar.getTime());
                SimpleDateFormat Year = new SimpleDateFormat("YYYY");
                year = Year.format(calendar.getTime());
                binding.date.setText(dateFormat.format(calendar.getTime()));

            });
            datePickerDialog.show();
        });

        binding.category.setOnClickListener(c-> {
            ListDialogBinding dialogBinding = ListDialogBinding.inflate(inflater);
            AlertDialog categoryDialog = new AlertDialog.Builder(getContext()).create();
            categoryDialog.setView(dialogBinding.getRoot());

            ArrayList<Category> categories = new ArrayList<>();
            categories.add(new Category("Groceries",R.drawable.ic_accounts,R.color.category1));
            categories.add(new Category("Fuel",R.drawable.ic_accounts,R.color.category2));
            categories.add(new Category("Business",R.drawable.ic_accounts,R.color.category3));
            categories.add(new Category("Dining",R.drawable.ic_accounts,R.color.category4));
            categories.add(new Category("Utilities",R.drawable.ic_accounts,R.color.category5));
            categories.add(new Category("General",R.drawable.ic_accounts,R.color.category6));

            CategoryAdapter categoryAdapter = new CategoryAdapter(getContext(), categories, new CategoryAdapter.CategoryClickListener() {
                @Override
                public void onCategoryClicked(Category category) {

                    binding.category.setText(category.getCategoryName());
                    categoryDialog.dismiss();
                }
            });
            dialogBinding.recyclerView.setLayoutManager(new GridLayoutManager(getContext(),3));
            dialogBinding.recyclerView.setAdapter(categoryAdapter);

            categoryDialog.show();
        });

        binding.account.setOnClickListener(c-> {
            ListDialogBinding dialogBinding = ListDialogBinding.inflate(inflater);
            AlertDialog accountsDialog = new AlertDialog.Builder(getContext()).create();
            accountsDialog.setView(dialogBinding.getRoot());

            ArrayList<Account> accounts = new ArrayList<>();
            accounts.add(new Account(0, "Cash"));
            accounts.add(new Account(0, "Credit Card"));
            accounts.add(new Account(0, "Debit Card"));
            accounts.add(new Account(0, "PayPal"));
            accounts.add(new Account(0, "Other"));

            AccountsAdapter adapter = new AccountsAdapter(getContext(), accounts, new AccountsAdapter.AccountsClickListener() {
                @Override
                public void onAccountSelected(Account account) {
                    binding.account.setText(account.getAccountName());
                    accountsDialog.dismiss();
                }
            });
            dialogBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            dialogBinding.recyclerView.setAdapter(adapter);

            accountsDialog.show();

        });

        binding.saveTransactionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String category = binding.category.getText().toString().trim();
                String account = binding.account.getText().toString().trim();
                String note = binding.note.getText().toString().trim();
                String date = binding.date.getText().toString().trim();
                String amountStr = binding.amount.getText().toString().trim();
                if (category.isEmpty() || account.isEmpty() || date.isEmpty() || amountStr.isEmpty()) {
                    Toast.makeText(getContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                double amount;
                try {
                    amount = Double.parseDouble(amountStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Invalid amount", Toast.LENGTH_SHORT).show();
                    return;
                }

                mindYourMoneyRef = FirebaseDatabase.getInstance().getReference("transactionHistory");

                String transactionId = mindYourMoneyRef.push().getKey();

                Transaction transaction = new Transaction(transactionId, category, account,
                        note, date, monthYear, year, amount);

                Log.println(Log.INFO,"Trasaction",transaction.toString());

                mindYourMoneyRef.child(transactionId).setValue(transaction);

                // Show success message
                Toast.makeText(getContext(), "Transaction added successfully!", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                getActivity().finish();

            }
        });

        return binding.getRoot();
    }
}