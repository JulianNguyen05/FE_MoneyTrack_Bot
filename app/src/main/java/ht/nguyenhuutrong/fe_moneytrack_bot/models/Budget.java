package ht.nguyenhuutrong.fe_moneytrack_bot.models;

import com.google.gson.annotations.SerializedName;

public class Budget {

    @SerializedName("id")
    private int id;

    @SerializedName("amount")
    private double amount; // Hạn mức

    @SerializedName("month")
    private int month;

    @SerializedName("year")
    private int year;

    // Đây là đối tượng Category lồng bên trong
    @SerializedName("category_details")
    private Category categoryDetails;

    // Getters
    public int getId() { return id; }
    public double getAmount() { return amount; }
    public int getMonth() { return month; }
    public int getYear() { return year; }
    public Category getCategoryDetails() { return categoryDetails; }
}