package dhbw.smartmoderation.util.Mocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import dhbw.smartmoderation.data.model.Group;
import dhbw.smartmoderation.data.model.Meeting;
import dhbw.smartmoderation.data.model.Member;
import dhbw.smartmoderation.exceptions.GroupNotFoundException;
import dhbw.smartmoderation.group.detail.DetailGroupController;

public class DetailGroupControllerMock extends DetailGroupController {

    public Group Group;
    public Member Member1;
    public Member Member2;

    public Meeting Meeting1;
    public Meeting Meeting2;


    public DetailGroupControllerMock(Long groupId) {
        super(groupId);
        InitDatabase();
    }

    private void InitDatabase() {

    }

    @Override
    public Collection<Member> getMembers() throws GroupNotFoundException {
        return new ArrayList<Member>(Arrays.asList(new Member[]{Member1,Member2, Member2, Member1}));
    }

    @Override
    public Collection<Meeting> getMeetings() throws GroupNotFoundException {
        return new ArrayList<Meeting>(Arrays.asList(new Meeting[]{Meeting1,Meeting2, Meeting2, Meeting1, Meeting2}));
    }

    @Override
    public Group getGroup() throws GroupNotFoundException {
        return Group;
    }
}
