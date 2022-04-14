package dhbw.smartmoderation.exceptions;

import android.content.Context;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import dhbw.smartmoderation.R;

public class ParitcipationCantBeChangedException extends SmartModerationException {
    @Override
    public String getMessage(Context context) {
        return context.getString(R.string.ParitcipationCouldNotBeChangedException_Message);
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
