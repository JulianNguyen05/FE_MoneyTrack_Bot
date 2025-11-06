package ht.nguyenhuutrong.fe_moneytrack_bot.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ht.nguyenhuutrong.fe_moneytrack_bot.R;
import ht.nguyenhuutrong.fe_moneytrack_bot.adapters.BudgetAdapter;
import ht.nguyenhuutrong.fe_moneytrack_bot.api.ApiService;
import ht.nguyenhuutrong.fe_moneytrack_bot.api.RetrofitClient;
import ht.nguyenhuutrong.fe_moneytrack_bot.api.TokenManager;
import ht.nguyenhuutrong.fe_moneytrack_bot.models.Budget;
import ht.nguyenhuutrong.fe_moneytrack_bot.models.Category;
import ht.nguyenhuutrong.fe_moneytrack_bot.models.ReportEntry;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BudgetActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BudgetAdapter adapter;
    private TextView textViewCurrentMonth;
    private FloatingActionButton fabAddBudget;

    private ApiService apiService;
    // private String authToken; // <-- SỬA LẠI: Không cần nữa
    private TokenManager tokenManager;

    // Dữ liệu
    private List<Budget> budgetList = new ArrayList<>();
    private List<ReportEntry> reportList = new ArrayList<>();

    // Tháng/năm hiện tại đang xem
    private int currentMonth;
    private int currentYear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        // Lấy tháng/năm hiện tại
        Calendar today = Calendar.getInstance();
        currentMonth = today.get(Calendar.MONTH) + 1; // Calendar.MONTH bắt đầu từ 0
        currentYear = today.get(Calendar.YEAR);

        // Khởi tạo API
        tokenManager = new TokenManager(this);
        // authToken = "Bearer " + tokenManager.getToken(); // <-- SỬA LẠI: Xóa dòng này

        // SỬA LẠI: Dùng getApiService(this) để Retrofit có Context
        // và tự động đính kèm Token qua Interceptor
        apiService = RetrofitClient.getApiService(this);

        // Ánh xạ View
        textViewCurrentMonth = findViewById(R.id.textViewCurrentMonth);
        recyclerView = findViewById(R.id.recyclerViewBudgets);
        fabAddBudget = findViewById(R.id.fab_add_budget);

        // Cập nhật text tháng
        textViewCurrentMonth.setText(String.format(Locale.getDefault(), "Tháng %d/%d", currentMonth, currentYear));

        // Setup RecyclerView
        setupRecyclerView();

        // Setup nút "+"
        fabAddBudget.setOnClickListener(v -> showAddBudgetDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Tải lại dữ liệu mỗi khi quay lại màn hình
        loadAllData();
    }

    private void setupRecyclerView() {
        adapter = new BudgetAdapter(new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    // --- (1) HÀM TẢI DỮ LIỆU CHÍNH ---
    private void loadAllData() {
        loadBudgets();
    }

    // --- (2) TẢI DANH SÁCH NGÂN SÁCH ---
    private void loadBudgets() {
        // SỬA LẠI: Xóa 'authToken' khỏi lời gọi hàm
        apiService.getBudgets(currentMonth, currentYear).enqueue(new Callback<List<Budget>>() {
            @Override
            public void onResponse(Call<List<Budget>> call, Response<List<Budget>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    budgetList = response.body();
                    loadSpendingReport();
                } else {
                    Toast.makeText(BudgetActivity.this, "Không thể tải Ngân sách", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<List<Budget>> call, Throwable t) {
                Toast.makeText(BudgetActivity.this, "Lỗi mạng (Ngân sách)", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- (3) TẢI BÁO CÁO CHI TIÊU ---
    private void loadSpendingReport() {
        String startDate = String.format(Locale.US, "%d-%02d-01", currentYear, currentMonth);

        Calendar calendar = Calendar.getInstance();
        calendar.set(currentYear, currentMonth - 1, 1);
        int lastDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        String endDate = String.format(Locale.US, "%d-%02d-%d", currentYear, currentMonth, lastDayOfMonth);

        // SỬA LẠI: Xóa 'authToken' khỏi lời gọi hàm
        apiService.getReportSummary(startDate, endDate).enqueue(new Callback<List<ReportEntry>>() {
            @Override
            public void onResponse(Call<List<ReportEntry>> call, Response<List<ReportEntry>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    reportList = response.body();
                    mergeDataAndUpdateAdapter();
                } else {
                    Toast.makeText(BudgetActivity.this, "Không thể tải Báo cáo", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<List<ReportEntry>> call, Throwable t) {
                Toast.makeText(BudgetActivity.this, "Lỗi mạng (Báo cáo)", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- (4) GỘP (MERGE) DỮ LIỆU ---
    private void mergeDataAndUpdateAdapter() {
        // (Hàm này đã đúng, giữ nguyên)
        Map<String, Double> spendingMap = new HashMap<>();
        for (ReportEntry entry : reportList) {
            spendingMap.put(entry.getCategoryName(), entry.getTotalAmount());
        }

        List<BudgetAdapter.BudgetStatus> statusList = new ArrayList<>();

        for (Budget budget : budgetList) {
            String categoryName = budget.getCategoryDetails().getName();
            double spentAmount = spendingMap.getOrDefault(categoryName, 0.0);
            statusList.add(new BudgetAdapter.BudgetStatus(budget, spentAmount));
        }

        adapter.setData(statusList);
    }

    // --- (5) HIỂN THỊ HỘP THOẠI THÊM NGÂN SÁCH ---
    private void showAddBudgetDialog() {
        // (Hàm này đã đúng, giữ nguyên)
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_budget, null);
        builder.setView(dialogView);

        Spinner spinnerCategory = dialogView.findViewById(R.id.spinnerCategoryBudget);
        EditText editTextAmount = dialogView.findViewById(R.id.editTextAmountBudget);
        Button buttonSave = dialogView.findViewById(R.id.buttonSaveBudget);

        loadCategoriesForSpinner(spinnerCategory);

        AlertDialog dialog = builder.create();

        buttonSave.setOnClickListener(v -> {
            String amountStr = editTextAmount.getText().toString();
            if (amountStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = Double.parseDouble(amountStr);
            Category selectedCategory = (Category) spinnerCategory.getSelectedItem();

            if (selectedCategory == null) {
                Toast.makeText(this, "Chưa chọn danh mục", Toast.LENGTH_SHORT).show();
                return;
            }

            createBudget(selectedCategory.getId(), amount, dialog);
        });

        dialog.show();
    }

    // (6) Hàm phụ 1: Tải Category cho Spinner (trong Dialog)
    private void loadCategoriesForSpinner(Spinner spinnerCategory) {
        // SỬA LẠI: Xóa 'authToken' khỏi lời gọi hàm
        apiService.getCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Category> expenseCategories = new ArrayList<>();

                    for (Category c : response.body()) {
                        if ("expense".equals(c.getType())) {
                            expenseCategories.add(c);
                        }
                    }

                    // (Phần adapter ghi đè của bạn rất tốt, giữ nguyên)
                    ArrayAdapter<Category> adapter = new ArrayAdapter<Category>(
                            BudgetActivity.this,
                            android.R.layout.simple_spinner_item,
                            expenseCategories
                    ) {
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            TextView view = (TextView) super.getView(position, convertView, parent);
                            view.setText(expenseCategories.get(position).getName());
                            return view;
                        }
                        @Override
                        public View getDropDownView(int position, View convertView, ViewGroup parent) {
                            TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                            view.setText(expenseCategories.get(position).getName());
                            return view;
                        }
                    };

                    spinnerCategory.setAdapter(adapter);
                }
            }
            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {}
        });
    }

    // (7) Hàm phụ 2: Gọi API Tạo Ngân sách
    private void createBudget(int categoryId, double amount, AlertDialog dialog) {

        // SỬA LẠI: Gửi một Budget object, không gửi 5 trường riêng lẻ

        // 1. Tạo một Budget object
        Budget newBudget = new Budget();
        newBudget.setCategory(categoryId); // Giả định model Budget dùng setCategory(int id)
        newBudget.setAmount(amount);
        newBudget.setMonth(currentMonth);
        newBudget.setYear(currentYear);

        // 2. Gửi object đó bằng @Body
        apiService.createBudget(newBudget)
                .enqueue(new Callback<Budget>() {
                    @Override
                    public void onResponse(Call<Budget> call, Response<Budget> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(BudgetActivity.this, "Đã tạo ngân sách!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            loadAllData(); // Tải lại toàn bộ
                        } else {
                            Toast.makeText(BudgetActivity.this, "Tạo thất bại (Có thể đã tồn tại?)", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<Budget> call, Throwable t) {
                        Toast.makeText(BudgetActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}