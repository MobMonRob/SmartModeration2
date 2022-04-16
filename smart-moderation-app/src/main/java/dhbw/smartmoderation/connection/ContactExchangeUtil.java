package dhbw.smartmoderation.connection;

import static com.google.common.base.Charsets.ISO_8859_1;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.Log;

import com.google.zxing.common.BitMatrix;

import org.briarproject.bramble.api.keyagreement.Payload;
import org.briarproject.bramble.api.keyagreement.PayloadEncoder;

import java.util.Arrays;

import dhbw.smartmoderation.SmartModerationApplicationImpl;
import dhbw.smartmoderation.account.contactexchange.QrCodeUtils;

final class ContactExchangeUtil {

    private ContactExchangeUtil() {
    }

    static String byteArrayToString(byte[] bytes) {
        String s = "";
        for (byte b :
                bytes) {
            s += b + ",";
        }
        return s;
    }

    static byte[] stringToByteArray(String s) {
        String[] split = s.split(",");
        byte[] b = new byte[split.length];
        for (int i = 0; i < b.length; i++) {
            b[i] = Byte.parseByte(split[i]);
        }
        return b;
    }
}
