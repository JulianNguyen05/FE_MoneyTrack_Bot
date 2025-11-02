package ht.nguyenhuutrong.fe_moneytrack_bot.api;

import java.util.List;

import ht.nguyenhuutrong.fe_moneytrack_bot.models.LoginRequest;
import ht.nguyenhuutrong.fe_moneytrack_bot.models.LoginResponse;
import ht.nguyenhuutrong.fe_moneytrack_bot.models.RegisterRequest;
import ht.nguyenhuutrong.fe_moneytrack_bot.models.Transaction;
import retrofit2.Call;
import retrofit2.http.Body;
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

    // (Tuỳ chọn) Thêm giao dịch mới
    // @POST("api/transactions/")
    // Call<Transaction> createTransaction(@Header("Authorization") String authToken, @Body Transaction transaction);
}
