package edu.northeastern.mindyourmoney;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import edu.northeastern.mindyourmoney.databinding.FragmentAddTransactionBinding;
import edu.northeastern.mindyourmoney.databinding.ListDialogBinding;

public class AddTransactionFragment extends BottomSheetDialogFragment {

    FragmentAddTransactionBinding binding;

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
        binding.date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext());
                datePickerDialog.setOnDateSetListener((datePicker, i, i1, i2) -> {
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
                    calendar.set(Calendar.MONTH, datePicker.getMonth());
                    calendar.set(Calendar.YEAR, datePicker.getYear());

                    SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
//                    String dateToShow = Helper.formatDate(calendar.getTime());

                    binding.date.setText(dateFormat.format(calendar.getTime()));
//
//                    transaction.setDate(calendar.getTime());
//                    transaction.setId(calendar.getTime().getTime());
                });
                datePickerDialog.show();
            }
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
        return binding.getRoot();
    }
}