package ht.nguyenhuutrong.fe_moneytrack_bot.activities;

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

public class MainActivity extends AppCompatActivity {

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
        adapter = new TransactionAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        apiService = RetrofitClient.getClient().create(ApiService.class);

        // Setup các nút
        setupButtons();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData(); // Tải tổng số dư
        loadTransactions();  // Tải giao dịch
    }

    private void setupButtons() {
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
    }

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
}
