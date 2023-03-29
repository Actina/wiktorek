package pl.gov.mf.etoll.front.critical
// FYI: this is left to be fixed after rush - there is small error somewhere in DI option
//import androidx.lifecycle.Lifecycle
//import androidx.lifecycle.ViewModelProvider
//import androidx.lifecycle.ViewModelStoreOwner
//import pl.gov.mf.etoll.base.BaseComponent
//import dagger.Module
//import dagger.Provides
//import dagger.Subcomponent
//import javax.inject.Scope
//
//@CritErrorFragmentScope
//@Subcomponent(modules = [CritErrorFragmentModule::class])
//interface CritErrorFragmentComponent :
//    BaseComponent<CritErrorFragmentViewModel> {
//    fun inject(target: CritErrorFragment)
//}
//
//@Module
//class CritErrorFragmentModule(
//    private val viewModelStoreOwner: ViewModelStoreOwner,
//    private val lifecycle: Lifecycle,
//    private val errorTypeIn: Int
//) {
//
//    @Provides
//    @CritErrorFragmentScope
//    fun providesViewModel(): CritErrorFragmentViewModel =
//        ViewModelProvider(viewModelStoreOwner).get(CritErrorFragmentViewModel::class.java)
//            .apply {
//                lifecycle.addObserver(this)
//                errorType = errorTypeIn
//            }
//}
//
//@Scope
//@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
//annotation class CritErrorFragmentScope