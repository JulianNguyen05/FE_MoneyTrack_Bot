package ht.nguyenhuutrong.fe_moneytrack_bot.models;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {

    @SerializedName("access")
    private String access;

    @SerializedName("refresh")
    private String refresh;

    // ✅ Bắt buộc cần constructor rỗng để Gson parse dữ liệu JSON từ server
    public LoginResponse() {
    }

    public String getAccess() {
        return access;
    }

    public void setAccess(String access) {
        this.access = access;
    }

    public String getRefresh() {
        return refresh;
    }

    public void setRefresh(String refresh) {
        this.refresh = refresh;
    }
}
