package com.example.receipematcher.ui.favourites;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.receipematcher.R;
import com.example.receipematcher.data.entities.Favourite;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class FavouritesListAdapter extends RecyclerView.Adapter<FavouritesListAdapter.VH> {
    private final List<Favourite> items = new ArrayList<>();
    private OnUnfavoriteListener listener;
    public interface OnItemClickListener { void onClick(Favourite f); }
    private OnItemClickListener clickListener;
    public interface OnUnfavoriteListener { void onUnfavorite(String recipeId); }
    public void setOnUnfavoriteListener(OnUnfavoriteListener l) { this.listener = l; }
    public void setOnItemClickListener(OnItemClickListener l) { this.clickListener = l; }

    public void submit(List<Favourite> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favourite, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int position) {
        Favourite f = items.get(position);
        String name = f.recipeId == null ? "" : f.recipeId;
        h.title.setText(name);
        h.btnRemove.setOnClickListener(v -> { if (listener != null && f.recipeId != null) listener.onUnfavorite(f.recipeId); });
        h.itemView.setOnClickListener(v -> { if (clickListener != null) clickListener.onClick(f); });
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView title;
        MaterialButton btnRemove;
        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.textFavTitle);
            btnRemove = itemView.findViewById(R.id.btnRemoveFav);
        }
    }
}
