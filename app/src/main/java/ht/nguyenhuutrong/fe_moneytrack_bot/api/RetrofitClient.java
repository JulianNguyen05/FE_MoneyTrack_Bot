package ht.nguyenhuutrong.fe_moneytrack_bot.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    // ⚙️ Nếu chạy trên Android Emulator: 10.0.2.2 trỏ về localhost của máy tính thật.
    // Nếu bạn chạy trên thiết bị thật, cần thay bằng IP của máy tính trong cùng mạng LAN.
    private static final String BASE_URL = "http://10.0.2.2:8000/";

    private static Retrofit retrofit = null;

    // Hàm khởi tạo Retrofit (Singleton pattern)
    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    // Tiện ích để lấy sẵn ApiService
    public static ApiService getApiService() {
        return getClient().create(ApiService.class);
    }
}
