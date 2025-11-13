package com.example.receipematcher.ui.pantry;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.receipematcher.R;
import com.example.receipematcher.data.entities.Ingredient;

/**
 * ItemTouchHelper callback for handling swipe gestures on pantry items
 */
public class PantrySwipeCallback extends ItemTouchHelper.SimpleCallback {

    public interface SwipeActionListener {
        void onSwipeEdit(Ingredient ingredient);
        void onSwipeDelete(Ingredient ingredient);
    }

    private final PantryAdapter adapter;
    private final SwipeActionListener listener;
    private final Context context;
    private final Paint paint;

    public PantrySwipeCallback(Context context, PantryAdapter adapter, SwipeActionListener listener) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.context = context;
        this.adapter = adapter;
        this.listener = listener;
        this.paint = new Paint();
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false; // We don't support move operations
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        if (position != RecyclerView.NO_POSITION) {
            Ingredient ingredient = adapter.getCurrentList().get(position);
            
            if (direction == ItemTouchHelper.RIGHT) {
                // Swipe right for edit
                listener.onSwipeEdit(ingredient);
            } else if (direction == ItemTouchHelper.LEFT) {
                // Swipe left for delete
                listener.onSwipeDelete(ingredient);
            }
        }
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, 
                           @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, 
                           int actionState, boolean isCurrentlyActive) {
        
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            drawSwipeBackground(c, viewHolder, dX);
        }
        
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    private void drawSwipeBackground(Canvas canvas, RecyclerView.ViewHolder viewHolder, float dX) {
        RectF background = new RectF();
        RectF iconBounds = new RectF();
        
        float itemTop = viewHolder.itemView.getTop();
        float itemBottom = viewHolder.itemView.getBottom();
        float itemLeft = viewHolder.itemView.getLeft();
        float itemRight = viewHolder.itemView.getRight();
        
        if (dX > 0) {
            // Swiping right - Edit action
            background.set(itemLeft, itemTop, itemLeft + dX, itemBottom);
            paint.setColor(ContextCompat.getColor(context, R.color.primary_light));
            canvas.drawRect(background, paint);
            
            // Draw edit icon
            Drawable editIcon = ContextCompat.getDrawable(context, R.drawable.ic_edit);
            if (editIcon != null) {
                int iconSize = 64;
                int iconMargin = 32;
                int iconTop = (int) (itemTop + (itemBottom - itemTop - iconSize) / 2);
                int iconLeft = (int) (itemLeft + iconMargin);
                int iconRight = iconLeft + iconSize;
                int iconBottom = iconTop + iconSize;
                
                editIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                editIcon.setTint(Color.WHITE);
                editIcon.draw(canvas);
            }
            
        } else if (dX < 0) {
            // Swiping left - Delete action
            background.set(itemRight + dX, itemTop, itemRight, itemBottom);
            paint.setColor(ContextCompat.getColor(context, R.color.error));
            canvas.drawRect(background, paint);
            
            // Draw delete icon
            Drawable deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete);
            if (deleteIcon != null) {
                int iconSize = 64;
                int iconMargin = 32;
                int iconTop = (int) (itemTop + (itemBottom - itemTop - iconSize) / 2);
                int iconRight = (int) (itemRight - iconMargin);
                int iconLeft = iconRight - iconSize;
                int iconBottom = iconTop + iconSize;
                
                deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                deleteIcon.setTint(Color.WHITE);
                deleteIcon.draw(canvas);
            }
        }
    }

    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        return 0.3f; // 30% swipe threshold
    }

    @Override
    public float getSwipeEscapeVelocity(float defaultValue) {
        return defaultValue * 1.5f; // Make it easier to trigger swipe
    }
}