package pl.gov.mf.etoll.base

interface BaseComponent<VIEWMODEL : BaseViewModel> {
    fun viewModel(): VIEWMODEL
    fun inject(target: VIEWMODEL)
}