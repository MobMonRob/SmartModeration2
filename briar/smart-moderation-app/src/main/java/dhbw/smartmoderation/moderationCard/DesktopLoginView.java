package dhbw.smartmoderation.moderationCard;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.briarproject.bramble.api.keyagreement.KeyAgreementTask;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.account.contactexchange.BaseFragmentListener;
import dhbw.smartmoderation.account.contactexchange.CameraException;
import dhbw.smartmoderation.account.contactexchange.CameraView;
import dhbw.smartmoderation.account.contactexchange.KeyAgreementFragment;
import dhbw.smartmoderation.account.contactexchange.QrCodeView;

public class DesktopLoginView {
    private static final Charset ISO_8859_1 = StandardCharsets.ISO_8859_1;
    private static final String TAG = "Login";

    private CameraView cameraView;
    private LinearLayout cameraOverlay;
    private View statusView;
    private QrCodeView qrCodeView;
    private TextView status;

    private boolean gotRemotePayload;
    private volatile boolean gotLocalPayload;
    private KeyAgreementTask keyAgreementTask;
    private KeyAgreementFragment.KeyAgreementEventListener listener;

    private BaseFragmentListener baseFragmentListener;
    private AlertDialog alertDialog;

    public DesktopLoginView(FragmentActivity activity) throws CameraException {
        initializePopup(activity);
    }

    private void initializePopup(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View popUp = inflater.inflate(R.layout.popup_desktop_login_view, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(popUp);

        Thread thread = new Thread(() -> {
            try {
                sendLoginCall();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        });
        thread.start();

        //cameraView = popUp.findViewById(R.id.cameraView);
        /*
        cameraOverlay = popUp.findViewById(R.id.camera_overlay);
        statusView = popUp.findViewById(R.id.status_container);
        status = popUp.findViewById(R.id.connect_status);
        qrCodeView = popUp.findViewById(R.id.qr_code_view);
        qrCodeView.setFullScreenListener(this);
         */
        // try {
        //     cameraView.start();
        // } catch (CameraException e) {
        //     e.printStackTrace();
        // }
        alertDialog = builder.create();
    }

    //TODO: get login information from scanned QR-Code
    private void sendLoginCall() throws UnsupportedEncodingException {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost("192.168.0.80:8080/login");
        httpPost.setHeader("Authorization", "Bearer Test");
        httpPost.setHeader("Content-type", "application/json");

        String inputJson = "{\r\n    \"meetingId\": 3570151905752727837,\r\n    \"ipAddress\": \"127.0.0.1\",\r\n    \"port\": 8000\r\n}";
        StringEntity stringEntity = new StringEntity(inputJson);
        httpPost.setEntity(stringEntity);

        try {
            HttpResponse response = httpClient.execute(httpPost);
            // write response to log
            Log.d("Http Post Response:", response.toString());
        } catch (IOException e) {
            // Log exception
            e.printStackTrace();
        }

    }

    public void show() {
        alertDialog.show();
    }
}

