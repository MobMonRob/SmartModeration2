package dhbw.smartmoderation.connection;

import android.util.Log;

import org.briarproject.bramble.api.account.AccountManager;
import org.briarproject.bramble.api.contact.Contact;
import org.briarproject.bramble.api.contact.ContactExchangeManager;
import org.briarproject.bramble.api.contact.ContactId;
import org.briarproject.bramble.api.contact.ContactManager;
import org.briarproject.bramble.api.crypto.DecryptionException;
import org.briarproject.bramble.api.crypto.SecretKey;
import org.briarproject.bramble.api.db.DbException;
import org.briarproject.bramble.api.event.EventBus;
import org.briarproject.bramble.api.identity.IdentityManager;
import org.briarproject.bramble.api.identity.LocalAuthor;
import org.briarproject.bramble.api.keyagreement.KeyAgreementResult;
import org.briarproject.bramble.api.keyagreement.KeyAgreementTask;
import org.briarproject.bramble.api.keyagreement.Payload;
import org.briarproject.bramble.api.keyagreement.PayloadEncoder;
import org.briarproject.bramble.api.keyagreement.PayloadParser;
import org.briarproject.bramble.api.lifecycle.LifecycleManager;
import org.briarproject.bramble.api.plugin.BluetoothConstants;
import org.briarproject.bramble.api.connection.ConnectionManager;
import org.briarproject.bramble.api.connection.ConnectionRegistry;
import org.briarproject.bramble.api.plugin.PluginManager;
import org.briarproject.bramble.api.plugin.TorConstants;
import org.briarproject.bramble.api.plugin.TransportId;
import org.briarproject.bramble.api.plugin.duplex.DuplexPlugin;
import org.briarproject.bramble.api.plugin.duplex.DuplexTransportConnection;
import org.briarproject.bramble.api.properties.TransportProperties;
import org.briarproject.bramble.api.properties.TransportPropertyManager;
import org.briarproject.bramble.api.sync.GroupId;
import org.briarproject.bramble.api.sync.MessageId;
import org.briarproject.bramble.api.system.Clock;
import org.briarproject.briar.api.client.MessageTracker;
import org.briarproject.briar.api.conversation.ConversationManager;
import org.briarproject.briar.api.conversation.ConversationMessageHeader;
import org.briarproject.briar.api.messaging.MessagingManager;
import org.briarproject.briar.api.privategroup.GroupMember;
import org.briarproject.briar.api.privategroup.GroupMessage;
import org.briarproject.briar.api.privategroup.GroupMessageFactory;
import org.briarproject.briar.api.privategroup.GroupMessageHeader;
import org.briarproject.briar.api.privategroup.JoinMessageHeader;
import org.briarproject.briar.api.privategroup.PrivateGroup;
import org.briarproject.briar.api.privategroup.PrivateGroupFactory;
import org.briarproject.briar.api.privategroup.PrivateGroupManager;
import org.briarproject.briar.api.privategroup.invitation.GroupInvitationFactory;
import org.briarproject.briar.api.privategroup.invitation.GroupInvitationManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import dhbw.smartmoderation.SmartModerationApplicationImpl;
import dhbw.smartmoderation.data.model.LocalAuthorDao;
import dhbw.smartmoderation.exceptions.CantCreateGroupException;
import dhbw.smartmoderation.exceptions.CantReceiveInvitationsException;
import dhbw.smartmoderation.exceptions.GroupNotFoundException;
import dhbw.smartmoderation.exceptions.NoContactsFoundException;
import dhbw.smartmoderation.exceptions.NoGroupHeadersFoundException;
import dhbw.smartmoderation.exceptions.NoGroupMessageTextFoundException;
import dhbw.smartmoderation.util.Util;

import static java.lang.Math.max;

/**
 * A service for everything regarding the Briar-communication.
 */
@Immutable
@Singleton
public class ConnectionService {

    private static final String TAG = ConnectionService.class.getSimpleName();

    LocalAuthorDao localAuthorDao = ((SmartModerationApplicationImpl) SmartModerationApplicationImpl.getApp()).getDaoSession().getLocalAuthorDao();

