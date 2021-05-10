package dhbw.smartmoderation.group.settings;

import org.briarproject.briar.api.privategroup.PrivateGroup;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import dhbw.smartmoderation.SmartModerationApplication;
import dhbw.smartmoderation.controller.SmartModerationController;
import dhbw.smartmoderation.data.model.ConsensusLevel;
import dhbw.smartmoderation.data.model.Group;
import dhbw.smartmoderation.data.model.GroupSettings;
import dhbw.smartmoderation.data.model.ModelClass;
import dhbw.smartmoderation.exceptions.ConsensusLevelsNotFoundException;
import dhbw.smartmoderation.exceptions.CouldNotAddConsensusLevel;
import dhbw.smartmoderation.exceptions.CouldNotChangeConsensusLevel;
import dhbw.smartmoderation.exceptions.CouldNotDeleteConsensusLevel;
import dhbw.smartmoderation.exceptions.GroupNotFoundException;
import dhbw.smartmoderation.exceptions.GroupSettingsNotFoundException;
import dhbw.smartmoderation.util.Mocks.ISettings;
import dhbw.smartmoderation.util.Util;

public class SettingsController extends SmartModerationController  {

    private Long groupId;

    public SettingsController(Long groupId) {
        this.groupId = groupId;
    }

    public void update() {

        try {

            this.synchronizationService.pull(getPrivateGroup());

        } catch (GroupNotFoundException e) {
            e.printStackTrace();
        }
    }

    public PrivateGroup getPrivateGroup() throws GroupNotFoundException{

        Collection<PrivateGroup> privateGroups = connectionService.getGroups();

        for(PrivateGroup group : privateGroups) {

            if(groupId.equals(Util.bytesToLong(group.getId().getBytes()))) {

                return group;
            }
        }

        throw new GroupNotFoundException();
    }

    public Group getGroup() throws GroupNotFoundException {

        try {

            return dataService.getGroup(groupId);

        } catch (GroupNotFoundException e) {

            e.printStackTrace();
            throw new GroupNotFoundException();
        }
    }

    public GroupSettings getGroupSettings() throws GroupSettingsNotFoundException {

        try {

            return getGroup().getGroupSettings();

        } catch (GroupNotFoundException e) {

            e.printStackTrace();
            throw new GroupSettingsNotFoundException();
        }

    }

    public Collection<ConsensusLevel> getConsensusLevels() throws ConsensusLevelsNotFoundException  {

        GroupSettings groupSettings = null;

        try {

            groupSettings = getGroupSettings();

            groupSettings = dataService.getGroupSetting(groupSettings.getSettingsId());

            return groupSettings.getConsensusLevels();

        } catch (GroupSettingsNotFoundException e) {

            e.printStackTrace();
            throw new ConsensusLevelsNotFoundException();
        }

    }

    public ConsensusLevel createConsensusLevel(String name, int color, String description, int number) {

        ConsensusLevel consensusLevel = new ConsensusLevel();
        consensusLevel.setNumber(number);
        consensusLevel.setName(name);
        consensusLevel.setColor(color);
        consensusLevel.setDescription(description);

        try {
            consensusLevel.setGroupSettings(getGroupSettings());

        } catch (GroupSettingsNotFoundException e) {

            e.printStackTrace();
        }
        dataService.mergeConsensusLevel(consensusLevel);

        return consensusLevel;
    }

    public void addConsensusLevel(ConsensusLevel consensusLevel) throws CouldNotAddConsensusLevel {

        PrivateGroup group;

        try{

            group = getPrivateGroup();

        } catch(GroupNotFoundException exception){

            throw new CouldNotAddConsensusLevel();
        }

        Collection<ModelClass> pushData = new ArrayList<>();
        pushData.add(consensusLevel);
        synchronizationService.push(group, pushData);
    }

    public void changeConsensusLevel(ConsensusLevel consensusLevel) throws CouldNotChangeConsensusLevel {

        PrivateGroup group;

        try{

            group = getPrivateGroup();

        } catch (GroupNotFoundException exception){

            throw new CouldNotChangeConsensusLevel();
        }

        Collection<ModelClass> pushData = new ArrayList<>();
        pushData.add(consensusLevel);
        synchronizationService.push(group, pushData);

    }

    public void changeConsensusLevelNumbering(Collection<ConsensusLevel> consensusLevels) throws CouldNotChangeConsensusLevel {

        PrivateGroup group;

        try {

            group = getPrivateGroup();

        } catch (GroupNotFoundException exception) {

            throw new CouldNotChangeConsensusLevel();
        }

        Collection<ModelClass> pushData = new ArrayList<>();

        for (ConsensusLevel consensusLevel : consensusLevels) {

            dataService.saveConsensusLevel(consensusLevel);
            pushData.add(consensusLevel);
        }

        synchronizationService.push(group, pushData);

    }

    public void deleteConsensusLevel(Long consensusLevelId) throws CouldNotDeleteConsensusLevel {

        PrivateGroup group;

        try {

            group = getPrivateGroup();

        } catch (GroupNotFoundException groupNotFoundException) {

            throw new CouldNotDeleteConsensusLevel();
        }

        ConsensusLevel consensusLevel = dataService.getConsensusLevel(consensusLevelId);
        dataService.deleteConsensusLevel(consensusLevel);

        Collection<ModelClass> data = new ArrayList<>();
        consensusLevel.setIsDeleted(true);
        data.add(consensusLevel);
        synchronizationService.push(group, data);
    }
}
