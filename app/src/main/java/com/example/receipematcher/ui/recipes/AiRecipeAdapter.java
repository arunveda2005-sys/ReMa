package com.example.receipematcher.ui.recipes;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.receipematcher.R;
import com.example.receipematcher.data.repository.RecipeRepository.AiRecipe;
import com.example.receipematcher.ui.components.CircularProgressIndicator;
import com.example.receipematcher.utils.RecipeMatchCalculator;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Random;

public class AiRecipeAdapter extends ListAdapter<AiRecipe, AiRecipeAdapter.AiVH> {

    public AiRecipeAdapter() {
        super(DIFF);
    }

    public interface OnItemClickListener {
        void onClick(AiRecipe recipe);
    }

    private OnItemClickListener listener;
    public void setOnItemClickListener(OnItemClickListener l) { this.listener = l; }

    private List<String> pantryTerms;
    public void setPantryTerms(List<String> terms) { this.pantryTerms = terms; }

    public interface OnFavoriteToggleListener {
        void onToggle(com.example.receipematcher.data.repository.RecipeRepository.AiRecipe recipe, boolean toFavorite);
    }
    private OnFavoriteToggleListener favListener;
    public void setOnFavoriteToggleListener(OnFavoriteToggleListener l) { this.favListener = l; }

    private java.util.Set<String> favoriteIds = new java.util.HashSet<>();
    public void setFavoriteIds(java.util.Set<String> ids) {
        favoriteIds = (ids == null) ? new java.util.HashSet<>() : new java.util.HashSet<>(ids);
        notifyDataSetChanged();
    }

    private static final DiffUtil.ItemCallback<AiRecipe> DIFF = new DiffUtil.ItemCallback<AiRecipe>() {
        @Override
        public boolean areItemsTheSame(@NonNull AiRecipe oldItem, @NonNull AiRecipe newItem) {
            // Use title as identity proxy (Gemini results don't have IDs)
            return safe(oldItem.name).equalsIgnoreCase(safe(newItem.name));
        }

        @Override
        public boolean areContentsTheSame(@NonNull AiRecipe oldItem, @NonNull AiRecipe newItem) {
            return safe(oldItem.name).equals(newItem.name)
                    && join(oldItem.ingredients).equals(join(newItem.ingredients))
                    && join(oldItem.steps).equals(join(newItem.steps));
        }

        private String join(List<String> list) {
            return list == null ? "" : TextUtils.join("|", list);
        }

        private String safe(String s) { return s == null ? "" : s; }
    };

