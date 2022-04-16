package dhbw.smartmoderation.connection.synchronization;

import java.util.ArrayList;
import java.util.Collection;

public class PullEvent {
    private final Collection<SynchronizableDataType> SynchronizableDataTypeCollection = new ArrayList<>();

    public PullEvent(){}

    public Collection<SynchronizableDataType> getDataTypes(){
        return SynchronizableDataTypeCollection;
    }
}
