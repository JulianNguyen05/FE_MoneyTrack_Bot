package ht.nguyenhuutrong.fe_moneytrack_bot.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
// --- (1) THÊM IMPORT MỚI ---
import androidx.appcompat.widget.SearchView;
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

    private SearchView searchView; // <-- (2) THÊM BIẾN MỚI

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Đảm bảo layout có <SearchView android:id="@+id/searchView" />

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
        searchView = findViewById(R.id.searchView); // <-- (3) ÁNH XẠ SEARCHVIEW
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TransactionAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        apiService = RetrofitClient.getClient().create(ApiService.class);

        setupButtons();
        setupSearchListener(); // <-- (4) GỌI HÀM LẮNG NGHE MỚI
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData();

        // --- (5) SỬA LẠI onResume ---
        // Tải lại các giao dịch dựa trên nội dung đang có trong thanh tìm kiếm
        String currentSearch = searchView.getQuery().toString();
        loadTransactions(currentSearch.isEmpty() ? null : currentSearch);
    }

    // --- (6) HÀM MỚI: LẮNG NGHE SỰ KIỆN TÌM KIẾM ---
    private void setupSearchListener() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Khi người dùng nhấn Enter (hoặc nút tìm kiếm)
                loadTransactions(query);
                searchView.clearFocus(); // Ẩn bàn phím
                return true; // Đã xử lý
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Khi người dùng xóa (bấm nút "X"), newText sẽ rỗng
                if (newText != null && newText.isEmpty()) {
                    loadTransactions(null); // Tải lại toàn bộ danh sách
                }
                return false; // Để SearchView tự cập nhật text
            }
        });
    }

    // --- Cấu hình các nút ---
    private void setupButtons() {
        // (Tất cả code setupButtons của bạn giữ nguyên)
        Button buttonLogout = findViewById(R.id.buttonLogout);
        buttonLogout.setOnClickListener(v -> {
            tokenManager.clearToken();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        Button buttonGoToCategories = findViewById(R.id.buttonGoToCategories);
        buttonGoToCategories.setOnClickListener(v ->
                startActivity(new Intent(this, CategoryActivity.class)));

        Button buttonGoToWallets = findViewById(R.id.buttonGoToWallets);
        buttonGoToWallets.setOnClickListener(v ->
                startActivity(new Intent(this, WalletActivity.class)));

        Button buttonGoToTransfer = findViewById(R.id.buttonGoToTransfer);
        if (buttonGoToTransfer != null) {
            buttonGoToTransfer.setOnClickListener(v ->
                    startActivity(new Intent(this, TransferActivity.class)));
        }

        Button buttonReport = findViewById(R.id.buttonReport);
        if (buttonReport != null) {
            buttonReport.setOnClickListener(v ->
                    startActivity(new Intent(this, ReportActivity.class)));
        }

        Button buttonBudget = findViewById(R.id.buttonBudget);
        if (buttonBudget != null) {
            buttonBudget.setOnClickListener(v ->
                    startActivity(new Intent(this, BudgetActivity.class)));
        }

        FloatingActionButton fabAdd = findViewById(R.id.fab_add_transaction);
        fabAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AddTransactionActivity.class)));
    }

    // --- Tải tổng số dư ---
    private void loadDashboardData() {
        // (Hàm này giữ nguyên, không thay đổi)
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

    // --- (7) SỬA HÀM TẢI GIAO DỊCH ---
    // (Thêm tham số @Nullable String searchTerm)
    private void loadTransactions(@Nullable String searchTerm) {
        // Gọi API với tham số tìm kiếm (có thể là null)
        apiService.getTransactions(authToken, searchTerm).enqueue(new Callback<List<Transaction>>() {
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
        // (Hàm này giữ nguyên)
        Intent intent = new Intent(MainActivity.this, EditTransactionActivity.class);
        intent.putExtra("TRANSACTION_ID", transaction.getId());
        startActivity(intent);
    }

    // --- Khi click "Xóa" ---
    @Override
    public void onDeleteClick(Transaction transaction) {
        // (Hàm này giữ nguyên)
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

                    // --- (8) SỬA LẠI SAU KHI XÓA ---
                    // Tải lại dashboard VÀ tải lại danh sách giao dịch
                    // với từ khóa tìm kiếm hiện tại
                    loadDashboardData();
                    String currentSearch = searchView.getQuery().toString();
                    loadTransactions(currentSearch.isEmpty() ? null : currentSearch);

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