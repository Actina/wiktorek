package pl.gov.mf.etoll.front

import androidx.databinding.ViewDataBinding
import pl.gov.mf.etoll.base.BaseMVVMFragment
import pl.gov.mf.etoll.base.BaseDatabindingViewModel

abstract class BaseDatabindingFragment<VIEWMODEL : BaseDatabindingViewModel, COMPONENT> :
    BaseMVVMFragment<VIEWMODEL, COMPONENT>() {
    open fun getBindings(): ViewDataBinding? = null

    override fun onResume() {
        super.onResume()
        viewModel.invalidateBindings.observe(this) {
            getBindings()?.invalidateAll()
            invalidateViewAfterModeChange()
        }
    }

    open fun invalidateViewAfterModeChange() {

    }
}