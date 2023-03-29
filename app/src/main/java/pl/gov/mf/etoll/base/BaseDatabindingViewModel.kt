package pl.gov.mf.etoll.base

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import pl.gov.mf.etoll.translations.AppLanguageManagerUC
import pl.gov.mf.etoll.translations.TranslationsContainer
import pl.gov.mf.mobile.utils.addSafe
import javax.inject.Inject

abstract class BaseDatabindingViewModel : BaseViewModel(), LifecycleObserver {

    @Inject
    lateinit var getCurrentLocalizationUseCase: AppLanguageManagerUC.GetCurrentLanguageUseCase

    private val _invalidateBindings = MutableLiveData(false)
    val invalidateBindings: LiveData<Boolean>
        get() = _invalidateBindings

    private val _translations = MutableLiveData<TranslationsContainer>()
    val translations: LiveData<TranslationsContainer> = _translations

    override fun onResume() {
        super.onResume()
        compositeDisposable = CompositeDisposable()
        compositeDisposable.addSafe(getCurrentAppModeUseCase.executeObservation().subscribe {
            _invalidateBindings.value =
                invalidateBindings.value == true || (it != appMode.value?.appMode)
        })
    }

}