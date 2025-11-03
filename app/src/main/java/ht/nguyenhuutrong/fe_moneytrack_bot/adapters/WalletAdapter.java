package ht.nguyenhuutrong.fe_moneytrack_bot.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import ht.nguyenhuutrong.fe_moneytrack_bot.models.Wallet;

public class WalletAdapter extends RecyclerView.Adapter<WalletAdapter.WalletViewHolder> {

    private List<Wallet> walletList;

    @NonNull
    @Override
    public WalletViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new WalletViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WalletViewHolder holder, int position) {
        if (walletList == null || walletList.get(position) == null) return;

        Wallet wallet = walletList.get(position);
        holder.text1.setText(wallet.getName());

        // Format số dư, ví dụ 1000000 -> 1,000,000 đ
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
        holder.text2.setText("Số dư: " + formatter.format(wallet.getBalance()) + " đ");
    }

    @Override
    public int getItemCount() {
        return (walletList != null) ? walletList.size() : 0;
    }

    public void setData(List<Wallet> wallets) {
        this.walletList = wallets;
        notifyDataSetChanged();
    }

    static class WalletViewHolder extends RecyclerView.ViewHolder {
        TextView text1, text2;
        public WalletViewHolder(@NonNull View itemView) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);
        }
    }
}
