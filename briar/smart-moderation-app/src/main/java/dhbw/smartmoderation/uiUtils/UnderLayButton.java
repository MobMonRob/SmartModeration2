package dhbw.smartmoderation.uiUtils;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import androidx.core.content.res.ResourcesCompat;

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.SmartModerationApplicationImpl;

public class UnderLayButton {

    private String text;
    private int imageResId;
    private int color;
    private int position;
    private RectF clickRegion;
    private UnderLayButtonClickListener clickListener;

    public UnderLayButton(String text, int imageResId, int color, UnderLayButtonClickListener clickListener) {
        this.text = text;
        this.imageResId = imageResId;
        this.color = color;
        this.clickListener = clickListener;
    }

    public boolean onClick(float x, float y) {
        if(clickRegion != null && clickRegion.contains(x, y)) {
            clickListener.onClick(this.position);
            return true;
        }
        return false;
    }

    public void onDraw(Canvas canvas, RectF rect, int position) {
        Paint paint = new Paint();
        paint.setColor(color);
        canvas.drawRect(rect, paint);
        paint.setColor(ResourcesCompat.getColor(SmartModerationApplicationImpl.getApp().getApplicationContext().getResources(), R.color.default_color, null));
        paint.setTextSize(30);

        Rect rectangle = new Rect();
        float height = rect.height();
        float width = rect.width();
        paint.setTextAlign(Paint.Align.LEFT);
        paint.getTextBounds(text, 0, text.length(), rectangle);
        float x = width/2f - rectangle.width()/2f - rectangle.left;
        float y = height/2f + rectangle.height()/2f - rectangle.bottom;
        canvas.drawText(text, rect.left + x, rect.top + y, paint);
        this.clickRegion = rect;
        this.position = position;
    }

}