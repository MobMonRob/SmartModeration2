package dhbw.smartmoderation.util;

import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.EditText;

import com.google.common.primitives.Longs;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * A Utility class for the Smart Moderation App.
 */
public final class Util {

	private final static TreeMap<Integer, String> map = new TreeMap<Integer, String>();

	private Util() {

	}

	public static void fillTreeMap() {

		map.put(1000, "M");
		map.put(900, "CM");
		map.put(500, "D");
		map.put(400, "CD");
		map.put(100, "C");
		map.put(90, "XC");
		map.put(50, "L");
		map.put(40, "XL");
		map.put(10, "X");
		map.put(9, "IX");
		map.put(5, "V");
		map.put(4, "IV");
		map.put(1, "I");
	}

	/**
	 * Checks whether a given EditText is empty.
	 *
	 * @param editText The EditText to check
	 * @return {@code true} if it is empty, {@code false} otherwise
	 */
	public static boolean isEmpty(EditText editText) {
		return getText(editText).isEmpty();
	}

	/**
	 * Gets the text from a EditText.
	 *
	 * @param editText The EditText to get the content from
	 * @return The content as a String
	 */
	public static String getText(EditText editText) {
		return editText.getText().toString().trim();
	}

	/**
	 * Checks whether a permission was granted.
	 *
	 * @param grantResult The grant result code
	 * @return {@code true} if it was granted, {@code false} otherwise
	 */
	public static boolean checkGranted(int grantResult) {
		return grantResult == PackageManager.PERMISSION_GRANTED;
	}

	/**
	 * Checks whether multiple permissions were granted.
	 *
	 * @param grantResults The grand result codes
	 * @return {@code true} if all of them were granted, {@code false} otherwise
	 * @see #checkGranted(int)
	 */
	public static boolean checkGranted(int[] grantResults) {
		for (int result :
				grantResults) {
			if (!checkGranted(result)) return false;
		}
		return true;
	}

	public final static String toRoman(int number) {

		fillTreeMap();

		int l =  map.floorKey(number);
		if ( number == l ) {
			return map.get(number);
		}
		return map.get(l) + toRoman(number-l);
	}

	public static String convertMilliSecondsToTimeString(long milliseconds) {

		int hour = (int)(TimeUnit.MILLISECONDS.toMinutes(milliseconds)/60);
		int minute = (int)(TimeUnit.MILLISECONDS.toMinutes(milliseconds) % 60);

		String minuteStr = minute + "";

		if(minute < 10) {

			minuteStr = 0 + minuteStr;
		}

		return hour + ":" + minuteStr;
	}

	public static String convertMilliSecondsToMinutesTimeString(long milliseconds) {

		int seconds = (int)((milliseconds/1000) % 60);
		int minutes = (int)((milliseconds/1000)/60);

		String minutesStr = minutes + "";

		if(minutes < 10) {

			minutesStr = 0 + minutesStr;
		}

		String secondsStr = seconds + "";

		if(seconds < 10) {

			secondsStr = 0 + secondsStr;
		}

		return minutesStr + ":" + secondsStr;
	}

	public static long milliSecondsToMinutes(long milliSeconds) {

		long minutes = milliSeconds/(60*1000);
		return minutes;
	}

	public static long convertTimeStringToMilliSeconds(String timeString) {

		long minutes = Long.parseLong(timeString.split(":")[0])*60 +  Long.parseLong(timeString.split(":")[1]);
		return TimeUnit.MINUTES.toMillis(minutes);
	}

	public static long convertDateStringToMilliSeconds(String dateString) {

		SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
		long milliseconds = 0;

		try {

			Date date = format.parse(dateString);
			milliseconds = date.getTime();

		} catch (ParseException e) {

			e.printStackTrace();
		}

		return milliseconds;
	}

	public static byte[] longToBytes(long x) {
		return Longs.toByteArray(x);
	}

	public static long bytesToLong(byte[] bytes) {
		return Longs.fromByteArray(bytes);
	}



}
