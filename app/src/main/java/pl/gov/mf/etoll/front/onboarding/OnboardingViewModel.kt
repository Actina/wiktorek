package pl.gov.mf.etoll.front.onboarding

import pl.gov.mf.etoll.base.BaseDatabindingViewModel
import pl.gov.mf.etoll.front.onboarding.viewpager.OnboardingPage
import pl.gov.mf.etoll.storage.settings.SettingsUC
import javax.inject.Inject

class OnboardingViewModel : BaseDatabindingViewModel() {

    @Inject
    lateinit var readSettingsUseCase: SettingsUC.ReadSettingsUseCase

    fun getPages(showSentTutorial: Boolean?): List<OnboardingPage> =
        showSentTutorial?.let {
            if (showSentTutorial)
                OnboardingPages.sentPages
            else
                OnboardingPages.heavyPages
        } ?: OnboardingPages.heavyPages
}