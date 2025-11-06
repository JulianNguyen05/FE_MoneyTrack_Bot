package ht.nguyenhuutrong.fe_moneytrack_bot.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ht.nguyenhuutrong.fe_moneytrack_bot.R;
import ht.nguyenhuutrong.fe_moneytrack_bot.adapters.CategoryAdapter;
import ht.nguyenhuutrong.fe_moneytrack_bot.api.ApiService;
import ht.nguyenhuutrong.fe_moneytrack_bot.api.RetrofitClient;
import ht.nguyenhuutrong.fe_moneytrack_bot.api.TokenManager;
import ht.nguyenhuutrong.fe_moneytrack_bot.models.Category;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CategoryAdapter adapter;
    private ApiService apiService;
    private TokenManager tokenManager;
    // private String authToken; // <-- SỬA LẠI: Xóa, Interceptor sẽ lo

    private EditText editTextCategoryName;
    private RadioGroup radioGroupCategoryType;
    private Button buttonAddCategory;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        // --- Khởi tạo API & Token ---
        tokenManager = new TokenManager(this);
        String token = tokenManager.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Token không hợp lệ. Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // authToken = "Bearer " + token; // <-- SỬA LẠI: Xóa

        // SỬA LẠI: Dùng getApiService(this) để có Context và Interceptor
        apiService = RetrofitClient.getApiService(this);

        // --- Ánh xạ View ---
        setupViews();
        setupRecyclerView();

        // --- Tải danh mục ---
        loadCategories();

        // --- Sự kiện nút Thêm ---
        buttonAddCategory.setOnClickListener(v -> createCategory());
    }

    private void setupViews() {
        editTextCategoryName = findViewById(R.id.editTextCategoryName);
        radioGroupCategoryType = findViewById(R.id.radioGroupCategoryType);
        buttonAddCategory = findViewById(R.id.buttonAddCategory);
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerViewCategories);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new CategoryAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
    }

    private void loadCategories() {
        // SỬA LẠI: Xóa authToken khỏi lời gọi hàm
        apiService.getCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setData(response.body());
                } else {
                    Toast.makeText(CategoryActivity.this, "Không thể tải danh mục", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Toast.makeText(CategoryActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createCategory() {
        String name = editTextCategoryName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên danh mục", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedId = radioGroupCategoryType.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Vui lòng chọn loại danh mục", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedRadio = findViewById(selectedId);
        // (Giả sử R.id.radioIncome là RadioButton cho "Thu nhập")
        String type = (selectedRadio.getId() == R.id.radioIncome) ? "income" : "expense";

        // --- SỬA LẠI: Gửi một Category object, không gửi 3 trường riêng lẻ ---

        // 1. Tạo một Category object
        Category newCategory = new Category();
        newCategory.setName(name);
        newCategory.setType(type);

        // 2. Gửi object đó bằng @Body
        apiService.createCategory(newCategory).enqueue(new Callback<Category>() {
            @Override
            public void onResponse(Call<Category> call, Response<Category> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(CategoryActivity.this, "Đã thêm danh mục!", Toast.LENGTH_SHORT).show();
                    editTextCategoryName.setText("");
                    radioGroupCategoryType.clearCheck();
                    loadCategories(); // Refresh danh sách
                } else {
                    Toast.makeText(CategoryActivity.this, "Thêm thất bại (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Category> call, Throwable t) {
                Toast.makeText(CategoryActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}