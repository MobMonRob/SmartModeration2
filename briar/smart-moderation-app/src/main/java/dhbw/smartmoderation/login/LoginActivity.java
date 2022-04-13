package dhbw.smartmoderation.login;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.home.HomeActivity;
import dhbw.smartmoderation.util.Util;

/**
 * Activity for logging in with an existing account.
 */
public class LoginActivity extends AppCompatActivity {

    private LoginController controller;

    private EditText edtUsername;
    private EditText edtPassword;
    private Button btnLogin;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle(getString(R.string.login_title));

        controller = new LoginController();

        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);

        TextWatcher inputChangeListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                onInputChange();
            }
        };

        edtUsername.addTextChangedListener(inputChangeListener);
        edtPassword.addTextChangedListener(inputChangeListener);
        btnLogin.setOnClickListener(this::onLogin);
    }

    public void onInputChange() {
        btnLogin.setEnabled(!Util.isEmpty(edtUsername) && !Util.isEmpty(edtPassword));
    }

    public void onLogin(View v) {
        LoginAsyncTask loginAsyncTask = new LoginAsyncTask();
        loginAsyncTask.execute(Util.getText(edtPassword));
    }

    @SuppressLint("StaticFieldLeak")
    public class LoginAsyncTask extends AsyncTask<String, String, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(LoginActivity.this, R.style.MyAlertDialogStyle);
            progressDialog.setMessage(getString(R.string.login));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }


        @Override
        protected Boolean doInBackground(String... strings) {
            String password = strings[0];
            return controller.login(password);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            progressDialog.dismiss();
            if (aBoolean) {
                Intent homeIntent = new Intent(LoginActivity.this, HomeActivity.class);
                startActivity(homeIntent);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setMessage(getString(R.string.wrongPassword));
                builder.setCancelable(false);
                builder.setNeutralButton(R.string.ok, (dialog, which) -> dialog.cancel());
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        }
    }

}
