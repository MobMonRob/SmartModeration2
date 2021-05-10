package dhbw.smartmoderation.account.create;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import dhbw.smartmoderation.R;
import dhbw.smartmoderation.home.HomeActivity;
import dhbw.smartmoderation.util.Util;

/**
 * Activity for creating an account.
 */
public class CreateAccountActivity extends AppCompatActivity {

	private CreateAccountController controller;

	private EditText edtUsername;
	private EditText edtPassword;
	private Button btnCreate;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_account);
		setTitle(getString(R.string.CreateAccount_title));

		controller = new CreateAccountController();

		edtUsername = findViewById(R.id.edtUsername);
		edtPassword = findViewById(R.id.edtPassword);
		btnCreate = findViewById(R.id.btnCreate);

		TextWatcher inputChangeListener = new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i,
					int i1, int i2) {
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1,
					int i2) {
			}

			@Override
			public void afterTextChanged(Editable editable) {
				onInputChange();
			}
		};

		edtUsername.addTextChangedListener(inputChangeListener);
		edtPassword.addTextChangedListener(inputChangeListener);

		btnCreate.setOnClickListener(this::onCreate);
	}

	public void onInputChange() {
		if (!Util.isEmpty(edtUsername) && !Util.isEmpty(edtPassword)) {
			btnCreate.setEnabled(true);
		} else {
			btnCreate.setEnabled(false);
		}
	}

	public void onCreate(View v) {
		controller.createAccount(Util.getText(edtUsername), Util.getText(edtPassword));

		Intent homeIntent = new Intent(CreateAccountActivity.this, HomeActivity.class);
		finish();
		startActivity(homeIntent);
	}

}
