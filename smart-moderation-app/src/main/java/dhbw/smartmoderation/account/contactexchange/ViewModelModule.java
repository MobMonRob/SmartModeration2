package dhbw.smartmoderation.account.contactexchange;

import androidx.lifecycle.ViewModelProvider;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;

@Module
public abstract class ViewModelModule {
    //class not needed?
    @Binds
    @Singleton
    abstract ViewModelProvider.Factory bindViewModelFactory(
            ViewModelFactory viewModelFactory);
}
