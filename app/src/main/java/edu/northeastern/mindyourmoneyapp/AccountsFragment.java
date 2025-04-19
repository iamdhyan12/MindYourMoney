package edu.northeastern.mindyourmoneyapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AccountsFragment extends Fragment {

    private RecyclerView recyclerView;
    private AccountsAdapter adapter;
    private ArrayList<Account> accountList = new ArrayList<>();
    private DatabaseReference userRef;
    private String username;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_accounts, container, false);
        recyclerView = view.findViewById(R.id.accountsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        SharedPreferences prefs = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        username = prefs.getString("username", null);
        userRef = FirebaseDatabase.getInstance().getReference("users").child(username).child("accounts");

        adapter = new AccountsAdapter(getContext(), accountList, account -> {});
        recyclerView.setAdapter(adapter);
        DividerItemDecoration divider = new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(divider);

        loadAccounts();

        view.findViewById(R.id.addAccountFab).setOnClickListener(v -> showAddAccountDialog());
        return view;
    }

    private void loadAccounts() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                accountList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Account account = ds.getValue(Account.class);
                    if (account != null) accountList.add(account);
                }
                adapter.notifyDataSetChanged();
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showAddAccountDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_account, null);
        EditText input = dialogView.findViewById(R.id.accountNameEditText);
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        dialog.show();

        dialogView.findViewById(R.id.cancelButton).setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.addButton).setOnClickListener(v -> {
            String newAccount = input.getText().toString().trim();
            if (!newAccount.isEmpty()) {
                userRef.child(newAccount).setValue(new Account(newAccount));
                loadAccounts();
            }
            dialog.dismiss();
        });
    }


}