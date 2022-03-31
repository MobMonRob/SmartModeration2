package dhbw.smartmoderation.moderationCard;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.SmartModerationApplication;

public class DesktopLoginQRScanner extends AppCompatActivity {

    private CodeScanner mCodeScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_desktop_login_view);
        if (ContextCompat.checkSelfPermission(DesktopLoginQRScanner.this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(DesktopLoginQRScanner.this, new String[]{Manifest.permission.CAMERA}, 123);
        } else {
            startScanning();
        }
    }

    private void startScanning() {
        CodeScannerView scannerView = findViewById(R.id.scanner_view);
        if (mCodeScanner == null)
            mCodeScanner = new CodeScanner(this, scannerView);
        mCodeScanner.setDecodeCallback(result -> runOnUiThread(() -> {
            String loginJSONstring = result.getText();
            JSONObject loginJSON = null;
            try {
                loginJSON = new JSONObject(loginJSONstring);

                String ipAddress = (String) loginJSON.get("ipAddress");
                String port = (String) loginJSON.get("port");
                String apiKey = (String) loginJSON.get("apiKey");

                SmartModerationApplication app = (SmartModerationApplication) SmartModerationApplication.getApp();
                app.getClient().startClient(ipAddress, port, apiKey, app.getWebServer(), app.getMeetingId());
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }

            this.finish();
        }));
        scannerView.setOnClickListener(view -> mCodeScanner.startPreview());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 123) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera permission granted", Toast.LENGTH_LONG).show();
                startScanning();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCodeScanner != null) {
            mCodeScanner.startPreview();
        }
    }

    @Override
    protected void onPause() {
        if (mCodeScanner != null) {
            mCodeScanner.releaseResources();
        }
        super.onPause();
    }
}
