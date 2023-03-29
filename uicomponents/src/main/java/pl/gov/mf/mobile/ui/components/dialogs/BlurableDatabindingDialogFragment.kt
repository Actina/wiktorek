package pl.gov.mf.mobile.ui.components.dialogs

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import io.reactivex.disposables.CompositeDisposable
import pl.gov.mf.etoll.app.BaseApplication
import pl.gov.mf.etoll.appmode.AppMode
import pl.gov.mf.etoll.appmode.AppModeManagerUC
import pl.gov.mf.mobile.ui.components.utils.BlurDelegate
import pl.gov.mf.mobile.utils.addSafe
import pl.gov.mf.mobile.utils.disposeSafe

abstract class BlurableDatabindingDialogFragment : DialogFragment() {

    private lateinit var getCurrentAppModeUseCase: AppModeManagerUC.GetCurrentAppModeUseCase
    private var compositeDisposable: CompositeDisposable? = null
    private var currentMode: AppMode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // get use case about dark mode changes
        if (requireContext().applicationContext is BaseApplication)
            getCurrentAppModeUseCase =
                (requireContext().applicationContext as BaseApplication).getApplicationComponent()
                    .useCaseGetCurrentAppMode()
        else
            throw IllegalStateException("BlurableDialog used outside nkspo ecosystem")
    }

    override fun onPause() {
        super.onPause()
        // stop listening to dark mode changes
        compositeDisposable.disposeSafe()
        compositeDisposable = null
    }

    override fun onResume() {
        super.onResume()
        // start listening to dark mode changes
        compositeDisposable = CompositeDisposable()
        compositeDisposable.addSafe(
            getCurrentAppModeUseCase.executeObservation().subscribe { mode ->
                if (currentMode == null || currentMode != mode) {
                    currentMode = mode
                    onAppModeChanged(mode)
                }
            })
    }

    private fun onAppModeChanged(mode: AppMode) {
        // mode left for "if" in the future
        getBindings()?.invalidateAll()
        view?.invalidate()
        invalidateViewAfterModeChange()
    }

    open fun invalidateViewAfterModeChange() {

    }

    abstract fun getBindings(): ViewDataBinding?

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let { act ->
            if (act is BlurDelegate)
                act.showBlur()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        activity?.let { act ->
            if (act is BlurDelegate)
                act.hideBlur()
        }
    }
}
