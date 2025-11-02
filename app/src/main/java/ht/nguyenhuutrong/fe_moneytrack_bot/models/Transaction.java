package ht.nguyenhuutrong.fe_moneytrack_bot.models;

import com.google.gson.annotations.SerializedName;

public class Transaction {

    @SerializedName("id")
    private int id;

    @SerializedName("amount")
    private double amount;

    @SerializedName("description")
    private String description;

    // API trả về ngày dạng "YYYY-MM-DD"
    @SerializedName("date")
    private String date;

    // Tên danh mục hiển thị trong list
    @SerializedName("category_name")
    private String categoryName;

    // ✅ Constructor rỗng để Gson có thể parse JSON
    public Transaction() {}

    // ✅ Constructor đầy đủ (dùng nếu tạo transaction thủ công)
    public Transaction(int id, double amount, String description, String date, String categoryName) {
        this.id = id;
        this.amount = amount;
        this.description = description;
        this.date = date;
        this.categoryName = categoryName;
    }

    // ✅ Getters & Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
