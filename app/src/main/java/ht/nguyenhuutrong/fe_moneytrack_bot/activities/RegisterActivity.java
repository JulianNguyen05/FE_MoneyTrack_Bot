package ht.nguyenhuutrong.fe_moneytrack_bot.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.util.Patterns; // 1. THÊM EMAIL (Import để check email)

import androidx.appcompat.app.AppCompatActivity;

import ht.nguyenhuutrong.fe_moneytrack_bot.R;
import ht.nguyenhuutrong.fe_moneytrack_bot.api.ApiService;
import ht.nguyenhuutrong.fe_moneytrack_bot.api.RetrofitClient;
import ht.nguyenhuutrong.fe_moneytrack_bot.models.RegisterRequest;
// Bạn cũng cần sửa file RegisterResponse nếu có, hoặc dùng một User model
import ht.nguyenhuutrong.fe_moneytrack_bot.models.User; // Giả sử bạn có User model
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    // 2. THÊM EMAIL
    private EditText editTextUsername, editTextEmail, editTextPassword;
    private Button buttonRegister;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editTextUsername = findViewById(R.id.editTextUsernameRegister);
        // 3. THÊM EMAIL (Nhớ đảm bảo ID này khớp với file XML của bạn)
        editTextEmail = findViewById(R.id.editTextEmailRegister);
        editTextPassword = findViewById(R.id.editTextPasswordRegister);
        buttonRegister = findViewById(R.id.buttonRegister);

        // SỬA LẠI: Dùng getApiService(this) để nhất quán
        // và để Retrofit có Context (dù API này không cần token)
        apiService = RetrofitClient.getApiService(this);

        buttonRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String username = editTextUsername.getText().toString().trim();
        // 4. THÊM EMAIL
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // 5. THÊM EMAIL (vào validation)
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        // (Nên check thêm định dạng email)
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // 6. THÊM EMAIL (vào request)
        RegisterRequest registerRequest = new RegisterRequest(username, email, password);

        // API của bạn trả về User object, không phải Void
        Call<User> call = apiService.registerUser(registerRequest);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, "Đăng ký thành công! Vui lòng đăng nhập.", Toast.LENGTH_LONG).show();
                    finish(); // Quay lại LoginActivity
                } else if (response.code() == 400) {
                    // Lỗi 400 giờ có thể do nhiều thứ (username, email, password)
                    Toast.makeText(RegisterActivity.this, "Tên đăng nhập hoặc Email đã tồn tại/không hợp lệ.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(RegisterActivity.this, "Đăng ký thất bại (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}