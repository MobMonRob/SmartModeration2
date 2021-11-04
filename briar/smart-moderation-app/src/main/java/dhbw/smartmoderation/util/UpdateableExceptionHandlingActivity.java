package dhbw.smartmoderation.util;

import android.os.Handler;

import java.util.ArrayList;
import java.util.Collection;

import dhbw.smartmoderation.connection.synchronization.PullEvent;
import dhbw.smartmoderation.connection.synchronization.SynchronizableDataType;

public abstract class UpdateableExceptionHandlingActivity extends ExceptionHandlingActivity {

    private final Handler handler = new Handler();

    public abstract Collection<SynchronizableDataType> getSynchronizableDataTypes();

    public void onPullUpdate(PullEvent event){
        Collection<SynchronizableDataType> thisActivityDataTypes = this.getSynchronizableDataTypes();
        for (SynchronizableDataType dataType : event.getDataTypes()){
            if (thisActivityDataTypes.contains(dataType)){
                handler.post(() -> updateUI());
                return;
            }
        }
    }

    protected abstract void updateUI();
}
