package dhbw.smartmoderation.exceptions;

import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.view.View;

import com.google.android.material.button.MaterialButton;

import java.util.function.Function;

public abstract class SmartModerationException extends Exception {

    public SmartModerationException(){
        super();
    }

    public SmartModerationException(String message){
        super(message);
    }
    public abstract String getMessage(Context context);

    public abstract boolean hasAction();

    public abstract View.OnClickListener getAction(Context context, AlertDialog popup);

    public abstract String getActionName(Context context);

}
