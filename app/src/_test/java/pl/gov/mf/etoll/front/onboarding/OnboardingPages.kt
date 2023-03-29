package pl.gov.mf.etoll.front.onboarding

import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.front.onboarding.viewpager.OnboardingPage

class OnboardingPages {
    companion object {
        val heavyPages = listOf(
            OnboardingPage(
                "startup_onboarding_heavy_page1_header",
                "startup_onboarding_heavy_page1_content",
                "ic_onboarding_common_ride_icon"
            ),
            OnboardingPage(
                "startup_onboarding_heavy_page2_header",
                "startup_onboarding_heavy_page2_content",
                "ic_onboarding_heavy_icon2"
            ),
            OnboardingPage(
                "startup_onboarding_heavy_page3_header",
                "startup_onboarding_heavy_page3_content",
                "ic_onboarding_support_and_monitoring"
            )
        )

        val lightPages = listOf(
            OnboardingPage(
                "startup_onboarding_light_page1_header",
                "startup_onboarding_light_page1_content",
                "ic_onboarding_common_ride_icon"
            ),
            OnboardingPage(
                "startup_onboarding_light_page2_header",
                "startup_onboarding_light_page2_content",
                "ic_onboarding_light_icon2"
            ),
            OnboardingPage(
                "startup_onboarding_light_page3_header",
                "startup_onboarding_light_page3_content",
                "ic_onboarding_light_icon3"
            )
        )

        val sentPages = listOf(
            OnboardingPage(
                "startup_onboarding_sent_page1_header",
                "startup_onboarding_sent_page1_content",
                "ic_onboarding_common_ride_icon"
            ),
            OnboardingPage(
                "startup_onboarding_sent_page2_header",
                "startup_onboarding_sent_page2_content",
                "ic_onboarding_sent_icon2"
            ),
            OnboardingPage(
                "startup_onboarding_sent_page3_header",
                "startup_onboarding_sent_page3_content",
                "ic_onboarding_support_and_monitoring"
            )
        )
    }
}