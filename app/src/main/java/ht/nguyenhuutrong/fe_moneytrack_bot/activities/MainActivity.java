package ht.nguyenhuutrong.fe_moneytrack_bot.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ht.nguyenhuutrong.fe_moneytrack_bot.R;
import ht.nguyenhuutrong.fe_moneytrack_bot.adapters.TransactionAdapter;
import ht.nguyenhuutrong.fe_moneytrack_bot.api.ApiService;
import ht.nguyenhuutrong.fe_moneytrack_bot.api.RetrofitClient;
import ht.nguyenhuutrong.fe_moneytrack_bot.api.TokenManager;
import ht.nguyenhuutrong.fe_moneytrack_bot.models.Transaction;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TransactionAdapter adapter;
    private List<Transaction> transactionList;
    private ApiService apiService;
    private TokenManager tokenManager;
    private String authToken;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tokenManager = new TokenManager(this);
        authToken = tokenManager.getToken();

        // --- Kiểm tra đăng nhập ---
        if (authToken == null || authToken.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập trước khi sử dụng!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        // --- Khởi tạo RecyclerView ---
        recyclerView = findViewById(R.id.recyclerViewTransactions);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        transactionList = new ArrayList<>();
        adapter = new TransactionAdapter(transactionList);
        recyclerView.setAdapter(adapter);

        // --- Khởi tạo API ---
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // --- Tải danh sách giao dịch ---
        fetchTransactions();

        // --- Nút chuyển sang CategoryActivity ---
        Button buttonGoToCategories = findViewById(R.id.buttonGoToCategories);
        buttonGoToCategories.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, CategoryActivity.class));
        });

        // --- Nút Đăng xuất (chỉ gọi nếu layout có nút này) ---
        Button buttonLogout = findViewById(R.id.buttonLogout);
        if (buttonLogout != null) {
            buttonLogout.setOnClickListener(v -> {
                tokenManager.clearToken();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            });
        }
    }

    private void fetchTransactions() {
        Call<List<Transaction>> call = apiService.getTransactions("Bearer " + authToken);

        call.enqueue(new Callback<List<Transaction>>() {
            @Override
            public void onResponse(Call<List<Transaction>> call, Response<List<Transaction>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setData(response.body());
                } else if (response.code() == 401) {
                    Toast.makeText(MainActivity.this, "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
                    tokenManager.clearToken();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Không thể tải dữ liệu (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                    Log.e("API_ERROR", "Response code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Transaction>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API_FAILURE", "Error: " + t.getMessage());
            }
        });
    }
}
