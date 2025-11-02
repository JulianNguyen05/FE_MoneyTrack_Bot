package ht.nguyenhuutrong.fe_moneytrack_bot.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ht.nguyenhuutrong.fe_moneytrack_bot.R;
import ht.nguyenhuutrong.fe_moneytrack_bot.models.Transaction;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactionList;

    public TransactionAdapter(List<Transaction> transactionList) {
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);
        holder.category.setText(transaction.getCategoryName());
        holder.amount.setText(String.valueOf(transaction.getAmount()));
        holder.date.setText(transaction.getDate());
    }

    @Override
    public int getItemCount() {
        return transactionList != null ? transactionList.size() : 0;
    }

    // Cập nhật dữ liệu cho Adapter
    public void setData(List<Transaction> newList) {
        this.transactionList = newList;
        notifyDataSetChanged(); // Báo RecyclerView cập nhật lại dữ liệu
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView category, amount, date;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            category = itemView.findViewById(R.id.textViewCategory);
            amount = itemView.findViewById(R.id.textViewAmount);
            date = itemView.findViewById(R.id.textViewDate);
        }
    }
}
