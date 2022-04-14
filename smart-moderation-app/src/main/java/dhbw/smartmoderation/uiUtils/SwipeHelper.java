package dhbw.smartmoderation.uiUtils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public abstract class SwipeHelper extends ItemTouchHelper.SimpleCallback {

    public static final int BUTTON_WIDTH = 200;
    private RecyclerView recyclerView;
    private List<UnderLayButton> underLayButtons;
    private GestureDetector gestureDetector;
    private int swipedPosition = -1;
    private float swipeThreshold = 0.5f;
    private Map<Integer, List<UnderLayButton>> buttonsBuffer;
    private Queue<Integer> recoverQueue;

    private GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            for(UnderLayButton underLayButton : underLayButtons) {
                if(underLayButton.onClick(e.getX(), e.getY())) {
                    break;
                }
            }

            return true;
        }
    };

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            if(swipedPosition < 0) {
                return false;
            }

            Point point = new Point((int) event.getRawX(), (int)event.getRawY());
            RecyclerView.ViewHolder swipedViewHolder = recyclerView.findViewHolderForAdapterPosition(swipedPosition);
            View swipedItem = swipedViewHolder.itemView;
            Rect rect = new Rect();
            swipedItem.getGlobalVisibleRect(rect);

            if(event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_MOVE) {

                if(rect.top < point.y && rect.bottom > point.y) {
                    gestureDetector.onTouchEvent(event);
                }

                else {
                    recoverQueue.add(swipedPosition);
                    swipedPosition = -1;
                    recoverSwipedItem();
                }
            }
            return false;
        }
    };

    public SwipeHelper(Context context, RecyclerView recyclerView) {
        super(0, ItemTouchHelper.LEFT);
        this.recyclerView = recyclerView;
        this.underLayButtons = new ArrayList<>();
        this.gestureDetector = new GestureDetector(context, gestureListener);
        this.recyclerView.setOnTouchListener(onTouchListener);
        buttonsBuffer = new HashMap<>();
        recoverQueue = new LinkedList<Integer>() {
            @Override
            public boolean add(Integer integer) {
                if(contains(integer)) {
                    return false;
                }
                return super.add(0);
            }
        };

        attachSwipe();
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();

        if(swipedPosition != position) {
            recoverQueue.add(swipedPosition);
        }
        swipedPosition = position;

        if(buttonsBuffer.containsKey(swipedPosition)) {
            underLayButtons = buttonsBuffer.get(swipedPosition);
        }

        else {
            underLayButtons.clear();
        }
        buttonsBuffer.clear();
        swipeThreshold = 0.5f * underLayButtons.size() * BUTTON_WIDTH;
        recoverSwipedItem();
    }

    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        return swipeThreshold;
    }

    @Override
    public float getSwipeEscapeVelocity(float defaultValue) {
        return 0.1f * defaultValue;
    }

    @Override
    public float getSwipeVelocityThreshold(float defaultValue) {
        return 5.0f * defaultValue;
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        int position = viewHolder.getAdapterPosition();
        float translationX = dX;
        View itemView = viewHolder.itemView;

        if(position < 0) {
            swipedPosition = position;
            return;
        }

        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

            if(dX < 0) {
                List<UnderLayButton> buffer = new ArrayList<>();

                if(!buttonsBuffer.containsKey(position)) {
                    instantiateUnderLayButton(viewHolder, buffer);
                    buttonsBuffer.put(position, buffer);
                }

                else {
                    buffer = buttonsBuffer.get(position);
                }
                translationX = dX * buffer.size() * BUTTON_WIDTH/itemView.getWidth();
                drawButtons(c, itemView, buffer, position, translationX);
            }
        }

        super.onChildDraw(c, recyclerView, viewHolder, translationX, dY, actionState, isCurrentlyActive);
    }

    private synchronized void recoverSwipedItem() {

        while(!recoverQueue.isEmpty()) {
            int position = recoverQueue.poll();

            if(position > -1) {
                recyclerView.getAdapter().notifyItemChanged(position);
            }
        }
    }

    private void drawButtons(Canvas c, View itemView, List<UnderLayButton> buffer, int position, float dX) {
        float right = itemView.getRight();
        float dButtonWidth = (-1) * dX/buffer.size();

        for(UnderLayButton button : buffer) {
            float left = right - dButtonWidth;
            button.onDraw(c, new RectF(left, itemView.getTop(), right, itemView.getBottom()), position);
            right = left;

        }
    }

    public void attachSwipe() {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(this);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    public abstract void instantiateUnderLayButton(RecyclerView.ViewHolder viewHolder, List<UnderLayButton> underLayButtons);
}
