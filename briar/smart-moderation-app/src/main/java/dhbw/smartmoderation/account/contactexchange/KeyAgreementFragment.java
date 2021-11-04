package dhbw.smartmoderation.account.contactexchange;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.CallSuper;
import androidx.annotation.UiThread;
import androidx.fragment.app.Fragment;

import com.google.zxing.Result;

import org.briarproject.bramble.api.UnsupportedVersionException;
import org.briarproject.bramble.api.event.Event;
import org.briarproject.bramble.api.event.EventBus;
import org.briarproject.bramble.api.event.EventListener;
import org.briarproject.bramble.api.keyagreement.KeyAgreementResult;
import org.briarproject.bramble.api.keyagreement.KeyAgreementTask;
import org.briarproject.bramble.api.keyagreement.Payload;
import org.briarproject.bramble.api.keyagreement.PayloadEncoder;
import org.briarproject.bramble.api.keyagreement.PayloadParser;
import org.briarproject.bramble.api.keyagreement.event.KeyAgreementAbortedEvent;
import org.briarproject.bramble.api.keyagreement.event.KeyAgreementFailedEvent;
import org.briarproject.bramble.api.keyagreement.event.KeyAgreementFinishedEvent;
import org.briarproject.bramble.api.keyagreement.event.KeyAgreementListeningEvent;
import org.briarproject.bramble.api.keyagreement.event.KeyAgreementStartedEvent;
import org.briarproject.bramble.api.keyagreement.event.KeyAgreementWaitingEvent;
import org.briarproject.bramble.api.lifecycle.IoExecutor;
import org.briarproject.bramble.api.nullsafety.NotNullByDefault;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.SmartModerationApplication;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.widget.LinearLayout.HORIZONTAL;
import static android.widget.Toast.LENGTH_LONG;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static org.briarproject.bramble.util.LogUtils.logException;

public class KeyAgreementFragment extends Fragment implements EventListener, QrCodeDecoder.ResultCallback, QrCodeView.FullScreenListener, DestroyableContext {

    static final String TAG = KeyAgreementFragment.class.getName();

    private static final Logger LOG = Logger.getLogger(KeyAgreementFragment.class.getName());

    private static final Charset ISO_8859_1 = StandardCharsets.ISO_8859_1;

    @Inject
    Provider<KeyAgreementTask> keyAgreementTaskProvider;

    @Inject
    PayloadEncoder payloadEncoder;

    @Inject
    PayloadParser payloadParser;

    @Inject
    @IoExecutor
    Executor ioExecutor;

    @Inject
    EventBus eventBus;

    private CameraView cameraView;
    private LinearLayout cameraOverlay;
    private View statusView;
    private QrCodeView qrCodeView;
    private TextView status;

    private boolean gotRemotePayload;
    private volatile boolean gotLocalPayload;
    private KeyAgreementTask keyAgreementTask;
    private KeyAgreementEventListener listener;

    private BaseFragmentListener baseFragmentListener;

