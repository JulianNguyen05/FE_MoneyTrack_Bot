package ht.nguyenhuutrong.fe_moneytrack_bot.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ht.nguyenhuutrong.fe_moneytrack_bot.R;
import ht.nguyenhuutrong.fe_moneytrack_bot.models.Category;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categoryList;

    // --- Constructor ---
    public CategoryAdapter(List<Category> categoryList) {
        this.categoryList = categoryList;
    }

    // --- ViewHolder ---
    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView txtCategory;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCategory = itemView.findViewById(android.R.id.text1); // Dùng layout có sẵn
        }
    }

    // --- Override các phương thức cần thiết ---
    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Dùng layout mặc định có sẵn: simple_list_item_1
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        if (categoryList == null || categoryList.isEmpty()) return;

        Category category = categoryList.get(position);
        holder.txtCategory.setText(category.getName() + " (" + category.getType() + ")");
    }

    @Override
    public int getItemCount() {
        return (categoryList != null) ? categoryList.size() : 0;
    }

    // --- Cập nhật dữ liệu ---
    public void setData(List<Category> categories) {
        this.categoryList = categories;
        notifyDataSetChanged();
    }
}
