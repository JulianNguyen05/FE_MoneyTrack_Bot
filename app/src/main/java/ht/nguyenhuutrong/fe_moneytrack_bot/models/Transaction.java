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

    // Lưu ID của Category
    @SerializedName("category")
    private int category;

    // Lưu ID của Wallet
    @SerializedName("wallet")
    private int wallet;

    // ✅ Constructor rỗng để Gson có thể parse JSON
    public Transaction() {}

    // ✅ Constructor đầy đủ (dùng nếu tạo transaction thủ công)
    public Transaction(int id, double amount, String description, String date, String categoryName, int category, int wallet) {
        this.id = id;
        this.amount = amount;
        this.description = description;
        this.date = date;
        this.categoryName = categoryName;
        this.category = category;
        this.wallet = wallet;
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

    // ✅ Getter & Setter cho Category
    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    // ✅ Getter & Setter cho Wallet
    public int getWallet() {
        return wallet;
    }

    public void setWallet(int wallet) {
        this.wallet = wallet;
    }
}
