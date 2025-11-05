package ht.nguyenhuutrong.fe_moneytrack_bot.activities;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import ht.nguyenhuutrong.fe_moneytrack_bot.R;
import ht.nguyenhuutrong.fe_moneytrack_bot.api.ApiService;
import ht.nguyenhuutrong.fe_moneytrack_bot.api.RetrofitClient;
import ht.nguyenhuutrong.fe_moneytrack_bot.api.TokenManager;
import ht.nguyenhuutrong.fe_moneytrack_bot.models.ReportEntry;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportActivity extends AppCompatActivity {

    private PieChart pieChart;
    private ApiService apiService;
    private String authToken;
    private TokenManager tokenManager;

    // --- Biến mới cho Lọc ngày ---
    private Button buttonStartDate, buttonEndDate, buttonFilter;
    private Calendar startDate = Calendar.getInstance();
    private Calendar endDate = Calendar.getInstance();
    // ----------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        pieChart = findViewById(R.id.pieChart);

        // --- Ánh xạ các nút Lọc ---
        buttonStartDate = findViewById(R.id.buttonStartDate);
        buttonEndDate = findViewById(R.id.buttonEndDate);
        buttonFilter = findViewById(R.id.buttonFilter);
        // -------------------------

        tokenManager = new TokenManager(this);
        String token = tokenManager.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        authToken = "Bearer " + token;
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // --- Cài đặt ngày mặc định ---
        // (Mặc định: 30 ngày qua)
        endDate.setTime(Calendar.getInstance().getTime());
        startDate.setTime(Calendar.getInstance().getTime());
        startDate.add(Calendar.DAY_OF_MONTH, -30);
        updateDateButtonText();
        // -----------------------------

        // --- Cài đặt sự kiện ---
        buttonStartDate.setOnClickListener(v -> showDatePicker(true));
        buttonEndDate.setOnClickListener(v -> showDatePicker(false));
        buttonFilter.setOnClickListener(v -> loadReportData()); // Tải lại khi nhấn nút
        // -----------------------

        setupPieChart();
        loadReportData(); // Tải lần đầu với 30 ngày
    }

    // --- Hàm chọn ngày (DatePicker) ---
    private void showDatePicker(boolean isStartDate) {
        Calendar calendarToUpdate = isStartDate ? startDate : endDate;

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    calendarToUpdate.set(year, month, day);
                    updateDateButtonText();
                },
                calendarToUpdate.get(Calendar.YEAR),
                calendarToUpdate.get(Calendar.MONTH),
                calendarToUpdate.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    // --- Cập nhật văn bản trên nút ---
    private void updateDateButtonText() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        buttonStartDate.setText("Từ: " + sdf.format(startDate.getTime()));
        buttonEndDate.setText("Đến: " + sdf.format(endDate.getTime()));
    }

    // --- Lấy ngày (chuẩn YYYY-MM-DD) để gửi API ---
    private String getFormattedDate(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(calendar.getTime());
    }

    private void setupPieChart() {
        // (Giữ nguyên code setupPieChart của bạn)
        pieChart.getDescription().setEnabled(false);
        pieChart.setUsePercentValues(true);
        pieChart.setHoleRadius(25f);
        pieChart.setTransparentCircleRadius(30f);
        pieChart.setDrawEntryLabels(false);

        Legend legend = pieChart.getLegend();
        legend.setEnabled(true);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setWordWrapEnabled(true);
        legend.setDrawInside(false);
        legend.setTextSize(12f);
    }

    private void loadReportData() {
        // --- NÂNG CẤP HÀM NÀY ---
        String start = getFormattedDate(startDate);
        String end = getFormattedDate(endDate);

        // Gọi API với ngày đã chọn
        apiService.getReportSummary(authToken, start, end).enqueue(new Callback<List<ReportEntry>>() {
            @Override
            public void onResponse(Call<List<ReportEntry>> call, Response<List<ReportEntry>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ReportEntry> reportList = response.body();
                    if (reportList.isEmpty()) {
                        Toast.makeText(ReportActivity.this, "Không có dữ liệu chi tiêu!", Toast.LENGTH_SHORT).show();
                        pieChart.clear(); // Xóa biểu đồ cũ
                        pieChart.setCenterText("Không có dữ liệu");
                        pieChart.invalidate();
                        return;
                    }
                    populatePieChart(reportList);
                } else {
                    Toast.makeText(ReportActivity.this,
                            "Không thể tải báo cáo (mã lỗi: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ReportEntry>> call, Throwable t) {
                Toast.makeText(ReportActivity.this,
                        "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void populatePieChart(List<ReportEntry> reportData) {
        // (Giữ nguyên code populatePieChart của bạn)
        ArrayList<PieEntry> entries = new ArrayList<>();
        for (ReportEntry entry : reportData) {
            if (entry.getTotalAmount() > 0) {
                entries.add(new PieEntry((float) entry.getTotalAmount(), entry.getCategoryName()));
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "");

        // Cấu hình màu sắc
        ArrayList<Integer> colors = new ArrayList<>();
        for (int c : ColorTemplate.VORDIPLOM_COLORS) colors.add(c);
        for (int c : ColorTemplate.JOYFUL_COLORS) colors.add(c);
        for (int c : ColorTemplate.COLORFUL_COLORS) colors.add(c);
        dataSet.setColors(colors);

        // Cấu hình giá trị (%)
        dataSet.setDrawValues(true);
        dataSet.setValueFormatter(new PercentFormatter(pieChart));
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setValueLinePart1OffsetPercentage(100.f);
        dataSet.setValueLinePart1Length(0.4f);
        dataSet.setValueLinePart2Length(0.4f);
        dataSet.setValueLineColor(Color.GRAY);

        PieData pieData = new PieData(dataSet);
        pieData.setDrawValues(true);

        pieChart.setData(pieData);
        pieChart.animateY(1000);
        pieChart.invalidate();
    }
}