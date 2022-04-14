package dhbw.smartmoderation.launcher;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import dhbw.smartmoderation.R;
import dhbw.smartmoderation.account.create.CreateAccountActivity;
import dhbw.smartmoderation.login.LoginActivity;

/**
 * Activity used when application is launched. Decides whether to display the "Create account" or "Login" screen.
 */
public class LauncherActivity extends AppCompatActivity {

	private LauncherController controller;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		this.getSupportActionBar().hide();

		setContentView(R.layout.activity_launcher);

		controller = new LauncherController();

		if (controller.accountExists()) {
			Intent loginIntent = new Intent(LauncherActivity.this, LoginActivity.class);
			startActivity(loginIntent);
			finish();
		} else {
			Intent createAccountIntent = new Intent(LauncherActivity.this, CreateAccountActivity.class);
			startActivity(createAccountIntent);
			finish();
		}
	}

}
