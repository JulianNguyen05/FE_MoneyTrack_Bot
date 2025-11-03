package ht.nguyenhuutrong.fe_moneytrack_bot.api;

import java.util.List;

import ht.nguyenhuutrong.fe_moneytrack_bot.models.Category;
import ht.nguyenhuutrong.fe_moneytrack_bot.models.LoginRequest;
import ht.nguyenhuutrong.fe_moneytrack_bot.models.LoginResponse;
import ht.nguyenhuutrong.fe_moneytrack_bot.models.RegisterRequest;
import ht.nguyenhuutrong.fe_moneytrack_bot.models.Transaction;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {

    // Đăng ký người dùng mới
    @POST("api/users/")
    Call<Void> registerUser(@Body RegisterRequest registerRequest);

    // Đăng nhập người dùng -> nhận token
    @POST("api/token/")
    Call<LoginResponse> loginUser(@Body LoginRequest loginRequest);

    // Lấy danh sách giao dịch (yêu cầu token xác thực)
    @GET("api/transactions/")
    Call<List<Transaction>> getTransactions(@Header("Authorization") String authToken);

    // Lấy danh sách danh mục (categories)
    @GET("api/categories/")
    Call<List<Category>> getCategories(@Header("Authorization") String authToken);

    // Tạo một category mới (gửi form có 2 trường 'name' và 'type')
    @FormUrlEncoded
    @POST("api/categories/")
    Call<Category> createCategory(
            @Header("Authorization") String authToken,
            @Field("name") String name,
            @Field("type") String type // 'income' (thu) hoặc 'expense' (chi)
    );

    // (Tùy chọn) Thêm giao dịch mới
    // @POST("api/transactions/")
    // Call<Transaction> createTransaction(@Header("Authorization") String authToken, @Body Transaction transaction);
}
