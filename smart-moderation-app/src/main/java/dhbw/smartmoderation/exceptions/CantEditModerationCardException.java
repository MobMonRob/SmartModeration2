package dhbw.smartmoderation.exceptions;

import android.content.Context;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

public class CantEditModerationCardException extends SmartModerationException {
    @Override
    public String getMessage(Context context) {
        //todo
        return "Moderation card couldn't be edited";
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
