package dhbw.smartmoderation.exceptions;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.account.contactexchange.ContactExchangeActivity;

public class NoContactsFoundException extends SmartModerationException {
    @Override
    public String getMessage(Context context) {
        return context.getString(R.string.NoContactsFoundException_Message);
    }

    @Override
    public boolean hasAction() {
        return true;
    }

    @Override
    public View.OnClickListener getAction(Context context, AlertDialog popup) {
        return (v -> {
            Intent addContactIntent = new Intent(context, ContactExchangeActivity.class);
            context.startActivity(addContactIntent);
            popup.cancel();
        });
    }

    @Override
    public String getActionName(Context context) {
        return context.getString(R.string.NoContactsFoundException_Action);
    }
}
