package ht.nguyenhuutrong.fe_moneytrack_bot.api;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {

    private static final String PREFS_NAME = "AuthPrefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";

    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    // ✅ Sử dụng Singleton để quản lý token toàn app
    private static TokenManager instance;

    public TokenManager(Context context) {
        sharedPreferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // Lấy instance duy nhất (singleton)
    public static synchronized TokenManager getInstance(Context context) {
        if (instance == null) {
            instance = new TokenManager(context);
        }
        return instance;
    }

    // Lưu token đăng nhập
    public void saveToken(String token) {
        editor.putString(KEY_ACCESS_TOKEN, token);
        editor.apply();
    }

    // Lấy token hiện tại
    public String getToken() {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null);
    }

    // Xóa token khi đăng xuất
    public void clearToken() {
        editor.remove(KEY_ACCESS_TOKEN);
        editor.apply();
    }
}
