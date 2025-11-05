package ht.nguyenhuutrong.fe_moneytrack_bot.models;
import com.google.gson.annotations.SerializedName;

public class CashFlowEntry {
    @SerializedName("day")
    private String day; // "YYYY-MM-DD"

    @SerializedName("total_income")
    private double totalIncome;

    @SerializedName("total_expense")
    private double totalExpense;

    // Getters...
    public String getDay() { return day; }
    public double getTotalIncome() { return totalIncome; }
    public double getTotalExpense() { return totalExpense; }
}