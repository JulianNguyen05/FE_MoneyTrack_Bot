package ht.nguyenhuutrong.fe_moneytrack_bot.models;

import com.google.gson.annotations.SerializedName;

public class Budget {

    // --- (A) Dùng để NHẬN dữ liệu (Get) ---
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

    // --- (B) Dùng để GỬI dữ liệu (Create/Post) ---
    // API của bạn cần một trường "category" (là ID) khi tạo mới
    @SerializedName("category")
    private int category;
    // (Lưu ý: Gson sẽ bỏ qua trường này nếu nó không có trong JSON trả về,
    // và chỉ gửi nó đi khi chúng ta gọi hàm setCategory)

    // --- (C) Getters (Giữ nguyên) ---
    public int getId() { return id; }
    public double getAmount() { return amount; }
    public int getMonth() { return month; }
    public int getYear() { return year; }
    public Category getCategoryDetails() { return categoryDetails; }

    // --- (D) Setters (BẮT BUỘC PHẢI THÊM VÀO) ---
    // Đây là các hàm mà BudgetActivity đang gọi

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public void setYear(int year) {
        this.year = year;
    }

    /**
     * Dùng để gán ID của Category khi TẠO MỚI một Budget
     * @param categoryId ID của danh mục
     */
    public void setCategory(int categoryId) {
        this.category = categoryId;
    }
}