    private final AccountManager accountManager;
    private final ContactManager contactManager;
    private final LifecycleManager lifecycleManager;
    private final Provider<KeyAgreementTask> keyAgreementTaskProvider;
    private final PayloadEncoder payloadEncoder;
    private final PayloadParser payloadParser;
    private final ContactExchangeManager contactExchangeManager;
    private final ConnectionManager connectionManager;
    private final IdentityManager identityManager;
    private final PrivateGroupFactory privateGroupFactory;
    private final PrivateGroupManager privateGroupManager;
    private final GroupMessageFactory groupMessageFactory;
    private final GroupInvitationFactory groupInvitationFactory;
    private final GroupInvitationManager groupInvitationManager;
    private final ConversationManager conversationManager;
    private final PluginManager pluginManager;
    private final TransportPropertyManager transportPropertyManager;
    private final ConnectionRegistry connectionRegistry;
    private final MessagingManager messagingManager;
    private final EventBus eventBus;
    private final Clock clock;
    private LocalAuthor localAuthor;

    private final GroupInvitationVisitor groupInvitationVisitor;
    private ContactExchangeCallback contactExchangeCallback;
    private KeyAgreementTask keyAgreementTask;

    @Inject
    ConnectionService(AccountManager accountManager, ContactManager contactManager, LifecycleManager lifecycleManager,
                      Provider<KeyAgreementTask> keyAgreementTaskProvider, EventBus eventBus, PayloadEncoder payloadEncoder,
                      PayloadParser payloadParser, ContactExchangeManager contactExchangeManager, ConnectionManager connectionManager,
                      IdentityManager identityManager, PrivateGroupFactory privateGroupFactory,
                      PrivateGroupManager privateGroupManager, GroupMessageFactory groupMessageFactory,
                      GroupInvitationFactory groupInvitationFactory, GroupInvitationManager groupInvitationManager,
                      ConversationManager conversationManager, PluginManager pluginManager,
                      TransportPropertyManager transportPropertyManager, ConnectionRegistry connectionRegistry,
                      MessagingManager messagingManager, Clock clock) {
        this.accountManager = accountManager;
        this.contactManager = contactManager;
        this.lifecycleManager = lifecycleManager;
        this.keyAgreementTaskProvider = keyAgreementTaskProvider;
        this.payloadEncoder = payloadEncoder;
        this.payloadParser = payloadParser;
        this.contactExchangeManager = contactExchangeManager;
        this.connectionManager = connectionManager;
        this.identityManager = identityManager;
        this.privateGroupFactory = privateGroupFactory;
        this.privateGroupManager = privateGroupManager;
        this.groupMessageFactory = groupMessageFactory;
        this.groupInvitationFactory = groupInvitationFactory;
        this.groupInvitationManager = groupInvitationManager;
        this.conversationManager = conversationManager;
        this.pluginManager = pluginManager;
        this.transportPropertyManager = transportPropertyManager;
        this.connectionRegistry = connectionRegistry;
        this.messagingManager = messagingManager;
        this.eventBus = eventBus;
        this.clock = clock;

        ContactExchangeUtil.init(this);

        groupInvitationVisitor = new GroupInvitationVisitor();
    }

    public boolean isConnected(ContactId contactId) {
        return connectionRegistry.isConnected(contactId);
    }


    public void setLocalAuthor() {
        try {
            this.localAuthor = identityManager.getLocalAuthor();
        } catch (DbException e) {
            e.printStackTrace();
        }
    }


    /**
     * Checks whether a Briar-account for the Smart Moderation App exists.
     *
     * @return {@code true} if an account exists, {@code false} otherwise
     */
    public boolean accountExists() {
        return accountManager.accountExists();
    }

    /**
     * Create a new Briar-account.
     *
     * @param name     The name of the Briar-account
     * @param password The password for the Briar-account
     */
    public void createAccount(String name, String password) {
        accountManager.deleteAccount();

        accountManager.createAccount(name, password);
        startLifecycleManager();
    }

