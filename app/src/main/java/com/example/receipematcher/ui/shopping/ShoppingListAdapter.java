package com.example.receipematcher.ui.shopping;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.receipematcher.R;
import com.example.receipematcher.data.entities.ShoppingItem;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.VH> {
    private final List<ShoppingItem> items = new ArrayList<>();

    public interface OnChangeQuantityListener { void onChange(ShoppingItem item, int delta); }
    private OnChangeQuantityListener qtyListener;
    public void setOnChangeQuantityListener(OnChangeQuantityListener l) { this.qtyListener = l; }

    public void submit(List<ShoppingItem> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shopping, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int position) {
        ShoppingItem it = items.get(position);
        h.name.setText(it.name == null ? "" : it.name);
        h.qty.setText(String.valueOf(Math.max(0, it.quantity)));
        h.btnMinus.setOnClickListener(v -> { if (qtyListener != null) qtyListener.onChange(it, -1); });
        h.btnPlus.setOnClickListener(v -> { if (qtyListener != null) qtyListener.onChange(it, +1); });
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView name;
        MaterialButton btnMinus;
        MaterialButton btnPlus;
        TextView qty;
        VH(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.textItemName);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            qty = itemView.findViewById(R.id.textQty);
        }
    }
}
