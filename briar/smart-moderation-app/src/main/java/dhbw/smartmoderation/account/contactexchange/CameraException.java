package dhbw.smartmoderation.account.contactexchange;

import java.io.IOException;

public class CameraException extends IOException {

    CameraException(String message) {
        super(message);
    }

    CameraException(Throwable cause) {
        super(cause);
    }
}
