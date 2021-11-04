package dhbw.smartmoderation.account.contactexchange;

import android.hardware.Camera;
import androidx.annotation.UiThread;
import org.briarproject.bramble.api.nullsafety.NotNullByDefault;

@SuppressWarnings("deprecation")
@NotNullByDefault
public interface PreviewConsumer {

    @UiThread
    void start(Camera camera, int cameraIndex);

    @UiThread
    void stop();
}
