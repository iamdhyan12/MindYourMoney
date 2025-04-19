package edu.northeastern.mindyourmoneyapp;

import java.util.ArrayList;

public class Constants {

    public static ArrayList<Category> categories;

    public static void setCategories() {
        categories = new ArrayList<>();
        categories.add(new Category("Groceries",R.drawable.groceries,R.color.category1));
        categories.add(new Category("Fuel",R.drawable.gas_station,R.color.category2));
        categories.add(new Category("Business",R.drawable.business,R.color.category3));
        categories.add(new Category("Dining",R.drawable.dining,R.color.category4));
        categories.add(new Category("Utilities",R.drawable.utlities,R.color.category5));
        categories.add(new Category("General",R.drawable.general,R.color.category6));
    }

    public static Category getCategoryDetails(String categoryName) {
        for (Category cat : categories) {
            if (cat.getCategoryName().equals(categoryName)) {
                return cat;
            }
        }
        return null;
    }

}