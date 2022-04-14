package dhbw.smartmoderation.data.events;

public class GroupUpdateEvent {

    private Long groupId;

    public GroupUpdateEvent(Long groupId){
        this.groupId = groupId;
    }

    public Long getId(){
        return groupId;
    }
}
