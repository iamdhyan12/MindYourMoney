package edu.northeastern.mindyourmoney;

import java.util.ArrayList;

public class Constants {
    public static String INCOME = "INCOME";
    public static String EXPENSE = "EXPENSE";

    public static ArrayList<Category> categories;

    public static int DAILY = 0;
    public static int MONTHLY = 1;
    public static int CALENDAR = 2;
    public static int SUMMARY = 3;
    public static int NOTES = 4;

    public static int SELECTED_TAB = 0;
    public static int SELECTED_TAB_STATS = 0;
    public static String SELECTED_STATS_TYPE = Constants.INCOME;

    public static void setCategories() {
        categories = new ArrayList<>();
        categories.add(new Category("Groceries", R.drawable.ic_accounts, R.color.category1));
        categories.add(new Category("Fuel", R.drawable.ic_accounts, R.color.category2));
        categories.add(new Category("Business", R.drawable.ic_accounts, R.color.category3));
        categories.add(new Category("Dining", R.drawable.ic_accounts, R.color.category4));
        categories.add(new Category("Utilities", R.drawable.ic_accounts, R.color.category5));
        categories.add(new Category("General", R.drawable.ic_accounts, R.color.category6));
    }

    public static Category getCategoryDetails(String categoryName) {
        for (Category cat : categories) {
            if (cat.getCategoryName().equals(categoryName)) {
                return cat;
            }
        }
        return null;
    }

    public static int getAccountsColor(String accountName) {
        switch (accountName) {
            case "Bank":
                return R.color.category1;
            case "Cash":
                return R.color.category2;
            case "Card":
                return R.color.category3;
            default:
                return R.color.category4;
        }
    }

}