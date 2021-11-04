package dhbw.smartmoderation.connection;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.briarproject.bramble.api.keyagreement.Payload;
import org.briarproject.bramble.api.keyagreement.PayloadEncoder;

import java.util.Arrays;

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
		byte[] bytes = payloadEncoder.encode(payload);

		Log.d(TAG, "Bytes sent: " + Arrays.toString(bytes));

		String string = byteArrayToString(bytes);
		try {
			BitMatrix bitMatrix = new QRCodeWriter().encode(string, BarcodeFormat.QR_CODE, QR_CODE_DIM, QR_CODE_DIM);
			return renderQrCode(bitMatrix);
		} catch (WriterException e) {
			return null;
		}
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
