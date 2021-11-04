package dhbw.smartmoderation.data.model;

import org.briarproject.bramble.api.identity.Author;
import org.briarproject.bramble.api.identity.AuthorId;

import dhbw.smartmoderation.util.Util;

public class PrivateGroup {

    org.briarproject.briar.api.privategroup.PrivateGroup delegate;

    public PrivateGroup(org.briarproject.briar.api.privategroup.PrivateGroup delegate) { this.delegate = delegate; }

    public Long getId() { return Util.bytesToLong(this.delegate.getId().getBytes()); }

    public String getName() { return this.delegate.getName(); }

    public void setName(String name) {

        this.delegate = new org.briarproject.briar.api.privategroup.PrivateGroup(delegate.getGroup(),name,delegate.getCreator(),delegate.getSalt());
    }

}
