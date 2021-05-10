package dhbw.smartmoderation.uiUtils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import dhbw.smartmoderation.R;

public class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private final ItemTouchHelperAdapter itemTouchHelperAdapter;
    private int color;
    private int icon;
    private int swipeFlag;

    public SimpleItemTouchHelperCallback(ItemTouchHelperAdapter itemTouchHelperAdapter, int color, int icon, int swipeFlag) {

        this.itemTouchHelperAdapter = itemTouchHelperAdapter;
        this.color = color;
        this.icon = icon;
        this.swipeFlag = swipeFlag;
    }

    private DrawCommand createDrawCommand(View viewItem, float dX, int iconResId) {
        Context context = viewItem.getContext();
        Drawable icon = ContextCompat.getDrawable(context, iconResId);
        icon = DrawableCompat.wrap(icon).mutate();
        icon.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.default_color), PorterDuff.Mode.SRC_IN));
        int backgroundColor = getBackgroundColor(this.color, R.color.default_grey, dX, viewItem);
        return new DrawCommand(icon, backgroundColor);
    }

    private int getBackgroundColor(int firstColor, int secondColor, float dX, View viewItem) {

        if(willActionBeTriggered(dX, viewItem.getWidth())) {

            return ContextCompat.getColor(viewItem.getContext(), firstColor);
        }

        else {

            return ContextCompat.getColor(viewItem.getContext(), secondColor);
        }

    }

    private boolean willActionBeTriggered(float dX, int viewWidth) {

        return Math.abs(dX) >= viewWidth/500;
    }

    private void drawBackground(Canvas canvas, View viewItem, float dX, int color) {

        Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(color);
        RectF backgroundRectangle = getBackgroundRectangle(viewItem, dX);
        canvas.drawRect(backgroundRectangle, backgroundPaint);
    }

    private RectF getBackgroundRectangle(View viewItem, float dX) {

        if(this.swipeFlag == ItemTouchHelper.START) {

            return new RectF(new Float(viewItem.getRight()) + dX, new Float(viewItem.getTop()), new Float(viewItem.getRight()), new Float(viewItem.getBottom()));
        }

        return new RectF(new Float(viewItem.getLeft()), new Float(viewItem.getTop()), new Float(viewItem.getLeft() + dX), new Float(viewItem.getBottom()));

    }

    private int calculateTopMargin(Drawable icon, View viewItem) {

        return (viewItem.getHeight() - icon.getIntrinsicHeight())/2;
    }

    private Rect getStartContainerRectangle(View viewItem, int iconWidth, int topMargin, int sideOffset, float dx) {

        if(this.swipeFlag == ItemTouchHelper.START) {

            int leftBound = viewItem.getRight() + (int)dx + sideOffset;
            int rightBound = viewItem.getRight() + (int)dx + sideOffset + iconWidth;
            int topBound = viewItem.getTop() + topMargin;
            int bottomBound = viewItem.getBottom() - topMargin;

            return new Rect(leftBound, topBound, rightBound, bottomBound);

        }

        int leftBound = viewItem.getLeft() + (int)dx - sideOffset - iconWidth;
        int rightBound = viewItem.getLeft() + (int)dx - sideOffset;
        int topBound = viewItem.getTop() + topMargin;
        int bottomBound = viewItem.getBottom() - topMargin;

        return new Rect(leftBound, topBound, rightBound, bottomBound);

    }

    private void drawIcon(Canvas canvas, View viewItem, float dX, Drawable icon) {

        int topMargin = calculateTopMargin(icon, viewItem);
        icon.setBounds(getStartContainerRectangle(viewItem, icon.getIntrinsicWidth(), topMargin, 50, dX));
        icon.draw(canvas);
    }

    private void paintDrawCommand(DrawCommand drawCommand, Canvas canvas, float dX, View viewItem) {
        drawBackground(canvas, viewItem, dX, drawCommand.getBackgroundColor());
        drawIcon(canvas, viewItem, dX, drawCommand.getIcon());
    }

    private void paintCommandToStart(Canvas canvas, View viewItem, int iconResId, float dX) {
        DrawCommand drawCommand = createDrawCommand(viewItem, dX, iconResId);
        paintDrawCommand(drawCommand, canvas, dX, viewItem);
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {

        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlags = this.swipeFlag;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {

        itemTouchHelperAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

        itemTouchHelperAdapter.onItemDismiss(viewHolder.getAdapterPosition());

    }

    @Override
    public void onChildDraw(Canvas canvas, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

        if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

            View viewItem = viewHolder.itemView;
            paintCommandToStart(canvas, viewItem, this.icon, dX);
        }

        super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {

        if(actionState != ItemTouchHelper.ACTION_STATE_IDLE) {

            if(viewHolder instanceof ItemTouchHelperViewHolder) {

                ItemTouchHelperViewHolder itemTouchHelperViewHolder = (ItemTouchHelperViewHolder)viewHolder;
                itemTouchHelperViewHolder.onItemSelected();
            }
        }

        super.onSelectedChanged(viewHolder, actionState);
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {

        super.clearView(recyclerView, viewHolder);

        if(viewHolder instanceof  ItemTouchHelperViewHolder) {
            ItemTouchHelperViewHolder itemTouchHelperViewHolder = (ItemTouchHelperViewHolder)viewHolder;
            itemTouchHelperViewHolder.onItemClear();
        }
    }
}
