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

	private static final String TAG = ContactExchangeUtil.class.getSimpleName();
	private static final int QR_CODE_DIM = 1080;
	private static ConnectionService service;

	private ContactExchangeUtil() {
	}

	static void init(ConnectionService service) {
		ContactExchangeUtil.service = service;
	}

	static Bitmap generateQrCode(Payload payload) {
		PayloadEncoder payloadEncoder = service.getPayloadEncoder();
		DisplayMetrics dm = SmartModerationApplicationImpl.getApp().getResources().getDisplayMetrics();
		byte[] payloadBytes = payloadEncoder.encode(payload);

		Log.d(TAG, "Bytes sent: " + Arrays.toString(payloadBytes));
		String content = new String(payloadBytes, ISO_8859_1);
		return QrCodeUtils.createQrCode(dm, content);
	}

	private static Bitmap renderQrCode(BitMatrix matrix) {
		int width = matrix.getWidth();
		int height = matrix.getHeight();
		int[] pixels = new int[width * height];

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				pixels[y * width + x] = matrix.get(x, y) ? Color.BLACK : Color.WHITE;
			}
		}

		Bitmap qrCode = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		qrCode.setPixels(pixels, 0, width, 0, 0, width, height);
		return qrCode;
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
