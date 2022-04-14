package dhbw.smartmoderation.util.Mocks;

import org.briarproject.bramble.api.contact.Contact;
import org.briarproject.bramble.api.contact.ContactId;
import org.briarproject.bramble.api.crypto.PublicKey;
import org.briarproject.bramble.api.identity.Author;
import org.briarproject.bramble.api.identity.AuthorId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import dhbw.smartmoderation.data.model.IContact;

public class CreateGroupTesting {

    public Collection<IContact> getContacts() {

        ArrayList<IContact> contacts = new ArrayList<>();

        for (int i = 0; i < 10; i++) {

            Random r = new Random();
            byte[] b = new byte[32];
            r.nextBytes(b);

            PublicKey publicKey = new PublicKey() {
                @Override
                public String getKeyType() {
                    return "Ed25519";
                }

                @Override
                public byte[] getEncoded() {
                    return new byte[0];
                }
            };

            AuthorId authorId1 = new AuthorId(b);
            r.nextBytes(b);
            AuthorId authorId2 = new AuthorId(b);
            Author author = new Author(authorId2, 0, "Tanja Junker", publicKey);
            ContactId contactId = new ContactId(r.nextInt());
            Contact contact = new Contact(contactId, author, authorId1, null, null,true);
            dhbw.smartmoderation.data.model.Contact newContact = new dhbw.smartmoderation.data.model.Contact(contact);
            contacts.add(newContact);
        }

        return contacts;
    }

    public void createGroup(String name, Collection<IContact> contacts) {

    }
}
