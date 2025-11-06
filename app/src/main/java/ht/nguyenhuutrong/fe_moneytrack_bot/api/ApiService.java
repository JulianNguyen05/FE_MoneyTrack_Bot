package ht.nguyenhuutrong.fe_moneytrack_bot.api;

import java.util.List;

// Import các model của bạn
import ht.nguyenhuutrong.fe_moneytrack_bot.models.Category;
import ht.nguyenhuutrong.fe_moneytrack_bot.models.LoginRequest;
import ht.nguyenhuutrong.fe_moneytrack_bot.models.LoginResponse;
import ht.nguyenhuutrong.fe_moneytrack_bot.models.RegisterRequest;
import ht.nguyenhuutrong.fe_moneytrack_bot.models.Transaction;
import ht.nguyenhuutrong.fe_moneytrack_bot.models.Wallet;
import ht.nguyenhuutrong.fe_moneytrack_bot.models.ReportEntry;
import ht.nguyenhuutrong.fe_moneytrack_bot.models.Budget;
import ht.nguyenhuutrong.fe_moneytrack_bot.models.CashFlowEntry;
import ht.nguyenhuutrong.fe_moneytrack_bot.models.User; // Cần import model User
// (Import các model chatbot mới ở dưới)
import ht.nguyenhuutrong.fe_moneytrack_bot.models.ChatbotRequest;
import ht.nguyenhuutrong.fe_moneytrack_bot.models.ChatbotResponse;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.FormUrlEncoded; // Vẫn giữ cho API Transfer
import retrofit2.http.Field;       // Vẫn giữ cho API Transfer

public interface ApiService {

    // ==========================================================
    // 🧑 USER (Đăng ký / Đăng nhập)
    // ==========================================================

    // SỬA LẠI: URL là "api/register/" và trả về "User"
    @POST("api/register/")
    Call<User> registerUser(@Body RegisterRequest registerRequest);

    @POST("api/token/")
    Call<LoginResponse> loginUser(@Body LoginRequest loginRequest);


    // ==========================================================
    // 💸 TRANSACTIONS
    // ==========================================================

    @GET("api/transactions/")
    Call<List<Transaction>> getTransactions(
            @Header("Authorization") String authToken,
            @Query("search") String searchTerm
    );

    // SỬA LẠI: Dùng @Body thay vì @FormUrlEncoded
    @POST("api/transactions/")
    Call<Transaction> createTransaction(
            @Header("Authorization") String authToken,
            @Body Transaction transaction // Gửi cả object Transaction (hoặc TransactionRequest)
    );

    @GET("api/transactions/{id}/")
    Call<Transaction> getTransactionDetails(
            @Header("Authorization") String authToken,
            @Path("id") int transactionId
    );

    // SỬA LẠI: Dùng @Body thay vì @FormUrlEncoded
    @PUT("api/transactions/{id}/")
    Call<Transaction> updateTransaction(
            @Header("Authorization") String authToken,
            @Path("id") int transactionId,
            @Body Transaction transaction // Gửi cả object Transaction
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

    // SỬA LẠI: Dùng @Body
    @POST("api/categories/")
    Call<Category> createCategory(
            @Header("Authorization") String authToken,
            @Body Category category
    );


    // ==========================================================
    // 💰 WALLETS
    // ==========================================================

    @GET("api/wallets/")
    Call<List<Wallet>> getWallets(
            @Header("Authorization") String authToken
    );

    // SỬA LẠI: Dùng @Body
    @POST("api/wallets/")
    Call<Wallet> createWallet(
            @Header("Authorization") String authToken,
            @Body Wallet wallet
    );


    // ==========================================================
    // 🔁 TRANSFER (Chuyển tiền giữa 2 ví)
    // ==========================================================

    // Giữ nguyên @FormUrlEncoded vì đây là custom view
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
    // 📊 REPORT & BUDGET
    // ==========================================================

    @GET("api/reports/summary/")
    Call<List<ReportEntry>> getReportSummary(
            @Header("Authorization") String authToken,
            @Query("start_date") String startDate,
            @Query("end_date") String endDate
    );

    @GET("api/budgets/")
    Call<List<Budget>> getBudgets(
            @Header("Authorization") String authToken,
            @Query("month") int month,
            @Query("year") int year
    );

    // SỬA LẠI: Dùng @Body
    @POST("api/budgets/")
    Call<Budget> createBudget(
            @Header("Authorization") String authToken,
            @Body Budget budget
    );

    @GET("api/reports/cashflow/")
    Call<List<CashFlowEntry>> getCashFlowReport(
            @Header("Authorization") String authToken,
            @Query("start_date") String startDate,
            @Query("end_date") String endDate
    );


    // ==========================================================
    // 💬 CHATBOT (API BỊ THIẾU)
    // ==========================================================

    @POST("api/chatbot/")
    Call<ChatbotResponse> postChatbotMessage(
            @Header("Authorization") String authToken,
            @Body ChatbotRequest request
    );
}