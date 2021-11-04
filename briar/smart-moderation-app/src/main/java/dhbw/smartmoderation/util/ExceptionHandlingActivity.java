package dhbw.smartmoderation.util;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;


import dhbw.smartmoderation.R;
import dhbw.smartmoderation.SmartModerationApplication;
import dhbw.smartmoderation.exceptions.SmartModerationException;

public abstract class ExceptionHandlingActivity extends AppCompatActivity {
    private View exceptionPopup;
    private TextView message;
    private MaterialButton okButton;
    private MaterialButton exceptionActionButton;

    private SmartModerationApplication app;

    @Override
    protected void onResume() {
        super.onResume();
        app = (SmartModerationApplication) SmartModerationApplication.getApp();
        app.setCurrentActivity(this);
    }

    public void handleException(Exception exception){
        if (exception instanceof SmartModerationException){
            smartModerationExceptionHandling((SmartModerationException) exception);
        }else{
            defaultExceptionHandling(exception);
        }
    }

    private void smartModerationExceptionHandling(SmartModerationException exception) {
        AlertDialog exceptionAlertDialog = defaultExceptionHandling(exception);
        message.setText(exception.getMessage(this));
        if(exception.hasAction()){
            exceptionActionButton.setVisibility(View.VISIBLE);
            exceptionActionButton.setOnClickListener(exception.getAction(this,exceptionAlertDialog));
            exceptionActionButton.setText(exception.getActionName(this));
        }

    }


    private AlertDialog defaultExceptionHandling(Exception exception) {
        LayoutInflater inflater = LayoutInflater.from(this);
        exceptionPopup = inflater.inflate(R.layout.popup_exceptions, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(exceptionPopup);

        alertDialogBuilder.setCancelable(false);

        AlertDialog exceptionAlertDialog = alertDialogBuilder.create();

        message = exceptionPopup.findViewById(R.id.exceptionMessage);
        message.setText(exception.getMessage() != null ? exception.getMessage() : "Keine Fehlermeldung");
        okButton = exceptionPopup.findViewById(R.id.okButton);
        exceptionActionButton = exceptionPopup.findViewById(R.id.exceptionActionButton);

        okButton.setOnClickListener(v -> {
            exceptionAlertDialog.cancel();
        });

        exceptionActionButton.setVisibility(View.GONE);
        exceptionAlertDialog.show();
        return exceptionAlertDialog;
    }

}
