package dhbw.smartmoderation.connection.synchronization;

import java.util.ArrayList;
import java.util.Collection;

public class PullEvent {
    private Collection<SynchronizableDataType> SynchronizableDataTypeCollection = new ArrayList<SynchronizableDataType>();

    public PullEvent(Collection<SynchronizableDataType> synchronizableDataTypeCollection){
        for (SynchronizableDataType type: synchronizableDataTypeCollection){
            addDistictSynchronizableDataType(type);
        }
    }
    public PullEvent(){}

    public void addDistictSynchronizableDataType(SynchronizableDataType data){
        if (!SynchronizableDataTypeCollection.contains(data)){
            SynchronizableDataTypeCollection.add(data);
        }
    }

    public Collection<SynchronizableDataType> getDataTypes(){
        return SynchronizableDataTypeCollection;
    }

}
