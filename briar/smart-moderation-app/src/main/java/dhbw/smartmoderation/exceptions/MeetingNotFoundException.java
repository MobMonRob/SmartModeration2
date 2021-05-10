package dhbw.smartmoderation.exceptions;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.group.create.CreateGroup;

public class MeetingNotFoundException extends SmartModerationException {

    public MeetingNotFoundException(){

        super("Meeting was not found");

    }

    @Override
    public String getMessage(Context context) {
        return context.getString(R.string.MeetingNotFoundException_Message);
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
