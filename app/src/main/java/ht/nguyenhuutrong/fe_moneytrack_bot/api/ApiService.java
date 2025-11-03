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
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {

    // ------------------- USER -------------------

    // Đăng ký tài khoản
    @POST("api/users/")
    Call<Void> registerUser(@Body RegisterRequest registerRequest);

    // Đăng nhập, lấy token
    @POST("api/token/")
    Call<LoginResponse> loginUser(@Body LoginRequest loginRequest);

    // ------------------- TRANSACTIONS -------------------

    // Lấy danh sách giao dịch
    @GET("api/transactions/")
    Call<List<Transaction>> getTransactions(@Header("Authorization") String authToken);

    // Thêm giao dịch mới (nếu backend hỗ trợ POST)
    // @FormUrlEncoded
    // @POST("api/transactions/")
    // Call<Transaction> createTransaction(
    //     @Header("Authorization") String authToken,
    //     @Field("amount") double amount,
    //     @Field("description") String description,
    //     @Field("date") String date,
    //     @Field("category") int categoryId,
    //     @Field("wallet") int walletId
    // );

    // Lấy chi tiết giao dịch
    @GET("api/transactions/{id}/")
    Call<Transaction> getTransactionDetails(
            @Header("Authorization") String authToken,
            @Path("id") int transactionId
    );

    // Cập nhật giao dịch
    @FormUrlEncoded
    @PUT("api/transactions/{id}/")
    Call<Transaction> updateTransaction(
            @Header("Authorization") String authToken,
            @Path("id") int transactionId,
            @Field("amount") double amount,
            @Field("description") String description,
            @Field("date") String date, // "YYYY-MM-DD"
            @Field("category") int categoryId,
            @Field("wallet") int walletId
    );

    // Xóa giao dịch
    @DELETE("api/transactions/{id}/")
    Call<Void> deleteTransaction(
            @Header("Authorization") String authToken,
            @Path("id") int transactionId
    );

    // ------------------- CATEGORIES -------------------

    // Lấy danh sách danh mục
    @GET("api/categories/")
    Call<List<Category>> getCategories(@Header("Authorization") String authToken);

    // Tạo danh mục mới
    @FormUrlEncoded
    @POST("api/categories/")
    Call<Category> createCategory(
            @Header("Authorization") String authToken,
            @Field("name") String name,
            @Field("type") String type // "income" hoặc "expense"
    );

    // ------------------- WALLETS -------------------

    // Lấy danh sách ví
    @GET("api/wallets/")
    Call<List<Wallet>> getWallets(@Header("Authorization") String authToken);

    // Tạo ví mới
    @FormUrlEncoded
    @POST("api/wallets/")
    Call<Wallet> createWallet(
            @Header("Authorization") String authToken,
            @Field("name") String name,
            @Field("balance") double balance
    );
}
