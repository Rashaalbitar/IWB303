package com.example.iwb303;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PurchaseAdapter extends RecyclerView.Adapter<PurchaseAdapter.PurchaseViewHolder> {

    private List<Purchase> purchaseList;
    private OnItemMenuClickListener menuClickListener;

    public interface OnItemMenuClickListener {
        void onItemMenuClick(Purchase purchase, View view);
    }

    public PurchaseAdapter(List<Purchase> purchaseList, OnItemMenuClickListener menuClickListener) {
        this.purchaseList = purchaseList;
        this.menuClickListener = menuClickListener;
    }

    @NonNull
    @Override
    public PurchaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_purchase, parent, false);
        return new PurchaseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PurchaseViewHolder holder, int position) {
        Purchase purchase = purchaseList.get(position);
        if (purchase != null) {
            holder.tvDate.setText(purchase.getDate() != null ? purchase.getDate() : "");
            holder.tvTotal.setText(String.format(java.util.Locale.US, "%.2f", purchase.getTotalCost()));
            holder.tvCategory.setText(purchase.getCategoryName() != null ? purchase.getCategoryName() : "-");
            holder.tvItemName.setText(purchase.getItemName() != null ? purchase.getItemName() : "");

            holder.btnMenu.setOnClickListener(v -> {
                if (menuClickListener != null) {
                    menuClickListener.onItemMenuClick(purchase, v);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return purchaseList.size();
    }

    public void updateData(List<Purchase> newList) {
        this.purchaseList = newList;
        notifyDataSetChanged();
    }

    static class PurchaseViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvTotal, tvCategory, tvItemName;
        android.widget.ImageButton btnMenu;

        public PurchaseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTotal = itemView.findViewById(R.id.tvTotalCost);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            btnMenu = itemView.findViewById(R.id.btnItemMenu);
        }
    }
}
