package pl.gov.mf.etoll.ui.components

import dagger.Module
import dagger.Provides
import pl.gov.mf.etoll.app.di.ApplicationScope
import pl.gov.mf.mobile.ui.components.dialogs.DialogsHelper
import pl.gov.mf.mobile.ui.components.dialogs.DialogsHelperImpl

@Module
class UIComponentsModule {

    @Provides
    @ApplicationScope
    fun providesDialogsHelper(impl: DialogsHelperImpl): DialogsHelper = impl
}