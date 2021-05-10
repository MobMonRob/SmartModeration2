package dhbw.smartmoderation.group.create;

import android.content.Context;
import android.graphics.Color;

import org.briarproject.bramble.api.contact.Contact;
import org.briarproject.briar.api.privategroup.GroupMember;
import org.briarproject.briar.api.privategroup.PrivateGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.controller.SmartModerationController;
import dhbw.smartmoderation.data.model.ConsensusLevel;
import dhbw.smartmoderation.data.model.Ghost;
import dhbw.smartmoderation.data.model.Group;
import dhbw.smartmoderation.data.model.GroupSettings;
import dhbw.smartmoderation.data.model.IContact;
import dhbw.smartmoderation.data.model.Member;
import dhbw.smartmoderation.data.model.MemberGroupRelation;
import dhbw.smartmoderation.data.model.ModelClass;
import dhbw.smartmoderation.data.model.Role;
import dhbw.smartmoderation.exceptions.CantCreateGroupException;
import dhbw.smartmoderation.exceptions.NoContactsFoundException;

public class CreateGroupController extends SmartModerationController {

    Context context;

    public CreateGroupController(Context context) {

        this.context = context;

    }

    public Collection<IContact> getContacts() throws NoContactsFoundException{

        ArrayList<IContact> newContactList = new ArrayList<>();
        Collection<Contact> oldContactList = connectionService.getContacts();


        for(Contact contact : oldContactList) {

            dhbw.smartmoderation.data.model.Contact newContact = new  dhbw.smartmoderation.data.model.Contact(contact);
            newContactList.add(newContact);
        }


        return newContactList;
    }

    public Collection<ConsensusLevel> createStandardConsensusLevels() {

        Collection<ConsensusLevel> consensusLevels = new ArrayList<>();

        ConsensusLevel approval = new ConsensusLevel();
        approval.setNumber(1);
        approval.setColor(Color.parseColor("#009900"));
        approval.setName(context.getString(R.string.vorbehaltloseZustimmung));
        approval.setDescription(context.getString(R.string.vorbehaltloseZustimmungBeschreibung));
        consensusLevels.add(approval);

        ConsensusLevel slightConcerns = new ConsensusLevel();
        slightConcerns.setNumber(2);
        slightConcerns.setColor(Color.parseColor("#00ff00"));
        slightConcerns.setName(context.getString(R.string.leichteBedenken));
        slightConcerns.setDescription(context.getString(R.string.leichteBedenkenBeschreibung));
        consensusLevels.add(slightConcerns);

        ConsensusLevel abstention = new ConsensusLevel();
        abstention.setNumber(3);
        abstention.setColor(Color.parseColor("#ffff00"));
        abstention.setName(context.getString(R.string.enthaltung));
        abstention.setDescription(context.getString(R.string.enthaltungBeschreibung));
        consensusLevels.add(abstention);

        ConsensusLevel standAside = new ConsensusLevel();
        standAside.setNumber(4);
        standAside.setColor(Color.parseColor("#ff9900"));
        standAside.setName(context.getString(R.string.beiseiteStehen));
        standAside.setDescription(context.getString(R.string.beiseiteStehenBeschreibung));
        consensusLevels.add(standAside);

        ConsensusLevel seriousConcerns = new ConsensusLevel();
        seriousConcerns.setNumber(5);
        seriousConcerns.setColor(Color.parseColor("#fa0404"));
        seriousConcerns.setName(context.getString(R.string.schwereBedenken));
        seriousConcerns.setDescription(context.getString(R.string.schwereBedenkenBeschreibung));
        consensusLevels.add(seriousConcerns);

        ConsensusLevel veto = new ConsensusLevel();
        veto.setNumber(6);
        veto.setColor(Color.parseColor("#990000"));
        veto.setName(context.getString(R.string.veto));
        veto.setDescription(context.getString(R.string.vetoBeschreibung));
        consensusLevels.add(veto);

        return consensusLevels;
    }

    public void createGroup(String name, Collection<IContact> contacts) throws CantCreateGroupException {

        ArrayList<Contact> newContactList = new ArrayList<>();

        ArrayList<Ghost> ghostList = new ArrayList<>();

        for(IContact contact : contacts) {

            if (contact instanceof  dhbw.smartmoderation.data.model.Contact) {

                newContactList.add(((dhbw.smartmoderation.data.model.Contact)contact).getBriarContact());
            }

            else {

                ghostList.add((Ghost)contact);
            }
        }

        PrivateGroup privateGroup = connectionService.createGroup(name, newContactList);
        Collection<GroupMember> groupMembers = connectionService.getGroupMembers(privateGroup);

        Group group = new Group(privateGroup);
        group.setName(name);

        GroupSettings groupSettings = new GroupSettings();
        group.setGroupSettings(groupSettings);
        dataService.mergeGroupSettings(groupSettings);
        dataService.mergeGroup(group);

        List<ConsensusLevel> consensusLevels = new ArrayList<>();

        for(ConsensusLevel consensusLevel : createStandardConsensusLevels()) {

            consensusLevel.setGroupSettings(groupSettings);
            dataService.mergeConsensusLevel(consensusLevel);
            consensusLevels.add(consensusLevel);
        }

        List<Member> members = new ArrayList<>();

        for(GroupMember groupMember : groupMembers) {

            Member member = new Member(groupMember.getAuthor());
            member.setName(groupMember.getAuthor().getName());
            members.add(member);
            dataService.saveMember(member);

            if(groupMember.getAuthor().getId().equals(connectionService.getLocalAuthor().getId())) {

                MemberGroupRelation memberGroupRelation = group.addMember(member, Role.MODERATOR);
                dataService.saveMemberGroupRelation(memberGroupRelation);
            }

            MemberGroupRelation memberGroupRelation = group.addMember(member, Role.PARTICIPANT);
            dataService.saveMemberGroupRelation(memberGroupRelation);
        }

        for(Ghost ghost : ghostList) {

            Member member = new Member(ghost);
            members.add(member);
            dataService.saveMember(member);
            MemberGroupRelation memberGroupRelation = group.addMember(member, Role.SPECTATOR);
            dataService.saveMemberGroupRelation(memberGroupRelation);
        }

        ArrayList<ModelClass> pushData = new ArrayList<>();

        pushData.add(group);
        pushData.add(groupSettings);
        pushData.addAll(consensusLevels);
        pushData.addAll(members);
        synchronizationService.push(privateGroup, pushData);
    }
}
