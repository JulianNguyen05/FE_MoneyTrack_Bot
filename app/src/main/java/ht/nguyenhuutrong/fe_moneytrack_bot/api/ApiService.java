package ht.nguyenhuutrong.fe_moneytrack_bot.api;

import java.util.List;

import ht.nguyenhuutrong.fe_moneytrack_bot.models.Category;
import ht.nguyenhuutrong.fe_moneytrack_bot.models.LoginRequest;
import ht.nguyenhuutrong.fe_moneytrack_bot.models.LoginResponse;
import ht.nguyenhuutrong.fe_moneytrack_bot.models.RegisterRequest;
import ht.nguyenhuutrong.fe_moneytrack_bot.models.Transaction;
import ht.nguyenhuutrong.fe_moneytrack_bot.models.Wallet;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {

    // ------------------- User -------------------

    @POST("api/users/")
    Call<Void> registerUser(@Body RegisterRequest registerRequest);

    @POST("api/token/")
    Call<LoginResponse> loginUser(@Body LoginRequest loginRequest);

    // ------------------- Transactions -------------------

    @GET("api/transactions/")
    Call<List<Transaction>> getTransactions(@Header("Authorization") String authToken);

    // (Tùy chọn) Thêm giao dịch mới
    // @POST("api/transactions/")
    // Call<Transaction> createTransaction(@Header("Authorization") String authToken, @Body Transaction transaction);

    // ------------------- Categories -------------------

    @GET("api/categories/")
    Call<List<Category>> getCategories(@Header("Authorization") String authToken);

    @FormUrlEncoded
    @POST("api/categories/")
    Call<Category> createCategory(
            @Header("Authorization") String authToken,
            @Field("name") String name,
            @Field("type") String type // 'income' hoặc 'expense'
    );

    // ------------------- Wallets -------------------

    @GET("api/wallets/")
    Call<List<Wallet>> getWallets(@Header("Authorization") String authToken);

    @FormUrlEncoded
    @POST("api/wallets/")
    Call<Wallet> createWallet(
            @Header("Authorization") String authToken,
            @Field("name") String name,
            @Field("balance") double balance
    );
}
