// trong ht.nguyenhuutrong.fe_moneytrack_bot.adapters/BudgetAdapter.java
package ht.nguyenhuutrong.fe_moneytrack_bot.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import ht.nguyenhuutrong.fe_moneytrack_bot.R;
import ht.nguyenhuutrong.fe_moneytrack_bot.models.Budget;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {

    // (1) Tạo 1 class nội bộ (inner class) để chứa dữ liệu đã "gộp"
    public static class BudgetStatus {
        public Budget budget;
        public double spentAmount; // Số tiền đã tiêu (từ API Report)

        public BudgetStatus(Budget budget, double spentAmount) {
            this.budget = budget;
            this.spentAmount = spentAmount;
        }
    }

    // (2) Adapter sẽ dùng List<BudgetStatus>
    private List<BudgetStatus> budgetStatusList;
    private NumberFormat currencyFormatter;

    public BudgetAdapter(List<BudgetStatus> budgetStatusList) {
        this.budgetStatusList = budgetStatusList;
        // Dùng Locale của Việt Nam để định dạng tiền tệ
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_budget, parent, false);
        return new BudgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        BudgetStatus status = budgetStatusList.get(position);
        Budget budget = status.budget;
        double spent = status.spentAmount;
        double total = budget.getAmount();

        holder.categoryName.setText(budget.getCategoryDetails().getName());

        // Tính toán
        double remaining = total - spent;
        int progress = (total > 0) ? (int) ((spent / total) * 100) : 0;

        // Cập nhật ProgressBar
        holder.progressBar.setProgress(progress);

        // Cập nhật Text
        holder.budgetProgress.setText(
                "Đã tiêu: " + currencyFormatter.format(spent) + " / " + currencyFormatter.format(total)
        );

        holder.remaining.setText("Còn lại: " + currencyFormatter.format(remaining));

        // Cảnh báo nếu tiêu vượt mức
        if (remaining < 0) {
            holder.remaining.setTextColor(Color.RED);
            holder.progressBar.getProgressDrawable().setColorFilter(Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
        } else {
            holder.remaining.setTextColor(Color.GRAY);
            holder.progressBar.getProgressDrawable().setColorFilter(Color.parseColor("#3498DB"), android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }

    @Override
    public int getItemCount() {
        return budgetStatusList.size();
    }

    // (3) Hàm setData
    public void setData(List<BudgetStatus> newList) {
        this.budgetStatusList = newList;
        notifyDataSetChanged();
    }


    static class BudgetViewHolder extends RecyclerView.ViewHolder {
        TextView categoryName, budgetProgress, remaining;
        ProgressBar progressBar;

        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.textViewCategoryName);
            budgetProgress = itemView.findViewById(R.id.textViewBudgetProgress);
            remaining = itemView.findViewById(R.id.textViewRemaining);
            progressBar = itemView.findViewById(R.id.progressBarBudget);
        }
    }
}