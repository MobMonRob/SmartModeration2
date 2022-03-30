package dhbw.smartmoderation.account.contactexchange;

import static java.util.logging.Level.WARNING;

import android.hardware.Camera;
import android.os.AsyncTask;

import androidx.annotation.UiThread;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import org.briarproject.bramble.api.nullsafety.NotNullByDefault;

import java.util.Collections;
import java.util.logging.Logger;

@SuppressWarnings("deprecation")
public class QrCodeDecoder implements PreviewConsumer, Camera.PreviewCallback {

    private static final Logger LOG = Logger.getLogger(QrCodeDecoder.class.getName());

    private final Reader reader = new QRCodeReader();

    private ResultCallback callback;

    private Camera camera = null;

    private int cameraIndex = 0;

    public QrCodeDecoder(ResultCallback callback) {
        this.callback = callback;
    }

    @Override
    public void start(Camera camera, int cameraIndex) {
        this.camera = camera;
        this.cameraIndex = cameraIndex;
        askForPreviewFrame();


    }

    @UiThread
    private void askForPreviewFrame() {
        if (camera != null) {
            camera.setOneShotPreviewCallback(this);
        }
    }


    @Override
    public void stop() {
        this.camera = null;
        this.cameraIndex = 0;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if(camera == this.camera) {

            try {

                Camera.Size size = camera.getParameters().getPreviewSize();

                if(data.length == size.width * size.height * 3 / 2) {

                    Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                    Camera.getCameraInfo(cameraIndex, cameraInfo);
                    new DecoderTask(data, size.width, size.height, cameraInfo.orientation).execute();
                }

                else {

                    LOG.info("Preview size does not match camera parameters");
                    askForPreviewFrame();
                }

            } catch (RuntimeException e) {

                LOG.log(WARNING, "Error getting camera parameters", e);
            }
        }

        else {

            LOG.info("Camera has changed, ignoring preview frame");
        }

    }

    private class DecoderTask extends AsyncTask<Void, Void, Void> {

        private final byte[] data;
        private final int width;
        private final int height;
        private final int orientation;

        private DecoderTask(byte[] data, int width, int height, int orientation) {
            this.data = data;
            this.width = width;
            this.height = height;
            this.orientation = orientation;
        }


        @Override
        protected Void doInBackground(Void... voids) {
            BinaryBitmap bitmap = binarize(data, width, height, orientation);
            Result result;
            try {
                result = reader.decode(bitmap, Collections.singletonMap(DecodeHintType.CHARACTER_SET, "ISO8859_1"));

            } catch (ReaderException e) {

                LOG.warning("Invalid preview frame");
                return null;

            } finally {

                reader.reset();
            }

            try {
                result = reader.decode(bitmap, Collections.singletonMap(DecodeHintType.CHARACTER_SET, "UTF-8"));

            } catch (ReaderException e) {

                LOG.warning("Invalid preview frame");
                return null;

            } finally {

                reader.reset();
            }

            callback.handleResult(result);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            askForPreviewFrame();
        }
    }

    private static BinaryBitmap binarize(byte[] data, int width, int height, int orientation) {

        int crop = Math.min(width, height);
        int left = orientation >= 180 ? width - crop : 0;
        int top = orientation >= 180 ? height - crop : 0;
        LuminanceSource src = new PlanarYUVLuminanceSource(data, width, height, left, top, crop, crop, false);
        return new BinaryBitmap(new HybridBinarizer(src));

    }

    @NotNullByDefault
    public
    interface ResultCallback {

        void handleResult(Result result);
    }
}
