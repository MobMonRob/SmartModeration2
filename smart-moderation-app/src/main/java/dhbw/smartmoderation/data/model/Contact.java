package dhbw.smartmoderation.data.model;

import dhbw.smartmoderation.util.Util;

public class Contact implements IContact {

    org.briarproject.bramble.api.contact.Contact delegate;

    public Contact(org.briarproject.bramble.api.contact.Contact contact) { this.delegate = contact; }

    public Long getId() { return Util.bytesToLong(this.delegate.getAuthor().getId().getBytes()); }

    public String getName() { return this.delegate.getAuthor().getName(); }

    public  org.briarproject.bramble.api.contact.Contact getBriarContact() { return this.delegate; }

}
