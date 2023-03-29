package pl.gov.mf.etoll.front.dashboard

import androidx.navigation.NavController
import pl.gov.mf.etoll.MainNavgraphDirections
import pl.gov.mf.etoll.databinding.FragmentDashboardBinding
import pl.gov.mf.etoll.security.checker.SecuritySanityCheckerUseCase
import javax.inject.Inject

class DashboardFlavourDelegateImpl @Inject constructor(private val signAndroidComponentUseCase: SecuritySanityCheckerUseCase.SignAndroidComponentUseCase) :
    DashboardFlavourDelegate {
    override fun initializeView(binding: FragmentDashboardBinding, navController: NavController) {
        binding.dashboardDemo.root.setOnClickListener {
            navController.navigate(
                MainNavgraphDirections.actionShowDemo(
                    signAndroidComponentUseCase.executeCustomSign()
                )
            )
        }
    }

    override fun shouldShowGpsChangeSwitch(): Boolean = true
}