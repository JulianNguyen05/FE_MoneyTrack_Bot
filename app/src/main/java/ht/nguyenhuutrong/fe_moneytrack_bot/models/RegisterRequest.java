package ht.nguyenhuutrong.fe_moneytrack_bot.models;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest {

    @SerializedName("username")
    private String username;

    @SerializedName("password")
    private String password;

    // ✅ Bắt buộc phải có constructor rỗng để Gson parse dữ liệu dễ dàng
    public RegisterRequest() {
    }

    // ✅ Constructor dùng khi gửi yêu cầu đăng ký
    public RegisterRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // ✅ Getter & Setter để Retrofit và Gson truy cập dữ liệu
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
