package edu.northeastern.mindyourmoney;

import java.util.Date;


public class Transaction {
    private String  category, account, note;
    private Date date;
    private double amount;


    public Transaction() {
    }

    public Transaction(String category, String account, String note, Date date, double amount, long id) {
        this.category = category;
        this.account = account;
        this.note = note;
        this.date = date;
        this.amount = amount;
    }
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}