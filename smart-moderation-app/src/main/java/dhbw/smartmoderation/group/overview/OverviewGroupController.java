package dhbw.smartmoderation.group.overview;

import org.briarproject.briar.api.privategroup.PrivateGroup;

import java.util.ArrayList;
import java.util.Collection;

import dhbw.smartmoderation.controller.SmartModerationController;
import dhbw.smartmoderation.data.model.Group;
import dhbw.smartmoderation.exceptions.GroupNotFoundException;

public class OverviewGroupController extends SmartModerationController {

    public Collection<Group> getGroups() {
        return dataService.getGroups();
    }

    public void update() {

        Collection<PrivateGroup> groups = new ArrayList<>();

        try {
            groups = connectionService.getGroups();

        } catch (GroupNotFoundException e) {
            e.printStackTrace();
        }

        for(PrivateGroup privateGroup : groups) {
            synchronizationService.pull(privateGroup);
        }
    }
}
