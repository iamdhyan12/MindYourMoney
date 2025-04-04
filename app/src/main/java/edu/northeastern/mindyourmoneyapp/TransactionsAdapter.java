package edu.northeastern.mindyourmoneyapp;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import edu.northeastern.mindyourmoneyapp.databinding.RowTransactionBinding;


public class TransactionsAdapter  extends  RecyclerView.Adapter<TransactionsAdapter.TransactionViewHolder> {


    Context context;
    RowTransactionBinding binding;
    ArrayList<Transaction> transactions;

    private OnTransactionDeletedListener transactionDeletedListener;

    private DatabaseReference mindYourMoneyRef;
    public TransactionsAdapter(Context context, ArrayList<Transaction> transactions, OnTransactionDeletedListener transactionDeletedListener) {
        this.context = context;
        this.transactions = transactions;
        this.transactionDeletedListener = transactionDeletedListener;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TransactionViewHolder(LayoutInflater.from(context).inflate(R.layout.row_transaction, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);

        holder.binding.transactionAmount.setText(String.valueOf(transaction.getAmount()));
        holder.binding.transactionCategory.setText(transaction.getAccount());
        SimpleDateFormat format = new SimpleDateFormat("MMMM dd, YYYY");
        holder.binding.transactionDate.setText(transaction.getDate());
        holder.binding.category.setText(transaction.getCategory());

        Category transactionCategory = Constants.getCategoryDetails(transaction.getCategory());

        holder.binding.categoryIcon.setImageResource(transactionCategory.getCategoryImage());
        holder.binding.categoryIcon.setBackgroundTintList(context.getColorStateList(transactionCategory.getCategoryColor()));

        holder.binding.transactionCategory.setBackgroundTintList(context.getColorStateList(Constants.getAccountsColor(transaction.getAccount())));


        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog deleteDialog = new AlertDialog.Builder(context).create();
                deleteDialog.setTitle("Delete Transaction");
                deleteDialog.setMessage("Are you sure to delete this transaction?");
                deleteDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", (dialogInterface, i) -> {
                    deleteTransaction(transaction);

                });
                deleteDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "No", (dialogInterface, i) -> {
                    deleteDialog.dismiss();
                });
                deleteDialog.show();
                return false;
            }
        });

    }


    public void deleteTransaction(Transaction transaction){
        mindYourMoneyRef = FirebaseDatabase.getInstance().getReference("transactionHistory");
        mindYourMoneyRef.child(transaction.getId()).removeValue();
        transactionDeletedListener.onTransactionDeleted();
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public class TransactionViewHolder extends RecyclerView.ViewHolder {

        RowTransactionBinding binding;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = RowTransactionBinding.bind(itemView);
        }
    }


    public interface OnTransactionDeletedListener {
        void onTransactionDeleted();
    }
}