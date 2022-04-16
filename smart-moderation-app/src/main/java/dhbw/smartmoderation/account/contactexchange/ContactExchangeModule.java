package dhbw.smartmoderation.account.contactexchange;

import androidx.lifecycle.ViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public abstract class ContactExchangeModule {
    //class not needed?
    @Binds
    @IntoMap
    @ViewModelKey(ContactExchangeViewModel.class)
    public abstract ViewModel bindContactExchangeViewModel (ContactExchangeViewModel contactExchangeViewModel);

}
