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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import ht.nguyenhuutrong.fe_moneytrack_bot.R;
import ht.nguyenhuutrong.fe_moneytrack_bot.api.ApiService;
import ht.nguyenhuutrong.fe_moneytrack_bot.api.RetrofitClient;
import ht.nguyenhuutrong.fe_moneytrack_bot.api.TokenManager;
import ht.nguyenhuutrong.fe_moneytrack_bot.models.Category;
import ht.nguyenhuutrong.fe_moneytrack_bot.models.Transaction;
import ht.nguyenhuutrong.fe_moneytrack_bot.models.Wallet;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditTransactionActivity extends AppCompatActivity {

    private EditText editTextAmount, editTextDescription;
    private Spinner spinnerCategory, spinnerWallet;
    private Button buttonSelectDate, buttonSaveTransaction;
    private TextView textViewTitle;

    private ApiService apiService;
    private String authToken;
    private TokenManager tokenManager;

    private List<Category> categoryList = new ArrayList<>();
    private List<Wallet> walletList = new ArrayList<>();

    private Calendar selectedDate = Calendar.getInstance();
    private int transactionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_form);

        // ✅ Nhận ID giao dịch từ Intent
        transactionId = getIntent().getIntExtra("TRANSACTION_ID", -1);
        if (transactionId == -1) {
            Toast.makeText(this, "Lỗi: Không tìm thấy giao dịch", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // ✅ Token và API service
        tokenManager = new TokenManager(this);
        String token = tokenManager.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        authToken = "Bearer " + token;
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // ✅ Ánh xạ View
        textViewTitle = findViewById(R.id.textViewTitle);
        editTextAmount = findViewById(R.id.editTextAmount);
        editTextDescription = findViewById(R.id.editTextDescription);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerWallet = findViewById(R.id.spinnerWallet);
        buttonSelectDate = findViewById(R.id.buttonSelectDate);
        buttonSaveTransaction = findViewById(R.id.buttonSaveTransaction);

        // ✅ Giao diện
        textViewTitle.setText("Sửa Giao dịch");
        buttonSaveTransaction.setText("Cập nhật");

        // ✅ Sự kiện
        buttonSelectDate.setOnClickListener(v -> showDatePicker());
        buttonSaveTransaction.setOnClickListener(v -> updateTransaction());

        // ✅ Tải dữ liệu
        loadCategories();
    }

    // --- Tải danh mục ---
    private void loadCategories() {
        apiService.getCategories(authToken).enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categoryList = response.body();
                    List<String> categoryNames = new ArrayList<>();
                    for (Category c : categoryList) categoryNames.add(c.getName());

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            EditTransactionActivity.this,
                            android.R.layout.simple_spinner_item,
                            categoryNames
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCategory.setAdapter(adapter);

                    // Sau khi tải category → tải ví
                    loadWallets();
                } else {
                    Toast.makeText(EditTransactionActivity.this, "Không thể tải danh mục", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Toast.makeText(EditTransactionActivity.this, "Lỗi mạng (tải danh mục)", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- Tải ví ---
    private void loadWallets() {
        apiService.getWallets(authToken).enqueue(new Callback<List<Wallet>>() {
            @Override
            public void onResponse(Call<List<Wallet>> call, Response<List<Wallet>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    walletList = response.body();
                    List<String> walletNames = new ArrayList<>();
                    for (Wallet w : walletList) walletNames.add(w.getName());

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            EditTransactionActivity.this,
                            android.R.layout.simple_spinner_item,
                            walletNames
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerWallet.setAdapter(adapter);

                    // Sau khi có cả category & wallet → tải chi tiết giao dịch
                    loadTransactionDetails();
                } else {
                    Toast.makeText(EditTransactionActivity.this, "Không thể tải ví", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Wallet>> call, Throwable t) {
                Toast.makeText(EditTransactionActivity.this, "Lỗi mạng (tải ví)", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- Tải chi tiết giao dịch ---
    private void loadTransactionDetails() {
        apiService.getTransactionDetails(authToken, transactionId).enqueue(new Callback<Transaction>() {
            @Override
            public void onResponse(Call<Transaction> call, Response<Transaction> response) {
                if (response.isSuccessful() && response.body() != null) {
                    prefillForm(response.body());
                } else {
                    Toast.makeText(EditTransactionActivity.this, "Không thể tải chi tiết giao dịch", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Transaction> call, Throwable t) {
                Toast.makeText(EditTransactionActivity.this, "Lỗi mạng (tải chi tiết)", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- Điền dữ liệu cũ ---
    private void prefillForm(Transaction t) {
        editTextAmount.setText(String.valueOf(t.getAmount()));
        editTextDescription.setText(t.getDescription());

        // Ngày
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            selectedDate.setTime(sdf.parse(t.getDate()));
            updateDateButtonText();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Spinner
        spinnerCategory.setSelection(findCategoryIndex(t.getCategory()));
        spinnerWallet.setSelection(findWalletIndex(t.getWallet()));
    }

    private int findCategoryIndex(int categoryId) {
        for (int i = 0; i < categoryList.size(); i++) {
            if (categoryList.get(i).getId() == categoryId) return i;
        }
        return 0;
    }

    private int findWalletIndex(int walletId) {
        for (int i = 0; i < walletList.size(); i++) {
            if (walletList.get(i).getId() == walletId) return i;
        }
        return 0;
    }

    // --- Hiển thị chọn ngày ---
    private void showDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    selectedDate.set(year, month, day);
                    updateDateButtonText();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void updateDateButtonText() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        buttonSelectDate.setText(sdf.format(selectedDate.getTime()));
    }

    private String getSelectedDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(selectedDate.getTime());
    }

    // --- Gửi dữ liệu cập nhật ---
    private void updateTransaction() {
        String amountStr = editTextAmount.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();

        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        String date = getSelectedDateString();

        int categoryIndex = spinnerCategory.getSelectedItemPosition();
        int walletIndex = spinnerWallet.getSelectedItemPosition();
        int categoryId = categoryList.get(categoryIndex).getId();
        int walletId = walletList.get(walletIndex).getId();

        apiService.updateTransaction(authToken, transactionId, amount, description, date, categoryId, walletId)
                .enqueue(new Callback<Transaction>() {
                    @Override
                    public void onResponse(Call<Transaction> call, Response<Transaction> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(EditTransactionActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(EditTransactionActivity.this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Transaction> call, Throwable t) {
                        Toast.makeText(EditTransactionActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
