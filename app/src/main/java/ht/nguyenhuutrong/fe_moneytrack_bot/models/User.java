package ht.nguyenhuutrong.fe_moneytrack_bot.models;

/**
 * Model này đại diện cho đối tượng User
 * mà server trả về sau khi đăng ký thành công.
 */
public class User {

    private int id;
    private String username;
    private String email;

    // --- Getters ---

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    // --- Setters (Không bắt buộc nhưng nên có) ---

    public void setId(int id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}