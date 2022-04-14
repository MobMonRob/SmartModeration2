package dhbw.smartmoderation.account.contactexchange;

public interface DestroyableContext {

    void runOnUiThreadUnlessDestroyed(Runnable runnable);
}
