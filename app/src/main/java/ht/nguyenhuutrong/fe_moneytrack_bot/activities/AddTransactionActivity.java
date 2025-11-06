package ht.nguyenhuutrong.fe_moneytrack_bot.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import ht.nguyenhuutrong.fe_moneytrack_bot.R;
import ht.nguyenhuutrong.fe_moneytrack_bot.api.ApiService;
import ht.nguyenhuutrong.fe_moneytrack_bot.api.RetrofitClient;
// TokenManager vẫn cần thiết để check xem đã đăng nhập chưa
import ht.nguyenhuutrong.fe_moneytrack_bot.api.TokenManager;
import ht.nguyenhuutrong.fe_moneytrack_bot.models.Category;
import ht.nguyenhuutrong.fe_moneytrack_bot.models.Wallet;
import ht.nguyenhuutrong.fe_moneytrack_bot.models.Transaction;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddTransactionActivity extends AppCompatActivity {

    private EditText editTextAmount, editTextDescription;
    private Spinner spinnerCategory, spinnerWallet;
    private Button buttonSelectDate, buttonSaveTransaction;
    private TextView textViewTitle;

    private ApiService apiService;
    // private String authToken; // <-- SỬA LẠI: Không cần nữa, Interceptor sẽ lo
    private TokenManager tokenManager;

    private List<Category> categoryList = new ArrayList<>();
    private List<Wallet> walletList = new ArrayList<>();

    private Calendar selectedDate = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_form);

        tokenManager = new TokenManager(this);
        // authToken = "Bearer " + tokenManager.getToken(); // <-- SỬA LẠI: Xóa dòng này

        // SỬA LẠI: Dùng getApiService(this) để Retrofit có Context
        // và tự động đính kèm Token qua Interceptor
        apiService = RetrofitClient.getApiService(this);

        // Ánh xạ Views
        textViewTitle = findViewById(R.id.textViewTitle);
        editTextAmount = findViewById(R.id.editTextAmount);
        editTextDescription = findViewById(R.id.editTextDescription);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerWallet = findViewById(R.id.spinnerWallet);
        buttonSelectDate = findViewById(R.id.buttonSelectDate);
        buttonSaveTransaction = findViewById(R.id.buttonSaveTransaction);

        // Tiêu đề và nút
        textViewTitle.setText("Thêm Giao dịch Mới");
        buttonSaveTransaction.setText("Lưu Mới");

        updateDateButtonText();

        // Sự kiện
        buttonSelectDate.setOnClickListener(v -> showDatePicker());
        buttonSaveTransaction.setOnClickListener(v -> saveTransaction());

        loadCategories();
        loadWallets();
    }

    private void loadCategories() {
        // Hàm này gọi apiService.getCategories() đã đúng (không cần authToken)
        apiService.getCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categoryList = response.body();
                    List<String> categoryNames = new ArrayList<>();
                    for (Category category : categoryList) {
                        categoryNames.add(category.getName());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            AddTransactionActivity.this,
                            android.R.layout.simple_spinner_item,
                            categoryNames
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCategory.setAdapter(adapter);
                } else {
                    Toast.makeText(AddTransactionActivity.this, "Không có danh mục nào!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Toast.makeText(AddTransactionActivity.this, "Không thể tải Danh mục: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadWallets() {
        // Hàm này gọi apiService.getWallets() đã đúng (không cần authToken)
        apiService.getWallets().enqueue(new Callback<List<Wallet>>() {
            @Override
            public void onResponse(Call<List<Wallet>> call, Response<List<Wallet>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    walletList = response.body();
                    List<String> walletNames = new ArrayList<>();
                    for (Wallet wallet : walletList) {
                        walletNames.add(wallet.getName() + " (" + wallet.getBalance() + "₫)");
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            AddTransactionActivity.this,
                            android.R.layout.simple_spinner_item,
                            walletNames
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerWallet.setAdapter(adapter);
                } else {
                    Toast.makeText(AddTransactionActivity.this, "Không có ví nào!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Wallet>> call, Throwable t) {
                Toast.makeText(AddTransactionActivity.this, "Không thể tải Ví: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDatePicker() {
        new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(year, month, dayOfMonth);
                    updateDateButtonText();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void updateDateButtonText() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        buttonSelectDate.setText(sdf.format(selectedDate.getTime()));
    }

    private String getSelectedDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(selectedDate.getTime());
    }

    private void saveTransaction() {
        String amountStr = editTextAmount.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();

        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (categoryList.isEmpty() || walletList.isEmpty()) {
            Toast.makeText(this, "Vui lòng tạo Danh mục và Ví trước", Toast.LENGTH_SHORT).show();
            return;
        }

        int categoryPosition = spinnerCategory.getSelectedItemPosition();
        int walletPosition = spinnerWallet.getSelectedItemPosition();

        if (categoryPosition < 0 || walletPosition < 0) {
            Toast.makeText(this, "Chưa chọn Danh mục hoặc Ví", Toast.LENGTH_SHORT).show();
            return;
        }

        int categoryId = categoryList.get(categoryPosition).getId();
        int walletId = walletList.get(walletPosition).getId();
        String date = getSelectedDateString();

        // --- SỬA LẠI: Gửi một Transaction object, không gửi 5 trường riêng lẻ ---

        // 1. Tạo một Transaction object
        Transaction newTransaction = new Transaction();
        newTransaction.setAmount(amount);
        newTransaction.setDescription(description);
        newTransaction.setDate(date);
        newTransaction.setCategory(categoryId); // Giả định model Transaction dùng setCategory(int id)
        newTransaction.setWallet(walletId);     // Giả định model Transaction dùng setWallet(int id)

        // 2. Gửi object đó bằng @Body
        apiService.createTransaction(newTransaction)
                .enqueue(new Callback<Transaction>() {
                    @Override
                    public void onResponse(Call<Transaction> call, Response<Transaction> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(AddTransactionActivity.this, "Đã lưu giao dịch!", Toast.LENGTH_SHORT).show();
                            finish(); // Tự động đóng Activity và quay về MainActivity
                        } else {
                            Toast.makeText(AddTransactionActivity.this, "Lưu thất bại (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Transaction> call, Throwable t) {
                        Toast.makeText(AddTransactionActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}