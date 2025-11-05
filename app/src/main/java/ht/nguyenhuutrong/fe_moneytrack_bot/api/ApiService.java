package ht.nguyenhuutrong.fe_moneytrack_bot.api;

import java.util.List;

import ht.nguyenhuutrong.fe_moneytrack_bot.models.Category;
import ht.nguyenhuutrong.fe_moneytrack_bot.models.LoginRequest;
import ht.nguyenhuutrong.fe_moneytrack_bot.models.LoginResponse;
import ht.nguyenhuutrong.fe_moneytrack_bot.models.RegisterRequest;
import ht.nguyenhuutrong.fe_moneytrack_bot.models.Transaction;
import ht.nguyenhuutrong.fe_moneytrack_bot.models.Wallet;
import ht.nguyenhuutrong.fe_moneytrack_bot.models.ReportEntry;
import ht.nguyenhuutrong.fe_moneytrack_bot.models.Budget;
import ht.nguyenhuutrong.fe_moneytrack_bot.models.CashFlowEntry;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // ==========================================================
    // 🧑 USER (Đăng ký / Đăng nhập)
    // ==========================================================

    @POST("api/users/")
    Call<Void> registerUser(@Body RegisterRequest registerRequest);

    @POST("api/token/")
    Call<LoginResponse> loginUser(@Body LoginRequest loginRequest);


    // ==========================================================
    // 💸 TRANSACTIONS
    // ==========================================================

    @GET("api/transactions/")
    Call<List<Transaction>> getTransactions(
            @Header("Authorization") String authToken
    );

    @FormUrlEncoded
    @POST("api/transactions/")
    Call<Transaction> createTransaction(
            @Header("Authorization") String authToken,
            @Field("amount") double amount,
            @Field("description") String description,
            @Field("date") String date,
            @Field("category") int categoryId,
            @Field("wallet") int walletId
    );

    @GET("api/transactions/{id}/")
    Call<Transaction> getTransactionDetails(
            @Header("Authorization") String authToken,
            @Path("id") int transactionId
    );

    @FormUrlEncoded
    @PUT("api/transactions/{id}/")
    Call<Transaction> updateTransaction(
            @Header("Authorization") String authToken,
            @Path("id") int transactionId,
            @Field("amount") double amount,
            @Field("description") String description,
            @Field("date") String date,
            @Field("category") int categoryId,
            @Field("wallet") int walletId
    );

    @DELETE("api/transactions/{id}/")
    Call<Void> deleteTransaction(
            @Header("Authorization") String authToken,
            @Path("id") int transactionId
    );


    // ==========================================================
    // 🏷️ CATEGORIES
    // ==========================================================

    @GET("api/categories/")
    Call<List<Category>> getCategories(
            @Header("Authorization") String authToken
    );

    @FormUrlEncoded
    @POST("api/categories/")
    Call<Category> createCategory(
            @Header("Authorization") String authToken,
            @Field("name") String name,
            @Field("type") String type // "income" hoặc "expense"
    );


    // ==========================================================
    // 💰 WALLETS
    // ==========================================================

    @GET("api/wallets/")
    Call<List<Wallet>> getWallets(
            @Header("Authorization") String authToken
    );

    @FormUrlEncoded
    @POST("api/wallets/")
    Call<Wallet> createWallet(
            @Header("Authorization") String authToken,
            @Field("name") String name,
            @Field("balance") double balance
    );


    // ==========================================================
    // 🔁 TRANSFER (Chuyển tiền giữa 2 ví)
    // ==========================================================

    @FormUrlEncoded
    @POST("api/transfer/")
    Call<Void> transferFunds(
            @Header("Authorization") String authToken,
            @Field("from_wallet_id") int fromWalletId,
            @Field("to_wallet_id") int toWalletId,
            @Field("amount") double amount,
            @Field("date") String date, // "YYYY-MM-DD"
            @Field("description") String description
    );


    // ==========================================================
    // 📊 REPORT (Tổng hợp chi tiêu theo danh mục)
    // ==========================================================

    @GET("api/reports/summary/")
    Call<List<ReportEntry>> getReportSummary(
            @Header("Authorization") String authToken,
            @Query("start_date") String startDate,
            @Query("end_date") String endDate
    );

    // --- THÊM HÀM MỚI CHO NGÂN SÁCH ---

    // (1) Lấy danh sách ngân sách (cho tháng/năm)
    @GET("api/budgets/")
    Call<List<Budget>> getBudgets(
            @Header("Authorization") String authToken,
            @Query("month") int month,
            @Query("year") int year
    );

    // (2) Tạo một ngân sách mới
    @FormUrlEncoded
    @POST("api/budgets/")
    Call<Budget> createBudget(
            @Header("Authorization") String authToken,
            @Field("category") int categoryId,
            @Field("amount") double amount,
            @Field("month") int month,
            @Field("year") int year
    );

    @GET("api/reports/cashflow/")
    Call<List<CashFlowEntry>> getCashFlowReport(
            @Header("Authorization") String authToken,
            @Query("start_date") String startDate,
            @Query("end_date") String endDate
    );
}
