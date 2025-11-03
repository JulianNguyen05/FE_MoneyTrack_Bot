package ht.nguyenhuutrong.fe_moneytrack_bot.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ht.nguyenhuutrong.fe_moneytrack_bot.R;
import ht.nguyenhuutrong.fe_moneytrack_bot.models.Transaction;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactionList;
    private TransactionClickListener listener;

    // --- (1) Interface bắt sự kiện ---
    public interface TransactionClickListener {
        void onEditClick(Transaction transaction);
        void onDeleteClick(Transaction transaction);
    }

    // --- (2) Constructor có listener ---
    public TransactionAdapter(List<Transaction> transactionList, TransactionClickListener listener) {
        this.transactionList = transactionList;
        this.listener = listener;
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
        if (transaction == null) return;

        holder.category.setText(transaction.getCategoryName());
        holder.amount.setText(String.valueOf(transaction.getAmount()));
        holder.date.setText(transaction.getDate());

        // --- (3) Gán sự kiện cho nút ---
        holder.buttonEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEditClick(transaction);
        });

        holder.buttonDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(transaction);
        });
    }

    @Override
    public int getItemCount() {
        return (transactionList != null) ? transactionList.size() : 0;
    }

    // --- (4) Hàm cập nhật dữ liệu ---
    public void setData(List<Transaction> newList) {
        this.transactionList = newList;
        notifyDataSetChanged();
    }

    // --- (5) ViewHolder ---
    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView category, amount, date;
        Button buttonEdit, buttonDelete;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            category = itemView.findViewById(R.id.textViewCategory);
            amount = itemView.findViewById(R.id.textViewAmount);
            date = itemView.findViewById(R.id.textViewDate);
            buttonEdit = itemView.findViewById(R.id.buttonEdit);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}
