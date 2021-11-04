package dhbw.smartmoderation.connection;

import android.graphics.Bitmap;

/**
 * A callback interface for contact exchange.
 */
public interface ContactExchangeCallback {

	/**
	 * Called, when the contact information form the local author was read an encoded.
	 *
	 * @param bitmap A bitmap of a QR-Code containing the information about the local author
	 */
	void onGotContactInformation(Bitmap bitmap);

	/**
	 * Called, when the contacts were successfully exchanged.
	 */
	void onContactsExchanged();

}
