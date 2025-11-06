package ht.nguyenhuutrong.fe_moneytrack_bot.models;

public class RegisterRequest {
    private String username;
    private String email; // THÊM DÒNG NÀY
    private String password;

    // Sửa lại constructor
    public RegisterRequest(String username, String email, String password) {
        this.username = username;
        this.email = email; // THÊM DÒNG NÀY
        this.password = password;
    }

    // (getter/setter nếu cần)
}