    @NonNull
    @Override
    public AiVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ai_recipe, parent, false);
        return new AiVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AiVH holder, int position) {
        AiRecipe r = getItem(position);
        holder.title.setText(r.name == null ? "Recipe" : r.name);

        // Calculate match score using RecipeMatchCalculator
        float matchPercentage = RecipeMatchCalculator.calculateMatchPercentage(r, pantryTerms);
        holder.matchScoreIndicator.setProgress(matchPercentage);

        // Show missing ingredients count
        List<String> missingIngredients = RecipeMatchCalculator.getMissingIngredients(r, pantryTerms);
        if (missingIngredients.size() > 0) {
            holder.missingIngredientsContainer.setVisibility(View.VISIBLE);
            String missingText = "Need " + missingIngredients.size() + " ingredient" + 
                (missingIngredients.size() == 1 ? "" : "s");
            holder.textMissingIngredients.setText(missingText);
        } else {
            holder.missingIngredientsContainer.setVisibility(View.GONE);
        }

        // Set recipe metadata from enhanced data model
        holder.textCookingTime.setText(r.cookingTimeMinutes + " min");
        holder.textDifficulty.setText(r.difficulty);
        holder.textRating.setText(String.format("%.1f", r.rating));

        // Show category badges based on recipe characteristics
        showCategoryBadges(holder, r);

        if (r.ingredients != null && !r.ingredients.isEmpty()) {
            String ing = "Ingredients: " + TextUtils.join(", ", r.ingredients);
            holder.ingredients.setText(ing);
        } else {
            holder.ingredients.setText("");
        }

        // Only the explicit button navigates to details
        holder.btnViewRecipe.setOnClickListener(v -> {
            if (listener != null) listener.onClick(r);
        });

        // Favorite button state and toggle
        String rid = recipeIdFor(r);
        boolean isFav = favoriteIds.contains(rid);
        holder.btnFavorite.setIconResource(isFav ? R.drawable.ic_star_filled : R.drawable.ic_star);
        holder.btnFavorite.setIconTintResource(isFav ? R.color.primary : R.color.text_hint);
        holder.btnFavorite.setOnClickListener(v -> {
            boolean nowFav = !favoriteIds.contains(rid);
            if (nowFav) favoriteIds.add(rid); else favoriteIds.remove(rid);
            notifyItemChanged(holder.getBindingAdapterPosition());
            if (favListener != null) favListener.onToggle(r, nowFav);
        });
    }

    private int matchCount(AiRecipe r) {
        if (r == null || r.ingredients == null || pantryTerms == null) return 0;
        int c = 0;
        for (String ing : r.ingredients) {
            String s = ing == null ? "" : ing.toLowerCase();
            boolean found = false;
            for (String p : pantryTerms) {
                if (p != null && !p.isEmpty() && s.contains(p)) { found = true; break; }
            }
            if (found) c++;
        }
        return c;
    }

    private void showCategoryBadges(AiVH holder, AiRecipe recipe) {
        // Hide all badges initially
        holder.badgeQuick.setVisibility(View.GONE);
        holder.badgeHealthy.setVisibility(View.GONE);
        holder.badgePopular.setVisibility(View.GONE);
        holder.badgeTrending.setVisibility(View.GONE);
        holder.categoryBadgesContainer.setVisibility(View.GONE);

        boolean hasVisibleBadge = false;

        // Show badges based on recipe metadata
        if (recipe.isQuick) {
            holder.badgeQuick.setVisibility(View.VISIBLE);
            hasVisibleBadge = true;
        }

        if (recipe.isHealthy) {
            holder.badgeHealthy.setVisibility(View.VISIBLE);
            hasVisibleBadge = true;
        }

        if (recipe.isPopular) {
            holder.badgePopular.setVisibility(View.VISIBLE);
            hasVisibleBadge = true;
        }

        if (recipe.isTrending) {
            holder.badgeTrending.setVisibility(View.VISIBLE);
            hasVisibleBadge = true;
        }

        if (hasVisibleBadge) {
            holder.categoryBadgesContainer.setVisibility(View.VISIBLE);
        }
    }



    static class AiVH extends RecyclerView.ViewHolder {
        TextView title;
        TextView ingredients;
        TextView textCookingTime;
        TextView textDifficulty;
        TextView textRating;
        TextView textMissingIngredients;
        TextView badgeQuick;
        TextView badgeHealthy;
        TextView badgePopular;
        TextView badgeTrending;
        LinearLayout categoryBadgesContainer;
        LinearLayout missingIngredientsContainer;
        CircularProgressIndicator matchScoreIndicator;
        MaterialButton btnViewRecipe;
        MaterialButton btnFavorite;
        
        AiVH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.textTitle);
            ingredients = itemView.findViewById(R.id.textIngredients);
            textCookingTime = itemView.findViewById(R.id.textCookingTime);
            textDifficulty = itemView.findViewById(R.id.textDifficulty);
            textRating = itemView.findViewById(R.id.textRating);
            textMissingIngredients = itemView.findViewById(R.id.textMissingIngredients);
            badgeQuick = itemView.findViewById(R.id.badgeQuick);
            badgeHealthy = itemView.findViewById(R.id.badgeHealthy);
            badgePopular = itemView.findViewById(R.id.badgePopular);
            badgeTrending = itemView.findViewById(R.id.badgeTrending);
            categoryBadgesContainer = itemView.findViewById(R.id.categoryBadgesContainer);
            missingIngredientsContainer = itemView.findViewById(R.id.missingIngredientsContainer);
            matchScoreIndicator = itemView.findViewById(R.id.matchScoreIndicator);
            btnViewRecipe = itemView.findViewById(R.id.btnViewRecipe);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }
    }

    private static String recipeIdFor(AiRecipe r) {
        String name = (r == null || r.name == null) ? "" : r.name.trim().toLowerCase();
        return name;
    }
}
