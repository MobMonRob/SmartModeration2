package dhbw.smartmoderation;

import org.briarproject.bramble.BrambleApplication;

public interface SmartModerationApplication extends BrambleApplication {

    SmartModerationComponent getSmartModerationComponent();

    boolean isRunningInBackground();

    boolean isInstrumentationTest();
}
