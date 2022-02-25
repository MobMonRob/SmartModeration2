package dhbw.smartmoderation.exceptions;

import android.content.Context;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

public class CantCreateModerationCardException extends SmartModerationException {
    @Override
    public String getMessage(Context context) {
        //todo
        return "Moderation Card couldn't be created";
    }

    @Override
    public boolean hasAction() {
        return false;
    }

    @Override
    public View.OnClickListener getAction(Context context, AlertDialog popup) {
        return null;
    }

    @Override
    public String getActionName(Context context) {
        return "";
    }
}
