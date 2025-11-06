package ht.nguyenhuutrong.fe_moneytrack_bot.api;

import android.content.Context; // 1. Cần Context

import java.io.IOException; // 2. Cần cho Interceptor

import ht.nguyenhuutrong.fe_moneytrack_bot.api.TokenManager; // 3. Cần TokenManager
import okhttp3.Interceptor; // 4. Import Interceptor
import okhttp3.OkHttpClient; // 5. Import OkHttp
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "http://10.0.2.2:8000/";

    // Chúng ta không dùng static Retrofit nữa vì client cần Context
    // private static Retrofit retrofit = null;

    /**
     * Hàm khởi tạo Retrofit (ĐÃ SỬA ĐỔI)
     * Giờ đây nó nhận Context để tạo TokenManager
     */
    public static Retrofit getClient(Context context) {

        // 1. Khởi tạo TokenManager
        // Dùng applicationContext để tránh rò rỉ bộ nhớ (memory leak)
        final TokenManager tokenManager = new TokenManager(context.getApplicationContext());

        // 2. Tạo một OkHttpClient
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        // 3. Tạo một Interceptor (Đây là phần quan trọng nhất)
        httpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                // Lấy request gốc
                Request originalRequest = chain.request();

                // Lấy token từ TokenManager
                String token = tokenManager.getToken();

                // Nếu đã có token
                if (token != null && !token.isEmpty()) {
                    // Tạo request mới và thêm "Authorization" header
                    Request.Builder requestBuilder = originalRequest.newBuilder()
                            .header("Authorization", "Bearer " + token)
                            .method(originalRequest.method(), originalRequest.body());

                    Request newRequest = requestBuilder.build();
                    return chain.proceed(newRequest);
                }

                // Nếu không có token (ví dụ: đang gọi API Login/Register)
                // thì cho request đi tiếp
                return chain.proceed(originalRequest);
            }
        });

        // 4. Xây dựng Retrofit với OkHttpClient đã thêm Interceptor
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(httpClient.build()) // Gắn OkHttpClient vào Retrofit
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    /**
     * Tiện ích để lấy sẵn ApiService (ĐÃ SỬA ĐỔI)
     * Phải truyền context vào
     */
    public static ApiService getApiService(Context context) {
        return getClient(context).create(ApiService.class);
    }
}