package dhbw.smartmoderation.uiUtils;
import android.graphics.drawable.Drawable;


public class DrawCommand {

    private Drawable icon;
    private int backgroundColor;

    public DrawCommand(Drawable icon, int backgroundColor) {
        this.icon = icon;
        this.backgroundColor = backgroundColor;
    }

    public int getBackgroundColor() { return this.backgroundColor; }

    public Drawable getIcon() { return this.icon; }
}
