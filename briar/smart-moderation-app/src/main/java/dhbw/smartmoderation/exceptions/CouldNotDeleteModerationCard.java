package dhbw.smartmoderation.exceptions;

import android.content.Context;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import dhbw.smartmoderation.R;

public class CouldNotDeleteModerationCard extends SmartModerationException {
    @Override
    public String getMessage(Context context) {
        //todo: i18n
        return "Couldn't delete moderation card";
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
        return null;
    }
}
