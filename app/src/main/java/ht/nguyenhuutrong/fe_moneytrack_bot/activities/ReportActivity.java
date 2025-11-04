package ht.nguyenhuutrong.fe_moneytrack_bot.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        pieChart = findViewById(R.id.pieChart);

        tokenManager = new TokenManager(this);
        String token = tokenManager.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        authToken = "Bearer " + token;
        apiService = RetrofitClient.getClient().create(ApiService.class);

        setupPieChart();
        loadReportData();
    }

    private void setupPieChart() {
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
        // --- 💡 FIX Ở ĐÂY: Đã xóa 2 tham số 'null' bị dư ---
        apiService.getReportSummary(authToken, null, null).enqueue(new Callback<List<ReportEntry>>() {
            @Override
            public void onResponse(Call<List<ReportEntry>> call, Response<List<ReportEntry>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ReportEntry> reportList = response.body();
                    if (reportList.isEmpty()) {
                        Toast.makeText(ReportActivity.this, "Không có dữ liệu chi tiêu!", Toast.LENGTH_SHORT).show();
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
        ArrayList<PieEntry> entries = new ArrayList<>();

        for (ReportEntry entry : reportData) {
            if (entry.getTotalAmount() > 0) {
                // Chúng ta truyền Tên (label) vào đây
                entries.add(new PieEntry((float) entry.getTotalAmount(), entry.getCategoryName()));
            }
        }

        if (entries.isEmpty()) {
            pieChart.setCenterText("Không có dữ liệu hợp lệ");
            pieChart.invalidate();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, ""); // Label dataset rỗng là đúng

        // --- (1) CẤU HÌNH MÀU SẮC ---
        // Thêm nhiều màu hơn cho đẹp
        ArrayList<Integer> colors = new ArrayList<>();
        for (int c : ColorTemplate.VORDIPLOM_COLORS) colors.add(c);
        for (int c : ColorTemplate.JOYFUL_COLORS) colors.add(c);
        for (int c : ColorTemplate.COLORFUL_COLORS) colors.add(c);
        for (int c : ColorTemplate.LIBERTY_COLORS) colors.add(c);
        for (int c : ColorTemplate.PASTEL_COLORS) colors.add(c);
        colors.add(ColorTemplate.getHoloBlue());
        dataSet.setColors(colors);

        // --- (2) CẤU HÌNH HIỂN THỊ GIÁ TRỊ (%) BÊN NGOÀI ---
        dataSet.setDrawValues(true); // Hiển thị giá trị
        dataSet.setValueFormatter(new PercentFormatter(pieChart)); // Dùng %
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE); // Đặt bên ngoài

        // Cài đặt đường kẻ (lines)
        dataSet.setValueLinePart1OffsetPercentage(100.f);
        dataSet.setValueLinePart1Length(0.4f); // <-- Trả lại độ dài
        dataSet.setValueLinePart2Length(0.4f); // <-- Trả lại độ dài
        dataSet.setValueLineColor(Color.GRAY); // Màu đường kẻ

        // --- (3) TẠO DỮ LIỆU ---
        PieData pieData = new PieData(dataSet);
        pieData.setDrawValues(true);

        pieChart.setData(pieData);
        pieChart.animateY(1000); // Thêm hiệu ứng
        pieChart.invalidate(); // Vẽ lại
    }
}