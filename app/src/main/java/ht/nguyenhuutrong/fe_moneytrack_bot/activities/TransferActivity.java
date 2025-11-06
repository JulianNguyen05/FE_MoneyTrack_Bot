package ht.nguyenhuutrong.fe_moneytrack_bot.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
import ht.nguyenhuutrong.fe_moneytrack_bot.api.TokenManager;
import ht.nguyenhuutrong.fe_moneytrack_bot.models.Wallet;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransferActivity extends AppCompatActivity {

    private Spinner spinnerFromWallet, spinnerToWallet;
    private EditText editTextAmount, editTextDescription;
    private Button buttonSelectDate, buttonConfirmTransfer;

    private ApiService apiService;
    // private String authToken; // <-- SỬA LẠI: Xóa, Interceptor sẽ lo
    private TokenManager tokenManager;

    private List<Wallet> walletList = new ArrayList<>();
    private Calendar selectedDate = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);

        tokenManager = new TokenManager(this);
        // authToken = "Bearer " + tokenManager.getToken(); // <-- SỬA LẠI: Xóa dòng này

        // SỬA LẠI: Dùng getApiService(this) để có Context và Interceptor
        apiService = RetrofitClient.getApiService(this);

        // Ánh xạ views
        spinnerFromWallet = findViewById(R.id.spinnerFromWallet);
        spinnerToWallet = findViewById(R.id.spinnerToWallet);
        editTextAmount = findViewById(R.id.editTextAmount);
        editTextDescription = findViewById(R.id.editTextDescription);
        buttonSelectDate = findViewById(R.id.buttonSelectDate);
        buttonConfirmTransfer = findViewById(R.id.buttonConfirmTransfer);

        updateDateButtonText();

        // Gán sự kiện
        buttonSelectDate.setOnClickListener(v -> showDatePicker());
        buttonConfirmTransfer.setOnClickListener(v -> confirmTransfer());

        loadWallets();
    }

    private void loadWallets() {
        // SỬA LẠI: Xóa authToken khỏi lời gọi hàm
        apiService.getWallets().enqueue(new Callback<List<Wallet>>() {
            @Override
            public void onResponse(Call<List<Wallet>> call, Response<List<Wallet>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    walletList = response.body();
                    List<String> walletNames = new ArrayList<>();
                    for (Wallet wallet : walletList) {
                        walletNames.add(wallet.getName());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            TransferActivity.this,
                            android.R.layout.simple_spinner_item,
                            walletNames
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    spinnerFromWallet.setAdapter(adapter);
                    spinnerToWallet.setAdapter(adapter);
                } else {
                    Toast.makeText(TransferActivity.this, "Không thể tải danh sách ví!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Wallet>> call, Throwable t) {
                Toast.makeText(TransferActivity.this, "Lỗi mạng khi tải ví: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmTransfer() {
        String amountStr = editTextAmount.getText().toString();
        String description = editTextDescription.getText().toString();
        String date = getSelectedDateString();

        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        int fromPosition = spinnerFromWallet.getSelectedItemPosition();
        int toPosition = spinnerToWallet.getSelectedItemPosition();

        // (Kiểm tra xem list có rỗng không)
        if (walletList.isEmpty()) {
            Toast.makeText(this, "Không có ví nào để chuyển", Toast.LENGTH_SHORT).show();
            return;
        }

        if (fromPosition == toPosition) {
            Toast.makeText(this, "Ví nguồn và ví đích không được trùng nhau!", Toast.LENGTH_SHORT).show();
            return;
        }

        int fromWalletId = walletList.get(fromPosition).getId();
        int toWalletId = walletList.get(toPosition).getId();

        // SỬA LẠI: Xóa authToken khỏi lời gọi hàm
        apiService.transferFunds(fromWalletId, toWalletId, amount, date, description)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(TransferActivity.this, "Chuyển tiền thành công!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(TransferActivity.this, "Chuyển tiền thất bại!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(TransferActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // (Các hàm showDatePicker, updateDateButtonText, getSelectedDateString giữ nguyên)
    private void showDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, day) -> {
            selectedDate.set(year, month, day);
            updateDateButtonText();
        }, selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH));
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
}