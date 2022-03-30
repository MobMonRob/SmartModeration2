package dhbw.smartmoderation.moderationCard;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.google.zxing.Result;

import org.briarproject.bramble.api.event.Event;
import org.briarproject.bramble.api.event.EventBus;
import org.briarproject.bramble.api.event.EventListener;
import org.briarproject.bramble.api.keyagreement.KeyAgreementTask;
import org.briarproject.bramble.api.keyagreement.PayloadEncoder;
import org.briarproject.bramble.api.keyagreement.PayloadParser;
import org.briarproject.bramble.api.lifecycle.IoExecutor;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Provider;

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.account.contactexchange.BaseFragmentListener;
import dhbw.smartmoderation.account.contactexchange.CameraException;
import dhbw.smartmoderation.account.contactexchange.CameraView;
import dhbw.smartmoderation.account.contactexchange.DestroyableContext;
import dhbw.smartmoderation.account.contactexchange.KeyAgreementFragment;
import dhbw.smartmoderation.account.contactexchange.QrCodeDecoder;
import dhbw.smartmoderation.account.contactexchange.QrCodeView;

public class DesktopLoginView implements QrCodeDecoder.ResultCallback {
    static final String TAG = KeyAgreementFragment.class.getName();
    private static final Logger LOG = Logger.getLogger(KeyAgreementFragment.class.getName());
    private static final Charset ISO_8859_1 = StandardCharsets.ISO_8859_1;


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

    public DesktopLoginView(FragmentActivity activity) {
        initializePopup(activity);
    }

    private void initializePopup(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View popUp = inflater.inflate(R.layout.popup_desktop_login_view, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(popUp);
        cameraView = popUp.findViewById(R.id.cameraView);
        cameraView.setPreviewConsumer(new QrCodeDecoder(this));
        /*
        cameraOverlay = popUp.findViewById(R.id.camera_overlay);
        statusView = popUp.findViewById(R.id.status_container);
        status = popUp.findViewById(R.id.connect_status);
        qrCodeView = popUp.findViewById(R.id.qr_code_view);
        qrCodeView.setFullScreenListener(this);
         */
        try {
            cameraView.start();

        } catch (CameraException e) {
            e.printStackTrace();
        }
        alertDialog = builder.create();
    }

    public void show() {
        alertDialog.show();
    }

    @Override
    public void handleResult(Result result) {
        int x = 5;

    }
}