    public static KeyAgreementFragment newInstance() {
        Bundle args = new Bundle();
        KeyAgreementFragment fragment = new KeyAgreementFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onAttach(Context context) {
        ((SmartModerationApplication)getActivity().getApplicationContext()).comp.inject(this);
        super.onAttach(context);
        listener = (KeyAgreementEventListener)context;
        baseFragmentListener = (BaseFragmentListener) context;
    }

    public String getUniqueTag() {

        return TAG;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_keyagreement, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cameraView = view.findViewById(R.id.camera_view);
        cameraOverlay = view.findViewById(R.id.camera_overlay);
        statusView = view.findViewById(R.id.status_container);
        status = view.findViewById(R.id.connect_status);
        qrCodeView = view.findViewById(R.id.qr_code_view);
        qrCodeView.setFullScreenListener(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        cameraView.setPreviewConsumer(new QrCodeDecoder(this));
    }

    @Override
    public void onStart() {
        super.onStart();
        eventBus.addListener(this);

        try {

            cameraView.start();

        } catch (CameraException e) {

            logCameraExceptionAndFinish(e);
        }

        startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        eventBus.removeListener(this);
        stopListening();

        try {

            cameraView.stop();

        } catch (CameraException e) {

            logCameraExceptionAndFinish(e);
        }
    }

    @Override
    public void setFullScreen(boolean fullscreen) {

        LinearLayout.LayoutParams statusParams;
        LinearLayout.LayoutParams qrCodeParams;

        if (fullscreen) {
            statusParams = new LinearLayout.LayoutParams(0, 0, 0f);
            qrCodeParams = new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT, 1f);

        }

        else {

            if (cameraOverlay.getOrientation() == HORIZONTAL) {
                statusParams = new LinearLayout.LayoutParams(0, MATCH_PARENT, 1f);
                qrCodeParams = new LinearLayout.LayoutParams(0, MATCH_PARENT, 1f);
            }

            else {
                statusParams = new LinearLayout.LayoutParams(MATCH_PARENT, 0, 1f);
                qrCodeParams = new LinearLayout.LayoutParams(MATCH_PARENT, 0, 1f);
            }
        }

        statusView.setLayoutParams(statusParams);
        qrCodeView.setLayoutParams(qrCodeParams);
        cameraOverlay.invalidate();
    }

    @UiThread
    private void logCameraExceptionAndFinish(CameraException e) {

        logException(LOG, WARNING, e);
        Toast.makeText(getActivity(), getString(R.string.CameraException_Message), LENGTH_LONG).show();
        finish();
    }

    @UiThread
    private void startListening() {

        KeyAgreementTask oldTask = keyAgreementTask;
        KeyAgreementTask newTask = keyAgreementTaskProvider.get();

       keyAgreementTask = newTask;

        ioExecutor.execute(() -> {
            if (oldTask != null) oldTask.stopListening();
            newTask.listen();
        });
    }

    @UiThread
    private void stopListening() {
        KeyAgreementTask oldTask = keyAgreementTask;
        ioExecutor.execute(() -> {
            if (oldTask != null) oldTask.stopListening();
        });
    }

    @UiThread
    private void reset() {

        if (gotRemotePayload) {

            try {
                cameraView.start();

            } catch (CameraException e) {

                logCameraExceptionAndFinish(e);
                return;
            }
        }

        statusView.setVisibility(INVISIBLE);
        cameraView.setVisibility(VISIBLE);
        gotRemotePayload = false;
        gotLocalPayload = false;
        startListening();
    }

    @UiThread
    private void qrCodeScanned(String content) {

        try {

            byte[] payloadBytes = content.getBytes(ISO_8859_1);

            if (LOG.isLoggable(INFO)) {

                LOG.info("Remote payload is " + payloadBytes.length + " bytes");
            }

            Payload remotePayload = payloadParser.parse(payloadBytes);
            gotRemotePayload = true;
            cameraView.stop();
            cameraView.setVisibility(INVISIBLE);
            statusView.setVisibility(VISIBLE);

            status.setText(getString(R.string.connect_to_phone));

            keyAgreementTask.connectAndRunProtocol(remotePayload);

        } catch (UnsupportedVersionException e) {

            reset();
            String msg;

            if (e.isTooOld()) {

                msg = getString(R.string.qrcode_old);

            } else {

                msg = getString(R.string.qrcode_new);
            }

            ContactExchangeErrorFragment fragment = ContactExchangeErrorFragment.newInstance(msg);

            showNextFragment(fragment, fragment.getUniqueTag());

        } catch (CameraException e) {

            logCameraExceptionAndFinish(e);

        } catch (IOException | IllegalArgumentException e) {

            LOG.log(WARNING, "QR Code Invalid", e);
            reset();
            Toast.makeText(getActivity(), getString(R.string.qrcode_invalid), LENGTH_LONG).show();
        }
    }



    @Override
    public void eventOccurred(Event e) {

        if (e instanceof KeyAgreementListeningEvent) {

            KeyAgreementListeningEvent event = (KeyAgreementListeningEvent) e;
            gotLocalPayload = true;
            setQrCode(event.getLocalPayload());

        }

        else if (e instanceof KeyAgreementFailedEvent) {
            keyAgreementFailed();

        }

        else if (e instanceof KeyAgreementWaitingEvent) {
            keyAgreementWaiting();

        }

        else if (e instanceof KeyAgreementStartedEvent) {
            keyAgreementStarted();
        }

        else if (e instanceof KeyAgreementAbortedEvent) {
            KeyAgreementAbortedEvent event = (KeyAgreementAbortedEvent) e;
            keyAgreementAborted(event.didRemoteAbort());
        }

        else if (e instanceof KeyAgreementFinishedEvent) {
            keyAgreementFinished(((KeyAgreementFinishedEvent) e).getResult());
        }

    }

    @UiThread
    private void keyAgreementFailed() {
        reset();
        listener.keyAgreementFailed();
    }

    @UiThread
    private void keyAgreementWaiting() {
        status.setText(listener.keyAgreementWaiting());
    }

    @UiThread
    private void keyAgreementStarted() {
        qrCodeView.setVisibility(INVISIBLE);
        statusView.setVisibility(VISIBLE);
        status.setText(listener.keyAgreementStarted());
    }

    @UiThread
    private void keyAgreementAborted(boolean remoteAborted) {
        reset();
        listener.keyAgreementAborted(remoteAborted);
    }

    @UiThread
    private void keyAgreementFinished(KeyAgreementResult result) {
        statusView.setVisibility(VISIBLE);
        status.setText(listener.keyAgreementFinished(result));
    }

    private void setQrCode(Payload localPayload) {

        Context context = getContext();
        if (context == null) {
            return;
        }
        DisplayMetrics dm = context.getResources().getDisplayMetrics();

        ioExecutor.execute(() -> {
            byte[] payloadBytes = payloadEncoder.encode(localPayload);

            if (LOG.isLoggable(INFO)) {

                LOG.info("Local payload is " + payloadBytes.length + " bytes");
            }

            String content = new String(payloadBytes, ISO_8859_1);
            Bitmap qrCode = QrCodeUtils.createQrCode(dm, content);
            runOnUiThreadUnlessDestroyed(() -> qrCodeView.setQrCode(qrCode));
        });
    }

    @Deprecated
    @CallSuper
    public void runOnUiThreadUnlessDestroyed(Runnable r) {
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(() -> {
                if (isAdded() && !activity.isFinishing()) {
                    r.run();
                }
            });
        }
    }

    @Override
    public void handleResult(Result result) {

        runOnUiThreadUnlessDestroyed(() -> {

            LOG.info("Got result from decoder");
            if (!gotLocalPayload) {
                return;
            }

            if (!gotRemotePayload) {
                qrCodeScanned(result.getText());
            }
        });

    }

    protected void finish() {
        getActivity().getSupportFragmentManager().popBackStack();
    }

    protected void showNextFragment(Fragment fragment, String tag) {

        baseFragmentListener.showNextFragment(fragment, tag);
    }

    @UiThread
    protected void handleException(Exception e) {

        baseFragmentListener.handleException(e);
    }

    @NotNullByDefault
    interface KeyAgreementEventListener {

        @UiThread
        void keyAgreementFailed();

        @UiThread
        @Nullable
        String keyAgreementWaiting();

        @UiThread
        @Nullable
        String keyAgreementStarted();

        @UiThread
        void keyAgreementAborted(boolean remoteAborted);

        @UiThread
        @Nullable
        String keyAgreementFinished(KeyAgreementResult result);
    }
}