    /**
     * Login with a Briar-account.
     *
     * @param password The password for the Briar-account
     */
    public boolean login(String password) {

        try {
            accountManager.signIn(password);
            startLifecycleManager();
            return true;
        } catch (DecryptionException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete the Briar-account.
     */
    public void deleteAccount() {
        accountManager.deleteAccount();
    }

    /**
     * Initiate the contact exchange. When the execution of this method was finished, {@code performContactExchange} can be called.
     *
     * @param callback The {@code ContactExchangeCallback} to be called when the initial setup was performed.
     * @see ContactExchangeCallback
     * @see #performContactExchange(String)
     */
    public void initiateContactExchange(ContactExchangeCallback callback) {
        contactExchangeCallback = callback;
        keyAgreementTask = keyAgreementTaskProvider.get();
        keyAgreementTask.listen();
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public PluginManager getPluginManager() {
        return pluginManager;
    }

    public Provider<KeyAgreementTask> getKeyAgreementTaskProvider() {
        return this.keyAgreementTaskProvider;
    }

    public PayloadEncoder getPayloadEncoder() {
        return this.payloadEncoder;
    }

    public PayloadParser getPayloadParser() {
        return this.payloadParser;
    }

    /**
     * Perform the contact exchange. Call {@code initiateContactExchange} before calling this method.
     *
     * @param payload The remote contact and transport information, coded into a string
     * @see #initiateContactExchange(ContactExchangeCallback)
     */
    public void performContactExchange(String payload) {
        if (payload != null) {
            try {
                byte[] bytes = ContactExchangeUtil.stringToByteArray(payload);
                Payload remotePayload = payloadParser.parse(bytes);
                keyAgreementTask.connectAndRunProtocol(remotePayload);
            } catch (IOException e) {
            }
        }
    }

    /**
     * Get a collection of all Briar-contacts of the current user.
     *
     * @return The Briar-contacts
     */
    public Collection<Contact> getContacts() throws NoContactsFoundException {
        try {
            return contactManager.getContacts();
        } catch (DbException e) {
            e.printStackTrace();
            throw new NoContactsFoundException();
        }
    }

    /**
     * Create a private Briar-group.
     *
     * @param name     The name of the group
     * @param contacts The contacts to be added to the group
     * @return The {@code PrivateGroup}-object representing the just created group
     */
    public PrivateGroup createGroup(String name, Collection<Contact> contacts) throws CantCreateGroupException {
        try {
            LocalAuthor author = identityManager.getLocalAuthor();
            PrivateGroup group = privateGroupFactory.createPrivateGroup(name, author);
            GroupMessage joinMessage = groupMessageFactory.createJoinMessage(group.getId(), System.currentTimeMillis(), author);
            privateGroupManager.addPrivateGroup(group, joinMessage, true);

            long timestamp = System.currentTimeMillis();
            for (Contact contact :
                    contacts) {
                connectToContact(contact); // TODO Maybe has to be removed in the future
                byte[] signature = groupInvitationFactory.signInvitation(contact, group.getId(), timestamp, author.getPrivateKey());
                groupInvitationManager.sendInvitation(group.getId(), contact.getId(), null, timestamp, signature, 1000000L);
            }
            return group;
        } catch (DbException e) {
            e.printStackTrace();
            throw new CantCreateGroupException();
        }
    }

    public void addContactToGroup(PrivateGroup group, Contact contact) {
        try {
            LocalAuthor author = identityManager.getLocalAuthor();
            long timestamp = System.currentTimeMillis();
            //connectToContact(contact);
            byte[] signature = groupInvitationFactory.signInvitation(contact, group.getId(), timestamp, author.getPrivateKey());
            groupInvitationManager.sendInvitation(group.getId(), contact.getId(), null, timestamp, signature, 1000000L);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get all private Briar-groups the current user is in.
     *
     * @return All private Briar-groups
     */
    public Collection<PrivateGroup> getGroups() throws GroupNotFoundException {
        try {
            return privateGroupManager.getPrivateGroups();
        } catch (DbException e) {
            e.printStackTrace();
            throw new GroupNotFoundException();
        }
    }

    /**
     * Get all message headers for a specific contact.
     *
     * @param contactId The {@code ContactId} of the contact
     * @return All message headers
     * @see #getMessageText(MessageId) to get the content
     */
    public Collection<ConversationMessageHeader> getMessageHeaders(ContactId contactId) throws CantReceiveInvitationsException {
        try {
            return conversationManager.getMessageHeaders(contactId);
        } catch (DbException e) {
            e.printStackTrace();
            throw new CantReceiveInvitationsException();
        }
    }

    /**
     * Get all message headers in a private group.
     *
     * @param group The private group
     * @return All message headers
     * @see #getGroupMessageText(MessageId, String) to get the content
     */
    public Collection<GroupMessageHeader> getMessageHeaders(PrivateGroup group) throws NoGroupHeadersFoundException {
        try {
            return privateGroupManager.getHeaders(new GroupId(group.getId().getBytes()));
        } catch (DbException e) {
            e.printStackTrace();
            throw new NoGroupHeadersFoundException(e, group.getId().toString());
        }
    }

    /**
     * Get the text in a private message.
     *
     * @param messageId The id of the message
     * @return The message content
     */
    public String getMessageText(MessageId messageId) {
        try {
            return messagingManager.getMessageText(messageId);
        } catch (DbException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get the text in a group message.
     *
     * @param messageId The id of the message
     * @return The message content
     */
    public String getGroupMessageText(MessageId messageId, String groupId) throws NoGroupMessageTextFoundException {
        try {
            return privateGroupManager.getMessageText(messageId);
        } catch (DbException e) {
            e.printStackTrace();
            throw new NoGroupMessageTextFoundException(e, groupId);
        }
    }

    /**
     * Get all open group invitations for the current user.
     *
     * @return THe open group invitations
     * @see Invitation
     * @see #acceptGroupInvitation(Invitation) to accept an invitation
     * @see #rejectGroupInvitation(Invitation) to reject an invitation
     */
    public List<Invitation> getGroupInvitations() throws NoContactsFoundException {
        Collection<Contact> contacts = getContacts();
        Map<ContactId, Collection<ConversationMessageHeader>> messages = new HashMap<>();

        for (Contact contact : contacts) {
            try {
                messages.put(contact.getId(), getMessageHeaders(contact.getId()));
            } catch (CantReceiveInvitationsException exception) {
                Log.d(TAG, "Cant get MessageHeader for contact: " + contact.getId());
            }
        }

        List<Invitation> groupInvitations = new ArrayList<>();

        // For all messages by all contacts
        for (Map.Entry<ContactId, Collection<ConversationMessageHeader>> messagesForContact : messages.entrySet()) {
            // ID of current contact
            ContactId contactId = messagesForContact.getKey();

            // For all messages by this contact
            for (ConversationMessageHeader message : messagesForContact.getValue()) {

                PrivateGroup group = message.accept(groupInvitationVisitor);
                // If the current message is a group invitation
                if (group != null) {
                    groupInvitations.add(new Invitation(contactId, group));
                }

            }

        }

        return groupInvitations;
    }

    /**
     * Accept a group invitation.
     *
     * @param invitation The invitation to accept
     */
    public void acceptGroupInvitation(Invitation invitation) {
        try {
            groupInvitationManager.respondToInvitation(invitation.getContactId(), invitation.getPrivateGroup(), true);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reject a group invitation.
     *
     * @param invitation The invitation to reject
     */
    public void rejectGroupInvitation(Invitation invitation) {
        try {
            groupInvitationManager.respondToInvitation(invitation.getContactId(), invitation.getPrivateGroup(), false);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the members of a private group.
     *
     * @param group The private group to get the members from
     * @return The members of the group
     */
    public Collection<GroupMember> getGroupMembers(PrivateGroup group) throws CantCreateGroupException {
        try {
            return privateGroupManager.getMembers(group.getId());
        } catch (DbException e) {
            e.printStackTrace();
            throw new CantCreateGroupException();
        }
    }


    /**
     * Get the current author.
     *
     * @return The current author
     */
    public LocalAuthor getLocalAuthor() {

        try {
            return identityManager.getLocalAuthor();
        } catch (DbException e) {
            e.printStackTrace();
            return localAuthor;
        }
    }

    public Long getLocalAuthorId() {

        LocalAuthor briarLocalAuthor = getLocalAuthor();

        if (briarLocalAuthor != null) {
            return Util.bytesToLong(briarLocalAuthor.getId().getBytes());
        } else {
            dhbw.smartmoderation.data.model.LocalAuthor localAuthor = localAuthorDao.loadAll().get(0);
            return localAuthor.getLocalAuthorId();
        }
    }

    /**
     * Send a message to a private group.
     *
     * @param data  The {@code String} to send as the message payload
     * @param group The private group to send the message to
     */
    public void sendToGroup(String data, PrivateGroup group) {
        try {
            GroupId groupId = group.getId();
            long timestamp = System.currentTimeMillis();
            MessageTracker.GroupCount count = privateGroupManager.getGroupCount(groupId);
            long lastTimestamp = count.getLatestMsgTime();
            if (lastTimestamp >= timestamp) {
                timestamp = lastTimestamp + 1;
            }
            GroupMessage message = groupMessageFactory.createGroupMessage(groupId, timestamp, null, getLocalAuthor(), data,
                    privateGroupManager.getPreviousMsgId(groupId));
            privateGroupManager.addLocalMessage(message);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    void exchangeContacts(KeyAgreementResult result) throws IOException, DbException {
        TransportId transportId = result.getTransportId();
        DuplexTransportConnection connection = result.getConnection();
        SecretKey masterKey = result.getMasterKey();
        boolean wasAlice = result.wasAlice();
        Contact contact = contactExchangeManager.exchangeContacts(connection, masterKey, wasAlice, true);
        connectionManager.manageOutgoingConnection(contact.getId(), transportId, connection);
    }

    ContactExchangeCallback getContactExchangeCallback() {
        return contactExchangeCallback;
    }

    private void startLifecycleManager() {
        SecretKey dbKey = accountManager.getDatabaseKey();
        if (dbKey == null) throw new AssertionError();
        lifecycleManager.startServices(dbKey);
        try {
            lifecycleManager.waitForStartup();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Does currently not work, maybe has to be removed in the future.
     * <p>
     * This is an attempt in reconnecting to a user added via Bluetooth, but a bug in Briar currently prevents it form working.
     *
     * @param contact The contact to connect to
     */
    // TODO Is maybe useless in the future, since it will be done automatically
    private void connectToContact(Contact contact) {
        Collection<DuplexPlugin> duplexPlugins = pluginManager.getDuplexPlugins();
        DuplexPlugin bluetoothPlugin = null;
        DuplexPlugin torPlugin = null;
        for (DuplexPlugin duplexPlugin : duplexPlugins) {
            if (duplexPlugin.getId().equals(BluetoothConstants.ID)) {
                bluetoothPlugin = duplexPlugin;
            }
            if (duplexPlugin.getId().equals(TorConstants.ID)) {
                torPlugin = torPlugin;
            }
        }
        if (bluetoothPlugin == null)
            throw new AssertionError("Bluetooth Plugin should have been found to connect");

        if (!connectionRegistry.isConnected(contact.getId(), bluetoothPlugin.getId())) {
            try {
                TransportProperties transportProperties = transportPropertyManager.getRemoteProperties(contact.getId(), bluetoothPlugin.getId());
                DuplexTransportConnection connection = bluetoothPlugin.createConnection(transportProperties);
                try {
                    connectionManager.manageOutgoingConnection(contact.getId(), bluetoothPlugin.getId(), connection);
                    Log.d(TAG, "Connected to contact with id: " + contact.getId().getInt());
                } catch (NullPointerException e) {
                    // Happens if contact is not available
                    // At the moment: Happens all the time
                    Log.d(TAG, "Could not connect to contact with id: " + contact.getId().getInt());
                }
            } catch (DbException e) {
                e.printStackTrace();
            }
        }
    }

    public void createAndStoreMessage(String message, PrivateGroup group) {
        try {
            LocalAuthor author = identityManager.getLocalAuthor();
            GroupId groupId = group.getId();
            MessageId previousMessageId = privateGroupManager.getPreviousMsgId(groupId);
            MessageTracker.GroupCount count = privateGroupManager.getGroupCount(groupId);
            long timestamp = count.getLatestMsgTime();
            timestamp = max(clock.currentTimeMillis(), timestamp + 1);
            createMessage(message, timestamp, author, groupId, previousMessageId);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    private void createMessage(String text, long timestamp, LocalAuthor author, GroupId groupId, MessageId previousMsgId) {
        GroupMessage msg = groupMessageFactory.createGroupMessage(groupId, timestamp, null, author, text, previousMsgId);
        try {
            privateGroupManager.addLocalMessage(msg);
        } catch (DbException e) {
            e.printStackTrace();
            //TODO handling
        }
    }

    public Collection<String> getMessages(PrivateGroup group)
            throws NoGroupMessageTextFoundException {
        try {
            GroupId groupId = group.getId();
            Collection<GroupMessageHeader> headers = privateGroupManager.getHeaders(groupId);
            Collection<String> items = new ArrayList<>();

            for (GroupMessageHeader header : headers) {
                items.add(loadItem(header, groupId));
            }
            return items;
        } catch (DbException e) {
            e.printStackTrace();
            //TODO handle exception
        }
        return new ArrayList<>();
    }

    private String loadItem(GroupMessageHeader header, GroupId groupId)
            throws NoGroupMessageTextFoundException {
        if (header instanceof JoinMessageHeader) {
            return "";
        } else {
            try {
                return privateGroupManager.getMessageText(header.getId());
            } catch (DbException e) {
                throw new NoGroupMessageTextFoundException(e, new String(groupId.getBytes()));
            }
        }
    }

    public void removePrivateGroup(PrivateGroup group) {
        try {
            privateGroupManager.removePrivateGroup(group.getId());
        } catch (DbException e) {
            e.printStackTrace();
        }
    }
}
