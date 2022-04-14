package dhbw.smartmoderation.connection;

import android.graphics.Bitmap;

import org.briarproject.bramble.api.db.DbException;
import org.briarproject.bramble.api.event.Event;
import org.briarproject.bramble.api.event.EventListener;
import org.briarproject.bramble.api.keyagreement.KeyAgreementResult;
import org.briarproject.bramble.api.keyagreement.Payload;
import org.briarproject.bramble.api.keyagreement.event.KeyAgreementFinishedEvent;
import org.briarproject.bramble.api.keyagreement.event.KeyAgreementListeningEvent;

import java.io.IOException;

class ContactExchangeEventListener implements EventListener {

	private final ConnectionService service;

	ContactExchangeEventListener(ConnectionService service) {
		this.service = service;
	}

	@Override
	public void eventOccurred(Event e) {
		if (e instanceof KeyAgreementListeningEvent) {
			KeyAgreementListeningEvent event = (KeyAgreementListeningEvent) e;
			Payload payload = event.getLocalPayload();
			Bitmap bitmap = ContactExchangeUtil.generateQrCode(payload);
			service.getContactExchangeCallback().onGotContactInformation(bitmap);
		} else if (e instanceof KeyAgreementFinishedEvent) {
			KeyAgreementFinishedEvent event = (KeyAgreementFinishedEvent) e;
			KeyAgreementResult result = event.getResult();
			try {
				service.exchangeContacts(result);
				service.getContactExchangeCallback().onContactsExchanged();
			}
			catch (IOException ex) {
				ex.printStackTrace();
			} catch (DbException ex) {
				ex.printStackTrace();
			}
		}
	}
}
