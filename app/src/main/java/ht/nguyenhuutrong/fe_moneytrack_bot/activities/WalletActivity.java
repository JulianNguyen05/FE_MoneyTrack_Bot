package ht.nguyenhuutrong.fe_moneytrack_bot.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// SỬA LẠI: Cần ArrayList để khởi tạo adapter
import java.util.ArrayList;
import java.util.List;

import ht.nguyenhuutrong.fe_moneytrack_bot.R;
import ht.nguyenhuutrong.fe_moneytrack_bot.adapters.WalletAdapter;
import ht.nguyenhuutrong.fe_moneytrack_bot.api.ApiService;
import ht.nguyenhuutrong.fe_moneytrack_bot.api.RetrofitClient;
import ht.nguyenhuutrong.fe_moneytrack_bot.api.TokenManager;
import ht.nguyenhuutrong.fe_moneytrack_bot.models.Wallet;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WalletActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private WalletAdapter adapter;
    private ApiService apiService;
    private TokenManager tokenManager;
    // private String authToken; // <-- SỬA LẠI: Xóa, Interceptor sẽ lo

    private EditText editTextWalletName;
    private EditText editTextInitialBalance;
    private Button buttonAddWallet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        tokenManager = new TokenManager(this);
        // authToken = "Bearer " + tokenManager.getToken(); // <-- SỬA LẠI: Xóa

        // SỬA LẠI: Dùng getApiService(this) để có Context và Interceptor
        apiService = RetrofitClient.getApiService(this);

        setupViews();
        setupRecyclerView();
        loadWallets();

        buttonAddWallet.setOnClickListener(v -> createWallet());
    }

    private void setupViews() {
        editTextWalletName = findViewById(R.id.editTextWalletName);
        editTextInitialBalance = findViewById(R.id.editTextInitialBalance);
        buttonAddWallet = findViewById(R.id.buttonAddWallet);
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerViewWallets);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // SỬA LẠI: Khởi tạo adapter với danh sách rỗng (nếu WalletAdapter của bạn yêu cầu)
        // Nếu adapter của bạn có constructor rỗng thì `new WalletAdapter()` là OK.
        adapter = new WalletAdapter();
        recyclerView.setAdapter(adapter);
    }

    private void loadWallets() {
        // SỬA LẠI: Xóa authToken khỏi lời gọi hàm
        apiService.getWallets().enqueue(new Callback<List<Wallet>>() {
            @Override
            public void onResponse(Call<List<Wallet>> call, Response<List<Wallet>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setData(response.body());
                } else {
                    Toast.makeText(WalletActivity.this, "Không thể tải danh sách ví", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Wallet>> call, Throwable t) {
                Toast.makeText(WalletActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createWallet() {
        String name = editTextWalletName.getText().toString().trim();
        String balanceStr = editTextInitialBalance.getText().toString().trim();

        if (name.isEmpty() || balanceStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        double balance;
        try {
            balance = Double.parseDouble(balanceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số dư không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- SỬA LẠI: Gửi một Wallet object, không gửi 3 trường riêng lẻ ---

        // 1. Tạo một Wallet object
        Wallet newWallet = new Wallet();
        newWallet.setName(name);
        newWallet.setBalance(balance); // Giả định model Wallet dùng setBalance(double)

        // 2. Gửi object đó bằng @Body
        apiService.createWallet(newWallet).enqueue(new Callback<Wallet>() {
            @Override
            public void onResponse(Call<Wallet> call, Response<Wallet> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(WalletActivity.this, "Đã thêm ví!", Toast.LENGTH_SHORT).show();
                    editTextWalletName.setText("");
                    editTextInitialBalance.setText("");
                    loadWallets(); // Tải lại danh sách
                } else {
                    Toast.makeText(WalletActivity.this, "Thêm thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Wallet> call, Throwable t) {
                Toast.makeText(WalletActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}