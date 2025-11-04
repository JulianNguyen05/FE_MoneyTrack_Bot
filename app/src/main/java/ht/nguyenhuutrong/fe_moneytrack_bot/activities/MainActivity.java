package ht.nguyenhuutrong.fe_moneytrack_bot.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ht.nguyenhuutrong.fe_moneytrack_bot.R;
import ht.nguyenhuutrong.fe_moneytrack_bot.adapters.TransactionAdapter;
import ht.nguyenhuutrong.fe_moneytrack_bot.api.ApiService;
import ht.nguyenhuutrong.fe_moneytrack_bot.api.RetrofitClient;
import ht.nguyenhuutrong.fe_moneytrack_bot.api.TokenManager;
import ht.nguyenhuutrong.fe_moneytrack_bot.models.Transaction;
import ht.nguyenhuutrong.fe_moneytrack_bot.models.Wallet;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements TransactionAdapter.TransactionClickListener {

    private RecyclerView recyclerView;
    private TransactionAdapter adapter;
    private ApiService apiService;
    private TokenManager tokenManager;
    private String authToken;
    private TextView textViewTotalBalance;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tokenManager = new TokenManager(this);
        authToken = tokenManager.getToken();

        if (authToken == null || authToken.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập trước khi sử dụng!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        authToken = "Bearer " + authToken;

        // Ánh xạ view
        textViewTotalBalance = findViewById(R.id.textViewTotalBalance);
        recyclerView = findViewById(R.id.recyclerViewTransactions);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TransactionAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        apiService = RetrofitClient.getClient().create(ApiService.class);

        setupButtons();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData();
        loadTransactions();
    }

    // --- Cấu hình các nút ---
    private void setupButtons() {
        // Đăng xuất
        Button buttonLogout = findViewById(R.id.buttonLogout);
        buttonLogout.setOnClickListener(v -> {
            tokenManager.clearToken();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        // Danh mục
        Button buttonGoToCategories = findViewById(R.id.buttonGoToCategories);
        buttonGoToCategories.setOnClickListener(v ->
                startActivity(new Intent(this, CategoryActivity.class)));

        // Ví tiền
        Button buttonGoToWallets = findViewById(R.id.buttonGoToWallets);
        buttonGoToWallets.setOnClickListener(v ->
                startActivity(new Intent(this, WalletActivity.class)));

        // Chuyển tiền
        Button buttonGoToTransfer = findViewById(R.id.buttonGoToTransfer);
        if (buttonGoToTransfer != null) {
            buttonGoToTransfer.setOnClickListener(v ->
                    startActivity(new Intent(this, TransferActivity.class)));
        }

        // Báo cáo (NEW)
        Button buttonReport = findViewById(R.id.buttonReport);
        if (buttonReport != null) {
            buttonReport.setOnClickListener(v ->
                    startActivity(new Intent(this, ReportActivity.class)));
        }

        // Nút Floating (+)
        FloatingActionButton fabAdd = findViewById(R.id.fab_add_transaction);
        fabAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AddTransactionActivity.class)));
    }

    // --- Tải tổng số dư ---
    private void loadDashboardData() {
        apiService.getWallets(authToken).enqueue(new Callback<List<Wallet>>() {
            @Override
            public void onResponse(Call<List<Wallet>> call, Response<List<Wallet>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    double totalBalance = 0.0;
                    for (Wallet wallet : response.body()) {
                        totalBalance += wallet.getBalance();
                    }
                    NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
                    textViewTotalBalance.setText(formatter.format(totalBalance));
                } else {
                    Toast.makeText(MainActivity.this, "Không thể tải số dư", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Wallet>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Lỗi mạng (tải ví): " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API_FAILURE", t.getMessage(), t);
            }
        });
    }

    // --- Tải danh sách giao dịch ---
    private void loadTransactions() {
        apiService.getTransactions(authToken).enqueue(new Callback<List<Transaction>>() {
            @Override
            public void onResponse(Call<List<Transaction>> call, Response<List<Transaction>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setData(response.body());
                } else if (response.code() == 401) {
                    Toast.makeText(MainActivity.this, "Phiên đăng nhập hết hạn.", Toast.LENGTH_LONG).show();
                    tokenManager.clearToken();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Không thể tải giao dịch", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Transaction>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Lỗi mạng (tải giao dịch): " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API_FAILURE", t.getMessage(), t);
            }
        });
    }

    // --- Khi click "Sửa" ---
    @Override
    public void onEditClick(Transaction transaction) {
        Intent intent = new Intent(MainActivity.this, EditTransactionActivity.class);
        intent.putExtra("TRANSACTION_ID", transaction.getId());
        startActivity(intent);
    }

    // --- Khi click "Xóa" ---
    @Override
    public void onDeleteClick(Transaction transaction) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận Xóa")
                .setMessage("Bạn có chắc muốn xóa giao dịch này?\n(" + transaction.getCategoryName() + ": " + transaction.getAmount() + ")")
                .setPositiveButton("Xóa", (dialog, which) -> deleteTransaction(transaction.getId()))
                .setNegativeButton("Hủy", null)
                .show();
    }

    // --- API Xóa giao dịch ---
    private void deleteTransaction(int transactionId) {
        apiService.deleteTransaction(authToken, transactionId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Đã xóa giao dịch", Toast.LENGTH_SHORT).show();
                    loadDashboardData();
                    loadTransactions();
                } else {
                    Toast.makeText(MainActivity.this, "Xóa thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
