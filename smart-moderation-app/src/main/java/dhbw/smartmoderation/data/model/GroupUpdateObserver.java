package dhbw.smartmoderation.data.model;

import dhbw.smartmoderation.data.events.GroupUpdateEvent;

public interface GroupUpdateObserver {

    void update(GroupUpdateEvent event);

}
