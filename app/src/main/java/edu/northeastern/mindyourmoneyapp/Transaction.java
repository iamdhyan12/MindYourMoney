package edu.northeastern.mindyourmoneyapp;


public class Transaction {

    private String id;

    private String  category, account, note, imageUrl;
    private String date;
    private double amount;

    private String monthYear, year;

    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Transaction(){

    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Transaction(String id, String category, String account, String note, String date,
                       String monthYear, String year, double amount, String imageUrl,String username) {
        this.id = id;
        this.category = category;
        this.account = account;
        this.note = note;
        this.date = date;
        this.monthYear = monthYear;
        this.year = year;
        this.amount = amount;
        this.imageUrl = imageUrl;
        this.username = username;
    }

    public String getId() {
        return id;
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

    public String getDate() {
        return date;
    }

    public String getMonthYear() {
        return monthYear;
    }

    public String getYear() {
        return year;
    }

    public double getAmount() {
        return amount;
    }


    @Override
    public String toString() {
        return "Transaction{" +
                "id='" + id + '\'' +
                ", category='" + category + '\'' +
                ", account='" + account + '\'' +
                ", note='" + note + '\'' +
                ", date='" + date + '\'' +
                ", amount=" + amount +
                ", monthYear='" + monthYear + '\'' +
                ", year='" + year + '\'' +
                '}';
    }
}