package ht.nguyenhuutrong.fe_moneytrack_bot.models;

import com.google.gson.annotations.SerializedName;

public class ReportEntry {

    // 🏷️ Tên trường phải khớp với key trong JSON trả về từ API Django
    @SerializedName("category_name")   // Đổi từ "category__name" ➜ "category_name" cho chuẩn REST API (nếu bạn đã đổi ở backend)
    private String categoryName;

    @SerializedName("total_amount")
    private double totalAmount;

    // ✅ Constructor rỗng (cần cho Gson)
    public ReportEntry() {
    }

    // ✅ Constructor đầy đủ (nếu cần tạo thủ công)
    public ReportEntry(String categoryName, double totalAmount) {
        this.categoryName = categoryName;
        this.totalAmount = totalAmount;
    }

    // ✅ Getter & Setter
    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    // ✅ Gợi ý thêm: override toString() để dễ debug log
    @Override
    public String toString() {
        return "ReportEntry{" +
                "categoryName='" + categoryName + '\'' +
                ", totalAmount=" + totalAmount +
                '}';
    }
}
