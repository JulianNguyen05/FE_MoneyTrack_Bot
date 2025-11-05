package ht.nguyenhuutrong.fe_moneytrack_bot.activities;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// --- (1) THÊM IMPORT CHO LINE CHART ---
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
// ------------------------------------

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
import ht.nguyenhuutrong.fe_moneytrack_bot.models.CashFlowEntry; // <-- (2) THÊM IMPORT
import ht.nguyenhuutrong.fe_moneytrack_bot.models.ReportEntry;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportActivity extends AppCompatActivity {

    private PieChart pieChart;
    private LineChart lineChart; // <-- (3) THÊM BIẾN MỚI
    private ApiService apiService;
    private String authToken;
    private TokenManager tokenManager;

    // Biến cho Lọc ngày (giữ nguyên)
    private Button buttonStartDate, buttonEndDate, buttonFilter;
    private Calendar startDate = Calendar.getInstance();
    private Calendar endDate = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report); // Đảm bảo layout có cả PieChart và LineChart

        pieChart = findViewById(R.id.pieChart);
        lineChart = findViewById(R.id.lineChart); // <-- (4) ÁNH XẠ VIEW MỚI

        // Ánh xạ các nút Lọc
        buttonStartDate = findViewById(R.id.buttonStartDate);
        buttonEndDate = findViewById(R.id.buttonEndDate);
        buttonFilter = findViewById(R.id.buttonFilter);

        tokenManager = new TokenManager(this);
        String token = tokenManager.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        authToken = "Bearer " + token;
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // Cài đặt ngày mặc định
        endDate.setTime(Calendar.getInstance().getTime());
        startDate.setTime(Calendar.getInstance().getTime());
        startDate.add(Calendar.DAY_OF_MONTH, -30);
        updateDateButtonText();

        // --- (5) SỬA LẠI SỰ KIỆN NÚT LỌC ---
        buttonStartDate.setOnClickListener(v -> showDatePicker(true));
        buttonEndDate.setOnClickListener(v -> showDatePicker(false));
        // Khi nhấn Lọc, tải CẢ HAI biểu đồ
        buttonFilter.setOnClickListener(v -> loadAllCharts());
        // ------------------------------------

        setupPieChart();
        setupLineChart(); // <-- (6) GỌI HÀM SETUP MỚI
        loadAllCharts(); // <-- (7) GỌI HÀM TẢI DỮ LIỆU CHÍNH
    }

    // Hàm chọn ngày (DatePicker) - (Giữ nguyên)
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

    // Cập nhật văn bản trên nút - (Giữ nguyên)
    private void updateDateButtonText() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        buttonStartDate.setText("Từ: " + sdf.format(startDate.getTime()));
        buttonEndDate.setText("Đến: " + sdf.format(endDate.getTime()));
    }

    // Lấy ngày (chuẩn YYYY-MM-DD) - (Giữ nguyên)
    private String getFormattedDate(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(calendar.getTime());
    }

    // --- (8) HÀM TẢI DỮ LIỆU MỚI ---
    // Hàm này sẽ gọi cả hai hàm tải dữ liệu con
    private void loadAllCharts() {
        loadPieChartData();
        loadCashFlowData();
    }

    // --- (9) ĐỔI TÊN HÀM TẢI PIE CHART ---
    // (Tên cũ: loadReportData)
    private void loadPieChartData() {
        String start = getFormattedDate(startDate);
        String end = getFormattedDate(endDate);

        apiService.getReportSummary(authToken, start, end).enqueue(new Callback<List<ReportEntry>>() {
            @Override
            public void onResponse(Call<List<ReportEntry>> call, Response<List<ReportEntry>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ReportEntry> reportList = response.body();
                    if (reportList.isEmpty()) {
                        pieChart.clear();
                        pieChart.setCenterText("Không có dữ liệu chi tiêu");
                        pieChart.invalidate();
                        return;
                    }
                    populatePieChart(reportList);
                } else {
                    Toast.makeText(ReportActivity.this,
                            "Không thể tải Báo cáo PieChart (mã lỗi: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<List<ReportEntry>> call, Throwable t) {
                Toast.makeText(ReportActivity.this,
                        "Lỗi mạng (PieChart): " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // --- (10) HÀM MỚI: TẢI DỮ LIỆU LINE CHART ---
    private void loadCashFlowData() {
        String start = getFormattedDate(startDate);
        String end = getFormattedDate(endDate);

        apiService.getCashFlowReport(authToken, start, end).enqueue(new Callback<List<CashFlowEntry>>() {
            @Override
            public void onResponse(Call<List<CashFlowEntry>> call, Response<List<CashFlowEntry>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<CashFlowEntry> cashFlowList = response.body();
                    if (cashFlowList.isEmpty()) {
                        lineChart.clear();
                        lineChart.setNoDataText("Không có dữ liệu dòng tiền");
                        lineChart.invalidate();
                        return;
                    }
                    populateLineChart(cashFlowList);
                } else {
                    Toast.makeText(ReportActivity.this,
                            "Không thể tải Báo cáo LineChart (mã lỗi: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<List<CashFlowEntry>> call, Throwable t) {
                Toast.makeText(ReportActivity.this,
                        "Lỗi mạng (LineChart): " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    // --- CÁC HÀM SETUP VÀ POPULATE BIỂU ĐỒ ---

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

    private void populatePieChart(List<ReportEntry> reportData) {
        // (Giữ nguyên code populatePieChart của bạn)
        ArrayList<PieEntry> entries = new ArrayList<>();
        for (ReportEntry entry : reportData) {
            if (entry.getTotalAmount() > 0) {
                entries.add(new PieEntry((float) entry.getTotalAmount(), entry.getCategoryName()));
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        ArrayList<Integer> colors = new ArrayList<>();
        for (int c : ColorTemplate.VORDIPLOM_COLORS) colors.add(c);
        for (int c : ColorTemplate.JOYFUL_COLORS) colors.add(c);
        for (int c : ColorTemplate.COLORFUL_COLORS) colors.add(c);
        dataSet.setColors(colors);

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

    // --- (11) HÀM MỚI: SETUP LINE CHART ---
    private void setupLineChart() {
        lineChart.getDescription().setEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);

        // Cấu hình trục X (Trục Ngày)
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f); // Chỉ hiển thị các giá trị nguyên (0, 1, 2...)
        xAxis.setAvoidFirstLastClipping(true);

        // Tắt trục Y bên phải
        lineChart.getAxisRight().setEnabled(false);
    }

    // --- (12) HÀM MỚI: POPULATE LINE CHART ---
    private void populateLineChart(List<CashFlowEntry> data) {
        ArrayList<Entry> incomeEntries = new ArrayList<>();
        ArrayList<Entry> expenseEntries = new ArrayList<>();
        ArrayList<String> dateLabels = new ArrayList<>(); // Danh sách nhãn ngày

        // Lặp qua dữ liệu API trả về
        for (int i = 0; i < data.size(); i++) {
            CashFlowEntry entry = data.get(i);

            // Thêm dữ liệu Thu/Chi vào 2 danh sách
            incomeEntries.add(new Entry(i, (float) entry.getTotalIncome()));
            expenseEntries.add(new Entry(i, (float) entry.getTotalExpense()));

            // Lưu lại nhãn ngày (ví dụ: "2025-11-05")
            dateLabels.add(entry.getDay());
        }

        // Cấu hình đường "Tổng Thu" (Màu xanh)
        LineDataSet incomeSet = new LineDataSet(incomeEntries, "Tổng Thu");
        incomeSet.setColor(Color.parseColor("#4CAF50")); // Màu xanh lá
        incomeSet.setCircleColor(Color.parseColor("#4CAF50"));
        incomeSet.setLineWidth(2f);
        incomeSet.setCircleRadius(3f);
        incomeSet.setDrawValues(false); // Ẩn số tiền trên điểm

        // Cấu hình đường "Tổng Chi" (Màu đỏ)
        LineDataSet expenseSet = new LineDataSet(expenseEntries, "Tổng Chi");
        expenseSet.setColor(Color.parseColor("#F44336")); // Màu đỏ
        expenseSet.setCircleColor(Color.parseColor("#F44336"));
        expenseSet.setLineWidth(2f);
        expenseSet.setCircleRadius(3f);
        expenseSet.setDrawValues(false);

        // Gán bộ định dạng ngày (DateAxisFormatter) cho trục X
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new DateAxisFormatter(dateLabels));

        // Tạo dữ liệu và gán vào biểu đồ
        LineData lineData = new LineData(incomeSet, expenseSet);
        lineChart.setData(lineData);
        lineChart.animateX(1000); // Hiệu ứng
        lineChart.invalidate(); // Vẽ lại
    }

    // --- (13) LỚP NỘI BỘ (INNER CLASS) ĐỂ ĐỊNH DẠNG NGÀY TRỤC X ---
    private class DateAxisFormatter extends ValueFormatter {
        private final List<String> labels;
        private SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        private SimpleDateFormat formatter = new SimpleDateFormat("dd/MM", Locale.getDefault()); // Định dạng "Ngày/Tháng"

        public DateAxisFormatter(List<String> labels) {
            this.labels = labels;
        }

        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            int index = (int) value; // Lấy chỉ số (0, 1, 2...)
            // Đảm bảo chỉ số nằm trong phạm vi
            if (index >= 0 && index < labels.size()) {
                try {
                    // Chuyển "YYYY-MM-DD" thành "DD/MM"
                    return formatter.format(parser.parse(labels.get(index)));
                } catch (ParseException e) {
                    return labels.get(index); // Nếu lỗi, trả về chuỗi gốc
                }
            }
            return ""; // Trả về rỗng nếu ngoài phạm vi
        }
    }
}