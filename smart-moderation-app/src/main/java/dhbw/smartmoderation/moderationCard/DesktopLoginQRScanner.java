package dhbw.smartmoderation.moderationCard;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;

import org.briarproject.bramble.api.plugin.TransportId;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.SmartModerationApplicationImpl;
import dhbw.smartmoderation.util.ExceptionHandlingActivity;

public class DesktopLoginQRScanner extends ExceptionHandlingActivity {

    private CodeScanner mCodeScanner;
    public static final TransportId DESKTOPID = new TransportId("DESKTOPAPP");

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
            System.out.println("Callback");
            String loginJSONString = result.getText();
            System.out.println(loginJSONString);
            try {
                startClient(loginJSONString);
            } catch (JSONException | IOException e) {
                handleException(e);
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

    private void startClient(String loginJSONString) throws JSONException, IOException {
        JSONObject loginJSON = new JSONObject(loginJSONString);
        String ipAddress = (String) loginJSON.get("ipAddress");
        int port = (int) loginJSON.get("port");
        String apiKey = (String) loginJSON.get("apiKey");
        SmartModerationApplicationImpl app = (SmartModerationApplicationImpl) SmartModerationApplicationImpl.getApp();
        Intent intent = getIntent();
        long meetingId = intent.getLongExtra("meetingId", 0);
        app.getClient().startClient(ipAddress, port, apiKey, meetingId);
    }
}
