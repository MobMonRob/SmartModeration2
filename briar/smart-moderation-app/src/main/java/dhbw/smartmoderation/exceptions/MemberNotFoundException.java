package dhbw.smartmoderation.exceptions;

import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.annotation.Nullable;

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.group.personInfo.AddContactActivity;

public class MemberNotFoundException extends SmartModerationException{

    @Nullable
    @Override
    public String getMessage(Context context) {
        return context.getString(R.string.MemberNotFoundException_Message);
    }

    @Override
    public boolean hasAction() {
        return true;
    }

    @Override
    public View.OnClickListener getAction(Context context, AlertDialog popup) {
        return (v -> {
            Intent addContactIntent = new Intent(context, AddContactActivity.class);
            popup.cancel();
            context.startActivity(addContactIntent);
        });
    }

    @Override
    public String getActionName(Context context) {
        return context.getString(R.string.MemberNotFoundException_Action);
    }
}
