package dhbw.smartmoderation.util.Mocks;

import java.util.ArrayList;
import java.util.Collection;
import dhbw.smartmoderation.data.events.GroupUpdateEvent;
import dhbw.smartmoderation.data.model.Group;
import dhbw.smartmoderation.group.overview.OverviewGroupController;

public class OverViewGroupControllerMock extends OverviewGroupController {

    public final Collection<Group> Groups = new ArrayList<Group>();
    public Group Group1;
    public Group Group2;

    public OverViewGroupControllerMock(){
        super();
        InitDatabase();
    }

    private void InitDatabase() {


    }

    @Override
    public Collection<Group> getGroups() {
        return Groups;
    }
}
