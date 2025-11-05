// trong ht.nguyenhuutrong.fe_moneytrack_bot.activities/BudgetActivity.java
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
    private String authToken;
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
        authToken = "Bearer " + tokenManager.getToken();
        apiService = RetrofitClient.getClient().create(ApiService.class);

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
        // Khởi tạo adapter với 1 danh sách rỗng
        adapter = new BudgetAdapter(new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    // --- (1) HÀM TẢI DỮ LIỆU CHÍNH ---
    // Bắt đầu chuỗi API call: Lấy Ngân sách -> Lấy Chi tiêu
    private void loadAllData() {
        loadBudgets();
    }

    // --- (2) TẢI DANH SÁCH NGÂN SÁCH ---
    private void loadBudgets() {
        apiService.getBudgets(authToken, currentMonth, currentYear).enqueue(new Callback<List<Budget>>() {
            @Override
            public void onResponse(Call<List<Budget>> call, Response<List<Budget>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    budgetList = response.body();
                    // Sau khi lấy Ngân sách thành công, lấy Báo cáo Chi tiêu
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
    // --- (3) TẢI BÁO CÁO CHI TIÊU ---
    private void loadSpendingReport() {
        // Lấy báo cáo cho tháng/năm hiện tại
        String startDate = String.format(Locale.US, "%d-%02d-01", currentYear, currentMonth);

        // --- 💡 FIX LỖI "31" TẠI ĐÂY ---
        // 1. Tạo 1 calendar
        Calendar calendar = Calendar.getInstance();
        // 2. Set năm và tháng (LƯU Ý: Calendar.MONTH bắt đầu từ 0, nên phải -1)
        calendar.set(currentYear, currentMonth - 1, 1);
        // 3. Lấy ngày CUỐI CÙNG thực tế của tháng đó
        int lastDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // 4. Tạo endDate chính xác (ví dụ: 2025-11-30)
        String endDate = String.format(Locale.US, "%d-%02d-%d", currentYear, currentMonth, lastDayOfMonth);
        // --- (Kết thúc fix) ---

        apiService.getReportSummary(authToken, startDate, endDate).enqueue(new Callback<List<ReportEntry>>() {
            @Override
            public void onResponse(Call<List<ReportEntry>> call, Response<List<ReportEntry>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    reportList = response.body();
                    // Đã có cả 2 danh sách -> Gộp chúng lại
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
        // Dùng Map để tra cứu chi tiêu (spentAmount) nhanh hơn
        Map<String, Double> spendingMap = new HashMap<>();
        for (ReportEntry entry : reportList) {
            spendingMap.put(entry.getCategoryName(), entry.getTotalAmount());
        }

        // Tạo danh sách BudgetStatus mới
        List<BudgetAdapter.BudgetStatus> statusList = new ArrayList<>();

        for (Budget budget : budgetList) {
            String categoryName = budget.getCategoryDetails().getName();

            // Lấy số tiền đã tiêu, nếu không có thì là 0
            double spentAmount = spendingMap.getOrDefault(categoryName, 0.0);

            // Thêm vào danh sách
            statusList.add(new BudgetAdapter.BudgetStatus(budget, spentAmount));
        }

        // Cập nhật Adapter
        adapter.setData(statusList);
    }

    // --- (5) HIỂN THỊ HỘP THOẠI THÊM NGÂN SÁCH ---
    private void showAddBudgetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_budget, null); // Tạo file layout mới
        builder.setView(dialogView);

        Spinner spinnerCategory = dialogView.findViewById(R.id.spinnerCategoryBudget);
        EditText editTextAmount = dialogView.findViewById(R.id.editTextAmountBudget);
        Button buttonSave = dialogView.findViewById(R.id.buttonSaveBudget);

        // Tải danh sách danh mục (CHỈ DANH MỤC CHI)
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

            // Gọi API tạo
            createBudget(selectedCategory.getId(), amount, dialog);
        });

        dialog.show();
    }

    // (6) Hàm phụ 1: Tải Category cho Spinner (trong Dialog)
    private void loadCategoriesForSpinner(Spinner spinnerCategory) {
        apiService.getCategories(authToken).enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Category> expenseCategories = new ArrayList<>();
                    List<String> categoryNames = new ArrayList<>();

                    for (Category c : response.body()) {
                        if ("expense".equals(c.getType())) { // Chỉ lọc danh mục CHI
                            expenseCategories.add(c);
                            categoryNames.add(c.getName());
                        }
                    }

                    // Dùng Adapter riêng để có thể lấy Object Category
                    ArrayAdapter<Category> adapter = new ArrayAdapter<Category>(
                            BudgetActivity.this,
                            android.R.layout.simple_spinner_item,
                            expenseCategories
                    ) {
                        // Ghi đè để hiển thị tên
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
        apiService.createBudget(authToken, categoryId, amount, currentMonth, currentYear)
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